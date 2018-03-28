package io.development.tymo.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.TagView;
import com.cunoraz.tagview.Tag;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

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
import io.development.tymo.adapters.CustomizeAddActivityAdapter;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.adapters.SectionedGridRecyclerViewAdapter;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.CategoryServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.IconServer;
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
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static io.development.tymo.utils.Validation.validateFields;

public class EditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {

    private Rect rect;
    private ActivityWrapper activityWrapper, activityWrapperEdited;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String urlIcon = "", urlIconTemp = "", email = "";
    private int d, m, y, selected = 0, invite = 0;
    private boolean first_open = true, permissionInvite = false;
    private boolean isInPast = false;
    private DateFormat dateFormat;
    private int actualColor, actualColorUpper;
    private String actualIcon;

    private ArrayList<ActivityServer> activityServers;
    private ArrayList<IconServer> iconList;
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> admList = new ArrayList<>();
    private User creator_activity, user_friend = null;
    private ArrayList<User> invitedList = new ArrayList<>();
    private ArrayList<User> confirmedList = new ArrayList<>();

    private TextView customizeApplyButton, customizeCleanButton, privacyText, confirmationButton, repeatMax, repeatAddText, repeatText, repeatLastDate;
    private TextView addImageText, loadedImageText, titleMax, addTagText, dateStart, dateEnd, timeStart, timeEnd, locationText, locationTextAdd;
    private TextView guestText, guestsNumber, addGuestText, feedVisibility;
    private EditText titleEditText, descriptionEditText, whatsAppEditText, repeatEditText;
    private ImageView cubeLowerBoxIcon, cubeUpperBoxIcon, pieceIcon, customizeCubeLowerBoxIcon, customizeCubeUpperBoxIcon, customizePieceIcon;
    private ImageView mBackButton, privacyIcon, privacyArrowIcon, repeatAddIcon;
    private ImageView clearDateStart, clearDateEnd, clearTimeStart, clearTimeEnd;
    private ImageView addImageIcon, loadedImageIcon, addTagIcon, locationIconAdd, locationIcon, locationIcon2;
    private RelativeLayout pieceBox, addPersonButton, addTagBox, locationBox, repeatAdd;
    private LinearLayout privacyBox, addImage, loadedImage, repeatBox, repeatNumberBox, locationBoxAdd, guestBox;
    private View addGuestButtonDivider, profilesPhotos, progressLoadingBox, viewClicked;
    private ColorPickerView customizeColorPicker;
    private TagView tagGroup;
    private Tag tag;
    private MaterialSpinner spinner, spinnerRepeatPicker;
    private RecyclerView recyclerViewGuestRow;

    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;
    private int repeat_type = 0;
    private int repeat_qty = -1;
    private double lat = -500;
    private double lng = -500;
    private static final int PLACE_PICKER_REQUEST = 1020;
    private boolean locationNotWorking = false, repeat_blocked = false;
    private Calendar calendarStart;
    private PlacePicker.IntentBuilder builder = null;
    private Intent placePicker = null;
    private String location = "";
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;

    private ArrayList<User> data = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private ArrayList<User> listToInvite = new ArrayList<>();
    private PersonAdapter adapter;

