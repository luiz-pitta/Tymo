package io.development.tymo.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.adapters.PlansAdapter;
import io.development.tymo.models.WeekModel;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.adapters.CompareFragmentAdapter;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.fragments.CompareFreeFragment;
import io.development.tymo.fragments.CompareTotalFragment;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.Plans;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.models.CompareModel;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CompareActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener, View.OnTouchListener, CreatePopUpDialogFragment.RefreshLayoutPlansCallback {

    private ImageView mBackButton, icon2;
    private ImageView previousWeek, nextWeek;
    private TextView mText, guestText;
    private TextView mDateText, contactsQty;
    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private DateFormat dateFormat;
    private RelativeLayout addPersonButton;
    private Rect rect;
    private LinearLayout guestBox;

    private TextView commitmentsButton;
    private TextView freeTimeButton;
    private TextView deselectAll;

    private String email_user;
    private User user_friend;

    private int day_start, month_start, year_start;
    private int day1, month1, year1;
    private int day2, month2, year2;
    private int d1f;
    private List<CompareModel> listCompare = new ArrayList<>();
    private List<User> listPerson = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();

    private FragmentNavigator mNavigator;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        mSubscriptions = new CompositeDisposable();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email_user = mSharedPreferences.getString(Constants.EMAIL, "");

        UserWrapper wrap =
                (UserWrapper) getIntent().getSerializableExtra("email_compare_friend");

        if(wrap!=null)
            user_friend = wrap.getUser();

        findViewById(R.id.icon1).setVisibility(View.GONE);

        dateFormat = new DateFormat(this);

        icon2 = (ImageView) findViewById(R.id.icon2);
        commitmentsButton = (TextView) findViewById(R.id.commitmentsButton);
        freeTimeButton = (TextView) findViewById(R.id.freeTimeButton);
        recyclerView = (RecyclerView) findViewById(R.id.guestRow);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        mText = (TextView) findViewById(R.id.text);
        mDateText = (TextView) findViewById(R.id.dateComplete);
        contactsQty = (TextView) findViewById(R.id.contactsQty);
        guestBox = (LinearLayout) findViewById(R.id.guestBox);
        guestText = (TextView) findViewById(R.id.guestText);
        addPersonButton = (RelativeLayout) findViewById(R.id.addGuestButton);
        previousWeek = (ImageView) findViewById(R.id.previousWeek);
        nextWeek = (ImageView) findViewById(R.id.nextWeek);
        deselectAll = (TextView) findViewById(R.id.deselectAll);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        icon2.setImageResource(R.drawable.ic_add);
        icon2.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));

        icon2.setOnClickListener(this);
        icon2.setOnTouchListener(this);
        deselectAll.setOnTouchListener(this);
        previousWeek.setOnTouchListener(this);
        nextWeek.setOnTouchListener(this);
        mDateText.setOnTouchListener(this);
        guestBox.setOnClickListener(this);
        guestBox.setOnTouchListener(this);

        mSwipeRefreshLayout.setDistanceToTriggerSync(850);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,R.color.deep_purple_400));

        mText.setText(getResources().getString(R.string.compare_agendas));

        mBackButton.setOnClickListener(this);
        mDateText.setOnClickListener(this);
        commitmentsButton.setOnClickListener(this);
        freeTimeButton.setOnClickListener(this);
        addPersonButton.setOnClickListener(this);
        previousWeek.setOnClickListener(this);
        nextWeek.setOnClickListener(this);
        deselectAll.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);

        mNavigator = new FragmentNavigator(getFragmentManager(), new CompareFragmentAdapter(), R.id.contentBox);
        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.onCreate(savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {
                ImageView delete = (ImageView) view.findViewById(R.id.deleteButton);

                if(delete != null && delete.getVisibility() == View.VISIBLE && isPointInsideView(e.getX(), e.getY(), view)) {
                    listPerson.remove(position);
                    listCompare.remove(position);
                    adapter.notifyItemRemoved(position);

                    CompareTotalFragment compareTotalFragment = (CompareTotalFragment)mNavigator.getFragment(0);
                    CompareFreeFragment compareFreeFragment = (CompareFreeFragment)mNavigator.getFragment(1);

                    if(compareFreeFragment != null)
                        compareFreeFragment.updateDelete(position);


                    if(compareTotalFragment != null)
                        compareTotalFragment.updateDelete(position);

                    if(listPerson.size() == 1)
                        deselectAll.setVisibility(View.GONE);
                    else if(listPerson.size() == 2 && user_friend != null)
                        deselectAll.setVisibility(View.GONE);
                    else
                        deselectAll.setVisibility(View.VISIBLE);

                    contactsQty.setText(String.valueOf(listPerson.size()));
                }else if(position > 0) {
                    String name = listPerson.get(position).getName();
                    String email = listPerson.get(position).getEmail();
                    Intent myIntent = new Intent(CompareActivity.this, FriendProfileActivity.class);
                    myIntent.putExtra("name", name);
                    myIntent.putExtra("friend_email", email);
                    startActivity(myIntent);
                }
            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        }));

        new Actor.Builder(SpringSystem.create(), addPersonButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

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

        setCurrentTab(mNavigator.getCurrentPosition());

        commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left_pressed);
        freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right);
        commitmentsButton.setTextColor(ContextCompat.getColor(this,R.color.white));
        freeTimeButton.setTextColor(ContextCompat.getColor(this,R.color.deep_purple_400));

        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.add(Calendar.DAY_OF_WEEK, -15);
        day1 = cal.get(Calendar.DAY_OF_MONTH);
        month1 = cal.get(Calendar.MONTH)+1;
        year1 = cal.get(Calendar.YEAR);

        cal.add(Calendar.DAY_OF_WEEK, 15);
        day2 = cal.get(Calendar.DAY_OF_MONTH);
        month2 = cal.get(Calendar.MONTH)+1;
        year2 = cal.get(Calendar.YEAR);

        d1f = day1;

        if(month1 != month2){
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);
            cal2.add(Calendar.DAY_OF_WEEK, -15);
            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while(day3!=1){
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();
        plans.setA(year1);
        plans.setA2(year2);
        plans.setD1(day1);
        plans.setD2(day2);
        plans.setM(month1);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        plans.addEmails(email_user);
        if(wrap != null)
            plans.addEmails(user_friend.getEmail());

        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH)+1;
        year_start = cal.get(Calendar.YEAR);

        String month_text= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(cal.getTime().getTime());
        String day_text= dateFormat.formatDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

        mDateText.setText(getResources().getString(R.string.date_format_3, day_text, String.format("%02d", day_start), month_text, year_start));

        setProgress(true);
        setCompare(plans);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                mSwipeRefreshLayout.setRefreshing(false);
                updateLayout(day_start,month_start-1,year_start);
            }
        }, 500);

        // Load complete
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressScreen).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressScreen).setVisibility(View.GONE);
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

    private boolean isInThePast(int y1, int m1, int d1, int y2, int m2, int d2) {
        LocalDate end = new LocalDate(y1, m1, d1);
        LocalDate start = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        return timePeriod.getDays() < 0;
    }

    private void setCompare(Plans plans) {
        //setProgress(true);
        CompareTotalFragment compareTotalFragment = (CompareTotalFragment) mNavigator.getFragment(0);
        CompareFreeFragment compareFreeFragment = (CompareFreeFragment) mNavigator.getFragment(1);

        if (compareFreeFragment != null)
            compareFreeFragment.setProgress(true);


        if (compareTotalFragment != null)
            compareTotalFragment.setProgress(true);

        mSubscriptions.add(NetworkUtil.getRetrofit().getCompare(plans)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        listCompare.clear();

        if(listPerson.size() == 0) {

            response.getUser().setDelete(false);
            listPerson.add(response.getUser());

            if (user_friend != null){
                user_friend.setDelete(false);
                listPerson.add(user_friend);
            }
        }

        if(listPerson.size() == 1)
            deselectAll.setVisibility(View.GONE);
        else if(listPerson.size() == 2 && user_friend != null)
            deselectAll.setVisibility(View.GONE);
        else
            deselectAll.setVisibility(View.VISIBLE);

        adapter = new PersonAdapter(listPerson, this);
        contactsQty.setText(String.valueOf(listPerson.size()));
        recyclerView.setAdapter(adapter);

        Calendar cal = Calendar.getInstance();
        cal.set(year_start,month_start-1,day_start);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        int day, month, year;
        String invited;

        for(int i = 0; i < listPerson.size(); i++){
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH)+1;
            year = cal.get(Calendar.YEAR);
            invited = listPerson.get(i).getEmail();

            Calendar now = Calendar.getInstance();
            boolean inPast = isInThePast(year,month, day,
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

            CompareModel compareModel = new CompareModel(listPerson.get(i).getPhoto(), listPerson.get(i).getName(), listPerson.get(i).getEmail(), inPast);

            int j;
            for(j = 0; j < response.getMyCommitAct().size(); j++) {
                ActivityServer activity = response.getMyCommitAct().get(j);
                ActivityServer activityServer = new ActivityServer(activity);
                if(isActivityInRange(activityServer.getDayStart(), activityServer.getMonthStart(), activityServer.getDayEnd(), activityServer.getMonthEnd(), day, month) && invited.equals(activityServer.getEmailInvited())) {
                    boolean start = isStartedFinishedToday(day, activityServer.getDayStart());
                    boolean finish = isStartedFinishedToday(day, activityServer.getDayEnd());
                    if(!start && finish) {
                        activityServer.setMinuteCard(0);
                        activityServer.setHourCard(0);
                        activityServer.setMinuteEndCard(activityServer.getMinuteEnd());
                        activityServer.setHourEndCard(activityServer.getHourEnd());
                    }else if(start && !finish) {
                        activityServer.setMinuteCard(activityServer.getMinuteStart());
                        activityServer.setHourCard(activityServer.getHourStart());
                        activityServer.setMinuteEndCard(59);
                        activityServer.setHourEndCard(23);
                    }else if(!start && !finish){
                        activityServer.setMinuteCard(0);
                        activityServer.setHourCard(0);
                        activityServer.setMinuteEndCard(59);
                        activityServer.setHourEndCard(23);
                    }else {
                        activityServer.setMinuteCard(activityServer.getMinuteStart());
                        activityServer.setHourCard(activityServer.getHourStart());
                        activityServer.setMinuteEndCard(activityServer.getMinuteEnd());
                        activityServer.setHourEndCard(activityServer.getHourEnd());
                    }

                    compareModel.addPlans(activityServer);
                }
            }
            for(j = 0; j < response.getMyCommitFlag().size(); j++) {
                FlagServer flag = response.getMyCommitFlag().get(j);
                FlagServer flagServer = new FlagServer(flag);
                if(isActivityInRange(flagServer.getDayStart(), flagServer.getMonthStart(), flagServer.getDayEnd(), flagServer.getMonthEnd(), day, month) && invited.equals(flagServer.getEmailInvited())) {
                    boolean start = isStartedFinishedToday(day, flagServer.getDayStart());
                    boolean finish = isStartedFinishedToday(day, flagServer.getDayEnd());
                    if(!start && finish) {
                        flagServer.setMinuteCard(0);
                        flagServer.setHourCard(0);
                        flagServer.setMinuteEndCard(flagServer.getMinuteEnd());
                        flagServer.setHourEndCard(flagServer.getHourEnd());
                    }else if(start && !finish) {
                        flagServer.setMinuteCard(flagServer.getMinuteStart());
                        flagServer.setHourCard(flagServer.getHourStart());
                        flagServer.setMinuteEndCard(59);
                        flagServer.setHourEndCard(23);
                    }else if(!start && !finish){
                        flagServer.setMinuteCard(0);
                        flagServer.setHourCard(0);
                        flagServer.setMinuteEndCard(59);
                        flagServer.setHourEndCard(23);
                    }else {
                        flagServer.setMinuteCard(flagServer.getMinuteStart());
                        flagServer.setHourCard(flagServer.getHourStart());
                        flagServer.setMinuteEndCard(flagServer.getMinuteEnd());
                        flagServer.setHourEndCard(flagServer.getHourEnd());
                    }
                    compareModel.addPlans(flagServer);
                }
            }

            if(i == 0) {
                for (j = 0; j < response.getMyCommitReminder().size(); j++) {
                    ReminderServer reminderServer = response.getMyCommitReminder().get(j);
                    compareModel.addPlans(reminderServer);
                }
            }

            Collections.sort(compareModel.getActivities(), new Comparator<Object>() {
                @Override
                public int compare(Object c1, Object c2) {
                    ActivityServer activityServer;
                    FlagServer flagServer;
                    ReminderServer reminderServer;
                    int start_hour = 0, start_minute= 0;
                    int start_hour2= 0, start_minute2= 0;
                    int end_hour = 0, end_minute= 0;
                    int end_hour2= 0, end_minute2= 0;

                    if(c1 instanceof ActivityServer) {
                        activityServer = (ActivityServer) c1;
                        start_hour = activityServer.getHourCard();
                        start_minute = activityServer.getMinuteCard();
                        end_hour = activityServer.getHourEndCard();
                        end_minute = activityServer.getMinuteEndCard();
                    }
                    else if(c1 instanceof FlagServer) {
                        flagServer = (FlagServer) c1;
                        start_hour = flagServer.getHourCard();
                        start_minute = flagServer.getMinuteCard();
                        end_hour = flagServer.getHourEndCard();
                        end_minute = flagServer.getMinuteEndCard();
                    }else if (c1 instanceof ReminderServer) {
                        reminderServer = (ReminderServer) c1;
                        start_hour = reminderServer.getHourStart();
                        start_minute = reminderServer.getMinuteStart();
                        end_hour = reminderServer.getHourStart();
                        end_minute = reminderServer.getMinuteStart();
                    }

                    if(c2 instanceof ActivityServer) {
                        activityServer = (ActivityServer) c2;
                        start_hour2 = activityServer.getHourCard();
                        start_minute2 = activityServer.getMinuteCard();
                        end_hour2 = activityServer.getHourEndCard();
                        end_minute2 = activityServer.getMinuteEndCard();
                    }
                    else if(c2 instanceof FlagServer) {
                        flagServer = (FlagServer) c2;
                        start_hour2 = flagServer.getHourCard();
                        start_minute2 = flagServer.getMinuteCard();
                        end_hour2 = flagServer.getHourEndCard();
                        end_minute2 = flagServer.getMinuteEndCard();
                    }else if (c2 instanceof ReminderServer) {
                        reminderServer = (ReminderServer) c2;
                        start_hour2 = reminderServer.getHourStart();
                        start_minute2 = reminderServer.getMinuteStart();
                        end_hour2 = reminderServer.getHourStart();
                        end_minute2 = reminderServer.getMinuteStart();
                    }

                    if(start_hour < start_hour2)
                        return -1;
                    else if(start_hour > start_hour2)
                        return 1;
                    else if(start_minute < start_minute2)
                        return -1;
                    else if (start_minute > start_minute2)
                        return 1;
                    else if(end_hour < end_hour2)
                        return -1;
                    else if(end_hour > end_hour2)
                        return 1;
                    else if(end_minute < end_minute2)
                        return -1;
                    else if (end_minute > end_minute2)
                        return 1;
                    else
                        return 0;

                }
            });

            ArrayList<Object> arrayList = new ArrayList<>();

            if(compareModel.getActivities().size() == 0){
                FreeTimeServer freeTimeServer = new FreeTimeServer();
                freeTimeServer.setHourStart(0);
                freeTimeServer.setMinuteStart(0);
                freeTimeServer.setHourEnd(23);
                freeTimeServer.setMinuteEnd(59);

                compareModel.addFree(freeTimeServer);
            }else{
                int start_hour = 0, start_minute= 0;
                int end_hour = 0, end_minute= 0;
                int free_hour = 0, free_minute= 0;

                for(j = 0; j < compareModel.getActivities().size(); j++) {
                    Object object = compareModel.getActivities().get(j);
                    ActivityServer activityServer;
                    FlagServer flagServer;
                    ReminderServer reminderServer;

                    if(object instanceof ActivityServer) {
                        activityServer = (ActivityServer) object;
                        start_hour = activityServer.getHourCard();
                        start_minute = activityServer.getMinuteCard();
                        end_hour = activityServer.getHourEndCard();
                        end_minute = activityServer.getMinuteEndCard();
                    }
                    else if(object instanceof FlagServer) {
                        flagServer = (FlagServer) object;
                        start_hour = flagServer.getHourCard();
                        start_minute = flagServer.getMinuteCard();
                        end_hour = flagServer.getHourEndCard();
                        end_minute = flagServer.getMinuteEndCard();
                    }
                    else if(object instanceof ReminderServer) {
                        reminderServer = (ReminderServer) object;
                        start_hour = reminderServer.getHourStart();
                        start_minute = reminderServer.getMinuteStart();
                        end_hour = start_hour;
                        end_minute = start_minute;
                    }

                    FreeTimeServer freeTimeServer = new FreeTimeServer();

                    if((free_hour < start_hour) ||(free_hour == start_hour && free_minute < start_minute)){
                        freeTimeServer.setHourStart(free_hour);
                        freeTimeServer.setMinuteStart(free_minute);
                        freeTimeServer.setHourEnd(start_hour);
                        freeTimeServer.setMinuteEnd(start_minute);

                        free_hour = end_hour;
                        free_minute = end_minute;

                        arrayList.add(freeTimeServer);

                    }else if((free_hour == start_hour && free_minute == start_minute) || (free_hour < end_hour) || (free_hour == end_hour && free_minute < end_minute)){
                        free_hour = end_hour;
                        free_minute = end_minute;
                    }

                    if((j == compareModel.getActivities().size() -1) && (end_hour < 23 || (end_hour == 23 && end_minute < 59))){
                        if(!(free_hour == 23 && free_minute == 59)){
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

            compareModel.addAllFree(arrayList);

            listCompare.add(compareModel);
        }

        CompareTotalFragment compareTotalFragment = (CompareTotalFragment)mNavigator.getFragment(0);
        CompareFreeFragment compareFreeFragment = (CompareFreeFragment)mNavigator.getFragment(1);

        if(compareFreeFragment != null) {
            mNavigator.resetFragments(mNavigator.getCurrentPosition());
            compareFreeFragment.setDataAdapter(listCompare);
            compareFreeFragment.setProgress(false);
        }

        if(compareTotalFragment != null) {
            compareTotalFragment.setDataAdapter(listCompare);
            compareTotalFragment.setProgress(false);
        }

        setProgress(false);
    }

    public boolean isFreeTimeOneMinute(int start_hour, int start_minute, int end_hour, int end_minute){
        return start_hour == end_hour && ((end_minute - start_minute) == 1);
    }

    public boolean isStartFinishSame(int start_hour, int start_minute, int end_hour, int end_minute){
        return start_hour == end_hour && end_minute == start_minute;
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    public boolean isStartedFinishedToday(int day, int day2){
        return day == day2;
    }

    public boolean isActivityInRange(int ds, int ms, int df, int mf, int d, int m){
        if(ms == mf) {
            if (ds > d || df < d)
                return false;
            else
                return true;
        }
        else{
            if(d >= ds || d <= df)
                return true;
            else
                return false;
        }
    }

    public List<CompareModel> getListCompare(){
        return listCompare;
    }

    public List<Integer> getTodayDate(){
        List<Integer> list = new ArrayList<>();
        list.add(day_start);
        list.add(month_start);
        list.add(year_start);
        return list;
    }

    @Override
    public void onClick(View view) {
        if(view == mDateText) {

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mDateText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();

            now.set(year_start, month_start-1,day_start);

            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    CompareActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.setAccentColor(ContextCompat.getColor(CompareActivity.this,R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog");
        }else if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if(view == freeTimeButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "freeTimeButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left);
            freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right_pressed);
            commitmentsButton.setTextColor(ContextCompat.getColor(CompareActivity.this,R.color.deep_purple_400));
            freeTimeButton.setTextColor(ContextCompat.getColor(CompareActivity.this,R.color.white));
            setCurrentTab(1);
        }
        else if(view == commitmentsButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "commitmentsButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left_pressed);
            freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right);
            commitmentsButton.setTextColor(ContextCompat.getColor(CompareActivity.this,R.color.white));
            freeTimeButton.setTextColor(ContextCompat.getColor(CompareActivity.this,R.color.deep_purple_400));
            setCurrentTab(0);
        }else if(view == addPersonButton || view == guestBox){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(CompareActivity.this, SelectPeopleActivity.class);
            ArrayList<String> list = new ArrayList<>();
            for(int i = 0; i < listPerson.size(); i++){
                list.add(listPerson.get(i).getEmail());
            }
            intent.putStringArrayListExtra("guest_list", list);
            if(user_friend != null)
                intent.putExtra("user_friend_exclude", new UserWrapper(user_friend));
            startActivityForResult(intent, 1);
        }else if (view == previousWeek) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "previousWeek" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.DAY_OF_WEEK, -1);
            day_start = cal.get(Calendar.DAY_OF_MONTH);
            month_start = cal.get(Calendar.MONTH) + 1;
            year_start = cal.get(Calendar.YEAR);

            updateLayout(day_start, month_start - 1, year_start);

        } else if (view == nextWeek) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "nextWeek" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.DAY_OF_WEEK, 1);
            day_start = cal.get(Calendar.DAY_OF_MONTH);
            month_start = cal.get(Calendar.MONTH) + 1;
            year_start = cal.get(Calendar.YEAR);

            updateLayout(day_start, month_start - 1, year_start);
        }else if(view == deselectAll){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deselectAll" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int start;

            if(user_friend == null)
                start = 1;
            else
                start = 2;


            for(int i=start;i<listPerson.size();i++) {

                listPerson.remove(i);
                listCompare.remove(i);
                adapter.notifyItemRemoved(i);

                CompareTotalFragment compareTotalFragment = (CompareTotalFragment) mNavigator.getFragment(0);
                CompareFreeFragment compareFreeFragment = (CompareFreeFragment) mNavigator.getFragment(1);

                if (compareFreeFragment != null)
                    compareFreeFragment.updateDelete(i);


                if (compareTotalFragment != null)
                    compareTotalFragment.updateDelete(i);

                i--;
            }

            deselectAll.setVisibility(View.GONE);

            contactsQty.setText(String.valueOf(listPerson.size()));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mNavigator!=null)
            mNavigator.onSaveInstanceState(outState);
    }

    private void setCurrentTab(int position) {
        mNavigator.showFragment(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");

        if(dpd != null) dpd.setOnDateSetListener(this);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ArrayList<User> list;
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                PersonModelWrapper wrap =
                        (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                list = wrap.getItemDetails();

                if (list.size() > 0) {
                    User personModel = listPerson.get(0);
                    list.add(0, personModel);
                    if(user_friend!=null)
                        list.add(1, listPerson.get(1));

                    adapter.swap(list);

                    Plans plans = new Plans();

                    for(int i=0;i<listPerson.size();i++)
                        plans.addEmails(listPerson.get(i).getEmail());

                    plans.setA(year1);
                    plans.setA2(year2);
                    plans.setD1(day1);
                    plans.setD2(day2);
                    plans.setM(month1);
                    plans.setM2(month2);
                    plans.setD1f(d1f);
                    plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

                    setCompare(plans);
                }
                else
                    Toast.makeText(this, R.string.validation_field_compare_selected_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateLayout(int dayOfMonth, int monthOfYear, int year) {
        listCompare.clear();

        Calendar cal = Calendar.getInstance();
        cal.set(year,monthOfYear,dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.add(Calendar.DAY_OF_WEEK, -15);
        day1 = cal.get(Calendar.DAY_OF_MONTH);
        month1 = cal.get(Calendar.MONTH)+1;
        year1 = cal.get(Calendar.YEAR);

        cal.add(Calendar.DAY_OF_WEEK, 15);
        day2 = cal.get(Calendar.DAY_OF_MONTH);
        month2 = cal.get(Calendar.MONTH)+1;
        year2 = cal.get(Calendar.YEAR);

        d1f = day1;

        if(month1 != month2){
            Calendar cal2 = Calendar.getInstance();
            cal2.set(year1,month1-1,day1);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);

            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while(day3!=1){
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();

        for(int i=0;i<listPerson.size();i++)
            plans.addEmails(listPerson.get(i).getEmail());

        plans.setA(year1);
        plans.setA2(year2);
        plans.setD1(day1);
        plans.setD2(day2);
        plans.setM(month1);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH)+1;
        year_start = cal.get(Calendar.YEAR);

        String month_text= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(cal.getTime().getTime());
        String day_text= dateFormat.formatDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

        mDateText.setText(getResources().getString(R.string.date_format_3, day_text, String.format("%02d", day_start), month_text, year_start));

        setCompare(plans);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        listCompare.clear();

        Calendar cal = Calendar.getInstance();
        cal.set(year,monthOfYear,dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.add(Calendar.DAY_OF_WEEK, -15);
        day1 = cal.get(Calendar.DAY_OF_MONTH);
        month1 = cal.get(Calendar.MONTH)+1;
        year1 = cal.get(Calendar.YEAR);

        cal.add(Calendar.DAY_OF_WEEK, 15);
        day2 = cal.get(Calendar.DAY_OF_MONTH);
        month2 = cal.get(Calendar.MONTH)+1;
        year2 = cal.get(Calendar.YEAR);

        d1f = day1;

        if(month1 != month2){
            Calendar cal2 = Calendar.getInstance();
            cal2.set(year1,month1-1,day1);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);

            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while(day3!=1){
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();

        for(int i=0;i<listPerson.size();i++)
            plans.addEmails(listPerson.get(i).getEmail());

        plans.setA(year1);
        plans.setA2(year2);
        plans.setD1(day1);
        plans.setD2(day2);
        plans.setM(month1);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH)+1;
        year_start = cal.get(Calendar.YEAR);

        String month_text= new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(cal.getTime().getTime());
        String day_text= dateFormat.formatDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

        mDateText.setText(getResources().getString(R.string.date_format_3, day_text, String.format("%02d", day_start), month_text, year_start));

        setCompare(plans);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    @Override
    public void refreshLayout(boolean showRefresh) {
        updateLayout(day_start, month_start - 1,year_start);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        }
        else if (view == guestBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                guestText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                contactsQty.setBackground(ContextCompat.getDrawable(this, R.drawable.box_qty_guests));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                guestText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                contactsQty.setBackground(ContextCompat.getDrawable(this, R.drawable.box_qty_guests_pressed));
            }
        }
        else if (view == icon2) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                icon2.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                icon2.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        }
        else if (view == deselectAll) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                deselectAll.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                deselectAll.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        }
        else if (view == mDateText) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mDateText.setTextColor(ContextCompat.getColor(this, R.color.grey_900));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mDateText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
            }
        }
        else if (view == nextWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                nextWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_900));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                nextWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            }
        }
        else if (view == previousWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                previousWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_900));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                previousWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            }
        }

        return false;
    }

}
