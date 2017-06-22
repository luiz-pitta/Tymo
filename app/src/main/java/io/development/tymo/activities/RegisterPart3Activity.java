package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.adapters.SelectionInterestAdapter;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterPart3Activity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, m_title2, advanceButton;
    private LinearLayout progressBox;
    private RelativeLayout addTagBox;

    private TagView tagGroup;
    ArrayList<String> interestList = new ArrayList<>();

    private int privacy;
    private UserWrapper wrap;
    private CompositeDisposable mSubscriptions;

    private Cloudinary cloudinary;
    private InputStream inputStream;
    private UploadCloudinary uploadCloudinary;
    private SharedPreferences mSharedPreferences;

    private Boolean register_with_facebook;
    private FirebaseAnalytics mFirebaseAnalytics;

    private OnTagDeleteListener mOnTagDeleteListener = new OnTagDeleteListener() {

        @Override
        public void onTagDeleted(final TagView view, final Tag tag, final int position) {
            view.remove(position);
            interestList.remove(position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_part_3);

        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);

        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));
        uploadCloudinary = new UploadCloudinary();

        tagGroup = (TagView) findViewById(R.id.tagGroup);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        m_title2 = (TextView) findViewById(R.id.text2);
        advanceButton = (TextView) findViewById(R.id.advanceButton);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);
        addTagBox = (RelativeLayout) findViewById(R.id.addTagBox);

        addTagBox.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        advanceButton.setOnClickListener(this);

        tagGroup.setOnTagDeleteListener(mOnTagDeleteListener);

        m_title.setText(getResources().getString(R.string.register));

        register_with_facebook = true;

        register_with_facebook = getIntent().getBooleanExtra("register_with_facebook", true);

        if(register_with_facebook) {
            m_title2.setText(getResources().getString(R.string.register_steps, 2, 2));
        }
        else{
            m_title2.setText(getResources().getString(R.string.register_steps, 3, 3));
        }

        mSubscriptions = new CompositeDisposable();

        wrap = (UserWrapper) getIntent().getSerializableExtra("user_wrapper");
        privacy = getIntent().getIntExtra("user_privacy",0);

        inputStream = TymoApplication.getInstance().getInputStreamer();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

    }

    private void createDialogMessage(int listSize) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        button1.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        if (listSize == 0){
            text2.setText(getResources().getString(R.string.validation_field_my_interests_selected_none));
        }
        else if (listSize == 1){
            text2.setText(getResources().getString(R.string.validation_field_my_interests_selected_one));
        }
        else{
            text2.setText(getResources().getString(R.string.validation_field_my_interests_selected, listSize));
        }

        text1.setText(R.string.validation_field_my_interests_minimum);
        buttonText2.setText(R.string.try_again);

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        dg.show();
    }

    private void registerProcess(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().register(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResister,this::handleError));
    }

    private void handleResister(Response response) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        User user = wrap.getUser();

        editor.putString(Constants.EMAIL,user.getEmail());
        editor.putBoolean(Constants.LOGIN_TYPE, user.getFromFacebook());
        editor.putString(Constants.USER_NAME, user.getName());
        editor.putBoolean(Constants.LOCATION, user.isLocationGps());
        editor.putBoolean(Constants.NOTIFICATION_ACT, user.isNotificationActivity());
        editor.putBoolean(Constants.NOTIFICATION_FLAG, user.isNotificationFlag());
        editor.putBoolean(Constants.NOTIFICATION_REMINDER, user.isNotificationReminder());
        editor.putBoolean(Constants.NOTIFICATION_PUSH, user.isNotificationPush());

        editor.apply();

        Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        progressBox.setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {
        if (error instanceof retrofit2.HttpException) {
            Gson gson = new GsonBuilder().create();
            try {

                String errorBody = ((retrofit2.HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                progressBox.setVisibility(View.GONE);
                Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                List<String> list = intent.getStringArrayListExtra("tags_objs");
                interestList.clear();
                interestList.addAll(list);

                tagGroup.removeAll();

                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String c1, String c2) {
                        if (c1.compareTo(c2) > 0)
                            return 1;
                        else if (c1.compareTo(c2) < 0)
                            return -1;
                        else
                            return 0;
                    }
                });

                for (int i=0;i<list.size();i++){
                    Tag tag;
                    tag = new Tag(list.get(i));
                    tag.radius = Utilities.convertDpToPixel(10.0f, this);
                    tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
                    tag.isDeletable = true;
                    tagGroup.addTag(tag);
                }

            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == advanceButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "advanceButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            progressBox.setVisibility(View.VISIBLE);
            User user = wrap.getUser();

            user.setPrivacy(privacy);
            if(interestList.size() < 5) {
                progressBox.setVisibility(View.GONE);
                createDialogMessage(interestList.size());
            }
            else {
                user.addAllInterest(interestList);
                if(inputStream != null)
                    uploadCloudinary.execute(user);
                else
                    registerProcess(user);
            }
        }else if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        }else if(view == addTagBox){

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addTagBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int i;
            ArrayList<String> list = new ArrayList<>();
            List<Tag> list_tags = tagGroup.getTags();
            for(i = 0; i < list_tags.size(); i++){
                list.add(list_tags.get(i).text);
            }
            Intent intent = new Intent(this, SelectInterestActivity.class);
            intent.putStringArrayListExtra("tags_list", list);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_left_exit_back, R.anim.push_left_enter_back);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

}
