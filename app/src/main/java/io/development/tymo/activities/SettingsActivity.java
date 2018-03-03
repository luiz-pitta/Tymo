package io.development.tymo.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.evernote.android.job.JobManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.api.services.calendar.model.CalendarList;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.development.tymo.BuildConfig;
import io.development.tymo.Login1Activity;
import io.development.tymo.R;
import io.development.tymo.adapters.SelectionCalendarAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.AppInfoServer;
import io.development.tymo.model_server.AppInfoWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnTouchListener,
        EasyPermissions.PermissionCallbacks{

    private ImageView mBackButton, profilePhoto, logo;
    private TextView m_title, fullName;
    private TextView versionName, textFromFacebook;
    private View importFromFacebookHorizontalLine;

    private CallbackManager callbackManager;
    private GoogleAccountCredential mCredential;

    private final int USER_UPDATE = 37;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private LinearLayout account, importFromFacebook, importFromGoogleAgenda;
    private LinearLayout privacy, blockedUserList, tutorial, logout, preferences, notifications;
    private LinearLayout contactUs, useTerms, privacyPolicy;
    private LinearLayout profileAboutBox;
    private Switch notificationsSwitch, locationSwitch;

    private FirebaseAnalytics mFirebaseAnalytics;
    private int email_google_position = 0;
    private GraphRequest request = null;

    private CompositeDisposable mSubscriptions;
    private AppInfoServer appInfoServer = null;
    private String urlStringPolicyPrivacy = "", urlStringTermsUse = "";
    private Integer google_calendar_months_to_add = null;


    private ArrayList<ArrayList<String>> list_google = new ArrayList<>();
    private ArrayList<String> list_email = new ArrayList<>();

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        UserWrapper userWrapper = (UserWrapper)getIntent().getSerializableExtra("user_about");
        user = userWrapper.getUser();

        facebookSDKInitialize();

        mSubscriptions = new CompositeDisposable();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        logo = (ImageView) findViewById(R.id.logo);
        m_title = (TextView) findViewById(R.id.text);
        versionName = (TextView) findViewById(R.id.versionName);
        fullName = (TextView) findViewById(R.id.fullName);
        notificationsSwitch = (Switch) findViewById(R.id.notificationCenterSwitch);
        locationSwitch = (Switch) findViewById(R.id.locationSwitch);
        textFromFacebook = (TextView) findViewById(R.id.textFromFacebook);
        importFromFacebookHorizontalLine = (View) findViewById(R.id.importFromFacebookHorizontalLine);

        account = (LinearLayout) findViewById(R.id.account);
        importFromFacebook = (LinearLayout) findViewById(R.id.importFromFacebook);
        importFromGoogleAgenda = (LinearLayout) findViewById(R.id.importFromGoogleAgenda);
        privacy = (LinearLayout) findViewById(R.id.privacy);
        blockedUserList = (LinearLayout) findViewById(R.id.blockedUserList);
        contactUs = (LinearLayout) findViewById(R.id.contactUs);
        useTerms = (LinearLayout) findViewById(R.id.useTerms);
        privacyPolicy = (LinearLayout) findViewById(R.id.privacyPolicy);
        profileAboutBox = (LinearLayout) findViewById(R.id.profileAboutBox);
        tutorial = (LinearLayout) findViewById(R.id.tutorial);
        logout = (LinearLayout) findViewById(R.id.logout);
        preferences = (LinearLayout) findViewById(R.id.myInterests);
        notifications = (LinearLayout) findViewById(R.id.notificationCenter);

        mBackButton.setOnClickListener(this);

        account.setOnClickListener(this);
        logout.setOnClickListener(this);
        importFromFacebook.setOnClickListener(this);
        importFromGoogleAgenda.setOnClickListener(this);
        privacy.setOnClickListener(this);
        blockedUserList.setOnClickListener(this);
        contactUs.setOnClickListener(this);
        useTerms.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
        profileAboutBox.setOnClickListener(this);
        tutorial.setOnClickListener(this);
        preferences.setOnClickListener(this);
        notifications.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);

        versionName.setText(getResources().getString(R.string.settings_version,BuildConfig.VERSION_NAME));

        Glide.clear(logo);
        Glide.with(this)
                .load(R.drawable.ic_login_logo)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(logo);

        m_title.setText(getResources().getString(R.string.profile_menu_4));

        if(!user.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(this)
                    .load(user.getPhoto())
                    .asBitmap()
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setNotifications(isChecked);
                setNotifications(user);
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setLocationGps(isChecked);
                setLocation(user);
            }
        });

        fullName.setText(user.getName());
        locationSwitch.setChecked(user.isLocationGps());
        notificationsSwitch.setChecked(user.isNotifications());

        if(user.getFromFacebook()){
            importFromFacebookHorizontalLine.setVisibility(View.GONE);
            importFromFacebook.setVisibility(View.GONE);
        }
        else{
            textFromFacebook.setVisibility(View.GONE);
        }

        boolean login_type = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.LOGIN_TYPE, false);

        if(!login_type)
            getLoginDetails();

        getContactUs();

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if(!Utilities.isDeviceOnline(this)) {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        } else {
            new GetCalendarListAsync(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE)
                    .getString(Constants.PREF_ACCOUNT_NAME, null);
            if (accountName != null && !accountName.equals("")) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getResources().getString(R.string.permission_import_from_google_agenda),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void setNotifications(User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().setNotificationUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void setLocation(User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().setLocationUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void deletePushNotification(UserPushNotification pushNotification) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().deletePushNotification(pushNotification)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponsePush,this::handleError));
    }

    private void handleResponsePush(Response response) {

        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
        editor.putString(Constants.EMAIL,"");
        editor.putBoolean(Constants.LOGIN_TYPE, false);
        editor.putString(Constants.USER_NAME, "");
        editor.putBoolean(Constants.LOCATION, false);
        editor.putBoolean(Constants.NOTIFICATION_ACT, false);
        editor.putBoolean(Constants.NOTIFICATION_FLAG, false);
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, false);
        editor.putBoolean(Constants.NOTIFICATION_PUSH, false);
        editor.putBoolean(Constants.INTRO, false);
        editor.putString(Constants.PREF_ACCOUNT_NAME, "");
        editor.putString("ListCalendarImportGoogle", "");
        editor.apply();

        if (AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();

        JobManager mJobManager = JobManager.instance();
        if(mJobManager.getAllJobRequests().size() > 0)
            mJobManager.cancelAll();

        FirebaseMessaging.getInstance().unsubscribeFromTopic("Tymo");

        Intent intent = new Intent(getApplicationContext(), Login1Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        setProgress(false);
    }

    private void handleResponse(User user) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
        editor.putBoolean(Constants.LOCATION, user.isLocationGps());
        editor.putBoolean(Constants.NOTIFICATION_ACT, user.isNotificationActivity());
        editor.putBoolean(Constants.NOTIFICATION_FLAG, user.isNotificationFlag());
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, user.isNotificationReminder());
        editor.putBoolean(Constants.NOTIFICATION_PUSH, user.isNotificationPush());
        editor.apply();

        setProgress(false);
    }

    private void importFromFacebook(ArrayList<ActivityServer> activityServers) {
        setProgress(true);
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivityFacebook(mSharedPreferences.getString(Constants.EMAIL, ""), activityServers)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFacebookImported,this::handleError));
    }

    private void handleResponseFacebookImported(Response response) {
        if(response.getNumberInvitationRequest() > 0)
            Toast.makeText(this, getResources().getString(R.string.settings_import_from_facebook_success), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_facebook_no_events), Toast.LENGTH_LONG).show();
        setProgress(false);
    }

    private void importFromGoogle(ArrayList<ActivityServer> activityServers) {
        setProgress(true);
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivityGooglenewApi(mSharedPreferences.getString(Constants.EMAIL, ""), activityServers)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGoogleImported,this::handleError));
    }

    private void handleResponseGoogleImported(Response response) {
        if(response.getNumberInvitationRequest() > 0)
            Toast.makeText(this, getResources().getString(R.string.settings_import_from_google_agenda_success), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_facebook_no_events), Toast.LENGTH_LONG).show();

        setProgress(false);
    }

    private void getContactUs() {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getAppInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(ArrayList<AppInfoServer> appInfoServers) {
        appInfoServer = appInfoServers.get(0);
        if(appInfoServer != null) {
            urlStringPolicyPrivacy = appInfoServer.getPrivacyPoliceUrl();
            urlStringTermsUse = appInfoServer.getUseTermsUrl();
            google_calendar_months_to_add = appInfoServer.getGoogleCalendarMonthsToAdd();
        }
        setProgress(false);
    }

    private void handleError(Throwable error) {
        setProgress(false);
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);

        switch (requestCode) {
            case 1:
                if (grantResults.length == 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getResources().getString(R.string.permission_import_from_google_agenda), Toast.LENGTH_LONG).show();
                    break;
                }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if(view == profileAboutBox){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "profileAboutBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra("user_about", new UserWrapper(user));
            startActivity(intent);
        }
        else if(view == account){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "account" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra("user_about", new UserWrapper(user));
            startActivity(intent);
        }
        else if(view == importFromFacebook){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "importFromFacebook" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogFacebookImport();
        }
        else if(view == importFromGoogleAgenda){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "importFromGoogleAgenda" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            getResultsFromApi();
        }
        else if(view == privacy){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "privacy" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(new Intent(SettingsActivity.this, PrivacyActivity.class));
        }
        else if(view == preferences){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "preferences" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(new Intent(SettingsActivity.this, MyInterestsActivity.class));
        }
        else if(view == notifications){
            Intent intent = new Intent(this, NotificationCenterActivity.class);
            intent.putExtra("user_about", new UserWrapper(user));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "notifications" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, USER_UPDATE);
        }
        else if(view == blockedUserList){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "blockedUserList" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(new Intent(SettingsActivity.this, BlockedUsersActivity.class));
        }
        else if(view == logout) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "logout" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogLogout();
        }
        else if(view == contactUs){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "contactUs" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(SettingsActivity.this, ContactUsSettingsActivity.class);
            intent.putExtra("contact_us", new AppInfoWrapper(appInfoServer));
            startActivity(intent);
        }
        else if(view == tutorial){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "tutorial" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(this, IntroActivity.class);
            intent.putExtra("settings", true);
            startActivity(intent);
        }
        else if(view == useTerms){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "useTerms" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(urlStringTermsUse));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.android.chrome");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                intent.setPackage(null);
                startActivity(intent);
            }
        }
        else if(view == privacyPolicy){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "privacyPolicy" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(urlStringPolicyPrivacy));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.android.chrome");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                intent.setPackage(null);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if (requestCode == USER_UPDATE) {
                boolean notification1 = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_ACT, true);
                boolean notification2 = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_FLAG, true);
                boolean notification3 = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_REMINDER, true);
                boolean notification4 = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_PUSH, true);
                user.setNotificationActivity(notification1);
                user.setNotificationFlag(notification2);
                user.setNotificationReminder(notification3);
                user.setNotificationPush(notification4);
            }else
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }else if(resultCode == RESULT_CANCELED)
            setProgress(false);

        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.permission_import_from_google_agenda2), Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    private void createDialogFacebookImport() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);

        customView.findViewById(R.id.editText).setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.settings_import_from_facebook_tag));
        text2.setText(getResources().getString(R.string.settings_import_from_facebook_text));
        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean login_type = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.LOGIN_TYPE, false);

                if(!login_type || AccessToken.getCurrentAccessToken() == null) {
                    setProgress(true);

                    if(AccessToken.getCurrentAccessToken() != null)
                        importFromFacebookRequest();
                    else
                        LoginManager.getInstance().logInWithReadPermissions(SettingsActivity.this, Arrays.asList("email", "public_profile", "user_events"));
                }else {
                    setProgress(true);
                    importFromFacebookRequest();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected void getLoginDetails(){

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult login_result) {

                ArrayList<ActivityServer> list_activities_to_import = new ArrayList<>();

                request = GraphRequest.newGraphPathRequest(
                        login_result.getAccessToken(),
                        "/me/events?limit=100",
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject object = response.getJSONObject();
                                // Application code
                                try {
                                    JSONArray events = object.getJSONArray("data");

                                    for(int i=0;i<events.length();i++){
                                        JSONObject jsonObject = events.getJSONObject(i);
                                        ActivityServer server = createActivity(jsonObject);
                                        if(server != null)
                                            list_activities_to_import.add(server);
                                    }

                                    if(list_activities_to_import.size() > 0) {
                                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                                        if(nextRequest != null) {
                                            nextRequest.setCallback(request.getCallback());
                                            nextRequest.executeAsync();
                                        }else
                                            importFromFacebook(list_activities_to_import);
                                    }
                                    else {
                                        setProgress(false);
                                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_facebook_no_events), Toast.LENGTH_LONG).show();
                                    }
                                }
                                catch (Exception  e){
                                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, description, place, start_time, end_time");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                showSnackbarFacebookError();
                setProgress(false);
            }

            @Override
            public void onError(FacebookException exception) {
                showSnackbarFacebookError();
                setProgress(false);
            }
        });
    }

    private void showSnackbarFacebookError(){
        Snackbar snackbar =  Snackbar.make(findViewById(android.R.id.content),getString(R.string.error_facebook_login), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(this, R.color.white))
                .setAction(getResources().getString(R.string.help), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "error_facebook_login" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tymo.me/"));
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

    private void createDialogGoogleImport(ArrayList<String> accountsList) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_list_multiple_select, null);

        SelectionCalendarAdapter selectionCalendarAdapter;

        TextView text1 = customView.findViewById(R.id.text1);
        TextView text2 = customView.findViewById(R.id.text2);
        TextView buttonText1 = customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = customView.findViewById(R.id.buttonText2);
        RecyclerView mMultiChoiceRecyclerView = customView.findViewById(R.id.recyclerSelectView);

        text1.setText(getResources().getString(R.string.settings_import_from_google_agenda_title));
        text2.setText(getResources().getString(R.string.settings_import_from_google_agenda_text));
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.action_import));

        mMultiChoiceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMultiChoiceRecyclerView.setNestedScrollingEnabled(false);

        ArrayList<String> arrayList = new ArrayList<>();
        for(int i=1;i<accountsList.size();i=i+2){
            arrayList.add(accountsList.get(i));
        }

        selectionCalendarAdapter = new SelectionCalendarAdapter(arrayList, this) ;
        mMultiChoiceRecyclerView.setAdapter(selectionCalendarAdapter);
        selectionCalendarAdapter.setSingleClickMode(true);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);
        mMultiChoiceRecyclerView.setHasFixedSize(true);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });
        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> list = selectionCalendarAdapter.getSelectedItemList();
                ArrayList<String> calendarSelectedList = new ArrayList<>();
                for(int i=0;i<list.size();i++){
                    int j = 2*list.get(i);
                    calendarSelectedList.add(accountsList.get(j));
                }

                if (calendarSelectedList.size() > 0) {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(calendarSelectedList);
                    editor.putString("ListCalendarImportGoogle", json);
                    editor.apply();

                    new GetCalendarEventsAsync(mCredential).execute(calendarSelectedList.toArray(new String[calendarSelectedList.size()]));

                    dialog.dismiss();
                }else
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_error3), Toast.LENGTH_LONG).show();

            }
        });

        dialog.show();
    }

    private void createDialogLogout() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.logout_app));
        text2.setText(getResources().getString(R.string.logout_confirmation_question));
        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPushNotification pushNotification = new UserPushNotification();
                pushNotification.setEmail(user.getEmail());
                pushNotification.setIdDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

                dialog.dismiss();

                deletePushNotification(pushNotification);
            }
        });

        dialog.show();
    }

    private void importFromFacebookRequest(){

        ArrayList<ActivityServer> list_activities_to_import = new ArrayList<>();

        request = GraphRequest.newGraphPathRequest(
               AccessToken.getCurrentAccessToken(),
                "/me/events?limit=100",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject object = response.getJSONObject();
                        // Application code
                        try {
                            JSONArray events = object.getJSONArray("data");

                            for(int i=0;i<events.length();i++){
                                JSONObject jsonObject = events.getJSONObject(i);
                                ActivityServer server = createActivity(jsonObject);
                                if(server != null)
                                    list_activities_to_import.add(server);
                            }

                            if(list_activities_to_import.size() > 0) {
                                GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                                if(nextRequest != null) {
                                    nextRequest.setCallback(request.getCallback());
                                    nextRequest.executeAsync();
                                }else
                                    importFromFacebook(list_activities_to_import);
                            }
                            else {
                                setProgress(false);
                                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_facebook_no_events), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception  e){
                            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, description, place, start_time, end_time");
        request.setParameters(parameters);
        request.executeAsync();
    }

    protected void facebookSDKInitialize() {
        callbackManager = CallbackManager.Factory.create();
    }

    private ActivityServer createActivity(JSONObject jsonObject){
        ActivityServer activityServer = new ActivityServer();
        String id = "0", description="", start_time, end_time, name="";
        String name_place;
        JSONObject place;
        Double lat, lng;
        Date start = new Date(), end;
        Calendar calendar = Calendar.getInstance();
        Calendar c = Calendar.getInstance();

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String creator = mSharedPreferences.getString(Constants.EMAIL, "");

        try {
            id = jsonObject.getString("id");
            name = jsonObject.getString("name");

            try {
                description = jsonObject.getString("description");
            } catch (Exception e) {
                description = "";
            }

            try {
                start_time = jsonObject.getString("start_time");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                start = format.parse(start_time);
            } catch (Exception e) {
            }

            try {
                end_time = jsonObject.getString("end_time");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                end = format.parse(end_time);
            } catch (Exception e) {
                end = start;
            }

            try {
                place = jsonObject.getJSONObject("place");
                name_place = place.getString("name");
                try {
                    lat = place.getJSONObject("location").getDouble("latitude");
                    lng = place.getJSONObject("location").getDouble("longitude");
                } catch (Exception e) {
                    lat = -250.0;
                    lng = -250.0;
                }
            } catch (Exception e) {
                name_place = "";
                lat = -500.0;
                lng = -500.0;
            }

            activityServer.setIdFacebook(Long.parseLong(id));
            activityServer.setTitle(name);
            activityServer.setDescription(description);
            activityServer.setLocation(name_place);
            activityServer.setInvitationType(0);

            activityServer.setLat(lat);
            activityServer.setLng(lng);

            calendar.setTime(start);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            activityServer.setDayStart(day);
            activityServer.setMonthStart(month);
            activityServer.setYearStart(year);
            activityServer.setHourStart(calendar.get(Calendar.HOUR_OF_DAY));
            activityServer.setMinuteStart(calendar.get(Calendar.MINUTE));

            Calendar c2 = Calendar.getInstance();
            c2.setTime(end);

            int y2 = c2.get(Calendar.YEAR);
            int m2 = c2.get(Calendar.MONTH) + 1;
            int d2 = c2.get(Calendar.DAY_OF_MONTH);
            int minute2 = c2.get(Calendar.MINUTE);
            int hour2 = c2.get(Calendar.HOUR_OF_DAY);

            int day_today = c.get(Calendar.DAY_OF_MONTH);
            int month_today = c.get(Calendar.MONTH)+1;
            int year_today = c.get(Calendar.YEAR);

            if(y2 < year_today)
                return null;
            else if(y2 == year_today && m2 < month_today)
                return null;
            else if(y2 == year_today && m2 == month_today && d2 < day_today)
                return null;

            LocalDate starts = new LocalDate(year, month, day);
            LocalDate ends = new LocalDate(y2, m2, d2);
            Period timePeriod = new Period(starts, ends, PeriodType.days());
            if (timePeriod.getDays() > 15) {
                activityServer.setDayEnd(day);
                activityServer.setMonthEnd(month);
                activityServer.setYearEnd(year);
                activityServer.setMinuteEnd(calendar.get(Calendar.MINUTE));
                activityServer.setHourEnd(calendar.get(Calendar.HOUR_OF_DAY));
            } else {
                activityServer.setDayEnd(d2);
                activityServer.setMonthEnd(m2);
                activityServer.setYearEnd(y2);
                activityServer.setMinuteEnd(minute2);
                activityServer.setHourEnd(hour2);
            }

            activityServer.setRepeatType(0);
            activityServer.setRepeatQty(-1);

            activityServer.setCubeColor(ContextCompat.getColor(getApplication(), R.color.facebook_dark_blue));
            activityServer.setCubeColorUpper(ContextCompat.getColor(getApplication(), R.color.facebook_blue));
            activityServer.setCubeIcon("");

            activityServer.setWhatsappGroupLink("");

            activityServer.addTags(getResources().getString(R.string.settings_import_from_facebook_tag));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar2 = Calendar.getInstance();
            calendar2.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar2.getTimeInMillis());

            calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
            activityServer.setDateTimeEnd(calendar2.getTimeInMillis());

            activityServer.setCreator(creator);
            activityServer.setDeletedActivityImported(true);
        }
        catch (Exception e){

        }
        return activityServer;
    }

    private ActivityServer createActivityGoogle(Event event){
        ActivityServer activityServer = new ActivityServer();

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String creator = mSharedPreferences.getString(Constants.EMAIL, "");

        activityServer.setIdGoogle(event.getId());
        activityServer.setTitle(event.getSummary() != null ? event.getSummary() : getResources().getString(R.string.settings_import_from_google_agenda_tag));
        activityServer.setDescription(event.getDescription() != null ? event.getDescription() : "");
        activityServer.setLat(-500);
        activityServer.setLng(-500);
        activityServer.setLocation(event.getLocation() != null ? event.getLocation() : "");
        activityServer.setRepeatType(0);
        activityServer.setRepeatQty(-1);
        activityServer.setInvitationType(0);
        activityServer.setWhatsappGroupLink("");
        activityServer.addTags(getResources().getString(R.string.settings_import_from_google_agenda_tag));
        activityServer.setDeletedActivityImported(true);

        activityServer.setCubeColor(ContextCompat.getColor(getApplication(), R.color.google_agenda_cube));
        activityServer.setCubeColorUpper(ContextCompat.getColor(getApplication(), R.color.google_agenda_cube_light));

        activityServer.setCreator(creator);
        activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

        DateTime start = event.getStart().getDateTime();
        DateTime end = event.getEnd().getDateTime();
        if (start == null) // All-day events don't have start times, so just use the start date.
            start = event.getStart().getDate();

        if (end == null) // All-day events don't have start times, so just use the start date.
            end = event.getEnd().getDate();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(start.getValue());
        int y1 = c.get(Calendar.YEAR);
        int m1 = c.get(Calendar.MONTH) + 1;
        int d1 = c.get(Calendar.DAY_OF_MONTH);
        int minute1 = c.get(Calendar.MINUTE);
        int hour1 = c.get(Calendar.HOUR_OF_DAY);

        activityServer.setDayStart(d1);
        activityServer.setMonthStart(m1);
        activityServer.setYearStart(y1);
        activityServer.setMinuteStart(minute1);
        activityServer.setHourStart(hour1);

        c.setTimeInMillis(end.getValue());
        int y2 = c.get(Calendar.YEAR);
        int m2 = c.get(Calendar.MONTH) + 1;
        int d2 = c.get(Calendar.DAY_OF_MONTH);
        int minute2 = c.get(Calendar.MINUTE);
        int hour2 = c.get(Calendar.HOUR_OF_DAY);

        LocalDate starts = new LocalDate(y1, m1, d1);
        LocalDate ends = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(starts, ends, PeriodType.days());
        if (timePeriod.getDays() > 15) {
            activityServer.setDayEnd(d1);
            activityServer.setMonthEnd(m1);
            activityServer.setYearEnd(y1);
            activityServer.setMinuteEnd(minute1);
            activityServer.setHourEnd(hour1);
        } else {
            activityServer.setDayEnd(d2);
            activityServer.setMonthEnd(m2);
            activityServer.setYearEnd(y2);
            activityServer.setMinuteEnd(minute2);
            activityServer.setHourEnd(hour2);
        }

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
        activityServer.setDateTimeStart(calendar2.getTimeInMillis());

        calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
        activityServer.setDateTimeEnd(calendar2.getTimeInMillis());

        return activityServer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        }

        return false;
    }


    private class GetCalendarListAsync extends AsyncTask<Void, Void, ArrayList<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        GetCalendarListAsync(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private ArrayList<String> getDataFromApi() throws IOException {
            ArrayList<String> eventStrings = new ArrayList<String>();

            String pageToken = null;
            do {
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    String calendar_type = calendarListEntry.getSummary();

                    if(!calendar_type.contains("Holiday") && !calendar_type.contains("Contacts")) {
                        eventStrings.add(calendarListEntry.getId());
                        eventStrings.add(calendarListEntry.getSummary());
                    }
                }
                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);

            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            setProgress(true);
        }

        @Override
        protected void onPostExecute(ArrayList<String> output) {
            if (output == null || output.size() == 0) {
                setProgress(false);
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
            } else {
                createDialogGoogleImport(output);
                setProgress(false);
            }
        }

        @Override
        protected void onCancelled() {
            setProgress(false);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(SettingsActivity.this, mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_error2), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class GetCalendarEventsAsync extends AsyncTask<String, Void, Integer> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        GetCalendarEventsAsync(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                ArrayList<String> calendarList = new ArrayList<>();
                calendarList.addAll(Arrays.asList(params));

                return getDataFromApi(calendarList);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private Integer getDataFromApi(ArrayList<String> calendarList) throws IOException {
            Calendar calendar = Calendar.getInstance();

            if(google_calendar_months_to_add == null)
                google_calendar_months_to_add = 3;

            calendar.add(Calendar.MONTH, google_calendar_months_to_add);
            DateTime now = new DateTime(System.currentTimeMillis());
            DateTime end = new DateTime(calendar.getTimeInMillis());

            ArrayList<ActivityServer> list_activities_to_import = new ArrayList<>();

            for(String name : calendarList) {
                String pageToken = null;
                do { //Get events without repeat
                    Events events = mService.events()
                            .list(name)
                            .setPageToken(pageToken)
                            .setMaxResults(2000)
                            .setTimeMin(now)
                            .execute();

                    List<Event> items = events.getItems();

                    for (Event event : items) {

                        if (event.getRecurrence() == null && event.getRecurringEventId() == null && !event.getStatus().equals("cancelled")) {
                            ActivityServer server = createActivityGoogle(event);
                            if(server != null)
                                list_activities_to_import.add(server);
                        }
                    }
                    pageToken = events.getNextPageToken();
                } while (pageToken != null);

                pageToken = null;
                do { //Get events with repeat
                    Events eventsRepeat = mService.events()
                            .list(name)
                            .setPageToken(pageToken)
                            .setMaxResults(2000)
                            .setTimeMin(now)
                            .setTimeMax(end)
                            .setSingleEvents(true)
                            .setShowDeleted(false)
                            .setOrderBy("startTime")
                            .execute();
                    List<Event> itemsRepeat = eventsRepeat.getItems();

                    for (Event event : itemsRepeat) {

                        if(event.getRecurringEventId() != null && !event.getStatus().equals("cancelled")) {
                            ActivityServer server = createActivityGoogle(event);
                            if(server != null)
                                list_activities_to_import.add(server);
                        }
                    }
                    pageToken = eventsRepeat.getNextPageToken();
                } while (pageToken != null);
            }

            importFromGoogle(list_activities_to_import);

            return list_activities_to_import.size();
        }


        @Override
        protected void onPreExecute() {
            setProgress(true);
        }

        @Override
        protected void onPostExecute(Integer output) {
            if (output == null || output == 0) {
                setProgress(false);
                //Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            setProgress(false);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(SettingsActivity.this, mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_error2), Toast.LENGTH_LONG).show();
            }
        }
    }
}
