package io.development.tymo.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;

import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PrivacyActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView text, cancelButton, updatingButton;
    private LinearLayout progressBox;
    private MaterialSpinner spinner;

    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private int privacy_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_privacy);

        mSubscriptions = new CompositeSubscription();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        text = (TextView) findViewById(R.id.text);
        cancelButton = (TextView) findViewById(R.id.cancelButton);
        updatingButton = (TextView) findViewById(R.id.updatingButton);

        mBackButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        updatingButton.setOnClickListener(this);

        spinner = (MaterialSpinner) findViewById(R.id.visibilityCalendarPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_privacy));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                privacy_type = position;
            }
        });

        text.setText(getResources().getString(R.string.privacy));

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        getPrivacy(email);

        findViewById(R.id.progressLoadingBox).setVisibility(View.VISIBLE);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void getPrivacy(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfile(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void setPrivacy(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().setPrivacyUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(User user) {

        spinner.setSelectedIndex(user.getPrivacy());
        progressBox.setVisibility(View.GONE);
        findViewById(R.id.progressLoadingBox).setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {
        //progressBox.setVisibility(View.GONE);
        //findViewById(R.id.progressLoadingBox).setVisibility(View.GONE);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if(view == cancelButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancelButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            finish();
        }
        else if(view == updatingButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "updatingButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            User user = new User();

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            progressBox.setVisibility(View.VISIBLE);

            user.setPrivacy(privacy_type);
            user.setEmail(email);

            setPrivacy(user);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}
