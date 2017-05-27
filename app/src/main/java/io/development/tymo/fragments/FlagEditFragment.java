package io.development.tymo.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.SelectPeopleActivity;
import io.development.tymo.activities.ShowGuestsActivity;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.RecyclerItemClickListener;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FlagEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private ImageView addPersonButton;
    private TextView guestsNumber;

    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private ArrayList<User> listPerson = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;
    private FirebaseAnalytics mFirebaseAnalytics;

    private LinearLayout selectionGuestBox, sendBox;

    private int repeat_type = 0;
    private int repeat_qty = -1;
    private int send_toAll = 0;
    private boolean isEdit = false;

    ArrayList<User> data = new ArrayList<>();

    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;

    private TextView dateStart, dateEnd;
    private TextView timeStart, timeEnd;

    private LinearLayout repeatEditLayout, repeatBox;
    private EditText repeatEditText, titleEditText;
    private MaterialSpinner spinner, sendPicker;

    private CompositeSubscription mSubscriptions;

    public static Fragment newInstance(String text) {
        FlagEditFragment fragment = new FlagEditFragment();
        return fragment;
    }

    public FlagEditFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flag_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubscriptions = new CompositeSubscription();

        guestsNumber = (TextView) view.findViewById(R.id.guestsNumber);
        dateStart = (TextView)view.findViewById(R.id.dateStart);
        dateEnd = (TextView)view.findViewById(R.id.dateEnd);
        timeStart = (TextView)view.findViewById(R.id.timeStart);
        timeEnd = (TextView)view.findViewById(R.id.timeEnd);
        repeatEditLayout = (LinearLayout)view.findViewById(R.id.repeatNumberBox);
        repeatEditText = (EditText)view.findViewById(R.id.repeatEditText);
        titleEditText = (EditText)view.findViewById(R.id.title);
        selectionGuestBox = (LinearLayout) view.findViewById(R.id.selectionGuestBox);
        sendBox = (LinearLayout) view.findViewById(R.id.sendBox);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);
        recyclerView = (RecyclerView) view.findViewById(R.id.guestRow);
        addPersonButton = (ImageView) view.findViewById(R.id.addGuestButton);

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

        repeatEditLayout.setVisibility(View.GONE);
        selectionGuestBox.setVisibility(View.GONE);

        spinner = (MaterialSpinner) view.findViewById(R.id.repeatPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                repeat_type = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if(position != 0)
                    repeatEditLayout.setVisibility(View.VISIBLE);
                else
                    repeatEditLayout.setVisibility(View.GONE);
            }
        });

        sendPicker = (MaterialSpinner) view.findViewById(R.id.sendPicker);
        sendPicker.setItems(getResources().getStringArray(R.array.array_who_will_be_signalized));
        sendPicker.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                send_toAll = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sendPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if(position != 0) {
                    selectionGuestBox.setVisibility(View.VISIBLE);
                }
                else {
                    selectionGuestBox.setVisibility(View.GONE);
                }
            }
        });

        dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        timeEnd.setOnClickListener(this);
        addPersonButton.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        getUser(email);
        addPersonButton.setActivated(false);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);

    }

    public void setSelectionSendBox(boolean type) {
        if(!type){
            if(selectionGuestBox.getVisibility() == View.GONE)
                sendBox.setVisibility(View.GONE);
            else{
                selectionGuestBox.setVisibility(View.GONE);
                sendBox.setVisibility(View.GONE);
            }
        }else {
            if(send_toAll == 0)
                sendBox.setVisibility(View.VISIBLE);
            else{
                selectionGuestBox.setVisibility(View.VISIBLE);
                sendBox.setVisibility(View.VISIBLE);
            }
        }

    }

    private void getUser(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfile(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(User user) {
        FlagActivity flagActivity = (FlagActivity)getActivity();
        if(!isEdit) {
            data = new ArrayList<>();
            user.setDelete(false);
            data.add(user);
            User friend = flagActivity.getUserFriend();
            if(friend != null) {
                friend.setDelete(false);
                data.add(friend);
            }
            adapter = new PersonAdapter(data, getActivity());
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(data.size()));
            addPersonButton.setActivated(true);
        }
    }

    private void handleError(Throwable error) {
        Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                FlagActivity flagActivity = (FlagActivity) getActivity();
                flagActivity.refreshItems();
            }else if (requestCode == ADD_GUEST) {
                if(!isEdit) {
                    ArrayList<User> list = new ArrayList<>();
                    PersonModelWrapper wrap =
                            (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                    list.add(data.get(0));
                    list.addAll(wrap.getItemDetails());
                    for(int i = 0; i<list.size();i++){
                        User usr = list.get(i);
                        usr.setDelete(false);
                    }
                    adapter.swap(list);
                    guestsNumber.setText(String.valueOf(list.size()));
                }else {
                    FlagActivity flagActivity = (FlagActivity) getActivity();
                    ActivityServer activityServer = new ActivityServer();
                    SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    PersonModelWrapper wrap =
                            (PersonModelWrapper) intent.getSerializableExtra("guest_objs");


                    if(wrap.getItemDetails().size() > 0) {
                        activityServer.setId(flagActivity.getFlag().getId());
                        activityServer.setVisibility(Constants.FLAG);
                        activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                        for (int i = 0; i < wrap.getItemDetails().size(); i++)
                            activityServer.addGuest(wrap.getItemDetails().get(i).getEmail());

                        flagActivity.addGuestToFlag(activityServer);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog2");
        if(dpd != null) dpd.setOnDateSetListener(this);

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        if(tpd != null) tpd.setOnTimeSetListener(this);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth,int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,monthOfYear,dayOfMonth);
        String day= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        calendar.set(yearEnd,monthOfYearEnd,dayOfMonthEnd);
        String day2= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month2= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day+"/"+month+"/"+year;
        String date2 = day2+"/"+month2+"/"+yearEnd;


        if(validadeDate(year, monthOfYear, dayOfMonth, yearEnd, monthOfYearEnd, dayOfMonthEnd))
        {
            day_start = dayOfMonth;
            month_start = monthOfYear;
            year_start = year;
            day_end = dayOfMonthEnd;
            month_end = monthOfYearEnd;
            year_end = yearEnd;
            dateStart.setText(date);
            dateEnd.setText(date2);
        }
        else{
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

    private boolean validadeDate(int year, int monthOfYear, int dayOfMonth,int yearEnd, int monthOfYearEnd, int dayOfMonthEnd){
        if(yearEnd < year)
            return false;
        if(year == yearEnd){
            if(monthOfYearEnd < monthOfYear)
                return false;
            else if(monthOfYearEnd == monthOfYear){
                if(dayOfMonthEnd < dayOfMonth)
                    return false;
            }
        }

        return true;
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        String hourString = String.format("%02d", hourOfDay);;
        String minuteString = String.format("%02d", minute);;
        String hourStringEnd = String.format("%02d", hourOfDayEnd);;
        String minuteStringEnd = String.format("%02d", minuteEnd);;
        String time = hourString+":"+minuteString;
        String time2 = hourStringEnd+":"+minuteStringEnd;

        if(day_start == -1)
            Toast.makeText(getActivity(), "Preencha primeiro a data!", Toast.LENGTH_LONG).show();
        else if(validadeHour(hourOfDay, minute, hourOfDayEnd, minuteEnd))
        {
            minutes_start = minute;
            hour_start = hourOfDay;
            minutes_end = minuteEnd;
            hour_end = hourOfDayEnd;
            timeStart.setText(time);
            timeEnd.setText(time2);
        }
        else {
            minutes_start = minute;
            hour_start = hourOfDay;
            minutes_end = minute;
            hour_end = hourOfDay;
            timeStart.setText(time);
            timeEnd.setText(time);
        }

    }

    private boolean validadeHour(int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd){
        if(sameDay() && day_start != -1){
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

    private boolean sameDay(){
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

    public String getTitleFromView() {
        return titleEditText.getText().toString();
    }

    public int getSendToAll() {
        return send_toAll;
    }

    public List<User> getGuestFromView() {
        return data;
    }

    public List<Integer> getRepeatFromView() {
        List<Integer> list = new ArrayList<>();
        String temp = repeatEditText.getText().toString();
        if(repeat_type == 0)
            repeat_qty = -1;
        else if(temp.length() > 0)
            repeat_qty = Integer.parseInt(temp);

        list.add(repeat_type);
        list.add(repeat_qty);
        return list;
    }

    @Override
    public void onClick(View v) {
        if(v == timeStart){
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    FlagEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start, hour_end, minutes_end);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(),R.color.deep_purple_400), ContextCompat.getColor(getActivity(),R.color.grey_100));
            tpd.setStartTitle(getResources().getString(R.string.start));
            tpd.setEndTitle(getResources().getString(R.string.end));
            tpd.setCurrentTab(0);
            tpd.show(getFragmentManager(), "Timepickerdialog");
        }else if(v == timeEnd){
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    FlagEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            if(hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start, hour_end, minutes_end);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeEnd" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(),R.color.deep_purple_400), ContextCompat.getColor(getActivity(),R.color.grey_100));
            tpd.setStartTitle(getResources().getString(R.string.start));
            tpd.setEndTitle(getResources().getString(R.string.end));
            tpd.setCurrentTab(1);
            tpd.show(getFragmentManager(), "Timepickerdialog");
        }else if(v == dateStart){
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FlagEditFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(year_start != -1)
                dpd.setStartDate(year_start, month_start, day_start, year_end, month_end, day_end);

            Calendar week_ago = Calendar.getInstance();
            week_ago.add(Calendar.DATE, -7);
            dpd.setMinDate(week_ago);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(),R.color.deep_purple_400), ContextCompat.getColor(getActivity(),R.color.grey_100));
            dpd.setStartTitle(getResources().getString(R.string.start));
            dpd.setEndTitle(getResources().getString(R.string.end));
            dpd.setCurrentTab(0);
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        }else if(v == dateEnd){
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FlagEditFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateEnd" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(year_start != -1)
                dpd.setStartDate(year_start, month_start, day_start, year_end, month_end, day_end);

            Calendar week_ago = Calendar.getInstance();
            week_ago.add(Calendar.DATE, -7);
            dpd.setMinDate(week_ago);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(),R.color.deep_purple_400), ContextCompat.getColor(getActivity(),R.color.grey_100));
            dpd.setStartTitle(getResources().getString(R.string.start));
            dpd.setEndTitle(getResources().getString(R.string.end));
            dpd.setCurrentTab(1);
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if(v == addPersonButton && addPersonButton.isActivated()){
            Intent intent = new Intent(getActivity(), SelectPeopleActivity.class);
            ArrayList<String> list = new ArrayList<>();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(!isEdit) {
                for (int i = 0; i < data.size(); i++) {
                    list.add(data.get(i).getEmail());
                }
            }else {
                for (int i = 0; i < listPerson.size(); i++) {
                    list.add(listPerson.get(i).getEmail());
                }
                intent.putExtra("erase_from_list", true);
            }

            intent.putStringArrayListExtra("guest_list", list);

            startActivityForResult(intent, ADD_GUEST);
        }
    }

    public void setLayout(FlagServer flagServer, ArrayList<User> users, ArrayList<User> confirmed,  boolean edit, boolean free, boolean friend){
        Calendar calendar = Calendar.getInstance();
        calendar.set(flagServer.getYearStart(),flagServer.getMonthStart()-1,flagServer.getDayStart());

        isEdit = edit;

        String day= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        calendar.set(flagServer.getYearEnd(),flagServer.getMonthEnd()-1,flagServer.getDayEnd());
        String day2= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month2= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day+"/"+month+"/"+flagServer.getYearStart();
        String date2 = day2+"/"+month2+"/"+flagServer.getYearEnd();

        String hourString = String.format("%02d", flagServer.getHourStart());;
        String minuteString = String.format("%02d", flagServer.getMinuteStart());;
        String hourStringEnd = String.format("%02d", flagServer.getHourEnd());;
        String minuteStringEnd = String.format("%02d", flagServer.getMinuteEnd());;
        String time = hourString+":"+minuteString;
        String time2 = hourStringEnd+":"+minuteStringEnd;

        minutes_start = flagServer.getMinuteStart();
        hour_start = flagServer.getHourStart();
        minutes_end = flagServer.getMinuteEnd();
        hour_end = flagServer.getHourEnd();

        day_start = flagServer.getDayStart();
        month_start = flagServer.getMonthStart() - 1;
        year_start = flagServer.getYearStart();
        day_end = flagServer.getDayEnd();
        month_end = flagServer.getMonthEnd() - 1;
        year_end = flagServer.getYearEnd();

        dateStart.setText(date);
        dateEnd.setText(date2);
        timeStart.setText(time);
        timeEnd.setText(time2);
        titleEditText.setText(flagServer.getTitle());

        if (!isFlagInPast(flagServer)) {
            addPersonButton.setImageResource(R.drawable.btn_add_person);
            addPersonButton.setOnClickListener(this);
        } else {
            addPersonButton.setOnClickListener(null);
            addPersonButton.setVisibility(View.GONE);
        }

        if(edit) {
            dateStart.setOnClickListener(null);
            dateEnd.setOnClickListener(null);
            timeStart.setOnClickListener(null);
            timeEnd.setOnClickListener(null);

            repeatBox.setVisibility(View.GONE);

            sendBox.setVisibility(View.GONE);

            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, MotionEvent e) {
                    FlagActivity flagActivity = (FlagActivity) getActivity();

                    Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
                    intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
                    intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                    intent.putExtra("is_adm", false);
                    intent.putExtra("id_act", flagActivity.getFlag().getId());
                    intent.putExtra("is_flag", true);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    startActivityForResult(intent, GUEST_UPDATE);
                }

                @Override
                public void onLongItemClick(View view, int position, MotionEvent e) {
                }
            }));

            if(flagServer.getType()) {
                selectionGuestBox.setVisibility(View.VISIBLE);

                listPerson.clear();
                listConfirmed.clear();
                listConfirmed.addAll(confirmed);

                for (int i = 0; i < users.size(); i++) {
                    User usr = users.get(i);
                    usr.setDelete(false);
                    listPerson.add(usr);
                }

                adapter = new PersonAdapter(listPerson, getActivity());
                recyclerView.setAdapter(adapter);
                guestsNumber.setText(String.valueOf(listPerson.size()));
                addPersonButton.setActivated(true);
            }else
                selectionGuestBox.setVisibility(View.GONE);

        }else if(!free){
            sendPicker.setSelectedIndex(1);
            send_toAll = 1;
            selectionGuestBox.setVisibility(View.VISIBLE);

            for(int i = 0; i<users.size();i++){
                User usr = users.get(i);
                usr.setDelete(false);
                listPerson.add(usr);
            }

            adapter = new PersonAdapter(listPerson, getActivity());
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(listPerson.size()));

            if (flagServer.getRepeatType() == 0)
                spinner.setSelectedIndex(flagServer.getRepeatType());
            else {
                spinner.setSelectedIndex(flagServer.getRepeatType());
                repeatEditLayout.setVisibility(View.VISIBLE);
                repeatEditText.setText(String.valueOf(flagServer.getRepeatQty()));
            }
        }else if(friend){
            sendPicker.setSelectedIndex(1);
            send_toAll = 1;
            selectionGuestBox.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFlagInPast(FlagServer flagServer){
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        boolean isHourBefore = isTimeInBefore(hour + ":" + minute, flagServer.getHourEnd() + ":" + flagServer.getMinuteEnd());
        boolean isDateBefore = isDateInBefore(flagServer.getYearEnd(), flagServer.getMonthEnd(), flagServer.getDayEnd(), year, month, day);

        return (isHourBefore && isDateBefore) || isDateBefore;
    }

    private boolean isDateInBefore(int year, int monthOfYear, int dayOfMonth,int yearEnd, int monthOfYearEnd, int dayOfMonthEnd){
        if(yearEnd < year)
            return false;
        if(year == yearEnd){
            if(monthOfYearEnd < monthOfYear)
                return false;
            else if(monthOfYearEnd == monthOfYear){
                if(dayOfMonthEnd < dayOfMonth)
                    return false;
            }
        }

        return true;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSubscriptions != null)
            mSubscriptions.unsubscribe();
    }
}
