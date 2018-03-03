package io.development.tymo.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class NotificationCenterActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton;
    private TextView mTitle;
    private Switch activityNotificationSwitch, flagNotificationSwitch, reminderNotificationSwitch, otherNotificationSwitch;

    private CompositeDisposable mSubscriptions;
    private User user;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notification_center);

        UserWrapper userWrapper = (UserWrapper)getIntent().getSerializableExtra("user_about");
        user = userWrapper.getUser();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mTitle = (TextView) findViewById(R.id.text);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        activityNotificationSwitch = (Switch) findViewById(R.id.startActSwitch);
        flagNotificationSwitch = (Switch) findViewById(R.id.startFlagSwitch);
        reminderNotificationSwitch = (Switch) findViewById(R.id.startReminderSwitch);
        otherNotificationSwitch = (Switch) findViewById(R.id.otherNotificationSwitch2);

        mSubscriptions = new CompositeDisposable();

        mTitle.setText(getResources().getString(R.string.settings_notification_center));

        mBackButton.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);

        activityNotificationSwitch.setChecked(user.isNotificationActivity());
        flagNotificationSwitch.setChecked(user.isNotificationFlag());
        reminderNotificationSwitch.setChecked(user.isNotificationReminder());
        otherNotificationSwitch.setChecked(user.isNotificationPush());

        activityNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setNotificationActivity(isChecked);
                user.setNotificationFlag(flagNotificationSwitch.isChecked());
                user.setNotificationReminder(reminderNotificationSwitch.isChecked());
                user.setNotificationPush(otherNotificationSwitch.isChecked());
                setNotifications(user);
            }
        });

        flagNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setNotificationFlag(isChecked);
                user.setNotificationActivity(activityNotificationSwitch.isChecked());
                user.setNotificationReminder(reminderNotificationSwitch.isChecked());
                user.setNotificationPush(otherNotificationSwitch.isChecked());
                setNotifications(user);
            }
        });

        reminderNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setNotificationReminder(isChecked);
                user.setNotificationActivity(activityNotificationSwitch.isChecked());
                user.setNotificationFlag(flagNotificationSwitch.isChecked());
                user.setNotificationPush(otherNotificationSwitch.isChecked());
                setNotifications(user);
            }
        });

        otherNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setNotificationPush(isChecked);
                user.setNotificationActivity(activityNotificationSwitch.isChecked());
                user.setNotificationFlag(flagNotificationSwitch.isChecked());
                user.setNotificationReminder(reminderNotificationSwitch.isChecked());
                setNotifications(user);
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if(v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void setNotifications(User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateNotificationUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(User user) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
        editor.putBoolean(Constants.NOTIFICATION_ACT, user.isNotificationActivity());
        editor.putBoolean(Constants.NOTIFICATION_FLAG, user.isNotificationFlag());
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, user.isNotificationReminder());
        editor.putBoolean(Constants.NOTIFICATION_PUSH, user.isNotificationPush());
        editor.apply();

        setProgress(false);
    }

    private void handleError(Throwable error) {
        setProgress(false);
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
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
