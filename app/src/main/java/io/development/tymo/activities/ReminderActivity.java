package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateFields;

public class ReminderActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private LinearLayout confirmationButtonFit, confirmationButtonRemove, addDateHourButton, privacyBox;
    private RelativeLayout dateBox;
    private TextView confirmationButton, addDateHourButtonText, dateHourText, repeatText;
    private EditText reminderEditText;
    private ImageView mBackButton, addDateHourButtonIcon, dateIcon;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ReminderWrapper reminderWrapper, reminderWrapperTemp;
    private ReminderServer reminderServer, reminderServerTemp;
    private RelativeLayout confirmationButtonBar, confirmationButtonBarFitRemove;

    private boolean error = false;
    private boolean edit;
    private int d = -1, m = -1, y = -1;

    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        mSubscriptions = new CompositeDisposable();

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        confirmationButtonBar = (RelativeLayout) findViewById(R.id.confirmationButtonBar);
        confirmationButtonBarFitRemove = (RelativeLayout) findViewById(R.id.confirmationButtonBarFitRemove);
        confirmationButtonFit = (LinearLayout) findViewById(R.id.confirmationButtonFit);
        confirmationButtonRemove = (LinearLayout) findViewById(R.id.confirmationButtonRemove);
        privacyBox = (LinearLayout) findViewById(R.id.privacyBox);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        dateBox = (RelativeLayout) findViewById(R.id.dateBox);
        dateHourText = (TextView) findViewById(R.id.dateHourText);
        dateIcon = (ImageView) findViewById(R.id.dateIcon);
        repeatText = (TextView) findViewById(R.id.repeatText);
        addDateHourButton = (LinearLayout) findViewById(R.id.addDateHourButton);
        addDateHourButtonIcon = (ImageView) findViewById(R.id.addDateHourButtonIcon);
        addDateHourButtonText = (TextView) findViewById(R.id.addDateHourButtonText);
        reminderEditText = (EditText) findViewById(R.id.reminderEditText);

        confirmationButtonFit.setVisibility(View.GONE);
        confirmationButtonRemove.setVisibility(View.VISIBLE);

        confirmationButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        addDateHourButton.setOnClickListener(this);
        dateBox.setOnClickListener(this);
        repeatText.setOnClickListener(this);
        confirmationButtonRemove.setOnClickListener(this);

        mBackButton.setOnTouchListener(this);
        addDateHourButton.setOnTouchListener(this);
        dateBox.setOnTouchListener(this);
        repeatText.setOnTouchListener(this);

        dateFormat = new DateFormat(this);

        reminderWrapper = (ReminderWrapper) getIntent().getSerializableExtra("reminder_show");

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String creator = mSharedPreferences.getString(Constants.EMAIL, "");

        reminderServer = new ReminderServer();

        reminderServer.setCreator(creator);

        if (reminderWrapper != null) {
            edit = true;
            confirmationButtonBar.setVisibility(View.GONE);
            confirmationButtonBarFitRemove.setVisibility(View.VISIBLE);

            reminderServer.setId(reminderWrapper.getReminderServer().getId());
            reminderServer.setText(reminderWrapper.getReminderServer().getText());
            reminderEditText.setText(reminderServer.getText());

            setReminderInformation(reminderWrapper.getReminderServer().getId());
        } else {
            edit = false;
            mBackButton.setRotation(45);
            mBackButton.setImageResource(R.drawable.ic_add);
            confirmationButtonBarFitRemove.setVisibility(View.GONE);
            confirmationButtonBar.setVisibility(View.VISIBLE);
            confirmationButton.setText(R.string.create_reminder);
            dateBox.setVisibility(View.GONE);

            reminderServer.setText("");
            reminderEditText.setText(reminderServer.getText());

            setReminderServer(null);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

    }

    private void setReminderInformation(long id) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getFlagReminder(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGet, this::handleError));
    }

    private void handleResponseGet(Response response) {
        reminderWrapper.getReminderServer().setDayStart(response.getMyCommitReminder().get(0).getDayStart());
        reminderWrapper.getReminderServer().setMonthStart(response.getMyCommitReminder().get(0).getMonthStart());
        reminderWrapper.getReminderServer().setYearStart(response.getMyCommitReminder().get(0).getYearStart());
        reminderWrapper.getReminderServer().setDayEnd(response.getMyCommitReminder().get(0).getDayEnd());
        reminderWrapper.getReminderServer().setMonthEnd(response.getMyCommitReminder().get(0).getMonthEnd());
        reminderWrapper.getReminderServer().setYearEnd(response.getMyCommitReminder().get(0).getYearEnd());
        reminderWrapper.getReminderServer().setMinuteStart(response.getMyCommitReminder().get(0).getMinuteStart());
        reminderWrapper.getReminderServer().setHourStart(response.getMyCommitReminder().get(0).getHourStart());
        reminderWrapper.getReminderServer().setMinuteEnd(response.getMyCommitReminder().get(0).getMinuteEnd());
        reminderWrapper.getReminderServer().setHourEnd(response.getMyCommitReminder().get(0).getHourEnd());
        reminderWrapper.getReminderServer().setDateTimeStart(response.getMyCommitReminder().get(0).getDateTimeStart());
        reminderWrapper.getReminderServer().setDateTimeEnd(response.getMyCommitReminder().get(0).getDateTimeEnd());
        reminderWrapper.getReminderServer().setDateStartEmpty(response.getMyCommitReminder().get(0).getDateStartEmpty());
        reminderWrapper.getReminderServer().setTimeStartEmpty(response.getMyCommitReminder().get(0).getTimeStartEmpty());
        reminderWrapper.getReminderServer().setDateEndEmpty(response.getMyCommitReminder().get(0).getDateEndEmpty());
        reminderWrapper.getReminderServer().setTimeEndEmpty(response.getMyCommitReminder().get(0).getTimeEndEmpty());
        reminderWrapper.getReminderServer().setLastDateTime(response.getMyCommitReminder().get(0).getLastDateTime());

        setReminderServer(reminderWrapper.getReminderServer());
        setInformation(reminderWrapper.getReminderServer());

        setProgress(false);
    }

    private void setReminderServer(ReminderServer reminder) {
        if (reminder != null) {
            reminderServer.setRepeatQty(reminder.getRepeatQty());
            reminderServer.setRepeatType(reminder.getRepeatType());

            reminderServer.setDayStart(reminder.getDayStart());
            reminderServer.setMonthStart(reminder.getMonthStart());
            reminderServer.setYearStart(reminder.getYearStart());
            reminderServer.setDayEnd(reminder.getDayEnd());
            reminderServer.setMonthEnd(reminder.getMonthEnd());
            reminderServer.setYearEnd(reminder.getYearEnd());
            reminderServer.setMinuteStart(reminder.getMinuteStart());
            reminderServer.setHourStart(reminder.getHourStart());
            reminderServer.setMinuteEnd(reminder.getMinuteEnd());
            reminderServer.setHourEnd(reminder.getHourEnd());
            reminderServer.setDateTimeStart(reminder.getDateTimeStart());
            reminderServer.setDateTimeEnd(reminder.getDateTimeEnd());
            reminderServer.setDateStartEmpty(reminder.getDateStartEmpty());
            reminderServer.setTimeStartEmpty(reminder.getTimeStartEmpty());
            reminderServer.setDateEndEmpty(reminder.getDateEndEmpty());
            reminderServer.setTimeEndEmpty(reminder.getTimeEndEmpty());
            reminderServer.setLastDateTime(reminder.getLastDateTime());
        } else {
            reminderServer.setRepeatQty(-1);
            reminderServer.setRepeatType(0);

            reminderServer.setDayStart(-1);
            reminderServer.setMonthStart(-1);
            reminderServer.setYearStart(-1);
            reminderServer.setDayEnd(-1);
            reminderServer.setMonthEnd(-1);
            reminderServer.setYearEnd(-1);
            reminderServer.setMinuteStart(0);
            reminderServer.setHourStart(0);
            reminderServer.setMinuteEnd(0);
            reminderServer.setHourEnd(0);
            reminderServer.setDateTimeStart(-1);
            reminderServer.setDateTimeEnd(-1);
            reminderServer.setDateStartEmpty(true);
            reminderServer.setTimeStartEmpty(true);
            reminderServer.setDateEndEmpty(true);
            reminderServer.setTimeEndEmpty(true);
            reminderServer.setLastDateTime(-1);
        }
    }

    public void setInformation(ReminderServer reminder) {
        reminderEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                showHideUpdateButton();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showHideUpdateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
                showHideUpdateButton();
            }
        });

        if (!reminder.getDateStartEmpty()) {
            dateBox.setVisibility(View.VISIBLE);
            addDateHourButton.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            Calendar calendar3 = Calendar.getInstance();
            calendar.set(reminder.getYearStart(), reminder.getMonthStart() - 1, reminder.getDayStart());
            calendar2.set(reminder.getYearEnd(), reminder.getMonthEnd() - 1, reminder.getDayEnd());
            calendar3.setTimeInMillis(reminder.getLastDateTime());

            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayOfWeekStart2 = dateFormat.formatDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
            String dayStart = String.format("%02d", reminder.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = reminder.getYearStart();
            String hourStart = String.format("%02d", reminder.getHourStart());
            String minuteStart = String.format("%02d", reminder.getMinuteStart());

            String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
            String dayOfWeekEnd2 = dateFormat.formatDayOfWeek(calendar2.get(Calendar.DAY_OF_WEEK));
            String dayEnd = String.format("%02d", reminder.getDayEnd());
            String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
            int yearEnd = reminder.getYearEnd();
            String hourEnd = String.format("%02d", reminder.getHourEnd());
            String minuteEnd = String.format("%02d", reminder.getMinuteEnd());

            String dayLast = String.format("%02d", calendar3.get(Calendar.DAY_OF_MONTH));
            String monthLast = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar3.getTime().getTime());
            int yearLast = calendar3.get(Calendar.YEAR);

            if (reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty()) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_03, dayOfWeekStart, dayStart, monthStart, yearStart));
            } else if (!reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty()) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_14, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
            } else if (reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty()) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
            } else if (reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty()) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
            } else if (!reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty()) {
                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_16, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
                }
            } else if (!reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty()) {
                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_15, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                }
            } else if (reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty()) {
                if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                }
            } else {
                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                        dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                    } else {
                        dateHourText.setText(this.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                    }
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_06, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                }
            }

            if (reminder.getRepeatType() == 0) {
                repeatText.setVisibility(View.GONE);
            } else {
                repeatText.setVisibility(View.VISIBLE);
                String date = "";

                switch (reminder.getRepeatType()) {
                    case Constants.DAILY:
                        if (reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_daily_01);
                        else if (!reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_daily_02, hourStart, minuteStart);
                        else if (reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_daily_03, hourEnd, minuteEnd);
                        else if (!reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_daily_04, hourStart, minuteStart, hourEnd, minuteEnd);
                        break;
                    case Constants.WEEKLY:
                        if (reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_01, dayOfWeekStart2);
                        else if (reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_02, dayOfWeekStart2, hourStart, minuteStart);
                        else if (reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_03, dayOfWeekStart2, hourEnd, minuteEnd);
                        else if (reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_04, dayOfWeekStart2, hourStart, minuteStart, hourEnd, minuteEnd);
                        else if (!reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_05, dayOfWeekStart2, dayOfWeekEnd2);
                        else if (!reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_06, dayOfWeekStart2, hourStart, minuteStart, dayOfWeekEnd2);
                        else if (!reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_07, dayOfWeekStart2, dayOfWeekEnd2, hourEnd, minuteEnd);
                        else if (!reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_weekly_08, dayOfWeekStart2, hourStart, minuteStart, dayOfWeekEnd2, hourEnd, minuteEnd);
                        break;
                    case Constants.MONTHLY:
                        if (reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_01, dayStart);
                        else if (reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_02, dayStart, hourStart, minuteStart);
                        else if (reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_03, dayStart, hourEnd, minuteEnd);
                        else if (reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_04, dayStart, hourStart, minuteStart, hourEnd, minuteEnd);
                        else if (!reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_05, dayStart, dayEnd);
                        else if (!reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_06, dayStart, hourStart, minuteStart, dayEnd);
                        else if (!reminder.getDateEndEmpty() && reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_07, dayStart, dayEnd, hourEnd, minuteEnd);
                        else if (!reminder.getDateEndEmpty() && !reminder.getTimeStartEmpty() && !reminder.getTimeEndEmpty())
                            date = this.getResources().getString(R.string.date_format_monthly_08, dayStart, hourStart, minuteStart, dayEnd, hourEnd, minuteEnd);
                        break;
                    default:
                        break;
                }

                dateHourText.setText(date);
                repeatText.setText(this.getResources().getString(R.string.date_format_repeat, dayStart, monthStart, yearStart, dayLast, monthLast, yearLast));
            }
        } else {
            dateBox.setVisibility(View.GONE);
            addDateHourButton.setVisibility(View.VISIBLE);
        }

        showHideUpdateButton();

    }

    private void showHideUpdateButton() {
        if (reminderWrapper != null) {
            if (!reminderEditText.getText().toString().matches(getReminder().getText()) || getReminder().getDayStart() != reminderServer.getDayStart() || getReminder().getMonthStart() != reminderServer.getMonthStart() ||
                    getReminder().getYearStart() != reminderServer.getYearStart() || getReminder().getMinuteStart() != reminderServer.getMinuteStart() || getReminder().getHourStart() != reminderServer.getHourStart() ||
                    getReminder().getRepeatType() != reminderServer.getRepeatType() || getReminder().getRepeatQty() != reminderServer.getRepeatQty()) {
                confirmationButtonBar.setVisibility(View.VISIBLE);
                confirmationButtonBarFitRemove.setVisibility(View.GONE);
                confirmationButton.setText(R.string.save_updates);
            } else {
                confirmationButtonBar.setVisibility(View.GONE);
                confirmationButtonBarFitRemove.setVisibility(View.VISIBLE);
            }
        }
    }

    private void register() {

        String text = reminderEditText.getText().toString();

        int err = 0;
        if (!validateFields(text)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_text_required, Toast.LENGTH_LONG).show();
        } else {

        }

        if (err == 0) {
            reminderServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());
            reminderServer.setText(text);

            d = reminderServer.getDayStart();
            m = reminderServer.getMonthStart() - 1;
            y = reminderServer.getYearStart();

            registerProcess(reminderServer);
            setProgress(true);

        } else {
            error = true;
        }
    }

    private void editReminder() {

        String text = reminderEditText.getText().toString();

        int err = 0;
        if (!validateFields(text)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_text_required, Toast.LENGTH_LONG).show();
        } else {

        }

        if (err == 0) {
            reminderServer.setText(text);

            d = reminderServer.getDayStart();
            m = reminderServer.getMonthStart() - 1;
            y = reminderServer.getYearStart();

            editProcess(reminderServer);
            setProgress(true);

        } else {
            error = true;
        }
    }

    private void registerProcess(ReminderServer reminderServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerReminder(reminderServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void editProcess(ReminderServer reminderServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editReminder(reminderWrapper.getReminderServer().getId(), reminderServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    public ReminderServer getReminder() {
        if (reminderWrapper != null)
            return reminderWrapper.getReminderServer();
        else
            return null;
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

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void deleteFlagActReminder(long id, ActivityServer activity) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().deleteActivity(id, activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm, this::handleError));
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

        ReminderServer reminder = getReminder();
        int d = reminder.getDayStart();
        int m = reminder.getMonthStart();
        int y = reminder.getYearStart();

        if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();

        setProgress(false);
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
                list_notify.add(new ActivityOfDay(flagServer.getTitle(), flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
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

    private void createDialogRemove() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView button1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView button2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);

        button1.setText(getResources().getString(R.string.no));
        button2.setText(getResources().getString(R.string.yes));
        text2.setVisibility(View.GONE);
        text1.setVisibility(View.VISIBLE);
        text1.setText(getResources().getString(R.string.delete_plans_question_text_reminder_3));

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    button1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    button1.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    button2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    button2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteReminder" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                ActivityServer activityServer = new ActivityServer();
                activityServer.setId(0);
                activityServer.setVisibility(Constants.REMINDER);

                deleteFlagActReminder(getReminder().getId(), activityServer);

                dg.dismiss();
            }
        });

        dg.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                reminderWrapperTemp = (ReminderWrapper) intent.getSerializableExtra("add_date_time");

                setReminderServer(reminderWrapperTemp.getReminderServer());
                setInformation(reminderWrapperTemp.getReminderServer());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            if (edit)
                editReminder();
            else
                register();
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (v == addDateHourButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addDateHourButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            Intent intent = new Intent(ReminderActivity.this, ReminderDateTimeActivity.class);
            intent.putExtra("reminder_show", reminderWrapper);
            intent.putExtra("reminder_temp", new ReminderWrapper(reminderServer));
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
        } else if (v == dateBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            Intent intent = new Intent(ReminderActivity.this, ReminderDateTimeActivity.class);
            intent.putExtra("reminder_show", reminderWrapper);
            intent.putExtra("reminder_temp", new ReminderWrapper(reminderServer));
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
        } else if (v == confirmationButtonRemove) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonRemove" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogRemove();
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_900));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            }
        } else if (view == addDateHourButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                addDateHourButtonText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                addDateHourButtonIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                addDateHourButtonText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                addDateHourButtonIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == dateBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                dateHourText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                dateIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
                if (repeatText.getVisibility() == View.VISIBLE)
                    repeatText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dateHourText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
                dateIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
                if (repeatText.getVisibility() == View.VISIBLE)
                    repeatText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
            }
        }

        return false;
    }
}
