package io.development.tymo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import io.development.tymo.R;
import io.development.tymo.model_server.AppInfoServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.network.NetworkUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

public class ForceUpdateChecker {

    private static final String TAG = ForceUpdateChecker.class.getSimpleName();

    private static final String KEY_UPDATE_REQUIRED = "force_update_required";
    private static final String KEY_CURRENT_VERSION = "force_update_current_version";
    private static final String KEY_UPDATE_URL = "force_update_store_url";
    private static final String KEY_LAST_TIME_UPDATE = "last_time_update";

    private OnUpdateNeededListener onUpdateNeededListener;
    private Context context;
    private CompositeDisposable mSubscriptions;

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String updateUrl, String version);
        void onUpdateNotNeeded();
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public ForceUpdateChecker(@NonNull Context context,
                              OnUpdateNeededListener onUpdateNeededListener,
                              CompositeDisposable mSubscriptions) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
        this.mSubscriptions = mSubscriptions;
    }

    public void check() {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.APP_INFO, MODE_PRIVATE);
        long lastTime = mSharedPreferences.getLong(KEY_LAST_TIME_UPDATE, -1);

        if(isTimeToCheck(lastTime))
            isVersionUpdated();
        else
            checkOffline();
    }

    private boolean isTimeToCheck(long lastTime){
        Calendar c1 = Calendar.getInstance();
        long currentTime = c1.getTimeInMillis();
        long diff = currentTime - lastTime;
        long hours_12 = 1000*60*60*12;

        return lastTime == -1 || diff > hours_12;
    }

    private void isVersionUpdated() {

        mSubscriptions.add(NetworkUtil.getRetrofit().getAppInfoVersionUpdate()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(AppInfoServer appInfoServer) {
        Calendar c1 = Calendar.getInstance();
        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.APP_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        editor.putLong(KEY_LAST_TIME_UPDATE, c1.getTimeInMillis());
        editor.putString(KEY_CURRENT_VERSION, appInfoServer.getVersion());
        editor.putString(KEY_UPDATE_URL, appInfoServer.getPlayStoreUrl());
        editor.putBoolean(KEY_UPDATE_REQUIRED, appInfoServer.isUpdateRequired());
        editor.apply();


        String currentVersion = appInfoServer.getVersion();
        String appVersion = getAppVersion(context);
        String updateUrl = appInfoServer.getPlayStoreUrl();
        boolean required = appInfoServer.isUpdateRequired();

        if (required) {
            if(onUpdateNeededListener != null) {
                if (appVersion.compareTo(currentVersion) < 0)
                    onUpdateNeededListener.onUpdateNeeded(updateUrl, currentVersion);
                else
                    onUpdateNeededListener.onUpdateNotNeeded();
            }
            else{
                onUpdateNeededListener.onUpdateNotNeeded();
            }
        }
    }

    private void handleError(Throwable error) {
        checkOffline();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void checkOffline(){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.APP_INFO, MODE_PRIVATE);
        String currentVersion = mSharedPreferences.getString(KEY_CURRENT_VERSION, "");
        String appVersion = getAppVersion(context);
        String updateUrl = mSharedPreferences.getString(KEY_UPDATE_URL, "");
        boolean required = mSharedPreferences.getBoolean(KEY_UPDATE_REQUIRED, true);

        if (required) {
            if(onUpdateNeededListener != null) {
                if (appVersion.compareTo(currentVersion) < 0 && isNetworkAvailable())
                    onUpdateNeededListener.onUpdateNeeded(updateUrl, currentVersion);
                else
                    onUpdateNeededListener.onUpdateNotNeeded();
            }
        }
        else{
            onUpdateNeededListener.onUpdateNotNeeded();
        }
    }

    private String getAppVersion(Context context) {
        String result = "";

        try {
            result = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public static class Builder {

        private Context context;
        private OnUpdateNeededListener onUpdateNeededListener;
        private CompositeDisposable mSubscriptions;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener, CompositeDisposable mSubscriptions) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            this.mSubscriptions = mSubscriptions;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener, mSubscriptions);
        }

        public ForceUpdateChecker check() {
            ForceUpdateChecker forceUpdateChecker = build();
            forceUpdateChecker.check();

            return forceUpdateChecker;
        }
    }
}
