package io.development.tymo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.reactivex.disposables.CompositeDisposable;

public class ReminderDateTimeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private MaterialSpinner spinnerRepeatPicker;
    private LinearLayout repeatBox, repeatNumberBox;
    private TextView cleanButton, applyButton, repeatEditText, repeatMax;
    private ImageView clearDateStart, clearTimeStart;
    private TextView dateStart, timeStart, repeatLastDate;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private ReminderWrapper reminderWrapperTemp;

    private boolean error = false;

    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;

    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_date_time);

        mSubscriptions = new CompositeDisposable();

        cleanButton = (TextView) findViewById(R.id.cleanButton);
        applyButton = (TextView) findViewById(R.id.applyButton);
        spinnerRepeatPicker = (MaterialSpinner) findViewById(R.id.repeatPicker);
        repeatBox = (LinearLayout) findViewById(R.id.repeatBox);
        repeatNumberBox = (LinearLayout) findViewById(R.id.repeatNumberBox);
        repeatEditText = (EditText) findViewById(R.id.repeatEditText);
        repeatMax = (TextView) findViewById(R.id.repeatMax);
        dateStart = (TextView) findViewById(R.id.dateStart);
        timeStart = (TextView) findViewById(R.id.timeStart);
        repeatLastDate = (TextView) findViewById(R.id.repeatLastDate);
        clearDateStart = (ImageView) findViewById(R.id.clearDateStart);
        clearTimeStart = (ImageView) findViewById(R.id.clearTimeStart);

        cleanButton.setOnClickListener(this);
        applyButton.setOnClickListener(this);
        dateStart.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        clearDateStart.setOnClickListener(this);
        clearTimeStart.setOnClickListener(this);

        cleanButton.setText(R.string.cancel);

        dateFormat = new DateFormat(this);

        reminderWrapperTemp = (ReminderWrapper) getIntent().getSerializableExtra("reminder_temp");

        day_start = -1;
        month_start = -1;
        year_start = -1;
        day_end = -1;
        month_end = -1;
        year_end = -1;
        minutes_start = -1;
        minutes_end = -1;
        hour_start = -1;
        hour_end = -1;

        day_start = getReminder().getDayStart();
        month_start = getReminder().getMonthStart() - 1;
        year_start = getReminder().getYearStart();
        minutes_start = getReminder().getMinuteStart();
        hour_start = getReminder().getHourStart();

        if (!getReminder().getDateStartEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year_start, month_start, day_start);
            String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            dateStart.setText(day + "/" + month + "/" + year_start);
            clearDateStart.setVisibility(View.VISIBLE);
        } else {
            dateStart.setText("");
            clearDateStart.setVisibility(View.GONE);
            day_start = -1;
            month_start = -1;
            year_start = -1;
        }

        if (!getReminder().getTimeStartEmpty()) {
            String hourString = String.format("%02d", hour_start);
            String minuteString = String.format("%02d", minutes_start);
            timeStart.setText(hourString + ":" + minuteString);
            clearTimeStart.setVisibility(View.VISIBLE);
        } else {
            timeStart.setText("");
            clearTimeStart.setVisibility(View.GONE);
            minutes_start = -1;
            hour_start = -1;
        }

        spinnerRepeatPicker.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinnerRepeatPicker.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                getReminder().setRepeatType(position);

                if (position == 0) {
                    repeatNumberBox.setVisibility(View.GONE);
                } else {
                    repeatNumberBox.setVisibility(View.VISIBLE);
                }

                setRepeatLastDate();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        spinnerRepeatPicker.setSelectedIndex(getReminder().getRepeatType());
        repeatNumberBox.setActivated(spinnerRepeatPicker.getSelectedIndex() != 0);

        repeatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                getReminder().setRepeatQty(number);
                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                getReminder().setRepeatQty(number);
                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                getReminder().setRepeatQty(number);
                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }
        });

        if (getReminder().getRepeatQty() < 1) {
            repeatEditText.setText("");
            repeatLastDate.setVisibility(View.INVISIBLE);
        } else {
            repeatEditText.setText(String.valueOf(getReminder().getRepeatQty()));
            repeatLastDate.setVisibility(View.VISIBLE);
            setRepeatLastDate();
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

    }

    private void setRepeatLastDate() {
        if (getReminder().getRepeatQty() > 0 && getReminder().getRepeatType() > 0 && day_start != -1) {
            repeatNumberBox.setVisibility(View.VISIBLE);
            repeatLastDate.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(year_start, month_start, day_start);

            switch (getReminder().getRepeatType()) {
                case Constants.DAILY:
                    calendar.add(Calendar.DAY_OF_WEEK, 1 * getReminder().getRepeatQty());
                    getReminder().setLastDateTime(calendar.getTimeInMillis());
                    break;
                case Constants.WEEKLY:
                    calendar.add(Calendar.DAY_OF_WEEK, 7 * getReminder().getRepeatQty());
                    getReminder().setLastDateTime(calendar.getTimeInMillis());
                    break;
                case Constants.MONTHLY:
                    calendar.add(Calendar.MONTH, 1 * getReminder().getRepeatQty());
                    getReminder().setLastDateTime(calendar.getTimeInMillis());
                    break;
                default:
                    getReminder().setLastDateTime(calendar.getTimeInMillis());
                    break;
            }

            String dayOfWeek = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            String month = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int year = calendar.get(Calendar.YEAR);
            String date = this.getResources().getString(R.string.date_format_03, dayOfWeek, day, month, year);
            repeatLastDate.setText(this.getString(R.string.repeat_last_date, date));
        }
        else{
            repeatLastDate.setVisibility(View.INVISIBLE);
            if (getReminder().getRepeatType() == 0){
                repeatNumberBox.setVisibility(View.GONE);
            }
        }
    }

    private void applyChanges() {

        int err = 0, repeat_qty = 0;
        boolean dateStartEmpty = false, timeStartEmpty = false;
        List<Integer> date;

        date = getDateFromView();

        if (date.get(0) == -1) {
            dateStartEmpty = true;
            repeat_qty = -1;
            getReminder().setRepeatType(0);
        } else {
            dateStartEmpty = false;

            if (!repeatEditText.getText().toString().matches("")) {
                repeat_qty = Integer.parseInt(repeatEditText.getText().toString());
            } else {
                repeat_qty = -1;
            }
        }

        if (date.get(6) == -1) {
            timeStartEmpty = true;
            date.set(6, 0);
            date.set(7, 0);
        } else {
            timeStartEmpty = false;
        }

        if (dateStartEmpty && !timeStartEmpty) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_start_required_reminder, Toast.LENGTH_LONG).show();
        } else if (!dateStartEmpty && getReminder().getRepeatType() != 0 && repeat_qty < 1) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
        } else {

        }

        if (err == 0) {
            getReminder().setDateStartEmpty(dateStartEmpty);
            getReminder().setDateEndEmpty(true);
            getReminder().setTimeStartEmpty(timeStartEmpty);
            getReminder().setTimeEndEmpty(true);
            getReminder().setDayStart(date.get(0));
            getReminder().setMonthStart(date.get(1) + 1);
            getReminder().setYearStart(date.get(2));
            getReminder().setDayEnd(date.get(0));
            getReminder().setMonthEnd(date.get(1) + 1);
            getReminder().setYearEnd(date.get(2));
            getReminder().setMinuteStart(date.get(6));
            getReminder().setHourStart(date.get(7));
            getReminder().setMinuteEnd(date.get(6));
            getReminder().setHourEnd(date.get(7));

            if (dateStartEmpty) {
                getReminder().setDateStartEmpty(true);
                getReminder().setDateEndEmpty(true);
                getReminder().setTimeStartEmpty(true);
                getReminder().setTimeEndEmpty(true);
                getReminder().setDayStart(-1);
                getReminder().setMonthStart(-1);
                getReminder().setYearStart(-1);
                getReminder().setDayEnd(-1);
                getReminder().setMonthEnd(-1);
                getReminder().setYearEnd(-1);
                getReminder().setMinuteStart(0);
                getReminder().setHourStart(0);
                getReminder().setMinuteEnd(0);
                getReminder().setHourEnd(0);

                getReminder().setRepeatQty(-1);
                getReminder().setRepeatType(0);
            } else if (getReminder().getRepeatType() == 0) {
                getReminder().setRepeatQty(-1);
            } else {
                getReminder().setRepeatQty(Integer.parseInt(repeatEditText.getText().toString()));
            }

            if (!dateStartEmpty) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(getReminder().getYearStart(), getReminder().getMonthStart() - 1, getReminder().getDayStart(), getReminder().getHourStart(), getReminder().getMinuteStart());
                getReminder().setDateTimeStart(calendar.getTimeInMillis());

                calendar.set(getReminder().getYearEnd(), getReminder().getMonthEnd() - 1, getReminder().getDayEnd(), getReminder().getHourEnd(), getReminder().getMinuteEnd());
                getReminder().setDateTimeEnd(calendar.getTimeInMillis());

                switch (getReminder().getRepeatType()) {
                    case Constants.DAILY:
                        calendar.add(Calendar.DAY_OF_WEEK, 1 * getReminder().getRepeatQty());
                        getReminder().setLastDateTime(calendar.getTimeInMillis());
                        break;
                    case Constants.WEEKLY:
                        calendar.add(Calendar.DAY_OF_WEEK, 7 * getReminder().getRepeatQty());
                        getReminder().setLastDateTime(calendar.getTimeInMillis());
                        break;
                    case Constants.MONTHLY:
                        calendar.add(Calendar.MONTH, 1 * getReminder().getRepeatQty());
                        getReminder().setLastDateTime(calendar.getTimeInMillis());
                        break;
                    default:
                        getReminder().setLastDateTime(calendar.getTimeInMillis());
                        break;
                }

            } else {
                getReminder().setDateTimeStart(-1);
                getReminder().setDateTimeEnd(-1);
                getReminder().setLastDateTime(-1);
            }

            Intent intent = new Intent();
            intent.putExtra("add_date_time", reminderWrapperTemp);
            setResult(RESULT_OK, intent);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
            finish();

        } else {
            error = true;
        }
    }

    public List<Integer> getDateFromView() {
        List<Integer> list = new ArrayList<>();

        list.add(day_start);
        list.add(month_start);
        list.add(year_start);

        list.add(day_end);
        list.add(month_end);
        list.add(year_end);

        list.add(minutes_start);
        list.add(hour_start);

        list.add(minutes_end);
        list.add(hour_end);

        return list;
    }

    public ReminderServer getReminder() {
        if (reminderWrapperTemp != null)
            return reminderWrapperTemp.getReminderServer();
        else
            return null;
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day + "/" + month + "/" + year;

        day_start = dayOfMonth;
        month_start = monthOfYear;
        year_start = year;
        dateStart.setText(date);

        if (dateStart.getText().toString().matches("")) {
            clearDateStart.setVisibility(View.GONE);
        } else {
            clearDateStart.setVisibility(View.VISIBLE);
        }

        setRepeatLastDate();

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String time = hourString + ":" + minuteString;

        hour_start = hourOfDay;
        minutes_start = minute;
        timeStart.setText(time);

        if (timeStart.getText().toString().matches("")) {
            clearTimeStart.setVisibility(View.GONE);
        } else {
            clearTimeStart.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        if (v == applyButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            applyChanges();
        } else if (v == cleanButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (v == dateStart) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    year_start,
                    month_start,
                    day_start
            );

            dpd.setMinDate(now);
            dpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == timeStart) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            if (hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start);

            tpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == clearDateStart) {
            day_start = -1;
            month_start = -1;
            year_start = -1;
            dateStart.setText("");
            clearDateStart.setVisibility(View.GONE);
            setRepeatLastDate();
        } else if (v == clearTimeStart) {
            hour_start = -1;
            minutes_start = -1;
            timeStart.setText("");
            clearTimeStart.setVisibility(View.GONE);
        }

    }
}
