package io.development.tymo.utils;

import android.content.SharedPreferences;
import android.provider.Settings;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Calendar;

import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.network.NetworkUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FirebaseTymoIDService extends FirebaseInstanceIdService {
    private CompositeDisposable mSubscriptions;

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        mSubscriptions = new CompositeDisposable();

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        if(!email.matches("")) {

            UserPushNotification pushNotification = new UserPushNotification();
            pushNotification.setEmail(email);
            pushNotification.setIdDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            pushNotification.setName(android.os.Build.BRAND + " " + android.os.Build.MODEL);
            pushNotification.setToken(refreshedToken);
            pushNotification.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

            updatePushNotification(pushNotification);
        }
    }

    private void updatePushNotification(UserPushNotification pushNotification) {

        mSubscriptions.add(NetworkUtil.getRetrofit().setPushNotification(pushNotification)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }

    private void handleError(Throwable error) {
        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }
}
