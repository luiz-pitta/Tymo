package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lsjwzh.widget.recyclerviewpager.LoopRecyclerViewPagerAdapter;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPagerAdapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.RecoverAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.Response;
import io.development.tymo.models.CubeModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.MonthYearPickerDialog;
import io.development.tymo.utils.Utilities;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class RecoverActivity extends AppCompatActivity implements android.app.DatePickerDialog.OnDateSetListener, View.OnClickListener, RecoverAdapter.RefreshLayoutAdapterCallback {

    private ImageView mBackButton;
    private TextView monthButton, text1, text2, recoverButton;
    private ImageView mGif, fishHook, bgRecover;
    private SearchView searchView;

    private RecyclerViewPager mRecyclerViewPager;
    private RecoverAdapter adapter;
    private int month_recover, year_recover;

    private boolean activityStartup = true;
    private List<ActivityServer> activityServerList;
    private ActivityServer currentActivity = null;

    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();
    private FirebaseAnalytics mFirebaseAnalytics;

    /*private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (activityStartup) {
                    searchView.clearFocus();
                    activityStartup = false;
                }
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_recover);

        mSubscriptions = new CompositeSubscription();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        searchView = (SearchView) findViewById(R.id.searchView);
        monthButton = (TextView) findViewById(R.id.monthButton);
        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        recoverButton = (TextView) findViewById(R.id.recoverButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        mGif = (ImageView) findViewById(R.id.fishHook);
        bgRecover = (ImageView) findViewById(R.id.bgRecover);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,R.color.deep_purple_400));

        monthButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        recoverButton.setOnClickListener(this);

        int magId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magImage = (ImageView) searchView.findViewById(magId);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        magImage.setVisibility(View.GONE);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextFocusChangeListener(mOnFocusChangeListener);

        Glide.clear(mGif);
        Glide.with(this)
                .load(R.drawable.ic_fish_hook)
                .asBitmap()
                .thumbnail( 0.1f )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mGif);

        Glide.clear(bgRecover);
        Glide.with(this)
                .load(R.drawable.bg_recover)
                .asBitmap()
                .thumbnail( 0.1f )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(bgRecover);

        mGif.bringToFront();

        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, -Utilities.convertDpToPixel(100, this), Utilities.convertDpToPixel(0, this));
        animation.setDuration(3000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        mGif.startAnimation(animation);

        mRecyclerViewPager = (RecyclerViewPager) findViewById(R.id.listRescue);
        mRecyclerViewPager.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        mRecyclerViewPager.setAdapter(adapter = new RecoverAdapter(this));
        mRecyclerViewPager.setHasFixedSize(true);
        mRecyclerViewPager.setLongClickable(true);

        mRecyclerViewPager.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {
                mRecyclerViewPager.getWrapperAdapter().notifyItemChanged(oldPosition);
                adapter.setCurrentPosition(newPosition);

                setCurrent(adapter.getCurrentActivity());
            }
        });

        mRecyclerViewPager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = mRecyclerViewPager.getChildCount();
            }
        });

        monthButton.bringToFront();

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        month_recover = c.get(Calendar.MONTH)+1;
        year_recover = c.get(Calendar.YEAR);

        DateTymo dateTymo = new DateTymo();
        dateTymo.setMonth(month_recover);
        dateTymo.setYear(year_recover);

        getPastActivities(email,dateTymo);

        String m= new SimpleDateFormat("MMMM", getResources().getConfiguration().locale).format(c.getTime().getTime());
        String formattedDate = m.substring(0, 1).toUpperCase() + m.substring(1);
        monthButton.setText(formattedDate + " " + year_recover);

        activityServerList = new ArrayList<>();
        adapter.setCallback(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null);*/
    }

    void refreshItems() {
        /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                mSwipeRefreshLayout.setRefreshing(false);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                DateTymo dateTymo = new DateTymo();
                dateTymo.setMonth(month_recover);
                dateTymo.setYear(year_recover);

                getPastActivities(email,dateTymo);
            }
        }, 500);*/

    }

    public void setProgress(boolean progress) {
        /*if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);*/
    }

    private void getPastActivities(String email, DateTymo dateTymo) {
        /*setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getPastActivities(email, dateTymo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));*/
    }

    private void handleResponse(ArrayList<ActivityServer> activities) {
        /*activityServerList.clear();
        activityServerList.addAll(activities);

        if(activityServerList.size() > 0) {
            Collections.sort(activityServerList, new Comparator<Object>() {
                @Override
                public int compare(Object c1, Object c2) {
                    ActivityServer activityServer;
                    int day = 0, month = 0, year = 0;
                    int day2 = 0, month2 = 0, year2 = 0;

                    if (c1 instanceof ActivityServer) {
                        activityServer = (ActivityServer) c1;
                        day = activityServer.getDayStart();
                        month = activityServer.getMonthStart();
                        year = activityServer.getYearStart();
                    }

                    if (c2 instanceof ActivityServer) {
                        activityServer = (ActivityServer) c2;
                        day2 = activityServer.getDayStart();
                        month2 = activityServer.getMonthStart();
                        year2 = activityServer.getYearStart();
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
                    else
                        return 0;

                }
            });
            adapter.addActivityList(activityServerList);
            findViewById(R.id.itemBox).setVisibility(View.VISIBLE);
            mRecyclerViewPager.setVisibility(View.VISIBLE);
            setCurrent(adapter.getCurrentActivity());
        }else {
            findViewById(R.id.itemBox).setVisibility(View.GONE);
            mRecyclerViewPager.setVisibility(View.GONE);
        }

        setProgress(false);*/
    }

    private void handleError(Throwable error) {
        /*setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*MonthYearPickerDialog dpd = (MonthYearPickerDialog) getFragmentManager().findFragmentByTag("MonthYearPickerDialog");

        if(dpd != null) dpd.setListener(this);*/
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        /*String email = mSharedPreferences.getString(Constants.EMAIL, "");

        month_recover = month;
        year_recover = year;

        DateTymo dateTymo = new DateTymo();
        dateTymo.setMonth(month);
        dateTymo.setYear(year);

        getPastActivities(email,dateTymo);

        Calendar c = Calendar.getInstance();
        c.set(year,month-1, 1);

        String m= new SimpleDateFormat("MMMM", getResources().getConfiguration().locale).format(c.getTime().getTime());
        String formattedDate = m.substring(0, 1).toUpperCase() + m.substring(1);
        monthButton.setText(formattedDate + " " + year);*/
    }

    @Override
    public void onClick(View view) {
        /*if(view == monthButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "monthButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            MonthYearPickerDialog pd = new MonthYearPickerDialog();
            pd.setListener(RecoverActivity.this);
            pd.show(getFragmentManager(), "MonthYearPickerDialog");
        }else if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        }
        else if(view == recoverButton && currentActivity != null){

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "recoverButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(RecoverActivity.this, AddActivity.class);
            myIntent.putExtra("act_recover", new ActivityWrapper(currentActivity));
            startActivity(myIntent);
            finish();
        }*/
    }

    @Override
    public void setCurrent(ActivityServer activityServer) {
        /*text1.setText(activityServer.getTitle());

        if(activityServer.getDescription() == null || activityServer.getDescription().matches("")) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
            String day_text = new SimpleDateFormat("EE", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String day_text2 = activityServer.getDayStart() < 10 ? "0" + activityServer.getDayStart() : String.valueOf(activityServer.getDayStart());
            String month_text = new SimpleDateFormat("MMM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String hour = activityServer.getHourStart() < 10 ? "0" + activityServer.getHourStart() : String.valueOf(activityServer.getHourStart());
            String minute = activityServer.getMinuteStart() < 10 ? "0" + activityServer.getMinuteStart() : String.valueOf(activityServer.getMinuteStart());

            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());
            String day_text_end = new SimpleDateFormat("EE", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String day_text2_end = activityServer.getDayEnd() < 10 ? "0" + activityServer.getDayEnd() : String.valueOf(activityServer.getDayEnd());
            String month_text_end = new SimpleDateFormat("MMM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String hour_end = activityServer.getHourEnd() < 10 ? "0" + activityServer.getHourEnd() : String.valueOf(activityServer.getHourEnd());
            String minute_end = activityServer.getMinuteEnd() < 10 ? "0" + activityServer.getMinuteEnd() : String.valueOf(activityServer.getMinuteEnd());

            String date;

            if (activityServer.getDayStart() == activityServer.getDayEnd())
                date = day_text + ", " + day_text2 + " " + month_text + " " + activityServer.getYearStart()
                        + " " + getResources().getString(R.string.at_time) + " " + hour + ":" + minute
                        + " - " + hour_end + ":" + minute_end;
            else
                date = day_text + ", " + day_text2 + " " + month_text + " " + activityServer.getYearStart()
                        + " " + getResources().getString(R.string.at_time) + " " + hour + ":" + minute + "\n"
                        + day_text_end + ", " + day_text2_end + " " + month_text_end + " " + activityServer.getYearEnd()
                        + " " + getResources().getString(R.string.at_time) + " " + hour_end + ":" + minute_end;

            text2.setText(date);
        }
        else
            text2.setText(activityServer.getDescription());

        currentActivity = activityServer;*/
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}
