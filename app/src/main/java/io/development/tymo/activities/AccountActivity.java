package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import io.development.tymo.Login1Activity;
import io.development.tymo.R;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserPushNotification;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateEmail;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, email;
    private LinearLayout deleteAccount;
    private RelativeLayout passwordBox, emailBox;

    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);

        mSubscriptions = new CompositeSubscription();

        UserWrapper userWrapper = (UserWrapper)getIntent().getSerializableExtra("user_about");
        user = userWrapper.getUser();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        email = (TextView) findViewById(R.id.email);
        deleteAccount = (LinearLayout) findViewById(R.id.deleteAccount);
        passwordBox = (RelativeLayout) findViewById(R.id.passwordBox);
        emailBox = (RelativeLayout) findViewById(R.id.emailBox);


        mBackButton.setOnClickListener(this);
        passwordBox.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);
        emailBox.setOnClickListener(this);

        m_title.setText(getResources().getString(R.string.account));

        email.setText(user.getEmail());

        if(user.getFromFacebook()) {
            findViewById(R.id.horizontalLine).setVisibility(View.GONE);
            findViewById(R.id.emailActionForwardIcon).setVisibility(View.GONE);
            passwordBox.setVisibility(View.GONE);
            emailBox.setOnClickListener(null);
            passwordBox.setOnClickListener(null);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, getClass().getSimpleName(), null /* class override */);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void deleteUserAccount(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().deleteAccount(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void updateEmail(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().updateEmail(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseEmail,this::handleError));
    }

    private void handleResponseEmail(Response response) {
        findViewById(R.id.include).setVisibility(View.GONE);

        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
        editor.putString(Constants.EMAIL,response.getUser().getEmail());
        editor.apply();

        email.setText(response.getUser().getEmail());
    }

    private void handleResponse(User user) {
        findViewById(R.id.include).setVisibility(View.GONE);

        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
        editor.putString(Constants.EMAIL,"");
        editor.putBoolean(Constants.LOGIN_TYPE, false);
        editor.putString(Constants.USER_NAME, "");
        editor.putBoolean(Constants.LOCATION, false);
        editor.putBoolean(Constants.NOTIFICATION, false);
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), Login1Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void handleError(Throwable error) {
        //findViewById(R.id.include).setVisibility(View.GONE);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if(view == passwordBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "passwordBox");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(AccountActivity.this, ChangePasswordActivity.class));
        }
        else if(view == deleteAccount) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteAccount");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogDeleteAccount().show();
        }
        else if(view == emailBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "emailBox");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogChangeEmail();
        }

    }

    private void createDialogChangeEmail() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setHint(getResources().getString(R.string.email));

        text1.setText(getResources().getString(R.string.dialog_edit_email_title));
        text2.setText(getResources().getString(R.string.dialog_edit_email_text));
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.confirm));


        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                String newEmail = editText.getText().toString();

                if(validateEmail(newEmail)) {

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "newEmail");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    User usr = new User();
                    usr.setEmail(email);
                    usr.setName(newEmail);

                    updateEmail(usr);

                    dialog.dismiss();

                    findViewById(R.id.include).setVisibility(View.VISIBLE);
                }else
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.email_type_again), Toast.LENGTH_LONG).show();
            }
        });

        dialog.show();
    }

    private Dialog createDialogDeleteAccount() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.delete_account));
        text2.setText(getResources().getString(R.string.delete_account_text));
        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteUserAccount");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                deleteUserAccount(email);

                dialog.dismiss();

                findViewById(R.id.include).setVisibility(View.VISIBLE);
            }
        });

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }
}
