package io.development.tymo.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


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
import io.development.tymo.activities.ReminderActivity;
import io.development.tymo.model_server.ReminderServer;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReminderEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private int repeat_type = 0;
    private int repeat_qty = -1;

    private boolean error = false;
    private FirebaseAnalytics mFirebaseAnalytics;

    private int day_start, month_start, year_start;
    private int minutes_start, hour_start;

    private TextView dateStart;
    private TextView timeStart;
    private LinearLayout repeatNumberBox, repeatBox;
    private MaterialSpinner spinner;

    private EditText repeatEditText, titleEditText;

    public static Fragment newInstance(String text) {
        ReminderEditFragment fragment = new ReminderEditFragment();
        return fragment;
    }

    public ReminderEditFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reminder_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repeatEditText = (EditText) view.findViewById(R.id.repeatEditText);
        titleEditText = (EditText) view.findViewById(R.id.title);

        dateStart = (TextView) view.findViewById(R.id.dateStart);
        timeStart = (TextView) view.findViewById(R.id.timeStart);
        repeatNumberBox = (LinearLayout) view.findViewById(R.id.repeatNumberBox);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);

        day_start = -1;
        month_start = -1;
        year_start = -1;
        minutes_start = -1;
        hour_start = -1;

        repeatNumberBox.setVisibility(View.GONE);

        spinner = (MaterialSpinner) view.findViewById(R.id.repeatPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                repeat_type = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if (position != 0)
                    repeatNumberBox.setVisibility(View.VISIBLE);
                else
                    repeatNumberBox.setVisibility(View.GONE);
            }
        });

        timeStart.setOnClickListener(this);
        dateStart.setOnClickListener(this);

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ReminderActivity reminderActivity = (ReminderActivity) getActivity();
                reminderActivity.setReminderCardText(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ReminderActivity reminderActivity = (ReminderActivity) getActivity();
        if (reminderActivity != null)
            setLayout(reminderActivity.getReminder());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
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
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");
        if (dpd != null) dpd.setOnDateSetListener(this);

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        if (tpd != null) tpd.setOnTimeSetListener(this);
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

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String time = hourString + ":" + minuteString;

        minutes_start = minute;
        hour_start = hourOfDay;
        timeStart.setText(time);
        timeStart.setError(null);

        ReminderActivity reminderActivity = (ReminderActivity) getActivity();
        reminderActivity.setReminderCardTime(time);
    }

    @Override
    public void onClick(View v) {
        if (v == timeStart) {
            Calendar now = Calendar.getInstance();
            if (hour_start != -1)
                now.set(year_start, month_start, day_start, hour_start, minutes_start);
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    ReminderEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == dateStart) {
            Calendar now = Calendar.getInstance();
            if (year_start != -1)
                now.set(year_start, month_start, day_start);
            com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                    ReminderEditFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog");
        }

    }

    public String getTitleFromView() {
        return titleEditText.getText().toString();
    }

    public List<Integer> getDateFromView() {
        List<Integer> list = new ArrayList<>();

        list.add(day_start);
        list.add(month_start);
        list.add(year_start);

        list.add(minutes_start);
        list.add(hour_start);

        return list;
    }

    public void updateError(boolean error) {
        this.error = error;
        //titleEditText.setBackgroundResource(R.drawable.bg_act_edit_box_required);
    }

    public List<Integer> getRepeatFromView() {
        List<Integer> list = new ArrayList<>();
        String temp = repeatEditText.getText().toString();
        if (repeat_type == 0)
            repeat_qty = -1;
        else if (temp.length() > 0)
            repeat_qty = Integer.parseInt(temp);

        list.add(repeat_type);
        list.add(repeat_qty);
        return list;
    }

    public void setLayout(ReminderServer reminderServer) {
        if (reminderServer != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart());

            String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String date = day + "/" + month + "/" + reminderServer.getYearStart();

            String hourString = String.format("%02d", reminderServer.getHourStart());
            ;
            String minuteString = String.format("%02d", reminderServer.getMinuteStart());
            ;
            String time = hourString + ":" + minuteString;

            dateStart.setText(date);
            timeStart.setText(time);
            titleEditText.setText(reminderServer.getTitle());

            day_start = reminderServer.getDayStart();
            month_start = reminderServer.getMonthStart() - 1;
            year_start = reminderServer.getYearStart();
            minutes_start = reminderServer.getMinuteStart();
            hour_start = reminderServer.getHourStart();

            if (reminderServer.getRepeatType() == 0) {
                spinner.setSelectedIndex(0);
                repeatBox.setVisibility(View.VISIBLE);
            } else {
                repeatBox.setVisibility(View.GONE);
                repeatNumberBox.setVisibility(View.GONE);
            }
        }
    }
}
