package io.development.tymo.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import info.abdolahi.CircularMusicProgressBar;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.activities.AboutActivity;
import io.development.tymo.activities.ContactsActivity;
import io.development.tymo.activities.FriendRequestActivity;
import io.development.tymo.activities.InviteActivity;
import io.development.tymo.activities.NextCommitmentsActivity;
import io.development.tymo.activities.SettingsActivity;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.BgProfileServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private LinearLayout settingsBox, pendingRequestsBox, invitationsBox, timerBox;
    private RelativeLayout contactsBox;
    private ImageView profilePhoto, timerIcon;
    private ImageView editIcon;
    private CircularMusicProgressBar progressBar;
    private Rect rect;
    private TextView profileName, profileDescription, pendingRequestsQty, invitationsQty;
    private TextView commitmentStartTime, commitmentTitle, timer, todayDate, numberContacts;
    private View progressBox;
    private User user;

    private boolean commitments = false;
    private Thread t;

    private Calendar currentTime;
    private int threadSleepTime;
    private static int currentSecond, currentMinute, currentHour;
    private ArrayList<BgProfileServer> bgProfile = new ArrayList<>();

    private ImageView backgroundProfile;

    private DateFormat dateFormat;

    private boolean noInternet = true;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();

    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance(String text) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).updateProfileMainInformation();

        mSubscriptions = new CompositeSubscription();

        dateFormat = new DateFormat(getActivity());

        progressBar = (CircularMusicProgressBar) view.findViewById(R.id.clockAlarm);
        settingsBox = (LinearLayout) view.findViewById(R.id.settingsBox);
        contactsBox = (RelativeLayout) view.findViewById(R.id.myContactsBox);
        pendingRequestsBox = (LinearLayout) view.findViewById(R.id.pendingRequestsBox);
        invitationsBox = (LinearLayout) view.findViewById(R.id.invitationsBox);
        timerBox = (LinearLayout) view.findViewById(R.id.timerBox);
        profilePhoto = (ImageView) view.findViewById(R.id.profilePhoto);
        timerIcon = (ImageView) view.findViewById(R.id.timerIcon);
        editIcon = (ImageView) view.findViewById(R.id.editIcon);
        todayDate = (TextView) view.findViewById(R.id.todayDate);
        progressBox = view.findViewById(R.id.progressBox);

        backgroundProfile = (ImageView) view.findViewById(R.id.backgroundProfile);
        profileName = (TextView) view.findViewById(R.id.profileName);
        profileDescription = (TextView) view.findViewById(R.id.profileDescription);
        pendingRequestsQty = (TextView) view.findViewById(R.id.pendingRequestsQty);
        invitationsQty = (TextView) view.findViewById(R.id.invitationsQty);
        commitmentStartTime = (TextView) view.findViewById(R.id.commitmentStartTime);
        commitmentTitle = (TextView) view.findViewById(R.id.commitmentTitle);
        numberContacts = (TextView) view.findViewById(R.id.numberContacts);
        timer = (TextView) view.findViewById(R.id.timer);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));

        timerBox.setOnClickListener(this);
        contactsBox.setOnClickListener(this);
        settingsBox.setOnClickListener(this);
        pendingRequestsBox.setOnClickListener(this);
        invitationsBox.setOnClickListener(this);
        profilePhoto.setOnClickListener(this);

        getBgProfile();

        threadSleepTime = 1000;

        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(threadSleepTime);
                        if (getActivity() == null)
                            return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentTime = Calendar.getInstance();
                                currentSecond = currentTime.get(Calendar.SECOND);
                                currentMinute = currentTime.get(Calendar.MINUTE);
                                currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                                setBackgroundProfile();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.clockwise_rotation);

        progressBar.setValue(0);
        progressBar.startAnimation(rotation);
        progressBar.bringToFront();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        new Actor.Builder(SpringSystem.create(), editIcon)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                                    Intent intent = new Intent(getActivity(), AboutActivity.class);
                                    intent.putExtra("user_about", new UserWrapper(user));

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "user_about" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    startActivity(intent);
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

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

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

        getProfileMainInformation(query);
        setProgress(true);

    }

    private void setBackgroundProfile() {
        String timeStart, timeEnd;
        String currentTime = String.format("%02d", currentHour) + ":" + String.format("%02d", currentMinute);
        int period = 0;
        String urlBg = "";

        for (int i = 0; i < bgProfile.size(); i++) {
            timeStart = String.format("%02d", bgProfile.get(i).getHourStart()) + ":" + String.format("%02d", bgProfile.get(i).getMinuteStart());
            timeEnd = String.format("%02d", bgProfile.get(i).getHourEnd()) + ":" + String.format("%02d", bgProfile.get(i).getMinuteEnd());

            if (isTimeInBetween(currentTime, timeStart, timeEnd)) {

                threadSleepTime = (bgProfile.get(i).getHourEnd() * 60 * 60 * 1000 + bgProfile.get(i).getMinuteEnd() * 60 * 1000)
                        - (currentHour * 60 * 60 * 1000 + currentMinute * 60 * 1000 + currentSecond * 1000);

                period = bgProfile.get(i).getPeriod();
                urlBg = bgProfile.get(i).getUrlBg();

            }
        }

        if (bgProfile.size() > 0) {

            if (period == 3) {
                profileName.setTextColor(getResources().getColor(R.color.grey_900));
                profileDescription.setTextColor(getResources().getColor(R.color.grey_900));
            } else if (period == 2 || period == 4) {
                profileName.setTextColor(getResources().getColor(R.color.white));
                profileDescription.setTextColor(getResources().getColor(R.color.white));
            } else {
                profileName.setTextColor(getResources().getColor(R.color.white));
                profileDescription.setTextColor(getResources().getColor(R.color.white));
            }

            Glide.clear(backgroundProfile);
            Glide.with(getActivity())
                    .load(urlBg)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable drawable = new BitmapDrawable(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                backgroundProfile.setBackground(drawable);
                            } else
                                backgroundProfile.setBackgroundDrawable(drawable);
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

    private void getBgProfile() {
        mSubscriptions.add(NetworkUtil.getRetrofit().getBgProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseBgProfile, this::handleErrorBgProfile));
    }

    private void handleResponseBgProfile(ArrayList<BgProfileServer> bgProfile) {
        this.bgProfile = new ArrayList<>();
        this.bgProfile.addAll(bgProfile);
    }

    private void handleErrorBgProfile(Throwable error) {
        Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLayout();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refresh" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }, 500);

        // Load complete
    }

    public void setProgress(boolean progress) {
        if (progress)
            progressBox.setVisibility(View.VISIBLE);
        else
            progressBox.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        currentTime = Calendar.getInstance();
        currentSecond = currentTime.get(Calendar.SECOND);
        currentMinute = currentTime.get(Calendar.MINUTE);
        currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        setBackgroundProfile();
    }

    public void updateLayout() {
        currentTime = Calendar.getInstance();
        currentSecond = currentTime.get(Calendar.SECOND);
        currentMinute = currentTime.get(Calendar.MINUTE);
        currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        setBackgroundProfile();

        ((MainActivity) getActivity()).updateProfileMainInformation();

        settingsBox.setClickable(false);
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

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

        getProfileMainInformation(query);
    }

    private void getProfileMainInformation(Query query) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfileMain(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }


    private void handleResponse(Response response) {

        Calendar c = Calendar.getInstance();
        ArrayList<Object> list = new ArrayList<>();
        user = response.getUser();
        String time = "";
        int startsAtHour = 0;
        int startsAtMinute = 0;
        String title_is_happening = "";
        String title_will_happen = "";

        noInternet = false;

        if (response.getNumberContacts() > 0) {
            numberContacts.setText(getResources().getString(R.string.my_contacts_count, response.getNumberContacts()));
            numberContacts.setVisibility(View.VISIBLE);
        } else
            numberContacts.setVisibility(View.GONE);

        if (response.getNumberInvitationRequest() > 0) {
            invitationsQty.setText(String.valueOf(response.getNumberInvitationRequest()));
            invitationsQty.setVisibility(View.VISIBLE);
        } else
            invitationsQty.setVisibility(View.GONE);

        if (response.getNumberFriendRequest() > 0) {
            pendingRequestsQty.setText(String.valueOf(response.getNumberFriendRequest()));
            pendingRequestsQty.setVisibility(View.VISIBLE);
        } else
            pendingRequestsQty.setVisibility(View.GONE);

        profileName.setText(user.getName());

        if (!user.getDescription().matches("")) {
            profileDescription.setText(user.getDescription());
            profileDescription.setVisibility(View.VISIBLE);
        } else
            profileDescription.setVisibility(View.GONE);

        if (!user.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(getActivity())
                    .load(user.getPhoto())
                    .asBitmap()
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
        } else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

        if (response.getMyCommitAct() != null)
            list.addAll(response.getMyCommitAct());
        if (response.getMyCommitFlag() != null)
            list.addAll(response.getMyCommitFlag());
        if (response.getMyCommitReminder() != null)
            list.addAll(response.getMyCommitReminder());

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
                int status = 0; // -1 = already happened ; 0 = is happening ; 1 = will happen
                int status2 = 0; // -1 = already happened ; 0 = is happening ; 1 = will happen

                Calendar calendar = Calendar.getInstance();
                String hourNow = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
                String minuteNow = String.format("%02d", calendar.get(Calendar.MINUTE));

                // Activity
                if (c1 instanceof ActivityServer) {
                    activityServer = (ActivityServer) c1;
                    start_hour = activityServer.getHourStart();
                    start_minute = activityServer.getMinuteStart();
                    end_hour = activityServer.getHourEnd();
                    end_minute = activityServer.getMinuteEnd();

                    String hour = String.format("%02d", start_hour);
                    String minute = String.format("%02d", start_minute);
                    String hourEnd = String.format("%02d", end_hour);
                    String minuteEnd = String.format("%02d", end_minute);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                    calendarEnd.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour = 23;
                            end_minute = 59;
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour = 0;
                            start_minute = 0;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour = 0;
                            start_minute = 0;
                            end_hour = 23;
                            end_minute = 59;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status = 1;
                    } else {
                        status = -1;
                    }

                    activityServer.setStatus(status);
                }
                // Flag
                else if (c1 instanceof FlagServer) {
                    flagServer = (FlagServer) c1;
                    start_hour = flagServer.getHourStart();
                    start_minute = flagServer.getMinuteStart();
                    end_hour = flagServer.getHourEnd();
                    end_minute = flagServer.getMinuteEnd();

                    String hour = String.format("%02d", start_hour);
                    String minute = String.format("%02d", start_minute);
                    String hourEnd = String.format("%02d", end_hour);
                    String minuteEnd = String.format("%02d", end_minute);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                    calendarEnd.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour = 23;
                            end_minute = 59;
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour = 0;
                            start_minute = 0;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour = 0;
                            start_minute = 0;
                            end_hour = 23;
                            end_minute = 59;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status = 1;
                    } else {
                        status = -1;
                    }

                    flagServer.setStatus(status);
                }
                // Reminder
                else if (c1 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c1;
                    start_hour = reminderServer.getHourStart();
                    start_minute = reminderServer.getMinuteStart();

                    if (isTimeInBefore(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                        status = -1;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                        status = 1;
                    } else {
                        status = 0;
                    }

                    String hour = String.format("%02d", start_hour);
                    String minute = String.format("%02d", start_minute);

                    reminderServer.setStatus(status);
                }

                // Activity
                if (c2 instanceof ActivityServer) {
                    activityServer = (ActivityServer) c2;
                    start_hour2 = activityServer.getHourStart();
                    start_minute2 = activityServer.getMinuteStart();
                    end_hour2 = activityServer.getHourEnd();
                    end_minute2 = activityServer.getMinuteEnd();

                    String hour = String.format("%02d", start_hour2);
                    String minute = String.format("%02d", start_minute2);
                    String hourEnd = String.format("%02d", end_hour2);
                    String minuteEnd = String.format("%02d", end_minute2);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                    calendarEnd.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour2 = 23;
                            end_minute2 = 59;
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            end_hour2 = 23;
                            end_minute2 = 59;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status2 = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status2 = 1;
                    } else {
                        status2 = -1;
                    }

                    activityServer.setStatus(status2);
                }
                // Flag
                else if (c2 instanceof FlagServer) {
                    flagServer = (FlagServer) c2;
                    start_hour2 = flagServer.getHourStart();
                    start_minute2 = flagServer.getMinuteStart();
                    end_hour2 = flagServer.getHourEnd();
                    end_minute2 = flagServer.getMinuteEnd();

                    String hour = String.format("%02d", start_hour2);
                    String minute = String.format("%02d", start_minute2);
                    String hourEnd = String.format("%02d", end_hour2);
                    String minuteEnd = String.format("%02d", end_minute2);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                    calendarEnd.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour2 = 23;
                            end_minute2 = 59;
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            end_hour2 = 23;
                            end_minute2 = 59;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status2 = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status2 = 1;
                    } else {
                        status2 = -1;
                    }

                    flagServer.setStatus(status2);
                }
                // Reminder
                else if (c2 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c2;
                    start_hour2 = reminderServer.getHourStart();
                    start_minute2 = reminderServer.getMinuteStart();

                    if (isTimeInBefore(hourNow + ":" + minuteNow, start_hour2 + ":" + start_minute2)) {
                        status2 = -1;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, start_hour2 + ":" + start_minute2)) {
                        status2 = 1;
                    } else {
                        status2 = 0;
                    }

                    String hour = String.format("%02d", start_hour2);
                    String minute = String.format("%02d", start_minute2);

                    reminderServer.setStatus(status2);

                }

                if (status < status2)
                    return -1;
                else if (status > status2)
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

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        int day = c.get(Calendar.DAY_OF_MONTH);
        int year = c.get(Calendar.YEAR);

        String startsAtHourText = String.format("%02d", startsAtHour);
        String startsAtMinuteText = String.format("%02d", startsAtMinute);

        String hourStartText;
        String minuteStartText;

        int count_already_happened = 0;
        int count_is_happening = 0;
        int count_will_happen = 0;

        int count_will_happen_at_same_time = 1;

        Calendar calendar = Calendar.getInstance();
        String hourNow = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String minuteNow = String.format("%02d", calendar.get(Calendar.MINUTE));

        for (int i = 0; i < list.size(); i++) {

            commitments = true;

            int start_hour = 0, start_minute = 0;
            int end_hour = 0, end_minute = 0;
            int status = 0; // -1 = already happened ; 0 = is happening ; 1 = will happen

            // Activity
            if (list.get(i) instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) list.get(i);

                hourStartText = String.format("%02d", activityServer.getHourStart());
                minuteStartText = String.format("%02d", activityServer.getMinuteStart());

                start_hour = activityServer.getHourStart();
                start_minute = activityServer.getMinuteStart();
                end_hour = activityServer.getHourEnd();
                end_minute = activityServer.getMinuteEnd();

                String hour = String.format("%02d", start_hour);
                String minute = String.format("%02d", start_minute);
                String hourEnd = String.format("%02d", end_hour);
                String minuteEnd = String.format("%02d", end_minute);
                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarEnd = Calendar.getInstance();
                calendarStart.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                calendarEnd.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                    if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hourEnd = "23";
                        minuteEnd = "59";
                        activityServer.setHourEnd(23);
                        activityServer.setMinuteEnd(59);
                    } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hour = "00";
                        minute = "00";
                        activityServer.setHourStart(0);
                        activityServer.setMinuteStart(0);
                    } else {
                        hour = "00";
                        minute = "00";
                        hourEnd = "23";
                        minuteEnd = "59";
                        activityServer.setHourStart(0);
                        activityServer.setMinuteStart(0);
                        activityServer.setHourEnd(23);
                        activityServer.setMinuteEnd(59);
                    }
                }

                if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                    status = 0;
                } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                    status = 1;
                } else {
                    status = -1;
                }

                activityServer.setStatus(status);

                if (activityServer.getStatus() == 1) {
                    if (count_will_happen == 0) {
                        startsAtHour = activityServer.getHourStart();
                        startsAtMinute = activityServer.getMinuteStart();
                        startsAtHourText = String.format("%02d", startsAtHour);
                        startsAtMinuteText = String.format("%02d", startsAtMinute);
                        title_will_happen = activityServer.getTitle();
                    } else {
                        if (isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            startsAtHour = activityServer.getHourStart();
                            startsAtMinute = activityServer.getMinuteStart();
                            startsAtHourText = String.format("%02d", startsAtHour);
                            startsAtMinuteText = String.format("%02d", startsAtMinute);
                            title_will_happen = activityServer.getTitle();
                        } else if (!isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText) && !isTimeInAfter(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            count_will_happen_at_same_time++;
                        }
                    }
                    count_will_happen++;
                } else if (activityServer.getStatus() == 0) {
                    count_is_happening++;
                    title_is_happening = activityServer.getTitle();
                } else {
                    count_already_happened++;
                }

            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);

                hourStartText = String.format("%02d", flagServer.getHourStart());
                minuteStartText = String.format("%02d", flagServer.getMinuteStart());

                start_hour = flagServer.getHourStart();
                start_minute = flagServer.getMinuteStart();
                end_hour = flagServer.getHourEnd();
                end_minute = flagServer.getMinuteEnd();

                String hour = String.format("%02d", start_hour);
                String minute = String.format("%02d", start_minute);
                String hourEnd = String.format("%02d", end_hour);
                String minuteEnd = String.format("%02d", end_minute);
                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarEnd = Calendar.getInstance();
                calendarStart.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                calendarEnd.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                    if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hourEnd = "23";
                        minuteEnd = "59";
                        flagServer.setHourEnd(23);
                        flagServer.setMinuteEnd(59);
                    } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hour = "00";
                        minute = "00";
                        flagServer.setHourStart(0);
                        flagServer.setMinuteStart(0);
                    } else {
                        hour = "00";
                        minute = "00";
                        hourEnd = "23";
                        minuteEnd = "59";
                        flagServer.setHourStart(0);
                        flagServer.setMinuteStart(0);
                        flagServer.setHourEnd(23);
                        flagServer.setMinuteEnd(59);
                    }
                }

                if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                    status = 0;
                } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                    status = 1;
                } else {
                    status = -1;
                }

                flagServer.setStatus(status);

                if (flagServer.getStatus() == 1) {
                    if (count_will_happen == 0) {
                        startsAtHour = flagServer.getHourStart();
                        startsAtMinute = flagServer.getMinuteStart();
                        startsAtHourText = String.format("%02d", startsAtHour);
                        startsAtMinuteText = String.format("%02d", startsAtMinute);
                        title_will_happen = flagServer.getTitle();
                    } else {
                        if (isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            startsAtHour = flagServer.getHourStart();
                            startsAtMinute = flagServer.getMinuteStart();
                            startsAtHourText = String.format("%02d", startsAtHour);
                            startsAtMinuteText = String.format("%02d", startsAtMinute);
                            title_will_happen = flagServer.getTitle();
                        } else if (!isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText) && !isTimeInAfter(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            count_will_happen_at_same_time++;
                        }
                    }
                    count_will_happen++;
                } else if (flagServer.getStatus() == 0) {
                    count_is_happening++;
                    title_is_happening = flagServer.getTitle();
                } else {
                    count_already_happened++;
                }

            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);

                hourStartText = String.format("%02d", reminderServer.getHourStart());
                minuteStartText = String.format("%02d", reminderServer.getMinuteStart());

                start_hour = reminderServer.getHourStart();
                start_minute = reminderServer.getMinuteStart();

                if (isTimeInBefore(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                    status = -1;
                } else if (isTimeInAfter(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                    status = 1;
                } else {
                    status = 0;
                }

                String hour = String.format("%02d", start_hour);
                String minute = String.format("%02d", start_minute);

                reminderServer.setStatus(status);

                if (reminderServer.getStatus() == 1) {
                    if (count_will_happen == 0) {
                        startsAtHour = reminderServer.getHourStart();
                        startsAtMinute = reminderServer.getMinuteStart();
                        startsAtHourText = String.format("%02d", startsAtHour);
                        startsAtMinuteText = String.format("%02d", startsAtMinute);
                        title_will_happen = reminderServer.getTitle();
                    } else {
                        if (isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            startsAtHour = reminderServer.getHourStart();
                            startsAtMinute = reminderServer.getMinuteStart();
                            startsAtHourText = String.format("%02d", startsAtHour);
                            startsAtMinuteText = String.format("%02d", startsAtMinute);
                            title_will_happen = reminderServer.getTitle();
                        } else if (!isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText) && !isTimeInAfter(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            count_will_happen_at_same_time++;
                        }
                    }
                    count_will_happen++;
                } else if (reminderServer.getStatus() == 0) {
                    count_is_happening++;
                    title_is_happening = reminderServer.getTitle();
                } else {
                    count_already_happened++;
                }
            }
        }

        int hourMinToStart;
        float percent;
        String hourMinText = "h";

        if (startsAtHour - nowHour > 1) {
            hourMinToStart = startsAtHour - nowHour;
            percent = 100.0f - ((float) hourMinToStart / 24.0f) * 100.0f;
        } else {
            if (startsAtHour == nowHour)
                hourMinToStart = startsAtMinute - nowMinute;
            else
                hourMinToStart = startsAtMinute + (60 - nowMinute);

            hourMinText = "m";
            percent = 100.0f - ((float) hourMinToStart / 60.0f) * 100.0f;
        }

        String dayOfWeek = dateFormat.formatDayOfWeek(c.get(Calendar.DAY_OF_WEEK));
        String dayToday = String.format("%02d", day);
        String monthToday = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(c.getTime().getTime());

        todayDate.setText(getResources().getString(R.string.today).toUpperCase() + " - " + getResources().getString(R.string.date_format_3, dayOfWeek, dayToday, monthToday, year));

        if (commitments) {
            if (count_is_happening > 0) {
                if (count_is_happening == 1) {
                    commitmentTitle.setText(title_is_happening);
                } else {
                    commitmentTitle.setText(getResources().getString(R.string.n_commitments, count_is_happening));
                }
                commitmentStartTime.setVisibility(View.VISIBLE);
                commitmentStartTime.setText(R.string.happening_now);
                commitmentTitle.setLines(1);
                timer.setVisibility(View.GONE);
                timerIcon.setVisibility(View.VISIBLE);
                timerIcon.setImageResource(R.drawable.ic_add_cube);
                timerIcon.setColorFilter(getResources().getColor(R.color.white));
                progressBar.setValue(0);
            } else if (count_will_happen > 0) {
                if (count_will_happen_at_same_time > 1) {
                    commitmentTitle.setText(getResources().getString(R.string.n_commitments, count_will_happen_at_same_time));
                    commitmentStartTime.setText(getResources().getString(R.string.starts_at_plural, startsAtHourText, startsAtMinuteText));
                } else {
                    commitmentTitle.setText(title_will_happen);
                    commitmentStartTime.setText(getResources().getString(R.string.starts_at, startsAtHourText, startsAtMinuteText));
                }
                commitmentTitle.setLines(1);
                commitmentStartTime.setVisibility(View.VISIBLE);
                timer.setVisibility(View.VISIBLE);
                timer.setText(String.valueOf(hourMinToStart) + hourMinText);
                timerIcon.setVisibility(View.GONE);
                progressBar.setValue(percent);

            } else {
                commitmentStartTime.setVisibility(View.GONE);
                commitmentTitle.setText(R.string.all_commitments_already_finished);
                commitmentTitle.setLines(2);
                timer.setVisibility(View.GONE);
                timerIcon.setVisibility(View.VISIBLE);
                timerIcon.setImageResource(R.drawable.ic_alarm);
                timerIcon.setColorFilter(getResources().getColor(R.color.white));
                progressBar.setValue(0);
            }
        } else {
            commitmentStartTime.setVisibility(View.GONE);
            commitmentTitle.setLines(2);
            commitmentTitle.setText(getActivity().getResources().getString(R.string.empty_commitments));
            timer.setVisibility(View.GONE);
            timerIcon.setVisibility(View.VISIBLE);
            timerIcon.setImageResource(R.drawable.ic_alarm);
            timerIcon.setColorFilter(getResources().getColor(R.color.white));
            progressBar.setValue(0);
        }
        settingsBox.setClickable(true);
        setProgress(false);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private boolean isTimeInBefore(String now, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(time);

            return date1.after(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isTimeInAfter(String now, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(time);

            return date1.before(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        noInternet = true;
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v == contactsBox && !noInternet) {
            Intent intent = new Intent(getActivity(), ContactsActivity.class);
            intent.putExtra("email_contacts", user.getEmail());

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "contactsBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(intent);
        } else if (v == timerBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timerBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(getActivity(), NextCommitmentsActivity.class));
        } else if (v == settingsBox && settingsBox.isClickable() && !noInternet) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.putExtra("user_about", new UserWrapper(user));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "settingsBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(intent);
        } else if (v == profilePhoto && !noInternet) {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            intent.putExtra("user_about", new UserWrapper(user));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "profilePhoto" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(intent);
        } else if (v == pendingRequestsBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "pendingRequestsBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(getActivity(), FriendRequestActivity.class));
        } else if (v == invitationsBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "invitationsBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(getActivity(), InviteActivity.class));
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptions != null)
            mSubscriptions.unsubscribe();

        Glide.get(getActivity()).clearMemory();
        t.interrupt();
    }
}