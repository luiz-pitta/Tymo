package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.davidecirillo.multichoicerecyclerview.MultiChoiceRecyclerView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.adapters.SelectionInterestAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, advanceButton;
    private LinearLayout progressBox;

    private MultiChoiceRecyclerView mMultiChoiceRecyclerView;
    private List<String> interestList;
    private SelectionInterestAdapter selectionInterestAdapter;
    private CompositeSubscription mSubscriptions;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_preferences);

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        advanceButton = (TextView) findViewById(R.id.advanceButton);
        progressBox = (LinearLayout) findViewById(R.id.progressBox);

        mBackButton.setOnClickListener(this);
        advanceButton.setOnClickListener(this);

        m_title.setText(getResources().getString(R.string.preferences));

        mSubscriptions = new CompositeSubscription();

        mMultiChoiceRecyclerView = (MultiChoiceRecyclerView) findViewById(R.id.recyclerSelectView);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(false);

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);

        setUpMultiChoiceRecyclerView();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);

    }

    private void setUpMultiChoiceRecyclerView() {
        progressBox.setVisibility(View.VISIBLE);
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        mSubscriptions.add(NetworkUtil.getRetrofit().getInterest(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        int i,j;
        interestList = new ArrayList<>();
        ArrayList<TagServer> interests = response.getInterests();
        ArrayList<TagServer> interests_person = response.getTags();
        for(i = 0; i < interests.size(); i++){
            interestList.add(interests.get(i).getTitle());
        }

        mMultiChoiceRecyclerView.setRecyclerColumnNumber(1);

        selectionInterestAdapter = new SelectionInterestAdapter(interestList, this, false) ;
        mMultiChoiceRecyclerView.setAdapter(selectionInterestAdapter);
        mMultiChoiceRecyclerView.setSingleClickMode(true);

        for(i = 0; i < interests.size(); i++){
            String interest = interests.get(i).getTitle();
            for(j = 0; j < interests_person.size(); j++) {
                String my_interest = interests_person.get(j).getTitle();
                if (my_interest.matches(interest))
                    mMultiChoiceRecyclerView.select(i);
            }
        }

        progressBox.setVisibility(View.GONE);
    }

    private void updateInterest(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().updateInterest(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleInterest,this::handleError));
    }

    private void handleInterest(Response response) {
        progressBox.setVisibility(View.GONE);
    }

    private void handleError(Throwable error) {
        //progressBox.setVisibility(View.GONE);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == advanceButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "advanceButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            progressBox.setVisibility(View.VISIBLE);
            Collection<Integer> collection = mMultiChoiceRecyclerView.getSelectedItemList();
            ArrayList<String> list = new ArrayList<>();
            Iterator it = collection.iterator();
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            User user = new User();
            user.setEmail(email);
            while (it.hasNext()) {
                Integer i = (Integer)it.next();
                user.addInterest(interestList.get(i));
                list.add(interestList.get(i));
            }
            if(list.size() < 5) {
                progressBox.setVisibility(View.GONE);
                createDialogMessage(list.size());
            }
            else {
                updateInterest(user);
            }
        }else if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
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
            text2.setText(getResources().getString(R.string.error_preferences_none_selected));
        }
        else if (listSize == 1){
            text2.setText(getResources().getString(R.string.error_preferences_already_selected_one));
        }
        else{
            text2.setText(getResources().getString(R.string.error_preferences_already_selected, listSize));
        }

        text1.setText(R.string.error_preferences_required);
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
        mSubscriptions.unsubscribe();
    }

}
