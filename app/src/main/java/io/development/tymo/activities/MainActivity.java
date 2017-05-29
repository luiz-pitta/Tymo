package io.development.tymo.activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jaouan.revealator.Revealator;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.util.ArrayList;
import java.util.Calendar;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.fragments.FeedFragment;
import io.development.tymo.fragments.PlansFragment;
import io.development.tymo.fragments.ProfileFragment;
import io.development.tymo.fragments.SearchFragment;
import io.development.tymo.model_server.FilterServer;
import io.development.tymo.model_server.FilterWrapper;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.UpdateButtonController;
import io.development.tymo.utils.Utilities;
import io.development.tymo.adapters.MainFragmentAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ADD_VIEW_IS_VISIBLE = "add_is_visible";

    private FragmentNavigator mNavigator;
    private UpdateButtonController controller;
    private RelativeLayout mainMenu;

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

    private String email;
    private View notificationView;

    private boolean refresh = true;
    private FilterServer filterServer = null;

    private Rect rect;

    private static final int FEED = 0;
    private static final int PLANS = 1;
    private static final int ABOUT = 2;
    private static final int SEARCH = 3;

    private View addView;

    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSearch();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubscriptions = new CompositeSubscription();

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

        icon1.setOnClickListener(this);
        icon2.setOnClickListener(this);
        icon3.setOnClickListener(this);
        icon4.setOnClickListener(this);
        actButton.setOnClickListener(this);
        reminderButton.setOnClickListener(this);
        flagButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        if (savedInstanceState != null && savedInstanceState.getBoolean(ADD_VIEW_IS_VISIBLE)) {
            addView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
            controller.updateAll(mNavigator.getCurrentPosition(),0,R.color.deep_purple_400, 0);
            setCurrentTab(mNavigator.getCurrentPosition());
        }

        searchViewInit();
        setCurrentTab(mNavigator.getCurrentPosition());

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.ENGAGEMENT);

        initAnimation();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        updateProfileMainInformation();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("search_update"));
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
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    private void handleError2(Throwable error) {}

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
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "closeButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "fab" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
        if(v == icon1) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon1" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(FEED, 0, R.color.deep_purple_400, 0);
            FeedFragment feedFragment = (FeedFragment)mNavigator.getFragment(FEED);

            if(mNavigator.getCurrentPosition() == FEED)
                feedFragment.getRecyclerView().getRecyclerView().smoothScrollToPosition(0);

            setCurrentTab(FEED);
        }
        else if(v == icon2) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon2" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(PLANS, 0, R.color.deep_purple_400, 0);

            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            PlansFragment plansFragment = (PlansFragment)mNavigator.getFragment(PLANS);

            if (plansFragment!=null && mNavigator.getCurrentPosition() != PLANS)
                plansFragment.updateLayout(day, month, year, false);

            setCurrentTab(PLANS);
        }
        else if(v == icon3) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon3" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(ABOUT, 0, R.color.deep_purple_400, 0);
            ProfileFragment profileFragment = (ProfileFragment)mNavigator.getFragment(ABOUT);

            if (profileFragment!=null)
                profileFragment.updateLayout();

            setCurrentTab(ABOUT);
        }
        else if(v == icon4){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon4" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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

            filterServer.setQuery(searchView.getQuery());

            SearchFragment searchFragment = (SearchFragment)mNavigator.getFragment(SEARCH);

            searchFragment.doSearchFilter(filterServer);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void searchViewInit(){
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ((SearchFragment)mNavigator.getFragment(SEARCH)).doSearch(query);
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
        if(searchFragment != null)
            searchFragment.doSearch(query);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        if(mNavigator != null) {
            PlansFragment plansFragment = (PlansFragment)mNavigator.getFragment(PLANS);
            ProfileFragment profileFragment = (ProfileFragment) mNavigator.getFragment(ABOUT);
            FeedFragment feedFragment = (FeedFragment) mNavigator.getFragment(FEED);
            SearchFragment searchFragment = (SearchFragment)mNavigator.getFragment(SEARCH);

            if(plansFragment != null && refresh && mNavigator.getCurrentPosition() == PLANS){
                int d, m, y;
                ArrayList<Integer> date = TymoApplication.getInstance().getDate();
                if(date == null) {
                    Calendar cal = Calendar.getInstance();
                    d = cal.get(Calendar.DAY_OF_MONTH);
                    m = cal.get(Calendar.MONTH);
                    y = cal.get(Calendar.YEAR);
                }else {
                    d = date.get(0);
                    m = date.get(1)-1;
                    y = date.get(2);
                }
                plansFragment.updateLayout(d,m,y, false);
            }else
                refresh = true;

            if(profileFragment != null && mNavigator.getCurrentPosition() == ABOUT)
                profileFragment.updateLayout();

            if(feedFragment != null && mNavigator.getCurrentPosition() == FEED)
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
