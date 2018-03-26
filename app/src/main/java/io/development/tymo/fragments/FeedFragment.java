package io.development.tymo.fragments;


import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.development.tymo.BuildConfig;
import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.activities.FilterActivity;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.adapters.FeedFragmentAdapter;
import io.development.tymo.adapters.SelectionCalendarAdapter;
import io.development.tymo.adapters.SelectionRepeatActivitiesFeedAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.BgFeedServer;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FilterServer;
import io.development.tymo.model_server.FilterWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static io.development.tymo.utils.AlgorithmFeedSearch.algorithmFeedSearchWhats;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ArrayList<BgFeedServer> bgFeed = new ArrayList<>();

    private int period = 3;
    private boolean try_again = true;

    private Calendar currentTime;
    int d_notify, m_notify, y_notify;

    private TextView filterText, zoomText;

    private FragmentNavigator mNavigator;
    private ImageView filter, detail;
    private LinearLayout feedIgnoreButton, feedCheckButton, buttonsBar, filterButtonBox, zoomButtonBox;
    private ImageView cancelButtonImage, checkButtonImage;
    private Rect rect;
    private FilterServer filterServer = null;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private SharedPreferences mSharedPreferences;
    private ImageView backgroundFeed, backgroundCloud1, backgroundCloud2;
    private static int currentSecond, currentMinute, currentHour;

    private List<Object> listFeed = new ArrayList<>();
    private int currentPosition = 0;
    private double lat = -500, lng = -500;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private boolean activatedCheck = true;

    public static Fragment newInstance(String text) {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= 17)
            mNavigator = new FragmentNavigator(getChildFragmentManager(), new FeedFragmentAdapter(), R.id.containerFeed);
        else
            mNavigator = new FragmentNavigator(getFragmentManager(), new FeedFragmentAdapter(), R.id.containerFeed);


        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.setTransition(true);
        mNavigator.onCreate(savedInstanceState);

        // [START shared_app_measurement]
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        // [END shared_app_measurement]

        mSubscriptions = new CompositeDisposable();
        mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);

        view.findViewById(R.id.checkText).setVisibility(View.GONE);
        view.findViewById(R.id.ignoreText).setVisibility(View.GONE);

        filterButtonBox = (LinearLayout) view.findViewById(R.id.filterButtonBox);
        zoomButtonBox = (LinearLayout) view.findViewById(R.id.zoomButtonBox);
        filter = (ImageView) view.findViewById(R.id.filterButton);
        detail = (ImageView) view.findViewById(R.id.zoomButton);
        filterText = (TextView) view.findViewById(R.id.filterText);
        zoomText = (TextView) view.findViewById(R.id.zoomText);
        feedIgnoreButton = (LinearLayout) view.findViewById(R.id.feedIgnoreButton);
        feedCheckButton = (LinearLayout) view.findViewById(R.id.feedCheckButton);
        cancelButtonImage = (ImageView) view.findViewById(R.id.ignoreButton);
        checkButtonImage = (ImageView) view.findViewById(R.id.checkButton);
        backgroundFeed = (ImageView) view.findViewById(R.id.backgroundFeed);
        backgroundCloud1 = (ImageView) view.findViewById(R.id.backgroundCloud1);
        backgroundCloud2 = (ImageView) view.findViewById(R.id.backgroundCloud2);
        buttonsBar = (LinearLayout) view.findViewById(R.id.buttonsBar);

        filterButtonBox.setOnClickListener(this);
        zoomButtonBox.setOnClickListener(this);
        feedIgnoreButton.setVisibility(View.INVISIBLE);
        feedCheckButton.setVisibility(View.INVISIBLE);

        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, -1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(20000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                float height = backgroundCloud1.getHeight();
                float setTranslationY = height * progress;
                backgroundCloud1.setTranslationY(setTranslationY);
                backgroundCloud2.setTranslationY(setTranslationY + height);
            }
        });
        animator.start();

        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, -Utilities.convertDpToPixel(600, view.getContext()), Utilities.convertDpToPixel(600, view.getContext()));
        animation.setDuration(3000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RELATIVE_TO_SELF);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);

        initAnimation();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        setFeed();

        // [START set_current_screen]
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
        // [END set_current_screen]

        setCurrentTab(mNavigator.getCurrentPosition());
    }

    private void getBgFeed() {
        mSubscriptions.add(NetworkUtil.getRetrofit().getBgFeed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(ArrayList<BgFeedServer> bgFeed) {
        this.bgFeed = new ArrayList<>();
        this.bgFeed.addAll(bgFeed);
        isTimeToChangeBackground();
    }

    private void setBackgroundFeed() {
        String timeStart, timeEnd;
        String currentTime = String.format("%02d", currentHour) + ":" + String.format("%02d", currentMinute);
        int period = 0;
        String urlBg = "";
        String urlCloud = "";

        for (int i = 0; i < bgFeed.size(); i++) {
            timeStart = String.format("%02d", bgFeed.get(i).getHourStart()) + ":" + String.format("%02d", bgFeed.get(i).getMinuteStart());
            timeEnd = String.format("%02d", bgFeed.get(i).getHourEnd()) + ":" + String.format("%02d", bgFeed.get(i).getMinuteEnd());

            if (isTimeInBetween(currentTime, timeStart, timeEnd)) {
                period = bgFeed.get(i).getPeriod();
                urlBg = bgFeed.get(i).getUrlBg();
                urlCloud = bgFeed.get(i).getUrlBg2();
            }
        }

        if (bgFeed.size() > 0) {

            setPeriod(period);

            if (period == 3) {
                buttonsBar.setBackgroundResource(R.drawable.bg_feed_bottom_bar_morning);
                filterText.setTextColor(getResources().getColor(R.color.deep_purple_400));
                zoomText.setTextColor(getResources().getColor(R.color.deep_purple_400));
                detail.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                detail.setBackgroundResource(R.drawable.btn_feed_filter_morning);
            } else if (period == 2 || period == 4) {
                buttonsBar.setBackgroundResource(R.drawable.bg_feed_bottom_bar_sunset);
                filterText.setTextColor(getResources().getColor(R.color.white));
                zoomText.setTextColor(getResources().getColor(R.color.white));
                detail.setColorFilter(getResources().getColor(R.color.white));
                detail.setBackgroundResource(R.drawable.btn_feed_filter_sunset);
            } else {
                buttonsBar.setBackgroundResource(R.drawable.bg_feed_bottom_bar_night);
                filterText.setTextColor(getResources().getColor(R.color.white));
                zoomText.setTextColor(getResources().getColor(R.color.white));
                detail.setColorFilter(getResources().getColor(R.color.white));
                detail.setBackgroundResource(R.drawable.btn_feed_filter_night);
            }

            if(filterServer != null && filterServer.isFilterFilled()) {
                if (period == 3) {
                    filter.setColorFilter(getResources().getColor(R.color.white));
                    filter.setBackgroundResource(R.drawable.btn_feed_filter_morning_activated);
                } else if (period == 2 || period == 4) {
                    filter.setColorFilter(getResources().getColor(R.color.white));
                    filter.setBackgroundResource(R.drawable.btn_feed_filter_sunset_activated);
                } else {
                    filter.setColorFilter(getResources().getColor(R.color.white));
                    filter.setBackgroundResource(R.drawable.btn_feed_filter_night_activated);
                }
            }else {
                if (period == 3) {
                    filter.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                    filter.setBackgroundResource(R.drawable.btn_feed_filter_morning);
                } else if (period == 2 || period == 4) {
                    filter.setColorFilter(getResources().getColor(R.color.white));
                    filter.setBackgroundResource(R.drawable.btn_feed_filter_sunset);
                } else {
                    filter.setColorFilter(getResources().getColor(R.color.white));
                    filter.setBackgroundResource(R.drawable.btn_feed_filter_night);
                }
            }

            Glide.clear(backgroundFeed);
            Glide.with(getActivity())
                    .load(urlBg)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable drawable = new BitmapDrawable(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                backgroundFeed.setBackground(drawable);
                            } else
                                backgroundFeed.setBackgroundDrawable(drawable);
                        }
                    });

            Glide.clear(backgroundCloud1);
            Glide.with(getActivity())
                    .load(urlCloud)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable drawable = new BitmapDrawable(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                backgroundCloud1.setBackground(drawable);
                            } else
                                backgroundCloud1.setBackgroundDrawable(drawable);
                        }
                    });

            Glide.clear(backgroundCloud2);
            Glide.with(getActivity())
                    .load(urlCloud)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable drawable = new BitmapDrawable(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                backgroundCloud2.setBackground(drawable);
                            } else
                                backgroundCloud2.setBackgroundDrawable(drawable);
                        }
                    });

        }
    }

    private boolean isTimeInBetween(String now, String timeStart, String timeEnd) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(timeStart);
            Date date3 = sdf.parse(timeEnd);

            return date1.after(date2) && date1.before(date3);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return this.period;
    }

    private void isTimeToChangeBackground(){
        currentTime = Calendar.getInstance();
        currentSecond = currentTime.get(Calendar.SECOND);
        currentMinute = currentTime.get(Calendar.MINUTE);
        currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        setBackgroundFeed();
    }

    public void updateLayout() {
        isTimeToChangeBackground();

        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
        if (feedListFragment != null)
            feedListFragment.setProgress(true);

        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        DateTymo dateTymo = new DateTymo();
        dateTymo.setDay(day);
        dateTymo.setMonth(month);
        dateTymo.setYear(year);
        dateTymo.setMinute(minute);
        dateTymo.setHour(hour);
        dateTymo.setDateTimeNow(c.getTimeInMillis());
        dateTymo.setLatCurrent(TymoApplication.getInstance().getLatLng().get(0));
        dateTymo.setLngCurrent(TymoApplication.getInstance().getLatLng().get(1));

        retrieveFeedActivities(email, dateTymo);
    }

    public List<Object> getListFeed() {
        return listFeed;
    }

    public void setFeed() {
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        DateTymo dateTymo = new DateTymo();
        dateTymo.setDay(day);
        dateTymo.setMonth(month);
        dateTymo.setYear(year);
        dateTymo.setMinute(minute);
        dateTymo.setHour(hour);
        dateTymo.setDateTimeNow(c.getTimeInMillis());

        getBgFeed();

        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
        if (feedListFragment != null)
            feedListFragment.setProgress(true);


        //retrieveFeedActivities(email, dateTymo);
    }

    private void retrieveFeedFilter(FilterServer filterServer) {
        if (period == 3) {
            filter.setColorFilter(getResources().getColor(R.color.white));
            filter.setBackgroundResource(R.drawable.btn_feed_filter_morning_activated);
        } else if (period == 2 || period == 4) {
            filter.setColorFilter(getResources().getColor(R.color.white));
            filter.setBackgroundResource(R.drawable.btn_feed_filter_sunset_activated);
        } else {
            filter.setColorFilter(getResources().getColor(R.color.white));
            filter.setBackgroundResource(R.drawable.btn_feed_filter_night_activated);
        }

        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
        if (feedListFragment != null)
            feedListFragment.setProgress(true);

        mSubscriptions.add(NetworkUtil.getRetrofit().getFeedFilter(email, filterServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFilter, this::handleError));
    }

    private void retrieveFeedActivities(String email, DateTymo dateTymo) {
        if (period == 3) {
            filter.setColorFilter(getResources().getColor(R.color.deep_purple_400));
            filter.setBackgroundResource(R.drawable.btn_feed_filter_morning);
        } else if (period == 2 || period == 4) {
            filter.setColorFilter(getResources().getColor(R.color.white));
            filter.setBackgroundResource(R.drawable.btn_feed_filter_sunset);
        } else {
            filter.setColorFilter(getResources().getColor(R.color.white));
            filter.setBackgroundResource(R.drawable.btn_feed_filter_night);
        }

        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
        if (feedListFragment != null)
            feedListFragment.setProgress(true);

        dateTymo.setIdDevice(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));
        dateTymo.setCurrentVersionApp(BuildConfig.VERSION_NAME);
        mSubscriptions.add(NetworkUtil.getRetrofit().getFeedActivities(email, dateTymo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        User usr = response.getUser();
        editor.putBoolean(Constants.LOCATION, usr.isLocationGps());
        editor.putBoolean(Constants.NOTIFICATION_ACT, usr.isNotificationActivity());
        editor.putBoolean(Constants.NOTIFICATION_FLAG, usr.isNotificationFlag());
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, usr.isNotificationReminder());
        editor.putBoolean(Constants.NOTIFICATION_PUSH, usr.isNotificationPush());
        editor.apply();

        ArrayList<ActivityServer> whats_going_act = response.getWhatsGoingAct();
        ArrayList<FlagServer> whats_going_flagServer = response.getWhatsGoingFlag();

        listFeed.clear();

        listFeed.addAll(whats_going_act);
        listFeed.addAll(whats_going_flagServer);

        Collections.sort(listFeed, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmFeedSearchWhats(c1, c2, false, false, false, getActivity());
            }
        });

        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
        feedListFragment.setAdapterItens(listFeed);
        feedListFragment.setProgress(false);

        FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
        if (feedCardFragment != null) {

            feedCardFragment.setAdapterItens(listFeed);

            if (listFeed.size() == 0 || mNavigator.getCurrentPosition() == 0) {
                feedIgnoreButton.setVisibility(View.INVISIBLE);
                feedCheckButton.setVisibility(View.INVISIBLE);
            } else {
                feedIgnoreButton.setVisibility(View.VISIBLE);
                feedCheckButton.setVisibility(View.VISIBLE);
            }
        }

        isTimeToChangeBackground();
    }

    public void setAdapterItensCard(List<Object> list) {
        listFeed.clear();
        listFeed.addAll(list);

        FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
        if (feedCardFragment != null)
            feedCardFragment.setAdapterItens(list);
    }

    private void handleResponseFilter(Response response) {
        int i;
        ArrayList<ActivityServer> whats_going_act = response.getWhatsGoingAct();
        ArrayList<FlagServer> whats_going_flagServer = response.getWhatsGoingFlag();

        listFeed.clear();

        if (filterServer.getLat() != -500) {

            for (i = 0; i < whats_going_act.size(); i++) {
                ActivityServer activityServer = whats_going_act.get(i);
                if (activityServer.getLat() != -500
                        && Utilities.distance(activityServer.getLat(), activityServer.getLng(),
                        filterServer.getLat(), filterServer.getLng()) > 10) {
                    whats_going_act.remove(activityServer);
                    i--;
                } else if (activityServer.getLat() == -500 || (activityServer.getLat() == 0 && activityServer.getLng() == 0)) {
                    whats_going_act.remove(activityServer);
                    i--;
                }
            }

            whats_going_flagServer.clear();
        }

        if (filterServer.getWeekDays().size() > 0) {
            List<Integer> listWeek = filterServer.getWeekDays();

            for (i = 0; i < whats_going_act.size(); i++) {
                ActivityServer activityServer = whats_going_act.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, activityServer.getDayStart(),
                        activityServer.getMonthStart(), activityServer.getYearStart(),activityServer.getDayEnd(),
                        activityServer.getMonthEnd(), activityServer.getYearEnd())) {
                    whats_going_act.remove(activityServer);
                    i--;
                }
            }

            for (i = 0; i < whats_going_flagServer.size(); i++) {
                FlagServer flagServer = whats_going_flagServer.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, flagServer.getDayStart(),
                        flagServer.getMonthStart(), flagServer.getYearStart(),flagServer.getDayEnd(),
                        flagServer.getMonthEnd(), flagServer.getYearEnd())) {
                    whats_going_flagServer.remove(flagServer);
                    i--;
                }
            }
        }

        listFeed.addAll(whats_going_act);
        listFeed.addAll(whats_going_flagServer);

        Collections.sort(listFeed, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmFeedSearchWhats(c1, c2, filterServer.getProximity(), filterServer.getPopularity(), filterServer.getDateHour(), getActivity());
            }
        });

        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
        feedListFragment.setAdapterItens(listFeed);
        feedListFragment.setProgress(false);

        FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
        if (feedCardFragment != null) {

            feedCardFragment.setAdapterItens(listFeed);

            if (listFeed.size() == 0 || mNavigator.getCurrentPosition() == 0) {
                feedIgnoreButton.setVisibility(View.INVISIBLE);
                feedCheckButton.setVisibility(View.INVISIBLE);
            } else {
                feedIgnoreButton.setVisibility(View.VISIBLE);
                feedCheckButton.setVisibility(View.VISIBLE);
            }
        }

        isTimeToChangeBackground();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.permission_location), Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    private void handleError(Throwable error) {
        if (listFeed.size() == 0) {
            if(try_again) {
                try_again = false;
                setFeedRefresh();
            }else {
                FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                feedListFragment.setAdapterItens(listFeed);
            }
        }else {
            if(Utilities.isDeviceOnline(getActivity()))
                Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getActivity(), getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
        }
    }

    private void initAnimation() {
        new Actor.Builder(SpringSystem.create(), cancelButtonImage)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                                    FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
                                    if (feedCardFragment != null)
                                        feedCardFragment.rejectActivity();

                                    // [START image_view_event]
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancelButtonImage" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                    // [END image_view_event]
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

        new Actor.Builder(SpringSystem.create(), checkButtonImage)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()) && activatedCheck) {
                                    FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
                                    if (feedCardFragment != null)
                                        feedCardFragment.confirmActivity();

                                    // [START image_view_event]
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "checkButtonImage" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                    // [END image_view_event]
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


        new Actor.Builder(SpringSystem.create(), filterButtonBox)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                /*if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                                    Intent intent = new Intent(getActivity(), FilterActivity.class);

                                    // [START image_view_event]
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "filter" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                    // [END image_view_event]

                                    if (filterServer != null && filterServer.isFilterFilled())
                                        intent.putExtra("filter_load", new FilterWrapper(filterServer));

                                    startActivityForResult(intent, Constants.FILTER_RESULT);
                                }*/
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        new Actor.Builder(SpringSystem.create(), zoomButtonBox)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                                    /*int change = mNavigator.getCurrentPosition();
                                    if (change == 0) {
                                        detail.setImageResource(R.drawable.ic_zoom_less);
                                        if (listFeed.size() == 0) {
                                            feedIgnoreButton.setVisibility(View.INVISIBLE);
                                            feedCheckButton.setVisibility(View.INVISIBLE);
                                        } else {
                                            feedIgnoreButton.setVisibility(View.VISIBLE);
                                            feedCheckButton.setVisibility(View.VISIBLE);
                                        }
                                        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                                        FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);

                                        currentPosition = feedListFragment.getCurrentPosition();

                                        if (feedCardFragment != null)
                                            feedCardFragment.setCurrentPosition(currentPosition);

                                        // [START image_view_event]
                                        Bundle bundle = new Bundle();
                                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "ic_zoom_less" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                        // [END image_view_event]

                                        setCurrentTab(1);
                                    } else {
                                        detail.setImageResource(R.drawable.ic_zoom_more);
                                        feedIgnoreButton.setVisibility(View.INVISIBLE);
                                        feedCheckButton.setVisibility(View.INVISIBLE);

                                        FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                                        FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);

                                        currentPosition = feedCardFragment.getCurrentPosition();

                                        feedListFragment.setCurrentPosition(currentPosition);

                                        // [START image_view_event]
                                        Bundle bundle = new Bundle();
                                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "ic_zoom_more" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                                        // [END image_view_event]

                                        setCurrentTab(0);
                                    }*/
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

    public void setFeedIgnoreCheckButton(boolean set) {
        if (set) {
            checkButtonImage.setBackgroundResource(R.drawable.btn_feed_check_activated);
            activatedCheck = false;
        } else {
            checkButtonImage.setBackgroundResource(R.drawable.btn_feed_check);
            activatedCheck = true;
        }
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.FILTER_RESULT) {

            FilterWrapper wrap =
                    (FilterWrapper) data.getSerializableExtra("filter_att");

            filterServer = wrap.getFilterServer();

            filterServer.setLatCurrent(TymoApplication.getInstance().getLatLng().get(0));
            filterServer.setLngCurrent(TymoApplication.getInstance().getLatLng().get(1));

            if(filterServer.isFilterFilled())
                retrieveFeedFilter(filterServer);
            else
                updateLayout();
        }
    }

    public void setFeedRefresh(){
        ((MainActivity) getActivity()).updateProfileMainInformation();

        if(filterServer != null && filterServer.isFilterFilled())
            retrieveFeedFilter(filterServer);
        else
            updateLayout();

        if (mNavigator.getCurrentPosition() == 1) {
            detail.setImageResource(R.drawable.ic_zoom_more);
            feedIgnoreButton.setVisibility(View.INVISIBLE);
            feedCheckButton.setVisibility(View.INVISIBLE);

            FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);

            currentPosition = 0;

            feedListFragment.setCurrentPosition(currentPosition);

            setCurrentTab(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setCurrentTab(int position) {
        mNavigator.showFragment(position);
    }

    public EasyRecyclerView getRecyclerView() {
        return ((FeedListFragment) mNavigator.getFragment(0)).getRecyclerView();
    }

    @Override
    public void onClick(View view) {
        if(view == filterButtonBox){
            Intent intent = new Intent(getActivity(), FilterActivity.class);

            // [START image_view_event]
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "filter" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            // [END image_view_event]

            if (filterServer != null && filterServer.isFilterFilled())
                intent.putExtra("filter_load", new FilterWrapper(filterServer));

            startActivityForResult(intent, Constants.FILTER_RESULT);
        }else if(view == zoomButtonBox){
            int change = mNavigator.getCurrentPosition();
            if (change == 0) {
                detail.setImageResource(R.drawable.ic_zoom_less);
                if (listFeed.size() == 0) {
                    feedIgnoreButton.setVisibility(View.INVISIBLE);
                    feedCheckButton.setVisibility(View.INVISIBLE);
                } else {
                    feedIgnoreButton.setVisibility(View.VISIBLE);
                    feedCheckButton.setVisibility(View.VISIBLE);
                }
                FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);

                currentPosition = feedListFragment.getCurrentPosition();

                if (feedCardFragment != null)
                    feedCardFragment.setCurrentPosition(currentPosition);

                // [START image_view_event]
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "ic_zoom_less" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                // [END image_view_event]

                setCurrentTab(1);
            } else {
                detail.setImageResource(R.drawable.ic_zoom_more);
                feedIgnoreButton.setVisibility(View.INVISIBLE);
                feedCheckButton.setVisibility(View.INVISIBLE);

                FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);

                currentPosition = feedCardFragment.getCurrentPosition();

                feedListFragment.setCurrentPosition(currentPosition);

                // [START image_view_event]
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "ic_zoom_more" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                // [END image_view_event]

                setCurrentTab(0);
            }
        }
    }

    public void createDialogRepeatImport(ArrayList<Object> activities, int currentPosition, int fragment_type) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_list_multiple_select, null);

        SelectionRepeatActivitiesFeedAdapter selectionRepeatActivitiesFeedAdapter;

        TextView text1 = customView.findViewById(R.id.text1);
        TextView text2 = customView.findViewById(R.id.text2);
        TextView buttonText1 = customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = customView.findViewById(R.id.buttonText2);
        RecyclerView mMultiChoiceRecyclerView = customView.findViewById(R.id.recyclerSelectView);

        text1.setText(getResources().getString(R.string.dialog_fit_multiple_repeat_activity_title));
        text2.setText(getResources().getString(R.string.dialog_fit_multiple_repeat_activity_text));
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.fit));

        mMultiChoiceRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMultiChoiceRecyclerView.setNestedScrollingEnabled(false);

        selectionRepeatActivitiesFeedAdapter = new SelectionRepeatActivitiesFeedAdapter(activities, getActivity()) ;
        mMultiChoiceRecyclerView.setAdapter(selectionRepeatActivitiesFeedAdapter);
        selectionRepeatActivitiesFeedAdapter.setSingleClickMode(true);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(true);

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);
        mMultiChoiceRecyclerView.setHasFixedSize(true);

        Dialog dialog = new Dialog(getActivity(), R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(false);

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
                if(fragment_type == 0) {
                    FeedListFragment feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                    if (feedListFragment != null)
                        feedListFragment.insertActivityBack(activities.get(0), currentPosition);
                }else if(fragment_type == 1){
                    FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
                    if (feedCardFragment != null)
                        feedCardFragment.insertActivityBack(activities.get(0), currentPosition);
                }
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minPosition = 500;
                InviteRequest inviteRequest = new InviteRequest();
                ActivityServer activityServer = null;
                FlagServer flagServer = null;

                List<Integer> list = selectionRepeatActivitiesFeedAdapter.getSelectedItemList();

                for(int i=0;i<list.size();i++){
                    int position = list.get(i);
                    Object item = activities.get(position);
                    if (item instanceof ActivityServer) {
                        inviteRequest.setType(Constants.ACT);
                        activityServer = (ActivityServer) item;
                        inviteRequest.addIds(activityServer.getId());
                    } else if (item instanceof FlagServer) {
                        inviteRequest.setType(Constants.FLAG);
                        flagServer = (FlagServer) item;
                        inviteRequest.addIds(flagServer.getId());
                    }

                    if(position < minPosition){
                        minPosition = position;
                        if (activityServer != null) {
                            d_notify = activityServer.getDayStart();
                            m_notify = activityServer.getMonthStart();
                            y_notify = activityServer.getYearStart();
                        } else if (flagServer  != null) {
                            d_notify = flagServer.getDayStart();
                            m_notify = flagServer.getMonthStart();
                            y_notify = flagServer.getYearStart();
                        }
                    }
                }

                SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                inviteRequest.setEmail(email);
                inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                inviteRequest.setStatus(Constants.YES);

                updateInviteRequest(inviteRequest);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateInviteRequest(InviteRequest inviteRequest) {

        mSubscriptions.add(NetworkUtil.getRetrofit().updateInvitesRequest(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm,this::handleError));
    }

    private void handleDeleteIgnoreConfirm(Response response) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int year2 = c2.get(Calendar.YEAR);


        if((d_notify == day && m_notify == month && y_notify == year) || (d_notify == day2 && m_notify == month2 && y_notify == year2)) {
            d_notify = -1;
            m_notify = -1;
            y_notify = -1;
            Intent intent = new Intent("notification_update");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        int currentFragment = mNavigator.getCurrentPosition();
        FeedListFragment feedListFragment;
        FeedCardFragment feedCardFragment;
        switch (currentFragment){
            case 0:
                feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                setAdapterItensCard(feedListFragment.getListFeed());
                break;
            case 1:
                feedListFragment = (FeedListFragment) mNavigator.getFragment(0);
                feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);
                if (feedListFragment != null)
                    feedListFragment.setAdapterItens(feedCardFragment.getListFeed());
                break;
        }

        //Toast.makeText(getActivity(), ServerMessage.getServerMessage(getActivity(), response.getMessage()), Toast.LENGTH_LONG).show();
        //ACTIVITY_DELETED_SUCCESSFULLY, RELATIONSHIP_UPDATED_SUCCESSFULLY e WITHOUT_NOTIFICATION
    }

    @Override
    public void onResume() {
        super.onResume();

        isTimeToChangeBackground();

        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity(), this, this).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location lastLocation) {
        lat = lastLocation.getLatitude();
        lng = lastLocation.getLongitude();

        FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);

        if (feedCardFragment != null)
            feedCardFragment.setLatLng(lat, lng);

        TymoApplication.getInstance().setLatLng(lat, lng);
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation != null) {
                lat = lastLocation.getLatitude();
                lng = lastLocation.getLongitude();

                TymoApplication.getInstance().setLatLng(lat, lng);

                FeedCardFragment feedCardFragment = (FeedCardFragment) mNavigator.getFragment(1);

                if (feedCardFragment != null)
                    feedCardFragment.setLatLng(lat, lng);


                //XXXXXXXXXXXXX
                /*locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1000*60*5);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);*/
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null)
            googleApiClient.disconnect();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptions != null)
            mSubscriptions.dispose();

        Glide.get(getActivity()).clearMemory();
    }
}