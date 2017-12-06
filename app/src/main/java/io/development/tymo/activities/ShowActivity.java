package io.development.tymo.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Space;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import io.development.tymo.R;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.NotificationSyncJob;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.SecureStringPropertyConverter;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ShowActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton;
    private boolean isInPast = false;

    private TextView privacyText, editButton;
    private ImageView privacyIcon, privacyArrowIcon;

    private Context context;
    private TagView tagGroup;
    private Tag tag;
    private TextView tittleText, whatsAppGroupLink, descriptionShort;
    private ReadMoreTextView descriptionReadMore;
    private LinearLayout whatsAppGroupLinkBox;

    private TextView dateHourText, guestText, buttonTextPast;
    private TextView locationText, repeatText;
    private LinearLayout locationBox;
    private LinearLayout repeatBox, guestBox, confirmationButtonFit, confirmationButtonRemove, confirmationButtonPast;

    private DateFormat dateFormat;

    private Rect rect;

    private RelativeLayout pieceBox;
    private TextView whoCanInvite, guestsNumber, addGuestText;
    private View addGuestButtonDivider;
    private TextView feedVisibility;
    private RelativeLayout addGuestButton;
    private ImageView addGuestIcon;
    private View progressLoadingBox;
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;

    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private ArrayList<User> listPerson = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private ArrayList<User> listToInvite = new ArrayList<>();

    private ImageView pieceIcon, locationIcon;
    private ImageView cubeLowerBoxIcon;
    private ImageView cubeUpperBoxIcon;

    private LinearLayout privacyBox;
    private TextView invitationText;

    private ActivityWrapper activityWrapper;
    private int selected = 0;
    private long act_id;

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
    private SecureStringPropertyConverter converter = new SecureStringPropertyConverter();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act);

        mSubscriptions = new CompositeDisposable();

        privacyBox = (LinearLayout) findViewById(R.id.textsBox);
        privacyText = (TextView) findViewById(R.id.text);
        privacyIcon = (ImageView) findViewById(R.id.privacyIcon);
        privacyArrowIcon = (ImageView) findViewById(R.id.arrowIcon);
        editButton = (TextView) findViewById(R.id.editButton);
        cubeLowerBoxIcon = (ImageView) findViewById(R.id.cubeLowerBoxIcon);
        cubeUpperBoxIcon = (ImageView) findViewById(R.id.cubeUpperBoxIcon);
        pieceIcon = (ImageView) findViewById(R.id.pieceIcon);
        pieceBox = (RelativeLayout) findViewById(R.id.pieceBox);

        tagGroup = (TagView) findViewById(R.id.tagGroup);
        tittleText = (TextView) findViewById(R.id.title);
        descriptionReadMore = (ReadMoreTextView) findViewById(R.id.descriptionReadMore);
        descriptionShort = (TextView) findViewById(R.id.descriptionShort);
        whatsAppGroupLinkBox = (LinearLayout) findViewById(R.id.whatsAppGroupLinkBox);
        whatsAppGroupLink = (TextView) findViewById(R.id.whatsAppGroupLink);
        buttonTextPast = (TextView) findViewById(R.id.buttonTextPast);

        dateFormat = new DateFormat(this);

        dateHourText = (TextView) findViewById(R.id.dateHourText);
        repeatBox = (LinearLayout) findViewById(R.id.repeatBox);
        repeatText = (TextView) findViewById(R.id.repeatText);
        locationText = (TextView) findViewById(R.id.locationText);
        locationBox = (LinearLayout) findViewById(R.id.locationBox);
        locationIcon = (ImageView) findViewById(R.id.locationIcon);

        whoCanInvite = (TextView) findViewById(R.id.whoCanInvite);

        feedVisibility = (TextView) findViewById(R.id.feedVisibility);
        recyclerView = (RecyclerView) findViewById(R.id.guestRow);
        addGuestButton = (RelativeLayout) findViewById(R.id.addGuestButton);
        guestsNumber = (TextView) findViewById(R.id.guestsNumber);
        progressLoadingBox = findViewById(R.id.progressLoadingBox);
        addGuestIcon = (ImageView) findViewById(R.id.addGuestIcon);
        addGuestText = (TextView) findViewById(R.id.addGuestText);
        addGuestButtonDivider = (View) findViewById(R.id.addGuestButtonDivider);
        guestBox = (LinearLayout) findViewById(R.id.guestBox);
        guestText = (TextView) findViewById(R.id.guestText);
        invitationText = (TextView) findViewById(R.id.invitationText);
        confirmationButtonFit = (LinearLayout) findViewById(R.id.confirmationButtonFit);
        confirmationButtonRemove = (LinearLayout) findViewById(R.id.confirmationButtonRemove);
        confirmationButtonPast = (LinearLayout) findViewById(R.id.confirmationButtonPast);

        editButton.setVisibility(View.GONE);

        addGuestText.setText(getString(R.string.invite_guest_btn));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setDistanceToTriggerSync(700);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.deep_purple_400));

        confirmationButtonFit.setOnClickListener(this);
        confirmationButtonRemove.setOnClickListener(this);
        privacyBox.setOnClickListener(this);
        addGuestButton.setOnClickListener(this);
        guestBox.setOnClickListener(this);
        locationBox.setOnClickListener(this);

        privacyBox.setOnTouchListener(this);
        addGuestButton.setOnTouchListener(this);
        guestBox.setOnTouchListener(this);
        locationBox.setOnTouchListener(this);

        privacyBox.setVisibility(View.GONE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        mBackButton.setOnTouchListener(this);
        editButton.setOnTouchListener(this);
        editButton.setOnClickListener(this);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_show");

        if(activityWrapper != null) {
            ActivityServer activityServer = new ActivityServer();
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            activityServer.setId(0);
            activityServer.setCreator(email);
            activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            setActivityInformation(activityWrapper.getActivityServer().getId(), activityServer);
        }else {
            act_id = getIntent().getLongExtra("act_id", -1);
            getActivityInformation(act_id);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private void getActivityInformation(long id) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getActivityInformation(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseActivity, this::handleError));
    }

    private void handleResponseActivity(Response response) {
        ActivityServer activityServer = new ActivityServer();
        ActivityServer activityServer2 = response.getActivityServer();

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        activityServer.setId(0);
        activityServer.setCreator(email);
        activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

        activityServer2.setId(act_id);
        activityWrapper = new ActivityWrapper(activityServer2);

        setActivityInformation(activityWrapper.getActivityServer().getId(), activityServer);
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

                updateLayout();
            }
        }, 200);

        // Load complete
    }

    private void loadTags(ArrayList<TagServer> tags) {
        tagGroup.removeAll();

        Collections.sort(tags, new Comparator<TagServer>() {
            @Override
            public int compare(TagServer c1, TagServer c2) {
                String name1 = c1.getTitle();
                String name2 = c2.getTitle();

                if (name1.compareTo(name2) > 0)
                    return 1;
                else if (name1.compareTo(name2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        for (int i = 0; i < tags.size(); i++) {
            String text = tags.get(i).getTitle();
            tag = new Tag(text);
            tag.radius = Utilities.convertDpToPixel(10.0f, this);
            tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
            tag.isDeletable = false;
            tagGroup.addTag(tag);
        }
    }

    public void updateLayout() {
        ActivityServer activityServer = new ActivityServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        activityServer.setId(0);
        activityServer.setCreator(email);
        activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

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

        ActivityServer activity = getActivity();
        int d = activity.getDayStart();
        int m = activity.getMonthStart();
        int y = activity.getYearStart();

        if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();

        setProgress(false);
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

            ActivityServer activityServer = activityWrapper.getActivityServer();

            User user = checkIfInActivity(userList);

            if (user != null) {
                privacyBox.setVisibility(View.VISIBLE);
                privacyBox.setOnClickListener(this);
                privacyBox.setOnTouchListener(this);
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
                    editButton.setVisibility(View.VISIBLE);
                } else {
                    editButton.setVisibility(View.GONE);
                }

                privacyBox.setVisibility(View.VISIBLE);
                privacyBox.setOnClickListener(this);
                privacyBox.setOnTouchListener(this);

                if (checkIfCreator(response.getUser().getEmail())) {
                    editButton.setVisibility(View.VISIBLE);
                    confirmationButtonRemove.setVisibility(View.VISIBLE);
                    confirmationButtonFit.setVisibility(View.GONE);
                    invitationText.setVisibility(View.GONE);
                } else if (user.getInvited() > 0) {
                    if (user.getInvitation() == 1) {
                        confirmationButtonRemove.setVisibility(View.VISIBLE);
                        confirmationButtonFit.setVisibility(View.GONE);
                        invitationText.setVisibility(View.GONE);
                    } else {
                        privacyBox.setVisibility(View.GONE);
                        confirmationButtonRemove.setVisibility(View.GONE);
                        confirmationButtonFit.setVisibility(View.VISIBLE);
                        invitationText.setVisibility(View.VISIBLE);
                        invitationText.setText(getString(R.string.act_invited_by, activityServer.getNameInviter()));
                    }
                } else if (activityServer.getInvitationType() == 2) {
                    privacyBox.setVisibility(View.GONE);
                    confirmationButtonRemove.setVisibility(View.GONE);
                    confirmationButtonFit.setVisibility(View.VISIBLE);
                    invitationText.setVisibility(View.VISIBLE);
                    invitationText.setText(getString(R.string.agenda_status_anyone_can_participate));
                } else {
                    privacyBox.setVisibility(View.GONE);
                    confirmationButtonPast.setVisibility(View.VISIBLE);
                    confirmationButtonRemove.setVisibility(View.GONE);
                    confirmationButtonFit.setVisibility(View.GONE);
                    invitationText.setVisibility(View.GONE);
                    buttonTextPast.setText(getString(R.string.agenda_status_need_invitation));
                }

            } else {
                confirmationButtonPast.setVisibility(View.VISIBLE);
                confirmationButtonRemove.setVisibility(View.GONE);
                confirmationButtonFit.setVisibility(View.GONE);
                invitationText.setVisibility(View.GONE);
                buttonTextPast.setText(getString(R.string.agenda_status_need_invitation));
                if (activityServer.getInvitationType() == 2) {
                    confirmationButtonRemove.setVisibility(View.GONE);
                    confirmationButtonFit.setVisibility(View.VISIBLE);
                    invitationText.setVisibility(View.VISIBLE);
                    invitationText.setText(getString(R.string.agenda_status_anyone_can_participate));
                }
                privacyBox.setVisibility(View.GONE);
            }

            if (activityServer.getDateTimeEnd() < Calendar.getInstance().getTimeInMillis() && confirmationButtonRemove.getVisibility() == View.GONE && confirmationButtonFit.getVisibility() == View.VISIBLE){
                confirmationButtonPast.setVisibility(View.VISIBLE);
                confirmationButtonRemove.setVisibility(View.GONE);
                confirmationButtonFit.setVisibility(View.GONE);
                invitationText.setVisibility(View.GONE);
                buttonTextPast.setText(getString(R.string.agenda_status_past));
            }
            else{
                confirmationButtonPast.setVisibility(View.GONE);
            }

            Glide.clear(pieceIcon);
            Glide.with(this)
                    .load(activityServer.getCubeIcon())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(pieceIcon);

            cubeUpperBoxIcon.setColorFilter(activityServer.getCubeColorUpper());
            cubeLowerBoxIcon.setColorFilter(activityServer.getCubeColor());

            //m_title.setText(activityServer.getTitle());

            activityServers = response.getWhatsGoingAct();

            permissionInvite = checkIfCanInvite(getActivity().getInvitationType());

            if (tittleText != null) {
                tittleText.setText(activityServer.getTitle());

                if (activityServer.getWhatsappGroupLink() == null || activityServer.getWhatsappGroupLink().matches(""))
                    whatsAppGroupLinkBox.setVisibility(View.GONE);
                else
                    whatsAppGroupLink.setText(converter.toEntityAttribute(activityServer.getWhatsappGroupLink()));

                if (activityServer.getDescription() != null && activityServer.getDescription().length() <= 240) {
                    descriptionShort.setVisibility(View.VISIBLE);
                    descriptionReadMore.setVisibility(View.GONE);

                    if (!activityServer.getDescription().matches("")) {
                        descriptionShort.setText(activityServer.getDescription());
                        descriptionShort.post(new Runnable() {
                            @Override
                            public void run() {
                                if(descriptionShort.getLineCount() >= 6) {
                                    descriptionShort.setVisibility(View.GONE);
                                    descriptionReadMore.setVisibility(View.VISIBLE);
                                    String description = activityServer.getDescription() + "  " + "\n\n";
                                    descriptionReadMore.setText(description);
                                    descriptionReadMore.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            descriptionReadMore.setText();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else
                        descriptionShort.setVisibility(View.GONE);

                } else {
                    if (activityServer.getDescription() != null && !activityServer.getDescription().matches("")) {
                        String description = activityServer.getDescription() + " " + "\n\n";
                        descriptionReadMore.setText(description);
                        descriptionReadMore.post(new Runnable() {
                            @Override
                            public void run() {
                                descriptionReadMore.setText();
                            }
                        });
                    } else
                        descriptionReadMore.setVisibility(View.GONE);
                }

                loadTags(response.getTags());
            }

            if (locationText != null) {
                Calendar calendar = Calendar.getInstance();
                Calendar calendar2 = Calendar.getInstance();
                calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                String dayStart = String.format("%02d", activityServer.getDayStart());
                String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                int yearStart = activityServer.getYearStart();
                String hourStart = String.format("%02d", activityServer.getHourStart());
                String minuteStart = String.format("%02d", activityServer.getMinuteStart());
                String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
                String dayEnd = String.format("%02d", activityServer.getDayEnd());
                String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
                int yearEnd = activityServer.getYearEnd();
                String hourEnd = String.format("%02d", activityServer.getHourEnd());
                String minuteEnd = String.format("%02d", activityServer.getMinuteEnd());

                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                        dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                    } else {
                        dateHourText.setText(this.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                    }
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_06, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                }

                if (activityServer.getLocation().matches(""))
                    Log.d("xxx", "NÃO TEM LOCAL");
                else
                    Log.d("xxx", activityServer.getLocation());

                if (activityServer.getLocation() == null || activityServer.getLocation().matches(""))
                    locationBox.setVisibility(View.GONE);
                else
                    locationText.setText(activityServer.getLocation());

                if (activityServer.getRepeatType() == 0) {
                    repeatBox.setVisibility(View.GONE);
                } else {
                    String repeatly;
                    repeatBox.setVisibility(View.VISIBLE);
                    switch (activityServer.getRepeatType()) {
                        case Constants.DAYLY:
                            repeatly = this.getString(R.string.repeat_daily);
                            break;
                        case Constants.WEEKLY:
                            repeatly = this.getString(R.string.repeat_weekly);
                            break;
                        case Constants.MONTHLY:
                            repeatly = this.getString(R.string.repeat_monthly);
                            break;
                        default:
                            repeatly = "";
                            break;
                    }

                    if (activityServer.getRepeatType() == 5) {
                        repeatText.setText(this.getString(R.string.repeat_text_imported_google_agenda));
                    } else {
                        repeatText.setText(this.getString(R.string.repeat_text, repeatly, getLastActivity(activityServers)));
                    }
                }

                locationBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent;

                        if(activityServer.getLat() != -500) {
                            if(activityServer.getLat() == -250.0 && activityServer.getLat() == -250.0) {
                                intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                                        "http://maps.google.co.in/maps?q=" + activityServer.getLocation()));
                            }
                            else {
                                intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                                        "geo:" + activityServer.getLat() +
                                                "," + activityServer.getLng() +
                                                "?q=" + activityServer.getLat() +
                                                "," + activityServer.getLng() +
                                                "(" + activityServer.getLocation() + ")"));
                            }
                        }else {
                            intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                                    "http://maps.google.co.in/maps?q=" + activityServer.getLocation()));
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "locationBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(ShowActivity.this, getResources().getString(R.string.map_unable_to_find_application), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }

            new Actor.Builder(SpringSystem.create(), addGuestButton)
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

            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setNestedScrollingEnabled(false);

            context = recyclerView.getContext();

            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, MotionEvent e) {
                    SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    String email = mSharedPreferences.getString(Constants.EMAIL, "");

                    Intent intent = new Intent(context, ShowGuestsActivity.class);
                    intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
                    intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                    intent.putExtra("is_adm", checkIfAdm(getAdmList(), email));
                    intent.putExtra("id_act", getActivity().getId());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    startActivityForResult(intent, GUEST_UPDATE);
                }

                @Override
                public void onLongItemClick(View view, int position, MotionEvent e) {
                }
            }));

            setLayout(this.getActivity(), this.getUserList(), this.getUserConfirmedList(), this.getPermissionInvite());

        setProgress(false);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                setProgress(true);
                updateLayout();
            } else if (requestCode == ADD_GUEST) {
                ActivityServer activityServer = new ActivityServer();
                SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                PersonModelWrapper wrap =
                        (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                listToInvite.clear();
                listToInvite.addAll(wrap.getItemDetails());
                if(listToInvite.size() > 0) {
                    activityServer.setId(getActivity().getId());
                    activityServer.setVisibility(Constants.ACT);
                    activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                    for (int i = 0; i < listToInvite.size(); i++)
                        activityServer.addGuest(listToInvite.get(i).getEmail());

                    addGuestToActivity(activityServer);
                }
            }
        }
    }

    public void setLayout(ActivityServer activityServer, ArrayList<User> users, ArrayList<User> confirmed, boolean permissionInvite){

        if(recyclerView!=null) {
            String[] stringArray = this.getResources().getStringArray(R.array.array_who_can_invite_show);

            whoCanInvite.setText(stringArray[activityServer.getInvitationType()]);

            if (activityServer.getInvitationType() == 2) {
                feedVisibility.setText(R.string.feed_visibility_2);
            } else {
                feedVisibility.setText(R.string.feed_visibility_1);
            }

            listPerson.clear();
            listConfirmed.clear();
            listConfirmed.addAll(confirmed);

            for (int i = 0; i < users.size(); i++) {
                User usr = users.get(i);
                usr.setDelete(false);
                listPerson.add(usr);
            }

            adapter = new PersonAdapter(listPerson, this);
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(listPerson.size()));

            isInPast = isActivityInPast(activityServer);

            if (permissionInvite) {
                addGuestIcon.setImageResource(R.drawable.btn_add_person);
                addGuestButton.setOnClickListener(this);
            } else {
                addGuestButton.setOnClickListener(null);
                addGuestButton.setVisibility(View.GONE);
                addGuestButtonDivider.setVisibility(View.GONE);
            }
        }
    }

    private boolean isActivityInPast(ActivityServer activityServer){
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        boolean isHourBefore = isTimeInBefore(hour + ":" + minute, activityServer.getHourEnd() + ":" + activityServer.getMinuteEnd());
        boolean isDateBefore = isDateInBefore(activityServer.getYearEnd(), activityServer.getMonthEnd(), activityServer.getDayEnd(), year, month, day);

        return (isHourBefore && isDateBefore) || isDateBefore;
    }

    private boolean isDateInBefore(int year, int monthOfYear, int dayOfMonth,int yearEnd, int monthOfYearEnd, int dayOfMonthEnd){
        if(yearEnd < year)
            return false;
        if(year == yearEnd){
            if(monthOfYearEnd < monthOfYear)
                return false;
            else if(monthOfYearEnd == monthOfYear){
                if(dayOfMonthEnd <= dayOfMonth)
                    return false;
            }
        }

        return true;
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

    private String getLastActivity(ArrayList<ActivityServer> activityServers) {

        Collections.sort(activityServers, new Comparator<ActivityServer>() {
            @Override
            public int compare(ActivityServer c1, ActivityServer c2) {
                ActivityServer activityServer;
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

        ActivityServer activityServer = activityServers.get(activityServers.size() - 1);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());

        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(cal.get(Calendar.DAY_OF_WEEK), cal);
        String dayEnd = String.format("%02d", activityServer.getDayEnd());
        String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(cal.getTime().getTime());
        int yearEnd = activityServer.getYearEnd();

        String date = this.getResources().getString(R.string.date_format_03, dayOfWeekEnd.toLowerCase(), dayEnd, monthEnd, yearEnd);

        return date;
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
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.editButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "editButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(ShowActivity.this, EditActivity.class);
            myIntent.putExtra("act_edit", activityWrapper);
            startActivity(myIntent);
            finish();
        } else if (id == R.id.checkButtonBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "checkButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            InviteRequest inviteRequest = new InviteRequest();

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            inviteRequest.setEmail(email);
            inviteRequest.setStatus(Constants.YES);
            inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

            inviteRequest.setType(Constants.ACT);
            inviteRequest.setIdAct(getActivity().getId());

            updateInviteRequest(inviteRequest);
        } else if (id == R.id.deleteButtonBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (checkIfCreator(creator_activity.getEmail())) {
                createDialogRemove(getActivity().getRepeatType() > 0);
            } else {
                InviteRequest inviteRequest = new InviteRequest();

                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                inviteRequest.setEmail(email);
                inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                inviteRequest.setStatus(Constants.NO);

                inviteRequest.setType(Constants.ACT);
                inviteRequest.setIdAct(getActivity().getId());

                updateInviteRequest(inviteRequest);
            }

        } else if (v == privacyBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "privacyBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogPrivacy();
        }
        else if(v == addGuestButton){
            int i;
            ArrayList<String> list = new ArrayList<>();
            for(i = 0; i < listPerson.size(); i++){
                list.add(listPerson.get(i).getEmail());
            }
            Intent intent = new Intent(this, SelectPeopleActivity.class);
            intent.putStringArrayListExtra("guest_list", list);
            intent.putExtra("erase_from_list", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addGuestButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (isInPast) {
                Calendar now = Calendar.getInstance();
                ActivityServer activityServer = this.getActivity();
                createDialogMessageAddInPast(activityServer.getYearEnd(), activityServer.getMonthEnd(), activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd(),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            } else {
                startActivityForResult(intent, ADD_GUEST);
            }
        }
        else if(v == guestBox){
            SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            Intent intent = new Intent(context, ShowGuestsActivity.class);
            intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
            intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
            intent.putExtra("is_adm", checkIfAdm(getAdmList(), email));
            intent.putExtra("id_act", getActivity().getId());

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, GUEST_UPDATE);
        }
        else if(v == locationBox){
            Intent intent = null;
            if(this.getActivity().getLat() != -500) {
                intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                        "geo:" + this.getActivity().getLat() +
                                "," + this.getActivity().getLng() +
                                "?q=" + this.getActivity().getLat() +
                                "," + this.getActivity().getLng() +
                                "(" + this.getActivity().getLocation() + ")"));
            }else {

                intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                        "http://maps.google.co.in/maps?q=" + this.getActivity().getLocation()));
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "locationBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, this.getResources().getString(R.string.map_unable_to_find_application), Toast.LENGTH_LONG).show();
            }
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

                if(getActivity().getIdFacebook() > 0 || getActivity().getIdGoogle() != null)
                    activity.setInvitationType(1);

                activity.setVisibility(Constants.ACT);
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
        ImageView optionIcon1 = (ImageView) customView.findViewById(R.id.optionIcon1);
        ImageView optionIcon2 = (ImageView) customView.findViewById(R.id.optionIcon2);
        ImageView optionIcon3 = (ImageView) customView.findViewById(R.id.optionIcon3);
        TextView optionTitle1 = (TextView) customView.findViewById(R.id.optionTitle1);
        TextView optionTitle2 = (TextView) customView.findViewById(R.id.optionTitle2);
        TextView optionTitle3 = (TextView) customView.findViewById(R.id.optionTitle3);
        TextView optionText1 = (TextView) customView.findViewById(R.id.optionText1);
        TextView optionText2 = (TextView) customView.findViewById(R.id.optionText2);
        TextView optionText3 = (TextView) customView.findViewById(R.id.optionText3);

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        ActivityServer privacyUpdate = new ActivityServer();
        privacyUpdate.setCreator(email);
        privacyUpdate.setId(getActivity().getId());

        switch (selected) {
            case 1:
                optionIcon2.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                optionTitle2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                optionText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                break;
            case 2:
                optionIcon3.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                optionTitle3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                optionText3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                break;
            default:
                optionIcon1.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                optionTitle1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                optionText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                break;
        }

        optionBox1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    optionBox1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    optionBox1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_no_radius));
                }

                switch (selected) {
                    case 1:
                        optionIcon2.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                    case 2:
                        optionIcon3.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                    default:
                        optionIcon1.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                }

                return false;
            }
        });

        optionBox2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    optionBox2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    optionBox2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_no_radius));
                }

                switch (selected) {
                    case 1:
                        optionIcon2.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                    case 2:
                        optionIcon3.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                    default:
                        optionIcon1.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                }

                return false;
            }
        });

        optionBox3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    optionBox3.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    optionBox3.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_bottom_radius));
                }

                switch (selected) {
                    case 1:
                        optionIcon2.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                    case 2:
                        optionIcon3.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText3.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                    default:
                        optionIcon1.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionTitle1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        optionText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_400));
                        break;
                }

                return false;
            }
        });

        optionBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

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

        if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
            getActivityStartToday();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
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
            ArrayList<ActivityServer> activityServers = response.getMyCommitAct();
            for (int i = 0; i < activityServers.size(); i++) {
                list.add(activityServers.get(i));
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
                list_notify.add(new ActivityOfDay(reminderServer.getTitle(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
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

    private void handleErrorToday(Throwable error) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == editButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                editButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == privacyBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                privacyText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                privacyArrowIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
                privacyIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                privacyText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
                privacyArrowIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
                privacyIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == guestBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                guestText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                guestsNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.box_qty_guests));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                guestText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                guestsNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.box_qty_guests_pressed));
            }
        } else if (view == locationBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
                locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        }

        return false;
    }

    private void createDialogMessageAddInPast(int y1, int m1, int d1, int h1, int min1, int y2, int m2, int d2, int h2, int min2) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        button1.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        String date = String.format("%02d", d1) + "/" + String.format("%02d", m1) + "/" + String.valueOf(y1);
        String dateNow = String.format("%02d", d2) + "/" + String.format("%02d", m2) + "/" + String.valueOf(y2);
        String time = String.format("%02d", h1) + ":" + String.format("%02d", min1);
        String timeNow = String.format("%02d", h2) + ":" + String.format("%02d", min2);

        text1.setText(R.string.invite_past_dialog_text_1);
        text2.setText(this.getString(R.string.invite_past_dialog_text_2, date + " - " + time, dateNow + " - " + timeNow));
        buttonText2.setText(R.string.close);

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_radius));
                }

                return false;
            }
        });

        dg.show();
    }


}
