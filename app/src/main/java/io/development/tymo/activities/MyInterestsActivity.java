package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.firebase.analytics.FirebaseAnalytics;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyInterestsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, advanceButton;
    private LinearLayout progressBox;
    private RelativeLayout addTagBox;

    private TagView tagGroup;
    private ArrayList<String> interestList = new ArrayList<>();
    private CompositeDisposable mSubscriptions;

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
        setContentView(R.layout.activity_settings_my_interests);

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        tagGroup = (TagView) findViewById(R.id.tagGroup);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        advanceButton = (TextView) findViewById(R.id.advanceButton);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);
        addTagBox = (RelativeLayout) findViewById(R.id.addTagBox);

        addTagBox.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        advanceButton.setOnClickListener(this);

        tagGroup.setOnTagDeleteListener(mOnTagDeleteListener);

        m_title.setText(getResources().getString(R.string.settings_my_interests));

        mSubscriptions = new CompositeDisposable();

        setUpInterests();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

    }

    private void setUpInterests() {
        progressBox.setVisibility(View.VISIBLE);
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        mSubscriptions.add(NetworkUtil.getRetrofit().getInterest(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        int i;
        interestList = new ArrayList<>();
        ArrayList<TagServer> interests_person = response.getTags();
        for(i = 0; i < interests_person.size(); i++)
            interestList.add(interests_person.get(i).getTitle());


        tagGroup.removeAll();

        Collections.sort(interestList, new Comparator<String>() {
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

        for (i=0;i<interestList.size();i++){
            Tag tag;
            tag = new Tag(interestList.get(i));
            tag.radius = Utilities.convertDpToPixel(10.0f, this);
            tag.layoutColor = ContextCompat.getColor(this, R.color.deep_purple_400);
            tag.isDeletable = true;
            tagGroup.addTag(tag);
        }

        progressBox.setVisibility(View.GONE);
    }

    private void updateInterest(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().updateInterest(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleInterest,this::handleError));
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

    private void handleInterest(Response response) {
        progressBox.setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {
        //progressBox.setVisibility(View.GONE);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == advanceButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "advanceButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            progressBox.setVisibility(View.VISIBLE);
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            User user = new User();
            user.setEmail(email);
            user.addAllInterest(interestList);

            if(interestList.size() < 5) {
                progressBox.setVisibility(View.GONE);
                createDialogMessage(interestList.size());
            }
            else {
                updateInterest(user);
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_left_exit_back, R.anim.push_left_enter_back);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

}
