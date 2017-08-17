package io.development.tymo.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.Birthday;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.Holiday;
import io.development.tymo.model_server.Plans;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.models.WeekModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.GoogleCalendarEvents;
import io.development.tymo.utils.Utilities;
import io.development.tymo.activities.CompareActivity;
import io.development.tymo.adapters.PlansFragmentAdapter;
import io.development.tymo.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

public class PlansFragment extends Fragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener, View.OnTouchListener, CreatePopUpDialogFragment.RefreshLayoutPlansCallback {

    private FragmentNavigator mNavigator;
    private LinearLayout mDateBox;
    private NestedScrollView scrollView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();
    private DateFormat dateFormat;

    private TextView commitmentsButton, btnCompare;
    private TextView freeTimeButton;
    private TextView dateTextWeek, dateTextMonth;

    private LinearLayout compareButton;
    private ImageView previousWeek, nextWeek, calendarIcon;

    private String email;
    private int linePaint = -1;

    private int day_start, month_start, year_start;
    private List<WeekModel> listPlans = new ArrayList<>();
    private ArrayList<ArrayList<String>> list_holiday = new ArrayList<>();

    private GestureDetector gestureDetector;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private BroadcastReceiver mMessageReceiverRefresh = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    };

    public static Fragment newInstance(String text) {
        PlansFragment fragment = new PlansFragment();
        return fragment;
    }

    public PlansFragment() {
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

        return inflater.inflate(R.layout.activity_plans, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateFormat = new DateFormat(getActivity());

        if (Build.VERSION.SDK_INT >= 17)
            mNavigator = new FragmentNavigator(getChildFragmentManager(), new PlansFragmentAdapter(), R.id.container);
        else
            mNavigator = new FragmentNavigator(getFragmentManager(), new PlansFragmentAdapter(), R.id.container);

        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.onCreate(savedInstanceState);

        mSubscriptions = new CompositeDisposable();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        gestureDetector = new GestureDetector(getActivity(), new SingleTapConfirm());

        commitmentsButton = (TextView) view.findViewById(R.id.commitmentsButton);
        freeTimeButton = (TextView) view.findViewById(R.id.freeTimeButton);
        compareButton = (LinearLayout) view.findViewById(R.id.compareButton);
        btnCompare = (TextView) view.findViewById(R.id.btnCompare);
        dateTextMonth = (TextView) view.findViewById(R.id.dateMonthYear);
        calendarIcon = (ImageView) view.findViewById(R.id.calendarIcon);
        dateTextWeek = (TextView) view.findViewById(R.id.dateWeek);
        mDateBox = (LinearLayout) view.findViewById(R.id.dateBox);
        scrollView = (NestedScrollView) view.findViewById(R.id.scrollView);
        previousWeek = (ImageView) view.findViewById(R.id.previousWeek);
        nextWeek = (ImageView) view.findViewById(R.id.nextWeek);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setDistanceToTriggerSync(850);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(),R.color.deep_purple_400));

        mDateBox.setOnClickListener(this);
        dateTextWeek.setOnClickListener(this);
        commitmentsButton.setOnClickListener(this);
        freeTimeButton.setOnClickListener(this);
        compareButton.setOnClickListener(this);
        previousWeek.setOnClickListener(this);
        nextWeek.setOnClickListener(this);
        compareButton.setOnTouchListener(this);
        mDateBox.setOnTouchListener(this);
        previousWeek.setOnTouchListener(this);
        nextWeek.setOnTouchListener(this);
        dateTextWeek.setOnTouchListener(this);

        setCurrentTab(mNavigator.getCurrentPosition());

        commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left_pressed);
        freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right);
        commitmentsButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        freeTimeButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            cal.add(Calendar.DATE, -1);

        cal.add(Calendar.DAY_OF_WEEK, -15);
        int month = cal.get(Calendar.MONTH) + 1;

        cal.add(Calendar.DAY_OF_WEEK, 21);
        int month2 = cal.get(Calendar.MONTH) + 1;

        if (month != month2) {
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);
            cal2.set(Calendar.DAY_OF_WEEK, cal2.getFirstDayOfWeek());
            cal2.add(Calendar.DAY_OF_WEEK, 1);
            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while (day3 != 1) {
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        int day_start_temp = cal.get(Calendar.DAY_OF_MONTH);
        int month_start_temp = cal.get(Calendar.MONTH) + 1;

        cal.add(Calendar.DAY_OF_WEEK, -6);
        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH) + 1;
        year_start = cal.get(Calendar.YEAR);

        String month_text_start = dateFormat.formatMonthShort(month_start);
        String month_text_start_temp = dateFormat.formatMonthShort(month_start_temp);
        dateTextWeek.setText(getResources().getString(R.string.date_format_2, String.format("%02d", day_start), month_text_start, String.format("%02d", day_start_temp),month_text_start_temp));

        String month_text = dateFormat.formatMonth(cal.get(Calendar.MONTH)+1);
        dateTextMonth.setText(getResources().getString(R.string.date_format_1, month_text, year_start));

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.READ_CALENDAR },
                    1);
        }

        ArrayList<Integer> list = TymoApplication.getInstance().getDate();
        if(list == null && !TymoApplication.getInstance().isCreatedActivity())
            refreshLayout(true);
        else if(list != null && list.size() >= 3){
            updateLayout(list.get(0), list.get(1), list.get(2), true);
            TymoApplication.getInstance().setDate(null);
            TymoApplication.getInstance().setCreatedActivity(false);
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiverRefresh, new IntentFilter("refresh_screen_delete"));
    }



    void refreshItems() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                refreshLayout(true);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refresh" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }, 500);

    }

    public void updateLayout(int dayOfMonth, int monthOfYear, int year, boolean showRefresh) {

        listPlans.clear();
        list_holiday.clear();
        linePaint = -1;

        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }

        cal.add(Calendar.DAY_OF_WEEK, -15);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);

        cal.set(y, m-1,d, 0,0);
        long time1 = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_WEEK, 21);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int month2 = cal.get(Calendar.MONTH) + 1;
        int year2 = cal.get(Calendar.YEAR);

        cal.set(year2, month2-1,day2, 23,59);
        long time2 = cal.getTimeInMillis();

        int d1f = d;

        if (m != month2) {
            Calendar cal2 = Calendar.getInstance();
            cal2.set(y, m - 1, d);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);

            while (cal2.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal2.add(Calendar.DATE, -1);
            }
            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while (day3 != 1) {
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();
        plans.setEmail(email);
        plans.setA(y);
        plans.setA2(year2);
        plans.setD1(d);
        plans.setD2(day2);
        plans.setM(m);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        plans.addEmails("");

        int day_start_temp = cal.get(Calendar.DAY_OF_MONTH);
        int month_start_temp = cal.get(Calendar.MONTH) + 1;

        cal.add(Calendar.DAY_OF_WEEK, -6);
        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH) + 1;
        year_start = cal.get(Calendar.YEAR);

        String month_text_start = dateFormat.formatMonthShort(month_start);
        String month_text_start_temp = dateFormat.formatMonthShort(month_start_temp);
        dateTextWeek.setText(getResources().getString(R.string.date_format_2, String.format("%02d", day_start), month_text_start, String.format("%02d", day_start_temp),month_text_start_temp));

        String month_text = dateFormat.formatMonth(cal.get(Calendar.MONTH)+1);
        dateTextMonth.setText(getResources().getString(R.string.date_format_1, month_text, year_start));

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            list_holiday = GoogleCalendarEvents.readCalendarHolidays(getActivity(),time1,time2);
        }

        setPlans(plans, showRefresh);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.permission_location), Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    private void setPlans(Plans plans, boolean showRefresh) {

        CommitmentFragment commitmentFragment = (CommitmentFragment) mNavigator.getFragment(0);
        if (commitmentFragment != null && showRefresh)
            commitmentFragment.showProgressCommitment();

        FreeFragment freeFragment = (FreeFragment) mNavigator.getFragment(1);
        if (freeFragment != null && showRefresh)
            freeFragment.showProgressFree();

        plans.setIdDevice(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));

        mSubscriptions.add(NetworkUtil.getRetrofit().getPlans(plans)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {

        listPlans.clear();

        Calendar cal = Calendar.getInstance();
        cal.set(year_start, month_start - 1, day_start);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }

        int day, month, year;

        for (int i = 0; i < 7; i++) {
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH) + 1;
            year = cal.get(Calendar.YEAR);

            String day_text = dateFormat.formatDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
            String month_text = dateFormat.formatMonthShort(cal.get(Calendar.MONTH)+1);

            WeekModel weekModel = new WeekModel(String.format("%02d", day), day_text, month_text, day, month, year, false);

            int j;
            for (j = 0; j < response.getMyCommitAct().size(); j++) {
                ActivityServer activity = response.getMyCommitAct().get(j);
                ActivityServer activityServer = new ActivityServer(activity);
                if (Utilities.isActivityInRange(activityServer.getYearStart(), activityServer.getYearEnd(),activityServer.getDayStart(), activityServer.getMonthStart(), activityServer.getDayEnd(), activityServer.getMonthEnd(), day)) {
                    boolean start = Utilities.isStartedFinishedToday(day, activityServer.getDayStart());
                    boolean finish = Utilities.isStartedFinishedToday(day, activityServer.getDayEnd());
                    if (!start && finish) {
                        activityServer.setMinuteCard(0);
                        activityServer.setHourCard(0);
                        activityServer.setMinuteEndCard(activityServer.getMinuteEnd());
                        activityServer.setHourEndCard(activityServer.getHourEnd());
                    } else if (start && !finish) {
                        activityServer.setMinuteCard(activityServer.getMinuteStart());
                        activityServer.setHourCard(activityServer.getHourStart());
                        activityServer.setMinuteEndCard(59);
                        activityServer.setHourEndCard(23);
                    } else if (!start && !finish) {
                        activityServer.setMinuteCard(0);
                        activityServer.setHourCard(0);
                        activityServer.setMinuteEndCard(59);
                        activityServer.setHourEndCard(23);
                    } else {
                        activityServer.setMinuteCard(activityServer.getMinuteStart());
                        activityServer.setHourCard(activityServer.getHourStart());
                        activityServer.setMinuteEndCard(activityServer.getMinuteEnd());
                        activityServer.setHourEndCard(activityServer.getHourEnd());
                    }

                    weekModel.addPlans(activityServer);
                }
            }
            for (j = 0; j < response.getMyCommitFlag().size(); j++) {
                FlagServer flag = response.getMyCommitFlag().get(j);
                FlagServer flagServer = new FlagServer(flag);
                if (Utilities.isActivityInRange(flagServer.getYearStart(), flagServer.getYearEnd(),flagServer.getDayStart(), flagServer.getMonthStart(), flagServer.getDayEnd(), flagServer.getMonthEnd(), day)) {
                    boolean start = Utilities.isStartedFinishedToday(day, flagServer.getDayStart());
                    boolean finish = Utilities.isStartedFinishedToday(day, flagServer.getDayEnd());
                    if (!start && finish) {
                        flagServer.setMinuteCard(0);
                        flagServer.setHourCard(0);
                        flagServer.setMinuteEndCard(flagServer.getMinuteEnd());
                        flagServer.setHourEndCard(flagServer.getHourEnd());
                    } else if (start && !finish) {
                        flagServer.setMinuteCard(flagServer.getMinuteStart());
                        flagServer.setHourCard(flagServer.getHourStart());
                        flagServer.setMinuteEndCard(59);
                        flagServer.setHourEndCard(23);
                    } else if (!start && !finish) {
                        flagServer.setMinuteCard(0);
                        flagServer.setHourCard(0);
                        flagServer.setMinuteEndCard(59);
                        flagServer.setHourEndCard(23);
                    } else {
                        flagServer.setMinuteCard(flagServer.getMinuteStart());
                        flagServer.setHourCard(flagServer.getHourStart());
                        flagServer.setMinuteEndCard(flagServer.getMinuteEnd());
                        flagServer.setHourEndCard(flagServer.getHourEnd());
                    }
                    weekModel.addPlans(flagServer);
                }
            }
            for (j = 0; j < response.getMyCommitReminder().size(); j++) {
                ReminderServer reminderServer = response.getMyCommitReminder().get(j);
                if (reminderServer.getDayStart() == day && reminderServer.getMonthStart() == month && reminderServer.getYearStart() == year) {
                    weekModel.addPlans(reminderServer);
                }
            }

            Birthday birthday = new Birthday();
            for (j = 0; j < response.getPeople().size(); j++) {
                User user = response.getPeople().get(j);
                if (user.getDayBorn() == day && user.getMonthBorn() == month) {
                    birthday.addBirthday(user);
                    birthday.setDay(day);
                    birthday.setMonth(month);
                    birthday.setYear(year); //user.getYearBorn()
                }
            }

            if(birthday.getUsersBirthday().size() > 0)
                weekModel.addPlans(birthday);

            if(list_holiday.size() > 0) {
                ArrayList<String> name_holydays = list_holiday.get(0);
                ArrayList<String> date_holydays = list_holiday.get(1);
                Holiday holiday = new Holiday();

                for (j = 0; j < name_holydays.size(); j++) {

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(Long.parseLong(date_holydays.get(j)));
                    int year_holyday = c.get(Calendar.YEAR);
                    int month_holyday = c.get(Calendar.MONTH) + 1;
                    int day_holyday = c.get(Calendar.DAY_OF_MONTH);


                    if (day_holyday == day && month_holyday == month && year_holyday == year) {
                        holiday.addHoliday(name_holydays.get(j));
                        holiday.setDay(day_holyday);
                        holiday.setMonth(month_holyday);
                        holiday.setYear(year_holyday);
                    }
                }

                if (holiday.getHolidays().size() > 0)
                    weekModel.addPlans(holiday);
            }

            Collections.sort(weekModel.getActivities(), new Comparator<Object>() {
                @Override
                public int compare(Object c1, Object c2) {
                    ActivityServer activityServer;
                    FlagServer flagServer;
                    ReminderServer reminderServer;
                    int start_hour = 0, start_minute = 0;
                    int start_hour2 = 0, start_minute2 = 0;
                    int end_hour = 0, end_minute = 0;
                    int end_hour2 = 0, end_minute2 = 0;

                    if (c1 instanceof ActivityServer) {
                        activityServer = (ActivityServer) c1;
                        start_hour = activityServer.getHourCard();
                        start_minute = activityServer.getMinuteCard();
                        end_hour = activityServer.getHourEndCard();
                        end_minute = activityServer.getMinuteEndCard();
                    } else if (c1 instanceof FlagServer) {
                        flagServer = (FlagServer) c1;
                        start_hour = flagServer.getHourCard();
                        start_minute = flagServer.getMinuteCard();
                        end_hour = flagServer.getHourEndCard();
                        end_minute = flagServer.getMinuteEndCard();
                    } else if (c1 instanceof ReminderServer) {
                        reminderServer = (ReminderServer) c1;
                        start_hour = reminderServer.getHourStart();
                        start_minute = reminderServer.getMinuteStart();
                        end_hour = reminderServer.getHourStart();
                        end_minute = reminderServer.getMinuteStart();
                    }else if(c1 instanceof Holiday){
                        start_hour = 0;
                        start_minute = 0;
                        end_hour = 0;
                        end_minute = 0;
                    }else if(c1 instanceof Birthday){
                        start_hour = 0;
                        start_minute = 0;
                        end_hour = 0;
                        end_minute = 0;
                    }

                    if (c2 instanceof ActivityServer) {
                        activityServer = (ActivityServer) c2;
                        start_hour2 = activityServer.getHourCard();
                        start_minute2 = activityServer.getMinuteCard();
                        end_hour2 = activityServer.getHourEndCard();
                        end_minute2 = activityServer.getMinuteEndCard();
                    } else if (c2 instanceof FlagServer) {
                        flagServer = (FlagServer) c2;
                        start_hour2 = flagServer.getHourCard();
                        start_minute2 = flagServer.getMinuteCard();
                        end_hour2 = flagServer.getHourEndCard();
                        end_minute2 = flagServer.getMinuteEndCard();
                    } else if (c2 instanceof ReminderServer) {
                        reminderServer = (ReminderServer) c2;
                        start_hour2 = reminderServer.getHourStart();
                        start_minute2 = reminderServer.getMinuteStart();
                        end_hour2 = reminderServer.getHourStart();
                        end_minute2 = reminderServer.getMinuteStart();
                    }else if(c2 instanceof Holiday){
                        start_hour = 0;
                        start_minute = 0;
                        end_hour = 0;
                        end_minute = 0;
                    }else if(c2 instanceof Birthday){
                        start_hour = 0;
                        start_minute = 0;
                        end_hour = 0;
                        end_minute = 0;
                    }

                    if (start_hour < start_hour2)
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

            ArrayList<Object> arrayList = new ArrayList<>();

            if (weekModel.getActivities().size() == 0) {
                FreeTimeServer freeTimeServer = new FreeTimeServer();
                freeTimeServer.setHourStart(0);
                freeTimeServer.setMinuteStart(0);
                freeTimeServer.setHourEnd(23);
                freeTimeServer.setMinuteEnd(59);

                weekModel.addFree(freeTimeServer);
            } else {
                int start_hour = 0, start_minute = 0;
                int end_hour = 0, end_minute = 0;
                int free_hour = 0, free_minute = 0;

                for (j = 0; j < weekModel.getActivities().size(); j++) {
                    Object object = weekModel.getActivities().get(j);
                    ActivityServer activityServer;
                    FlagServer flagServer;
                    ReminderServer reminderServer;

                    if (object instanceof ActivityServer) {
                        activityServer = (ActivityServer) object;
                        start_hour = activityServer.getHourCard();
                        start_minute = activityServer.getMinuteCard();
                        end_hour = activityServer.getHourEndCard();
                        end_minute = activityServer.getMinuteEndCard();
                    } else if (object instanceof FlagServer) {
                        flagServer = (FlagServer) object;
                        start_hour = flagServer.getHourCard();
                        start_minute = flagServer.getMinuteCard();
                        end_hour = flagServer.getHourEndCard();
                        end_minute = flagServer.getMinuteEndCard();
                    } else if (object instanceof ReminderServer) {
                        reminderServer = (ReminderServer) object;
                        start_hour = reminderServer.getHourStart();
                        start_minute = reminderServer.getMinuteStart();
                        end_hour = start_hour;
                        end_minute = start_minute;
                    }else if (object instanceof Holiday) {
                        start_hour = 0;
                        start_minute = 0;
                        end_hour = 0;
                        end_minute = 0;
                    }else if (object instanceof Birthday) {
                        start_hour = 0;
                        start_minute = 0;
                        end_hour = 0;
                        end_minute = 0;
                    }

                    FreeTimeServer freeTimeServer = new FreeTimeServer();

                    if ((free_hour < start_hour) || (free_hour == start_hour && free_minute < start_minute)) {
                        freeTimeServer.setHourStart(free_hour);
                        freeTimeServer.setMinuteStart(free_minute);
                        freeTimeServer.setHourEnd(start_hour);
                        freeTimeServer.setMinuteEnd(start_minute);

                        free_hour = end_hour;
                        free_minute = end_minute;

                        arrayList.add(freeTimeServer);

                    } else if ((free_hour == start_hour && free_minute == start_minute) || (free_hour < end_hour) || (free_hour == end_hour && free_minute < end_minute)) {
                        free_hour = end_hour;
                        free_minute = end_minute;
                    }

                    if ((j == weekModel.getActivities().size() - 1) && (end_hour < 23 || (end_hour == 23 && end_minute < 59))) {
                        if (!(free_hour == 23 && free_minute == 59)) {
                            FreeTimeServer freeTimeServer2 = new FreeTimeServer();
                            freeTimeServer2.setHourStart(free_hour);
                            freeTimeServer2.setMinuteStart(free_minute);
                            freeTimeServer2.setHourEnd(23);
                            freeTimeServer2.setMinuteEnd(59);

                            arrayList.add(freeTimeServer2);
                        }
                    }

                }
            }

            for (j = 0; j < arrayList.size(); j++) {
                FreeTimeServer freeTimeServer = (FreeTimeServer)arrayList.get(j);
                if(isFreeTimeOneMinute(freeTimeServer.getHourStart(),freeTimeServer.getMinuteStart(),freeTimeServer.getHourEnd(),freeTimeServer.getMinuteEnd())){
                    arrayList.remove(j);
                    j--;
                }
            }

            for (j = 0; j < arrayList.size(); j++) {
                FreeTimeServer freeTimeServer = (FreeTimeServer)arrayList.get(j);
                if((j+1) < arrayList.size()){
                    FreeTimeServer freeTimeServer2 = (FreeTimeServer)arrayList.get(j+1);
                    if(isStartFinishSame(freeTimeServer.getHourEnd(),freeTimeServer.getMinuteEnd(), freeTimeServer2.getHourStart(),freeTimeServer2.getMinuteStart()))
                    {
                        freeTimeServer.setHourEnd(freeTimeServer2.getHourEnd());
                        freeTimeServer.setMinuteEnd(freeTimeServer2.getMinuteEnd());
                        arrayList.remove(j+1);
                        j--;
                    }
                }

            }

            weekModel.addAllFree(arrayList);

            listPlans.add(weekModel);

            cal.add(Calendar.DATE, 1);
        }

        CommitmentFragment commitmentFragment = (CommitmentFragment) mNavigator.getFragment(0);
        if (commitmentFragment != null)
            commitmentFragment.setDataAdapter(listPlans);

        FreeFragment freeFragment = (FreeFragment) mNavigator.getFragment(1);
        if (freeFragment != null)
            freeFragment.setDataAdapter(listPlans);

        cleanPaintRow();
        setPaintRow();
        mSwipeRefreshLayout.setRefreshing(false);

    }

    public boolean isFreeTimeOneMinute(int start_hour, int start_minute, int end_hour, int end_minute){
        return start_hour == end_hour && ((end_minute - start_minute) == 1);
    }

    public boolean isStartFinishSame(int start_hour, int start_minute, int end_hour, int end_minute){
        return start_hour == end_hour && end_minute == start_minute;
    }

    private void setPaintRow() {
        if (linePaint != -1) {
            for (int i = 0; i < listPlans.size(); i++) {
                if (listPlans.get(i).getDay() == linePaint)
                    listPlans.get(i).setPaint(true);
            }
        }
    }

    private void cleanPaintRow() {
        for (int i = 0; i < listPlans.size(); i++) {
            listPlans.get(i).setPaint(false);
        }
    }

    private void handleError(Throwable error) {
        mSwipeRefreshLayout.setRefreshing(false);
        if(Utilities.isDeviceOnline(getActivity()))
            Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    public List<WeekModel> getListPlans() {
        return listPlans;
    }

    public NestedScrollView getScrollView() {
        return scrollView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mNavigator!=null)
            mNavigator.onSaveInstanceState(outState);
    }

    private void setCurrentTab(int position) {
        if (position != 2) {
            mNavigator.showFragment(position);
        } else
            startActivity(new Intent(getActivity(), CompareActivity.class));

    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");

        if (dpd != null) dpd.setOnDateSetListener(this);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        linePaint = dayOfMonth;
        listPlans.clear();
        list_holiday.clear();

        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }

        cal.add(Calendar.DAY_OF_WEEK, -15);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);

        cal.set(y, m-1,d, 0,0);
        long time1 = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_WEEK, 21);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int month2 = cal.get(Calendar.MONTH) + 1;
        int year2 = cal.get(Calendar.YEAR);

        cal.set(year2, month2-1,day2, 23,59);
        long time2 = cal.getTimeInMillis();

        int d1f = d;

        if (m != month2) {
            Calendar cal2 = Calendar.getInstance();
            cal2.set(y, m - 1, d);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);

            while (cal2.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal2.add(Calendar.DATE, -1);
            }
            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while (day3 != 1) {
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();
        plans.setEmail(email);
        plans.setA(y);
        plans.setA2(year2);
        plans.setD1(d);
        plans.setD2(day2);
        plans.setM(m);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        plans.addEmails("");

        int day_start_temp = cal.get(Calendar.DAY_OF_MONTH);
        int month_start_temp = cal.get(Calendar.MONTH) + 1;

        cal.add(Calendar.DAY_OF_WEEK, -6);
        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH) + 1;
        year_start = cal.get(Calendar.YEAR);

        String month_text_start = dateFormat.formatMonthShort(month_start);
        String month_text_start_temp = dateFormat.formatMonthShort(month_start_temp);
        dateTextWeek.setText(getResources().getString(R.string.date_format_2, String.format("%02d", day_start), month_text_start, String.format("%02d", day_start_temp),month_text_start_temp));

        String month_text = dateFormat.formatMonth(cal.get(Calendar.MONTH)+1);
        dateTextMonth.setText(getResources().getString(R.string.date_format_1, month_text, year_start));

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            list_holiday = GoogleCalendarEvents.readCalendarHolidays(getActivity(),time1,time2);
        }

        setPlans(plans, true);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == compareButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                btnCompare.setBackgroundResource(R.drawable.btn_compare);
                btnCompare.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                btnCompare.setBackgroundResource(R.drawable.btn_compare_pressed);
                btnCompare.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            }

            if (gestureDetector.onTouchEvent(event)) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "compareButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                startActivity(new Intent(getActivity(), CompareActivity.class));
                btnCompare.setBackgroundResource(R.drawable.btn_compare);
                btnCompare.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            }
        }
        else if (view == mDateBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                dateTextMonth.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_900));
                calendarIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_900));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dateTextMonth.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_600));
                calendarIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_600));
            }
        }
        else if (view == nextWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                nextWeek.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                nextWeek.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_200));
            }
        }
        else if (view == previousWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                previousWeek.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                previousWeek.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_200));
            }
        }
        else if (view == dateTextWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                dateTextWeek.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_500));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dateTextWeek.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_300));
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == mDateBox || view == dateTextWeek) {
            Calendar now = Calendar.getInstance();

            if (linePaint != -1)
                now.set(year_start, month_start - 1, linePaint);
            else
                now.set(year_start, month_start - 1, day_start);

            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    PlansFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mDateBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog");
        } else if (view == freeTimeButton) {
            commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left);
            freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right_pressed);
            commitmentsButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            freeTimeButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "freeTimeButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            setCurrentTab(1);
        } else if (view == commitmentsButton) {
            commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left_pressed);
            freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right);
            commitmentsButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            freeTimeButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "commitmentsButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            setCurrentTab(0);
        } else if (view == previousWeek) {
            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.DAY_OF_WEEK, -7);
            day_start = cal.get(Calendar.DAY_OF_MONTH);
            month_start = cal.get(Calendar.MONTH) + 1;
            year_start = cal.get(Calendar.YEAR);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "previousWeek" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            updateLayout(day_start, month_start - 1, year_start, true);

        } else if (view == nextWeek) {
            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.DAY_OF_WEEK, 7);
            day_start = cal.get(Calendar.DAY_OF_MONTH);
            month_start = cal.get(Calendar.MONTH) + 1;
            year_start = cal.get(Calendar.YEAR);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "nextWeek" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            updateLayout(day_start, month_start - 1, year_start, true);
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
            mSubscriptions.dispose();
    }

    @Override
    public void refreshLayout(boolean showRefresh) {
        mSwipeRefreshLayout.setRefreshing(false);
        updateLayout(day_start, month_start - 1, year_start, showRefresh);
    }
}
