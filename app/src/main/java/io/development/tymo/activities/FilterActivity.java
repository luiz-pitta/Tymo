package io.development.tymo.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.aakira.expandablelayout.Utils;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.adapters.SelectionWeekDaysAdapter;
import io.development.tymo.model_server.FilterServer;
import io.development.tymo.model_server.FilterWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.UpdateButtonController;
import io.development.tymo.utils.Utilities;
import io.development.tymo.R;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class FilterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private UpdateButtonController controller;
    private FilterWrapper filterWrapper;

    private View topHorizontalLineInterests;
    private View topHorizontalLineFriends;
    private View topHorizontalLineLocal;
    private View topHorizontalLineDate;
    private View topHorizontalLineSchedule;

    private TextView cleanButton, applyButton;

    private TextView proximityText;
    private TextView popularText;
    private TextView dateTimeText;

    private ImageView proximityButton;
    private ImageView popularButton;
    private ImageView dateTimeButton;

    private View proximityCorners;
    private View popularityCorners;
    private View dateHourCorners;

    private FirebaseAnalytics mFirebaseAnalytics;

    private RelativeLayout itemBoxInterests;
    private TextView cleanInterests;
    private ImageView filterIconInterests, expandMoreIconInterests;
    private RelativeLayout addTagBox;
    private ExpandableLinearLayout expandableLayoutInterests;
    private TagView tagGroup;
    private OnTagDeleteListener mOnTagDeleteListener = new OnTagDeleteListener() {

        @Override
        public void onTagDeleted(final TagView view, final Tag tag, final int position) {
            view.remove(position);
            if(tagGroup.getTags().size() == 0) {
                filterIconInterests.setVisibility(View.INVISIBLE);
                cleanInterests.setVisibility(View.INVISIBLE);
            }
        }
    };

    private RelativeLayout itemBoxFriends;
    private ImageView filterIconFriends, expandMoreIconFriends;
    private ExpandableLinearLayout expandableLayoutFriends;
    private TextView cleanFriends;
    private ImageView addPersonButton;
    private RecyclerView recyclerView;
    private List<User> listPerson = new ArrayList<>();
    private PersonAdapter adapter;

    private RelativeLayout itemBoxLocation;
    private TextView cleanLocation;
    private boolean locationNotWorking = false;
    private ImageView filterIconLocation, expandMoreIconLocation;
    private ExpandableLinearLayout expandableLayoutLocation;
    private TextView locationText, mapText;
    private double lat = -500;
    private double lng = -500;
    private static final int PLACE_PICKER_REQUEST = 1020;
    private PlacePicker.IntentBuilder builder;
    private Intent placePicker;

    private RelativeLayout itemBoxDate;
    private TextView cleanDate;
    private ImageView filterIconDate, expandMoreIconDate;
    private ExpandableLinearLayout expandableLayoutDate;
    private TextView dateStart, dateEnd;
    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;

    private RelativeLayout itemBoxSchedule;
    private TextView cleanSchedule;
    private ImageView filterIconSchedule, expandMoreIconSchedule;
    private ExpandableLinearLayout expandableLayoutSchedule;
    private TextView timeStart, timeEnd;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;

    private RelativeLayout itemBoxWeekDays;
    private TextView cleanWeekDays;
    private ImageView filterIconWeekDays, expandMoreIconWeekDays;
    private ExpandableLinearLayout expandableLayoutWeekDays;
    private RecyclerView multichoiceRecyclerviewWeekDays;
    private SelectionWeekDaysAdapter selectionWeekDaysAdapter;

    private ImageView mBackButton;
    private TextView m_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        cleanButton = (TextView)findViewById(R.id.cleanButton);
        applyButton = (TextView)findViewById(R.id.applyButton);

        proximityText = (TextView)findViewById(R.id.proximityText);
        popularText = (TextView)findViewById(R.id.popularityText);
        dateTimeText = (TextView)findViewById(R.id.dateHourText);

        proximityCorners = findViewById(R.id.proximityCorners);
        popularityCorners = findViewById(R.id.popularityCorners);
        dateHourCorners = findViewById(R.id.dateHourCorners);

        proximityButton = (ImageView)findViewById(R.id.proximityIcon);
        popularButton = (ImageView)findViewById(R.id.popularityIcon);
        dateTimeButton = (ImageView)findViewById(R.id.dateHourIcon);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);

        topHorizontalLineInterests = findViewById(R.id.topHorizontalLineInterests);
        topHorizontalLineFriends = findViewById(R.id.topHorizontalLineFriends);
        topHorizontalLineLocal = findViewById(R.id.topHorizontalLineLocal);
        topHorizontalLineDate = findViewById(R.id.topHorizontalLineDate);
        topHorizontalLineSchedule = findViewById(R.id.topHorizontalLineSchedule);

        proximityText.setOnClickListener(this);
        popularText.setOnClickListener(this);
        dateTimeText.setOnClickListener(this);
        proximityButton.setOnClickListener(this);
        popularButton.setOnClickListener(this);
        dateTimeButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        applyButton.setOnClickListener(this);
        cleanButton.setOnClickListener(this);

        //set button controller
        controller = new UpdateButtonController(this);
        controller.attach(false, proximityText, proximityButton,proximityCorners);
        controller.attach(false, popularText, popularButton, popularityCorners);
        controller.attach(false, dateTimeText, dateTimeButton,dateHourCorners);

        controller.setMultiple(true);

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

        m_title.setText(getResources().getString(R.string.filter_and_sort));

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        filterWrapper = (FilterWrapper)getIntent().getSerializableExtra("filter_load");

        setFilterExpandableLayout();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
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
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String time = hourString + ":" + minuteString;

        minutes_start = minute;
        hour_start = hourOfDay;
        timeStart.setText(time);

        filterIconSchedule.setVisibility(View.VISIBLE);
        cleanSchedule.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,monthOfYear,dayOfMonth);
        String d= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String m= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        calendar.set(yearEnd,monthOfYearEnd,dayOfMonthEnd);
        String d2= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String m2= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = d+"/"+m+"/"+year;
        String date2 = d2+"/"+m2+"/"+yearEnd;


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

        filterIconDate.setVisibility(View.VISIBLE);
        cleanDate.setVisibility(View.VISIBLE);

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setFilterExpandableLayout(){

        setFilterExpandableLayoutInterests();

        setFilterExpandableLayoutFriends();

        setFilterExpandableLayoutLocation();

        setFilterExpandableLayoutDate();

        setFilterExpandableLayoutSchedule();

        setFilterExpandableLayoutWeekDays();

        if(filterWrapper != null) {
            FilterServer filterServer = filterWrapper.getFilterServer();

            boolean location = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.LOCATION, false);

            if(filterServer.getProximity() && location)
                controller.updateAll(0, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);

            if(filterServer.getPopularity())
                controller.updateAll(1, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);

            if(filterServer.getDateHour())
                controller.updateAll(2, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
        }

    }

    private void setFilterExpandableLayoutWeekDays(){
        //WeekDays
        itemBoxWeekDays = (RelativeLayout) findViewById(R.id.itemBoxWeekDays);
        cleanWeekDays = (TextView) findViewById(R.id.cleanWeekDays);
        filterIconWeekDays = (ImageView) findViewById(R.id.filterIconWeekDays);
        expandMoreIconWeekDays = (ImageView) findViewById(R.id.expandMoreIconWeekDays);
        expandableLayoutWeekDays = (ExpandableLinearLayout)findViewById(R.id.expandableLayoutWeekDays);
        multichoiceRecyclerviewWeekDays = (RecyclerView) findViewById(R.id.multichoiceRecyclerviewWeekDays);

        filterIconWeekDays.setVisibility(View.INVISIBLE);
        cleanWeekDays.setVisibility(View.INVISIBLE);
        expandableLayoutWeekDays.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR));
        expandableLayoutWeekDays.setExpanded(false);
        expandableLayoutWeekDays.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(expandMoreIconWeekDays, 0f, 180f).start();
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(expandMoreIconWeekDays, 180f, 0f).start();
            }
        });

        itemBoxWeekDays.setOnClickListener(this);
        cleanWeekDays.setOnClickListener(this);

        selectionWeekDaysAdapter = new SelectionWeekDaysAdapter(getDaysWeek(), this) ;
        multichoiceRecyclerviewWeekDays.setAdapter(selectionWeekDaysAdapter);
        multichoiceRecyclerviewWeekDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectionWeekDaysAdapter.setSingleClickMode(true);

        selectionWeekDaysAdapter.setMultiChoiceSelectionListener(new MultiChoiceAdapter.Listener() {
            @Override
            public void OnItemSelected(int selectedPosition, int itemSelectedCount, int allItemCount) {
                filterIconWeekDays.setVisibility(View.VISIBLE);
                cleanWeekDays.setVisibility(View.VISIBLE);
            }

            @Override
            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
                if(selectionWeekDaysAdapter.getSelectedItemList().size() == 0){
                    filterIconWeekDays.setVisibility(View.INVISIBLE);
                    cleanWeekDays.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void OnSelectAll(int itemSelectedCount, int allItemCount) {

            }

            @Override
            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {

            }
        });

        if(filterWrapper != null){
            FilterServer filterServer = filterWrapper.getFilterServer();
            for(int i = 0; i < filterServer.getWeekDays().size(); i++) {
                selectionWeekDaysAdapter.select(filterServer.getWeekDays().get(i));

            }

            if(filterServer.getWeekDays().size() > 0){
                filterIconWeekDays.setVisibility(View.VISIBLE);
                cleanWeekDays.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setFilterExpandableLayoutSchedule(){
        //Schedule
        itemBoxSchedule = (RelativeLayout) findViewById(R.id.itemBoxSchedule);
        cleanSchedule = (TextView) findViewById(R.id.cleanSchedule);
        filterIconSchedule = (ImageView) findViewById(R.id.filterIconSchedule);
        expandMoreIconSchedule = (ImageView) findViewById(R.id.expandMoreIconSchedule);
        expandableLayoutSchedule = (ExpandableLinearLayout)findViewById(R.id.expandableLayoutSchedule);
        timeStart = (TextView) findViewById(R.id.timeStart);
        timeEnd = (TextView) findViewById(R.id.timeEnd);

        filterIconSchedule.setVisibility(View.INVISIBLE);
        cleanSchedule.setVisibility(View.INVISIBLE);
        expandableLayoutSchedule.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR));
        expandableLayoutSchedule.setExpanded(false);
        expandableLayoutSchedule.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(expandMoreIconSchedule, 0f, 180f).start();
                topHorizontalLineSchedule.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(expandMoreIconSchedule, 180f, 0f).start();
                topHorizontalLineSchedule.setVisibility(View.GONE);
            }
        });

        itemBoxSchedule.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        timeEnd.setOnClickListener(this);
        cleanSchedule.setOnClickListener(this);

        if(filterWrapper != null){
            FilterServer filterServer = filterWrapper.getFilterServer();

            if(filterServer.getHourStart() != -1) {
                String hourString = String.format("%02d", filterServer.getHourStart());
                String minuteString = String.format("%02d", filterServer.getMinuteStart());
                String hourStringEnd = String.format("%02d", filterServer.getHourEnd());
                String minuteStringEnd = String.format("%02d", filterServer.getMinuteEnd());
                String time = hourString + ":" + minuteString;
                String time2 = hourStringEnd + ":" + minuteStringEnd;

                minutes_start = filterServer.getMinuteStart();
                hour_start = filterServer.getHourStart();
                minutes_end = filterServer.getMinuteEnd();
                hour_end = filterServer.getHourEnd();
                timeStart.setText(time);
                timeEnd.setText(time2);

                filterIconSchedule.setVisibility(View.VISIBLE);
                cleanSchedule.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setFilterExpandableLayoutDate(){
        //Date
        itemBoxDate = (RelativeLayout) findViewById(R.id.itemBoxDate);
        cleanDate = (TextView) findViewById(R.id.cleanDate);
        filterIconDate = (ImageView) findViewById(R.id.filterIconDate);
        expandMoreIconDate = (ImageView) findViewById(R.id.expandMoreIconDate);
        expandableLayoutDate = (ExpandableLinearLayout)findViewById(R.id.expandableLayoutDate);
        dateStart = (TextView) findViewById(R.id.dateStart);
        dateEnd = (TextView) findViewById(R.id.dateEnd);

        filterIconDate.setVisibility(View.INVISIBLE);
        cleanDate.setVisibility(View.INVISIBLE);
        expandableLayoutDate.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR));
        expandableLayoutDate.setExpanded(false);
        expandableLayoutDate.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(expandMoreIconDate, 0f, 180f).start();
                topHorizontalLineDate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(expandMoreIconDate, 180f, 0f).start();
                topHorizontalLineDate.setVisibility(View.GONE);
            }
        });

        itemBoxDate.setOnClickListener(this);
        dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        cleanDate.setOnClickListener(this);

        if(filterWrapper != null){
            FilterServer filterServer = filterWrapper.getFilterServer();

            if(filterServer.getDayStart() != -1) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(filterServer.getYearStart(),filterServer.getMonthStart()-1,filterServer.getDayStart());
                String d= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                String m= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                calendar.set(filterServer.getYearEnd(),filterServer.getMonthEnd()-1,filterServer.getDayEnd());
                String d2= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                String m2= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                String date = d+"/"+m+"/"+filterServer.getYearStart();
                String date2 = d2+"/"+m2+"/"+filterServer.getYearEnd();

                day_start = filterServer.getDayStart();
                month_start = filterServer.getMonthStart()-1;
                year_start = filterServer.getYearStart();
                day_end = filterServer.getDayEnd();
                month_end = filterServer.getMonthEnd()-1;
                year_end = filterServer.getYearEnd();
                dateStart.setText(date);
                dateEnd.setText(date2);

                filterIconDate.setVisibility(View.VISIBLE);
                cleanDate.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setFilterExpandableLayoutLocation(){
        //Location
        itemBoxLocation = (RelativeLayout) findViewById(R.id.itemBoxLocation);
        cleanLocation = (TextView) findViewById(R.id.cleanLocation);
        filterIconLocation = (ImageView) findViewById(R.id.filterIconLocation);
        expandMoreIconLocation = (ImageView) findViewById(R.id.expandMoreIconLocation);
        expandableLayoutLocation = (ExpandableLinearLayout)findViewById(R.id.expandableLayoutLocation);
        locationText = (TextView) findViewById(R.id.locationText);
        mapText = (TextView) findViewById(R.id.mapText);

        filterIconLocation.setVisibility(View.INVISIBLE);
        cleanLocation.setVisibility(View.INVISIBLE);
        expandableLayoutLocation.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR));
        expandableLayoutLocation.setExpanded(false);
        expandableLayoutLocation.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(expandMoreIconLocation, 0f, 180f).start();
                topHorizontalLineLocal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(expandMoreIconLocation, 180f, 0f).start();
                topHorizontalLineLocal.setVisibility(View.GONE);
            }
        });

        builder = new PlacePicker.IntentBuilder();

        itemBoxLocation.setOnClickListener(this);
        mapText.setOnClickListener(this);
        locationText.setOnClickListener(this);
        cleanLocation.setOnClickListener(this);

        if(filterWrapper != null){
            FilterServer filterServer = filterWrapper.getFilterServer();

            if(filterServer.getLat() != -500) {
                lat = filterServer.getLat();
                lng = filterServer.getLng();
                locationText.setText(filterServer.getLocation());
                filterIconLocation.setVisibility(View.VISIBLE);
                cleanLocation.setVisibility(View.VISIBLE);
                LatLng latLng = new LatLng(lat, lng);
                LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);
            }
        }else
            builder = new PlacePicker.IntentBuilder();

        try {
            placePicker = builder.build(this);
        } catch (Exception e) {
            locationNotWorking = true;
        }
    }

    private void setFilterExpandableLayoutFriends(){
        //Friends
        itemBoxFriends = (RelativeLayout) findViewById(R.id.itemBoxFriends);
        cleanFriends = (TextView) findViewById(R.id.cleanFriends);
        filterIconFriends = (ImageView) findViewById(R.id.filterIconFriends);
        expandMoreIconFriends = (ImageView) findViewById(R.id.expandMoreIconFriends);
        expandableLayoutFriends = (ExpandableLinearLayout)findViewById(R.id.expandableLayoutFriends);
        recyclerView = (RecyclerView) findViewById(R.id.guestRow);
        addPersonButton = (ImageView) findViewById(R.id.addGuestButton);

        filterIconFriends.setVisibility(View.INVISIBLE);
        cleanFriends.setVisibility(View.INVISIBLE);
        expandableLayoutFriends.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR));
        expandableLayoutFriends.setExpanded(false);
        expandableLayoutFriends.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(expandMoreIconFriends, 0f, 180f).start();
                topHorizontalLineFriends.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(expandMoreIconFriends, 180f, 0f).start();
                topHorizontalLineFriends.setVisibility(View.GONE);
            }
        });

        itemBoxFriends.setOnClickListener(this);
        addPersonButton.setOnClickListener(this);
        cleanFriends.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {
                ImageView delete = (ImageView) view.findViewById(R.id.deleteButton);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "listPersonDeleteButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if(delete != null && delete.getVisibility() == View.VISIBLE && isPointInsideView(e.getX(), e.getY(), view)) {
                    listPerson.remove(position);
                    adapter.notifyItemRemoved(position);
                    if(listPerson.size() == 0) {
                        filterIconFriends.setVisibility(View.INVISIBLE);
                        cleanFriends.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        }));

        listPerson = new ArrayList<>();
        adapter = new PersonAdapter(listPerson, this);
        recyclerView.setAdapter(adapter);

        if(filterWrapper != null){
            FilterServer filterServer = filterWrapper.getFilterServer();
            if(filterServer.getFriends().size() > 0) {
                adapter.swap(filterServer.getFriends());
                filterIconFriends.setVisibility(View.VISIBLE);
                cleanFriends.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setFilterExpandableLayoutInterests(){
        //Interests
        itemBoxInterests  = (RelativeLayout) findViewById(R.id.itemBoxInterests);
        cleanInterests = (TextView) findViewById(R.id.cleanInterests);
        filterIconInterests  = (ImageView) findViewById(R.id.filterIconInterests);
        expandMoreIconInterests = (ImageView) findViewById(R.id.expandMoreIconInterests);
        expandableLayoutInterests = (ExpandableLinearLayout)findViewById(R.id.expandableLayoutInterests);
        tagGroup = (TagView) findViewById(R.id.tag_group);
        addTagBox = (RelativeLayout) findViewById(R.id.addTagBox);

        tagGroup.setOnTagDeleteListener(mOnTagDeleteListener);

        filterIconInterests.setVisibility(View.INVISIBLE);
        cleanInterests.setVisibility(View.INVISIBLE);
        expandableLayoutInterests.setInterpolator(Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR));
        expandableLayoutInterests.setExpanded(false);
        expandableLayoutInterests.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(expandMoreIconInterests, 0f, 180f).start();
                topHorizontalLineInterests.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(expandMoreIconInterests, 180f, 0f).start();
                topHorizontalLineInterests.setVisibility(View.GONE);
            }
        });

        itemBoxInterests.setOnClickListener(this);
        addTagBox.setOnClickListener(this);
        cleanInterests.setOnClickListener(this);

        if(filterWrapper != null){
            FilterServer filterServer = filterWrapper.getFilterServer();
            tagGroup.removeAll();

            Collections.sort(filterServer.getTags(), new Comparator<String>() {
                @Override
                public int compare(String c1, String c2) {
                    if (c1.compareTo(c2) > 0)
                        return 1;
                    else if (c1.compareTo(c2) < 0)
                        return -1;
                    else
                        return 0;
                }
            });

            for (int i=0;i<filterServer.getTags().size();i++){
                Tag tag;
                tag = new Tag(filterServer.getTags().get(i));
                tag.radius = Utilities.convertDpToPixel(10.0f, this);
                tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
                tag.isDeletable = true;
                tagGroup.addTag(tag);
            }
            if(filterServer.getTags().size() > 0) {
                filterIconInterests.setVisibility(View.VISIBLE);
                cleanInterests.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isPointInsideView(float x, float y, View view){
        ViewGroup childView = (ViewGroup)view;
        ImageView cardBox = (ImageView) childView.findViewById(R.id.profilePhoto);
        int locationCard[] = new int[2];
        cardBox.getLocationOnScreen(locationCard);
        int cardBoxX = (int)Utilities.convertPixelsToDp(locationCard[0],this);
        float viewX = Utilities.convertPixelsToDp(x,this)+95-cardBoxX;
        float viewY = Utilities.convertPixelsToDp(y,this);

        return (viewX>=40 && viewY<=25);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        ArrayList<User> list = new ArrayList<>();
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(this,data);
                String name = selectedPlace.getAddress().toString();
                lat = selectedPlace.getLatLng().latitude;
                lng = selectedPlace.getLatLng().longitude;
                locationText.setText(name);
                filterIconLocation.setVisibility(View.VISIBLE);
                cleanLocation.setVisibility(View.VISIBLE);
            }
        }else if (requestCode == 133) {
            if(resultCode == RESULT_OK){
                PersonModelWrapper wrap =
                        (PersonModelWrapper) data.getSerializableExtra("guest_objs");

                list.addAll(wrap.getItemDetails());
                adapter.swap(list);
                filterIconFriends.setVisibility(View.VISIBLE);
                cleanFriends.setVisibility(View.VISIBLE);
            }
        }else if (requestCode == 135) {
            if(resultCode == RESULT_OK){
                List<String> listTag = data.getStringArrayListExtra("tags_objs");

                Collections.sort(listTag, new Comparator<String>() {
                    @Override
                    public int compare(String c1, String c2) {
                        if (c1.compareTo(c2) > 0)
                            return 1;
                        else if (c1.compareTo(c2) < 0)
                            return -1;
                        else
                            return 0;
                    }
                });

                tagGroup.removeAll();
                for (int i=0;i<listTag.size();i++){
                    Tag tag;
                    tag = new Tag(listTag.get(i));
                    tag.radius = Utilities.convertDpToPixel(10.0f, this);
                    tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
                    tag.isDeletable = true;
                    tagGroup.addTag(tag);
                }
                filterIconInterests.setVisibility(View.VISIBLE);
                cleanInterests.setVisibility(View.VISIBLE);
            }
        }
    }

    private List<String> getDaysWeek(){
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(getResources().getStringArray(R.array.array_week_days_first_letter)));
        return list;
    }
    @Override
    public void onClick(View v){
        boolean location = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.LOCATION, false);
        if((v == mapText || v == locationText) && !locationNotWorking) {

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mapText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(lat != -500) {
                LatLng latLng = new LatLng(lat, lng);
                LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);

                try {
                    placePicker = builder.build(this);
                } catch (Exception e) {
                    locationNotWorking = true;
                }
            }

            startActivityForResult(placePicker, PLACE_PICKER_REQUEST);
        }
        else if((v == proximityText || v == proximityButton) ){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "proximityText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(location)
                controller.updateAll(0, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            else
                Toast.makeText(this, getResources().getString(R.string.filter_proximity_enable), Toast.LENGTH_LONG).show();
        }else if(v == popularText || v == popularButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "popularText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            controller.updateAll(1, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
        }else if(v == dateTimeText || v == dateTimeButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateTimeText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            controller.updateAll(2, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
        }else if(v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if(v == itemBoxInterests) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBoxInterests" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            expandableLayoutInterests.toggle();
        }
        else if(v == itemBoxFriends) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBoxFriends" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            ArrayList<User> list = new ArrayList<>();
            list.addAll(listPerson);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.swap(list);
                }
            }, 400);
            expandableLayoutFriends.toggle();
        }
        else if(v == itemBoxLocation) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBoxLocation" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            expandableLayoutLocation.toggle();
        }
        else if(v == itemBoxDate) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBoxDate" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            expandableLayoutDate.toggle();
        }
        else if(v == itemBoxSchedule) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBoxSchedule" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            expandableLayoutSchedule.toggle();
        }
        else if(v == itemBoxWeekDays) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBoxWeekDays" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            expandableLayoutWeekDays.toggle();
        }
        else if(v == timeStart){

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart"+"=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();
            if (hour_start != -1)
                now.set(year_start, month_start, day_start, hour_start, minutes_start);
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            tpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        }else if(v == dateStart){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FilterActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.setMinDate(now);
            if(year_start != -1)
                dpd.setStartDate(year_start, month_start, day_start, year_end, month_end, day_end);

            dpd.setAccentColor(ContextCompat.getColor(this,R.color.deep_purple_400), ContextCompat.getColor(this,R.color.grey_100));
            dpd.setStartTitle(getResources().getString(R.string.date_start));
            dpd.setEndTitle(getResources().getString(R.string.date_end));
            dpd.setCurrentTab(0);
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        }else if(v == dateEnd){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateEnd" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FilterActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.setMinDate(now);
            if(year_start != -1)
                dpd.setStartDate(year_start, month_start, day_start, year_end, month_end, day_end);

            dpd.setAccentColor(ContextCompat.getColor(this,R.color.deep_purple_400), ContextCompat.getColor(this,R.color.grey_100));
            dpd.setStartTitle(getResources().getString(R.string.date_start));
            dpd.setEndTitle(getResources().getString(R.string.date_end));
            dpd.setCurrentTab(1);
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        }else if(v == addPersonButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int i;
            ArrayList<String> list = new ArrayList<>();
            for(i = 0; i < listPerson.size(); i++){
                list.add(listPerson.get(i).getEmail());
            }
            Intent intent = new Intent(this, SelectPeopleActivity.class);
            intent.putStringArrayListExtra("guest_list", list);

            startActivityForResult(intent, 133);
        }else if(v == addTagBox){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addTagBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int i;
            ArrayList<String> list = new ArrayList<>();
            List<Tag> list_tags = tagGroup.getTags();
            for(i = 0; i < list_tags.size(); i++){
                list.add(list_tags.get(i).text);
            }
            Intent intent = new Intent(this, SelectTagsActivity.class);
            intent.putStringArrayListExtra("tags_list", list);
            startActivityForResult(intent, 135);
        }else if(v == cleanButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Interest
            tagGroup.removeAll();
            filterIconInterests.setVisibility(View.INVISIBLE);
            cleanInterests.setVisibility(View.INVISIBLE);
            //Friends
            adapter.clearData();
            listPerson.clear();
            filterIconFriends.setVisibility(View.INVISIBLE);
            cleanFriends.setVisibility(View.INVISIBLE);
            //Location
            locationText.setText(getResources().getString(R.string.filter_local));
            lat = -500;
            lng = -500;
            filterIconLocation.setVisibility(View.INVISIBLE);
            cleanLocation.setVisibility(View.INVISIBLE);
            //Date
            filterIconDate.setVisibility(View.INVISIBLE);
            cleanDate.setVisibility(View.INVISIBLE);
            dateStart.setText(getResources().getString(R.string.hint_date));
            dateEnd.setText(getResources().getString(R.string.hint_date));
            day_start = -1;
            month_start = -1;
            year_start = -1;
            day_end = -1;
            month_end = -1;
            year_end = -1;
            //Schedule
            filterIconSchedule.setVisibility(View.INVISIBLE);
            cleanSchedule.setVisibility(View.INVISIBLE);
            timeStart.setText(getResources().getString(R.string.hint_time));
            timeEnd.setText(getResources().getString(R.string.hint_time));
            minutes_start = -1;
            minutes_end = -1;
            hour_start = -1;
            hour_end = -1;
            //WeekDays
            filterIconWeekDays.setVisibility(View.INVISIBLE);
            cleanWeekDays.setVisibility(View.INVISIBLE);
            selectionWeekDaysAdapter.deselectAll();
            //buttons
            controller.unselectAll();
        }else if(v == applyButton){

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "applyButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<Boolean> prox = controller.getSelected();

            if(!prox.get(0) || (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && location && prox.get(0)))
                setFilterServer();
            else if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                Utilities.buildAlertMessageNoGps(this);
            else if(!location)
                Toast.makeText(this, getResources().getString(R.string.filter_proximity_enable), Toast.LENGTH_LONG).show();

        }else if(v == cleanInterests){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanInterests" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Interest
            tagGroup.removeAll();
            filterIconInterests.setVisibility(View.INVISIBLE);
            cleanInterests.setVisibility(View.INVISIBLE);
        }else if(v == cleanFriends){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanFriends" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Friends
            adapter.clearData();
            listPerson.clear();
            filterIconFriends.setVisibility(View.INVISIBLE);
            cleanFriends.setVisibility(View.INVISIBLE);
        }else if(v == cleanLocation){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanLocation" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Location
            locationText.setText(getResources().getString(R.string.filter_local));
            lat = -500;
            lng = -500;
            filterIconLocation.setVisibility(View.INVISIBLE);
            cleanLocation.setVisibility(View.INVISIBLE);
        }else if(v == cleanDate){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanDate" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Date
            filterIconDate.setVisibility(View.INVISIBLE);
            cleanDate.setVisibility(View.INVISIBLE);
            dateStart.setText(getResources().getString(R.string.hint_date));
            dateEnd.setText(getResources().getString(R.string.hint_date));
            day_start = -1;
            month_start = -1;
            year_start = -1;
            day_end = -1;
            month_end = -1;
            year_end = -1;
        }else if(v == cleanSchedule){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanSchedule" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Schedule
            filterIconSchedule.setVisibility(View.INVISIBLE);
            cleanSchedule.setVisibility(View.INVISIBLE);
            timeStart.setText(getResources().getString(R.string.hint_time));
            timeEnd.setText(getResources().getString(R.string.hint_time));
            minutes_start = -1;
            minutes_end = -1;
            hour_start = -1;
            hour_end = -1;
        }else if(v == cleanWeekDays){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanWeekDays" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //WeekDays
            filterIconWeekDays.setVisibility(View.INVISIBLE);
            cleanWeekDays.setVisibility(View.INVISIBLE);
            selectionWeekDaysAdapter.deselectAll();
        }
    }

    public boolean checkFilterAvailable() {
        return (!(  filterIconInterests.getVisibility() == View.INVISIBLE
                 && filterIconFriends.getVisibility()   == View.INVISIBLE
                 && filterIconLocation.getVisibility()  == View.INVISIBLE
                 && filterIconDate.getVisibility()      == View.INVISIBLE
                 && filterIconSchedule.getVisibility()  == View.INVISIBLE
                 && filterIconWeekDays.getVisibility()  == View.INVISIBLE
                 && controller.checkAllUnselected()));
    }

    private void setFilterServer(){
        FilterServer filterServer = new FilterServer();

        filterServer.setFilterFilled(checkFilterAvailable());
        //Interest
        if(tagGroup.getTags().size() > 0)
            filterServer.addTags(tagGroup.getTags());
        //Friends
        if(listPerson.size() > 0)
            filterServer.addFriends(listPerson);
        //Location
        filterServer.setLat(lat);
        filterServer.setLng(lng);
        filterServer.setLocation(locationText.getText().toString());
        //Date
        if(day_start == -1) {
            filterServer.setMonthStart(month_start);
            filterServer.setMonthEnd(month_end);
        }else {
            filterServer.setMonthStart(month_start+1);
            filterServer.setMonthEnd(month_end+1);
        }
        filterServer.setDayStart(day_start);
        filterServer.setYearStart(year_start);
        filterServer.setDayEnd(day_end);
        filterServer.setYearEnd(year_end);

        //Schedule
        filterServer.setMinuteStart(minutes_start);
        filterServer.setHourStart(hour_start);

        //WeekDays
        if(selectionWeekDaysAdapter.getSelectedItemList().size() > 0)
            filterServer.addWeekDays(selectionWeekDaysAdapter.getSelectedItemList());
        //buttons
        List<Boolean> list = controller.getSelected();
        filterServer.setProximity(list.get(0));
        filterServer.setPopularity(list.get(1));
        filterServer.setDateHour(list.get(2));

        filterServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        FilterWrapper filterWrapper = new FilterWrapper(filterServer);

        Intent intent = new Intent();
        intent.putExtra("filter_att", filterWrapper);
        setResult(RESULT_OK, intent);
        finish();
    }

    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }

}
