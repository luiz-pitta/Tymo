package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.CustomizeAddActivityAdapter;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.adapters.SectionedGridRecyclerViewAdapter;
import io.development.tymo.fragments.FlagEditFragment;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.CategoryServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.IconServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateFields;

public class AddPart2Activity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ActivityWrapper activityWrapper;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CompositeDisposable mSubscriptions;
    private ActivityServer activityServer;
    private PersonAdapter adapter;
    private Handler handler = new Handler();

    private TextView confirmationButton, guestText, guestsNumber, addGuestText, feedVisibility;
    private ImageView mBackButton;
    private MaterialSpinner spinner;
    private RecyclerView recyclerViewGuestRow;
    private LinearLayout guestBox;
    private View addGuestButtonDivider, profilesPhotos;
    private RelativeLayout addPersonButton;

    private int invite = 0;
    private final int ADD_GUEST = 39;
    private Rect rect;

    ArrayList<User> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_create_step_2);

        mSubscriptions = new CompositeDisposable();

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        guestBox = (LinearLayout) findViewById(R.id.guestBox);
        guestText = (TextView) findViewById(R.id.guestText);
        guestsNumber = (TextView) findViewById(R.id.guestsNumber);
        profilesPhotos = findViewById(R.id.profilesPhotos);
        recyclerViewGuestRow = (RecyclerView) findViewById(R.id.guestRow);
        addPersonButton = (RelativeLayout) findViewById(R.id.addGuestButton);
        addGuestText = (TextView) findViewById(R.id.addGuestText);
        addGuestButtonDivider = (View) findViewById(R.id.addGuestButtonDivider);
        spinner = (MaterialSpinner) findViewById(R.id.visibilityCalendarPicker);
        feedVisibility = (TextView) findViewById(R.id.feedVisibility);

        mBackButton.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        addPersonButton.setOnClickListener(this);
        guestBox.setOnClickListener(this);

        guestBox.setOnTouchListener(this);
        mBackButton.setOnTouchListener(this);

        confirmationButton.setText(R.string.advance);

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_wrapper");

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

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        getUser(email);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private void getUser(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfile(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    public List<User> getGuestFromView() {
        return data;
    }

    private void handleResponse(User user) {
        /*data = new ArrayList<>();
        user.setDelete(false);
        data.add(user);
        User friend = flagActivity.getUserFriend();
        ArrayList<User> list = flagActivity.getListUserCompare();
        if (friend != null) {
            friend.setDelete(false);
            data.add(friend);
        } else if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                User usr = list.get(i);
                usr.setDelete(false);
                data.add(usr);
            }
        }

        data = setOrderGuests(data);

        adapter = new PersonAdapter(data, this);
        recyclerViewGuestRow.setAdapter(adapter);
        guestsNumber.setText(String.valueOf(data.size()));
        addPersonButton.setActivated(true);*/
    }

    private void handleError(Throwable error) {
        if (Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
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
                long id1 = c1.getInvitation();
                long id2 = c2.getInvitation();

                if (id1 == 1)
                    return -1;
                else if (id2 == 1)
                    return 1;
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
                boolean id1 = c1.isCreator();
                boolean id2 = c2.isCreator();

                if (id1 && !id2)
                    return -1;
                else if (!id1 && id2)
                    return 1;
                else
                    return 0;
            }
        });

        return users;
    }

    public ActivityServer getActivity() {
        if (activityWrapper != null)
            return activityWrapper.getActivityServer();
        else
            return null;
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_GUEST) {
                ArrayList<User> list = new ArrayList<>();
                PersonModelWrapper wrap =
                        (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                list.add(data.get(0));
                list.addAll(wrap.getItemDetails());
                for (int i = 0; i < list.size(); i++) {
                    User usr = list.get(i);
                    usr.setDelete(false);
                }
                adapter.swap(list);
                guestsNumber.setText(String.valueOf(list.size()));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            Intent register = new Intent(AddPart2Activity.this, AddPart3Activity.class);
            register.putExtra("act_wrapper", activityWrapper);
            startActivity(register);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
            //register();
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (v == addPersonButton || v == guestBox) {
            Intent intent = new Intent(this, SelectPeopleActivity.class);
            ArrayList<String> list = new ArrayList<>();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            for (int i = 0; i < data.size(); i++) {
                list.add(data.get(i).getEmail());
            }

            intent.putStringArrayListExtra("guest_list", list);

            startActivityForResult(intent, ADD_GUEST);
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
        } else if (view == guestBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                guestText.setTextColor(getResources().getColor(R.color.deep_purple_400));
                guestsNumber.setBackground(getResources().getDrawable(R.drawable.box_qty_guests));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                guestText.setTextColor(getResources().getColor(R.color.deep_purple_200));
                guestsNumber.setBackground(getResources().getDrawable(R.drawable.box_qty_guests_pressed));
            }
        }

        return false;
    }

}
