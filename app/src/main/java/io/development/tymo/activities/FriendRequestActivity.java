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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.FriendResquestAdapter;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.models.FriendRequestModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FriendRequestActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private EasyRecyclerView recyclerView;
    private FriendResquestAdapter adapter;

    private ImageView mBackButton;
    private TextView m_title;

    private List<FriendRequestModel> listRequest;
    private Handler handler = new Handler();

    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mSubscriptions = new CompositeSubscription();

        findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
        findViewById(R.id.dateBox).setVisibility(View.GONE);
        findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
        findViewById(R.id.searchSelection).setVisibility(View.GONE);
        findViewById(R.id.horizontalBottomLine).setVisibility(View.GONE);
        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);

        mBackButton.setOnClickListener(this);

        m_title.setText(getResources().getString(R.string.pending_requests));

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapterWithProgress(adapter = new FriendResquestAdapter(this));

        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColor(ContextCompat.getColor(this,R.color.deep_purple_400));

        recyclerView.setEmptyView(R.layout.empty_pending_requests);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.PEOPLE);

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        getFriendRequest(email);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);
    }

    private void getFriendRequest(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getFriendRequest(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void visualizeFriendRequest(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().visualizeFriendRequest(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseVisualize,this::handleError));
    }

    private void handleResponseVisualize(Response response) {
    }

    private void handleResponse(Response response) {

        int i;
        listRequest = new ArrayList<>();
        ArrayList<User> users = response.getPeople();
        ArrayList<User> users_accepted = response.getAdms();
        for(i = 0; i < users.size(); i++){

            String location = "";
            if(!users.get(i).getLivesIn().matches(""))
                location = users.get(i).getLivesIn();

            String friends_commun;

            if(users.get(i).getCountCommon() == 0)
                friends_commun = "";
            else if(users.get(i).getCountCommon() == 1)
                friends_commun = getResources().getString(R.string.friend_in_common_one);
            else
                friends_commun = getResources().getString(R.string.friend_in_common_many, users.get(i).getCountCommon());

            FriendRequestModel requestModel = new FriendRequestModel(users.get(i).getName(), location, friends_commun, users.get(i).getEmail(), users.get(i).getPhoto(), users.get(i));
            listRequest.add(requestModel);
        }

        for(i = 0; i < users_accepted.size(); i++){
            User usr = users_accepted.get(i);

            String location = "";
            if(!usr.getLivesIn().matches(""))
                location = usr.getLivesIn();

            FriendRequestModel requestModel = new FriendRequestModel(usr.getName(), location, "accept", usr.getEmail(), usr.getPhoto(), usr);
            listRequest.add(requestModel);
        }

        Collections.sort(listRequest, new Comparator<FriendRequestModel>() {
            @Override
            public int compare(FriendRequestModel c1, FriendRequestModel c2) {
                User usr1 = c1.getUser();
                User usr2 = c2.getUser();


                if(usr1.getDateTimeAccountCreation().compareTo(usr2.getDateTimeAccountCreation()) > 0)
                    return -1;
                else if(usr1.getDateTimeAccountCreation().compareTo(usr2.getDateTimeAccountCreation()) < 0)
                    return 1;
                else
                    return 0;
            }
        });

        adapter.clear();
        adapter.addAll(listRequest);

        visualizeFriendRequest(mSharedPreferences.getString(Constants.EMAIL, ""));
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
                getFriendRequest(mSharedPreferences.getString(Constants.EMAIL, ""));

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "onRefresh" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                recyclerView.showProgress();
            }
        }, 1000);
    }

    @Override
    public void onClick(View v){
        if(v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
