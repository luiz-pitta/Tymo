package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.widget.Space;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.adapters.FlagFragmentAdapter;
import io.development.tymo.fragments.FlagEditFragment;
import io.development.tymo.fragments.FlagShowFragment;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.UpdateButtonController;
import io.development.tymo.R;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FlagActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private FragmentNavigator mNavigator;
    private UpdateButtonController controller;
    private RelativeLayout bottomBarBox, bottomBarBoxFitRemove, mainBox;
    private int actionColor, actionColorPressed;
    private ArrayList<FlagServer> flagServers;

    private TextView confirmationButton;

    private int d, m, y;

    private TextView actionText, titleText, privacyText, editButton;
    private ImageView mBackButton, privacyIcon;
    private TextView availableText;
    private TextView unavailableText;
    private ImageView availableButton;
    private ImageView unavailableButton;
    private View availableCorners, unavailableCorners;

    private int type;
    private boolean free = true, act_free = false, friend_free;
    private boolean edit = false;
    private final static int CREATE_EDIT_FLAG = 0, SHOW_FLAG = 1;
    private FlagWrapper flagWrapper;
    private boolean permissionInvite = false;

    private int avaliableIcon, avaliableBg, unavaliableIcon, unavaliableBg;

    private CompositeDisposable mSubscriptions;

    private boolean error;

    private User creator_flag, user_friend = null;
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> invitedList = new ArrayList<>();
    private ArrayList<User> confirmedList = new ArrayList<>();
    private ArrayList<User> listUserCompare = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();

    private LinearLayout availableBox, unavailableBox, confirmationButtonFit, confirmationButtonRemove, confirmationButtonPast;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag);

        mSubscriptions = new CompositeDisposable();

        mainBox = (RelativeLayout) findViewById(R.id.mainBox);
        availableBox = (LinearLayout) findViewById(R.id.availableBox);
        unavailableBox = (LinearLayout) findViewById(R.id.unavailableBox);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        availableText = (TextView) findViewById(R.id.availableText);
        unavailableText = (TextView) findViewById(R.id.unavailableText);
        availableButton = (ImageView) findViewById(R.id.availableIcon);
        unavailableButton = (ImageView) findViewById(R.id.unavailableIcon);
        availableCorners = findViewById(R.id.availableCorners);
        unavailableCorners = findViewById(R.id.unavailableCorners);
        bottomBarBox = (RelativeLayout) findViewById(R.id.confirmationButtonBar);
        bottomBarBoxFitRemove = (RelativeLayout) findViewById(R.id.confirmationButtonBarFitRemove);
        actionText = (TextView) findViewById(R.id.text);
        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        confirmationButtonFit = (LinearLayout) findViewById(R.id.confirmationButtonFit);
        confirmationButtonRemove = (LinearLayout) findViewById(R.id.confirmationButtonRemove);
        confirmationButtonPast = (LinearLayout) findViewById(R.id.confirmationButtonPast);
        privacyIcon = (ImageView) findViewById(R.id.privacyIcon);
        privacyText = (TextView) findViewById(R.id.privacyText);
        editButton = (TextView) findViewById(R.id.editButton);
        titleText = (TextView) findViewById(R.id.title);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        actionColor = ContextCompat.getColor(this, R.color.grey_600);
        actionColorPressed = ContextCompat.getColor(this, R.color.grey_400);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setDistanceToTriggerSync(700);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.deep_purple_400));

        error = false;

        type = getIntent().getIntExtra("type_flag", 0);

        editButton.setVisibility(View.GONE);

        if (type == CREATE_EDIT_FLAG) {
            bottomBarBoxFitRemove.setVisibility(View.GONE);
            avaliableBg = R.drawable.bg_shape_oval_available_corners_2;
            unavaliableBg = R.drawable.bg_shape_oval_unavailable_corners_2;
            avaliableIcon = R.drawable.ic_flag;
            unavaliableIcon = R.drawable.ic_flag;
            titleText.setVisibility(View.GONE);
            mSwipeRefreshLayout.setEnabled(false);
            confirmationButton.setText(R.string.create);
            friend_free = getIntent().getBooleanExtra("flag_free_friend", false);
            actionText.setText(getResources().getString(R.string.create_flag));
            mBackButton.setImageResource(R.drawable.ic_add);
            mBackButton.setRotation(45);

            flagWrapper = (FlagWrapper) getIntent().getSerializableExtra("flag_edit");
            if (flagWrapper != null) {
                actionText.setText(getResources().getString(R.string.edit_flag));
                mBackButton.setImageResource(R.drawable.ic_add);
                mBackButton.setRotation(45);
                edit = true;
                confirmationButton.setText(R.string.save_updates);
            } else {
                flagWrapper = (FlagWrapper) getIntent().getSerializableExtra("flag_free");
                if (flagWrapper != null) {
                    act_free = true;
                    if (friend_free) {
                        unavailableBox.setVisibility(View.GONE);
                        UserWrapper userWrapper = (UserWrapper) getIntent().getSerializableExtra("flag_free_friend_usr");
                        if (userWrapper != null)
                            user_friend = userWrapper.getUser();
                        else {
                            userWrapper = (UserWrapper) getIntent().getSerializableExtra("ListCreateActivityCompare");
                            if (userWrapper != null) {
                                listUserCompare = userWrapper.getUsers();
                                listUserCompare.remove(0);
                            }
                        }
                    }

                }
            }

            if (flagWrapper != null) {
                setProgress(true);
                FlagServer flagServer = new FlagServer();
                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                flagServer.setId(0);
                flagServer.setCreator(email);
                flagServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                setFlagInformation(flagWrapper.getFlagServer().getId(), flagServer);
            }

            privacyIcon.setImageResource(R.drawable.ic_lock);
            privacyText.setText(getResources().getString(R.string.flag_privacy));

            if (edit) {
                avaliableBg = R.drawable.bg_shape_oval_available_corners;
                unavaliableBg = R.drawable.bg_shape_oval_unavailable_corners;
                avaliableIcon = R.drawable.ic_flag_available;
                unavaliableIcon = R.drawable.ic_flag_unavailable;
                if (flagWrapper.getFlagServer().getType()) {
                    paintMainBox(true);
                    unavailableBox.setVisibility(View.GONE);
                } else {
                    paintMainBox(false);
                    availableBox.setVisibility(View.GONE);
                }
            }
        } else {
            avaliableBg = R.drawable.bg_shape_oval_available_corners;
            unavaliableBg = R.drawable.bg_shape_oval_unavailable_corners;
            avaliableIcon = R.drawable.ic_flag_available;
            unavaliableIcon = R.drawable.ic_flag_unavailable;
            mSwipeRefreshLayout.setEnabled(true);
            actionText.setText(getResources().getString(R.string.flag));
            bottomBarBox.setVisibility(View.GONE);
            flagWrapper = (FlagWrapper) getIntent().getSerializableExtra("flag_show");
            FlagServer flagServer = new FlagServer();
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            flagServer.setId(0);
            flagServer.setCreator(email);
            flagServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            setFlagInformation(flagWrapper.getFlagServer().getId(), flagServer);
            setProgress(true);

            if (flagWrapper.getFlagServer().getType()) {
                paintMainBox(true);
                unavailableBox.setVisibility(View.GONE);
            } else {
                paintMainBox(false);
                availableBox.setVisibility(View.GONE);
            }
        }

        //Set Listners
        availableText.setOnClickListener(this);
        unavailableText.setOnClickListener(this);
        availableButton.setOnClickListener(this);
        unavailableButton.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        editButton.setOnTouchListener(this);

        confirmationButtonFit.setOnClickListener(this);
        confirmationButtonRemove.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);

        //set button controller
        controller = new UpdateButtonController(this);
        controller.attach(true, availableText, availableButton, availableCorners);
        controller.attach(false, unavailableText, unavailableButton, unavailableCorners);
        controller.updateAll(0, R.color.flag_available, R.color.flag_available, avaliableBg);
        availableButton.setImageResource(avaliableIcon);
        availableButton.clearColorFilter();


        mNavigator = new FragmentNavigator(getFragmentManager(), new FlagFragmentAdapter(), R.id.contentBox);
        mNavigator.setDefaultPosition(type);
        mNavigator.onCreate(savedInstanceState);
        mNavigator.showFragment(type);

        mBackButton.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    public void paintMainBox(boolean free) {
        mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        actionText.setTextColor(ContextCompat.getColor(this, R.color.white));
        editButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        privacyIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
        privacyText.setTextColor(ContextCompat.getColor(this, R.color.white));
        titleText.setTextColor(ContextCompat.getColor(this, R.color.white));

        availableText.setVisibility(View.GONE);
        unavailableText.setVisibility(View.GONE);

        actionColor = ContextCompat.getColor(this, R.color.white);

        if (free) {
            actionText.setText(getString(R.string.flag_menu_available_action_text));
            mainBox.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_flag_main_box_available));
            actionColorPressed = ContextCompat.getColor(this, R.color.green_100);
        } else {
            actionText.setText(getString(R.string.flag_menu_unavailable_action_text));
            mainBox.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_flag_main_box_unavailable));
            actionColorPressed = ContextCompat.getColor(this, R.color.red_100);
        }
    }

    public NestedScrollView getScrollView() {
        return (NestedScrollView) findViewById(R.id.scrollView);
    }

    public void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                mSwipeRefreshLayout.setRefreshing(false);
                if (type == SHOW_FLAG) {
                    FlagServer flagServer = new FlagServer();
                    SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    String email = mSharedPreferences.getString(Constants.EMAIL, "");
                    flagServer.setId(0);
                    flagServer.setCreator(email);
                    flagServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                    setFlagInformation(flagWrapper.getFlagServer().getId(), flagServer);
                    setProgress(true);

                    if (flagWrapper.getFlagServer().getType()) {
                        unavailableBox.setVisibility(View.GONE);
                    } else {
                        availableBox.setVisibility(View.GONE);
                    }
                }
            }
        }, 500);

        // Load complete
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    public FlagServer getFlag() {
        if (flagWrapper != null)
            return flagWrapper.getFlagServer();
        else
            return null;
    }

    @Nullable
    private User getCreator(ArrayList<User> users, User creator) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().contains(creator.getEmail()))
                return users.get(i);
        }
        return null;
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

    private ArrayList<User> getConfirmed(ArrayList<User> users) {
        ArrayList<User> confirmed = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (!users.get(i).getEmail().equals(creator_flag.getEmail()) && users.get(i).getInvitation() == 1)
                confirmed.add(users.get(i));
        }
        return setOrderGuests(confirmed);
    }

    private ArrayList<User> getInvited(ArrayList<User> users) {
        ArrayList<User> confirmed = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if ((users.get(i).getInvitation() == 0 || users.get(i).getInvitation() == 2) && !users.get(i).getEmail().equals(creator_flag.getEmail()))
                confirmed.add(users.get(i));
        }
        return setOrderGuests(confirmed);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setFlagInformation(long id, FlagServer flagServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getFlag2(id, flagServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFlagInformation, this::handleError));
    }

    public User getUserFriend() {
        return user_friend;
    }

    public ArrayList<User> getListUserCompare() {
        return listUserCompare;
    }

    private void handleFlagInformation(Response response) {

        invitedList.clear();
        confirmedList.clear();
        userList.clear();

        userList = response.getPeople();

        creator_flag = getCreator(userList, response.getUser());
        if (creator_flag != null)
            creator_flag.setCreator(true);

        invitedList.add(creator_flag);
        if (user_friend != null)
            invitedList.add(user_friend);
        else if (listUserCompare.size() > 0)
            invitedList.addAll(listUserCompare);

        invitedList.addAll(getConfirmed(userList));
        invitedList.addAll(getInvited(userList));

        confirmedList.add(creator_flag);
        confirmedList.addAll(getConfirmed(userList));

        permissionInvite = response.getUser() != null && checkIfCreator(response.getUser().getEmail());

        if (type == CREATE_EDIT_FLAG) {
            titleText.setVisibility(View.GONE);
            FlagEditFragment flagEditFragment = (FlagEditFragment) mNavigator.getFragment(type);
            flagEditFragment.setLayout(flagWrapper.getFlagServer(), response.getWhatsGoingFlag(), invitedList, confirmedList, edit, act_free, user_friend != null || listUserCompare.size() > 0);

            if (flagWrapper.getFlagServer().getType() || act_free) {
                privacyIcon.setImageResource(R.drawable.ic_lock);
                privacyText.setText(getResources().getString(R.string.flag_privacy));
            } else {
                privacyIcon.setImageResource(R.drawable.ic_public);
                privacyText.setText(getResources().getString(R.string.flag_privacy_public));
                controller.updateAll(1, R.color.flag_unavailable, R.color.flag_unavailable, unavaliableBg);
                unavailableButton.setImageResource(unavaliableIcon);
                unavailableButton.clearColorFilter();
            }
        } else {
            FlagShowFragment flagShowFragment = (FlagShowFragment) mNavigator.getFragment(type);
            flagShowFragment.setLayout(flagWrapper.getFlagServer(), invitedList, confirmedList, response.getWhatsGoingFlag(), permissionInvite);

            if (flagWrapper.getFlagServer().getType()) {
                paintMainBox(true);
            } else {
                paintMainBox(false);
            }

            if (!flagWrapper.getFlagServer().getTitle().matches("")) {
                titleText.setVisibility(View.VISIBLE);
                titleText.setText(flagWrapper.getFlagServer().getTitle());
            } else {
                titleText.setVisibility(View.GONE);
            }

            User user = checkIfInFlag(userList);

            confirmationButtonRemove.setVisibility(View.VISIBLE);
            confirmationButtonFit.setVisibility(View.GONE);

            if (!flagWrapper.getFlagServer().getType()) {
                editButton.setVisibility(View.VISIBLE);
                controller.updateAll(1, R.color.flag_unavailable, R.color.flag_unavailable, unavaliableBg);
                unavailableButton.setImageResource(unavaliableIcon);
                unavailableButton.clearColorFilter();
            } else if (permissionInvite) {
                editButton.setVisibility(View.VISIBLE);
            } else {
                editButton.setVisibility(View.GONE);

                if (user != null) {
                    if (user.getInvitation() == 1) {
                        confirmationButtonRemove.setVisibility(View.VISIBLE);
                        confirmationButtonFit.setVisibility(View.GONE);
                    } else {
                        confirmationButtonRemove.setVisibility(View.GONE);
                        confirmationButtonFit.setVisibility(View.VISIBLE);
                    }
                }
            }


            if (flagWrapper.getFlagServer().getType()) {
                privacyIcon.setImageResource(R.drawable.ic_lock);
                privacyText.setText(getResources().getString(R.string.flag_privacy));
            } else {
                privacyIcon.setImageResource(R.drawable.ic_public);
                privacyText.setText(getResources().getString(R.string.flag_privacy_public));
            }

            if (flagWrapper.getFlagServer().getDateTimeEnd() < Calendar.getInstance().getTimeInMillis() && confirmationButtonRemove.getVisibility() == View.GONE) {
                confirmationButtonFit.setVisibility(View.GONE);
                confirmationButtonPast.setVisibility(View.VISIBLE);
            } else {
                confirmationButtonPast.setVisibility(View.GONE);
            }
        }

        flagServers = response.getWhatsGoingFlag();

        setProgress(false);
    }

    private User checkIfInFlag(ArrayList<User> usr) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        for (int i = 0; i < usr.size(); i++) {
            if (email.equals(usr.get(i).getEmail()))
                return usr.get(i);
        }

        return null;
    }

    private boolean checkIfCreator(String email_creator) {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        return email.equals(email_creator);
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
        LocalDate start = new LocalDate(y1, m1 + 1, d1);
        LocalDate end = new LocalDate(y2, m2 + 1, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        if (timePeriod.getDays() > 15)
            return getResources().getString(R.string.validation_field_act_max_lenght_days);

        switch (period) {
            case 1:
                return getResources().getString(R.string.validation_field_act_max_lenght_days_daily);
            case 2:
                return getResources().getString(R.string.validation_field_act_max_lenght_days_weekly);
            case 3:
                return getResources().getString(R.string.validation_field_act_max_lenght_days_monthly);
            default:
                return "";
        }
    }

    private boolean validateDateTime(Calendar calendarStart, Calendar calendarEnd) {
        if (calendarEnd.getTimeInMillis() < calendarStart.getTimeInMillis()) {
            return false;
        }

        return true;
    }

    private void register() {

        List<Integer> date;
        List<Integer> repeat;
        List<User> list_guest = new ArrayList<>();
        boolean dateStartEmpty = false, dateEndEmpty = false, timeStartEmpty = false, timeEndEmpty = false;
        String title = ((FlagEditFragment) mNavigator.getFragment(0)).getTitleFromView();

        date = ((FlagEditFragment) mNavigator.getFragment(0)).getDateFromView();
        repeat = ((FlagEditFragment) mNavigator.getFragment(0)).getRepeatFromView();

        boolean sendAll = ((FlagEditFragment) mNavigator.getFragment(0)).getSendToAll() == 0;

        if (date.get(0) == -1) {
            dateStartEmpty = true;
        } else {
            dateStartEmpty = false;
        }

        if (date.get(3) == -1) {
            dateEndEmpty = true;
            date.set(3, date.get(0));
            date.set(4, date.get(1));
            date.set(5, date.get(2));
        } else {
            dateEndEmpty = false;
        }

        if (date.get(6) == -1) {
            timeStartEmpty = true;
            date.set(6, 0);
            date.set(7, 0);
        } else {
            timeStartEmpty = false;
        }

        if (date.get(8) == -1) {
            timeEndEmpty = true;
            date.set(8, 59);
            date.set(9, 23);
        } else {
            timeEndEmpty = false;
        }

        if (!sendAll) {
            list_guest = ((FlagEditFragment) mNavigator.getFragment(0)).getGuestFromView();
        } else {

        }

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();

        calendarStart.set(date.get(2), date.get(1), date.get(0));
        calendarEnd.set(date.get(5), date.get(4), date.get(3));
        boolean validateDate = validateDateTime(calendarStart, calendarEnd);

        calendarStart.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
        calendarEnd.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));
        boolean validateTime = validateDateTime(calendarStart, calendarEnd);

        int err = 0;
        if (dateStartEmpty) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_start_required, Toast.LENGTH_LONG).show();
        } else if (!validateDate) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_end_before_start, Toast.LENGTH_LONG).show();
        } else if (!validateTime) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_time_end_before_start, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) != 0 && repeat.get(1) <= 0)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) == 1 && repeat.get(1) > 365) || (repeat.get(0) == 2 && repeat.get(1) > 53) || (repeat.get(0) == 3 && repeat.get(1) > 12)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_min_max, Toast.LENGTH_LONG).show();
        } else if (!isActivityReadyRegister(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0))) {
            err++;
            Toast.makeText(getApplicationContext(), getErrorMessage(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0)), Toast.LENGTH_LONG).show();
        } else {

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

            FlagServer flagServer = new FlagServer();
            flagServer.setCreator(creator);
            flagServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            flagServer.setTitle(title);

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            flagServer.setDateStartEmpty(dateStartEmpty);
            flagServer.setDateEndEmpty(dateEndEmpty);
            flagServer.setTimeStartEmpty(timeStartEmpty);
            flagServer.setTimeEndEmpty(timeEndEmpty);
            flagServer.setDayStart(date.get(0));
            flagServer.setMonthStart(date.get(1) + 1);
            flagServer.setYearStart(date.get(2));
            flagServer.setDayEnd(date.get(3));
            flagServer.setMonthEnd(date.get(4) + 1);
            flagServer.setYearEnd(date.get(5));
            flagServer.setMinuteStart(date.get(6));
            flagServer.setHourStart(date.get(7));
            flagServer.setMinuteEnd(date.get(8));
            flagServer.setHourEnd(date.get(9));

            flagServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart(), flagServer.getHourStart(), flagServer.getMinuteStart());
            flagServer.setDateTimeStart(calendar.getTimeInMillis());

            calendar.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd(), flagServer.getHourEnd(), flagServer.getMinuteEnd());
            flagServer.setDateTimeEnd(calendar.getTimeInMillis());

            flagServer.setRepeatType(repeat.get(0));
            flagServer.setRepeatQty(repeat.get(1));

            flagServer.setType(free);
            if (free) {
                flagServer.setToAll(sendAll);
                flagServer.setText(getResources().getString(R.string.flag_available));
            } else {
                flagServer.setToAll(true);
                flagServer.setText(getResources().getString(R.string.flag_unavailable));
            }

            for (int i = 1; i < list_guest.size(); i++) {
                flagServer.addGuest(list_guest.get(i).getEmail());
            }

            registerProcess(flagServer);
            setProgress(true);

        } else {
            error = true;
        }
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

    private void registerProcess(FlagServer flagServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerFlag(flagServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void editFlag(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editFlag(getFlag().getId(), activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void getActivityStartToday() {
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
        if (mJobManager.getAllJobRequestsForTag(NotificationSyncJob.TAG).size() > 0)
            mJobManager.cancelAllForTag(NotificationSyncJob.TAG);

        ArrayList<Object> list = new ArrayList<>();
        ArrayList<ActivityOfDay> list_notify = new ArrayList<>();

        if (response.getMyCommitAct() != null) {
            ArrayList<ActivityServer> flagServers = response.getMyCommitAct();
            for (int i = 0; i < flagServers.size(); i++) {
                list.add(flagServers.get(i));
            }
        }
        if (response.getMyCommitFlag() != null) {
            ArrayList<FlagServer> flagServers = response.getMyCommitFlag();
            for (int i = 0; i < flagServers.size(); i++) {
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
                        activityServer.getDayStart(), activityServer.getMonthStart(), activityServer.getYearStart()));
            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);
                list_notify.add(new ActivityOfDay(flagServer.getTitle(), flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
                        flagServer.getDayStart(), flagServer.getMonthStart(), flagServer.getYearStart()));
            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);
                list_notify.add(new ActivityOfDay(reminderServer.getText(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
                        reminderServer.getDayStart(), reminderServer.getMonthStart(), reminderServer.getYearStart()));
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
            c1.set(Calendar.MONTH, activityOfDay.getMonth() - 1);
            c1.set(Calendar.YEAR, activityOfDay.getYear());
            c1.set(Calendar.HOUR_OF_DAY, activityOfDay.getHourStart());
            c1.set(Calendar.MINUTE, activityOfDay.getMinuteStart());
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
            c2.set(Calendar.MONTH, activityOfDayNext.getMonth() - 1);
            c2.set(Calendar.YEAR, activityOfDayNext.getYear());
            c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
            c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);

            while (activityOfDayNext != null && c1.getTimeInMillis() == c2.getTimeInMillis()) {
                j++;
                count_same++;
                if (j < list_notify.size()) {
                    activityOfDayNext = list_notify.get(j);
                    c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
                    c2.set(Calendar.MONTH, activityOfDayNext.getMonth() - 1);
                    c2.set(Calendar.YEAR, activityOfDayNext.getYear());
                    c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
                    c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
                    c2.set(Calendar.SECOND, 0);
                    c2.set(Calendar.MILLISECOND, 0);
                } else
                    activityOfDayNext = null;
            }
            activityOfDay.setCommitmentSameHour(count_same);

            time_exact = (int) (c1.getTimeInMillis() - c3.getTimeInMillis()) / (1000 * 60);
            if (time_exact >= Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT) {
                c1.add(Calendar.MINUTE, -Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT);
                time_to_happen = c1.getTimeInMillis() - c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            if (time_exact >= 1440) {
                c1.add(Calendar.MINUTE, -1380);
                time_to_happen = c1.getTimeInMillis() - c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras2)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            i = j - 1;
        }

        if (list_notify.size() > 0) {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
            Gson gson = new Gson();
            String json = gson.toJson(list_notify);
            editor.putString("ListActDay", json);
            editor.apply();
        }
    }

    private int getFutureActivities(List<Integer> date) {
        Collections.sort(flagServers, new Comparator<FlagServer>() {
            @Override
            public int compare(FlagServer c1, FlagServer c2) {
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

        ArrayList<FlagServer> list = new ArrayList<>();
        for (int i = 0; i < flagServers.size(); i++) {
            FlagServer act = flagServers.get(i);
            if (!isDatePrior(act.getDayStart(), act.getMonthStart(), act.getYearStart(), getFlag().getDayStart(), getFlag().getMonthStart(), getFlag().getYearStart()))
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

    private void edit_flag(boolean repeat) {
        boolean dateStartEmpty = false, dateEndEmpty = false, timeStartEmpty = false, timeEndEmpty = false;
        String title = ((FlagEditFragment) mNavigator.getFragment(0)).getTitleFromView();
        List<Integer> repeat_single = ((FlagEditFragment) mNavigator.getFragment(0)).getRepeatFromView();
        List<Integer> date = ((FlagEditFragment) mNavigator.getFragment(0)).getDateFromView();
        int repeat_left = -1;

        int err = 0;
        int repeat_type = getFlag().getRepeatType();
        boolean repeat_single_changed = false;

        if (date.get(0) == -1) {
            dateStartEmpty = true;
        } else {
            dateStartEmpty = false;
        }

        if (date.get(3) == -1) {
            dateEndEmpty = true;
            date.set(3, date.get(0));
            date.set(4, date.get(1));
            date.set(5, date.get(2));
        } else {
            dateEndEmpty = false;
        }

        if (date.get(6) == -1) {
            timeStartEmpty = true;
            date.set(6, 0);
            date.set(7, 0);
        } else {
            timeStartEmpty = false;
        }

        if (date.get(8) == -1) {
            timeEndEmpty = true;
            date.set(8, 59);
            date.set(9, 23);
        } else {
            timeEndEmpty = false;
        }

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();

        calendarStart.set(date.get(2), date.get(1), date.get(0));
        calendarEnd.set(date.get(5), date.get(4), date.get(3));
        boolean validateDate = validateDateTime(calendarStart, calendarEnd);

        calendarStart.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
        calendarEnd.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));
        boolean validateTime = validateDateTime(calendarStart, calendarEnd);

        if (dateStartEmpty) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_start_required, Toast.LENGTH_LONG).show();
        } else if (!validateDate) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_end_before_start, Toast.LENGTH_LONG).show();
        } else if (!validateTime) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_time_end_before_start, Toast.LENGTH_LONG).show();
        } else if (repeat_type == 0 && repeat_type != repeat_single.get(0)) {
            repeat_type = repeat_single.get(0);
            repeat_single_changed = true;
            if ((repeat_single.get(0) != 0 && repeat_single.get(1) < 0)) {
                err++;
                Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
            } else if (repeat_single.get(1) == 0 || repeat_single.get(1) > 30) {
                err++;
                Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_min_max, Toast.LENGTH_LONG).show();
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
            activityServer.setDateStartEmpty(dateStartEmpty);
            activityServer.setDateEndEmpty(dateEndEmpty);
            activityServer.setTimeStartEmpty(timeStartEmpty);
            activityServer.setTimeEndEmpty(timeEndEmpty);
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

            if (repeat) {
                activityServer.setRepeatType(repeat_type);
                activityServer.setRepeatQty(repeat_left);
            } else {
                activityServer.setRepeatType(0);
                activityServer.setRepeatQty(-1);
            }

            setProgress(true);

            if (!repeat_single_changed)
                editFlag(activityServer);

        } else
            error = true;
        //requiredText.setVisibility(View.VISIBLE);
    }

    private void handleResponse(Response response) {
        setProgress(false);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int year2 = c2.get(Calendar.YEAR);

        if (getFlag() != null) {
            if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
                getActivityStartToday();
        }


        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("d", d);
        intent.putExtra("m", m);
        intent.putExtra("y", y);
        setResult(RESULT_OK, intent);
        if (user_friend == null || listUserCompare.size() == 0) {
            finish();
        } else {
            startActivity(intent);
        }

    }

    private void handleError(Throwable error) {
        setProgress(false);
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    private void showSnackBarMessage(String message) {

        if (findViewById(android.R.id.content) != null) {

            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateInviteRequest(InviteRequest inviteRequest) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFlag, this::handleError));
    }

    public void addGuestToFlag(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().addNewGuest(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFlag, this::handleError));
    }

    private void handleFlag(Response response) {
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //INVITED_SUCCESSFULLY
        FlagServer flagServer = new FlagServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        flagServer.setId(0);
        flagServer.setCreator(email);
        flagServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
        setFlagInformation(getFlag().getId(), flagServer);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int year2 = c2.get(Calendar.YEAR);

        FlagServer flag = getFlag();
        int d = flag.getDayStart();
        int m = flag.getMonthStart();
        int y = flag.getYearStart();

        if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();
    }

    private boolean sameDay(int y1, int m1, int d1, int y2, int m2, int d2) {
        return d1 == d2 && m1 == m2 && y1 == y2;
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

        text1.setText(getResources().getString(R.string.popup_message_edit_commitments_with_repetitions));
        text2.setVisibility(View.GONE);
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.confirm));

        radioGroup.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
        text2.setText(getResources().getString(R.string.popup_message_edit_select_which_one_to_modify));

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
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
                    buttonText2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

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

                edit_flag(idx == 1);

                dg.dismiss();
            }
        });

        dg.show();
    }

    @Override
    public void onClick(View v) {
        if (type == CREATE_EDIT_FLAG && !edit) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "type" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            LinearLayout linearLayout = (LinearLayout) mNavigator.getCurrentFragment().getView();
            EditText textInputLayout = (EditText) linearLayout.getChildAt(0);

            FlagEditFragment flagEditFragment = (FlagEditFragment) mNavigator.getFragment(0);


            if (v == availableText || v == availableButton) {
                controller.updateAll(0, R.color.flag_available, R.color.flag_available, avaliableBg);
                textInputLayout.setHint(R.string.hint_title_opcional);
                free = true;
                flagEditFragment.setSelectionSendBox(free);
                privacyIcon.setImageResource(R.drawable.ic_lock);
                privacyText.setText(getResources().getString(R.string.flag_privacy));
                availableButton.setImageResource(avaliableIcon);
                unavailableButton.setImageResource(unavaliableIcon);
                availableButton.clearColorFilter();
            } else if (v == unavailableText || v == unavailableButton) {
                controller.updateAll(1, R.color.flag_unavailable, R.color.flag_unavailable, unavaliableBg);
                textInputLayout.setHint(R.string.hint_title_opcional);
                free = false;
                flagEditFragment.setSelectionSendBox(free);
                privacyIcon.setImageResource(R.drawable.ic_public);
                privacyText.setText(getResources().getString(R.string.flag_privacy_public));
                availableButton.setImageResource(avaliableIcon);
                unavailableButton.setImageResource(unavaliableIcon);
                unavailableButton.clearColorFilter();
            }
        }

        if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (edit) {
                Intent intent = new Intent(this, FlagActivity.class);
                intent.putExtra("type_flag", 1);
                intent.putExtra("flag_show", flagWrapper);
                startActivity(intent);
                finish();
            } else {
                onBackPressed();
            }
        } else if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (!edit) {
                register();
            } else {
                if (getFlag().getRepeatType() > 0) {
                    List<Integer> date;

                    date = ((FlagEditFragment) mNavigator.getFragment(0)).getDateFromView();

                    int d = date.get(0);
                    int m = date.get(1) + 1;
                    int y = date.get(2);

                    if (sameDay(y, m, d, getFlag().getYearStart(), getFlag().getMonthStart(), getFlag().getDayStart())
                            && sameDay(date.get(5), date.get(4) + 1, date.get(3), getFlag().getYearEnd(), getFlag().getMonthEnd(), getFlag().getDayEnd()))
                        edit_flag(false);
                    else
                        createDialogEditWithRepeat();
                } else
                    edit_flag(false);
            }
        } else if (v == editButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "editButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(FlagActivity.this, FlagActivity.class);
            myIntent.putExtra("flag_edit", flagWrapper);
            startActivity(myIntent);
            finish();
        } else if (v == confirmationButtonFit) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "checkButtonBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            InviteRequest inviteRequest = new InviteRequest();

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            inviteRequest.setEmail(email);
            inviteRequest.setStatus(Constants.YES);
            inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

            inviteRequest.setType(Constants.FLAG);
            inviteRequest.setIdAct(getFlag().getId());

            updateInviteRequest(inviteRequest);
        } else if (v == confirmationButtonRemove) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteButtonBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (checkIfCreator(creator_flag.getEmail())) {
                createDialogRemove(getFlag().getRepeatType() > 0);
            } else {
                InviteRequest inviteRequest = new InviteRequest();

                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                inviteRequest.setEmail(email);
                inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                inviteRequest.setStatus(Constants.NO);

                inviteRequest.setType(Constants.FLAG);
                inviteRequest.setIdAct(getFlag().getId());

                updateInviteRequest(inviteRequest);
            }
        }

    }

    private void deleteFlagActReminder(long id, ActivityServer activity) {
        setProgress(true);
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

        FlagServer flag = getFlag();
        int d = flag.getDayStart();
        int m = flag.getMonthStart();
        int y = flag.getYearStart();

        if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();

        setProgress(false);

        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //ACTIVITY_DELETED_SUCCESSFULLY e WITHOUT_NOTIFICATION
        finish();
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

        button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    button1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    button1.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    button2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    button2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

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
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actRemove" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                activity.setVisibility(Constants.FLAG);
                deleteFlagActReminder(getFlag().getId(), activity);

                dg.dismiss();
            }
        });

        dg.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    private void handleErrorToday(Throwable error) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(actionColor);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(actionColorPressed);
            }
        } else if (view == editButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                editButton.setTextColor(actionColor);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editButton.setTextColor(actionColorPressed);
            }
        }

        return false;
    }

}
