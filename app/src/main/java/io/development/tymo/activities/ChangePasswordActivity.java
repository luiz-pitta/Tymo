package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.development.tymo.LoginForgotActivity;
import io.development.tymo.R;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateFields;
import static io.development.tymo.utils.Validation.validatePasswordSize;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, updatingButton, cancelButton, forgot;
    private LinearLayout progressBox;
    private EditText passwordNew, passwordNewAgain;
    private EditText passwordActual;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account_password);

        mSubscriptions = new CompositeDisposable();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        forgot = (TextView) findViewById(R.id.forgot);
        passwordActual = (EditText) findViewById(R.id.passwordActual);
        passwordNew = (EditText) findViewById(R.id.passwordNew);
        passwordNewAgain = (EditText) findViewById(R.id.passwordNewAgain);
        updatingButton = (TextView) findViewById(R.id.updatingButton);
        cancelButton = (TextView) findViewById(R.id.cancelButton);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);

        mBackButton.setOnClickListener(this);
        updatingButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        forgot.setOnClickListener(this);

        m_title.setText(getResources().getString(R.string.password_reset_text_1));
        progressBox.setVisibility(View.GONE);


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void updatePassword(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().changePassword(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        Toast.makeText(this, getResources().getString(R.string.password_reset_success), Toast.LENGTH_LONG).show();
        progressBox.setVisibility(View.GONE);
        passwordActual.setText(null);
        passwordNew.setText(null);
        passwordNewAgain.setText(null);
    }

    private void handleError(Throwable error) {
        //progressBox.setVisibility(View.GONE);
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
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if(view == updatingButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "updatingButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            User user =  new User();

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");

            int err = 0;

            if (!validateFields(passwordActual.getText().toString())) {
                err++;
                passwordActual.setError(getResources().getString(R.string.validation_field_invalid_required_field));
                Toast.makeText(this, getResources().getString(R.string.validation_field_required_password), Toast.LENGTH_LONG).show();
            }
            else if (!validateFields(passwordNew.getText().toString())) {
                err++;
                passwordNew.setError(getResources().getString(R.string.validation_field_invalid_required_field));
                Toast.makeText(this, getResources().getString(R.string.validation_field_required_password), Toast.LENGTH_LONG).show();
            }
            else if (!validateFields(passwordNewAgain.getText().toString())) {
                err++;
                passwordNewAgain.setError(getResources().getString(R.string.validation_field_invalid_required_field));
                Toast.makeText(this, getResources().getString(R.string.validation_field_required_password), Toast.LENGTH_LONG).show();
            }
            else if (!validatePasswordSize(passwordNew.getText().toString())) {
                err++;
                passwordNew.setError(getResources().getString(R.string.validation_field_password_minimum));
                Toast.makeText(this, getResources().getString(R.string.validation_field_password_minimum), Toast.LENGTH_LONG).show();
            }
            else if(!passwordNew.getText().toString().matches(passwordNewAgain.getText().toString())) {
                err++;
                passwordNewAgain.setError(getResources().getString(R.string.validation_field_passwords_diff));
                Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.validation_field_password_new_wrong), Toast.LENGTH_LONG).show();
            }

            if(err == 0){
                progressBox.setVisibility(View.VISIBLE);
                user.setEmail(email);
                user.setNewPassword(passwordNew.getText().toString());
                user.setPassword(passwordActual.getText().toString());
                updatePassword(user);
            }

        }else if(view == cancelButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancelButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            finish();
        }
        else if(view == forgot) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "forgot" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(ChangePasswordActivity.this, LoginForgotActivity.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }
}
