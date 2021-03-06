package io.development.tymo.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.adapters.PlansFragmentAdapter;
import io.development.tymo.fragments.CommitmentFragment;
import io.development.tymo.fragments.FreeFragment;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.BgProfileServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.FriendRequest;
import io.development.tymo.model_server.Plans;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.models.WeekModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FriendProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener,
        View.OnTouchListener, CreatePopUpDialogFragment.RefreshLayoutPlansCallback {

    private int mBackButtonColor, mBackButtonColorPressed;
    private int linePaint = -1;

    private ImageView mBackButton;
    private TextView friendshipRequestsText, progressText, btnCompare, aboutText, contactsText;
    private ImageView calendarIcon;
    private ImageView profilePhoto, friendshipRequestsIcon, aboutIcon, contactsIcon;
    private LinearLayout mDateBox, friendshipRequestsBox, contactsBox, aboutBox;
    private RelativeLayout dateCompareBox, profilePhotoBox;
    private NestedScrollView scrollView;
    private ProgressBar progressIcon;
    private DateFormat dateFormat;
    private Rect rect;
    private boolean prived = false;

    private Calendar currentTime;
    private static int currentSecond, currentMinute, currentHour;
    private ArrayList<BgProfileServer> bgProfile = new ArrayList<>();
    private ImageView backgroundProfile;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private TextView commitmentsButton;
    private TextView freeTimeButton;
    private LinearLayout compareButton;
    private TextView dateTextWeek, dateTextMonth, profileName, profileDescription;

    private ImageView previousWeek, nextWeek;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();
    private int day_start, month_start, year_start;
    private List<WeekModel> listPlans = new ArrayList<>();

    private String email_friend;
    private User user;

    private GestureDetector gestureDetector;

    private FragmentNavigator mNavigator;

    private CompositeDisposable mSubscriptions;
    private SharedPreferences mSharedPreferences;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friend);

        dateFormat = new DateFormat(this);

        mSubscriptions = new CompositeDisposable();
        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email_friend = getIntent().getStringExtra("friend_email");

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        settings = getSharedPreferences(Utilities.PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();

        backgroundProfile = (ImageView) findViewById(R.id.backgroundProfile);
        scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        profileName = (TextView) findViewById(R.id.profileName);
        profileDescription = (TextView) findViewById(R.id.profileDescription);
        commitmentsButton = (TextView) findViewById(R.id.commitmentsButton);
        freeTimeButton = (TextView) findViewById(R.id.freeTimeButton);
        compareButton = (LinearLayout) findViewById(R.id.compareButton);
        btnCompare = (TextView) findViewById(R.id.btnCompare);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        mDateBox = (LinearLayout) findViewById(R.id.dateBox);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        friendshipRequestsBox = (LinearLayout) findViewById(R.id.friendshipRequestsBox);
        friendshipRequestsText = (TextView) findViewById(R.id.friendshipRequestsText);
        friendshipRequestsIcon = (ImageView) findViewById(R.id.friendshipRequestsIcon);
        progressText = (TextView) findViewById(R.id.progressText);
        progressIcon = (ProgressBar) findViewById(R.id.progressIcon);
        contactsBox = (LinearLayout) findViewById(R.id.contactsBox);
        contactsText = (TextView) findViewById(R.id.contactsText);
        contactsIcon = (ImageView) findViewById(R.id.contactsIcon);
        aboutBox = (LinearLayout) findViewById(R.id.aboutBox);
        aboutText = (TextView) findViewById(R.id.aboutText);
        aboutIcon = (ImageView) findViewById(R.id.aboutIcon);
        dateTextMonth = (TextView) findViewById(R.id.dateMonthYear);
        dateTextWeek = (TextView) findViewById(R.id.dateWeek);
        previousWeek = (ImageView) findViewById(R.id.previousWeek);
        nextWeek = (ImageView) findViewById(R.id.nextWeek);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        dateCompareBox = (RelativeLayout) findViewById(R.id.dateCompareBox);
        calendarIcon = (ImageView) findViewById(R.id.calendarIcon);
        profilePhotoBox = (RelativeLayout) findViewById(R.id.profilePhotoBox);

        mSwipeRefreshLayout.setDistanceToTriggerSync(700);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.deep_purple_400));

        mBackButton.setOnClickListener(this);
        mDateBox.setOnClickListener(this);
        commitmentsButton.setOnClickListener(this);
        freeTimeButton.setOnClickListener(this);
        compareButton.setOnClickListener(this);
        profilePhotoBox.setOnClickListener(this);
        compareButton.setOnTouchListener(this);
        friendshipRequestsBox.setOnClickListener(this);
        contactsBox.setOnClickListener(this);
        aboutBox.setOnClickListener(this);
        dateTextWeek.setOnClickListener(this);
        previousWeek.setOnClickListener(this);
        nextWeek.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);
        mDateBox.setOnTouchListener(this);
        previousWeek.setOnTouchListener(this);
        nextWeek.setOnTouchListener(this);
        dateTextWeek.setOnTouchListener(this);
        friendshipRequestsBox.setOnTouchListener(this);
        contactsBox.setOnTouchListener(this);
        aboutBox.setOnTouchListener(this);

        mNavigator = new FragmentNavigator(getFragmentManager(), new PlansFragmentAdapter(), R.id.container);
        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.onCreate(savedInstanceState);

        setCurrentTab(mNavigator.getCurrentPosition());
        friendshipRequestsIcon.setTag(R.drawable.ic_person_add);

        commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left_pressed);
        freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right);
        commitmentsButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        freeTimeButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));

        new Actor.Builder(SpringSystem.create(), profilePhotoBox)
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

        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }

        cal.add(Calendar.DAY_OF_WEEK, -15);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        cal.add(Calendar.DAY_OF_WEEK, 21);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int month2 = cal.get(Calendar.MONTH) + 1;
        int year2 = cal.get(Calendar.YEAR);

        int d1f = day;

        if (month != month2) {
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);
            cal2.set(Calendar.DAY_OF_WEEK, cal2.getFirstDayOfWeek());
            cal2.add(Calendar.DAY_OF_WEEK, 1);
            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while (day3 != 1) {
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();
        plans.setEmail(email_friend);
        plans.setA(year);
        plans.setA2(year2);
        plans.setD1(day);
        plans.setD2(day2);
        plans.setM(month);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        plans.addEmails(mSharedPreferences.getString(Constants.EMAIL, ""));

        int day_start_temp = cal.get(Calendar.DAY_OF_MONTH);
        int month_start_temp = cal.get(Calendar.MONTH) + 1;

        cal.add(Calendar.DAY_OF_WEEK, -6);
        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH) + 1;
        year_start = cal.get(Calendar.YEAR);

        String month_text_start = dateFormat.formatMonthShort(month_start);
        String month_text_start_temp = dateFormat.formatMonthShort(month_start_temp);
        dateTextWeek.setText(getResources().getString(R.string.date_format_02, String.format("%02d", day_start), month_text_start, String.format("%02d", day_start_temp), month_text_start_temp));

        String month_text = dateFormat.formatMonth(cal.get(Calendar.MONTH) + 1);
        dateTextMonth.setText(getResources().getString(R.string.date_format_01, month_text, year_start));

        getBgProfile();

        setPlans(plans, true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private void setBackgroundProfile() {
        String timeStart, timeEnd;
        String currentTime = String.format("%02d", currentHour) + ":" + String.format("%02d", currentMinute);
        int period = 0;
        String urlBg = "";

        for (int i = 0; i < bgProfile.size(); i++) {
            timeStart = String.format("%02d", bgProfile.get(i).getHourStart()) + ":" + String.format("%02d", bgProfile.get(i).getMinuteStart());
            timeEnd = String.format("%02d", bgProfile.get(i).getHourEnd()) + ":" + String.format("%02d", bgProfile.get(i).getMinuteEnd());

            if (isTimeInBetween(currentTime, timeStart, timeEnd)) {
                period = bgProfile.get(i).getPeriod();
                urlBg = bgProfile.get(i).getUrlBg();
            }
        }

        if (bgProfile.size() > 0) {

            if (period == 3) {
                mBackButtonColor = getResources().getColor(R.color.grey_900);
                mBackButtonColorPressed = getResources().getColor(R.color.grey_600);
                mBackButton.setColorFilter(getResources().getColor(R.color.grey_900));
                profileName.setTextColor(getResources().getColor(R.color.grey_900));
                profileDescription.setTextColor(getResources().getColor(R.color.grey_900));
            } else if (period == 2 || period == 4) {
                mBackButtonColor = getResources().getColor(R.color.white);
                mBackButtonColorPressed = getResources().getColor(R.color.grey_300);
                mBackButton.setColorFilter(getResources().getColor(R.color.white));
                profileName.setTextColor(getResources().getColor(R.color.white));
                profileDescription.setTextColor(getResources().getColor(R.color.white));
            } else {
                mBackButtonColor = getResources().getColor(R.color.white);
                mBackButtonColorPressed = getResources().getColor(R.color.grey_300);
                mBackButton.setColorFilter(getResources().getColor(R.color.white));
                profileName.setTextColor(getResources().getColor(R.color.white));
                profileDescription.setTextColor(getResources().getColor(R.color.white));
            }

            Glide.clear(backgroundProfile);
            Glide.with(this)
                    .load(urlBg)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Drawable drawable = new BitmapDrawable(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                backgroundProfile.setBackground(drawable);
                            } else
                                backgroundProfile.setBackgroundDrawable(drawable);
                        }
                    });
        }
    }

    private boolean isTimeInBetween(String now, String timeStart, String timeEnd) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(timeStart);
            Date date3 = sdf.parse(timeEnd);

            return date1.after(date2) && date1.before(date3);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void isTimeToChangeBackground() {
        currentTime = Calendar.getInstance();
        currentSecond = currentTime.get(Calendar.SECOND);
        currentMinute = currentTime.get(Calendar.MINUTE);
        currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        setBackgroundProfile();
    }

    private void getBgProfile() {
        mSubscriptions.add(NetworkUtil.getRetrofit().getBgProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseBgProfile, this::handleErrorBgProfile));
    }

    private void handleResponseBgProfile(ArrayList<BgProfileServer> bgProfile) {
        this.bgProfile = new ArrayList<>();
        this.bgProfile.addAll(bgProfile);
        isTimeToChangeBackground();
    }

    private void handleErrorBgProfile(Throwable error) {
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                mSwipeRefreshLayout.setRefreshing(false);
                updatePlansFriend(year_start, month_start - 1, day_start, true, false);
            }
        }, 500);

        // Load complete
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressScreen).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressScreen).setVisibility(View.GONE);
    }

    public NestedScrollView getScrollView() {
        return scrollView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == compareButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                btnCompare.setBackgroundResource(R.drawable.btn_compare);
                btnCompare.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                btnCompare.setBackgroundResource(R.drawable.btn_compare_pressed);
                btnCompare.setTextColor(ContextCompat.getColor(this, R.color.white));
            }

            if (gestureDetector.onTouchEvent(event)) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "compareButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Intent intent = new Intent(this, CompareActivity.class);
                intent.putExtra("email_compare_friend", new UserWrapper(user));
                startActivity(intent);

                btnCompare.setBackgroundResource(R.drawable.btn_compare);
                btnCompare.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            }

        } else if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(mBackButtonColor);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(mBackButtonColorPressed);
            }
        } else if (view == mDateBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                dateTextMonth.setTextColor(ContextCompat.getColor(this, R.color.grey_900));
                calendarIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_900));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dateTextMonth.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                calendarIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            }
        } else if (view == nextWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                nextWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                nextWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_200));
            }
        } else if (view == previousWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                previousWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                previousWeek.setColorFilter(ContextCompat.getColor(this, R.color.grey_200));
            }
        } else if (view == dateTextWeek) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                dateTextWeek.setTextColor(ContextCompat.getColor(this, R.color.grey_500));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dateTextWeek.setTextColor(ContextCompat.getColor(this, R.color.grey_300));
            }
        } else if (view == friendshipRequestsBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                friendshipRequestsBox.setBackgroundColor(0);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                friendshipRequestsBox.setBackgroundColor(ContextCompat.getColor(this, R.color.black_opacity_20));
            }
        } else if (view == contactsBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                contactsBox.setBackgroundColor(0);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                contactsBox.setBackgroundColor(ContextCompat.getColor(this, R.color.black_opacity_20));
            }
        } else if (view == aboutBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                aboutBox.setBackgroundColor(0);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                aboutBox.setBackgroundColor(ContextCompat.getColor(this, R.color.black_opacity_20));
            }
        }

        return false;
    }

    public List<WeekModel> getListPlans() {
        return listPlans;
    }

    private void setPlans(Plans plans, boolean progress) {
        setProgress(progress);
        CommitmentFragment commitmentFragment = (CommitmentFragment) mNavigator.getFragment(0);
        if (commitmentFragment != null)
            commitmentFragment.showProgressCommitment();

        FreeFragment freeFragment = (FreeFragment) mNavigator.getFragment(1);
        if (freeFragment != null)
            freeFragment.showProgressFree();

        mSubscriptions.add(NetworkUtil.getRetrofit().getFriendsPlans(plans)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void setProgressFriendRequest(boolean progress) {
        if (progress) {
            progressIcon.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.GONE);
            friendshipRequestsIcon.setVisibility(View.GONE);
            friendshipRequestsText.setVisibility(View.GONE);
        } else {
            progressIcon.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            friendshipRequestsIcon.setVisibility(View.VISIBLE);
            friendshipRequestsText.setVisibility(View.VISIBLE);
        }
    }

    private void cancelFriendRequest(FriendRequest friendRequest) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().cancelFriendRequest(friendRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFriendRequest, this::handleError));
    }

    private void updateFriendRequest(FriendRequest friendRequest) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateFriendRequest(friendRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFriendRequest, this::handleError));
    }

    private void handleFriendRequest(Response response) {
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //RELATIONSHIP_UPDATED_SUCCESSFULLY, WITHOUT_NOTIFICATION e REQUEST_TO_ADD_ACCEPTED
        Calendar cal = Calendar.getInstance();

        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH);
        int y = cal.get(Calendar.YEAR);

        updatePlansFriend(y, m, d, false, false);
    }

    private void sendFriendRequest(String email, User user) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerFriendRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFriendRequest, this::handleError));
    }

    private void handleResponseFriendRequest(Response response) {
        friendshipRequestsIcon.setImageResource(R.drawable.ic_person_cancel);
        friendshipRequestsText.setText(getResources().getString(R.string.cancel));
        friendshipRequestsIcon.setTag(R.drawable.ic_person_cancel);
        setProgressFriendRequest(false);
        friendshipRequestsBox.setOnClickListener(this);
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //SUCCESSFULLY e WITHOUT_NOTIFICATION
    }

    private void handleResponse(Response response) {

        user = response.getUser();

        profileName.setText(user.getName());

        if (!user.getDescription().matches(""))
            profileDescription.setText(user.getDescription());
        else
            profileDescription.setVisibility(View.GONE);

        isTimeToChangeBackground();

        if (user.getCountKnows() > 0) {
            if (user.getFromFacebook() && user.isFacebookMessengerEnable()) {
                //facebook messenger
            }
            friendshipRequestsText.setText(getResources().getString(R.string.profile_friend_menu_friends));
            friendshipRequestsIcon.setImageResource(R.drawable.ic_person_check);
            friendshipRequestsIcon.setTag(R.drawable.ic_person_check);
        } else if (user.getCountAskAdd() > 0) {
            if (user.getCountCommon() == 0) {
                friendshipRequestsText.setText(getResources().getString(R.string.cancel));
                friendshipRequestsIcon.setImageResource(R.drawable.ic_person_cancel);
                friendshipRequestsIcon.setTag(R.drawable.ic_person_cancel);
            } else {
                friendshipRequestsText.setText(getResources().getString(R.string.response_waiting));
                friendshipRequestsIcon.setImageResource(R.drawable.ic_person_waiting);
                friendshipRequestsIcon.setTag(R.drawable.ic_person_waiting);
            }
        } else {
            friendshipRequestsText.setText(getResources().getString(R.string.add));
            friendshipRequestsIcon.setImageResource(R.drawable.ic_person_add);
            friendshipRequestsIcon.setTag(R.drawable.ic_person_add);
        }

        if (user.getCountKnows() == 0 && user.getPrivacy() == 1) {
            prived = true;
            dateCompareBox.setVisibility(View.GONE);
            findViewById(R.id.nextPreviousBox).setVisibility(View.GONE);
            findViewById(R.id.commitmentsFreeTimeBox).setVisibility(View.GONE);
        } else {
            prived = false;
            findViewById(R.id.nextPreviousBox).setVisibility(View.VISIBLE);
            findViewById(R.id.commitmentsFreeTimeBox).setVisibility(View.VISIBLE);
            contactsBox.setVisibility(View.VISIBLE);
            aboutBox.setVisibility(View.VISIBLE);
        }

        if (!user.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(this)
                    .load(user.getPhoto())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

        listPlans.clear();

        if (!prived) {

            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DATE, -1);
            }

            int day, month, year;

            for (int i = 0; i < 7; i++) {
                day = cal.get(Calendar.DAY_OF_MONTH);
                month = cal.get(Calendar.MONTH) + 1;
                year = cal.get(Calendar.YEAR);

                String day_text = dateFormat.formatDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
                String month_text = dateFormat.formatMonthShort(cal.get(Calendar.MONTH) + 1);

                WeekModel weekModel = new WeekModel(String.format("%02d", day), day_text, month_text, day, month, year, false);

                int j;
                for (j = 0; j < response.getMyCommitAct().size(); j++) {
                    ActivityServer myCommitActivity = response.getMyCommitAct().get(j);
                    ActivityServer activity = new ActivityServer(myCommitActivity);

                    Calendar calendar = Calendar.getInstance();
                    Calendar calendar2 = Calendar.getInstance();
                    calendar.set(activity.getYearStart(), activity.getMonthStart() - 1, activity.getDayStart(), activity.getHourStart(), activity.getMinuteStart());
                    calendar2.set(activity.getYearEnd(), activity.getMonthEnd() - 1, activity.getDayEnd(), activity.getHourEnd(), activity.getMinuteEnd());

                    int qty = activity.getRepeatQty() < 0 ? 0 : activity.getRepeatQty();

                    for (int k = 0; k <= qty; k++) {
                        ActivityServer activityServer = new ActivityServer(myCommitActivity);

                        if (k > 0) {
                            switch (activityServer.getRepeatType()) {
                                case Constants.DAILY:
                                    calendar.add(Calendar.DAY_OF_WEEK, 1);
                                    calendar2.add(Calendar.DAY_OF_WEEK, 1);
                                    break;
                                case Constants.WEEKLY:
                                    calendar.add(Calendar.DAY_OF_WEEK, 7);
                                    calendar2.add(Calendar.DAY_OF_WEEK, 7);
                                    break;
                                case Constants.MONTHLY:
                                    calendar.add(Calendar.MONTH, 1);
                                    calendar2.add(Calendar.MONTH, 1);
                                    break;
                                default:
                                    break;
                            }
                        } else {

                        }

                        long dateTimeStart = calendar.getTimeInMillis();
                        int dayStart = calendar.get(Calendar.DAY_OF_MONTH);
                        int monthStart = calendar.get(Calendar.MONTH) + 1;
                        int yearStart = calendar.get(Calendar.YEAR);
                        long dateTimeEnd = calendar2.getTimeInMillis();
                        int dayEnd = calendar2.get(Calendar.DAY_OF_MONTH);
                        int monthEnd = calendar2.get(Calendar.MONTH) + 1;
                        int yearEnd = calendar2.get(Calendar.YEAR);

                        if (Utilities.isActivityInRange(yearStart, yearEnd, dayStart, monthStart, dayEnd, monthEnd, day)) {
                            boolean start = Utilities.isStartedFinishedToday(day, dayStart);
                            boolean finish = Utilities.isStartedFinishedToday(day, dayEnd);
                            if (!start && finish) {
                                activityServer.setTimeStartEmptyCard(true);
                                activityServer.setTimeEndEmptyCard(false);
                                activityServer.setMinuteCard(0);
                                activityServer.setHourCard(0);
                                activityServer.setMinuteEndCard(activityServer.getMinuteEnd());
                                activityServer.setHourEndCard(activityServer.getHourEnd());
                            } else if (start && !finish) {
                                activityServer.setTimeStartEmptyCard(false);
                                activityServer.setTimeEndEmptyCard(true);
                                activityServer.setMinuteCard(activityServer.getMinuteStart());
                                activityServer.setHourCard(activityServer.getHourStart());
                                activityServer.setMinuteEndCard(59);
                                activityServer.setHourEndCard(23);
                            } else if (!start && !finish) {
                                activityServer.setTimeStartEmptyCard(true);
                                activityServer.setTimeEndEmptyCard(true);
                                activityServer.setMinuteCard(0);
                                activityServer.setHourCard(0);
                                activityServer.setMinuteEndCard(59);
                                activityServer.setHourEndCard(23);
                            } else {
                                activityServer.setTimeStartEmptyCard(false);
                                activityServer.setTimeEndEmptyCard(false);
                                activityServer.setMinuteCard(activityServer.getMinuteStart());
                                activityServer.setHourCard(activityServer.getHourStart());
                                activityServer.setMinuteEndCard(activityServer.getMinuteEnd());
                                activityServer.setHourEndCard(activityServer.getHourEnd());
                            }

                            if (user.getCountKnows() > 0)
                                activityServer.setKnowCreator(1);
                            else
                                activityServer.setKnowCreator(0);

                            weekModel.addPlans(activityServer);
                        }
                    }
                }
                for (j = 0; j < response.getMyCommitFlag().size(); j++) {
                    FlagServer myCommitFlag = response.getMyCommitFlag().get(j);
                    FlagServer flag = new FlagServer(myCommitFlag);

                    Calendar calendar = Calendar.getInstance();
                    Calendar calendar2 = Calendar.getInstance();
                    calendar.set(flag.getYearStart(), flag.getMonthStart() - 1, flag.getDayStart(), flag.getHourStart(), flag.getMinuteStart());
                    calendar2.set(flag.getYearEnd(), flag.getMonthEnd() - 1, flag.getDayEnd(), flag.getHourEnd(), flag.getMinuteEnd());

                    int qty = flag.getRepeatQty() < 0 ? 0 : flag.getRepeatQty();

                    for (int k = 0; k <= qty; k++) {
                        FlagServer flagServer = new FlagServer(myCommitFlag);

                        if (k > 0) {
                            switch (flagServer.getRepeatType()) {
                                case Constants.DAILY:
                                    calendar.add(Calendar.DAY_OF_WEEK, 1);
                                    calendar2.add(Calendar.DAY_OF_WEEK, 1);
                                    break;
                                case Constants.WEEKLY:
                                    calendar.add(Calendar.DAY_OF_WEEK, 7);
                                    calendar2.add(Calendar.DAY_OF_WEEK, 7);
                                    break;
                                case Constants.MONTHLY:
                                    calendar.add(Calendar.MONTH, 1);
                                    calendar2.add(Calendar.MONTH, 1);
                                    break;
                                default:
                                    break;
                            }
                        } else {

                        }

                        long dateTimeStart = calendar.getTimeInMillis();
                        int dayStart = calendar.get(Calendar.DAY_OF_MONTH);
                        int monthStart = calendar.get(Calendar.MONTH) + 1;
                        int yearStart = calendar.get(Calendar.YEAR);
                        long dateTimeEnd = calendar2.getTimeInMillis();
                        int dayEnd = calendar2.get(Calendar.DAY_OF_MONTH);
                        int monthEnd = calendar2.get(Calendar.MONTH) + 1;
                        int yearEnd = calendar2.get(Calendar.YEAR);

                        if (Utilities.isActivityInRange(yearStart, yearEnd, dayStart, monthStart, dayEnd, monthEnd, day)) {
                            boolean start = Utilities.isStartedFinishedToday(day, dayStart);
                            boolean finish = Utilities.isStartedFinishedToday(day, dayEnd);
                            if (!start && finish) {
                                flagServer.setTimeStartEmptyCard(true);
                                flagServer.setTimeEndEmptyCard(false);
                                flagServer.setMinuteCard(0);
                                flagServer.setHourCard(0);
                                flagServer.setMinuteEndCard(flagServer.getMinuteEnd());
                                flagServer.setHourEndCard(flagServer.getHourEnd());
                            } else if (start && !finish) {
                                flagServer.setTimeStartEmptyCard(false);
                                flagServer.setTimeEndEmptyCard(true);
                                flagServer.setMinuteCard(flagServer.getMinuteStart());
                                flagServer.setHourCard(flagServer.getHourStart());
                                flagServer.setMinuteEndCard(59);
                                flagServer.setHourEndCard(23);
                            } else if (!start && !finish) {
                                flagServer.setTimeStartEmptyCard(true);
                                flagServer.setTimeEndEmptyCard(true);
                                flagServer.setMinuteCard(0);
                                flagServer.setHourCard(0);
                                flagServer.setMinuteEndCard(59);
                                flagServer.setHourEndCard(23);
                            } else {
                                flagServer.setTimeStartEmptyCard(false);
                                flagServer.setTimeEndEmptyCard(false);
                                flagServer.setMinuteCard(flagServer.getMinuteStart());
                                flagServer.setHourCard(flagServer.getHourStart());
                                flagServer.setMinuteEndCard(flagServer.getMinuteEnd());
                                flagServer.setHourEndCard(flagServer.getHourEnd());
                            }

                            if (user.getCountKnows() > 0)
                                flagServer.setKnowCreator(1);
                            else
                                flagServer.setKnowCreator(0);

                            weekModel.addPlans(flagServer);
                        }
                    }
                }

                Collections.sort(weekModel.getActivities(), new Comparator<Object>() {
                    @Override
                    public int compare(Object c1, Object c2) {
                        ActivityServer activityServer;
                        FlagServer flagServer;
                        ReminderServer reminderServer;
                        int start_hour = 0, start_minute = 0;
                        int start_hour2 = 0, start_minute2 = 0;
                        int end_hour = 0, end_minute = 0;
                        int end_hour2 = 0, end_minute2 = 0;

                        if (c1 instanceof ActivityServer) {
                            activityServer = (ActivityServer) c1;
                            start_hour = activityServer.getHourCard();
                            start_minute = activityServer.getMinuteCard();
                            end_hour = activityServer.getHourEndCard();
                            end_minute = activityServer.getMinuteEndCard();
                        } else if (c1 instanceof FlagServer) {
                            flagServer = (FlagServer) c1;
                            start_hour = flagServer.getHourCard();
                            start_minute = flagServer.getMinuteCard();
                            end_hour = flagServer.getHourEndCard();
                            end_minute = flagServer.getMinuteEndCard();
                        }
                        if (c2 instanceof ActivityServer) {
                            activityServer = (ActivityServer) c2;
                            start_hour2 = activityServer.getHourCard();
                            start_minute2 = activityServer.getMinuteCard();
                            end_hour2 = activityServer.getHourEndCard();
                            end_minute2 = activityServer.getMinuteEndCard();
                        } else if (c2 instanceof FlagServer) {
                            flagServer = (FlagServer) c2;
                            start_hour2 = flagServer.getHourCard();
                            start_minute2 = flagServer.getMinuteCard();
                            end_hour2 = flagServer.getHourEndCard();
                            end_minute2 = flagServer.getMinuteEndCard();
                        }

                        if (start_hour < start_hour2)
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

                ArrayList<Object> arrayList = new ArrayList<>();

                if (weekModel.getActivities().size() == 0) {
                    FreeTimeServer freeTimeServer = new FreeTimeServer();
                    freeTimeServer.setHourStart(0);
                    freeTimeServer.setMinuteStart(0);
                    freeTimeServer.setHourEnd(23);
                    freeTimeServer.setMinuteEnd(59);

                    weekModel.addFree(freeTimeServer);
                } else {
                    int start_hour = 0, start_minute = 0;
                    int end_hour = 0, end_minute = 0;
                    int free_hour = 0, free_minute = 0;

                    for (j = 0; j < weekModel.getActivities().size(); j++) {
                        Object object = weekModel.getActivities().get(j);
                        ActivityServer activityServer;
                        FlagServer flagServer;

                        if (object instanceof ActivityServer) {
                            activityServer = (ActivityServer) object;
                            start_hour = activityServer.getHourCard();
                            start_minute = activityServer.getMinuteCard();
                            end_hour = activityServer.getHourEndCard();
                            end_minute = activityServer.getMinuteEndCard();
                        } else if (object instanceof FlagServer) {
                            flagServer = (FlagServer) object;
                            start_hour = flagServer.getHourCard();
                            start_minute = flagServer.getMinuteCard();
                            end_hour = flagServer.getHourEndCard();
                            end_minute = flagServer.getMinuteEndCard();
                        }

                        FreeTimeServer freeTimeServer = new FreeTimeServer();

                        if ((free_hour < start_hour) || (free_hour == start_hour && free_minute < start_minute)) {
                            freeTimeServer.setHourStart(free_hour);
                            freeTimeServer.setMinuteStart(free_minute);
                            freeTimeServer.setHourEnd(start_hour);
                            freeTimeServer.setMinuteEnd(start_minute);

                            free_hour = end_hour;
                            free_minute = end_minute;

                            arrayList.add(freeTimeServer);

                        } else if ((free_hour == start_hour && free_minute == start_minute) || (free_hour < end_hour) || (free_hour == end_hour && free_minute < end_minute)) {
                            free_hour = end_hour;
                            free_minute = end_minute;
                        }

                        if ((j == weekModel.getActivities().size() - 1) && (end_hour < 23 || (end_hour == 23 && end_minute < 59))) {
                            if (!(free_hour == 23 && free_minute == 59)) {
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
                    FreeTimeServer freeTimeServer = (FreeTimeServer) arrayList.get(j);
                    if (isFreeTimeOneMinute(freeTimeServer.getHourStart(), freeTimeServer.getMinuteStart(), freeTimeServer.getHourEnd(), freeTimeServer.getMinuteEnd())) {
                        arrayList.remove(j);
                        j--;
                    }
                }

                for (j = 0; j < arrayList.size(); j++) {
                    FreeTimeServer freeTimeServer = (FreeTimeServer) arrayList.get(j);
                    if ((j + 1) < arrayList.size()) {
                        FreeTimeServer freeTimeServer2 = (FreeTimeServer) arrayList.get(j + 1);
                        if (isStartFinishSame(freeTimeServer.getHourEnd(), freeTimeServer.getMinuteEnd(), freeTimeServer2.getHourStart(), freeTimeServer2.getMinuteStart())) {
                            freeTimeServer.setHourEnd(freeTimeServer2.getHourEnd());
                            freeTimeServer.setMinuteEnd(freeTimeServer2.getMinuteEnd());
                            arrayList.remove(j + 1);
                            j--;
                        }
                    }

                }

                weekModel.addAllFree(arrayList);

                listPlans.add(weekModel);

                cal.add(Calendar.DATE, 1);
            }
        }

        CommitmentFragment commitmentFragment = (CommitmentFragment) mNavigator.getFragment(0);
        if (commitmentFragment != null)
            commitmentFragment.setDataAdapter(listPlans);

        FreeFragment freeFragment = (FreeFragment) mNavigator.getFragment(1);
        if (freeFragment != null)
            freeFragment.setDataAdapter(listPlans);

        cleanPaintRow();
        setPaintRow();

        setProgress(false);
        setProgressFriendRequest(false);
    }

    private void setPaintRow() {
        if (linePaint != -1) {
            for (int i = 0; i < listPlans.size(); i++) {
                if (listPlans.get(i).getDay() == linePaint)
                    listPlans.get(i).setPaint(true);
            }
        }
    }

    private void cleanPaintRow() {
        for (int i = 0; i < listPlans.size(); i++) {
            listPlans.get(i).setPaint(false);
        }
    }

    public boolean isFreeTimeOneMinute(int start_hour, int start_minute, int end_hour, int end_minute) {
        return start_hour == end_hour && ((end_minute - start_minute) == 1);
    }

    public boolean isStartFinishSame(int start_hour, int start_minute, int end_hour, int end_minute) {
        return start_hour == end_hour && end_minute == start_minute;
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        //setProgressFriendRequest(false);
        friendshipRequestsBox.setOnClickListener(this);
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if (view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        } else if (view == contactsBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "contactsBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(this, ContactsActivity.class);
            intent.putExtra("email_contacts", email_friend);
            intent.putExtra("contact_full_name", user.getName());
            startActivity(intent);
        } else if (view == mDateBox || view == dateTextWeek) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mDateBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();

            if (linePaint != -1)
                now.set(year_start, month_start - 1, linePaint);
            else
                now.set(year_start, month_start - 1, day_start);

            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FriendProfileActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            Calendar before1Month = Calendar.getInstance();
            before1Month.add(Calendar.MONTH, -1);
            dpd.setMinDate(before1Month);
            dpd.setAccentColor(ContextCompat.getColor(FriendProfileActivity.this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog");
        } else if (view == freeTimeButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "freeTimeButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left);
            freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right_pressed);
            commitmentsButton.setTextColor(ContextCompat.getColor(FriendProfileActivity.this, R.color.deep_purple_400));
            freeTimeButton.setTextColor(ContextCompat.getColor(FriendProfileActivity.this, R.color.white));
            setCurrentTab(1);
        } else if (view == commitmentsButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "commitmentsButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            commitmentsButton.setBackgroundResource(R.drawable.btn_commitments_free_time_left_pressed);
            freeTimeButton.setBackgroundResource(R.drawable.btn_commitments_free_time_right);
            commitmentsButton.setTextColor(ContextCompat.getColor(FriendProfileActivity.this, R.color.white));
            freeTimeButton.setTextColor(ContextCompat.getColor(FriendProfileActivity.this, R.color.deep_purple_400));
            setCurrentTab(0);
        } else if (view == profilePhotoBox || view == aboutBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "profilePhoto" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent friend = new Intent(FriendProfileActivity.this, AboutFriendActivity.class);
            friend.putExtra("user_about_friend", new UserWrapper(user));
            friend.putExtra("privated", prived);
            startActivity(friend);
        } else if (view == previousWeek) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "previousWeek" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.DAY_OF_WEEK, -6);
            day_start = cal.get(Calendar.DAY_OF_MONTH);
            month_start = cal.get(Calendar.MONTH) + 1;
            year_start = cal.get(Calendar.YEAR);

            updatePlansFriend(year_start, month_start - 1, day_start, false, false);

        } else if (view == nextWeek) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "nextWeek" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar cal = Calendar.getInstance();
            cal.set(year_start, month_start - 1, day_start);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.DAY_OF_WEEK, 7);
            day_start = cal.get(Calendar.DAY_OF_MONTH);
            month_start = cal.get(Calendar.MONTH) + 1;
            year_start = cal.get(Calendar.YEAR);

            updatePlansFriend(year_start, month_start - 1, day_start, false, false);
        } else if (view == friendshipRequestsBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "friendshipRequestsBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            User user = new User();
            user.setEmail(email);
            user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

            CharSequence[] items;

            AlertDialog.Builder builder = new AlertDialog.Builder(FriendProfileActivity.this);

            int friendshipRequestsIconId = (int) friendshipRequestsIcon.getTag();

            if (friendshipRequestsIconId == R.drawable.ic_person_add) {
                sendFriendRequest(email_friend, user);
                friendshipRequestsBox.setOnClickListener(null);
                progressText.setVisibility(View.GONE);
                //progressText.setText(getResources().getString(R.string.sending_request));
            } else if (friendshipRequestsIconId == R.drawable.ic_person_cancel) {
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setEmail(email);
                friendRequest.setEmailFriend(email_friend);
                friendRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                progressText.setVisibility(View.GONE);
                //progressText.setText(getResources().getString(R.string.canceling));
                cancelFriendRequest(friendRequest);
            } else {
                items = null;

                if (friendshipRequestsIconId == R.drawable.ic_person_check) {
                    items = getResources().getStringArray(R.array.array_contact_delete);
                    progressText.setText("");
                } else if (friendshipRequestsIconId == R.drawable.ic_person_waiting) {
                    items = getResources().getStringArray(R.array.array_pending_request_accept_ignore);
                    progressText.setText("");
                }

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        createDialogUpdate(item, friendshipRequestsIconId);
                    }
                });
                builder.show();
            }
        }
    }

    private void createDialogUpdate(int item, int type) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);

        if (item == 0) {
            if (type == R.drawable.ic_person_check) {
                text1.setText(this.getResources().getString(R.string.contact_confirmation_question_delete, user.getName()));
            } else {
                text1.setText(this.getResources().getString(R.string.contact_confirmation_question_add, user.getName()));
            }
        } else if (item == 1) {
            text1.setText(this.getResources().getString(R.string.contact_confirmation_question_ignore_request, user.getName()));
        }

        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                User user = new User();
                user.setEmail(email);
                user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "updateFriendRequest" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (type == R.drawable.ic_person_check)
                    sendDeleteRequest(email_friend, user);
                else if (type == R.drawable.ic_person_waiting) {
                    FriendRequest friendRequest = new FriendRequest();
                    friendRequest.setEmail(email);
                    friendRequest.setEmailFriend(email_friend);
                    friendRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());


                    if (item == 0) {
                        friendRequest.setStatus(1);
                        updateFriendRequest(friendRequest);
                    } else if (item == 1) {
                        friendRequest.setStatus(0);
                        updateFriendRequest(friendRequest);
                    }
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendDeleteRequest(String email, User user) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerDeleteRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseDeleteRequest, this::handleError));
    }

    private void handleResponseDeleteRequest(Response response) {

        friendshipRequestsIcon.setImageResource(R.drawable.ic_person_add);
        friendshipRequestsText.setText(getResources().getString(R.string.add));
        friendshipRequestsIcon.setTag(R.drawable.ic_person_add);
        setProgressFriendRequest(false);
        Toast.makeText(this, getResources().getString(R.string.contact_deleted), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavigator != null)
            mNavigator.onSaveInstanceState(outState);
    }

    private void setCurrentTab(int position) {
        if (position != 2) {
            mNavigator.showFragment(position);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isTimeToChangeBackground();

        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");

        if (dpd != null) dpd.setOnDateSetListener(this);
    }

    public void updatePlansFriend(int year, int monthOfYear, int dayOfMonth, boolean progress, boolean select) {
        isTimeToChangeBackground();

        listPlans.clear();

        if (select)
            linePaint = dayOfMonth;
        else
            linePaint = -1;

        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }

        cal.add(Calendar.DAY_OF_WEEK, -15);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);

        cal.add(Calendar.DAY_OF_WEEK, 21);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int month2 = cal.get(Calendar.MONTH) + 1;
        int year2 = cal.get(Calendar.YEAR);

        int d1f = d;

        if (m != month2) {
            Calendar cal2 = Calendar.getInstance();
            cal2.set(y, m - 1, d);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.clear(Calendar.MINUTE);
            cal2.clear(Calendar.SECOND);
            cal2.clear(Calendar.MILLISECOND);

            while (cal2.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal2.add(Calendar.DATE, -1);
            }
            int day3 = cal2.get(Calendar.DAY_OF_MONTH);
            while (day3 != 1) {
                d1f = day3;
                cal2.add(Calendar.DAY_OF_WEEK, 1);
                day3 = cal2.get(Calendar.DAY_OF_MONTH);
            }
        }

        Plans plans = new Plans();
        plans.setEmail(email_friend);
        plans.setA(y);
        plans.setA2(year2);
        plans.setD1(d);
        plans.setD2(day2);
        plans.setM(m);
        plans.setM2(month2);
        plans.setD1f(d1f);
        plans.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        plans.addEmails(mSharedPreferences.getString(Constants.EMAIL, ""));

        int day_start_temp = cal.get(Calendar.DAY_OF_MONTH);
        int month_start_temp = cal.get(Calendar.MONTH) + 1;

        cal.add(Calendar.DAY_OF_WEEK, -6);
        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH) + 1;
        year_start = cal.get(Calendar.YEAR);

        String month_text_start = dateFormat.formatMonthShort(month_start);
        String month_text_start_temp = dateFormat.formatMonthShort(month_start_temp);
        dateTextWeek.setText(getResources().getString(R.string.date_format_02, String.format("%02d", day_start), month_text_start, String.format("%02d", day_start_temp), month_text_start_temp));

        String month_text = dateFormat.formatMonth(cal.get(Calendar.MONTH) + 1);
        dateTextMonth.setText(getResources().getString(R.string.date_format_01, month_text, year_start));

        setPlans(plans, progress);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        updatePlansFriend(year, monthOfYear, dayOfMonth, false, true);
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    public User getUserFriend() {
        return user;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
        Glide.get(this).clearMemory();
    }

    @Override
    public void refreshLayout(boolean showRefresh) {
        updatePlansFriend(year_start, month_start - 1, day_start, false, false);
    }
}
