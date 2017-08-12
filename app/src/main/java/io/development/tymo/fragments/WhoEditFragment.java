package io.development.tymo.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.AddActivity;
import io.development.tymo.activities.SelectPeopleActivity;
import io.development.tymo.activities.ShowGuestsActivity;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.RecyclerItemClickListener;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhoEditFragment extends Fragment implements View.OnClickListener {

    private TextView guestsNumber, addGuestText;
    private View addGuestButtonDivider;
    private TextView feedVisibility;
    private ImageView addGuestIcon;
    private RelativeLayout addPersonButton;
    private int invite = 0;
    private View progressLoadingBox, whoLinearBox;
    private RecyclerView recyclerView;
    private MaterialSpinner spinner;
    private Rect rect;

    private View profilesPhotos;
    private LinearLayout guestBox;
    private boolean isEdit = false;
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;

    private ArrayList<User> data = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private ArrayList<User> listToInvite = new ArrayList<>();
    private PersonAdapter adapter;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance(String text) {
        WhoEditFragment fragment = new WhoEditFragment();
        return fragment;
    }

    public WhoEditFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_act_who_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubscriptions = new CompositeDisposable();

        guestsNumber = (TextView) view.findViewById(R.id.guestsNumber);
        feedVisibility = (TextView) view.findViewById(R.id.feedVisibility);
        recyclerView = (RecyclerView) view.findViewById(R.id.guestRow);
        profilesPhotos = view.findViewById(R.id.profilesPhotos);
        guestBox = (LinearLayout) view.findViewById(R.id.guestBox);
        addPersonButton = (RelativeLayout) view.findViewById(R.id.addGuestButton);
        progressLoadingBox = view.findViewById(R.id.progressLoadingBox);
        whoLinearBox = view.findViewById(R.id.whoLinearBox);
        addGuestIcon = (ImageView) view.findViewById(R.id.addGuestIcon);
        addGuestText = (TextView) view.findViewById(R.id.addGuestText);
        addGuestButtonDivider = (View) view.findViewById(R.id.addGuestButtonDivider);

        addGuestText.setText(getString(R.string.invite_guest_btn));

        addPersonButton.setOnClickListener(this);

        new Actor.Builder(SpringSystem.create(), addPersonButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        feedVisibility.setText(R.string.feed_visibility_1);


        spinner = (MaterialSpinner) view.findViewById(R.id.visibilityCalendarPicker);
        spinner.setItems(getActivity().getResources().getStringArray(R.array.array_who_can_invite));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                invite = position;
                if (invite == 2){
                    feedVisibility.setText(R.string.feed_visibility_2);
                }
                else{
                    feedVisibility.setText(R.string.feed_visibility_1);
                }
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "visibilityCalendarPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        addPersonButton.setActivated(false);

        AddActivity addActivity = (AddActivity)getActivity();

        if(addActivity.getActivity() == null || addActivity.getUserList() == null || (addActivity.getUserList() != null && addActivity.getUserList().size() == 0)) {
            getUser(email);
            setProgress(true);
        }
        else {
            setLayout(addActivity.getActivity(), addActivity.getUserList(), addActivity.getConfirmedList(), addActivity.getEditable());
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, MotionEvent e) {
                    SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    String email = mSharedPreferences.getString(Constants.EMAIL, "");
                    AddActivity addActivity = (AddActivity) getActivity();

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
                    intent.putExtra("guest_list_user", new ListUserWrapper(data));
                    intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                    intent.putExtra("is_adm", addActivity.checkIfAdm(addActivity.getAdmList(), email));
                    intent.putExtra("id_act", addActivity.getActivity().getId());
                    startActivityForResult(intent, GUEST_UPDATE);
                }

                @Override
                public void onLongItemClick(View view, int position, MotionEvent e) {
                }
            }));
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void getUser(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfile(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(User user) {
        AddActivity addActivity = (AddActivity)getActivity();
        if(!isEdit) {
            data = new ArrayList<>();
            user.setDelete(false);
            data.add(user);
            User friend = addActivity.getUserFriend();
            if(friend != null) {
                friend.setDelete(false);
                data.add(friend);
            }
            adapter = new PersonAdapter(data, getActivity());
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(data.size()));
            addPersonButton.setActivated(true);
        }

        setProgress(false);
    }

    private void handleError(Throwable error) {
        //AddActivity addActivity = (AddActivity)getActivity();
        //addActivity.setProgress(false);
        Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    public void setProgress(boolean progress){
        if(progress) {
            whoLinearBox.setVisibility(View.INVISIBLE);
            progressLoadingBox.setVisibility(View.VISIBLE);
        }
        else {
            whoLinearBox.setVisibility(View.VISIBLE);
            progressLoadingBox.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v == addPersonButton && addPersonButton.isActivated()){
            Intent intent = new Intent(getActivity(), SelectPeopleActivity.class);
            ArrayList<String> list = new ArrayList<>();

            for (int i = 0; i < data.size(); i++) {
                list.add(data.get(i).getEmail());
            }

            intent.putStringArrayListExtra("guest_list", list);
            if(isEdit)
                intent.putExtra("erase_from_list", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, ADD_GUEST);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ArrayList<User> list = new ArrayList<>();
        if(resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                AddActivity addActivity = (AddActivity) getActivity();
                ActivityServer activityServer = new ActivityServer();

                SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                activityServer.setId(0);
                activityServer.setCreator(email);

                addActivity.setActivityGuestInformation(addActivity.getActivity().getId(), activityServer);
            }else if (requestCode == ADD_GUEST) {
                if(!isEdit) {
                    PersonModelWrapper wrap =
                            (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                    list.add(data.get(0));
                    list.addAll(wrap.getItemDetails());
                    if (list.size() > 1) {
                        for(int i = 0; i<list.size();i++){
                            User usr = list.get(i);
                            usr.setDelete(false);
                        }
                    }
                    adapter.swap(list);
                    guestsNumber.setText(String.valueOf(list.size()));
                }else {
                    AddActivity addActivity = (AddActivity) getActivity();
                    ActivityServer activityServer = new ActivityServer();
                    SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    PersonModelWrapper wrap =
                            (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                    listToInvite.clear();
                    listToInvite.addAll(wrap.getItemDetails());
                    if(listToInvite.size() > 0) {
                        activityServer.setId(addActivity.getActivity().getId());
                        activityServer.setVisibility(Constants.ACT);
                        activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                        for (int i = 0; i < listToInvite.size(); i++)
                            activityServer.addGuest(listToInvite.get(i).getEmail());

                        addActivity.addGuestToActivity(activityServer);
                    }
                }
            }
        }
    }

    public int getPrivacyFromView() {
        return invite;
    }

    public List<User> getGuestFromView() {
        return data;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setLayout(ActivityServer activityServer, ArrayList<User> users, ArrayList<User> confirmed, boolean edit){
        if(recyclerView!=null) {
            invite = activityServer.getInvitationType();

            spinner.setSelectedIndex(invite);

            if (invite == 2) {
                feedVisibility.setText(R.string.feed_visibility_2);
            } else {
                feedVisibility.setText(R.string.feed_visibility_1);
            }

            isEdit = edit;

            data.clear();
            listConfirmed.clear();
            listConfirmed.addAll(confirmed);

            for (int i = 0; i < users.size(); i++) {
                User usr = users.get(i);
                usr.setDelete(false);
                data.add(usr);
            }

            adapter = new PersonAdapter(data, getActivity());
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(data.size()));
            addPersonButton.setActivated(true);

            if (!isActivityInPast(activityServer)) {
                addGuestIcon.setImageResource(R.drawable.btn_add_person);
                addPersonButton.setOnClickListener(this);
            } else {
                addPersonButton.setOnClickListener(null);
                addPersonButton.setVisibility(View.GONE);
                addGuestButtonDivider.setVisibility(View.GONE);
            }
        }
    }

    private boolean isActivityInPast(ActivityServer activityServer){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -7);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        boolean isHourBefore = isTimeInBefore(hour + ":" + minute, activityServer.getHourEnd() + ":" + activityServer.getMinuteEnd());
        boolean isDateBefore = isDateInBefore(activityServer.getYearEnd(), activityServer.getMonthEnd(), activityServer.getDayEnd(), year, month, day);

        return (isHourBefore && isDateBefore) || isDateBefore;
    }

    private boolean isDateInBefore(int year, int monthOfYear, int dayOfMonth,int yearEnd, int monthOfYearEnd, int dayOfMonthEnd){
        if(yearEnd < year)
            return false;
        if(year == yearEnd){
            if(monthOfYearEnd < monthOfYear)
                return false;
            else if(monthOfYearEnd == monthOfYear){
                if(dayOfMonthEnd <= dayOfMonth)
                    return false;
            }
        }

        return true;
    }

    private boolean isTimeInBefore(String now, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(time);

            return date1.after(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }
}
