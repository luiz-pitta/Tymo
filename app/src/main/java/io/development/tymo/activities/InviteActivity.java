package io.development.tymo.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.adapters.InviteAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.models.InviteModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class InviteActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, SwipeRefreshLayout.OnRefreshListener  {

    private EasyRecyclerView recyclerView;
    private InviteAdapter adapter;

    private Handler handler = new Handler();

    private ImageView mBackButton;
    private TextView m_title;
    
    private DateFormat dateFormat;

    private CompositeDisposable mSubscriptions;
    private SharedPreferences mSharedPreferences;

    private List<InviteModel> listInvite;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);

        dateFormat = new DateFormat(this);

        mSubscriptions = new CompositeDisposable();

        findViewById(R.id.dateBox).setVisibility(View.GONE);
        findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
        findViewById(R.id.searchSelection).setVisibility(View.GONE);
        findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
        findViewById(R.id.horizontalBottomLine).setVisibility(View.GONE);
        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);

        mBackButton.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);

        m_title.setText(getResources().getString(R.string.notification_2));

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.getSwipeToRefresh().setDistanceToTriggerSync(400);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapterWithProgress(adapter = new InviteAdapter(this));

        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColor(ContextCompat.getColor(this,R.color.deep_purple_400));

        recyclerView.setEmptyView(R.layout.empty_invitations);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                InviteModel model = adapter.getItem(positionStart);

                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH) + 1;
                int year = c.get(Calendar.YEAR);

                Calendar c2 = Calendar.getInstance();
                c2.add(Calendar.DATE, 1);
                int day2 = c2.get(Calendar.DAY_OF_MONTH);
                int month2 = c2.get(Calendar.MONTH) + 1;
                int year2 = c2.get(Calendar.YEAR);

                int d;
                int m;
                int y;

                if(model.getActivity() instanceof ActivityServer){
                    ActivityServer activityServer = (ActivityServer)model.getActivity();
                    d = activityServer.getDayStart();
                    m = activityServer.getMonthStart();
                    y = activityServer.getYearStart();
                }else {
                    FlagServer flagServer = (FlagServer)model.getActivity();
                    d = flagServer.getDayStart();
                    m = flagServer.getMonthStart();
                    y = flagServer.getYearStart();
                }

                if((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
                    getActivityStartToday();
            }
        });

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.INVITE_ACCEPT);

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        DateTymo dateTymo = new DateTymo();
        dateTymo.setDay(day);
        dateTymo.setMonth(month);
        dateTymo.setYear(year);
        dateTymo.setMinute(minute);
        dateTymo.setHour(hour);
        dateTymo.setDateTime(c.getTimeInMillis());

        retrieveInviteRequest(email, dateTymo);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void retrieveInviteRequest(String email, DateTymo dateTymo) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getInviteRequest(email, dateTymo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void visualizeInviteRequest(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().visualizeInviteRequest(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseVisualize,this::handleError));
    }

    private void handleResponseVisualize(Response response) {
    }

    private void handleResponse(Response response) {

        int i;
        listInvite = new ArrayList<>();
        ArrayList<ActivityServer> activity_accepted = response.getWhatsGoingAct();
        ArrayList<FlagServer> flag_accepted = response.getWhatsGoingFlag();

        for(i = 0; i < response.getMyCommitAct().size(); i++){
            ActivityServer activityServer = response.getMyCommitAct().get(i);

            String inviter_name = this.getResources().getString(R.string.invited_by, activityServer.getNameInviter());

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(),activityServer.getMonthStart()-1,activityServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", activityServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = activityServer.getYearStart();
            String hourStart = String.format("%02d", activityServer.getHourStart());
            String minuteStart = String.format("%02d", activityServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);
            
            InviteModel inviteModel = new InviteModel(activityServer.getTitle(), date, inviter_name, activityServer.getCubeIcon(), activityServer.getCubeColorUpper(), activityServer.getCubeColor(), activityServer);
            listInvite.add(inviteModel);
        }

        for(i = 0; i < response.getMyCommitFlag().size(); i++){
            FlagServer flagServer = response.getMyCommitFlag().get(i);

            String inviter_name = this.getResources().getString(R.string.invited_by, flagServer.getNameInviter());

            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(),flagServer.getMonthStart()-1,flagServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", flagServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = flagServer.getYearStart();
            String hourStart = String.format("%02d", flagServer.getHourStart());
            String minuteStart = String.format("%02d", flagServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            InviteModel inviteModel = new InviteModel(flagServer.getTitle(), date, inviter_name, "",0,0, flagServer);
            listInvite.add(inviteModel);
        }

        //Notification that friends accepted your invite

        Collections.sort(activity_accepted, new Comparator<ActivityServer>() {
            @Override
            public int compare(ActivityServer c1, ActivityServer c2) {
                long id1 = c1.getId();
                long id2 = c2.getId();


                if(id1 > id2)
                    return 1;
                else if(id1 < id2)
                    return -1;
                else
                    return 0;
            }
        });

        Collections.sort(flag_accepted, new Comparator<FlagServer>() {
            @Override
            public int compare(FlagServer c1, FlagServer c2) {
                long id1 = c1.getId();
                long id2 = c2.getId();


                if(id1 > id2)
                    return 1;
                else if(id1 < id2)
                    return -1;
                else
                    return 0;
            }
        });

        ArrayList<Object> list = new ArrayList<>();
        list.addAll(activity_accepted);

        for(i = 0; i < activity_accepted.size(); ){
            int index1 = getFriendsWhoAccepted(list, i);
            int num_elem = (index1 - i) + 1;
            String friend_accept = "";

            if(num_elem > 1) {
                num_elem--;
                if(num_elem == 1) {
                    friend_accept = activity_accepted.get(index1).getCreator() + " ";
                    friend_accept += getResources().getString(R.string.invitation_accepted_more_others_one);
                }
                else if(num_elem > 1){
                    friend_accept = activity_accepted.get(index1).getCreator() + " ";
                    friend_accept += getResources().getString(R.string.invitation_accepted_more_others, num_elem);
                }
            }else if(num_elem == 1){
                friend_accept = activity_accepted.get(index1).getCreator() + " ";
                friend_accept += getResources().getString(R.string.invitation_accepted_more_others_one);
            }



            InviteModel inviteModel = new InviteModel(activity_accepted.get(index1).getTitle(), friend_accept, "accept",
                    activity_accepted.get(index1).getCubeIcon(), activity_accepted.get(index1).getCubeColorUpper(),
                    activity_accepted.get(index1).getCubeColor(), activity_accepted.get(index1));

            listInvite.add(inviteModel);

            i = index1 + 1;
        }

        list.clear();
        list.addAll(flag_accepted);

        for(i = 0; i < flag_accepted.size(); ){
            int index1 = getFriendsWhoAccepted(list, i);
            int num_elem = (index1 - i) + 1;
            String friend_accept = "";

            if(num_elem > 1) {
                num_elem--;
                if(num_elem == 1) {
                    friend_accept = flag_accepted.get(index1).getCreator() + " ";
                    friend_accept += getResources().getString(R.string.invitation_accepted_more_others_one);
                }
                else if(num_elem > 1){
                    friend_accept = flag_accepted.get(index1).getCreator() + " ";
                    friend_accept += getResources().getString(R.string.invitation_accepted_more_others, num_elem);
                }
            }else if(num_elem == 1){
                friend_accept = flag_accepted.get(index1).getCreator() + " ";
                friend_accept += getResources().getString(R.string.invitation_accepted_more_others_one);
            }

            InviteModel inviteModel = new InviteModel(flag_accepted.get(index1).getTitle(), friend_accept, "accept", "",0,0, flag_accepted.get(index1));

            listInvite.add(inviteModel);

            i = index1 + 1;
        }

        //END

        Collections.sort(listInvite, new Comparator<InviteModel>() {
            @Override
            public int compare(InviteModel c1, InviteModel c2) {
                ActivityServer act1, act2;
                FlagServer f1, f2;

                String invite_date1 = "", invite_date2 = "";

                if(c1.getActivity() instanceof ActivityServer)
                {
                    act1 = (ActivityServer) c1.getActivity();
                    invite_date1 = act1.getInviteDate();

                }else if(c1.getActivity() instanceof FlagServer){
                    f1 = (FlagServer) c1.getActivity();
                    invite_date1 = f1.getInviteDate();
                }

                if(c2.getActivity() instanceof ActivityServer)
                {
                    act2 = (ActivityServer) c2.getActivity();
                    invite_date2 = act2.getInviteDate();
                }else if(c2.getActivity() instanceof FlagServer){
                    f2 = (FlagServer) c2.getActivity();
                    invite_date2 = f2.getInviteDate();
                }


                if(invite_date1.compareTo(invite_date2) > 0)
                    return -1;
                else if(invite_date1.compareTo(invite_date2) < 0)
                    return 1;
                else
                    return 0;
            }
        });

        adapter.clear();
        adapter.addAll(listInvite);

        visualizeInviteRequest(mSharedPreferences.getString(Constants.EMAIL, ""));
    }

    private int getFriendsWhoAccepted(ArrayList<Object> commitments, int index) {
        int id = index;
        ActivityServer act1;
        ActivityServer act2;
        FlagServer f1;
        FlagServer f2;
        int type;
        int i;

        if(commitments.get(0) instanceof ActivityServer)
            type = 0;
        else
            type = 1;

        for(i = index; i < (commitments.size()-1); i++){
            if(type == 0){
                act1 = (ActivityServer)commitments.get(i);
                act2 = (ActivityServer)commitments.get(i+1);
                if(act1.getId() != act2.getId())
                    return i;
            }else {
                f1 = (FlagServer)commitments.get(i);
                f2 = (FlagServer)commitments.get(i+1);
                if(f1.getId() != f2.getId())
                    return i;
            }
        }

        return i;
    }

    private void handleError(Throwable error) {
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.clear();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "onRefresh" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH)+1;
                int year = c.get(Calendar.YEAR);
                int minute = c.get(Calendar.MINUTE);
                int hour = c.get(Calendar.HOUR_OF_DAY);

                DateTymo dateTymo = new DateTymo();
                dateTymo.setDay(day);
                dateTymo.setMonth(month);
                dateTymo.setYear(year);
                dateTymo.setMinute(minute);
                dateTymo.setHour(hour);
                dateTymo.setDateTime(c.getTimeInMillis());

                retrieveInviteRequest(mSharedPreferences.getString(Constants.EMAIL, ""), dateTymo);

                recyclerView.showProgress();
            }
        }, 1000);
    }


    @Override
    public void onClick(View v){
        if(v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    private void getActivityStartToday(){
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
        if(mJobManager.getAllJobRequestsForTag(NotificationSyncJob.TAG).size() > 0)
            mJobManager.cancelAllForTag(NotificationSyncJob.TAG);

        ArrayList<Object> list = new ArrayList<>();
        ArrayList<ActivityOfDay> list_notify = new ArrayList<>();

        if (response.getMyCommitAct() != null) {
            ArrayList<ActivityServer> activityServers = response.getMyCommitAct();
            for(int i=0;i<activityServers.size();i++){
                list.add(activityServers.get(i));
            }
        }
        if (response.getMyCommitFlag() != null) {
            ArrayList<FlagServer> flagServers = response.getMyCommitFlag();
            for(int i=0;i<flagServers.size();i++){
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
                        activityServer.getDayStart(),activityServer.getMonthStart(),activityServer.getYearStart()));
            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);
                list_notify.add(new ActivityOfDay(flagServer.getTitle(), flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
                        flagServer.getDayStart(),flagServer.getMonthStart(),flagServer.getYearStart()));
            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);
                list_notify.add(new ActivityOfDay(reminderServer.getTitle(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
                        reminderServer.getDayStart(),reminderServer.getMonthStart(),reminderServer.getYearStart()));
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
            c1.set(Calendar.MONTH, activityOfDay.getMonth()-1);
            c1.set(Calendar.YEAR, activityOfDay.getYear());
            c1.set(Calendar.HOUR_OF_DAY, activityOfDay.getHourStart());
            c1.set(Calendar.MINUTE, activityOfDay.getMinuteStart());
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
            c2.set(Calendar.MONTH, activityOfDayNext.getMonth()-1);
            c2.set(Calendar.YEAR, activityOfDayNext.getYear());
            c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
            c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);

            while(activityOfDayNext !=null && c1.getTimeInMillis() == c2.getTimeInMillis()) {
                j++;
                count_same++;
                if(j < list_notify.size()) {
                    activityOfDayNext = list_notify.get(j);
                    c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
                    c2.set(Calendar.MONTH, activityOfDayNext.getMonth()-1);
                    c2.set(Calendar.YEAR, activityOfDayNext.getYear());
                    c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
                    c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
                    c2.set(Calendar.SECOND, 0);
                    c2.set(Calendar.MILLISECOND, 0);
                }
                else
                    activityOfDayNext = null;
            }
            activityOfDay.setCommitmentSameHour(count_same);

            time_exact = (int)(c1.getTimeInMillis()-c3.getTimeInMillis())/(1000*60);
            if(time_exact >= Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT) {
                c1.add(Calendar.MINUTE, -Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT);
                time_to_happen = c1.getTimeInMillis()-c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            if(time_exact >= 1440) {
                c1.add(Calendar.MINUTE, -1380);
                time_to_happen = c1.getTimeInMillis()-c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras2)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            i=j-1;
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

    @Override
    protected void onResume() {
        super.onResume();
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        DateTymo dateTymo = new DateTymo();
        dateTymo.setDay(day);
        dateTymo.setMonth(month);
        dateTymo.setYear(year);
        dateTymo.setMinute(minute);
        dateTymo.setHour(hour);
        dateTymo.setDateTime(c.getTimeInMillis());

        retrieveInviteRequest(email, dateTymo);
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

        return false;
    }
}
