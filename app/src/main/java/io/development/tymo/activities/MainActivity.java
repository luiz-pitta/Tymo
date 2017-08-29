package io.development.tymo.activities;


import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.rebound.SpringSystem;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaouan.revealator.Revealator;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.fragments.FeedFragment;
import io.development.tymo.fragments.PlansFragment;
import io.development.tymo.fragments.ProfileFragment;
import io.development.tymo.fragments.SearchFragment;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FilterServer;
import io.development.tymo.model_server.FilterWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.ActivitySyncJob;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ForceUpdateChecker;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.UpdateButtonController;
import io.development.tymo.utils.Utilities;
import io.development.tymo.adapters.MainFragmentAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ForceUpdateChecker.OnUpdateNeededListener{

    public static final String ADD_VIEW_IS_VISIBLE = "add_is_visible";

    private FragmentNavigator mNavigator;
    private UpdateButtonController controller;
    private RelativeLayout mainMenu;

    private GraphRequest request = null;

    private MaterialSearchView searchView;
    private FloatingActionButton fab;
    private ImageView icon1;
    private ImageView icon2;
    private ImageView icon3;
    private ImageView icon4;
    private ImageView actButton;
    private ImageView reminderButton;
    private ImageView flagButton;
    private ImageView closeButton;
    private RelativeLayout icon1Box, icon2Box, icon3Box, icon4Box;

    private String email;
    private View notificationView;

    private boolean refresh = true;
    private FilterServer filterServer = null;

    private Rect rect;

    private static final int FEED = 0;
    private static final int PLANS = 1;
    private static final int ABOUT = 2;
    private static final int SEARCH = 3;

    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private View addView;

    private CallbackManager callbackManager;
    private GoogleAccountCredential mCredential;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private JobManager mJobManager;

    private BroadcastReceiver mMessageReceiverSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSearch();
        }
    };

    private BroadcastReceiver mMessageReceiverFeed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateFeed();
        }
    };

    private BroadcastReceiver mMessageReceiverNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivityStartToday();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInterface(savedInstanceState);

        if(AccessToken.getCurrentAccessToken() != null)
            importFromFacebookRequest();

        Gson gson = new Gson();
        ArrayList<String> list_json = new ArrayList<>();
        String json = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString("ListCalendarImportGoogle", "");
        if(!json.matches(""))
            list_json = gson.fromJson(json, new TypeToken<ArrayList<String>>(){}.getType());

        if(list_json.size() > 0 && mCredential.getSelectedAccountName() != null && !mCredential.getSelectedAccountName().equals(""))
            new GetCalendarEventsAsync(mCredential).execute(list_json.toArray(new String[list_json.size()]));


    }

    private void importFromGoogle(ArrayList<ActivityServer> activityServers) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivityGooglenewApi(mSharedPreferences.getString(Constants.EMAIL, ""), activityServers)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGoogleImported,this::handleError));
    }

    private void handleResponseGoogleImported(Response response) {}

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
                        }
                        catch (Exception  e){
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, description, place, start_time, end_time");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void importFromFacebook(ArrayList<ActivityServer> activityServers) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivityFacebook(mSharedPreferences.getString(Constants.EMAIL, ""), activityServers)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFacebookImported,this::handleError));
    }

    private void handleResponseFacebookImported(Response response) {

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
    public void onUpdateNotNeeded() {}

    @Override
    public void onUpdateNeeded(String updateUrl, String version) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);

        customView.findViewById(R.id.editText).setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.version_new_update_title, version));
        text2.setText(getResources().getString(R.string.version_new_update_text));
        buttonText1.setText(getResources().getString(R.string.version_new_update_no));
        buttonText2.setText(getResources().getString(R.string.version_new_update_yes));

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
                finish();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectStore(updateUrl);
            }
        });

        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void updateProfileMainInformation(){
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        Query query = new Query();
        query.setEmail(email);
        query.setDay(day);
        query.setMonth(month);
        query.setYear(year);
        query.setHourStart(hour);
        query.setMinuteStart(minute);

        getPendingSolicitation(query);
    }

    private void getPendingSolicitation(Query query) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getPendingSolicitaion(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseMain,this::handleError2));
    }

    private void handleResponseMain(Response response) {
        boolean isTherePendingSolicitation = response.getNumberFriendRequest() + response.getNumberInvitationRequest() > 0;
        if(isTherePendingSolicitation){
            notificationView.setVisibility(View.VISIBLE);
        }else {
            notificationView.setVisibility(View.GONE);
        }
    }

    private void updatePushNotification(UserPushNotification pushNotification) {

        mSubscriptions.add(NetworkUtil.getRetrofit().setPushNotification(pushNotification)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {}

    private void handleError(Throwable error) {
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        //else
        //    Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    private void handleError2(Throwable error) {}

    private void initInterface(Bundle savedInstanceState){
        mSubscriptions = new CompositeDisposable();
        mJobManager = JobManager.instance();
        facebookSDKInitialize();

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        String accountName = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE)
                .getString(Constants.PREF_ACCOUNT_NAME, null);

        if (accountName != null && !accountName.equals(""))
            mCredential.setSelectedAccountName(accountName);

        String token = FirebaseInstanceId.getInstance().getToken();
        if(token != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("Tymo");

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            if (!email.matches("")) {

                UserPushNotification pushNotification = new UserPushNotification();
                pushNotification.setEmail(email);
                pushNotification.setIdDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                pushNotification.setName(android.os.Build.BRAND + " " + android.os.Build.MODEL);
                pushNotification.setToken(token);
                pushNotification.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

                updatePushNotification(pushNotification);
            }
        }

        mNavigator = new FragmentNavigator(getFragmentManager(), new MainFragmentAdapter(), R.id.container);
        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.onCreate(savedInstanceState);

        notificationView = findViewById(R.id.notificationView);
        searchView = (MaterialSearchView) findViewById(R.id.searchView);
        fab = (FloatingActionButton) findViewById(R.id.addButton);
        icon1 = (ImageView) findViewById(R.id.icon1);
        icon2 = (ImageView) findViewById(R.id.icon2);
        icon3 = (ImageView) findViewById(R.id.icon3);
        icon4 = (ImageView) findViewById(R.id.icon4);
        icon1Box = (RelativeLayout) findViewById(R.id.icon1Box);
        icon2Box = (RelativeLayout) findViewById(R.id.icon2Box);
        icon3Box = (RelativeLayout) findViewById(R.id.icon3Box);
        icon4Box = (RelativeLayout) findViewById(R.id.icon4Box);
        actButton = (ImageView) findViewById(R.id.actButton);
        reminderButton = (ImageView) findViewById(R.id.reminderButton);
        flagButton = (ImageView) findViewById(R.id.flagIcon);
        mainMenu = (RelativeLayout) findViewById(R.id.mainMenu);
        addView = findViewById(R.id.addView);
        closeButton = (ImageView) findViewById(R.id.closeButton);

        //set button controller
        controller = new UpdateButtonController(this);
        controller.attach(false, null, icon1, null);
        controller.attach(false, null, icon2, null);
        controller.attach(false, null, icon3, null);
        controller.attach(false, null, icon4, null);
        controller.setMultiple(false);
        controller.updateAll(FEED,0,R.color.deep_purple_400, 0);

        searchView.bringToFront();

        icon1Box.setOnClickListener(this);
        icon2Box.setOnClickListener(this);
        icon3Box.setOnClickListener(this);
        icon4Box.setOnClickListener(this);
        actButton.setOnClickListener(this);
        reminderButton.setOnClickListener(this);
        flagButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            if(mNavigator!=null) {
                controller.updateAll(mNavigator.getCurrentPosition(), 0, R.color.deep_purple_400, 0);
                setCurrentTab(mNavigator.getCurrentPosition());
            }

            if(searchView!=null && searchView.isSearchOpen())
                mainMenu.setVisibility(View.INVISIBLE);

            if(savedInstanceState.getBoolean(ADD_VIEW_IS_VISIBLE))
                addView.setVisibility(View.VISIBLE);
        }

        searchViewInit();
        setCurrentTab(mNavigator.getCurrentPosition());

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.ENGAGEMENT);

        initAnimation();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        updateProfileMainInformation();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverSearch, new IntentFilter("search_update"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverFeed, new IntentFilter("feed_update"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverNotification, new IntentFilter("notification_update"));

        setActivityPeriodicJob();
    }

    private void initAnimation() {
        new Actor.Builder(SpringSystem.create(), closeButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "closeButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    Revealator.unreveal(addView)
                                            .withUnrevealDuration(50)
                                            .withCurvedTranslation()
                                            .start();
                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        new Actor.Builder(SpringSystem.create(), actButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    startActivityForResult(new Intent(v.getContext(), AddActivity.class), Constants.REGISTER_ACT);
                                    Revealator.unreveal(addView)
                                            .start();
                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        new Actor.Builder(SpringSystem.create(), flagButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    startActivityForResult(new Intent(v.getContext(), FlagActivity.class), Constants.REGISTER_ACT);
                                    Revealator.unreveal(addView)
                                            .start();
                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        new Actor.Builder(SpringSystem.create(), reminderButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    startActivityForResult(new Intent(v.getContext(), ReminderActivity.class), Constants.REGISTER_ACT);
                                    Revealator.unreveal(addView)
                                            .start();
                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        new Actor.Builder(SpringSystem.create(), fab)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "fab" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    Revealator.reveal(addView)
                                            .withRevealDuration(25)
                                            .withCurvedTranslation()
                                            .withChildsAnimation()
                                            .start();
                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

    }

    @Override
    public void onClick(View v) {
        if(v == icon1Box) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon1" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(FEED, 0, R.color.deep_purple_400, 0);
            FeedFragment feedFragment = (FeedFragment)mNavigator.getFragment(FEED);

            if(mNavigator.getCurrentPosition() == FEED)
                feedFragment.getRecyclerView().getRecyclerView().smoothScrollToPosition(0);

            setCurrentTab(FEED);
        }
        else if(v == icon2Box) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon2" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(PLANS, 0, R.color.deep_purple_400, 0);

            PlansFragment plansFragment = (PlansFragment)mNavigator.getFragment(PLANS);

            if (plansFragment!=null && mNavigator.getCurrentPosition() != PLANS)
                plansFragment.refreshLayout(false);

            setCurrentTab(PLANS);
        }
        else if(v == icon3Box) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon3" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(ABOUT, 0, R.color.deep_purple_400, 0);
            ProfileFragment profileFragment = (ProfileFragment)mNavigator.getFragment(ABOUT);

            if (profileFragment!=null)
                profileFragment.updateLayout();

            setCurrentTab(ABOUT);
        }
        else if(v == icon4Box){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon4" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            SearchFragment searchFragment = (SearchFragment)mNavigator.getFragment(SEARCH);
            if(searchFragment != null)
                searchFragment.doSearch(".");

            controller.updateAll(SEARCH,0,R.color.deep_purple_400, 0);
            mainMenu.setVisibility(View.INVISIBLE);
            searchView.showSearch(true);
            setCurrentTab(SEARCH);
        }

    }

    public FragmentNavigator getFragmentNavigator(){
        return mNavigator;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mNavigator!=null)
            mNavigator.onSaveInstanceState(outState);
        outState.putBoolean(ADD_VIEW_IS_VISIBLE, addView.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(mNavigator!=null) {
            controller.updateAll(mNavigator.getCurrentPosition(), 0, R.color.deep_purple_400, 0);
            setCurrentTab(mNavigator.getCurrentPosition());
        }

        if(searchView!=null && searchView.isSearchOpen())
            mainMenu.setVisibility(View.INVISIBLE);

        if(savedInstanceState.getBoolean(ADD_VIEW_IS_VISIBLE))
            addView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            int lastPosition = mNavigator.getLastPosition();
            if(lastPosition == SEARCH)
                lastPosition = FEED;
            setCurrentTab(lastPosition);
            controller.updateAll(lastPosition,0,R.color.deep_purple_400, 0);
            mainMenu.setVisibility(View.VISIBLE);
        }else
            moveTaskToBack(true);
    }

    public void setCurrentTab(int position) {
        mNavigator.showFragment(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
            callbackManager.onActivityResult(requestCode, resultCode, data);


        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }

        if(resultCode == RESULT_OK && requestCode == Constants.REGISTER_ACT){
            controller.updateAll(PLANS, 0, R.color.deep_purple_400, 0);
            setCurrentTab(PLANS);

            PlansFragment plansFragment = (PlansFragment)mNavigator.getFragment(PLANS);

            int d = data.getIntExtra("d",0);
            int m = data.getIntExtra("m",0);
            int y = data.getIntExtra("y",0);

            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.DATE, 1);
            int day2 = c2.get(Calendar.DAY_OF_MONTH);
            int month2 = c2.get(Calendar.MONTH);
            int year2 = c2.get(Calendar.YEAR);

            if((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
                getActivityStartToday();

            ArrayList<Integer> list = new ArrayList<>();
            list.add(d);
            list.add(m);
            list.add(y);

            TymoApplication.getInstance().setDate(list);
            TymoApplication.getInstance().setCreatedActivity(true);

            refresh = false;

            if (plansFragment!=null)
                plansFragment.updateLayout(d,m,y, true);
        }

        if(resultCode == RESULT_OK && requestCode == Constants.FILTER_RESULT){
            FilterWrapper wrap =
                    (FilterWrapper) data.getSerializableExtra("filter_att");

            filterServer = wrap.getFilterServer();

            String query = !searchView.getQuery().equals("") ? searchView.getQuery() : ".";

            filterServer.setQuery(query);

            SearchFragment searchFragment = (SearchFragment)mNavigator.getFragment(SEARCH);

            searchFragment.doSearchFilter(filterServer);
        }
    }

    private void searchViewInit(){
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(filterServer == null || !filterServer.isFilterFilled())
                    ((SearchFragment)mNavigator.getFragment(SEARCH)).doSearch(query);
                else
                    ((SearchFragment)mNavigator.getFragment(SEARCH)).doSearchFilter(filterServer);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setPressBackViewListener(new MaterialSearchView.PressBackViewListener() {
            @Override
            public boolean onPressBack() {
                int lastPosition = mNavigator.getLastPosition();
                if(lastPosition == SEARCH)
                    lastPosition = FEED;
                setCurrentTab(lastPosition);
                controller.updateAll(lastPosition,0,R.color.deep_purple_400, 0);
                mainMenu.setVisibility(View.VISIBLE);
                return true;
            }
        });

        searchView.setPressFilterViewListener(new MaterialSearchView.PressFilterViewListener() {
            @Override
            public void OnFilterPressed() {
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);

                if (filterServer != null)
                    intent.putExtra("filter_load", new FilterWrapper(filterServer));

                startActivityForResult(intent, Constants.FILTER_RESULT);
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    public void updateSearch(){
        SearchFragment searchFragment = (SearchFragment)mNavigator.getFragment(SEARCH);
        String query = searchView.getQuery();
        if(query.matches(""))
            query = ".";
        if(searchFragment != null && filterServer != null && !filterServer.isFilterFilled())
            searchFragment.doSearch(query);
    }

    public void updateFeed(){
        FeedFragment feedFragment = (FeedFragment) mNavigator.getFragment(FEED);
        if(feedFragment != null)
            feedFragment.setFeedRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        ForceUpdateChecker.with(this).onUpdateNeeded(this ,mSubscriptions).check();

        if(mNavigator != null) {
            PlansFragment plansFragment = (PlansFragment)mNavigator.getFragment(PLANS);
            ProfileFragment profileFragment = (ProfileFragment) mNavigator.getFragment(ABOUT);
            FeedFragment feedFragment = (FeedFragment) mNavigator.getFragment(FEED);
            SearchFragment searchFragment = (SearchFragment)mNavigator.getFragment(SEARCH);

            if(plansFragment != null && refresh && mNavigator.getCurrentPosition() == PLANS){
                int d, m, y;
                ArrayList<Integer> date = TymoApplication.getInstance().getDate();
                if(date == null) {
                    plansFragment.refreshLayout(false);
                }else {
                    d = date.get(0);
                    m = date.get(1);
                    y = date.get(2);
                    plansFragment.updateLayout(d,m,y, true);
                }

            }else
                refresh = true;

            if(profileFragment != null && mNavigator.getCurrentPosition() == ABOUT)
                profileFragment.updateLayout();


            if(feedFragment != null)
                feedFragment.setFeedRefresh();

            if(searchFragment != null && mNavigator.getCurrentPosition() == SEARCH)
                updateSearch();

            controller.updateAll(mNavigator.getCurrentPosition(),0,R.color.deep_purple_400, 0);
            setCurrentTab(mNavigator.getCurrentPosition());

            int d = getIntent().getIntExtra("d",0);
            int m = getIntent().getIntExtra("m",0);
            int y = getIntent().getIntExtra("y",0);

            if(d > 0 && m >= 0 && y > 0) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add(d);
                list.add(m);
                list.add(y);

                TymoApplication.getInstance().setDate(list);
                TymoApplication.getInstance().setCreatedActivity(true);

                refresh = false;

                if (plansFragment != null)
                    plansFragment.updateLayout(d, m, y, true);

                controller.updateAll(PLANS, 0, R.color.deep_purple_400, 0);
                setCurrentTab(PLANS);
            }
        }
    }

    private void setActivityPeriodicJob() {
        if(mJobManager.getAllJobRequestsForTag(ActivitySyncJob.TAG).size() == 0) {
            getActivityStartToday();
            ActivitySyncJob.schedule();
        }
    }

    private void getActivityStartToday(){
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        Query query = new Query();
        query.setEmail(email);
        query.setDay(day);
        query.setMonth(month);
        query.setYear(year);
        query.setHourStart(hour);
        query.setMinuteStart(minute);

        setNotifications(query);
    }

    private void setNotifications(Query query) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getActivityStartToday(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseToday, this::handleErrorToday));
    }

    private void handleResponseToday(Response response) {

        JobManager mJobManager = JobManager.instance();
        if(mJobManager.getAllJobRequestsForTag(NotificationSyncJob.TAG).size() > 0)
            mJobManager.cancelAllForTag(NotificationSyncJob.TAG);

        ArrayList<Object> list = new ArrayList<>();
        ArrayList<ActivityOfDay> list_notify = new ArrayList<>();

        if (response.getMyCommitAct() != null) {
            ArrayList<ActivityServer> activityServers = response.getMyCommitAct();
            for(int i=0;i<activityServers.size();i++){
                list.add(activityServers.get(i));
            }
        }
        if (response.getMyCommitFlag() != null) {
            ArrayList<FlagServer> flagServers = response.getMyCommitFlag();
            for(int i=0;i<flagServers.size();i++){
                list.add(flagServers.get(i));
            }
        }
        if (response.getMyCommitReminder() != null) {
            list.addAll(response.getMyCommitReminder());
        }

        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                ActivityServer activityServer;
                FlagServer flagServer;
                ReminderServer reminderServer;
                int start_hour = 0, start_minute = 0;
                int start_hour2 = 0, start_minute2 = 0;
                int end_hour = 0, end_minute = 0;
                int end_hour2 = 0, end_minute2 = 0;
                int day = 0, day2 = 0;
                int month = 0, month2 = 0;
                int year = 0, year2 = 0;

                // Activity
                if (c1 instanceof ActivityServer) {
                    activityServer = (ActivityServer) c1;
                    start_hour = activityServer.getHourStart();
                    start_minute = activityServer.getMinuteStart();
                    end_hour = activityServer.getHourEnd();
                    end_minute = activityServer.getMinuteEnd();

                    day = activityServer.getDayStart();
                    month = activityServer.getMonthStart();
                    year = activityServer.getYearStart();
                }
                // Flag
                else if (c1 instanceof FlagServer) {
                    flagServer = (FlagServer) c1;
                    start_hour = flagServer.getHourStart();
                    start_minute = flagServer.getMinuteStart();
                    end_hour = flagServer.getHourEnd();
                    end_minute = flagServer.getMinuteEnd();

                    day = flagServer.getDayStart();
                    month = flagServer.getMonthStart();
                    year = flagServer.getYearStart();
                }
                // Reminder
                else if (c1 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c1;
                    start_hour = reminderServer.getHourStart();
                    start_minute = reminderServer.getMinuteStart();

                    day = reminderServer.getDayStart();
                    month = reminderServer.getMonthStart();
                    year = reminderServer.getYearStart();
                }

                // Activity
                if (c2 instanceof ActivityServer) {
                    activityServer = (ActivityServer) c2;
                    start_hour2 = activityServer.getHourStart();
                    start_minute2 = activityServer.getMinuteStart();
                    end_hour2 = activityServer.getHourEnd();
                    end_minute2 = activityServer.getMinuteEnd();

                    day2 = activityServer.getDayStart();
                    month2 = activityServer.getMonthStart();
                    year2 = activityServer.getYearStart();
                }
                // Flag
                else if (c2 instanceof FlagServer) {
                    flagServer = (FlagServer) c2;
                    start_hour2 = flagServer.getHourStart();
                    start_minute2 = flagServer.getMinuteStart();
                    end_hour2 = flagServer.getHourEnd();
                    end_minute2 = flagServer.getMinuteEnd();

                    day2 = flagServer.getDayStart();
                    month2 = flagServer.getMonthStart();
                    year2 = flagServer.getYearStart();
                }
                // Reminder
                else if (c2 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c2;
                    start_hour2 = reminderServer.getHourStart();
                    start_minute2 = reminderServer.getMinuteStart();

                    day2 = reminderServer.getDayStart();
                    month2 = reminderServer.getMonthStart();
                    year2 = reminderServer.getYearStart();

                }

                if (year < year2)
                    return -1;
                else if (year > year2)
                    return 1;
                else if (month < month2)
                    return -1;
                else if (month > month2)
                    return 1;
                else if (day < day2)
                    return -1;
                else if (day > day2)
                    return 1;
                else if (start_hour < start_hour2)
                    return -1;
                else if (start_hour > start_hour2)
                    return 1;
                else if (start_minute < start_minute2)
                    return -1;
                else if (start_minute > start_minute2)
                    return 1;
                else if (end_hour < end_hour2)
                    return -1;
                else if (end_hour > end_hour2)
                    return 1;
                else if (end_minute < end_minute2)
                    return -1;
                else if (end_minute > end_minute2)
                    return 1;
                else
                    return 0;

            }
        });

        for (int i = 0; i < list.size(); i++) {
            // Activity
            if (list.get(i) instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) list.get(i);
                list_notify.add(new ActivityOfDay(activityServer.getTitle(), activityServer.getMinuteStart(), activityServer.getHourStart(), Constants.ACT,
                        activityServer.getDayStart(),activityServer.getMonthStart(),activityServer.getYearStart()));
            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);
                list_notify.add(new ActivityOfDay(flagServer.getTitle(), flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
                        flagServer.getDayStart(),flagServer.getMonthStart(),flagServer.getYearStart()));
            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);
                list_notify.add(new ActivityOfDay(reminderServer.getTitle(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
                        reminderServer.getDayStart(),reminderServer.getMonthStart(),reminderServer.getYearStart()));
            }
        }

        int time_exact;
        long time_to_happen;
        Calendar c3 = Calendar.getInstance();

        for (int i = 0; i < list_notify.size(); i++) {
            PersistableBundleCompat extras = new PersistableBundleCompat();
            extras.putInt("position_act", i);

            PersistableBundleCompat extras2 = new PersistableBundleCompat();
            extras2.putInt("position_act", i);
            extras2.putBoolean("day_before", true);

            int j = i;
            int count_same = 0;
            ActivityOfDay activityOfDay = list_notify.get(i);
            ActivityOfDay activityOfDayNext = list_notify.get(j);

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            c1.set(Calendar.DAY_OF_MONTH, activityOfDay.getDay());
            c1.set(Calendar.MONTH, activityOfDay.getMonth()-1);
            c1.set(Calendar.YEAR, activityOfDay.getYear());
            c1.set(Calendar.HOUR_OF_DAY, activityOfDay.getHourStart());
            c1.set(Calendar.MINUTE, activityOfDay.getMinuteStart());
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
            c2.set(Calendar.MONTH, activityOfDayNext.getMonth()-1);
            c2.set(Calendar.YEAR, activityOfDayNext.getYear());
            c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
            c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);

            while(activityOfDayNext !=null && c1.getTimeInMillis() == c2.getTimeInMillis()) {
                j++;
                count_same++;
                if(j < list_notify.size()) {
                    activityOfDayNext = list_notify.get(j);
                    c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
                    c2.set(Calendar.MONTH, activityOfDayNext.getMonth()-1);
                    c2.set(Calendar.YEAR, activityOfDayNext.getYear());
                    c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
                    c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
                    c2.set(Calendar.SECOND, 0);
                    c2.set(Calendar.MILLISECOND, 0);
                }
                else
                    activityOfDayNext = null;
            }
            activityOfDay.setCommitmentSameHour(count_same);

            time_exact = (int)(c1.getTimeInMillis()-c3.getTimeInMillis())/(1000*60);
            if(time_exact >= Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT) {
                c1.add(Calendar.MINUTE, -Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT);
                time_to_happen = c1.getTimeInMillis()-c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            if(time_exact >= 1440) {
                c1.add(Calendar.MINUTE, -1380);
                time_to_happen = c1.getTimeInMillis()-c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras2)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            i=j-1;
        }

        if (list_notify.size() > 0) {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
            Gson gson = new Gson();
            String json = gson.toJson(list_notify);
            editor.putString("ListActDay", json);
            editor.apply();
        }
    }

    private void handleErrorToday(Throwable error) {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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

            calendar.add(Calendar.MONTH, 3);
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
        protected void onPreExecute() {}

        @Override
        protected void onPostExecute(Integer output) {
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                }
            }
        }
    }
}
