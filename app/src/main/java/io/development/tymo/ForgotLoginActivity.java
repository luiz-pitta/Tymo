package io.development.tymo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import io.development.tymo.model_server.Response;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateEmail;


public class ForgotLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, sendButton, text2;
    private EditText email;
    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_forgot);

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        editor = mSharedPreferences.edit();

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        email = (EditText) findViewById(R.id.email);
        sendButton = (TextView) findViewById(R.id.sendButton);
        text2 = (TextView) findViewById(R.id.text2);
        text2.setVisibility(View.GONE);

        mBackButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        m_title.setText(getResources().getString(R.string.help));

        mSubscriptions = new CompositeSubscription();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void passwordResetInit(String mEmail) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().resetPasswordInit(mEmail)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_login_email_check), Toast.LENGTH_LONG).show();
        startActivity(new Intent(ForgotLoginActivity.this, ResetPasswordActivity.class));
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
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        }else if(view == sendButton) {
            editor.putString(Constants.FORGOT_PASS, email.getText().toString());
            editor.commit();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sendButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(validateEmail(email.getText().toString()))
                passwordResetInit(email.getText().toString());
            else
                Toast.makeText(this, getResources().getString(R.string.error_email_required), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}
