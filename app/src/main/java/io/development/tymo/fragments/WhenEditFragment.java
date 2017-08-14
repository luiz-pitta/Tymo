package io.development.tymo.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.AddActivity;
import io.development.tymo.model_server.ActivityServer;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhenEditFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener, View.OnLongClickListener {

    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;

    private Calendar calendarStart;

    private int repeat_type = 0;
    private int repeat_qty = -1;
    private double lat = -500;
    private double lng = -500;

    private static final int PLACE_PICKER_REQUEST = 1020;
    private PlacePicker.IntentBuilder builder = null;
    private Intent placePicker = null;
    private String location = "";
    private boolean locationNotWorking = false;
    private AddActivity addActivity = null;

    private TextView mapText, repeatMax;
    private TextView dateStart, dateEnd;
    private TextView timeStart, timeEnd;
    private TextView locationText;
    private LinearLayout repeatNumberBox, repeatBox;
    private EditText repeatEditText;
    private MaterialSpinner spinner;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance(String text) {
        WhenEditFragment fragment = new WhenEditFragment();
        return fragment;
    }

    public WhenEditFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_act_where_when_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationText = (TextView) view.findViewById(R.id.locationText);
        repeatMax = (TextView) view.findViewById(R.id.repeatMax);
        dateStart = (TextView) view.findViewById(R.id.dateStart);
        dateEnd = (TextView) view.findViewById(R.id.dateEnd);
        timeStart = (TextView) view.findViewById(R.id.timeStart);
        timeEnd = (TextView) view.findViewById(R.id.timeEnd);
        repeatNumberBox = (LinearLayout) view.findViewById(R.id.repeatNumberBox);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);
        repeatEditText = (EditText) view.findViewById(R.id.repeatEditText);
        locationText.setOnClickListener(this);
        locationText.setOnLongClickListener(this);

        calendarStart = Calendar.getInstance();

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

        repeatNumberBox.setVisibility(View.GONE);

        repeatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (repeatEditText.getText().toString().matches("30")) {
                    repeatMax.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_600));
                }
                else {
                    repeatMax.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_400));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String number = String.valueOf(s);
                if (number.length() > 2) {
                    repeatEditText.setText("30");
                }
                if (repeatEditText.getText().toString().matches("30")) {
                    repeatMax.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_600));
                }
                else {
                    repeatMax.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_400));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (repeatEditText.getText().toString().matches("30")) {
                    repeatMax.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_600));
                }
                else {
                    repeatMax.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_400));
                }
            }
        });

        spinner = (MaterialSpinner) view.findViewById(R.id.repeatPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                repeat_type = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if (position != 0)
                    repeatNumberBox.setVisibility(View.VISIBLE);
                else
                    repeatNumberBox.setVisibility(View.GONE);
            }
        });


        dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        timeEnd.setOnClickListener(this);

        addActivity = (AddActivity) getActivity();

        if (addActivity.getActivity() != null && addActivity.getActivity().getLat() != -500) {
            LatLng latLng = new LatLng(addActivity.getActivity().getLat(), addActivity.getActivity().getLng());
            LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
            builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);
        } else
            builder = new PlacePicker.IntentBuilder();


        try {
            placePicker = builder.build(getActivity());
        } catch (Exception e) {
            locationNotWorking = true;
        }

        setLayout(addActivity.getActivity(), addActivity.getEditable());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog2");
        if (dpd != null) dpd.setOnDateSetListener(this);

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        if (tpd != null) tpd.setOnTimeSetListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        addActivity.setProgress(false);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(getActivity(), data);
                String name = selectedPlace.getAddress().toString();
                lat = selectedPlace.getLatLng().latitude;
                lng = selectedPlace.getLatLng().longitude;
                locationText.setText(name);
            }
        }
    }

    private void createDialogLocation(String adr) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        text1.setText(getActivity().getResources().getString(R.string.popup_message_naming_activity_local_title));
        text2.setText(getActivity().getResources().getString(R.string.popup_message_naming_activity_local_text));
        buttonText1.setText(getActivity().getResources().getString(R.string.close));
        buttonText2.setText(getActivity().getResources().getString(R.string.customize));
        editText.setText(adr);

        Dialog dialog = new Dialog(getActivity(), R.style.NewDialog);

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
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancelDialogLocation" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmDialogLocation" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                location = editText.getText().toString();
                if (!location.matches(""))
                    locationText.setText(location);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        calendarStart.set(year, monthOfYear, dayOfMonth);
        String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        calendar.set(yearEnd, monthOfYearEnd, dayOfMonthEnd);
        String day2 = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month2 = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day + "/" + month + "/" + year;
        String date2 = day2 + "/" + month2 + "/" + yearEnd;


        if (validadeDate(year, monthOfYear, dayOfMonth, yearEnd, monthOfYearEnd, dayOfMonthEnd)) {
            day_start = dayOfMonth;
            month_start = monthOfYear;
            year_start = year;
            day_end = dayOfMonthEnd;
            month_end = monthOfYearEnd;
            year_end = yearEnd;
            dateStart.setText(date);
            dateEnd.setText(date2);
        } else {
            day_start = dayOfMonth;
            month_start = monthOfYear;
            year_start = year;
            day_end = dayOfMonth;
            month_end = monthOfYear;
            year_end = year;
            dateStart.setText(date);
            dateEnd.setText(date);
        }

    }

    private boolean validadeDate(int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        if (yearEnd < year)
            return false;
        if (year == yearEnd) {
            if (monthOfYearEnd < monthOfYear)
                return false;
            else if (monthOfYearEnd == monthOfYear) {
                if (dayOfMonthEnd < dayOfMonth)
                    return false;
            }
        }

        return true;
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String hourStringEnd = String.format("%02d", hourOfDayEnd);
        String minuteStringEnd = String.format("%02d", minuteEnd);
        String time = hourString + ":" + minuteString;
        String time2 = hourStringEnd + ":" + minuteStringEnd;

        if (day_start == -1)
            Toast.makeText(getActivity(), "Preencha primeiro a data!", Toast.LENGTH_LONG).show();
        else if (validadeHour(hourOfDay, minute, hourOfDayEnd, minuteEnd)) {
            minutes_start = minute;
            hour_start = hourOfDay;
            minutes_end = minuteEnd;
            hour_end = hourOfDayEnd;
            timeStart.setText(time);
            timeEnd.setText(time2);
        } else {
            minutes_start = minute;
            hour_start = hourOfDay;
            minutes_end = minute;
            hour_end = hourOfDay;
            timeStart.setText(time);
            timeEnd.setText(time);
        }

    }

    private boolean validadeHour(int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        if (sameDay() && day_start != -1) {
            if (hourOfDayEnd < hourOfDay)
                return false;
            if (hourOfDayEnd == hourOfDay) {
                if (minuteEnd < minute)
                    return false;
            }
            return true;
        }
        return true;
    }

    private boolean sameDay() {
        return day_start == day_end && month_start == month_end && year_start == year_end;
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

    public String getLocationFromView() {
        return locationText.getText().toString();
    }

    public List<Double> getLatLngFromView() {
        List<Double> list = new ArrayList<>();

        list.add(lat);
        list.add(lng);
        return list;
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

    @Override
    public boolean onLongClick(View v) {
        if (v == locationText && lat != -500) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "locationText" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogLocation(locationText.getText().toString());
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == locationText && !locationNotWorking) {
            addActivity.setProgress(true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mapText" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (lat != -500) {
                LatLng latLng = new LatLng(lat, lng);
                LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);

                try {
                    placePicker = builder.build(getActivity());
                } catch (Exception e) {
                }
            }

            startActivityForResult(placePicker, PLACE_PICKER_REQUEST);
        } else if (v == timeStart) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    WhenEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start, hour_end, minutes_end);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400), ContextCompat.getColor(getActivity(), R.color.grey_100));
            tpd.setStartTitle(getResources().getString(R.string.date_start));
            tpd.setEndTitle(getResources().getString(R.string.date_end));
            tpd.setCurrentTab(0);
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == timeEnd) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    WhenEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start, hour_end, minutes_end);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400), ContextCompat.getColor(getActivity(), R.color.grey_100));
            tpd.setStartTitle(getResources().getString(R.string.date_start));
            tpd.setEndTitle(getResources().getString(R.string.date_end));
            tpd.setCurrentTab(1);
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == dateStart) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    WhenEditFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (year_start != -1)
                dpd.setStartDate(year_start, month_start, day_start, year_end, month_end, day_end);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400), ContextCompat.getColor(getActivity(), R.color.grey_100));
            dpd.setStartTitle(getResources().getString(R.string.date_start));
            dpd.setEndTitle(getResources().getString(R.string.date_end));
            dpd.setCurrentTab(0);
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == dateEnd) {

            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    WhenEditFragment.this,
                    calendarStart.get(Calendar.YEAR),
                    calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH)
            );

            dpd.setMinDate(calendarStart);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (year_start != -1)
                dpd.setStartDate(year_start, month_start, day_start, year_end, month_end, day_end);


            dpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400), ContextCompat.getColor(getActivity(), R.color.grey_100));
            dpd.setStartTitle(getResources().getString(R.string.date_start));
            dpd.setEndTitle(getResources().getString(R.string.date_end));
            dpd.setCurrentTab(1);
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        }

    }

    public void setLayout(ActivityServer activityServer, boolean edit) {
        if (activityServer != null && dateStart != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());

            String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());
            String day2 = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month2 = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String date = day + "/" + month + "/" + activityServer.getYearStart();
            String date2 = day2 + "/" + month2 + "/" + activityServer.getYearEnd();

            String hourString = String.format("%02d", activityServer.getHourStart());
            String minuteString = String.format("%02d", activityServer.getMinuteStart());
            String hourStringEnd = String.format("%02d", activityServer.getHourEnd());
            String minuteStringEnd = String.format("%02d", activityServer.getMinuteEnd());
            String time = hourString + ":" + minuteString;
            String time2 = hourStringEnd + ":" + minuteStringEnd;

            day_start = activityServer.getDayStart();
            month_start = activityServer.getMonthStart() - 1;
            year_start = activityServer.getYearStart();
            minutes_start = activityServer.getMinuteStart();
            hour_start = activityServer.getHourStart();

            day_end = activityServer.getDayEnd();
            month_end = activityServer.getMonthEnd() - 1;
            year_end = activityServer.getYearEnd();
            minutes_end = activityServer.getMinuteEnd();
            hour_end = activityServer.getHourEnd();

            dateStart.setText(date);
            dateEnd.setText(date2);
            timeStart.setText(time);
            timeEnd.setText(time2);

            locationText.setText(activityServer.getLocation());

            lat = activityServer.getLat();
            lng = activityServer.getLng();

            if (!edit) {

                if (activityServer.getRepeatType() == 0)
                    spinner.setSelectedIndex(activityServer.getRepeatType());
                else {
                    spinner.setSelectedIndex(activityServer.getRepeatType());
                    repeatNumberBox.setVisibility(View.VISIBLE);
                    repeatEditText.setText(String.valueOf(activityServer.getRepeatQty()));
                }
            } else {
                if (activityServer.getRepeatType() == 0 || activityServer.getRepeatType() == 5) {
                    spinner.setSelectedIndex(0);
                    repeatBox.setVisibility(View.VISIBLE);
                } else {
                    repeatBox.setVisibility(View.GONE);
                    repeatNumberBox.setVisibility(View.GONE);
                }

            }
        }
    }
}
