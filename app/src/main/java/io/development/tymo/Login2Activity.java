package io.development.tymo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import io.development.tymo.activities.IntroActivity;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.activities.RegisterPart1Activity;
import io.development.tymo.activities.RegisterPart2Activity;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateEmail;
import static io.development.tymo.utils.Validation.validateFields;

public class Login2Activity extends AppCompatActivity implements View.OnClickListener {

    private TextView forgot, loginButton, facebookLoginButton;
    private TextView signinButton;
    private CallbackManager callbackManager;
    private FirebaseAnalytics mFirebaseAnalytics;

    private LinearLayout progressBox;

    private EditText editUser;
    private EditText editPassword;

    private SharedPreferences mSharedPreferences;
    private CompositeSubscription mSubscriptions;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookSDKInitialize();
        setContentView(R.layout.activity_login_2);

        initSharedPreferences();

        String log = mSharedPreferences.getString(Constants.EMAIL, "");
        if(validateEmail(log))
            startActivity(new Intent(Login2Activity.this, MainActivity.class));

        mSubscriptions = new CompositeSubscription();

        facebookLoginButton = (TextView) findViewById(R.id.facebookLoginButton);
        loginButton = (TextView) findViewById(R.id.loginButton);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);
        forgot = (TextView) findViewById(R.id.forgot);
        editUser = (EditText) findViewById(R.id.email);
        editPassword = (EditText) findViewById(R.id.password);
        signinButton = (TextView) findViewById(R.id.signinButton);

        getLoginDetails();

        forgot.setOnClickListener(this);
        signinButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);

        initSharedPreferences();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void initSharedPreferences() {

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
    }

    private void login() {

        setError();

        String email = editUser.getText().toString();
        String password = editPassword.getText().toString();

        int err = 0;

        if (!validateEmail(email)) {
            err++;
            editUser.setError(getResources().getString(R.string.error_login_email_required));
        }
        if (!validateFields(password)) {
            err++;
            editPassword.setError(getResources().getString(R.string.error_login_password_required));
        }

        if (err == 0) {
            loginProcess(email,password);
        }
        else {
            showSnackBarMessage(getResources().getString(R.string.fill_fields_correctly));
        }
    }

    private void setError() {

        editUser.setError(null);
        editPassword.setError(null);
    }

    private void loginProcess(String email, String password) {
        progressBox.setVisibility(View.VISIBLE);
        mSubscriptions.add(NetworkUtil.getRetrofit(email, password).login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void loginProcessFacebook(User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit().loginFacebook(usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        User usr = response.getUser();
        String id = usr.getIdFacebook();

        editor.putString(Constants.EMAIL, usr.getEmail());
        editor.putBoolean(Constants.LOGIN_TYPE, usr.getFromFacebook());
        editor.putString(Constants.USER_NAME, usr.getName());
        editor.putBoolean(Constants.LOCATION, usr.isLocationGps());
        editor.putBoolean(Constants.NOTIFICATION, usr.isNotifications());
        editor.apply();

        if(user != null) {
            String id2 = user.getIdFacebook();
            if (id.matches(id2)) {
                editUser.setText(null);
                editPassword.setText(null);

                Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                editor.putString(Constants.EMAIL,"");
                editor.putBoolean(Constants.LOGIN_TYPE, false);
                editor.putString(Constants.USER_NAME, "");
                editor.putBoolean(Constants.LOCATION, false);
                editor.putBoolean(Constants.NOTIFICATION, false);
                editor.putBoolean(Constants.INTRO, false);
                editor.apply();
                Toast.makeText(this, getResources().getString(R.string.register_account_facebook_failed), Toast.LENGTH_LONG).show();
            }
        }else{
            editUser.setText(null);
            editPassword.setText(null);

            Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        progressBox.setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);

                if(response.getMessage().matches("REGISTER")) {

                    Intent register = new Intent(Login2Activity.this, RegisterPart2Activity.class);

                    UserWrapper wrapper = new UserWrapper(user);
                    register.putExtra("user_wrapper", wrapper);

                    startActivity(register);
                    overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
                    progressBox.setVisibility(View.GONE);
                }
                else {
                    progressBox.setVisibility(View.GONE);
                    showSnackBarMessage(ServerMessage.getServerMessage(this, response.getMessage()));
                }

            } catch (IOException e) {
                //progressBox.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            //progressBox.setVisibility(View.GONE);
            showSnackBarMessage(getResources().getString(R.string.network_error));
        }
    }

    private void showSnackBarMessage(String message) {

        if (findViewById(android.R.id.content) != null)
            Snackbar.make(findViewById(android.R.id.content), message,Snackbar.LENGTH_LONG).show();

    }

    private void showSnackbarFacebookError(){
        Snackbar snackbar =  Snackbar.make(findViewById(android.R.id.content),getString(R.string.error_facebook_login), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(Login2Activity.this, R.color.white))
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
                parameters.putString("fields", "id,name, email,gender,birthday, picture.width(600).height(600), location");
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
        if(view == forgot) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "forgot" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(Login2Activity.this, ForgotLoginActivity.class));
        }
        else if(view == signinButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "signinButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(Login2Activity.this, RegisterPart1Activity.class));
        }
        else if(view == loginButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "loginButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            login();
        }
        else if(view == facebookLoginButton){
            if(AccessToken.getCurrentAccessToken() != null)
                LoginManager.getInstance().logOut();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "facebookLoginButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            progressBox.setVisibility(View.VISIBLE);
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_birthday", "user_events"));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}

