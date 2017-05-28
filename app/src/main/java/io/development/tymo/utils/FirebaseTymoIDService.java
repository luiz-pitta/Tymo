package io.development.tymo.utils;

import android.content.SharedPreferences;
import android.provider.Settings;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.network.NetworkUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FirebaseTymoIDService extends FirebaseInstanceIdService {
    private CompositeSubscription mSubscriptions;

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        mSubscriptions = new CompositeSubscription();

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        if(!email.matches("")) {

            UserPushNotification pushNotification = new UserPushNotification();
            pushNotification.setEmail(email);
            pushNotification.setIdDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            pushNotification.setName(android.os.Build.BRAND + " " + android.os.Build.MODEL);
            pushNotification.setToken(refreshedToken);

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
            mSubscriptions.unsubscribe();
    }

    private void handleError(Throwable error) {
        if(mSubscriptions != null)
            mSubscriptions.unsubscribe();
    }
}
