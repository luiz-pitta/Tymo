package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import io.development.tymo.R;
import io.development.tymo.adapters.ShowActivityFragmentAdapter;
import io.development.tymo.fragments.WhatShowFragment;
import io.development.tymo.fragments.WhenShowFragment;
import io.development.tymo.fragments.WhoShowFragment;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.UpdateButtonController;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ShowActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;

    private FragmentNavigator mNavigator;

    private TextView m_title, privacyText;
    private ImageView m_ic_edit;
    private ImageView icon1;
    private ImageView privacyIcon;

    private LinearLayout privacyBox;

    private UpdateButtonController controller;

    private TextView whatText;
    private TextView whereWhenText;
    private TextView whoText;

    private ImageView whatButton;
    private ImageView whereWhenButton;
    private ImageView whoButton;

    private View whatCorners;
    private View whereWhenCorners;
    private View whoCorners;

    private ImageView pieceIcon;
    private ImageView cubeLowerBoxIcon;
    private ImageView cubeUpperBoxIcon;

    private View checkButtonBox, deleteButtonBox;
    private ImageView checkButton, deleteButton;
    private TextView checkText, deleteText, requiredText;

    private ActivityWrapper activityWrapper;
    private int selected = 0;

    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> admList = new ArrayList<>();
    private User creator_activity;
    private ArrayList<User> invitedList = new ArrayList<>();
    private ArrayList<User> confirmedList = new ArrayList<>();
    private ArrayList<ActivityServer> activityServers;
    private boolean permissionInvite = false;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();
    private CompositeDisposable mSubscriptions;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act);

        mSubscriptions = new CompositeDisposable();

        findViewById(R.id.buttonsBar).setVisibility(View.VISIBLE);
        findViewById(R.id.confirmationBar).setVisibility(View.GONE);
        findViewById(R.id.magicWandButton).setVisibility(View.GONE);

        privacyBox = (LinearLayout) findViewById(R.id.privacyBox);
        checkButtonBox = findViewById(R.id.checkButtonBox);
        deleteButtonBox = findViewById(R.id.deleteButtonBox);
        checkButton = (ImageView) findViewById(R.id.checkButton);
        deleteButton = (ImageView) findViewById(R.id.deleteButton);
        checkText = (TextView) findViewById(R.id.checkText);
        deleteText = (TextView) findViewById(R.id.deleteText);
        requiredText = (TextView) findViewById(R.id.requiredText);

        cubeLowerBoxIcon = (ImageView) findViewById(R.id.cubeLowerBoxIcon);
        cubeUpperBoxIcon = (ImageView) findViewById(R.id.cubeUpperBoxIcon);
        pieceIcon = (ImageView) findViewById(R.id.pieceIcon);

        whatText = (TextView) findViewById(R.id.whatText);
        whereWhenText = (TextView) findViewById(R.id.whereWhenText);
        whoText = (TextView) findViewById(R.id.whoText);

        whatCorners = findViewById(R.id.whatCorners);
        whereWhenCorners = findViewById(R.id.whereWhenCorners);
        whoCorners = findViewById(R.id.whoCorners);

        whatButton = (ImageView) findViewById(R.id.whatIcon);
        whereWhenButton = (ImageView) findViewById(R.id.whereWhenIcon);
        whoButton = (ImageView) findViewById(R.id.whoIcon);

        whatText.setOnClickListener(this);
        whereWhenText.setOnClickListener(this);
        whoText.setOnClickListener(this);

        whatButton.setOnClickListener(this);
        whereWhenButton.setOnClickListener(this);
        whoButton.setOnClickListener(this);

        whatCorners.setOnClickListener(this);
        whereWhenCorners.setOnClickListener(this);
        whoCorners.setOnClickListener(this);

        privacyBox.setOnClickListener(this);

        requiredText.setVisibility(View.GONE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setDistanceToTriggerSync(225);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.deep_purple_400));

        checkButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        //set button controller
        controller = new UpdateButtonController(this);
        controller.attach(true, whatText, whatButton, whatCorners);
        controller.attach(false, whereWhenText, whereWhenButton, whereWhenCorners);
        controller.attach(false, whoText, whoButton, whoCorners);
        controller.updateAll(Utilities.DEFAULT_POSITION, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);

        privacyIcon = (ImageView) findViewById(R.id.privacyIcon);
        privacyIcon.setVisibility(View.GONE);

        privacyText = (TextView) findViewById(R.id.privacyText);


        m_title = (TextView) findViewById(R.id.text);
        m_title.setText(getResources().getString(R.string.act));

        m_ic_edit = (ImageView) findViewById(R.id.icon2);
        m_ic_edit.setImageDrawable(getResources().getDrawable(R.drawable.ic_edit));
        m_ic_edit.setOnClickListener(this);

        icon1 = (ImageView) findViewById(R.id.icon1);
        //icon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_more_vertical));
        icon1.setVisibility(View.GONE);

        mNavigator = new FragmentNavigator(getFragmentManager(), new ShowActivityFragmentAdapter(), R.id.contentBox);
        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.onCreate(savedInstanceState);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setCurrentTab(mNavigator.getCurrentPosition());

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_show");

        ActivityServer activityServer = new ActivityServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        activityServer.setId(0);
        activityServer.setCreator(email);

        setActivityInformation(activityWrapper.getActivityServer().getId(), activityServer);
        setProgress(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                updateLayout();
            }
        }, 200);

        // Load complete
    }

    public void updateLayout(){
        ActivityServer activityServer = new ActivityServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        activityServer.setId(0);
        activityServer.setCreator(email);

        setActivityInformationRefresh(activityWrapper.getActivityServer().getId(), activityServer);
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }


    private void setActivityInformation(long id, ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getActivity2(id, activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void setActivityInformationRefresh(long id, ActivityServer activityServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getActivity2(id, activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private User checkIfInActivity(ArrayList<User> usr) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        for (int i = 0; i < usr.size(); i++) {
            if (email.equals(usr.get(i).getEmail()))
                return usr.get(i);
        }

        return null;
    }

    public boolean checkIfAdm(ArrayList<User> usr, String email) {
        for (int i = 0; i < usr.size(); i++) {
            if (email.equals(usr.get(i).getEmail()))
                return true;
        }

        return false;
    }

    private ArrayList<User> setOrderGuests(ArrayList<User> users) {

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                String name1 = c1.getName();
                String name2 = c2.getName();

                if (name1.compareTo(name2) > 0)
                    return 1;
                else if (name1.compareTo(name2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getCountKnows();
                long id2 = c2.getCountKnows();

                if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getCountFavorite();
                long id2 = c2.getCountFavorite();

                if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getInvitation();
                long id2 = c2.getInvitation();

                if (id1 == 2 || id2 == 2)
                    return 1;
                else if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        return users;
    }

    @Nullable
    private User getCreator(ArrayList<User> users, User creator) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().contains(creator.getEmail()))
                return users.get(i);
        }
        return null;
    }

    private ArrayList<User> getConfirmedNoAdm(ArrayList<User> users, ArrayList<User> adms) {
        ArrayList<User> confirmed = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (!checkIfAdm(adms, users.get(i).getEmail()) && users.get(i).getInvitation() == 1)
                confirmed.add(users.get(i));
        }
        return setOrderGuests(confirmed);
    }

    private ArrayList<User> getInvitedNoAdm(ArrayList<User> users, ArrayList<User> adms) {
        ArrayList<User> confirmed = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if ((users.get(i).getInvitation() == 0 || users.get(i).getInvitation() == 2) && !checkIfAdm(adms, users.get(i).getEmail()))
                confirmed.add(users.get(i));
        }
        return setOrderGuests(confirmed);
    }

    private ArrayList<User> getInvitedAdm(ArrayList<User> users, ArrayList<User> adms, User creator) {
        ArrayList<User> confirmed = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (!users.get(i).getEmail().contains(creator.getEmail()) && users.get(i).getInvitation() == 1 && checkIfAdm(adms, users.get(i).getEmail())) {
                users.get(i).setAdm(true);
                confirmed.add(users.get(i));
            }
        }
        return setOrderGuests(confirmed);
    }

    private ArrayList<User> getAdmNoCreator(ArrayList<User> adms, User creator) {
        ArrayList<User> confirmed = new ArrayList<>();
        for (int i = 0; i < adms.size(); i++) {
            if (!adms.get(i).getEmail().contains(creator.getEmail())) {
                adms.get(i).setAdm(true);
                confirmed.add(adms.get(i));
            }
        }
        return setOrderGuests(confirmed);
    }

    private boolean checkIfCanInvite(int invitation_type) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        switch (invitation_type) {
            case 0:
                return checkIfAdm(admList, email);
            case 1:
                return checkIfInActivity(invitedList) != null;
            case 2:
                return checkIfInActivity(invitedList) != null;
            default:
                return false;
        }
    }

    private void deleteFlagActReminder(long id, ActivityServer activity) {

        mSubscriptions.add(NetworkUtil.getRetrofit().deleteActivity(id, activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm, this::handleError));
    }

    private void handleDeleteIgnoreConfirm(Response response) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int year2 = c2.get(Calendar.YEAR);

        ActivityServer activity = getActivity();
        int d = activity.getDayStart();
        int m = activity.getMonthStart();
        int y = activity.getYearStart();

        if((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //ACTIVITY_DELETED_SUCCESSFULLY e WITHOUT_NOTIFICATION
        finish();
    }

    private void handleResponse(Response response) {

        invitedList.clear();
        confirmedList.clear();
        userList.clear();
        admList.clear();

        userList = response.getPeople();
        admList = setOrderGuests(response.getAdms());
        creator_activity = getCreator(userList, response.getUser());
        if (creator_activity != null)
            creator_activity.setCreator(true);


        invitedList.add(creator_activity);
        invitedList.addAll(getAdmNoCreator(admList, creator_activity));
        invitedList.addAll(getConfirmedNoAdm(userList, admList));
        invitedList.addAll(getInvitedNoAdm(userList, admList));

        confirmedList.add(creator_activity);
        confirmedList.addAll(getInvitedAdm(userList, admList, creator_activity));
        confirmedList.addAll(getConfirmedNoAdm(userList, admList));

        if (response.getTags().size() == 0) {
            finish();
            Toast.makeText(this, getString(R.string.act_not_found), Toast.LENGTH_LONG).show();
        } else {

            ActivityServer activityServer = activityWrapper.getActivityServer();

            User user = checkIfInActivity(userList);
            m_ic_edit.setVisibility(View.INVISIBLE);

            if (user != null) {
                privacyIcon.setVisibility(View.VISIBLE);
                privacyText.setVisibility(View.VISIBLE);
                findViewById(R.id.arrowIcon).setVisibility(View.VISIBLE);
                switch (user.getPrivacy()) {
                    case 0:
                        privacyIcon.setImageResource(R.drawable.ic_public);
                        privacyText.setText(getResources().getString(R.string.visibility_public));
                        selected = 0;
                        break;
                    case 1:
                        privacyIcon.setImageResource(R.drawable.ic_people_two);
                        privacyText.setText(getResources().getString(R.string.visibility_only_my_contacts));
                        selected = 1;
                        break;
                    case 2:
                        privacyIcon.setImageResource(R.drawable.ic_lock);
                        privacyText.setText(getResources().getString(R.string.visibility_private));
                        selected = 2;
                        break;
                }

                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                if (checkIfAdm(response.getAdms(), email)) {
                    m_ic_edit.setVisibility(View.VISIBLE);
                } else
                    m_ic_edit.setVisibility(View.INVISIBLE);

                privacyBox.setVisibility(View.VISIBLE);

                if (checkIfCreator(response.getUser().getEmail())) {
                    deleteButtonBox.setVisibility(View.VISIBLE);
                    deleteButton.setBackgroundResource(R.drawable.btn_feed_ignore);
                    deleteText.setTextColor(ContextCompat.getColor(this, R.color.red_600));
                    deleteButton.setOnClickListener(this);
                    checkButtonBox.setVisibility(View.GONE);
                } else if (user.getInvited() > 0) {
                    if (user.getInvitation() == 1) {
                        deleteButtonBox.setVisibility(View.VISIBLE);
                        deleteButton.setBackgroundResource(R.drawable.btn_feed_ignore);
                        deleteText.setTextColor(ContextCompat.getColor(this, R.color.red_600));
                        deleteButton.setOnClickListener(this);
                        checkButtonBox.setVisibility(View.GONE);
                    } else {
                        privacyBox.setVisibility(View.GONE);
                        checkButtonBox.setVisibility(View.VISIBLE);
                        checkButton.setBackgroundResource(R.drawable.btn_feed_check);
                        checkText.setTextColor(ContextCompat.getColor(this, R.color.green_600));
                        checkButton.setOnClickListener(this);
                        deleteButtonBox.setVisibility(View.GONE);
                    }
                } else if (activityServer.getInvitationType() == 2) {
                    privacyBox.setVisibility(View.GONE);
                    checkButtonBox.setVisibility(View.VISIBLE);
                    checkButton.setBackgroundResource(R.drawable.btn_feed_check);
                    checkText.setTextColor(ContextCompat.getColor(this, R.color.green_600));
                    checkButton.setOnClickListener(this);
                    deleteButtonBox.setVisibility(View.GONE);
                } else {
                    privacyBox.setVisibility(View.GONE);
                    checkButtonBox.setVisibility(View.GONE);
                    deleteButtonBox.setVisibility(View.GONE);
                }

            } else {
                deleteButtonBox.setVisibility(View.GONE);
                checkButtonBox.setVisibility(View.GONE);
                if (activityServer.getInvitationType() == 2) {
                    checkButtonBox.setVisibility(View.VISIBLE);
                    checkButton.setBackgroundResource(R.drawable.btn_feed_check);
                    checkText.setTextColor(ContextCompat.getColor(this, R.color.green_600));
                    checkButton.setOnClickListener(this);
                }
                privacyBox.setVisibility(View.GONE);
                privacyIcon.setVisibility(View.INVISIBLE);
                findViewById(R.id.arrowIcon).setVisibility(View.INVISIBLE);
            }

            Glide.clear(pieceIcon);
            Glide.with(this)
                    .load(activityServer.getCubeIcon())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(pieceIcon);

            cubeUpperBoxIcon.setColorFilter(activityServer.getCubeColorUpper());
            cubeLowerBoxIcon.setColorFilter(activityServer.getCubeColor());

            m_title.setText(activityServer.getTitle());

            WhatShowFragment whatShowFragment = (WhatShowFragment) mNavigator.getFragment(0);
            whatShowFragment.setLayout(activityServer, response.getTags());
            activityServers = response.getWhatsGoingAct();

            WhenShowFragment whenShowFragment = (WhenShowFragment) mNavigator.getFragment(1);
            if (whenShowFragment != null)
                whenShowFragment.setLayout(getActivity(), getActivityServers());

            permissionInvite = checkIfCanInvite(getActivity().getInvitationType());

            WhoShowFragment whoShowFragment = (WhoShowFragment) mNavigator.getFragment(2);
            if (whoShowFragment != null) {
                whoShowFragment.setLayout(activityServer, invitedList, confirmedList, permissionInvite);
                whoShowFragment.setProgress(false);
            }
        }

        setProgress(false);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public boolean getPermissionInvite() {
        return permissionInvite;
    }

    private boolean checkIfCreator(String email_creator) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        return email.equals(email_creator);
    }

    public ArrayList<ActivityServer> getActivityServers() {
        return activityServers;
    }


    public ArrayList<User> getUserList() {
        return invitedList;
    }

    public ArrayList<User> getUserConfirmedList() {
        return confirmedList;
    }

    public ArrayList<User> getAdmList() {
        return admList;
    }

    public ActivityServer getActivity() {
        return activityWrapper.getActivityServer();
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
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
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.whatText || id == R.id.whatButton || id == R.id.whatCorners) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "whatText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(0, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            setCurrentTab(0);
        } else if (id == R.id.whereWhenText || id == R.id.whereWhenButton || id == R.id.whereWhenCorners) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "whereWhenText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(1, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            setCurrentTab(1);
        } else if (id == R.id.whoText || id == R.id.whoButton || id == R.id.whoCorners) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "whoText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            controller.updateAll(2, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            setCurrentTab(2);
        } else if (id == R.id.icon2) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon2" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(ShowActivity.this, AddActivity.class);
            myIntent.putExtra("act_edit", activityWrapper);
            startActivity(myIntent);
            finish();
        } else if (id == R.id.checkButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "checkButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            InviteRequest inviteRequest = new InviteRequest();

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            inviteRequest.setEmail(email);
            inviteRequest.setStatus(Constants.YES);

            inviteRequest.setType(Constants.ACT);
            inviteRequest.setIdAct(getActivity().getId());

            updateInviteRequest(inviteRequest);
        } else if (id == R.id.deleteButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (checkIfCreator(creator_activity.getEmail())) {
                createDialogRemove(getActivity().getRepeatType() > 0);
            } else{
                InviteRequest inviteRequest = new InviteRequest();

                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                inviteRequest.setEmail(email);
                inviteRequest.setStatus(Constants.NO);

                inviteRequest.setType(Constants.ACT);
                inviteRequest.setIdAct(getActivity().getId());

                updateInviteRequest(inviteRequest);
            }

        } else if (v == privacyBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "privacyBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogPrivacy();
        }
    }

    private void createDialogRemove(boolean repeat) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);


        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView button1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView button2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);
        RadioGroup radioGroup = (RadioGroup) customView.findViewById(R.id.radioGroup);
        AppCompatRadioButton allRadioButton = (AppCompatRadioButton) customView.findViewById(R.id.allRadioButton);

        editText.setVisibility(View.GONE);

        allRadioButton.setText(getResources().getString(R.string.delete_plans_answer_all));

        if (repeat) {
            radioGroup.setVisibility(View.VISIBLE);
            radioGroup.setOrientation(LinearLayout.VERTICAL);
            button1.setText(getResources().getString(R.string.cancel));
            button2.setText(getResources().getString(R.string.confirm));
            text2.setVisibility(View.VISIBLE);
            text1.setText(getResources().getString(R.string.delete_plans_question_text_1));
            text2.setText(getResources().getString(R.string.delete_plans_question_text_2));
        } else {
            button1.setText(getResources().getString(R.string.no));
            button2.setText(getResources().getString(R.string.yes));
            text2.setVisibility(View.GONE);
            text1.setVisibility(View.VISIBLE);
            text1.setText(getResources().getString(R.string.delete_plans_question_text_3));
        }

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonID;
                View radioButton;
                int idx = -1;

                if (repeat) {
                    radioButtonID = radioGroup.getCheckedRadioButtonId();
                    radioButton = radioGroup.findViewById(radioButtonID);
                    idx = radioGroup.indexOfChild(radioButton);
                }

                ActivityServer activity = new ActivityServer();
                activity.setId(idx);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actRemove" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                activity.setVisibility(Constants.FLAG);
                deleteFlagActReminder(getActivity().getId(), activity);

                dg.dismiss();
            }
        });

        dg.show();
    }

    private void createDialogPrivacy() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_visibility, null);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        RelativeLayout optionBox1 = (RelativeLayout) customView.findViewById(R.id.optionBox1);
        RelativeLayout optionBox2 = (RelativeLayout) customView.findViewById(R.id.optionBox2);
        RelativeLayout optionBox3 = (RelativeLayout) customView.findViewById(R.id.optionBox3);
        ImageView checkBoxActivated1 = (ImageView) customView.findViewById(R.id.checkBoxActivated1);
        ImageView checkBoxActivated2 = (ImageView) customView.findViewById(R.id.checkBoxActivated2);
        ImageView checkBoxActivated3 = (ImageView) customView.findViewById(R.id.checkBoxActivated3);

        int color_selected = this.getResources().getColor(R.color.select);
        int color_transparent = this.getResources().getColor(R.color.transparent);

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        ActivityServer privacyUpdate = new ActivityServer();
        privacyUpdate.setCreator(email);
        privacyUpdate.setId(getActivity().getId());

        switch (selected) {
            case 1:
                //optionBox1.setBackgroundColor(color_transparent);
                //optionBox2.setBackgroundColor(color_selected);
                //optionBox3.setBackgroundColor(color_transparent);
                checkBoxActivated1.setVisibility(View.GONE);
                checkBoxActivated2.setVisibility(View.VISIBLE);
                checkBoxActivated3.setVisibility(View.GONE);
                break;
            case 2:
                //optionBox1.setBackgroundColor(color_transparent);
                //optionBox2.setBackgroundColor(color_transparent);
                //optionBox3.setBackgroundColor(color_selected);
                checkBoxActivated1.setVisibility(View.GONE);
                checkBoxActivated2.setVisibility(View.GONE);
                checkBoxActivated3.setVisibility(View.VISIBLE);
                break;
            default:
                //optionBox1.setBackgroundColor(color_selected);
                //optionBox2.setBackgroundColor(color_transparent);
                //optionBox3.setBackgroundColor(color_transparent);
                checkBoxActivated1.setVisibility(View.VISIBLE);
                checkBoxActivated2.setVisibility(View.GONE);
                checkBoxActivated3.setVisibility(View.GONE);
                break;
        }

        optionBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //optionBox1.setBackgroundColor(color_selected);
                //optionBox2.setBackgroundColor(color_transparent);
                //optionBox3.setBackgroundColor(color_transparent);
                checkBoxActivated1.setVisibility(View.VISIBLE);
                checkBoxActivated2.setVisibility(View.GONE);
                checkBoxActivated3.setVisibility(View.GONE);
                privacyIcon.setImageResource(R.drawable.ic_public);
                privacyText.setText(getResources().getString(R.string.visibility_public));
                selected = 0;

                privacyUpdate.setVisibility(selected);

                setPrivacyActivity(privacyUpdate);

                dialog.dismiss();
            }
        });

        optionBox2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //optionBox1.setBackgroundColor(color_transparent);
                //optionBox2.setBackgroundColor(color_selected);
                //optionBox3.setBackgroundColor(color_transparent);
                checkBoxActivated1.setVisibility(View.GONE);
                checkBoxActivated2.setVisibility(View.VISIBLE);
                checkBoxActivated3.setVisibility(View.GONE);
                privacyIcon.setImageResource(R.drawable.ic_people_two);
                privacyText.setText(getResources().getString(R.string.visibility_only_my_contacts));
                selected = 1;

                privacyUpdate.setVisibility(selected);

                setPrivacyActivity(privacyUpdate);

                dialog.dismiss();
            }
        });


        optionBox3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //optionBox1.setBackgroundColor(color_transparent);
                //optionBox2.setBackgroundColor(color_transparent);
                //optionBox3.setBackgroundColor(color_selected);
                checkBoxActivated1.setVisibility(View.GONE);
                checkBoxActivated2.setVisibility(View.GONE);
                checkBoxActivated3.setVisibility(View.VISIBLE);
                privacyIcon.setImageResource(R.drawable.ic_lock);
                privacyText.setText(getResources().getString(R.string.visibility_private));
                selected = 2;

                privacyUpdate.setVisibility(selected);

                setPrivacyActivity(privacyUpdate);

                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private void setPrivacyActivity(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().setPrivacyAct(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponsePrivacy, this::handleError));
    }

    private void handleResponsePrivacy(Response response) {
        setProgress(false);
    }


    private void updateInviteRequest(InviteRequest inviteRequest) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleActivity, this::handleError));
    }

    public void addGuestToActivity(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().addNewGuest(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleActivity, this::handleError));
    }

    private void handleActivity(Response response) {
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //INVITED_SUCCESSFULLY
        ActivityServer activityServer = new ActivityServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        activityServer.setId(0);
        activityServer.setCreator(email);

        setActivityInformation(getActivity().getId(), activityServer);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int year2 = c2.get(Calendar.YEAR);

        ActivityServer activity = getActivity();
        int d = activity.getDayStart();
        int m = activity.getMonthStart();
        int y = activity.getYearStart();

        if((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();
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
            if(time_exact >= 60) {
                c1.add(Calendar.MINUTE, -60);
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


}
