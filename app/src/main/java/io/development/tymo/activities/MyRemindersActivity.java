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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.MyRemindersAdapter;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.models.MyRemindersModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.Utilities;
import io.development.tymo.view_holder.MyRemindersHolder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyRemindersActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, SwipeRefreshLayout.OnRefreshListener, MyRemindersHolder.RefreshLayoutPlansCallback {

    private EasyRecyclerView recyclerView;
    private MyRemindersAdapter adapter;
    private SearchView searchView;

    private int my_reminders_qty;

    private DateFormat dateFormat;

    private Handler handler = new Handler();

    private ImageView mBackButton;
    private TextView m_title, remindersQty;

    private CompositeDisposable mSubscriptions;
    private SharedPreferences mSharedPreferences;

    private List<MyRemindersModel> listMyReminders, listMyRemindersQuery;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_reminders);

        mSubscriptions = new CompositeDisposable();

        dateFormat = new DateFormat(this);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);
        remindersQty = (TextView) findViewById(R.id.remindersQty);
        searchView = (SearchView) findViewById(R.id.searchSelectionView);

        mBackButton.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);
        searchView.setOnQueryTextListener(mOnQueryTextListener);

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);
        findViewById(R.id.horizontalBottomLine).setVisibility(View.GONE);
        findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
        findViewById(R.id.remindersQtyBox).setVisibility(View.GONE);
        findViewById(R.id.searchSelection).setVisibility(View.GONE);

        m_title.setText(getResources().getString(R.string.profile_menu_1));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapterWithProgress(adapter = new MyRemindersAdapter(this, this));

        recyclerView.getSwipeToRefresh().setDistanceToTriggerSync(700);

        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColor(ContextCompat.getColor(this, R.color.deep_purple_400));

        recyclerView.setEmptyView(R.layout.empty_my_reminders);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                MyRemindersModel model = adapter.getItem(positionStart);

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

                ReminderServer reminderServer = (ReminderServer) model.getActivity();
                d = reminderServer.getDayStart();
                m = reminderServer.getMonthStart();
                y = reminderServer.getYearStart();
            }
        });


        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
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

        retrieveMyReminders(email, dateTymo);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            if (adapter.getCount() > 60 || query.equals(""))
                recyclerView.showProgress();
            executeFilter(query);
            return true;
        }
    };

    public void executeFilter(String query) {
        // Load items
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<MyRemindersModel> filteredModelList = filter(listMyReminders, query);
                adapter.clear();
                adapter.addAll(filteredModelList);

                my_reminders_qty = adapter.getCount();

                if (my_reminders_qty == 0) {
                    findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
                    findViewById(R.id.remindersQtyBox).setVisibility(View.GONE);
                    recyclerView.showEmpty();
                } else if (my_reminders_qty == 1) {
                    findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
                    findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
                    findViewById(R.id.remindersQtyBox).setVisibility(View.VISIBLE);
                    findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
                    remindersQty.setText(R.string.my_reminders_qty_one);
                } else {
                    findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
                    findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
                    findViewById(R.id.remindersQtyBox).setVisibility(View.VISIBLE);
                    findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
                    remindersQty.setText(getResources().getString(R.string.my_reminders_qty, my_reminders_qty));
                }

                recyclerView.scrollToPosition(0);
            }
        });
        // Load complete
    }

    private ArrayList<MyRemindersModel> filter(List<MyRemindersModel> models, String query) {
        if (models == null)
            return new ArrayList<>();

        ArrayList<MyRemindersModel> filteredModelList = new ArrayList<>();
        for (MyRemindersModel model : models) {
            String text;
            text = model.getText1().toLowerCase() + " " + model.getText2().toLowerCase();

            if (Utilities.isListContainsQuery(text, query))
                filteredModelList.add(model);

        }

        return filteredModelList;
    }

    @Override
    public void refreshLayout() {

        listMyReminders.clear();
        listMyRemindersQuery.clear();
        listMyReminders.addAll(adapter.getAllData());
        listMyRemindersQuery.addAll(adapter.getAllData());

        my_reminders_qty = adapter.getCount();

        if (my_reminders_qty == 0) {
            recyclerView.setEmptyView(null);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
            findViewById(R.id.remindersQtyBox).setVisibility(View.GONE);
            recyclerView.showEmpty();
        } else if (my_reminders_qty == 1) {
            findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
            findViewById(R.id.remindersQtyBox).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
            remindersQty.setText(R.string.my_reminders_qty_one);
        } else {
            findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
            findViewById(R.id.remindersQtyBox).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
            remindersQty.setText(getResources().getString(R.string.my_reminders_qty, my_reminders_qty));
        }
    }

    private void retrieveMyReminders(String email, DateTymo dateTymo) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getMyReminders(email, dateTymo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        int i;
        listMyReminders = new ArrayList<>();
        listMyRemindersQuery = new ArrayList<>();

        for (i = 0; i < response.getMyCommitReminder().size(); i++) {
            ReminderServer reminderServer = response.getMyCommitReminder().get(i);
            String date = "";

            if (!reminderServer.getDateStartEmpty()) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(reminderServer.getYearStart(),reminderServer.getMonthStart()-1,reminderServer.getDayStart());

                String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                String dayStart = String.format("%02d", reminderServer.getDayStart());
                String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                int yearStart = reminderServer.getYearStart();
                String hourStart = String.format("%02d", reminderServer.getHourStart());
                String minuteStart = String.format("%02d", reminderServer.getMinuteStart());

                if (!reminderServer.getTimeStartEmpty())
                    date = this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);
                else
                    date = this.getResources().getString(R.string.date_format_03, dayOfWeekStart, dayStart, monthStart, yearStart);
            }

            MyRemindersModel myRemindersModel = new MyRemindersModel(reminderServer.getTitle(), reminderServer.getText(), date, reminderServer);
            listMyReminders.add(myRemindersModel);
            listMyRemindersQuery.add(myRemindersModel);

        }

        adapter.clear();
        adapter.addAll(listMyReminders);

        String query = searchView.getQuery().toString();
        if (!query.equals(""))
            executeFilter(query);

        my_reminders_qty = response.getMyCommitReminder().size();

        if (my_reminders_qty == 0) {
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
            findViewById(R.id.remindersQtyBox).setVisibility(View.GONE);
            recyclerView.showEmpty();
        } else if (my_reminders_qty == 1) {
            findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
            findViewById(R.id.remindersQtyBox).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
            remindersQty.setText(R.string.my_reminders_qty_one);
        } else {
            findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
            findViewById(R.id.remindersQtyBox).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
            remindersQty.setText(getResources().getString(R.string.my_reminders_qty, my_reminders_qty));
        }
    }

    private void handleError(Throwable error) {
        if (!Utilities.isDeviceOnline(this))
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
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "onRefresh" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH) + 1;
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

                retrieveMyReminders(mSharedPreferences.getString(Constants.EMAIL, ""), dateTymo);

                recyclerView.showProgress();
            }
        }, 1000);
    }


    @Override
    public void onClick(View v) {
        if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
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

        retrieveMyReminders(email, dateTymo);
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
