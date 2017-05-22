package io.development.tymo.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        setContentView(R.layout.activity_list);

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

        retrieveInviteRequest(email, dateTymo);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, getClass().getSimpleName(), null /* class override */);
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
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "onRefresh" + getClass().getSimpleName());
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
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

                retrieveInviteRequest(mSharedPreferences.getString(Constants.EMAIL, ""), dateTymo);

                recyclerView.showProgress();
            }
        }, 1000);
    }


    @Override
    public void onClick(View v){
        if(v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + getClass().getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }
}
