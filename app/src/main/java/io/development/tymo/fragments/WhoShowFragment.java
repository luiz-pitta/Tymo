package io.development.tymo.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.development.tymo.R;
import io.development.tymo.activities.SelectPeopleActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.activities.ShowGuestsActivity;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.RecyclerItemClickListener;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhoShowFragment extends Fragment  implements View.OnClickListener {

    private TextView whoCanInvite, guestsNumber;
    private TextView feedVisibility;
    private ImageView addGuestButton;
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;

    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private ArrayList<User> listPerson = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private ArrayList<User> listToInvite = new ArrayList<>();

    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance(String text) {
        WhoShowFragment fragment = new WhoShowFragment();
        return fragment;
    }

    public WhoShowFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_act_who, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        whoCanInvite = (TextView) view.findViewById(R.id.whoCanInvite);

        feedVisibility = (TextView) view.findViewById(R.id.feedVisibility);
        recyclerView = (RecyclerView) view.findViewById(R.id.guestRow);
        addGuestButton = (ImageView) view.findViewById(R.id.addGuestButton);
        guestsNumber = (TextView) view.findViewById(R.id.guestsNumber);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {
                SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                ShowActivity showActivity = (ShowActivity) getActivity();

                Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
                intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
                intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                intent.putExtra("is_adm", showActivity.checkIfAdm(showActivity.getAdmList(), email));
                intent.putExtra("id_act", showActivity.getActivity().getId());

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                startActivityForResult(intent, GUEST_UPDATE);
            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        }));

        ShowActivity showActivity = (ShowActivity)getActivity();
        setLayout(showActivity.getActivity(), showActivity.getUserList(), showActivity.getUserConfirmedList(), showActivity.getPermissionInvite());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                ShowActivity showActivity = (ShowActivity) getActivity();
                showActivity.refreshItems();
            } else if (requestCode == ADD_GUEST) {
                ShowActivity showActivity = (ShowActivity) getActivity();
                ActivityServer activityServer = new ActivityServer();
                SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                PersonModelWrapper wrap =
                        (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                listToInvite.clear();
                listToInvite.addAll(wrap.getItemDetails());
                if(listToInvite.size() > 0) {
                    activityServer.setId(showActivity.getActivity().getId());
                    activityServer.setVisibility(Constants.ACT);
                    activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                    for (int i = 0; i < listToInvite.size(); i++)
                        activityServer.addGuest(listToInvite.get(i).getEmail());

                    showActivity.addGuestToActivity(activityServer);
                }
            }
        }
    }


    public void setLayout(ActivityServer activityServer, ArrayList<User> users, ArrayList<User> confirmed, boolean permissionInvite){

        if(recyclerView!=null) {
            String[] stringArray = getActivity().getResources().getStringArray(R.array.array_who_can_invite);

            whoCanInvite.setText(stringArray[activityServer.getInvitationType()]);

            if (activityServer.getInvitationType() == 2) {
                feedVisibility.setText(R.string.feed_visibility_2);
            } else {
                feedVisibility.setText(R.string.feed_visibility_1);
            }

            listPerson.clear();
            listConfirmed.clear();
            listConfirmed.addAll(confirmed);

            for (int i = 0; i < users.size(); i++) {
                User usr = users.get(i);
                usr.setDelete(false);
                listPerson.add(usr);
            }

            adapter = new PersonAdapter(listPerson, getActivity());
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(listPerson.size()));

            if (permissionInvite && !isActivityInPast(activityServer)) {
                addGuestButton.setImageResource(R.drawable.btn_add_person);
                addGuestButton.setOnClickListener(this);
            } else {
                addGuestButton.setOnClickListener(null);
                addGuestButton.setVisibility(View.GONE);
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
    public void onClick(View v) {
        if(v == addGuestButton){
            int i;
            ArrayList<String> list = new ArrayList<>();
            for(i = 0; i < listPerson.size(); i++){
                list.add(listPerson.get(i).getEmail());
            }
            Intent intent = new Intent(getActivity(), SelectPeopleActivity.class);
            intent.putStringArrayListExtra("guest_list", list);
            intent.putExtra("erase_from_list", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addGuestButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, ADD_GUEST);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
