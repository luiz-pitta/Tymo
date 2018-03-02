package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterPart2Activity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton;
    private TextView m_title, m_title2, advanceButton;
    private LinearLayout progressBox;
    private InputStream inputStream;
    private Cloudinary cloudinary;
    private CompositeDisposable mSubscriptions;
    private SharedPreferences mSharedPreferences;
    private UploadCloudinary uploadCloudinary;

    private int visibilityCalendar = 0;
    private UserWrapper wrap;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_part_2);

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        m_title2 = (TextView) findViewById(R.id.text2);
        advanceButton = (TextView) findViewById(R.id.advanceButton);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);
        uploadCloudinary = new UploadCloudinary();

        mBackButton.setOnClickListener(this);
        advanceButton.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);
        advanceButton.setOnTouchListener(this);

        m_title.setText(getResources().getString(R.string.register));
        m_title2.setText(getResources().getString(R.string.register_steps, 2, 2));

        wrap = (UserWrapper) getIntent().getSerializableExtra("user_wrapper");

        inputStream = TymoApplication.getInstance().getInputStreamer();

        mSubscriptions = new CompositeDisposable();

        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.visibilityCalendarPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_privacy));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                visibilityCalendar = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "visibilityCalendarPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if (view == advanceButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "advanceButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            //Intent register = new Intent(RegisterPart2Activity.this, RegisterPart3Activity.class);
            //register.putExtra("user_wrapper", wrap);
            //register.putExtra("user_privacy", visibilityCalendar);
            //register.putExtra("register_with_facebook", false);
            //startActivity(register);
            //overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);

            progressBox.setVisibility(View.VISIBLE);
            User user = wrap.getUser();

            user.setPrivacy(visibilityCalendar);
                if (inputStream != null)
                    uploadCloudinary.execute(user);
                else
                    registerProcess(user);
        }
    }

    private class UploadCloudinary extends AsyncTask<User, Void, User> {

        private Exception exception;

        protected User doInBackground(User... users) {
            User mUser = users[0];
            try {
                Map options = ObjectUtils.asMap(
                        "transformation", new Transformation().width(600).height(600).crop("limit").quality(10).fetchFormat("png")
                );
                Map uploadResult = cloudinary.uploader().upload(inputStream, options);
                mUser.setPhoto((String) uploadResult.get("secure_url"));
            } catch (Exception e) {
                this.exception = e;
            }
            return mUser;
        }

        protected void onPostExecute(User user) {
            registerProcess(user);
        }
    }

    private void registerProcess(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().register(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResister, this::handleError));
    }

    private void handleResister(Response response) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        User user = wrap.getUser();

        editor.putString(Constants.EMAIL, user.getEmail());
        editor.putBoolean(Constants.LOGIN_TYPE, user.getFromFacebook());
        editor.putString(Constants.USER_NAME, user.getName());
        editor.putBoolean(Constants.LOCATION, user.isLocationGps());
        editor.putBoolean(Constants.NOTIFICATION_ACT, user.isNotificationActivity());
        editor.putBoolean(Constants.NOTIFICATION_FLAG, user.isNotificationFlag());
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, user.isNotificationReminder());
        editor.putBoolean(Constants.NOTIFICATION_PUSH, user.isNotificationPush());

        editor.apply();

        Intent intent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        progressBox.setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {
        try {
            if (error instanceof retrofit2.HttpException) {
                Gson gson = new GsonBuilder().create();
                try {

                    String errorBody = ((retrofit2.HttpException) error).response().errorBody().string();
                    Response response = gson.fromJson(errorBody, Response.class);
                    progressBox.setVisibility(View.GONE);
                    Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!Utilities.isDeviceOnline(this))
                    Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            if (!Utilities.isDeviceOnline(this))
                Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_left_exit_back, R.anim.push_left_enter_back);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_100));
            }
        }
        else if (view == advanceButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                advanceButton.setTextColor(ContextCompat.getColor(this, R.color.white));
                advanceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_login_advance));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                advanceButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_100));
                advanceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_login_advance_pressed));
            }
        }

        return false;
    }

}
