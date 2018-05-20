package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddPart3Activity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ActivityWrapper activityWrapper;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CompositeDisposable mSubscriptions;
    private ActivityServer activityServer;

    private TextView confirmationButton;
    private ImageView mBackButton;
    private RelativeLayout optionBox1, optionBox2, optionBox3;
    private ImageView checkBoxActivated1, checkBoxActivated2, checkBoxActivated3, optionIcon1, optionIcon2, optionIcon3;
    private TextView optionTitle1, optionTitle2, optionTitle3, optionText1, optionText2, optionText3;

    private int selected = -1;
    private User user_friend = null;
    private ArrayList<User> listUserCompare = new ArrayList<>();
    private int d = -1, m = -1, y = -1;

    ArrayList<User> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_create_step_3);

        mSubscriptions = new CompositeDisposable();

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        optionBox1 = (RelativeLayout) findViewById(R.id.optionBox1);
        optionBox2 = (RelativeLayout) findViewById(R.id.optionBox2);
        optionBox3 = (RelativeLayout) findViewById(R.id.optionBox3);
        checkBoxActivated1 = (ImageView) findViewById(R.id.checkBoxActivated1);
        checkBoxActivated2 = (ImageView) findViewById(R.id.checkBoxActivated2);
        checkBoxActivated3 = (ImageView) findViewById(R.id.checkBoxActivated3);
        optionIcon1 = (ImageView) findViewById(R.id.optionIcon1);
        optionIcon2 = (ImageView) findViewById(R.id.optionIcon2);
        optionIcon3 = (ImageView) findViewById(R.id.optionIcon3);
        optionTitle1 = (TextView) findViewById(R.id.optionTitle1);
        optionTitle2 = (TextView) findViewById(R.id.optionTitle2);
        optionTitle3 = (TextView) findViewById(R.id.optionTitle3);
        optionText1 = (TextView) findViewById(R.id.optionText1);
        optionText2 = (TextView) findViewById(R.id.optionText2);
        optionText3 = (TextView) findViewById(R.id.optionText3);

        mBackButton.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        optionBox1.setOnClickListener(this);
        optionBox2.setOnClickListener(this);
        optionBox3.setOnClickListener(this);

        mBackButton.setOnTouchListener(this);

        confirmationButton.setText(R.string.create);

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_wrapper");

        UserWrapper userWrapper = (UserWrapper) getIntent().getSerializableExtra("act_free_friend_usr");
        if (userWrapper != null)
            user_friend = userWrapper.getUser();
        else {
            userWrapper = (UserWrapper) getIntent().getSerializableExtra("ListCreateActivityCompare");
            if (userWrapper != null) {
                listUserCompare = userWrapper.getUsers();
                listUserCompare.remove(0);
            }
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private void register() {
        int err = 0;
        if (selected < 0) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_visibility_required, Toast.LENGTH_LONG).show();
        }

        if (err == 0) {
            activityWrapper.getActivityServer().setVisibility(selected);

            d = activityWrapper.getActivityServer().getDayStart();
            m = activityWrapper.getActivityServer().getMonthStart() - 1;
            y = activityWrapper.getActivityServer().getYearStart();

            registerProcess(activityWrapper.getActivityServer());
            setProgress(true);
        }
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void registerProcess(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivity(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int year2 = c2.get(Calendar.YEAR);

        if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();
        else {

        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("d", d);
        intent.putExtra("m", m);
        intent.putExtra("y", y);
        setResult(RESULT_OK, intent);
        startActivity(intent);

    }

    private void handleError(Throwable error) {
        setProgress(false);
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    private void getActivityStartToday() {
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
        if (mJobManager.getAllJobRequestsForTag(NotificationSyncJob.TAG).size() > 0)
            mJobManager.cancelAllForTag(NotificationSyncJob.TAG);

        ArrayList<Object> list = new ArrayList<>();
        ArrayList<ActivityOfDay> list_notify = new ArrayList<>();

        if (response.getMyCommitAct() != null) {
            ArrayList<ActivityServer> activityServers = response.getMyCommitAct();
            for (int i = 0; i < activityServers.size(); i++) {
                list.add(activityServers.get(i));
            }
        }
        if (response.getMyCommitFlag() != null) {
            ArrayList<FlagServer> flagServers = response.getMyCommitFlag();
            for (int i = 0; i < flagServers.size(); i++) {
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
                        activityServer.getDayStart(), activityServer.getMonthStart(), activityServer.getYearStart()));
            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);
                String title = "";
                if (flagServer.getTitle().matches("")) {
                    if (flagServer.getType()) {
                        title = getString(R.string.flag_available);
                    }
                    else{
                        title = getString(R.string.flag_unavailable);
                    }
                } else {
                    title = flagServer.getTitle();
                }
                list_notify.add(new ActivityOfDay(title, flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
                        flagServer.getDayStart(), flagServer.getMonthStart(), flagServer.getYearStart()));
            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);
                list_notify.add(new ActivityOfDay(reminderServer.getText(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
                        reminderServer.getDayStart(), reminderServer.getMonthStart(), reminderServer.getYearStart()));
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
            c1.set(Calendar.MONTH, activityOfDay.getMonth() - 1);
            c1.set(Calendar.YEAR, activityOfDay.getYear());
            c1.set(Calendar.HOUR_OF_DAY, activityOfDay.getHourStart());
            c1.set(Calendar.MINUTE, activityOfDay.getMinuteStart());
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
            c2.set(Calendar.MONTH, activityOfDayNext.getMonth() - 1);
            c2.set(Calendar.YEAR, activityOfDayNext.getYear());
            c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
            c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);

            while (activityOfDayNext != null && c1.getTimeInMillis() == c2.getTimeInMillis()) {
                j++;
                count_same++;
                if (j < list_notify.size()) {
                    activityOfDayNext = list_notify.get(j);
                    c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
                    c2.set(Calendar.MONTH, activityOfDayNext.getMonth() - 1);
                    c2.set(Calendar.YEAR, activityOfDayNext.getYear());
                    c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
                    c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
                    c2.set(Calendar.SECOND, 0);
                    c2.set(Calendar.MILLISECOND, 0);
                } else
                    activityOfDayNext = null;
            }
            activityOfDay.setCommitmentSameHour(count_same);

            time_exact = (int) (c1.getTimeInMillis() - c3.getTimeInMillis()) / (1000 * 60);
            if (time_exact >= Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT) {
                c1.add(Calendar.MINUTE, -Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT);
                time_to_happen = c1.getTimeInMillis() - c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            if (time_exact >= 1440) {
                c1.add(Calendar.MINUTE, -1380);
                time_to_happen = c1.getTimeInMillis() - c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras2)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            i = j - 1;
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

    private void updateLayout() {
        switch (selected) {
            case 1:
                optionIcon1.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle1.setTextColor(getResources().getColor(R.color.grey_600));
                optionText1.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox1.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon2.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                optionTitle2.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionText2.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionBox2.setBackgroundColor(getResources().getColor(R.color.select));
                optionIcon3.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle3.setTextColor(getResources().getColor(R.color.grey_600));
                optionText3.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox3.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case 2:
                optionIcon1.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle1.setTextColor(getResources().getColor(R.color.grey_600));
                optionText1.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox1.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon2.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle2.setTextColor(getResources().getColor(R.color.grey_600));
                optionText2.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox2.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon3.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                optionTitle3.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionText3.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionBox3.setBackgroundColor(getResources().getColor(R.color.select));
                break;
            default:
                optionIcon1.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                optionTitle1.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionText1.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionBox1.setBackgroundColor(getResources().getColor(R.color.select));
                optionIcon2.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle2.setTextColor(getResources().getColor(R.color.grey_600));
                optionText2.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox2.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon3.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle3.setTextColor(getResources().getColor(R.color.grey_600));
                optionText3.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox3.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            register();
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (v == optionBox1) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "optionBox1" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            checkBoxActivated1.setVisibility(View.VISIBLE);
            checkBoxActivated2.setVisibility(View.GONE);
            checkBoxActivated3.setVisibility(View.GONE);
            selected = 0;
            updateLayout();
        } else if (v == optionBox2) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "optionBox2" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            checkBoxActivated1.setVisibility(View.GONE);
            checkBoxActivated2.setVisibility(View.VISIBLE);
            checkBoxActivated3.setVisibility(View.GONE);
            selected = 1;
            updateLayout();
        } else if (v == optionBox3) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "optionBox3" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            checkBoxActivated1.setVisibility(View.GONE);
            checkBoxActivated2.setVisibility(View.GONE);
            checkBoxActivated3.setVisibility(View.VISIBLE);
            selected = 2;
            updateLayout();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(getResources().getColor(R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(getResources().getColor(R.color.grey_400));
            }
        }

        return false;
    }

}
