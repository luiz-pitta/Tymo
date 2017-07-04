package io.development.tymo.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v13.BuildConfig;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class ForceUpdateChecker {

    private static final String TAG = ForceUpdateChecker.class.getSimpleName();

    private static final String KEY_UPDATE_REQUIRED = "force_update_required";
    private static final String KEY_CURRENT_VERSION = "force_update_current_version";
    private static final String KEY_UPDATE_URL = "force_update_store_url";

    private OnUpdateNeededListener onUpdateNeededListener;
    private Context context;

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String updateUrl, String version);
        void onUpdateNotNeeded();
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public ForceUpdateChecker(@NonNull Context context,
                              OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public void check() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        //developer mode
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();
        remoteConfig.setConfigSettings(configSettings);

        long cacheExpiration = 60*60*12; // 12 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
            cacheExpiration = 0;


        remoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        remoteConfig.activateFetched();
                        if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
                            String currentVersion = remoteConfig.getString(KEY_CURRENT_VERSION);
                            String appVersion = getAppVersion(context);
                            String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);

                            if(onUpdateNeededListener != null) {

                                if (!TextUtils.equals(currentVersion, appVersion))
                                    onUpdateNeededListener.onUpdateNeeded(updateUrl, currentVersion);
                                else
                                    onUpdateNeededListener.onUpdateNotNeeded();
                            }

                        }
                    }
                }
        });
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

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener);
        }

        public ForceUpdateChecker check() {
            ForceUpdateChecker forceUpdateChecker = build();
            forceUpdateChecker.check();

            return forceUpdateChecker;
        }
    }
}
