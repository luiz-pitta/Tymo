package io.development.tymo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateFields;
import static io.development.tymo.utils.Validation.validatePasswordSize;


public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView sendButton;
    private EditText password, token;
    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password_reset);

        password = (EditText) findViewById(R.id.password);
        token = (EditText) findViewById(R.id.token);
        sendButton = (TextView) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(this);

        mSubscriptions = new CompositeSubscription();

        mUser = new User();
        Uri data = getIntent().getData();
        if(data != null) {
            mUser.setToken(data.getLastPathSegment());
        }
        else
            token.setVisibility(View.VISIBLE);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void passwordResetInit(String mEmail, User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().resetPasswordFinish(mEmail, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);
        startActivity(new Intent(ResetPasswordActivity.this, Login1Activity.class));
        finish();
    }

    private void handleError(Throwable error) {
        if (error instanceof HttpException) {
            Gson gson = new GsonBuilder().create();
            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                setProgress(false);
                Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                setProgress(false);
            }
        } else {
            setProgress(false);
            Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == sendButton) {
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.FORGOT_PASS, "");

            if (!validateFields(password.getText().toString())) {
                password.setError(getResources().getString(R.string.error_field_invalid));
                Toast.makeText(this, getResources().getString(R.string.error_password_required), Toast.LENGTH_LONG).show();
            }
            else if (!validatePasswordSize(password.getText().toString())) {
                password.setError(getResources().getString(R.string.error_password_minimun));
                Toast.makeText(this, getResources().getString(R.string.error_password_minimun), Toast.LENGTH_LONG).show();
            }else {
                mUser.setPassword(password.getText().toString());
                passwordResetInit(email, mUser);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sendButton"+ "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri data = getIntent().getData();
        if(data != null) {
            mUser.setToken(data.getLastPathSegment());
            token.setText(data.getLastPathSegment());
        }
        else
            token.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
