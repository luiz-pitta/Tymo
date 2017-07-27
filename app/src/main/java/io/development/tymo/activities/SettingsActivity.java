package io.development.tymo.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import io.development.tymo.BuildConfig;
import io.development.tymo.Login1Activity;
import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.AppInfoServer;
import io.development.tymo.model_server.AppInfoWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.GoogleCalendarEvents;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton, profilePhoto, logo;
    private TextView m_title, fullName;
    private TextView versionName;

    private CallbackManager callbackManager;

    private final int USER_UPDATE = 37;

    private LinearLayout account, importFromFacebook, importFromGoogleAgenda;
    private LinearLayout privacy, blockedUserList, tutorial, logout, preferences, notifications;
    private LinearLayout contactUs, useTerms, privacyPolicy;
    private LinearLayout profileAboutBox;
    private Switch notificationsSwitch, locationSwitch;

    private FirebaseAnalytics mFirebaseAnalytics;
    private int email_google_position = 0;

    private CompositeDisposable mSubscriptions;
    AppInfoServer appInfoServer = null;
    String urlStringPolicyPrivacy = "", urlStringTermsUse = "";

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

        versionName.setText(getResources().getString(R.string.settings_version,BuildConfig.VERSION_NAME));

        Glide.clear(logo);
        Glide.with(this)
                .load(R.drawable.ic_login_logo)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(logo);

        m_title.setText(getResources().getString(R.string.profile_menu_settings));

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

        boolean login_type = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.LOGIN_TYPE, false);

        if(!login_type)
            getLoginDetails();

        getContactUs();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void getImportedGoogle(String email) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getImportedGoogle(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGoogle,this::handleError));
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
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivityFacebook(activityServers)
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
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivityGoogle(activityServers)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGoogleImported,this::handleError));
    }

    private void handleResponseGoogleImported(Response response) {
        Toast.makeText(this, getResources().getString(R.string.settings_import_from_google_agenda_success), Toast.LENGTH_LONG).show();
        setProgress(false);
    }

    private boolean isActivityImported(ArrayList<ActivityServer> list, long id, String name){
        for(int i=0;i<list.size();i++){
            ActivityServer activityServer = list.get(i);
            if(activityServer.getIdGoogle() == id || name.contains(activityServer.getTitle()))
                return true;
        }
        return false;
    }

    private void handleResponseGoogle(Response response) {
        ArrayList<ActivityServer> list_activities_to_import = new ArrayList<>();
        ArrayList<ActivityServer> list_activities_imported_google = response.getWhatsGoingAct();
        if(list_google.size() > 0) {
            ArrayList<String> list_ids_google = list_google.get(0);
            ArrayList<String> list_names_google = list_google.get(1);
            ArrayList<String> list_description_google = list_google.get(2);
            ArrayList<String> list_locations_google = list_google.get(3);
            ArrayList<String> list_date_start_google = list_google.get(4);
            ArrayList<String> list_date_end_google = list_google.get(5);
            ArrayList<String> list_repeat_google = list_google.get(6);


            //Retirando as atividades já importadas
            for (int j = 0; j < list_ids_google.size() && list_activities_imported_google.size() > 0; j++) {
                if (isActivityImported(list_activities_imported_google, Long.parseLong(list_ids_google.get(j)), list_names_google.get(j))) {
                    list_ids_google.remove(j);
                    list_names_google.remove(j);
                    list_description_google.remove(j);
                    list_locations_google.remove(j);
                    list_date_start_google.remove(j);
                    list_date_end_google.remove(j);
                    list_repeat_google.remove(j);
                    j--;
                }
            }

            for (int j = 0; j < list_ids_google.size(); j++) {
                ActivityServer activityServer = new ActivityServer();
                if(list_names_google.get(j) == null)
                    continue;
                activityServer.setTitle(list_names_google.get(j));
                activityServer.setIdGoogle(Long.parseLong(list_ids_google.get(j)));
                activityServer.setDescription(list_description_google.get(j) != null ? list_description_google.get(j) : "");
                activityServer.setLat(-500);
                activityServer.setLng(-500);
                activityServer.setLocation(list_locations_google.get(j) != null ? list_locations_google.get(j) : "");
                activityServer.setRepeatType(list_repeat_google.get(j) != null ? 5 : 0); //Opção 5 caso atividade do Google se repita para colocar msg que ele tem que criar a repetição
                activityServer.setInvitationType(0); //Somente administrador
                activityServer.setWhatsappGroupLink("");
                activityServer.addTags(getResources().getString(R.string.settings_import_from_google_agenda_tag));

                activityServer.setCubeColor(ContextCompat.getColor(getApplication(), R.color.google_agenda_cube));
                activityServer.setCubeColorUpper(ContextCompat.getColor(getApplication(), R.color.google_agenda_cube_light));
                activityServer.setCubeIcon(response.getIcon().getUrl());

                activityServer.setCreator(user.getEmail());

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(Long.parseLong(list_date_start_google.get(j)));
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

                if(list_date_end_google.get(j) != null) {
                    c.setTimeInMillis(Long.parseLong(list_date_end_google.get(j)));
                    int y2 = c.get(Calendar.YEAR);
                    int m2 = c.get(Calendar.MONTH) + 1;
                    int d2 = c.get(Calendar.DAY_OF_MONTH);
                    int minute2 = c.get(Calendar.MINUTE);
                    int hour2 = c.get(Calendar.HOUR_OF_DAY);

                    LocalDate start = new LocalDate (y1, m1, d1);
                    LocalDate end = new LocalDate(y2, m2, d2);
                    Period timePeriod = new Period(start, end, PeriodType.days());
                    if(timePeriod.getDays() > 15) {
                        activityServer.setDayEnd(d1);
                        activityServer.setMonthEnd(m1);
                        activityServer.setYearEnd(y1);
                        activityServer.setMinuteEnd(minute1);
                        activityServer.setHourEnd(hour1);
                    }else {
                        activityServer.setDayEnd(d2);
                        activityServer.setMonthEnd(m2);
                        activityServer.setYearEnd(y2);
                        activityServer.setMinuteEnd(minute2);
                        activityServer.setHourEnd(hour2);
                    }
                }else {
                    activityServer.setDayEnd(d1);
                    activityServer.setMonthEnd(m1);
                    activityServer.setYearEnd(y1);
                    activityServer.setMinuteEnd(minute1);
                    activityServer.setHourEnd(hour1);
                }

                activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

                Calendar calendar2 = Calendar.getInstance();
                calendar2.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
                activityServer.setDateTimeStart(calendar2.getTimeInMillis());

                calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
                activityServer.setDateTimeEnd(calendar2.getTimeInMillis());

                list_activities_to_import.add(activityServer);
            }

            if(list_activities_to_import.size() > 0)
                importFromGoogle(list_activities_to_import);
            else {
                Toast.makeText(this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
                setProgress(false);
            }
        }else{
            Toast.makeText(this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
            setProgress(false);
        }
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
        }
        setProgress(false);
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
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
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, getResources().getString(R.string.permission_import_from_google_agenda), Toast.LENGTH_LONG).show();
                }

                break;
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

            createDialogGoogleImport();
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
                callbackManager.onActivityResult(requestCode, resultCode, intent);
        }else if(resultCode == RESULT_CANCELED)
            setProgress(false);
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

                if(!login_type) {
                    if (AccessToken.getCurrentAccessToken() != null)
                        LoginManager.getInstance().logOut();

                    setProgress(true);
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

                GraphRequest request = GraphRequest.newMeRequest(
                        login_result.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                // Application code
                                try {
                                    JSONArray events = object.getJSONObject("events").getJSONArray("data");
                                    ArrayList<ActivityServer> list_activities_to_import = new ArrayList<>();
                                    for(int i=0;i<events.length();i++){
                                        JSONObject jsonObject = events.getJSONObject(i);
                                        ActivityServer server = createActivity(jsonObject);
                                        if(server != null)
                                            list_activities_to_import.add(server);
                                    }

                                    if(list_activities_to_import.size() > 0)
                                        importFromFacebook(list_activities_to_import);
                                }
                                catch (Exception  e){
                                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_no_commitments), Toast.LENGTH_LONG).show();
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

    private void createDialogGoogleImport() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        MaterialSpinner spinner = (MaterialSpinner) customView.findViewById(R.id.emailPicker);

        customView.findViewById(R.id.editText).setVisibility(View.GONE);
        customView.findViewById(R.id.emailBox).setVisibility(View.VISIBLE);

        text1.setText(getResources().getString(R.string.settings_import_from_google_agenda_title));
        text2.setText(getResources().getString(R.string.settings_import_from_google_agenda_text));
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.action_import));

        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[] { Manifest.permission.READ_CALENDAR },
                    1);
        }

        if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            list_email = GoogleCalendarEvents.getCalendarTypes(SettingsActivity.this);
        }else {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_error), Toast.LENGTH_LONG).show();
            setProgress(false);
        }



        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[] { Manifest.permission.READ_CALENDAR },
                            1);
                }

                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_CALENDAR)
                        == PackageManager.PERMISSION_GRANTED) {
                    list_google = GoogleCalendarEvents.readCalendarEvent(SettingsActivity.this, list_email.get(email_google_position));
                    getImportedGoogle(user.getEmail());
                }else {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_error), Toast.LENGTH_LONG).show();
                    setProgress(false);
                }

                dialog.dismiss();
            }
        });

        if(list_email != null && list_email.size() > 0) {

            spinner.setItems(list_email);
            spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    email_google_position = position;
                }
            });

            dialog.show();

        }else {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_google_agenda_error), Toast.LENGTH_LONG).show();
            setProgress(false);
        }
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
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        // Application code
                        try {
                            JSONArray events = object.getJSONObject("events").getJSONArray("data");
                            ArrayList<ActivityServer> list_activities_to_import = new ArrayList<>();
                            for(int i=0;i<events.length();i++){
                                JSONObject jsonObject = events.getJSONObject(i);
                                ActivityServer server = createActivity(jsonObject);
                                if(server != null)
                                    list_activities_to_import.add(server);
                            }

                            if(list_activities_to_import.size() > 0)
                                importFromFacebook(list_activities_to_import);
                            else {
                                setProgress(false);
                                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_facebook_no_events), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception  e){
                            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.settings_import_from_facebook_no_events), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, email, events");
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
        Date start = new Date(), end = new Date();
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
                start_time = "";
            }

            try {
                end_time = jsonObject.getString("end_time");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                end = format.parse(end_time);
            } catch (Exception e) {
                end_time = "";
            }

            try {
                place = jsonObject.getJSONObject("place");
                name_place = place.getString("name");
                lat = place.getJSONObject("location").getDouble("latitude");
                lng = place.getJSONObject("location").getDouble("longitude");
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

            int day_today = c.get(Calendar.DAY_OF_MONTH);
            int month_today = c.get(Calendar.MONTH)+1;
            int year_today = c.get(Calendar.YEAR);

            if(year < year_today)
                return null;
            else if(year == year_today && month < month_today)
                return null;
            else if(year == year_today && month == month_today && day < day_today)
                return null;

            activityServer.setDayStart(calendar.get(Calendar.DAY_OF_MONTH));
            activityServer.setMonthStart(calendar.get(Calendar.MONTH)+1);
            activityServer.setYearStart(calendar.get(Calendar.YEAR));
            activityServer.setHourStart(calendar.get(Calendar.HOUR_OF_DAY));
            activityServer.setMinuteStart(calendar.get(Calendar.MINUTE));

            calendar.setTime(end);
            activityServer.setDayEnd(calendar.get(Calendar.DAY_OF_MONTH));
            activityServer.setMonthEnd(calendar.get(Calendar.MONTH)+1);
            activityServer.setYearEnd(calendar.get(Calendar.YEAR));
            activityServer.setHourEnd(calendar.get(Calendar.HOUR_OF_DAY));
            activityServer.setMinuteEnd(calendar.get(Calendar.MINUTE));

            activityServer.setRepeatType(0);
            activityServer.setRepeatQty(-1);

            activityServer.setCubeColor(ContextCompat.getColor(getApplication(), R.color.facebook_dark_blue));
            activityServer.setCubeColorUpper(ContextCompat.getColor(getApplication(), R.color.facebook_blue));
            activityServer.setCubeIcon("");

            activityServer.setCreator(user.getEmail());

            activityServer.setWhatsappGroupLink("");

            activityServer.addTags(getResources().getString(R.string.settings_import_from_facebook_tag));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar2 = Calendar.getInstance();
            calendar2.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar2.getTimeInMillis());

            calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
            activityServer.setDateTimeEnd(calendar2.getTimeInMillis());

            activityServer.setCreator(creator);
        }
        catch (Exception e){

        }
        return activityServer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }
}
