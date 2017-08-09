package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.evernote.android.job.JobManager;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Calendar;

import io.development.tymo.Login1Activity;
import io.development.tymo.R;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateEmail;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton;
    private TextView m_title, email;
    private LinearLayout deleteAccount;
    private RelativeLayout passwordBox, emailBox;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private Cloudinary cloudinary;
    private UploadCloudinary uploadCloudinary;

    private User user;
    private String oldUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_account);

        mSubscriptions = new CompositeDisposable();
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));
        uploadCloudinary = new UploadCloudinary();

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
        mBackButton.setOnTouchListener(this);

        m_title.setText(getResources().getString(R.string.settings_account));

        email.setText(user.getEmail());

        if(user.getFromFacebook()) {
            findViewById(R.id.horizontalLine).setVisibility(View.GONE);
            findViewById(R.id.emailActionForwardIcon).setVisibility(View.GONE);
            passwordBox.setVisibility(View.GONE);
            emailBox.setOnClickListener(null);
            passwordBox.setOnClickListener(null);
        }else {
            if(!user.getPhoto().matches("")) {
                String[] split = user.getPhoto().split("/");
                oldUrl = split[split.length - 1];
                oldUrl = oldUrl.substring(0, oldUrl.length() - 4);
            }
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
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
        editor.putBoolean(Constants.NOTIFICATION_ACT, false);
        editor.putBoolean(Constants.NOTIFICATION_FLAG, false);
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, false);
        editor.putBoolean(Constants.NOTIFICATION_PUSH, false);
        editor.putBoolean(Constants.INTRO, false);
        editor.apply();

        if(AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();

        JobManager mJobManager = JobManager.instance();
        if(mJobManager.getAllJobRequests().size() > 0)
            mJobManager.cancelAll();

        Intent intent = new Intent(getApplicationContext(), Login1Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void handleError(Throwable error) {
        if (error instanceof retrofit2.HttpException) {
            Gson gson = new GsonBuilder().create();
            try {

                String errorBody = ((retrofit2.HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                findViewById(R.id.include).setVisibility(View.GONE);
                Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        }
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
        else if(view == passwordBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "passwordBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(new Intent(AccountActivity.this, ChangePasswordActivity.class));
        }
        else if(view == deleteAccount) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteAccount" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogDeleteAccount().show();
        }
        else if(view == emailBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "emailBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
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

        editText.setHint(getResources().getString(R.string.register_email));

        text1.setText(getResources().getString(R.string.popup_message_edit_update_email));
        text2.setText(getResources().getString(R.string.popup_message_edit_enter_new_email));
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.confirm));


        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.grey_500));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.grey_300));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_300));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_100));
                }

                return false;
            }
        });

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
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    User usr = new User();
                    usr.setEmail(email);
                    usr.setName(newEmail);
                    usr.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

                    updateEmail(usr);

                    dialog.dismiss();

                    findViewById(R.id.include).setVisibility(View.VISIBLE);
                }else
                    Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_email_validation), Toast.LENGTH_LONG).show();
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

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.grey_500));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.grey_300));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_300));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_100));
                }

                return false;
            }
        });

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
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if(user.getFromFacebook())
                    deleteUserAccount(email);
                else
                    uploadCloudinary.execute(user);


                dialog.dismiss();

                findViewById(R.id.include).setVisibility(View.VISIBLE);
            }
        });

        return dialog;
    }

    private class UploadCloudinary extends AsyncTask<User, Void, User> {

        protected User doInBackground(User... users) {
            try {
                if(oldUrl.length() < 26 && !oldUrl.matches(""))
                    cloudinary.uploader().destroy(oldUrl, ObjectUtils.asMap("invalidate", true));

            } catch (Exception e) {
                Toast.makeText(AccountActivity.this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(User user) {
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String eml = mSharedPreferences.getString(Constants.EMAIL, "");
            deleteUserAccount(eml);
        }
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
