package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.cunoraz.tagview.Tag;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.AddActivityFragmentAdapter;
import io.development.tymo.adapters.CustomizeAddActivityAdapter;
import io.development.tymo.fragments.WhatEditFragment;
import io.development.tymo.fragments.WhenEditFragment;
import io.development.tymo.fragments.WhoEditFragment;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.IconServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.SecureStringPropertyConverter;
import io.development.tymo.utils.UpdateButtonController;
import io.development.tymo.utils.Utilities;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateFields;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private RelativeLayout mPieceCustom;
    private ColorPickerView mColorPickerView;
    private ImageView mColorView;
    private ImageView mColorViewUpper;
    private ImageView mColorIcon;

    private ImageView mColorViewMain;
    private ImageView mColorViewUpperMain;
    private ImageView mColorIconMain;

    private TextView mOkButton;
    private TextView mCancelButton;

    private ActivityWrapper activityWrapper;

    private ArrayList<ActivityServer> activityServers;
    private ArrayList<IconServer> iconList;

    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> admList = new ArrayList<>();
    private User creator_activity, user_friend = null;
    private ArrayList<User> invitedList = new ArrayList<>();
    private ArrayList<User> confirmedList = new ArrayList<>();
    private boolean permissionInvite = false;

    private FragmentNavigator mNavigator;

    private TextView m_title, privacyText, confirmationButton;
    private ImageView icon2;

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

    private LinearLayout privacyBox;
    private ImageView privacyIcon;

    private Space space;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int d, m, y;
    private String urlIcon = "";
    private boolean edit = false, recover = false;

    private int selected = 0;

    private CompositeSubscription mSubscriptions;

    private TextView requiredText;
    private FirebaseAnalytics mFirebaseAnalytics;

    private SecureStringPropertyConverter converter = new SecureStringPropertyConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act);

        findViewById(R.id.buttonsBar).setVisibility(View.GONE);
        findViewById(R.id.icon1).setVisibility(View.GONE);

        mSubscriptions = new CompositeSubscription();

        whatText = (TextView) findViewById(R.id.whatText);
        whereWhenText = (TextView) findViewById(R.id.whereWhenText);
        whoText = (TextView) findViewById(R.id.whoText);

        whatButton = (ImageView) findViewById(R.id.whatIcon);
        whereWhenButton = (ImageView) findViewById(R.id.whereWhenIcon);
        whoButton = (ImageView) findViewById(R.id.whoIcon);

        whatCorners = findViewById(R.id.whatCorners);
        whereWhenCorners = findViewById(R.id.whereWhenCorners);
        whoCorners = findViewById(R.id.whoCorners);

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        m_title = (TextView) findViewById(R.id.text);
        icon2 = (ImageView) findViewById(R.id.icon2);
        privacyIcon = (ImageView) findViewById(R.id.privacyIcon);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        mColorViewMain = (ImageView) findViewById(R.id.cubeLowerBoxIcon);
        mColorViewUpperMain = (ImageView) findViewById(R.id.cubeUpperBoxIcon);
        mColorIconMain = (ImageView) findViewById(R.id.pieceIcon);
        mPieceCustom = (RelativeLayout) findViewById(R.id.pieceBox);
        privacyText = (TextView) findViewById(R.id.privacyText);
        privacyBox = (LinearLayout) findViewById(R.id.privacyBox);
        space = (Space) findViewById(R.id.space);
        requiredText = (TextView) findViewById(R.id.requiredText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setEnabled(false);

        requiredText.setVisibility(View.GONE);

        space.getLayoutParams().height = (int) Utilities.convertDpToPixel(60, getApplicationContext());

        //icon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_fisherman));
        m_title.setText(getResources().getString(R.string.create_activity));
        confirmationButton.setText(R.string.confirm);

        //Set Listners
        whatText.setOnClickListener(this);
        whereWhenText.setOnClickListener(this);
        whoText.setOnClickListener(this);
        whatButton.setOnClickListener(this);
        whereWhenButton.setOnClickListener(this);
        whoButton.setOnClickListener(this);
        privacyBox.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mPieceCustom.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);

        icon2.setVisibility(View.INVISIBLE);
        icon2.setOnClickListener(null);

        //set button controller
        controller = new UpdateButtonController(this);
        controller.attach(true, whatText, whatButton, whatCorners);
        controller.attach(false, whereWhenText, whereWhenButton, whereWhenCorners);
        controller.attach(false, whoText, whoButton, whoCorners);
        controller.updateAll(Utilities.DEFAULT_POSITION, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);

        mNavigator = new FragmentNavigator(getFragmentManager(), new AddActivityFragmentAdapter(), R.id.contentBox);
        mNavigator.setDefaultPosition(Utilities.DEFAULT_POSITION);
        mNavigator.onCreate(savedInstanceState);

        setCurrentTab(mNavigator.getCurrentPosition());
        privacyText.setText(getResources().getString(R.string.privacy_always_public));
        privacyIcon.setImageResource(R.drawable.ic_public);

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_edit");
        if (activityWrapper == null) {
            activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_recover");
            if (activityWrapper != null) {
                icon2.setVisibility(View.INVISIBLE);
                icon2.setOnClickListener(null);
                recover = true;
            } else {
                activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_free");
                UserWrapper userWrapper = (UserWrapper) getIntent().getSerializableExtra("act_free_friend_usr");
                if(activityWrapper != null && userWrapper != null)
                    user_friend = userWrapper.getUser();
            }
        } else {
            edit = true;
            confirmationButton.setText(R.string.save_updates);
            m_title.setText(getResources().getString(R.string.edit_activity));
            icon2.setVisibility(View.INVISIBLE);
            icon2.setOnClickListener(null);
        }

        if (activityWrapper != null && (edit || recover)) {
            ActivityServer activityServer = new ActivityServer();
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            activityServer.setId(0);
            activityServer.setCreator(email);
            setActivityInformation(activityWrapper.getActivityServer().getId(), activityServer);
        }

        getIcons();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mNavigator!=null)
            mNavigator.onSaveInstanceState(outState);
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void getIcons() {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getIcons()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(ArrayList<IconServer> icons) {
        setProgress(false);
        iconList = new ArrayList<>();
        iconList.addAll(icons);
        Collections.sort(iconList, new Comparator<IconServer>() {
            @Override
            public int compare(IconServer c1, IconServer c2) {
                String[] u1 = c1.getUrl().split("/");
                String[] u2 = c2.getUrl().split("/");
                String url1 = u1[u1.length-1];
                String url2 = u2[u2.length-1];

                if (url1.compareTo(url2) > 0)
                    return 1;
                else if (url1.compareTo(url2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

    }

    private void setActivityInformation(long id, ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getActivity2(id, activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleEditActivity, this::handleError));
    }

    private User checkIfInActivity(ArrayList<User> usr) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        for (int i = 0; i < usr.size(); i++) {
            if (email.matches(usr.get(i).getEmail()))
                return usr.get(i);
        }

        return null;
    }

    public boolean checkIfAdm(ArrayList<User> usr, String email) {
        for (int i = 0; i < usr.size(); i++) {
            if (email.matches(usr.get(i).getEmail()))
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

    private void handleEditActivity(Response response) {

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
            Toast.makeText(this, getString(R.string.show_activity_404), Toast.LENGTH_LONG).show();
        } else {

            ActivityServer activityServer = activityWrapper.getActivityServer();
            User userEdit = checkIfInActivity(invitedList);

            if (!activityServer.getCubeIcon().matches("")) {

                Glide.clear(mColorIconMain);
                Glide.with(AddActivity.this)
                        .load(activityServer.getCubeIcon())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mColorIconMain);

                mColorViewUpperMain.setColorFilter(activityServer.getCubeColorUpper());
                mColorViewMain.setColorFilter(activityServer.getCubeColor());
            }

            if (userEdit != null) {
                /*if (edit) {
                    privacyIcon.setOnClickListener(null);
                    privacyText.setOnClickListener(null);
                }*/

                switch (userEdit.getPrivacy()) {
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
            }

            if (response.getTags().size() > 0) {
                WhatEditFragment whatEditFragment = (WhatEditFragment) mNavigator.getFragment(0);
                whatEditFragment.setLayout(activityServer, response.getTags());
            }

            activityServers = response.getWhatsGoingAct();
        }

        findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    public User getUserFriend() {
        return user_friend;
    }

    public ArrayList<User> getUserList() {
        return invitedList;
    }

    public ArrayList<User> getAdmList() {
        return admList;
    }

    public ArrayList<User> getConfirmedList() {
        return confirmedList;
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

        setActivityGuestInformation(getActivity().getId(), activityServer);
    }

    public void setActivityGuestInformation(long id, ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getActivity2(id, activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGuests, this::handleError));
    }

    private void handleResponseGuests(Response response) {
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

        WhoEditFragment whoEditFragment = (WhoEditFragment) mNavigator.getFragment(2);
        whoEditFragment.setLayout(getActivity(), invitedList, confirmedList, edit);

        setProgress(false);
    }

    public ActivityServer getActivity() {
        if (activityWrapper != null)
            return activityWrapper.getActivityServer();
        else
            return null;
    }

    private void setCurrentTab(int position) {
        mNavigator.showFragment(position);
    }

    @Override
    public void onClick(View v) {
        if (v == whatText || v == whatButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "whatText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            controller.updateAll(0, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            setCurrentTab(0);
        } else if (v == whereWhenText || v == whereWhenButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "whereWhenText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            controller.updateAll(1, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            setCurrentTab(1);
        } else if (v == whoText || v == whoButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "whoText" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            controller.updateAll(2, R.color.deep_purple_400, R.color.deep_purple_400, R.drawable.bg_shape_oval_deep_purple_400_corners);
            setCurrentTab(2);
        } else if (v == mPieceCustom) {
            loadListeners().show();
        }
        else if (v == confirmationButton) {
            if (!edit && !recover) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonCreate" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                register();
            }
            else if (edit) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (getActivity().getRepeatType() > 0) {
                    List<Integer> date;
                    if (mNavigator.getFragment(1) != null) {
                        date = ((WhenEditFragment) mNavigator.getFragment(1)).getDateFromView();
                    } else {
                        date = new ArrayList<>();
                        date.add(getActivity().getDayStart());
                        date.add(getActivity().getMonthStart() - 1);
                        date.add(getActivity().getYearStart());
                        date.add(getActivity().getDayEnd());
                        date.add(getActivity().getMonthEnd() - 1);
                        date.add(getActivity().getYearEnd());
                        date.add(getActivity().getMinuteStart());
                        date.add(getActivity().getHourStart());
                        date.add(getActivity().getMinuteEnd());
                        date.add(getActivity().getHourEnd());
                    }

                    int d = date.get(0);
                    int m = date.get(1) + 1;
                    int y = date.get(2);

                    if (sameDay(y, m, d, getActivity().getYearStart(), getActivity().getMonthStart(), getActivity().getDayStart())
                            && sameDay(date.get(5), date.get(4) + 1, date.get(3), getActivity().getYearEnd(), getActivity().getMonthEnd(), getActivity().getDayEnd()))
                        edit_activity(false);
                    else
                        createDialogEditWithRepeat();
                } else
                    edit_activity(false);
            } else {
                if (getActivity().getRepeatType() > 0)
                    createDialogRecoverWithRepeat();
                else {
                    register_recover();
                }
            }
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if (v == privacyBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "privacyBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogPrivacy();

        } else if (v == icon2) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon2" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(AddActivity.this, RecoverActivity.class));
            finish();
        }

    }

    private void register() {

        List<Integer> date = new ArrayList<>();
        List<Integer> repeat = new ArrayList<>();
        List<Double> latLng = new ArrayList<>();
        int invite = 0;
        List<User> list_guest = new ArrayList<>();
        int cube_color;
        int cube_color_upper;
        String cube_icon;
        String location = "";
        String title = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(0);
        String description = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(1);
        String whatsapp = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(2);
        List<Tag> tags = ((WhatEditFragment) mNavigator.getFragment(0)).getTags();

        if (mNavigator.getFragment(1) != null) {
            date = ((WhenEditFragment) mNavigator.getFragment(1)).getDateFromView();
            repeat = ((WhenEditFragment) mNavigator.getFragment(1)).getRepeatFromView();
            location = ((WhenEditFragment) mNavigator.getFragment(1)).getLocationFromView();
            latLng = ((WhenEditFragment) mNavigator.getFragment(1)).getLatLngFromView();
        }
        if (mNavigator.getFragment(2) != null) {
            list_guest = ((WhoEditFragment) mNavigator.getFragment(2)).getGuestFromView();
            invite = ((WhoEditFragment) mNavigator.getFragment(2)).getPrivacyFromView();
        }

        if (mColorView == null) {
            cube_color = ContextCompat.getColor(getApplication(), R.color.deep_purple_400);
            cube_color_upper = ContextCompat.getColor(getApplication(), R.color.deep_purple_400_light);
            cube_icon = Constants.IC_ADD_CUBE_URL;
        } else {
            cube_color = mColorView.getTag() == null ? ContextCompat.getColor(getApplication(), R.color.deep_purple_400) : (int) mColorView.getTag();
            cube_color_upper = mColorViewUpper.getTag() == null ? ContextCompat.getColor(getApplication(), R.color.deep_purple_400_light) : (int) mColorViewUpper.getTag();
            cube_icon = urlIcon;
        }

        int err = 0;

        if (!validateFields(title)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_title_required, Toast.LENGTH_LONG).show();
        }  else if (tags.size() == 0) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_tag_required, Toast.LENGTH_LONG).show();
        } else if (date.size() == 0 || date.get(0) == -1 || date.get(6) == -1) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_date_required, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) != 0 && repeat.get(1) < 0)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_repetitions_required, Toast.LENGTH_LONG).show();
        } else if (repeat.get(1) == 0 || repeat.get(1) > 30) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_repetitions_required_2, Toast.LENGTH_LONG).show();
        } else if (!isActivityReadyRegister(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0))) {
            err++;
            Toast.makeText(getApplicationContext(), getErrorMessage(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0)), Toast.LENGTH_LONG).show();
        }

        if (err == 0) {

            int repeat_type = repeat.get(0);
            int repeat_qty = repeat.get(1);
            List<Integer> day_list_start = new ArrayList<>();
            List<Integer> month_list_start = new ArrayList<>();
            List<Integer> year_list_start = new ArrayList<>();
            List<Integer> day_list_end = new ArrayList<>();
            List<Integer> month_list_end = new ArrayList<>();
            List<Integer> year_list_end = new ArrayList<>();
            List<Long> date_time_list_start = new ArrayList<>();
            List<Long> date_time_list_end = new ArrayList<>();

            if (repeat_type > 0) {
                int repeat_adder = getRepeatAdder(repeat_type);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.clear(Calendar.MINUTE);
                cal2.clear(Calendar.SECOND);
                cal2.clear(Calendar.MILLISECOND);

                cal.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
                cal2.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));

                for (int i = 0; i < repeat_qty; i++) {
                    day_list_start.add(cal.get(Calendar.DAY_OF_MONTH));
                    month_list_start.add(cal.get(Calendar.MONTH) + 1);
                    year_list_start.add(cal.get(Calendar.YEAR));
                    day_list_end.add(cal2.get(Calendar.DAY_OF_MONTH));
                    month_list_end.add(cal2.get(Calendar.MONTH) + 1);
                    year_list_end.add(cal2.get(Calendar.YEAR));

                    date_time_list_start.add(cal.getTimeInMillis());
                    date_time_list_end.add(cal2.getTimeInMillis());

                    if (repeat_type == Constants.MONTHLY) {
                        cal.add(Calendar.MONTH, 1);
                        cal2.add(Calendar.MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_WEEK, repeat_adder);
                        cal2.add(Calendar.DAY_OF_WEEK, repeat_adder);
                    }
                }

            }

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String creator = mSharedPreferences.getString(Constants.EMAIL, "");

            ActivityServer activityServer = new ActivityServer();
            activityServer.setTitle(title);
            activityServer.setDescription(description);
            activityServer.setLocation(location);
            activityServer.setInvitationType(invite);

            activityServer.setLat(latLng.get(0));
            activityServer.setLng(latLng.get(1));

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            activityServer.setDayStart(date.get(0));
            activityServer.setMonthStart(date.get(1) + 1);
            activityServer.setYearStart(date.get(2));
            activityServer.setDayEnd(date.get(3));
            activityServer.setMonthEnd(date.get(4) + 1);
            activityServer.setYearEnd(date.get(5));
            activityServer.setMinuteStart(date.get(6));
            activityServer.setHourStart(date.get(7));
            activityServer.setMinuteEnd(date.get(8));
            activityServer.setHourEnd(date.get(9));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar.getTimeInMillis());

            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
            activityServer.setDateTimeEnd(calendar.getTimeInMillis());

            activityServer.setRepeatType(repeat.get(0));
            activityServer.setRepeatQty(repeat.get(1));
            activityServer.setDayListStart(day_list_start);
            activityServer.setMonthListStart(month_list_start);
            activityServer.setYearListStart(year_list_start);
            activityServer.setDayListEnd(day_list_end);
            activityServer.setMonthListEnd(month_list_end);
            activityServer.setYearListEnd(year_list_end);

            activityServer.setDateTimeListStart(date_time_list_start);
            activityServer.setDateTimeListEnd(date_time_list_end);

            activityServer.setCubeColor(cube_color);
            activityServer.setCubeColorUpper(cube_color_upper);
            activityServer.setCubeIcon(cube_icon);

            activityServer.setVisibility(selected);

            //Criptografa a url do whatsapp
            String encryptedValue = "";
            if (whatsapp.length() > 0)
                encryptedValue = converter.toGraphProperty(whatsapp);

            activityServer.setWhatsappGroupLink(encryptedValue);

            int i;
            for (i = 0; i < tags.size(); i++) {
                activityServer.addTags(tags.get(i).text);
            }
            for (i = 1; i < list_guest.size(); i++) {
                activityServer.addGuest(list_guest.get(i).getEmail());
                if (list_guest.get(i).isAdm())
                    activityServer.addAdms(list_guest.get(i).getEmail());
            }

            if(list_guest.size() == 0 && user_friend != null)
                activityServer.addGuest(user_friend.getEmail());

            activityServer.setCreator(creator);

            registerProcess(activityServer);
            setProgress(true);

        } else
            showSnackBarMessage(getResources().getString(R.string.fill_fields_correctly));

    }

    private void register_recover() {

        List<Integer> date;
        List<Integer> repeat = new ArrayList<>();
        List<Double> latLng;
        int invite = 0;
        List<User> list_guest = new ArrayList<>();
        int cube_color;
        int cube_color_upper;
        String cube_icon;
        String location = "";
        String title = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(0);
        String description = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(1);
        String whatsapp = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(2);
        List<Tag> tags = ((WhatEditFragment) mNavigator.getFragment(0)).getTags();

        if (mNavigator.getFragment(1) != null) {
            date = ((WhenEditFragment) mNavigator.getFragment(1)).getDateFromView();
            repeat = ((WhenEditFragment) mNavigator.getFragment(1)).getRepeatFromView();
            location = ((WhenEditFragment) mNavigator.getFragment(1)).getLocationFromView();
            latLng = ((WhenEditFragment) mNavigator.getFragment(1)).getLatLngFromView();
        } else {
            date = new ArrayList<>();
            latLng = new ArrayList<>();
            date.add(getActivity().getDayStart());
            date.add(getActivity().getMonthStart() - 1);
            date.add(getActivity().getYearStart());
            date.add(getActivity().getDayEnd());
            date.add(getActivity().getMonthEnd() - 1);
            date.add(getActivity().getYearEnd());
            date.add(getActivity().getMinuteStart());
            date.add(getActivity().getHourStart());
            date.add(getActivity().getMinuteEnd());
            date.add(getActivity().getHourEnd());

            location = getActivity().getLocation();

            latLng.add(getActivity().getLat());
            latLng.add(getActivity().getLng());

            repeat.add(getActivity().getRepeatType());
            if (repeat.get(0) != 0)
                repeat.add(getActivity().getRepeatQty());
            else
                repeat.add(-1);
        }

        if (mNavigator.getFragment(2) != null) {
            list_guest = ((WhoEditFragment) mNavigator.getFragment(2)).getGuestFromView();
            invite = ((WhoEditFragment) mNavigator.getFragment(2)).getPrivacyFromView();
        } else {
            invite = getActivity().getInvitationType();
            list_guest.addAll(userList);
        }

        if (mColorView == null) {
            cube_color = getActivity().getCubeColor();
            cube_color_upper = getActivity().getCubeColorUpper();
            cube_icon = getActivity().getCubeIcon();
        } else {
            cube_color = mColorView.getTag() == null ? getActivity().getCubeColor() : (int) mColorView.getTag();
            cube_color_upper = mColorViewUpper.getTag() == null ? getActivity().getCubeColorUpper() : (int) mColorViewUpper.getTag();
            cube_icon = urlIcon.matches(Constants.IC_ADD_CUBE_URL) ? getActivity().getCubeIcon() : urlIcon;
        }

        int err = 0;

        if (!validateFields(title)) {

            err++;
            Toast.makeText(getApplicationContext(), R.string.error_title_required, Toast.LENGTH_LONG).show();
        } else if (tags.size() == 0) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_tag_required, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) != 0 && repeat.get(1) < 0)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_repetitions_required, Toast.LENGTH_LONG).show();
        } else if (repeat.get(1) == 0 || repeat.get(1) > 30) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_repetitions_required_2, Toast.LENGTH_LONG).show();
        }else if (!isActivityReadyRegister(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0))) {
            err++;
            Toast.makeText(getApplicationContext(), getErrorMessage(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0)), Toast.LENGTH_LONG).show();
        }

        if (err == 0) {

            int repeat_type = repeat.get(0);
            int repeat_qty = repeat.get(1);
            List<Integer> day_list_start = new ArrayList<>();
            List<Integer> month_list_start = new ArrayList<>();
            List<Integer> year_list_start = new ArrayList<>();
            List<Integer> day_list_end = new ArrayList<>();
            List<Integer> month_list_end = new ArrayList<>();
            List<Integer> year_list_end = new ArrayList<>();
            List<Long> date_time_list_start = new ArrayList<>();
            List<Long> date_time_list_end = new ArrayList<>();

            if (repeat_type > 0) {
                int repeat_adder = getRepeatAdder(repeat_type);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.clear(Calendar.MINUTE);
                cal2.clear(Calendar.SECOND);
                cal2.clear(Calendar.MILLISECOND);

                cal.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
                cal2.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));

                for (int i = 0; i < repeat_qty; i++) {
                    day_list_start.add(cal.get(Calendar.DAY_OF_MONTH));
                    month_list_start.add(cal.get(Calendar.MONTH) + 1);
                    year_list_start.add(cal.get(Calendar.YEAR));
                    day_list_end.add(cal2.get(Calendar.DAY_OF_MONTH));
                    month_list_end.add(cal2.get(Calendar.MONTH) + 1);
                    year_list_end.add(cal2.get(Calendar.YEAR));

                    date_time_list_start.add(cal.getTimeInMillis());
                    date_time_list_end.add(cal2.getTimeInMillis());

                    if (repeat_type == Constants.MONTHLY) {
                        cal.add(Calendar.MONTH, 1);
                        cal2.add(Calendar.MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_WEEK, repeat_adder);
                        cal2.add(Calendar.DAY_OF_WEEK, repeat_adder);
                    }
                }

            }

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String creator = mSharedPreferences.getString(Constants.EMAIL, "");

            ActivityServer activityServer = new ActivityServer();
            activityServer.setTitle(title);
            activityServer.setDescription(description);
            activityServer.setLocation(location);
            activityServer.setInvitationType(invite);

            activityServer.setLat(latLng.get(0));
            activityServer.setLng(latLng.get(1));

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            activityServer.setDayStart(date.get(0));
            activityServer.setMonthStart(date.get(1) + 1);
            activityServer.setYearStart(date.get(2));
            activityServer.setDayEnd(date.get(3));
            activityServer.setMonthEnd(date.get(4) + 1);
            activityServer.setYearEnd(date.get(5));
            activityServer.setMinuteStart(date.get(6));
            activityServer.setHourStart(date.get(7));
            activityServer.setMinuteEnd(date.get(8));
            activityServer.setHourEnd(date.get(9));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar.getTimeInMillis());

            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
            activityServer.setDateTimeEnd(calendar.getTimeInMillis());

            activityServer.setRepeatType(repeat.get(0));
            activityServer.setRepeatQty(repeat.get(1));
            activityServer.setDayListStart(day_list_start);
            activityServer.setMonthListStart(month_list_start);
            activityServer.setYearListStart(year_list_start);
            activityServer.setDayListEnd(day_list_end);
            activityServer.setMonthListEnd(month_list_end);
            activityServer.setYearListEnd(year_list_end);

            activityServer.setDateTimeListStart(date_time_list_start);
            activityServer.setDateTimeListEnd(date_time_list_end);

            activityServer.setCubeColor(cube_color);
            activityServer.setCubeColorUpper(cube_color_upper);
            activityServer.setCubeIcon(cube_icon);

            activityServer.setVisibility(selected);

            //Criptografa a url do whatsapp
            String encryptedValue = "";
            if (whatsapp.length() > 0)
                encryptedValue = converter.toGraphProperty(whatsapp);

            activityServer.setWhatsappGroupLink(encryptedValue);

            int i;
            for (i = 0; i < tags.size(); i++) {
                activityServer.addTags(tags.get(i).text);
            }
            for (i = 1; i < list_guest.size(); i++) {
                activityServer.addGuest(list_guest.get(i).getEmail());
                if (list_guest.get(i).isAdm())
                    activityServer.addAdms(list_guest.get(i).getEmail());
            }

            activityServer.setCreator(creator);

            registerProcess(activityServer);
            setProgress(true);

        } else
            showSnackBarMessage(getResources().getString(R.string.fill_fields_correctly));

    }

    private int getFutureActivities(List<Integer> date) {
        Collections.sort(activityServers, new Comparator<ActivityServer>() {
            @Override
            public int compare(ActivityServer c1, ActivityServer c2) {
                int day = 0, month = 0, year = 0;
                int day2 = 0, month2 = 0, year2 = 0;

                day = c1.getDayStart();
                month = c1.getMonthStart();
                year = c1.getYearStart();

                day2 = c2.getDayStart();
                month2 = c2.getMonthStart();
                year2 = c2.getYearStart();


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

        ArrayList<ActivityServer> list = new ArrayList<>();
        for (int i = 0; i < activityServers.size(); i++) {
            ActivityServer act = activityServers.get(i);
            if (!isDatePrior(act.getDayStart(), act.getMonthStart(), act.getYearStart(), getActivity().getDayStart(), getActivity().getMonthStart(), getActivity().getYearStart()))
                list.add(act);
        }

        return list.size();
    }

    private boolean isDatePrior(int d1, int m1, int y1, int d2, int m2, int y2) {
        if (y1 < y2)
            return true;
        else if (y1 == y2) {
            if (m1 < m2)
                return true;
            else if (m1 == m2)
                if (d1 < d2)
                    return true;
        }
        return false;
    }

    private void edit_activity(boolean repeat) {

        List<Integer> date;
        List<Double> latLng;
        List<Integer> repeat_single = new ArrayList<>();
        int invite = 0;
        int repeat_left = -1;
        int cube_color;
        int cube_color_upper;
        String cube_icon;
        String location = "";
        String title = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(0);
        String description = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(1);

        String whatsapp = ((WhatEditFragment) mNavigator.getFragment(0)).getTextFromView().get(2);
        List<Tag> tags = ((WhatEditFragment) mNavigator.getFragment(0)).getTags();

        if (mNavigator.getFragment(1) != null) {
            date = ((WhenEditFragment) mNavigator.getFragment(1)).getDateFromView();
            repeat_single = ((WhenEditFragment) mNavigator.getFragment(1)).getRepeatFromView(); //Repetir em caso da atividade ser simples ou do google agenda
            location = ((WhenEditFragment) mNavigator.getFragment(1)).getLocationFromView();
            latLng = ((WhenEditFragment) mNavigator.getFragment(1)).getLatLngFromView();
        } else {
            date = new ArrayList<>();
            latLng = new ArrayList<>();

            repeat_single.add(getActivity().getRepeatType());
            repeat_single.add(getActivity().getRepeatQty());

            date.add(getActivity().getDayStart());
            date.add(getActivity().getMonthStart() - 1);
            date.add(getActivity().getYearStart());
            date.add(getActivity().getDayEnd());
            date.add(getActivity().getMonthEnd() - 1);
            date.add(getActivity().getYearEnd());
            date.add(getActivity().getMinuteStart());
            date.add(getActivity().getHourStart());
            date.add(getActivity().getMinuteEnd());
            date.add(getActivity().getHourEnd());

            location = getActivity().getLocation();

            latLng.add(getActivity().getLat());
            latLng.add(getActivity().getLng());
        }

        if (mNavigator.getFragment(2) != null) {
            invite = ((WhoEditFragment) mNavigator.getFragment(2)).getPrivacyFromView();
        } else {
            invite = getActivity().getInvitationType();
        }

        if (mColorView == null) {
            cube_color = getActivity().getCubeColor();
            cube_color_upper = getActivity().getCubeColorUpper();
            cube_icon = getActivity().getCubeIcon();
        } else {
            cube_color = mColorView.getTag() == null ? getActivity().getCubeColor() : (int) mColorView.getTag();
            cube_color_upper = mColorViewUpper.getTag() == null ? getActivity().getCubeColorUpper() : (int) mColorViewUpper.getTag();
            cube_icon = urlIcon.matches(Constants.IC_ADD_CUBE_URL) ? getActivity().getCubeIcon() : urlIcon;
        }

        int err = 0;
        int repeat_type = getActivity().getRepeatType();
        boolean repeat_single_changed = false;

        if (!validateFields(title)) {

            err++;
            Toast.makeText(getApplicationContext(), R.string.error_title_required, Toast.LENGTH_LONG).show();
        } else if (tags.size() == 0) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.error_tag_required, Toast.LENGTH_LONG).show();
        } else if (!isActivityReadyRegister(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), getActivity().getRepeatType())) {
            err++;
            Toast.makeText(getApplicationContext(), getErrorMessage(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), getActivity().getRepeatType()), Toast.LENGTH_LONG).show();
        }else if ((repeat_type == 0 || repeat_type == 5) && repeat_type != repeat_single.get(0)) {
            repeat_type = repeat_single.get(0);
            repeat_single_changed = true;
            repeat = true;
            if ((repeat_single.get(0) != 0 && repeat_single.get(1) < 0)) {
                err++;
                Toast.makeText(getApplicationContext(), R.string.error_repetitions_required, Toast.LENGTH_LONG).show();
            } else if (repeat_single.get(1) == 0 || repeat_single.get(1) > 30) {
                err++;
                Toast.makeText(getApplicationContext(), R.string.error_repetitions_required_2, Toast.LENGTH_LONG).show();
            }
        }

        if (err == 0) {

            List<Integer> day_list_start = new ArrayList<>();
            List<Integer> month_list_start = new ArrayList<>();
            List<Integer> year_list_start = new ArrayList<>();
            List<Integer> day_list_end = new ArrayList<>();
            List<Integer> month_list_end = new ArrayList<>();
            List<Integer> year_list_end = new ArrayList<>();
            List<Long> date_time_list_start = new ArrayList<>();
            List<Long> date_time_list_end = new ArrayList<>();

            if (repeat_type > 0) {
                int repeat_adder = getRepeatAdder(repeat_type);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.clear(Calendar.MINUTE);
                cal2.clear(Calendar.SECOND);
                cal2.clear(Calendar.MILLISECOND);

                cal.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
                cal2.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));

                if (!repeat_single_changed)
                    repeat_left = getFutureActivities(date);
                else
                    repeat_left = repeat_single.get(1);

                for (int i = 0; i < repeat_left; i++) {
                    day_list_start.add(cal.get(Calendar.DAY_OF_MONTH));
                    month_list_start.add(cal.get(Calendar.MONTH) + 1);
                    year_list_start.add(cal.get(Calendar.YEAR));
                    day_list_end.add(cal2.get(Calendar.DAY_OF_MONTH));
                    month_list_end.add(cal2.get(Calendar.MONTH) + 1);
                    year_list_end.add(cal2.get(Calendar.YEAR));

                    date_time_list_start.add(cal.getTimeInMillis());
                    date_time_list_end.add(cal2.getTimeInMillis());

                    if (repeat_type == Constants.MONTHLY) {
                        cal.add(Calendar.MONTH, 1);
                        cal2.add(Calendar.MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_WEEK, repeat_adder);
                        cal2.add(Calendar.DAY_OF_WEEK, repeat_adder);
                    }
                }

            }

            ActivityServer activityServer = new ActivityServer();
            activityServer.setTitle(title);
            activityServer.setDescription(description);
            activityServer.setLocation(location);
            activityServer.setInvitationType(invite);

            activityServer.setLat(latLng.get(0));
            activityServer.setLng(latLng.get(1));

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            activityServer.setDayStart(date.get(0));
            activityServer.setMonthStart(date.get(1) + 1);
            activityServer.setYearStart(date.get(2));
            activityServer.setDayEnd(date.get(3));
            activityServer.setMonthEnd(date.get(4) + 1);
            activityServer.setYearEnd(date.get(5));
            activityServer.setMinuteStart(date.get(6));
            activityServer.setHourStart(date.get(7));
            activityServer.setMinuteEnd(date.get(8));
            activityServer.setHourEnd(date.get(9));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar.getTimeInMillis());

            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
            activityServer.setDateTimeEnd(calendar.getTimeInMillis());

            activityServer.setDayListStart(day_list_start);
            activityServer.setMonthListStart(month_list_start);
            activityServer.setYearListStart(year_list_start);
            activityServer.setDayListEnd(day_list_end);
            activityServer.setMonthListEnd(month_list_end);
            activityServer.setYearListEnd(year_list_end);

            activityServer.setDateTimeListStart(date_time_list_start);
            activityServer.setDateTimeListEnd(date_time_list_end);

            activityServer.setCubeColor(cube_color);
            activityServer.setCubeColorUpper(cube_color_upper);
            activityServer.setCubeIcon(cube_icon);

            activityServer.setVisibility(selected);

            //Criptografa a url do whatsapp
            String encryptedValue = "";
            if (whatsapp.length() > 0)
                encryptedValue = converter.toGraphProperty(whatsapp);

            activityServer.setWhatsappGroupLink(encryptedValue);

            int i;
            for (i = 0; i < tags.size(); i++) {
                activityServer.addTags(tags.get(i).text);
            }

            if (repeat_type > 0) {
                if (sameDay(y, m, d, getActivity().getYearStart(), getActivity().getMonthStart(), getActivity().getDayStart())
                        && sameDay(date.get(5), date.get(4) + 1, date.get(3), getActivity().getYearEnd(), getActivity().getMonthEnd(), getActivity().getDayEnd())) // So editar os dados
                    activityServer.setId(1);
                else
                    activityServer.setId(2);
            } else
                activityServer.setId(0);


            if (repeat) {
                activityServer.setRepeatType(repeat_type);
                activityServer.setRepeatQty(repeat_left);
            } else {
                activityServer.setRepeatType(0);
                activityServer.setRepeatQty(-1);
            }

            if (!repeat_single_changed)
                editActivity(activityServer);
            else {
                editActivityRepeatSingle(activityServer);
            }

            setProgress(true);
        }
    }

    private boolean isActivityReadyRegister(int y1, int m1, int d1, int y2, int m2, int d2, int period) {
        LocalDate start = new LocalDate(y1, m1 + 1, d1);
        LocalDate end = new LocalDate(y2, m2 + 1, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        if (timePeriod.getDays() > 15)
            return false;

        switch (period) {
            case 1:
                if (timePeriod.getDays() > 0)
                    return false;
            case 2:
                if (timePeriod.getDays() > 6)
                    return false;
            case 3:
                if (timePeriod.getDays() > 29)
                    return false;
            default:
                return true;
        }
    }

    private String getErrorMessage(int y1, int m1, int d1, int y2, int m2, int d2, int period) {
        LocalDate start = new LocalDate(y1, m1, d1);
        LocalDate end = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        if (timePeriod.getDays() > 15)
            return getResources().getString(R.string.error_act_max_lenght_days);

        switch (period) {
            case 1:
                return getResources().getString(R.string.error_act_max_lenght_days_daily);
            case 2:
                return getResources().getString(R.string.error_act_max_lenght_days_weekly);
            case 3:
                return getResources().getString(R.string.error_act_max_lenght_days_monthly);
            default:
                return "";
        }
    }

    private boolean sameDay(int y1, int m1, int d1, int y2, int m2, int d2) {
        return d1 == d2 && m1 == m2 && y1 == y2;
    }

    private int getRepeatAdder(int type) {
        switch (type) {
            case Constants.DAYLY:
                return 1;
            case Constants.WEEKLY:
                return 7;
            default:
                return 0;
        }
    }

    private void registerProcess(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivity(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void editActivity(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editActivity(getActivity().getId(), activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void editActivityRepeatSingle(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editActivityRepeatSingle(getActivity().getId(), activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("d", d);
        intent.putExtra("m", m);
        intent.putExtra("y", y);
        setResult(RESULT_OK, intent);
        if(user_friend == null)
            finish();
        else
            startActivity(intent);

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

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    private void showSnackBarMessage(String message) {

        if (findViewById(android.R.id.content) != null) {

            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private MaterialDialog loadListeners() {
        LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.activity_customize_piece, null);
        mColorPickerView = (ColorPickerView) customView.findViewById(R.id.colorpicker);
        mColorView = (ImageView) customView.findViewById(R.id.cubeLowerBoxIcon);
        mColorViewUpper = (ImageView) customView.findViewById(R.id.cubeUpperBoxIcon);
        mColorIcon = (ImageView) customView.findViewById(R.id.pieceIcon);
        ImageView closeButton = (ImageView) customView.findViewById(R.id.closeButton);
        RecyclerView recyclerView = (RecyclerView) customView.findViewById(R.id.recyclerIcons);

        mColorPickerView.setDrawDebug(false);

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(customView, false)
                .build();

        mOkButton = (TextView) customView.findViewById(R.id.applyButton);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mColorViewMain.setColorFilter((int) mColorView.getTag());
                mColorViewUpperMain.setColorFilter((int) mColorViewUpper.getTag());

                Glide.clear(mColorIconMain);
                Glide.with(AddActivity.this)
                        .load(urlIcon)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mColorIconMain);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mOkButtonPickIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        mCancelButton = (TextView) customView.findViewById(R.id.cleanButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mColorView.setColorFilter(ContextCompat.getColor(customView.getContext(), R.color.deep_purple_400));
                mColorViewUpper.setColorFilter(ContextCompat.getColor(customView.getContext(), R.color.deep_purple_400_light));
                mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400));
                mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400_light));

                Glide.clear(mColorIcon);
                Glide.with(AddActivity.this)
                        .load(Constants.IC_ADD_CUBE_URL)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mColorIcon);

                urlIcon = Constants.IC_ADD_CUBE_URL;

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mCancelButtonPickIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });


        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 5);
        recyclerView.setLayoutManager(layoutManager);

        CustomizeAddActivityAdapter adapter;

        recyclerView.setAdapter(adapter = new CustomizeAddActivityAdapter(this, iconList));

        RecyclerItemClickListener.OnItemClickListener onItemClickListener = new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {

                Glide.clear(mColorIcon);
                Glide.with(AddActivity.this)
                        .load(adapter.getItem(position).getUrl())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mColorIcon);

                urlIcon = adapter.getItem(position).getUrl();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "RecyclerItemPickIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        };

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView, onItemClickListener)
        );

        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {

            @Override
            public void onColorSelected(int color) {

                if (color == ContextCompat.getColor(AddActivity.this, R.color.red_A700)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.red_A700));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.red_A700_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.red_A700));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.red_A700_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.pink_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.pink_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.pink_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.pink_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.pink_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.pink_900)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.pink_900));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.pink_900_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.pink_900));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.pink_900_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.purple_500)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.purple_500));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.purple_500_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.purple_500));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.purple_500_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.deep_purple_800)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_800));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_800_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_800));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_purple_800_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.blue_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.blue_800)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_800));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_800_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_800));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_800_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.cyan_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.cyan_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.cyan_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.cyan_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.cyan_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.cyan_800)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.cyan_800));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.cyan_800_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.cyan_800));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.cyan_800_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.green_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.green_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.green_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.green_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.green_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.lime_600)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.lime_600));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.lime_600_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.lime_600));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.lime_600_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.deep_orange_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.deep_orange_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.deep_orange_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_orange_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.deep_orange_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.brown_400)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.brown_400));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.brown_400_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.brown_400));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.brown_400_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.brown_700)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.brown_700));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.brown_700_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.brown_700));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.brown_700_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.grey_500)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.grey_500));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.grey_500_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.grey_500));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.grey_500_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.blue_grey_500)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_500));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_500_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_500));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_500_light));
                } else if (color == ContextCompat.getColor(AddActivity.this, R.color.blue_grey_900)) {
                    mColorView.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_900));
                    mColorViewUpper.setColorFilter(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_900_light));
                    mColorView.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_900));
                    mColorViewUpper.setTag(ContextCompat.getColor(AddActivity.this, R.color.blue_grey_900_light));
                }

            }
        });

        //Set this to true, to enable visual debugging. To check the offset radius
        mColorPickerView.setDrawDebug(false);

        if (urlIcon.matches("")) {
            Glide.clear(mColorIcon);
            Glide.with(AddActivity.this)
                    .load(Constants.IC_ADD_CUBE_URL)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mColorIcon);

            urlIcon = Constants.IC_ADD_CUBE_URL;
        } else {
            Glide.clear(mColorIcon);
            Glide.with(AddActivity.this)
                    .load(urlIcon)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mColorIcon);
        }

        return dialog;
    }

    public boolean getEditable() {
        return edit;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    private void createDialogPrivacy() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_plans_visibility, null);

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

        if (edit){
            privacyUpdate.setCreator(email);
            privacyUpdate.setId(getActivity().getId());
        }

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

                if(edit) {
                    privacyUpdate.setVisibility(selected);
                    setPrivacyActivity(privacyUpdate);
                }

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



                if(edit) {
                    privacyUpdate.setVisibility(selected);
                    setPrivacyActivity(privacyUpdate);
                }

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


                if(edit) {
                    privacyUpdate.setVisibility(selected);
                    setPrivacyActivity(privacyUpdate);
                }

                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private void createDialogEditWithRepeat() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);
        RadioGroup radioGroup = (RadioGroup) customView.findViewById(R.id.radioGroup);

        editText.setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.dialog_edit_reminder_title));
        text2.setVisibility(View.GONE);
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.confirm));

        radioGroup.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
        text2.setText(getResources().getString(R.string.dialog_edit_reminder_text));

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonID;
                View radioButton;
                int idx = -1;

                radioButtonID = radioGroup.getCheckedRadioButtonId();
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);

                edit_activity(idx == 1);

                dg.dismiss();
            }
        });

        dg.show();
    }

    private void createDialogRecoverWithRepeat() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text1.setVisibility(View.GONE);

        text2.setText(getResources().getString(R.string.create_activity_recover_with_repeat_text));
        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().setRepeatType(0);
                getActivity().setRepeatQty(-1);
                register_recover();
                dg.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_recover();
                dg.dismiss();
            }
        });

        dg.show();
    }

}
