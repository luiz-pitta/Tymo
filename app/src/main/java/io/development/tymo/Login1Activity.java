package io.development.tymo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.development.tymo.activities.IntroActivity;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.activities.RegisterPart2Activity;
import io.development.tymo.fragments.FeedFragment;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateEmail;

public class Login1Activity extends AppCompatActivity implements View.OnClickListener, BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private CallbackManager callbackManager;

    private SliderLayout mDemoSlider;
    private TextView loginButton, facebookLoginButton, text;

    private SharedPreferences mSharedPreferences;
    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private LinearLayout progressBox;

    private User user;

    //teste
    private List<Integer> list1 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookSDKInitialize();
        setContentView(R.layout.activity_login_1);

        initSharedPreferences();

        text = (TextView) findViewById(R.id.text);
        loginButton = (TextView) findViewById(R.id.loginButton);
        mDemoSlider = (SliderLayout)findViewById(R.id.slider);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);
        facebookLoginButton = (TextView) findViewById(R.id.facebookLoginButton);

        loginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);

        list1.add(R.drawable.bg_slideshow_1);
        list1.add(R.drawable.bg_slideshow_2);
        list1.add(R.drawable.bg_slideshow_3);
        list1.add(R.drawable.bg_slideshow_4);
        for(Integer num : list1){
            DefaultSliderView defaultSliderView = new DefaultSliderView(this);
            defaultSliderView
                    .image(num)
                    .setOnSliderClickListener(this);
            mDemoSlider.addSlider(defaultSliderView);
        }

        String log = mSharedPreferences.getString(Constants.EMAIL, "");
        if(validateEmail(log))
            startActivity(new Intent(Login1Activity.this, MainActivity.class));
        else {
            mSubscriptions = new CompositeSubscription();
            getLoginDetails();

            initSharedPreferences();
            initDemoSlider();

            FirebaseMessaging.getInstance().unsubscribeFromTopic("Tymo");

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
        }
    }

    private void initDemoSlider(){
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setCustomIndicator((PagerIndicator) findViewById(R.id.customIndicator));
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.stopAutoCycle();
        mDemoSlider.addOnPageChangeListener(this);
        mDemoSlider.setCurrentPosition(0);
    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    private void initSharedPreferences() {

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
    }

    private void loginProcessFacebook(User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit().loginFacebook(usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void setPushNotification(UserPushNotification pushNotification) {

        mSubscriptions.add(NetworkUtil.getRetrofit().setPushNotification(pushNotification)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponsePush,this::handleError));
    }

    private void handleResponsePush(Response response) {

    }

    private void handleResponse(Response response) {

        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        User usr = response.getUser();
        String id = usr.getIdFacebook();
        String id2 = user.getIdFacebook();
        if(id.matches(id2)) {
            editor.putString(Constants.EMAIL, usr.getEmail());
            editor.putBoolean(Constants.LOGIN_TYPE, usr.getFromFacebook());
            editor.putString(Constants.USER_NAME, usr.getName());
            editor.putBoolean(Constants.LOCATION, usr.isLocationGps());
            editor.putBoolean(Constants.NOTIFICATION, usr.isNotifications());
            editor.putBoolean(Constants.NOTIFICATION_ACT, usr.isNotificationActivity());
            editor.putBoolean(Constants.NOTIFICATION_FLAG, usr.isNotificationFlag());
            editor.putBoolean(Constants.NOTIFICATION_REMINDER, usr.isNotificationReminder());
            editor.putBoolean(Constants.NOTIFICATION_PUSH, usr.isNotificationPush());
            editor.apply();

            if(token != null){
                UserPushNotification pushNotification = new UserPushNotification();
                pushNotification.setEmail(usr.getEmail());
                pushNotification.setIdDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                pushNotification.setName(android.os.Build.BRAND + " " + android.os.Build.MODEL);
                pushNotification.setToken(token);

                setPushNotification(pushNotification);
            }

            Intent intent = new Intent(this, IntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else
            Toast.makeText(this, getResources().getString(R.string.register_account_facebook_failed), Toast.LENGTH_LONG).show();

        progressBox.setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                if(response.getMessage().matches("REGISTER")) {

                    Intent register = new Intent(Login1Activity.this, RegisterPart2Activity.class);

                    UserWrapper wrapper = new UserWrapper(user);
                    register.putExtra("user_wrapper", wrapper);

                    startActivity(register);
                    overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
                    progressBox.setVisibility(View.GONE);
                }
                else {
                    progressBox.setVisibility(View.GONE);
                    Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
                }

            } catch (IOException e) {
                //progressBox.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            //progressBox.setVisibility(View.GONE);
            Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void getLoginDetails(){

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult login_result) {

                GraphRequest request = GraphRequest.newMeRequest(
                        login_result.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                // Application code
                                try {
                                    user = new User();
                                    try{
                                        String email = object.getString("email");
                                        if(!validateEmail(email)){
                                            showSnackbarFacebookError();
                                            progressBox.setVisibility(View.GONE);
                                            return;
                                        }
                                    }catch (Exception  e) {
                                        showSnackbarFacebookError();
                                        progressBox.setVisibility(View.GONE);
                                        return;
                                    }
                                    user.setEmail(object.getString("email"));
                                    user.setName(object.getString("name"));
                                    user.setIdFacebook(object.getString("id"));

                                    try {
                                        String date = object.getString("birthday");
                                        user.setDayBorn(Integer.valueOf(date.substring(3, 5)));
                                        user.setMonthBorn(Integer.valueOf(date.substring(0, 2)));
                                        user.setYearBorn(Integer.valueOf(date.substring(6, 10)));
                                    }catch (Exception  e) {
                                        user.setDayBorn(0);
                                        user.setMonthBorn(0);
                                        user.setYearBorn(0);
                                    }

                                    user.setLivesIn("");
                                    user.setGender(object.getString("gender"));
                                    user.setPhoto(object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                    user.setFromFacebook(true);
                                    user.setPassword(user.getPhoto());

                                    user.setFacebookMessenger("");
                                    user.setWhereStudy("");
                                    user.setWhereWork("");
                                    user.setDescription("");
                                    user.setUrl("");

                                    loginProcessFacebook(user);
                                }
                                catch (Exception  e){
                                    showSnackbarFacebookError();
                                    progressBox.setVisibility(View.GONE);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name, email,gender,birthday, picture.width(600).height(600), events, link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                showSnackbarFacebookError();
                progressBox.setVisibility(View.GONE);
            }

            @Override
            public void onError(FacebookException exception) {
                showSnackbarFacebookError();
                progressBox.setVisibility(View.GONE);
            }
        });
    }

    private void showSnackbarFacebookError(){
        Snackbar snackbar =  Snackbar.make(findViewById(android.R.id.content),getString(R.string.error_facebook_login), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(Login1Activity.this, R.color.white))
                .setAction(getResources().getString(R.string.help), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "error_facebook_login" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tymo.me/termos-de-uso"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage("com.android.chrome");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            intent.setPackage(null);
                            startActivity(intent);
                        }
                    }
                });

        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED)
            progressBox.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getApplication());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void facebookSDKInitialize() {
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onClick(View view) {
        if(view == loginButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "loginButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(Login1Activity.this, Login2Activity.class));
        }
        else if(view == facebookLoginButton){
            if(AccessToken.getCurrentAccessToken() != null)
                LoginManager.getInstance().logOut();
            progressBox.setVisibility(View.VISIBLE);
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_birthday", "user_events"));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "facebookLoginButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        switch (position){
            case 0:
                text.setText(getResources().getString(R.string.login_slideshow_1));
                break;
            case 1:
                text.setText(getResources().getString(R.string.login_slideshow_2));
                break;
            case 2:
                text.setText(getResources().getString(R.string.login_slideshow_3));
                break;
            case 3:
                text.setText(getResources().getString(R.string.login_slideshow_4));
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSubscriptions != null)
            mSubscriptions.unsubscribe();
    }
}

