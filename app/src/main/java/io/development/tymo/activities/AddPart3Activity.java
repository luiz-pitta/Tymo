package io.development.tymo.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
import io.development.tymo.fragments.FlagEditFragment;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
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

public class AddPart3Activity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ActivityWrapper activityWrapper;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CompositeDisposable mSubscriptions;
    private ActivityServer activityServer;

    private TextView confirmationButton;
    private ImageView mBackButton;
    private RelativeLayout optionBox1, optionBox2, optionBox3;
    private ImageView checkBoxActivated1, checkBoxActivated2, checkBoxActivated3, optionIcon1, optionIcon2, optionIcon3;
    private TextView optionTitle1, optionTitle2, optionTitle3, optionText1, optionText2, optionText3;
    
    private int selected = -1;

    ArrayList<User> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_create_step_3);

        mSubscriptions = new CompositeDisposable();

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        optionBox1 = (RelativeLayout) findViewById(R.id.optionBox1);
        optionBox2 = (RelativeLayout) findViewById(R.id.optionBox2);
        optionBox3 = (RelativeLayout) findViewById(R.id.optionBox3);
        checkBoxActivated1 = (ImageView) findViewById(R.id.checkBoxActivated1);
        checkBoxActivated2 = (ImageView) findViewById(R.id.checkBoxActivated2);
        checkBoxActivated3 = (ImageView) findViewById(R.id.checkBoxActivated3);
        optionIcon1 = (ImageView) findViewById(R.id.optionIcon1);
        optionIcon2 = (ImageView) findViewById(R.id.optionIcon2);
        optionIcon3 = (ImageView) findViewById(R.id.optionIcon3);
        optionTitle1 = (TextView) findViewById(R.id.optionTitle1);
        optionTitle2 = (TextView) findViewById(R.id.optionTitle2);
        optionTitle3 = (TextView) findViewById(R.id.optionTitle3);
        optionText1 = (TextView) findViewById(R.id.optionText1);
        optionText2 = (TextView) findViewById(R.id.optionText2);
        optionText3 = (TextView) findViewById(R.id.optionText3);

        mBackButton.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        optionBox1.setOnClickListener(this);
        optionBox2.setOnClickListener(this);
        optionBox3.setOnClickListener(this);

        mBackButton.setOnTouchListener(this);

        confirmationButton.setText(R.string.create);

        activityWrapper = (ActivityWrapper) getIntent().getSerializableExtra("act_wrapper");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private void register() {
        int err = 0;
        if (selected < 0) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_visibility_required, Toast.LENGTH_LONG).show();
        }

        if (err == 0){
            activityWrapper.getActivityServer().setVisibility(selected);

            registerProcess(activityWrapper.getActivityServer());
            setProgress(true);
        }
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void registerProcess(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerActivity(activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        /*setProgress(false);

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
        } else
            startActivity(intent);*/

    }

    private void handleError(Throwable error) {
        //setProgress(false);
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }
    
    private void updateLayout(){
        switch (selected) {
            case 1:
                optionIcon1.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle1.setTextColor(getResources().getColor(R.color.grey_600));
                optionText1.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox1.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon2.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                optionTitle2.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionText2.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionBox2.setBackgroundColor(getResources().getColor(R.color.select));
                optionIcon3.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle3.setTextColor(getResources().getColor(R.color.grey_600));
                optionText3.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox3.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case 2:
                optionIcon1.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle1.setTextColor(getResources().getColor(R.color.grey_600));
                optionText1.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox1.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon2.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle2.setTextColor(getResources().getColor(R.color.grey_600));
                optionText2.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox2.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon3.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                optionTitle3.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionText3.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionBox3.setBackgroundColor(getResources().getColor(R.color.select));
                break;
            default:
                optionIcon1.setColorFilter(getResources().getColor(R.color.deep_purple_400));
                optionTitle1.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionText1.setTextColor(getResources().getColor(R.color.deep_purple_400));
                optionBox1.setBackgroundColor(getResources().getColor(R.color.select));
                optionIcon2.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle2.setTextColor(getResources().getColor(R.color.grey_600));
                optionText2.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox2.setBackgroundColor(getResources().getColor(R.color.transparent));
                optionIcon3.setColorFilter(getResources().getColor(R.color.grey_600));
                optionTitle3.setTextColor(getResources().getColor(R.color.grey_600));
                optionText3.setTextColor(getResources().getColor(R.color.grey_600));
                optionBox3.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            register();
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (v == optionBox1) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "optionBox1" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            checkBoxActivated1.setVisibility(View.VISIBLE);
            checkBoxActivated2.setVisibility(View.GONE);
            checkBoxActivated3.setVisibility(View.GONE);
            selected = 0;
            updateLayout();
        } else if (v == optionBox2) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "optionBox2" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            checkBoxActivated1.setVisibility(View.GONE);
            checkBoxActivated2.setVisibility(View.VISIBLE);
            checkBoxActivated3.setVisibility(View.GONE);
            selected = 1;
            updateLayout();
        } else if (v == optionBox3) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "optionBox3" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            checkBoxActivated1.setVisibility(View.GONE);
            checkBoxActivated2.setVisibility(View.GONE);
            checkBoxActivated3.setVisibility(View.VISIBLE);
            selected = 2;
            updateLayout();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(getResources().getColor(R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(getResources().getColor(R.color.grey_400));
            }
        }

        return false;
    }

}