    private SecureStringPropertyConverter converter = new SecureStringPropertyConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_edit);

        mSubscriptions = new CompositeDisposable();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        privacyBox = (LinearLayout) findViewById(R.id.textsBox);
        privacyIcon = (ImageView) findViewById(R.id.privacyIcon);
        privacyText = (TextView) findViewById(R.id.text);
        privacyArrowIcon = (ImageView) findViewById(R.id.arrowIcon);
        cubeLowerBoxIcon = (ImageView) findViewById(R.id.cubeLowerBoxIcon);
        cubeUpperBoxIcon = (ImageView) findViewById(R.id.cubeUpperBoxIcon);
        pieceIcon = (ImageView) findViewById(R.id.pieceIcon);
        pieceBox = (RelativeLayout) findViewById(R.id.pieceBox);
        addImage = (LinearLayout) findViewById(R.id.addImage);
        addImageIcon = (ImageView) findViewById(R.id.addImageIcon);
        addImageText = (TextView) findViewById(R.id.addImageText);
        loadedImage = (LinearLayout) findViewById(R.id.loadedImage);
        loadedImageIcon = (ImageView) findViewById(R.id.loadedImageIcon);
        loadedImageText = (TextView) findViewById(R.id.loadedImageText);
        titleEditText = (EditText) findViewById(R.id.title);
        titleMax = (TextView) findViewById(R.id.titleMax);
        tagGroup = (TagView) findViewById(R.id.tagGroup);
        addTagBox = (RelativeLayout) findViewById(R.id.addTagBox);
        addTagText = (TextView) findViewById(R.id.addTagText);
        addTagIcon = (ImageView) findViewById(R.id.addTagIcon);
        descriptionEditText = (EditText) findViewById(R.id.description);
        dateStart = (TextView) findViewById(R.id.dateStart);
        dateEnd = (TextView) findViewById(R.id.dateEnd);
        timeStart = (TextView) findViewById(R.id.timeStart);
        timeEnd = (TextView) findViewById(R.id.timeEnd);
        clearDateStart = (ImageView) findViewById(R.id.clearDateStart);
        clearDateEnd = (ImageView) findViewById(R.id.clearDateEnd);
        clearTimeStart = (ImageView) findViewById(R.id.clearTimeStart);
        clearTimeEnd = (ImageView) findViewById(R.id.clearTimeEnd);
        repeatAdd = (RelativeLayout) findViewById(R.id.repeatAdd);
        repeatAddIcon = (ImageView) findViewById(R.id.repeatAddIcon);
        repeatAddText = (TextView) findViewById(R.id.repeatAddText);
        repeatBox = (LinearLayout) findViewById(R.id.repeatBox);
        repeatNumberBox = (LinearLayout) findViewById(R.id.repeatNumberBox);
        repeatEditText = (EditText) findViewById(R.id.repeatEditText);
        repeatMax = (TextView) findViewById(R.id.repeatMax);
        locationBoxAdd = (LinearLayout) findViewById(R.id.locationBoxAdd);
        locationIconAdd = (ImageView) findViewById(R.id.locationIconAdd);
        locationTextAdd = (TextView) findViewById(R.id.locationTextAdd);
        locationBox = (RelativeLayout) findViewById(R.id.locationBox);
        locationIcon = (ImageView) findViewById(R.id.locationIcon);
        locationIcon2 = (ImageView) findViewById(R.id.locationIcon2);
        locationText = (TextView) findViewById(R.id.locationText);
        whatsAppEditText = (EditText) findViewById(R.id.whatsAppGroupLink);
        guestBox = (LinearLayout) findViewById(R.id.guestBox);
        guestText = (TextView) findViewById(R.id.guestText);
        guestsNumber = (TextView) findViewById(R.id.guestsNumber);
        profilesPhotos = findViewById(R.id.profilesPhotos);
        recyclerViewGuestRow = (RecyclerView) findViewById(R.id.guestRow);
        progressLoadingBox = findViewById(R.id.progressLoadingBox);
        addPersonButton = (RelativeLayout) findViewById(R.id.addGuestButton);
        addGuestText = (TextView) findViewById(R.id.addGuestText);
        addGuestButtonDivider = (View) findViewById(R.id.addGuestButtonDivider);
        spinner = (MaterialSpinner) findViewById(R.id.visibilityCalendarPicker);
        spinnerRepeatPicker = (MaterialSpinner) findViewById(R.id.repeatPicker);
        feedVisibility = (TextView) findViewById(R.id.feedVisibility);
        repeatText = (TextView) findViewById(R.id.repeatText);
        repeatLastDate = (TextView) findViewById(R.id.repeatLastDate);

        findViewById(R.id.addGuestBox).setVisibility(View.GONE);

        mBackButton.setOnClickListener(this);
        privacyBox.setOnClickListener(this);
        pieceBox.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        addImage.setOnClickListener(this);
        loadedImage.setOnClickListener(this);
        addTagBox.setOnClickListener(this);
        dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        timeEnd.setOnClickListener(this);
        clearDateStart.setOnClickListener(this);
        clearDateEnd.setOnClickListener(this);
        clearTimeStart.setOnClickListener(this);
        clearTimeEnd.setOnClickListener(this);
        repeatAdd.setOnClickListener(this);
        locationBoxAdd.setOnClickListener(this);
        locationBox.setOnClickListener(this);
        locationBox.setOnLongClickListener(this);
        locationIcon2.setOnClickListener(this);
        guestBox.setOnClickListener(this);
        addPersonButton.setOnClickListener(this);

        mBackButton.setOnTouchListener(this);
        privacyBox.setOnTouchListener(this);
        addImage.setOnTouchListener(this);
        loadedImage.setOnTouchListener(this);
        addTagBox.setOnTouchListener(this);
        repeatAdd.setOnTouchListener(this);
        locationBoxAdd.setOnTouchListener(this);
        locationBox.setOnTouchListener(this);
        locationIcon2.setOnTouchListener(this);
        guestBox.setOnTouchListener(this);

        mSwipeRefreshLayout.setEnabled(false);
        mBackButton.setImageResource(R.drawable.ic_add);
        mBackButton.setRotation(45);
        confirmationButton.setText(R.string.save_updates);
        titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
        tagGroup.setOnTagDeleteListener(mOnTagDeleteListener);

        clearDateStart.setVisibility(View.GONE);
        clearDateEnd.setVisibility(View.GONE);
        clearTimeStart.setVisibility(View.GONE);
        clearTimeEnd.setVisibility(View.GONE);
        repeatBox.setVisibility(View.GONE);
        repeatText.setVisibility(View.GONE);

        repeatAdd.setVisibility(View.GONE);

        dateFormat = new DateFormat(this);

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }
        });

        repeatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }
        });

        new Actor.Builder(SpringSystem.create(), addPersonButton)
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

        new Actor.Builder(SpringSystem.create(), pieceBox)
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

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_edit");

        actualIcon = activityWrapper.getActivityServer().getCubeIcon();
        actualColor = activityWrapper.getActivityServer().getCubeColor();
        actualColorUpper = activityWrapper.getActivityServer().getCubeColorUpper();

        ActivityServer activityServer = new ActivityServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        this.email = mSharedPreferences.getString(Constants.EMAIL, "");
        activityServer.setId(0);
        activityServer.setCreator(email);
        activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
        setActivityInformation(activityWrapper.getActivityServer().getId(), activityServer);

        getIcons();

        calendarStart = Calendar.getInstance();

        day_start = -1;
        month_start = -1;
        year_start = -1;
        day_end = -1;
        month_end = -1;
        year_end = -1;
        minutes_start = -1;
        minutes_end = -1;
        hour_start = -1;
        hour_end = -1;

        spinnerRepeatPicker.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinnerRepeatPicker.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                repeat_type = position;

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (position == 0) {
                    repeatNumberBox.setVisibility(View.GONE);
                } else {
                    repeatNumberBox.setVisibility(View.VISIBLE);
                }

                setRepeatLastDate();
            }
        });

        if (this.getActivity() != null && this.getActivity().getLat() != -500) {
            LatLng latLng = new LatLng(this.getActivity().getLat(), this.getActivity().getLng());
            LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
            builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);
        } else
            builder = new PlacePicker.IntentBuilder();


        try {
            placePicker = builder.build(this);
        } catch (Exception e) {
            locationNotWorking = true;
        }

        setLayoutWhenWhere(this.getActivity());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

    }

    private void getUser(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfile(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseGetUser, this::handleError));
    }

    private void handleResponseGetUser(User user) {
        setProgress(false);
    }

    private OnTagDeleteListener mOnTagDeleteListener = new OnTagDeleteListener() {

        @Override
        public void onTagDeleted(final TagView view, final Tag tag, final int position) {
            view.remove(position);
        }
    };

    private void loadTags(ArrayList<TagServer> tags) {

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
            if (text.matches(getResources().getString(R.string.settings_import_from_facebook_tag)) ||
                    text.matches(getResources().getString(R.string.settings_import_from_google_agenda_tag)))
                tag.isDeletable = false;
            else
                tag.isDeletable = true;
            tagGroup.addTag(tag);
        }
    }

    public boolean isTagPresent(String tag) {
        List<Tag> tags = tagGroup.getTags();
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t.text.equals(tag))
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        setProgress(false);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(this, intent);
                String name = selectedPlace.getAddress().toString();
                lat = selectedPlace.getLatLng().latitude;
                lng = selectedPlace.getLatLng().longitude;
                locationText.setText(name);
            }
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                List<String> list = intent.getStringArrayListExtra("tags_objs");

                boolean tag_face = isTagPresent(getResources().getString(R.string.settings_import_from_facebook_tag));
                boolean tag_google = isTagPresent(getResources().getString(R.string.settings_import_from_google_agenda_tag));
                tagGroup.removeAll();

                if (tag_face) {
                    Tag tag;
                    tag = new Tag(getResources().getString(R.string.settings_import_from_facebook_tag));
                    tag.radius = Utilities.convertDpToPixel(10.0f, this);
                    tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
                    tag.isDeletable = false;
                    tagGroup.addTag(tag);
                }
                if (tag_google) {
                    Tag tag;
                    tag = new Tag(getResources().getString(R.string.settings_import_from_google_agenda_tag));
                    tag.radius = Utilities.convertDpToPixel(10.0f, this);
                    tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
                    tag.isDeletable = false;
                    tagGroup.addTag(tag);
                }

                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String c1, String c2) {
                        if (c1.compareTo(c2) > 0)
                            return 1;
                        else if (c1.compareTo(c2) < 0)
                            return -1;
                        else
                            return 0;
                    }
                });

                for (int i = 0; i < list.size(); i++) {
                    Tag tag;
                    tag = new Tag(list.get(i));
                    tag.radius = Utilities.convertDpToPixel(10.0f, this);
                    tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
                    tag.isDeletable = true;
                    tagGroup.addTag(tag);
                }


            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                ActivityServer activityServer = new ActivityServer();

                SharedPreferences mSharedPreferences = this.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                activityServer.setId(0);
                activityServer.setCreator(email);

                this.setActivityGuestInformation(this.getActivity().getId(), activityServer);
            } else if (requestCode == ADD_GUEST) {
                ActivityServer activityServer = new ActivityServer();
                SharedPreferences mSharedPreferences = this.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                PersonModelWrapper wrap =
                        (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                listToInvite.clear();
                listToInvite.addAll(wrap.getItemDetails());
                if (listToInvite.size() > 0) {
                    activityServer.setId(this.getActivity().getId());
                    activityServer.setVisibility(Constants.ACT);
                    activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                    for (int i = 0; i < listToInvite.size(); i++)
                        activityServer.addGuest(listToInvite.get(i).getEmail());

                    this.addGuestToActivity(activityServer);
                }
            }
        }
    }

    public List<Tag> getTags() {
        return tagGroup.getTags();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
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
        } else if (view == addImage) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                addImageText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                addImageIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                addImageText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                addImageIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == loadedImage) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                loadedImageText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                loadedImageIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                loadedImageText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
                loadedImageIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == addTagBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                addTagText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                addTagIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                addTagText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                addTagIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == repeatAdd) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                repeatAddText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                repeatAddIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                repeatAddText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                repeatAddIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == locationBoxAdd) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationTextAdd.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                locationIconAdd.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationTextAdd.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                locationIconAdd.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == locationBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
                locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == locationIcon2) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationIcon2.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationIcon2.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == guestBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                guestText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                guestsNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.box_qty_guests));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                guestText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                guestsNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.box_qty_guests_pressed));
            }
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                .subscribe(this::handleResponseIcons, this::handleError));
    }

    private void handleResponseIcons(ArrayList<IconServer> icons) {
        setProgress(false);
        iconList = new ArrayList<>();
        iconList.addAll(icons);
        Collections.sort(iconList, new Comparator<IconServer>() {
            @Override
            public int compare(IconServer c1, IconServer c2) {
                String u1 = c1.getCategory();
                String u2 = c2.getCategory();

                if (u1.matches("Tymo") && !u2.matches("Tymo"))
                    return -1;
                else if (!u1.matches("Tymo") && u2.matches("Tymo"))
                    return 1;
                else if (u1.matches("Outros") && !u2.matches("Outros"))
                    return 1;
                else if (!u1.matches("Outros") && u2.matches("Outros"))
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

        ActivityServer activityServer = activityWrapper.getActivityServer();
        User userEdit = checkIfInActivity(userList);

        if (!activityServer.getCubeIcon().matches("")) {

            Glide.clear(pieceIcon);
            Glide.with(EditActivity.this)
                    .load(activityServer.getCubeIcon())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(pieceIcon);

            cubeUpperBoxIcon.setColorFilter(activityServer.getCubeColorUpper());
            cubeLowerBoxIcon.setColorFilter(activityServer.getCubeColor());
        }

        if (userEdit != null) {

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

        if (titleEditText != null) {
            titleEditText.setText(activityServer.getTitle());

            if (activityServer.getDescription() != null)
                descriptionEditText.setText(activityServer.getDescription());
            else
                descriptionEditText.setText("");

            if (activityServer.getDescription() != null)
                whatsAppEditText.setText(activityServer.getWhatsappGroupLink());
            else
                whatsAppEditText.setText("");

            loadTags(response.getTags());
        }

        activityServers = response.getWhatsGoingAct();

        if (activityServer.getRepeatType() == 0) {
            repeatText.setVisibility(View.GONE);
        } else {
            String repeatly;
            repeatText.setVisibility(View.VISIBLE);
            switch (activityServer.getRepeatType()) {
                case Constants.DAILY:
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
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(activityServer.getLastDateTime());
                String dayOfWeek = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
                String month = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                int year = calendar.get(Calendar.YEAR);
                String date = this.getResources().getString(R.string.date_format_03, dayOfWeek, day, month, year);
                repeatText.setText(this.getString(R.string.repeat_text, repeatly, date));
            }
        }

        findViewById(R.id.progressBox).setVisibility(View.GONE);

        feedVisibility.setText(R.string.feed_visibility_1);

        spinner.setItems(this.getResources().getStringArray(R.array.array_who_can_invite));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                invite = position;
                if (invite == 2) {
                    feedVisibility.setText(R.string.feed_visibility_2);
                } else {
                    feedVisibility.setText(R.string.feed_visibility_1);
                }
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "visibilityCalendarPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        recyclerViewGuestRow.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewGuestRow.setItemAnimator(new LandingAnimator());
        recyclerViewGuestRow.setNestedScrollingEnabled(false);

        addPersonButton.setActivated(false);

        EditActivity editActivity = this;

        if (this.getActivity() == null || invitedList == null || (invitedList != null && invitedList.size() == 0)) {
            getUser(email);
            setProgress(true);
        } else {
            setLayoutWho(this.getActivity(), invitedList, confirmedList);
            recyclerViewGuestRow.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewGuestRow, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, MotionEvent e) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    Intent intent = new Intent(editActivity, ShowGuestsActivity.class);
                    intent.putExtra("guest_list_user", new ListUserWrapper(data));
                    intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                    intent.putExtra("is_adm", editActivity.checkIfAdm(admList, email));
                    intent.putExtra("id_act", editActivity.getActivity().getId());
                    startActivityForResult(intent, GUEST_UPDATE);
                }

                @Override
                public void onLongItemClick(View view, int position, MotionEvent e) {
                }
            }));
        }
    }

    public void addGuestToActivity(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().addNewGuest(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleActivity, this::handleError));
    }

    private void handleActivity(Response response) {
        ActivityServer activityServer = new ActivityServer();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        activityServer.setId(0);
        activityServer.setCreator(email);
        activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

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

        setLayoutWho(getActivity(), invitedList, confirmedList);
        setProgress(false);
    }

    public ActivityServer getActivity() {
        if (activityWrapper != null)
            return activityWrapper.getActivityServer();
        else
            return null;
    }

    @Override
    public void onClick(View v) {
        viewClicked = v;
        if (v == pieceBox) {
            if (iconList.size() > 0)
                createDialogSelectIcon();
        } else if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            edit_activity();
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            activityWrapper.getActivityServer().setCubeIcon(actualIcon);
            activityWrapper.getActivityServer().setCubeColor(actualColor);
            activityWrapper.getActivityServer().setCubeColorUpper(actualColorUpper);
            Intent intent = new Intent(this, ShowActivity.class);
            intent.putExtra("act_show", activityWrapper);
            startActivity(intent);
            finish();
        } else if (v == privacyBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "privacyBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogPrivacy();
        } else if (v == addTagBox) {

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addTagBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int i;
            ArrayList<String> list = new ArrayList<>();
            List<Tag> list_tags = tagGroup.getTags();
            for (i = 0; i < list_tags.size(); i++) {
                list.add(list_tags.get(i).text);
            }
            Intent intent = new Intent(this, SelectTagsActivity.class);
            intent.putStringArrayListExtra("tags_list", list);
            startActivityForResult(intent, 1);
        } else if (v == clearDateStart || v == clearDateEnd || v == clearTimeStart || v == clearTimeEnd) {

            if (v == clearDateStart) {
                day_start = -1;
                month_start = -1;
                year_start = -1;
                dateStart.setText("");
                clearDateStart.setVisibility(View.GONE);
                setRepeatLastDate();
            } else if (v == clearDateEnd) {
                day_end = -1;
                month_end = -1;
                year_end = -1;
                dateEnd.setText("");
                clearDateEnd.setVisibility(View.GONE);
                setRepeatLastDate();
            } else if (v == clearTimeStart) {
                hour_start = -1;
                minutes_start = -1;
                timeStart.setText("");
                clearTimeStart.setVisibility(View.GONE);
            } else if (v == clearTimeEnd) {
                minutes_end = -1;
                hour_end = -1;
                timeEnd.setText("");
                clearTimeEnd.setVisibility(View.GONE);
            }

        } else if (v == repeatAdd) {
            findViewById(R.id.progressRepeatAdd).setVisibility(View.VISIBLE);
            repeatAddIcon.setVisibility(View.INVISIBLE);
            repeatAddText.setVisibility(View.INVISIBLE);

            repeatAdd.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progressRepeatAdd).setVisibility(View.GONE);
                    repeatAdd.setVisibility(View.GONE);
                    repeatBox.setVisibility(View.VISIBLE);
                }
            }, 700);

        } else if ((v == locationBox || v == locationBoxAdd) && !locationNotWorking) {
            setProgress(true);

            if (v == locationBoxAdd) {
                locationBoxAdd.setVisibility(View.GONE);
                locationBox.setVisibility(View.VISIBLE);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mapText" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (lat != -500) {
                LatLng latLng = new LatLng(lat, lng);
                LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);

                try {
                    placePicker = builder.build(this);
                } catch (Exception e) {
                }
            }

            startActivityForResult(placePicker, PLACE_PICKER_REQUEST);
        } else if (v == locationIcon2) {
            findViewById(R.id.progressLocation).setVisibility(View.VISIBLE);
            locationText.setVisibility(View.INVISIBLE);
            locationIcon.setVisibility(View.INVISIBLE);
            locationIcon2.setVisibility(View.INVISIBLE);

            locationIcon2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    locationText.setText("");
                    lat = -500;
                    lng = -500;
                    locationBoxAdd.setVisibility(View.VISIBLE);
                    locationText.setVisibility(View.VISIBLE);
                    locationIcon.setVisibility(View.VISIBLE);
                    locationIcon2.setVisibility(View.VISIBLE);
                    locationBox.setVisibility(View.GONE);
                    findViewById(R.id.progressLocation).setVisibility(View.GONE);
                }
            }, 700);

        } else if (v == timeStart) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start);

            tpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");

        } else if (v == timeEnd) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            if (hour_end != -1)
                tpd.setStartTime(hour_end, minutes_end);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            tpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == dateStart) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    year_start,
                    month_start,
                    day_start
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == dateEnd) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    year_end,
                    month_end,
                    day_end
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == addPersonButton && addPersonButton.isActivated()) {
            Intent intent = new Intent(this, SelectPeopleActivity.class);
            ArrayList<String> list = new ArrayList<>();

            for (int i = 0; i < data.size(); i++) {
                list.add(data.get(i).getEmail());
            }

            intent.putStringArrayListExtra("guest_list", list);
            intent.putExtra("erase_from_list", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (isInPast) {
                Calendar now = Calendar.getInstance();
                Calendar last = Calendar.getInstance();
                ActivityServer activityServer = this.getActivity();
                last.setTimeInMillis(activityServer.getLastDateTime());
                createDialogMessageAddInPast(last.get(Calendar.YEAR), last.get(Calendar.MONTH) + 1, last.get(Calendar.DAY_OF_MONTH), activityServer.getHourEnd(), activityServer.getMinuteEnd(),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            } else {
                startActivityForResult(intent, ADD_GUEST);
            }
        } else if (v == guestBox) {
            SharedPreferences mSharedPreferences = this.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            Intent intent = new Intent(this, ShowGuestsActivity.class);
            intent.putExtra("guest_list_user", new ListUserWrapper(data));
            intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
            intent.putExtra("is_adm", checkIfAdm(admList, email));
            intent.putExtra("id_act", getActivity().getId());

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, GUEST_UPDATE);
        }

    }

    private void edit_activity() {

        boolean dateStartEmpty = false, dateEndEmpty = false, timeStartEmpty = false, timeEndEmpty = false;
        List<Integer> date;
        List<Double> latLng;
        List<Integer> repeat_list_accepted = new ArrayList<>();
        int cube_color;
        int cube_color_upper;
        int repeat_type, repeat_qty;
        String cube_icon;
        String location = "";
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        String whatsapp = whatsAppEditText.getText().toString();
        List<Tag> tags = getTags();

        date = getDateFromView();
        location = getLocationFromView();
        latLng = getLatLngFromView();

        if (customizeCubeLowerBoxIcon == null) {
            cube_color = getActivity().getCubeColor();
            cube_color_upper = getActivity().getCubeColorUpper();
            cube_icon = getActivity().getCubeIcon();
        } else {
            cube_color = customizeCubeLowerBoxIcon.getTag() == null ? getActivity().getCubeColor() : (int) customizeCubeLowerBoxIcon.getTag();
            cube_color_upper = customizeCubeUpperBoxIcon.getTag() == null ? getActivity().getCubeColorUpper() : (int) customizeCubeUpperBoxIcon.getTag();
            cube_icon = urlIcon.equals(Constants.IC_ADD_CUBE_URL) ? getActivity().getCubeIcon() : urlIcon;
        }

        int err = 0;

        if (!repeat_blocked) {
            if (repeatEditText.getText().toString().matches(""))
                repeat_qty = 0;
            else
                repeat_qty = Integer.parseInt(repeatEditText.getText().toString());

            repeat_type = this.repeat_type;
        } else {
            repeat_type = getActivity().getRepeatType();
            repeat_qty = getActivity().getRepeatQty();
        }

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

        if (!validateFields(title)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_title_required, Toast.LENGTH_LONG).show();
        } else if (dateStartEmpty) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_start_required, Toast.LENGTH_LONG).show();
        } else if (!validateDate) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_end_before_start, Toast.LENGTH_LONG).show();
        } else if (!validateTime) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_time_end_before_start, Toast.LENGTH_LONG).show();
        } else if ((repeat_type != 0 && repeat_qty <= 0)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
        } else if (repeat_type == 1 && repeat_qty > 500) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_min_max, Toast.LENGTH_LONG).show();
        } else if (!isActivityReadyRegister(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat_type)) {
            err++;
            Toast.makeText(getApplicationContext(), getErrorMessage(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat_type), Toast.LENGTH_LONG).show();
        } else {

        }

        if (err == 0) {
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

            switch (repeat_type) {
                case Constants.DAILY:
                    calendar.add(Calendar.DAY_OF_WEEK, 1 * repeat_qty);
                    activityServer.setLastDateTime(calendar.getTimeInMillis());
                    break;
                case Constants.WEEKLY:
                    calendar.add(Calendar.DAY_OF_WEEK, 7 * repeat_qty);
                    activityServer.setLastDateTime(calendar.getTimeInMillis());
                    break;
                case Constants.MONTHLY:
                    calendar.add(Calendar.MONTH, 1 * repeat_qty);
                    activityServer.setLastDateTime(calendar.getTimeInMillis());
                    break;
                default:
                    activityServer.setLastDateTime(calendar.getTimeInMillis());
                    break;
            }

            activityServer.setRepeatType(repeat_type);
            activityServer.setRepeatQty(repeat_qty);

            if (!repeat_blocked) {
                if (repeat_qty > 0) {
                    for (int i = 0; i <= repeat_qty; i++) {
                        repeat_list_accepted.add(i);
                    }
                } else {
                    repeat_list_accepted.add(0);
                }

                activityServer.setRepeatListAccepted(repeat_list_accepted);
            }
            else {
                activityServer.setRepeatListAccepted(getActivity().getRepeatListAccepted());
            }

            activityServer.setCubeColor(cube_color);
            activityServer.setCubeColorUpper(cube_color_upper);
            activityServer.setCubeIcon(cube_icon);

            activityServer.setVisibility(selected);

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            activityServer.setCreator(email);

            //Criptografa a url do whatsapp
            String encryptedValue = "";
            if (whatsapp.length() > 0)
                encryptedValue = converter.toGraphProperty(whatsapp);

            activityServer.setWhatsappGroupLink(encryptedValue);

            int i;
            for (i = 0; i < tags.size(); i++) {
                activityServer.addTags(tags.get(i).text);
            }

            activityWrapperEdited = new ActivityWrapper(activityServer);

            editActivity(activityServer);
        }
    }

    private void setRepeatLastDate() {
        int qty = 0;
        int repeat_type = this.repeat_type;

        if (!repeat_blocked) {
            if (!repeatEditText.getText().toString().matches(""))
                qty = Integer.parseInt(repeatEditText.getText().toString());
        } else {
            qty = getActivity().getRepeatQty();
            repeat_type = getActivity().getRepeatType();
        }

        if (qty > 0 && repeat_type > 0 && day_start != -1) {
            repeatNumberBox.setVisibility(View.VISIBLE);
            repeatLastDate.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();

            if (day_end != -1)
                calendar.set(year_end, month_end, day_end);
            else
                calendar.set(year_start, month_start, day_start);

            switch (repeat_type) {
                case Constants.DAILY:
                    calendar.add(Calendar.DAY_OF_WEEK, 1 * qty);
                    getActivity().setLastDateTime(calendar.getTimeInMillis());
                    break;
                case Constants.WEEKLY:
                    calendar.add(Calendar.DAY_OF_WEEK, 7 * qty);
                    getActivity().setLastDateTime(calendar.getTimeInMillis());
                    break;
                case Constants.MONTHLY:
                    calendar.add(Calendar.MONTH, 1 * qty);
                    getActivity().setLastDateTime(calendar.getTimeInMillis());
                    break;
                default:
                    getActivity().setLastDateTime(calendar.getTimeInMillis());
                    break;
            }

            String dayOfWeek = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            String month = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int year = calendar.get(Calendar.YEAR);
            String date = this.getResources().getString(R.string.date_format_03, dayOfWeek, day, month, year);
            repeatLastDate.setText(this.getString(R.string.repeat_last_date, date));
        } else {
            repeatLastDate.setVisibility(View.INVISIBLE);
            if (repeat_type == 0) {
                repeatNumberBox.setVisibility(View.GONE);
            }
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

    private void editActivity(ActivityServer activityServer) {
        //setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editActivity(getActivity().getId(), activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
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

        if (getActivity() != null) {
            if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
                getActivityStartToday();
        }

        activityWrapperEdited.getActivityServer().setId(activityWrapper.getActivityServer().getId());

        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("act_show", activityWrapperEdited);
        startActivity(intent);
        finish();

    }

    private void handleError(Throwable error) {
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

    private void createDialogSelectIcon() {
        LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.activity_customize_piece, null);
        customizeColorPicker = (ColorPickerView) customView.findViewById(R.id.colorpicker);
        customizeCubeLowerBoxIcon = (ImageView) customView.findViewById(R.id.cubeLowerBoxIcon);
        customizeCubeUpperBoxIcon = (ImageView) customView.findViewById(R.id.cubeUpperBoxIcon);
        customizePieceIcon = (ImageView) customView.findViewById(R.id.pieceIcon);
        ImageView closeButton = (ImageView) customView.findViewById(R.id.closeButton);
        RecyclerView recyclerIcons = (RecyclerView) customView.findViewById(R.id.recyclerIcons);

        customizeColorPicker.setDrawDebug(false);

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(customView, false)
                .build();

        customizeApplyButton = (TextView) customView.findViewById(R.id.applyButton);
        customizeApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cubeLowerBoxIcon.setColorFilter((int) customizeCubeLowerBoxIcon.getTag());
                cubeUpperBoxIcon.setColorFilter((int) customizeCubeUpperBoxIcon.getTag());
                cubeLowerBoxIcon.setTag(customizeCubeLowerBoxIcon.getTag());
                cubeUpperBoxIcon.setTag(customizeCubeUpperBoxIcon.getTag());

                if (!urlIconTemp.matches(""))
                    urlIcon = urlIconTemp;

                Glide.clear(pieceIcon);
                Glide.with(EditActivity.this)
                        .load(urlIcon)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(pieceIcon);

                if (activityWrapper != null) {
                    activityWrapper.getActivityServer().setCubeColor((int) customizeCubeLowerBoxIcon.getTag());
                    activityWrapper.getActivityServer().setCubeColorUpper((int) customizeCubeUpperBoxIcon.getTag());
                    activityWrapper.getActivityServer().setCubeIcon(urlIcon);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "customizeApplyButtonPickIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        closeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    closeButton.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.grey_400));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeButton.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.grey_200));
                }

                return false;
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        customizeCleanButton = (TextView) customView.findViewById(R.id.cleanButton);
        customizeCleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activityWrapper != null) {
                    customizeCubeLowerBoxIcon.setColorFilter(activityWrapper.getActivityServer().getCubeColor());
                    customizeCubeUpperBoxIcon.setColorFilter(activityWrapper.getActivityServer().getCubeColorUpper());
                    customizeCubeLowerBoxIcon.setTag(activityWrapper.getActivityServer().getCubeColor());
                    customizeCubeUpperBoxIcon.setTag(activityWrapper.getActivityServer().getCubeColorUpper());
                    urlIcon = activityWrapper.getActivityServer().getCubeIcon();
                } else {
                    customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(customView.getContext(), R.color.deep_purple_400));
                    customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(customView.getContext(), R.color.deep_purple_400_light));
                    customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400));
                    customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400_light));
                    urlIcon = Constants.IC_ADD_CUBE_URL;
                }

                urlIconTemp = "";

                Glide.clear(customizePieceIcon);
                Glide.with(EditActivity.this)
                        .load(urlIcon)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(customizePieceIcon);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "customizeCleanButtonPickIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        recyclerIcons.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 5);
        recyclerIcons.setLayoutManager(layoutManager);
        recyclerIcons.setNestedScrollingEnabled(false);

        CustomizeAddActivityAdapter adapter = new CustomizeAddActivityAdapter(this, iconList);

        ArrayList<CategoryServer> iconCategory = new ArrayList<>();
        List<Integer> positionsNull = new ArrayList<>();

        iconCategory = getIconCategory(iconCategory, iconList);

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();

        //Sections
        for (int i = 0; i < iconCategory.size(); i++) {
            sections.add(new SectionedGridRecyclerViewAdapter.Section(iconCategory.get(i).getPosition(), iconCategory.get(i).getName()));
            if (i == 0)
                positionsNull.add(0);
            else
                positionsNull.add(iconCategory.get(i - 1).getPosition() + iconCategory.get(i - 1).getIconsQty() + i);
        }

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(this, R.layout.list_item_section, R.id.textIcon, recyclerIcons, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        recyclerIcons.setAdapter(mSectionedAdapter);

        RecyclerItemClickListener.OnItemClickListener onItemClickListener = new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {

                if (!positionsNull.contains(position)) {
                    for (int i = 0; i < positionsNull.size(); i++) {
                        if (position < positionsNull.get(i)) {
                            urlIconTemp = adapter.getItem(position - i).getUrl();
                            break;
                        }
                    }

                    Glide.clear(customizePieceIcon);
                    Glide.with(EditActivity.this)
                            .load(urlIconTemp)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(customizePieceIcon);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "RecyclerItemPickIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        };

        recyclerIcons.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerIcons, onItemClickListener)
        );

        customizeColorPicker.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {

                if (first_open && (cubeUpperBoxIcon.getTag() != null || (activityWrapper != null && activityWrapper.getActivityServer().getCubeIcon().contains("http")))) {

                    if (activityWrapper != null) {
                        customizeCubeLowerBoxIcon.setColorFilter(activityWrapper.getActivityServer().getCubeColor());
                        customizeCubeUpperBoxIcon.setColorFilter(activityWrapper.getActivityServer().getCubeColorUpper());
                        customizeCubeLowerBoxIcon.setTag(activityWrapper.getActivityServer().getCubeColor());
                        customizeCubeUpperBoxIcon.setTag(activityWrapper.getActivityServer().getCubeColorUpper());
                    } else {
                        customizeCubeLowerBoxIcon.setColorFilter((int) cubeLowerBoxIcon.getTag());
                        customizeCubeUpperBoxIcon.setColorFilter((int) cubeUpperBoxIcon.getTag());
                        customizeCubeLowerBoxIcon.setTag(cubeLowerBoxIcon.getTag());
                        customizeCubeUpperBoxIcon.setTag(cubeUpperBoxIcon.getTag());
                    }
                    first_open = false;
                } else {
                    if (color == ContextCompat.getColor(EditActivity.this, R.color.red_A700)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.red_A700));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.red_A700_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.red_A700));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.red_A700_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.pink_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.pink_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.pink_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.pink_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.pink_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.pink_900)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.pink_900));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.pink_900_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.pink_900));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.pink_900_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.purple_500)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.purple_500));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.purple_500_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.purple_500));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.purple_500_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.deep_purple_800)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_800));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_800_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_800));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_800_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.blue_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.blue_800)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_800));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_800_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_800));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_800_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.cyan_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.cyan_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.cyan_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.cyan_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.cyan_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.cyan_800)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.cyan_800));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.cyan_800_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.cyan_800));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.cyan_800_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.green_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.green_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.green_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.green_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.green_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.lime_600)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.lime_600));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.lime_600_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.lime_600));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.lime_600_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.deep_orange_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.deep_orange_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.deep_orange_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_orange_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_orange_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.brown_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.brown_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.brown_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.brown_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.brown_400_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.brown_700)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.brown_700));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.brown_700_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.brown_700));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.brown_700_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.grey_500)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.grey_500));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.grey_500_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.grey_500));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.grey_500_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.blue_grey_500)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_500));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_500_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_500));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_500_light));
                    } else if (color == ContextCompat.getColor(EditActivity.this, R.color.blue_grey_900)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_900));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_900_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_900));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.blue_grey_900_light));
                    }
                }
            }
        });

        if (activityWrapper == null || !activityWrapper.getActivityServer().getCubeIcon().contains("http")) {
            if (cubeUpperBoxIcon.getTag() == null) {
                urlIcon = Constants.IC_ADD_CUBE_URL;
                cubeLowerBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400));
                cubeUpperBoxIcon.setTag(ContextCompat.getColor(EditActivity.this, R.color.deep_purple_400_light));
            } else {
                cubeLowerBoxIcon.setTag(cubeLowerBoxIcon.getTag());
                cubeUpperBoxIcon.setTag(cubeUpperBoxIcon.getTag());
            }

            Glide.clear(customizePieceIcon);
            Glide.with(EditActivity.this)
                    .load(urlIcon)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(customizePieceIcon);
        } else {
            urlIcon = activityWrapper.getActivityServer().getCubeIcon();
            cubeLowerBoxIcon.setTag(activityWrapper.getActivityServer().getCubeColor());
            cubeUpperBoxIcon.setTag(activityWrapper.getActivityServer().getCubeColorUpper());

            Glide.clear(customizePieceIcon);
            Glide.with(EditActivity.this)
                    .load(urlIcon)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(customizePieceIcon);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                first_open = true;
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
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

                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private ArrayList<CategoryServer> getIconCategory(ArrayList<CategoryServer> iconCategory, ArrayList<IconServer> icons) {
        int i, j = 0;

        for (i = 0; i < icons.size(); i++) {
            CategoryServer category = new CategoryServer();

            if (i == 0) {
                category.setName(icons.get(i).getCategory());
                category.setIconsQty(1);
                category.setPosition(0);
                iconCategory.add(category);
            } else if (icons.get(i).getCategory().matches(icons.get(i - 1).getCategory())) {
                iconCategory.get(j).setIconsQty(iconCategory.get(j).getIconsQty() + 1);
            } else {
                j++;
                category.setName(icons.get(i).getCategory());
                category.setIconsQty(1);
                category.setPosition(iconCategory.get(j - 1).getPosition() + iconCategory.get(j - 1).getIconsQty());
                iconCategory.add(category);
            }
        }

        return iconCategory;
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

    private void handleErrorToday(Throwable error) {
    }

    private boolean isActivityInPast(ActivityServer activityServer) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        Calendar last = Calendar.getInstance();
        last.setTimeInMillis(activityServer.getLastDateTime());

        boolean isHourBefore = isTimeInBefore(hour + ":" + minute, activityServer.getHourEnd() + ":" + activityServer.getMinuteEnd());
        boolean isDateBefore = isDateInBefore(last.get(Calendar.YEAR), last.get(Calendar.MONTH) + 1, last.get(Calendar.DAY_OF_MONTH), year, month, day);

        return (isHourBefore && isDateBefore) || isDateBefore;
    }

    private boolean isDateInBefore(int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        if (yearEnd < year)
            return false;
        if (year == yearEnd) {
            if (monthOfYearEnd < monthOfYear)
                return false;
            else if (monthOfYearEnd == monthOfYear) {
                if (dayOfMonthEnd <= dayOfMonth)
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

    public void setLayoutWho(ActivityServer activityServer, ArrayList<User> users, ArrayList<User> confirmed) {
        if (recyclerViewGuestRow != null) {
            invite = activityServer.getInvitationType();

            spinner.setSelectedIndex(invite);

            if (invite == 2) {
                feedVisibility.setText(R.string.feed_visibility_2);
            } else {
                feedVisibility.setText(R.string.feed_visibility_1);
            }

            data.clear();
            listConfirmed.clear();
            listConfirmed.addAll(confirmed);

            for (int i = 0; i < users.size(); i++) {
                User usr = users.get(i);
                usr.setDelete(false);
                data.add(usr);
            }

            adapter = new PersonAdapter(data, this);
            recyclerViewGuestRow.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(data.size()));
            addPersonButton.setActivated(true);

            isInPast = isActivityInPast(activityServer);

            addPersonButton.setOnClickListener(this);
        }
    }

    public void setLayoutWhenWhere(ActivityServer activityServer) {
        if (activityServer != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());

            String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());
            String day2 = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month2 = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String date = day + "/" + month + "/" + activityServer.getYearStart();
            String date2 = day2 + "/" + month2 + "/" + activityServer.getYearEnd();

            String hourString = String.format("%02d", activityServer.getHourStart());
            String minuteString = String.format("%02d", activityServer.getMinuteStart());
            String hourStringEnd = String.format("%02d", activityServer.getHourEnd());
            String minuteStringEnd = String.format("%02d", activityServer.getMinuteEnd());
            String time = hourString + ":" + minuteString;
            String time2 = hourStringEnd + ":" + minuteStringEnd;

            day_start = activityServer.getDayStart();
            month_start = activityServer.getMonthStart() - 1;
            year_start = activityServer.getYearStart();
            minutes_start = activityServer.getMinuteStart();
            hour_start = activityServer.getHourStart();

            day_end = activityServer.getDayEnd();
            month_end = activityServer.getMonthEnd() - 1;
            year_end = activityServer.getYearEnd();
            minutes_end = activityServer.getMinuteEnd();
            hour_end = activityServer.getHourEnd();

            dateStart.setText(date);
            dateEnd.setText(date2);
            timeStart.setText(time);
            timeEnd.setText(time2);

            if (activityServer.getLat() == -500) {
                locationBoxAdd.setVisibility(View.VISIBLE);
                locationBox.setVisibility(View.GONE);
                locationText.setText("");
            } else {
                locationBoxAdd.setVisibility(View.GONE);
                locationBox.setVisibility(View.VISIBLE);
                locationText.setText(activityServer.getLocation());
            }

            lat = activityServer.getLat();
            lng = activityServer.getLng();

            if (activityServer.getRepeatType() == 0 || activityServer.getRepeatType() == 5) {
                repeatAdd.setVisibility(View.VISIBLE);
                repeatBox.setVisibility(View.GONE);
                repeatNumberBox.setVisibility(View.GONE);
            } else {
                repeat_blocked = true;
                repeatAdd.setVisibility(View.GONE);
                repeatBox.setVisibility(View.GONE);
                repeatNumberBox.setVisibility(View.GONE);
            }

            if (activityServer.getDateStartEmpty()) {
                day_start = -1;
                month_start = -1;
                year_start = -1;
                dateStart.setText("");
                clearDateStart.setVisibility(View.GONE);
            } else {
                clearDateStart.setVisibility(View.VISIBLE);
            }

            if (activityServer.getDateEndEmpty()) {
                day_end = -1;
                month_end = -1;
                year_end = -1;
                dateEnd.setText("");
                clearDateEnd.setVisibility(View.GONE);
            } else {
                clearDateEnd.setVisibility(View.VISIBLE);
            }

            if (activityServer.getTimeStartEmpty()) {
                hour_start = -1;
                minutes_start = -1;
                timeStart.setText("");
                clearTimeStart.setVisibility(View.GONE);
            } else {
                clearTimeStart.setVisibility(View.VISIBLE);
            }

            if (activityServer.getTimeEndEmpty()) {
                minutes_end = -1;
                hour_end = -1;
                timeEnd.setText("");
                clearTimeEnd.setVisibility(View.GONE);
            } else {
                clearTimeEnd.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog2");
        if (dpd != null) dpd.setOnDateSetListener(this);

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        if (tpd != null) tpd.setOnTimeSetListener(this);
    }

    private void createDialogLocation(String adr) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        text1.setText(this.getResources().getString(R.string.popup_message_naming_activity_local_title));
        text2.setText(this.getResources().getString(R.string.popup_message_naming_activity_local_text));
        buttonText1.setText(this.getResources().getString(R.string.cancel));
        buttonText2.setText(this.getResources().getString(R.string.customize));
        editText.setText(adr);

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
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancelDialogLocation" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmDialogLocation" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                location = editText.getText().toString();
                if (!location.matches(""))
                    locationText.setText(location);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day + "/" + month + "/" + year;


        if (viewClicked == dateStart) {
            day_start = dayOfMonth;
            month_start = monthOfYear;
            year_start = year;
            dateStart.setText(date);
        } else {
            day_end = dayOfMonth;
            month_end = monthOfYear;
            year_end = year;
            dateEnd.setText(date);
        }

        if (dateStart.getText().toString().matches("")) {
            clearDateStart.setVisibility(View.GONE);
        } else {
            clearDateStart.setVisibility(View.VISIBLE);
        }

        if (dateEnd.getText().toString().matches("")) {
            clearDateEnd.setVisibility(View.GONE);
        } else {
            clearDateEnd.setVisibility(View.VISIBLE);
        }

        setRepeatLastDate();

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String time = hourString + ":" + minuteString;

        if (viewClicked == timeStart) {
            hour_start = hourOfDay;
            minutes_start = minute;
            timeStart.setText(time);
        } else {
            hour_end = hourOfDay;
            minutes_end = minute;
            timeEnd.setText(time);
        }

        if (timeStart.getText().toString().matches("")) {
            clearTimeStart.setVisibility(View.GONE);
        } else {
            clearTimeStart.setVisibility(View.VISIBLE);
        }

        if (timeEnd.getText().toString().matches("")) {
            clearTimeEnd.setVisibility(View.GONE);
        } else {
            clearTimeEnd.setVisibility(View.VISIBLE);
        }

    }

    private boolean validateDateTime(Calendar calendarStart, Calendar calendarEnd) {
        if (calendarEnd.getTimeInMillis() < calendarStart.getTimeInMillis()) {
            return false;
        }

        return true;
    }

    public List<Integer> getDateFromView() {
        List<Integer> list = new ArrayList<>();

        list.add(day_start);
        list.add(month_start);
        list.add(year_start);

        list.add(day_end);
        list.add(month_end);
        list.add(year_end);

        list.add(minutes_start);
        list.add(hour_start);

        list.add(minutes_end);
        list.add(hour_end);

        return list;
    }

    public String getLocationFromView() {
        return locationText.getText().toString();
    }

    public List<Double> getLatLngFromView() {
        List<Double> list = new ArrayList<>();

        list.add(lat);
        list.add(lng);
        return list;
    }

    public List<Integer> getRepeatFromView() {
        List<Integer> list = new ArrayList<>();
        String temp = repeatEditText.getText().toString();
        if (repeat_type == 0)
            repeat_qty = -1;
        else if (temp.length() > 0)
            repeat_qty = Integer.parseInt(temp);

        list.add(repeat_type);
        list.add(repeat_qty);
        return list;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == locationBox && lat != -500) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "locationText" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogLocation(locationText.getText().toString());
        }
        return true;
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
