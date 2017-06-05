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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class InviteActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener  {

    private EasyRecyclerView recyclerView;
    private InviteAdapter adapter;

    private Handler handler = new Handler();

    private ImageView mBackButton;
    private TextView m_title;
    
    private DateFormat dateFormat;

    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;

    private List<InviteModel> listInvite;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);

        dateFormat = new DateFormat(this);

        mSubscriptions = new CompositeSubscription();

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

        m_title.setText(getResources().getString(R.string.invitations));

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

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

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);
            
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

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

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
                    friend_accept += getResources().getString(R.string.invite_accept_one_person, 1);
                }
                else if(num_elem > 1){
                    friend_accept = activity_accepted.get(index1).getCreator() + " ";
                    friend_accept += getResources().getString(R.string.invite_accept_more_one_person, num_elem);
                }
            }else if(num_elem == 1){
                friend_accept = activity_accepted.get(index1).getCreator() + " ";
                friend_accept += getResources().getString(R.string.invite_accept_zero_people);
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
                    friend_accept += getResources().getString(R.string.invite_accept_one_person, 1);
                }
                else if(num_elem > 1){
                    friend_accept = flag_accepted.get(index1).getCreator() + " ";
                    friend_accept += getResources().getString(R.string.invite_accept_more_one_person, num_elem);
                }
            }else if(num_elem == 1){
                friend_accept = flag_accepted.get(index1).getCreator() + " ";
                friend_accept += getResources().getString(R.string.invite_accept_zero_people);
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
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
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

            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
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
        boolean commitments = false;

        int startsAtHour = 0;
        int startsAtMinute = 0;

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
                int status = 0; // -1 = already happened ; 0 = is happening ; 1 = will happen
                int status2 = 0; // -1 = already happened ; 0 = is happening ; 1 = will happen

                Calendar calendar = Calendar.getInstance();
                String hourNow = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
                String minuteNow = String.format("%02d", calendar.get(Calendar.MINUTE));

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

                    String hour = String.format("%02d", start_hour);
                    String minute = String.format("%02d", start_minute);
                    String hourEnd = String.format("%02d", end_hour);
                    String minuteEnd = String.format("%02d", end_minute);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                    calendarEnd.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour = 23;
                            end_minute = 59;
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour = 0;
                            start_minute = 0;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour = 0;
                            start_minute = 0;
                            end_hour = 23;
                            end_minute = 59;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status = 1;
                    } else {
                        status = -1;
                    }

                    activityServer.setStatus(status);
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

                    String hour = String.format("%02d", start_hour);
                    String minute = String.format("%02d", start_minute);
                    String hourEnd = String.format("%02d", end_hour);
                    String minuteEnd = String.format("%02d", end_minute);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                    calendarEnd.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour = 23;
                            end_minute = 59;
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour = 0;
                            start_minute = 0;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour = 0;
                            start_minute = 0;
                            end_hour = 23;
                            end_minute = 59;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status = 1;
                    } else {
                        status = -1;
                    }

                    flagServer.setStatus(status);
                }
                // Reminder
                else if (c1 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c1;
                    start_hour = reminderServer.getHourStart();
                    start_minute = reminderServer.getMinuteStart();

                    day = reminderServer.getDayStart();
                    month = reminderServer.getMonthStart();
                    year = reminderServer.getYearStart();

                    if (isTimeInBefore(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                        status = -1;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                        status = 1;
                    } else {
                        status = 0;
                    }

                    reminderServer.setStatus(status);
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

                    String hour = String.format("%02d", start_hour2);
                    String minute = String.format("%02d", start_minute2);
                    String hourEnd = String.format("%02d", end_hour2);
                    String minuteEnd = String.format("%02d", end_minute2);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                    calendarEnd.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour2 = 23;
                            end_minute2 = 59;
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            end_hour2 = 23;
                            end_minute2 = 59;
                            activityServer.setHourStart(0);
                            activityServer.setMinuteStart(0);
                            activityServer.setHourEnd(23);
                            activityServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status2 = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status2 = 1;
                    } else {
                        status2 = -1;
                    }

                    activityServer.setStatus(status2);
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

                    String hour = String.format("%02d", start_hour2);
                    String minute = String.format("%02d", start_minute2);
                    String hourEnd = String.format("%02d", end_hour2);
                    String minuteEnd = String.format("%02d", end_minute2);
                    Calendar calendarStart = Calendar.getInstance();
                    Calendar calendarEnd = Calendar.getInstance();
                    calendarStart.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                    calendarEnd.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                    if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                        if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hourEnd = "23";
                            minuteEnd = "59";
                            end_hour2 = 23;
                            end_minute2 = 59;
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                            hour = "00";
                            minute = "00";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                        } else {
                            hour = "00";
                            minute = "00";
                            hourEnd = "23";
                            minuteEnd = "59";
                            start_hour2 = 0;
                            start_minute2 = 0;
                            end_hour2 = 23;
                            end_minute2 = 59;
                            flagServer.setHourStart(0);
                            flagServer.setMinuteStart(0);
                            flagServer.setHourEnd(23);
                            flagServer.setMinuteEnd(59);
                        }
                    }

                    if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                        status2 = 0;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                        status2 = 1;
                    } else {
                        status2 = -1;
                    }

                    flagServer.setStatus(status2);
                }
                // Reminder
                else if (c2 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c2;
                    start_hour2 = reminderServer.getHourStart();
                    start_minute2 = reminderServer.getMinuteStart();

                    day2 = reminderServer.getDayStart();
                    month2 = reminderServer.getMonthStart();
                    year2 = reminderServer.getYearStart();

                    if (isTimeInBefore(hourNow + ":" + minuteNow, start_hour2 + ":" + start_minute2)) {
                        status2 = -1;
                    } else if (isTimeInAfter(hourNow + ":" + minuteNow, start_hour2 + ":" + start_minute2)) {
                        status2 = 1;
                    } else {
                        status2 = 0;
                    }

                    reminderServer.setStatus(status2);

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
                else if (status < status2)
                    return -1;
                else if (status > status2)
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

        String startsAtHourText = String.format("%02d", startsAtHour);
        String startsAtMinuteText = String.format("%02d", startsAtMinute);

        String hourStartText;
        String minuteStartText;

        int count_will_happen = 0;

        int count_will_happen_at_same_time = 1;

        Calendar calendar = Calendar.getInstance();
        String hourNow = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String minuteNow = String.format("%02d", calendar.get(Calendar.MINUTE));

        for (int i = 0; i < list.size(); i++) {

            commitments = true;

            int start_hour = 0, start_minute = 0;
            int end_hour = 0, end_minute = 0;
            int status = 0; // -1 = already happened ; 0 = is happening ; 1 = will happen

            // Activity
            if (list.get(i) instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) list.get(i);
                list_notify.add(new ActivityOfDay(activityServer.getTitle(), activityServer.getMinuteStart(), activityServer.getHourStart(), Constants.ACT,
                        activityServer.getDayStart(),activityServer.getMonthStart(),activityServer.getYearStart()));

                hourStartText = String.format("%02d", activityServer.getHourStart());
                minuteStartText = String.format("%02d", activityServer.getMinuteStart());

                start_hour = activityServer.getHourStart();
                start_minute = activityServer.getMinuteStart();
                end_hour = activityServer.getHourEnd();
                end_minute = activityServer.getMinuteEnd();

                String hour = String.format("%02d", start_hour);
                String minute = String.format("%02d", start_minute);
                String hourEnd = String.format("%02d", end_hour);
                String minuteEnd = String.format("%02d", end_minute);
                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarEnd = Calendar.getInstance();
                calendarStart.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                calendarEnd.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                    if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hourEnd = "23";
                        minuteEnd = "59";
                        activityServer.setHourEnd(23);
                        activityServer.setMinuteEnd(59);
                    } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hour = "00";
                        minute = "00";
                        activityServer.setHourStart(0);
                        activityServer.setMinuteStart(0);
                    } else {
                        hour = "00";
                        minute = "00";
                        hourEnd = "23";
                        minuteEnd = "59";
                        activityServer.setHourStart(0);
                        activityServer.setMinuteStart(0);
                        activityServer.setHourEnd(23);
                        activityServer.setMinuteEnd(59);
                    }
                }

                if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                    status = 0;
                } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                    status = 1;
                } else {
                    status = -1;
                }

                activityServer.setStatus(status);

                if (activityServer.getStatus() == 1) {
                    if (count_will_happen == 0) {
                        startsAtHour = activityServer.getHourStart();
                        startsAtMinute = activityServer.getMinuteStart();
                        startsAtHourText = String.format("%02d", startsAtHour);
                        startsAtMinuteText = String.format("%02d", startsAtMinute);
                    } else {
                        if (isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            startsAtHour = activityServer.getHourStart();
                            startsAtMinute = activityServer.getMinuteStart();
                            startsAtHourText = String.format("%02d", startsAtHour);
                            startsAtMinuteText = String.format("%02d", startsAtMinute);
                        } else if (!isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText) && !isTimeInAfter(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            count_will_happen_at_same_time++;
                        }
                    }
                    count_will_happen++;
                }

            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);
                list_notify.add(new ActivityOfDay(flagServer.getTitle(), flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
                        flagServer.getDayStart(),flagServer.getMonthStart(),flagServer.getYearStart()));

                hourStartText = String.format("%02d", flagServer.getHourStart());
                minuteStartText = String.format("%02d", flagServer.getMinuteStart());

                start_hour = flagServer.getHourStart();
                start_minute = flagServer.getMinuteStart();
                end_hour = flagServer.getHourEnd();
                end_minute = flagServer.getMinuteEnd();

                String hour = String.format("%02d", start_hour);
                String minute = String.format("%02d", start_minute);
                String hourEnd = String.format("%02d", end_hour);
                String minuteEnd = String.format("%02d", end_minute);
                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarEnd = Calendar.getInstance();
                calendarStart.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                calendarEnd.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                if (calendarStart.get(Calendar.DATE) != calendarEnd.get(Calendar.DATE)) {
                    if (calendarStart.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hourEnd = "23";
                        minuteEnd = "59";
                        flagServer.setHourEnd(23);
                        flagServer.setMinuteEnd(59);
                    } else if (calendarEnd.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
                        hour = "00";
                        minute = "00";
                        flagServer.setHourStart(0);
                        flagServer.setMinuteStart(0);
                    } else {
                        hour = "00";
                        minute = "00";
                        hourEnd = "23";
                        minuteEnd = "59";
                        flagServer.setHourStart(0);
                        flagServer.setMinuteStart(0);
                        flagServer.setHourEnd(23);
                        flagServer.setMinuteEnd(59);
                    }
                }

                if (!isTimeInBefore(hour + ":" + minute, hourNow + ":" + minuteNow) && !isTimeInAfter(hourEnd + ":" + minuteEnd, hourNow + ":" + minuteNow)) {
                    status = 0;
                } else if (isTimeInAfter(hourNow + ":" + minuteNow, hourEnd + ":" + minuteEnd)) {
                    status = 1;
                } else {
                    status = -1;
                }

                flagServer.setStatus(status);

                if (flagServer.getStatus() == 1) {
                    if (count_will_happen == 0) {
                        startsAtHour = flagServer.getHourStart();
                        startsAtMinute = flagServer.getMinuteStart();
                        startsAtHourText = String.format("%02d", startsAtHour);
                        startsAtMinuteText = String.format("%02d", startsAtMinute);
                    } else {
                        if (isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            startsAtHour = flagServer.getHourStart();
                            startsAtMinute = flagServer.getMinuteStart();
                            startsAtHourText = String.format("%02d", startsAtHour);
                            startsAtMinuteText = String.format("%02d", startsAtMinute);
                        } else if (!isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText) && !isTimeInAfter(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            count_will_happen_at_same_time++;
                        }
                    }
                    count_will_happen++;
                }

            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);
                list_notify.add(new ActivityOfDay(reminderServer.getTitle(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
                        reminderServer.getDayStart(),reminderServer.getMonthStart(),reminderServer.getYearStart()));

                hourStartText = String.format("%02d", reminderServer.getHourStart());
                minuteStartText = String.format("%02d", reminderServer.getMinuteStart());

                start_hour = reminderServer.getHourStart();
                start_minute = reminderServer.getMinuteStart();

                if (isTimeInBefore(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                    status = -1;
                } else if (isTimeInAfter(hourNow + ":" + minuteNow, start_hour + ":" + start_minute)) {
                    status = 1;
                } else {
                    status = 0;
                }

                reminderServer.setStatus(status);

                if (reminderServer.getStatus() == 1) {
                    if (count_will_happen == 0) {
                        startsAtHour = reminderServer.getHourStart();
                        startsAtMinute = reminderServer.getMinuteStart();
                        startsAtHourText = String.format("%02d", startsAtHour);
                        startsAtMinuteText = String.format("%02d", startsAtMinute);
                    } else {
                        if (isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            startsAtHour = reminderServer.getHourStart();
                            startsAtMinute = reminderServer.getMinuteStart();
                            startsAtHourText = String.format("%02d", startsAtHour);
                            startsAtMinuteText = String.format("%02d", startsAtMinute);
                        } else if (!isTimeInBefore(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText) && !isTimeInAfter(startsAtHourText + ":" + startsAtMinuteText, hourStartText + ":" + minuteStartText)) {
                            count_will_happen_at_same_time++;
                        }
                    }
                    count_will_happen++;
                }
            }
        }

        for (int i = 0; i < list_notify.size(); i++) {
            int j = i;
            int count_same = 0;
            ActivityOfDay activityOfDay = list_notify.get(i);
            ActivityOfDay activityOfDayNext = list_notify.get(j);

            while(activityOfDayNext !=null &&
                    (activityOfDay.getMinuteStart() == activityOfDayNext.getMinuteStart() &&
                            activityOfDay.getHourStart() == activityOfDayNext.getHourStart())){

                j++;
                count_same++;
                if(j < list_notify.size())
                    activityOfDayNext = list_notify.get(j);
                else
                    activityOfDayNext = null;
            }
            activityOfDay.setCommitmentSameHour(count_same);
            i=j-1;
        }

        if (commitments && count_will_happen > 0) {

            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
            Gson gson = new Gson();
            String json = gson.toJson(list_notify);
            editor.putString("ListActDay", json);
            editor.apply();

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            int time_exact;
            long time_to_happen;

            for(int i=0;i<list_notify.size();i++) {
                PersistableBundleCompat extras = new PersistableBundleCompat();
                extras.putInt("position_act", i);

                ActivityOfDay activityOfDay = list_notify.get(i);
                c2.set(Calendar.DAY_OF_MONTH, activityOfDay.getDay());
                c2.set(Calendar.MONTH, activityOfDay.getMonth()-1);
                c2.set(Calendar.YEAR, activityOfDay.getYear());
                c2.set(Calendar.HOUR_OF_DAY, activityOfDay.getHourStart());
                c2.set(Calendar.MINUTE, activityOfDay.getMinuteStart());
                time_exact = (int)(c2.getTimeInMillis()-c1.getTimeInMillis())/(1000*60);
                if(time_exact > 30) {
                    c2.add(Calendar.MINUTE, -30);
                    time_to_happen = c2.getTimeInMillis()-c1.getTimeInMillis();
                    new JobRequest.Builder(NotificationSyncJob.TAG)
                            .setExact(time_to_happen)
                            .setExtras(extras)
                            .setPersisted(true)
                            .build()
                            .schedule();
                }

                if(activityOfDay.getCommitmentSameHour() > 1)
                    i+=activityOfDay.getCommitmentSameHour()-1;
            }
        }
    }

    private void handleErrorToday(Throwable error) {
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

    private boolean isTimeInAfter(String now, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(time);

            return date1.before(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
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
}
