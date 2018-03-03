package io.development.tymo.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.BuildConfig;
import io.development.tymo.R;
import io.development.tymo.adapters.ViewPagerAdapter;
import io.development.tymo.fragments.FitPeopleFragment;
import io.development.tymo.fragments.InvitedPeopleFragment;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.AppInfoWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NotificationsActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private TextView m_title, friendshipRequestsQty, invitationsQty, updatesQty;
    private RelativeLayout friendshipRequestsBox, invitationsBox, updatesBox;
    private ImageView mBackButton;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CompositeDisposable mSubscriptions;
    private boolean noInternet = true;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();
    private User user;
    private View progressBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mSubscriptions = new CompositeDisposable();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        m_title = (TextView) findViewById(R.id.text);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        friendshipRequestsBox = (RelativeLayout) findViewById(R.id.friendshipRequestsBox);
        friendshipRequestsQty = (TextView) findViewById(R.id.friendshipRequestsQty);
        invitationsBox = (RelativeLayout) findViewById(R.id.invitationsBox);
        invitationsQty = (TextView) findViewById(R.id.invitationsQty);
        updatesBox = (RelativeLayout) findViewById(R.id.updatesBox);
        updatesQty = (TextView) findViewById(R.id.updatesQty);
        progressBox = findViewById(R.id.progressBox);

        m_title.setText(getResources().getString(R.string.profile_menu_2));

        mBackButton.setOnClickListener(this);
        friendshipRequestsBox.setOnClickListener(this);
        invitationsBox.setOnClickListener(this);
        updatesBox.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.deep_purple_400));

        setProgress(true);
    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLayout();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refresh" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }, 500);

        // Load complete
    }

    private void getProfileMainInformation(Query query) {
        mSubscriptions.add(NetworkUtil.getRetrofit().getProfileMain(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {

        Calendar c = Calendar.getInstance();
        ArrayList<Object> list = new ArrayList<>();
        user = response.getUser();

        noInternet = false;

        friendshipRequestsQty.setText(String.valueOf(response.getNumberFriendRequest()));
        invitationsQty.setText(String.valueOf(response.getNumberInvitationRequest()));

        if (response.getNumberFriendRequest() > 0) {
            friendshipRequestsQty.setVisibility(View.VISIBLE);
        } else {
            friendshipRequestsQty.setVisibility(View.GONE);
        }

        if (response.getNumberInvitationRequest() > 0) {
            invitationsQty.setVisibility(View.VISIBLE);
        } else {
            invitationsQty.setVisibility(View.GONE);
        }

        updatesQty.setVisibility(View.GONE);

        setProgress(false);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void handleError(Throwable error) {
        setProgress(false);
        noInternet = true;
        mSwipeRefreshLayout.setRefreshing(false);
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    public void updateLayout() {

        SharedPreferences mSharedPreferences = this.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        Query query = new Query();
        query.setEmail(email);
        query.setDay(day);
        query.setMonth(month);
        query.setYear(year);
        query.setHourStart(hour);
        query.setMinuteStart(minute);

        getProfileMainInformation(query);
    }

    public void setProgress(boolean progress) {
        if (progress)
            progressBox.setVisibility(View.VISIBLE);
        else
            progressBox.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (view == friendshipRequestsBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "friendshipRequestsBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(this, FriendRequestActivity.class));
        } else if (view == invitationsBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "invitationsBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(this, InviteActivity.class));
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        updateLayout();
    }
}
