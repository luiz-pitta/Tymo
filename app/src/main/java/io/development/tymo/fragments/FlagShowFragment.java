package io.development.tymo.fragments;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.SelectPeopleActivity;
import io.development.tymo.activities.ShowGuestsActivity;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
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
public class FlagShowFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private TextView guestsNumber, repeatText, dateHourText, addGuestText, guestText, feedVisibility;
    private RelativeLayout addGuestButton;
    private ImageView addGuestIcon;
    private LinearLayout repeatBox, guestBox, whoCanInviteBox, profilesPhotos;
    private View addGuestButtonDivider;
    private Rect rect;
    private FlagServer flagServer;
    private boolean isInPast = false;

    private DateFormat dateFormat;

    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private ArrayList<User> listPerson = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance(String text) {
        FlagShowFragment fragment = new FlagShowFragment();
        return fragment;
    }

    public FlagShowFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateFormat = new DateFormat(getActivity());

        dateHourText = (TextView) view.findViewById(R.id.dateHourText);
        recyclerView = (RecyclerView) view.findViewById(R.id.guestRow);
        addGuestButton = (RelativeLayout) view.findViewById(R.id.addGuestButton);
        guestsNumber = (TextView) view.findViewById(R.id.guestsNumber);
        guestText = (TextView) view.findViewById(R.id.guestText);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);
        guestBox = (LinearLayout) view.findViewById(R.id.guestBox);
        whoCanInviteBox = (LinearLayout) view.findViewById(R.id.whoCanInviteBox);
        feedVisibility = (TextView) view.findViewById(R.id.feedVisibility);
        profilesPhotos = (LinearLayout) view.findViewById(R.id.profilesPhotos);
        repeatText = (TextView) view.findViewById(R.id.repeatText);
        addGuestIcon = (ImageView) view.findViewById(R.id.addGuestIcon);
        addGuestText = (TextView) view.findViewById(R.id.addGuestText);
        addGuestButtonDivider = (View) view.findViewById(R.id.addGuestButtonDivider);

        addGuestText.setText(getString(R.string.signalize_guest_btn));

        guestBox.setOnClickListener(this);
        guestBox.setOnTouchListener(this);

        new Actor.Builder(SpringSystem.create(), addGuestButton)
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {
                FlagActivity flagActivity = (FlagActivity) getActivity();

                Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
                intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
                intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                intent.putExtra("is_adm", false);
                intent.putExtra("id_act", flagActivity.getFlag().getId());
                intent.putExtra("is_flag", true);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                startActivityForResult(intent, GUEST_UPDATE);
            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        }));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

    }

    private void createDialogMessageAddInPast(int y1, int m1, int d1, int h1, int min1, int y2, int m2, int d2, int h2, int min2) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        button1.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        Dialog dg = new Dialog(getActivity(), R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        String date = String.format("%02d", d1) + "/" + String.format("%02d", m1) + "/" + String.valueOf(y1);
        String dateNow = String.format("%02d", d2) + "/" + String.format("%02d", m2) + "/" + String.valueOf(y2);
        String time = String.format("%02d", h1) + ":" + String.format("%02d", min1);
        String timeNow = String.format("%02d", h2) + ":" + String.format("%02d", min2);

        text1.setText(R.string.signalize_past_dialog_text_1);
        text2.setText(getActivity().getString(R.string.signalize_past_dialog_text_2, date + " - " + time, dateNow + " - " + timeNow));
        buttonText2.setText(R.string.close);

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_radius));
                }

                return false;
            }
        });

        dg.show();
    }

    public void setLayout(FlagServer flagServer, ArrayList<User> users, ArrayList<User> confirmed, ArrayList<FlagServer> flagServers, boolean permissionToInvite) {
        this.flagServer = flagServer;
        if (recyclerView != null) {
            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
            calendar2.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", flagServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = flagServer.getYearStart();
            String hourStart = String.format("%02d", flagServer.getHourStart());
            String minuteStart = String.format("%02d", flagServer.getMinuteStart());
            String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
            String dayEnd = String.format("%02d", flagServer.getDayEnd());
            String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
            int yearEnd = flagServer.getYearEnd();
            String hourEnd = String.format("%02d", flagServer.getHourEnd());
            String minuteEnd = String.format("%02d", flagServer.getMinuteEnd());

            if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()){
                dateHourText.setText(this.getResources().getString(R.string.date_format_03, dayOfWeekStart, dayStart, monthStart, yearStart));
            }
            else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()){
                dateHourText.setText(this.getResources().getString(R.string.date_format_14, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
            }
            else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
            }
            else if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty()) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
            }
            else if (!flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) {
                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_16, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
                }
            }
            else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty()) {
                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_15, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                }
            }
            else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty()) {
                if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                }
            }
            else{
                if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                    if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                        dateHourText.setText(this.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                    } else {
                        dateHourText.setText(this.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                    }
                } else {
                    dateHourText.setText(this.getResources().getString(R.string.date_format_06, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                }
            }

            isInPast = isFlagInPast(flagServer);

            if (permissionToInvite) {
                addGuestIcon.setImageResource(R.drawable.btn_add_person);
                addGuestButton.setOnClickListener(this);
            } else {
                addGuestButton.setOnClickListener(null);
                addGuestButton.setVisibility(View.GONE);
                addGuestButtonDivider.setVisibility(View.GONE);
            }

            if (flagServer.getType()) {
                listPerson.clear();
                listConfirmed.clear();
                listConfirmed.addAll(confirmed);

                for (int i = 0; i < users.size(); i++) {
                    User usr = users.get(i);
                    usr.setDelete(false);
                    listPerson.add(usr);
                }

                listPerson = setOrderGuests(listPerson);

                adapter = new PersonAdapter(listPerson, getActivity());
                recyclerView.setAdapter(adapter);
                guestsNumber.setText(String.valueOf(listPerson.size()));
            } else {
                guestBox.setVisibility(View.GONE);
                whoCanInviteBox.setVisibility(View.GONE);
                profilesPhotos.setVisibility(View.GONE);
            }


            if (flagServer.getRepeatType() == 0)
                repeatBox.setVisibility(View.GONE);
            else {
                String repeatly;
                switch (flagServer.getRepeatType()) {
                    case Constants.DAYLY:
                        repeatly = getActivity().getString(R.string.repeat_daily);
                        break;
                    case Constants.WEEKLY:
                        repeatly = getActivity().getString(R.string.repeat_weekly);
                        break;
                    case Constants.MONTHLY:
                        repeatly = getActivity().getString(R.string.repeat_monthly);
                        break;
                    default:
                        repeatly = "";
                        break;
                }

                repeatText.setText(getActivity().getString(R.string.repeat_text, repeatly, getLastActivity(flagServers)));
            }
        }
    }

    private ArrayList<User> setOrderGuests(ArrayList<User> users) {

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                String name1 = c1.getName();
                String name2 = c2.getName();

                if (name1.compareTo(name2) > 0)
                    return 1;
                else if (name1.compareTo(name2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getInvitation();
                long id2 = c2.getInvitation();

                if (id1 == 1)
                    return -1;
                else if (id2 == 1)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getCountKnows();
                long id2 = c2.getCountKnows();

                if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getCountFavorite();
                long id2 = c2.getCountFavorite();

                if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                boolean id1 = c1.isCreator();
                boolean id2 = c2.isCreator();

                if (id1 && !id2)
                    return -1;
                else if (!id1 && id2)
                    return 1;
                else
                    return 0;
            }
        });

        return users;
    }

    private boolean isFlagInPast(FlagServer flagServer) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        boolean isHourBefore = isTimeInBefore(hour + ":" + minute, flagServer.getHourEnd() + ":" + flagServer.getMinuteEnd());
        boolean isDateBefore = isDateInBefore(flagServer.getYearEnd(), flagServer.getMonthEnd(), flagServer.getDayEnd(), year, month, day);

        return (isHourBefore && isDateBefore) || isDateBefore;
    }

    private boolean isDateInBefore(int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        if (yearEnd < year)
            return false;
        if (year == yearEnd) {
            if (monthOfYearEnd < monthOfYear)
                return false;
            else if (monthOfYearEnd == monthOfYear) {
                if (dayOfMonthEnd <= dayOfMonth)
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
        if (v == addGuestButton) {
            int i;
            ArrayList<String> list = new ArrayList<>();
            for (i = 0; i < listPerson.size(); i++) {
                list.add(listPerson.get(i).getEmail());
            }
            Intent intent = new Intent(getActivity(), SelectPeopleActivity.class);
            intent.putStringArrayListExtra("guest_list", list);
            intent.putExtra("erase_from_list", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addGuestButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(isInPast){
                Calendar now = Calendar.getInstance();

                createDialogMessageAddInPast(flagServer.getYearEnd(), flagServer.getMonthEnd(), flagServer.getDayEnd(), flagServer.getHourEnd(), flagServer.getMinuteEnd(),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            }
            else{
                startActivityForResult(intent, ADD_GUEST);
            }
        }
        else if(v == guestBox){
            FlagActivity flagActivity = (FlagActivity) getActivity();

            Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
            intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
            intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
            intent.putExtra("is_adm", false);
            intent.putExtra("id_act", flagActivity.getFlag().getId());
            intent.putExtra("is_flag", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, GUEST_UPDATE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                FlagActivity flagActivity = (FlagActivity) getActivity();
                flagActivity.refreshItems();
            } else if (requestCode == ADD_GUEST) {
                FlagActivity flagActivity = (FlagActivity) getActivity();
                ActivityServer activityServer = new ActivityServer();
                SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                PersonModelWrapper wrap =
                        (PersonModelWrapper) intent.getSerializableExtra("guest_objs");


                if (wrap.getItemDetails().size() > 0) {
                    activityServer.setId(flagActivity.getFlag().getId());
                    activityServer.setVisibility(Constants.FLAG);
                    activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                    for (int i = 0; i < wrap.getItemDetails().size(); i++)
                        activityServer.addGuest(wrap.getItemDetails().get(i).getEmail());

                    flagActivity.addGuestToFlag(activityServer);
                }
            }
        }
    }

    private String getLastActivity(ArrayList<FlagServer> flagServers) {

        Collections.sort(flagServers, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                FlagServer flagServer;
                int day = 0, month = 0, year = 0;
                int day2 = 0, month2 = 0, year2 = 0;

                if (c1 instanceof FlagServer) {
                    flagServer = (FlagServer) c1;
                    day = flagServer.getDayStart();
                    month = flagServer.getMonthStart();
                    year = flagServer.getYearStart();
                }

                if (c2 instanceof FlagServer) {
                    flagServer = (FlagServer) c2;
                    day2 = flagServer.getDayStart();
                    month2 = flagServer.getMonthStart();
                    year2 = flagServer.getYearStart();
                }

                if (year < year2)
                    return -1;
                else if (year > year2)
                    return 1;
                else if (month < month2)
                    return -1;
                else if (month > month2)
                    return 1;
                else if (day < day2)
                    return -1;
                else if (day > day2)
                    return 1;
                else
                    return 0;

            }
        });

        FlagServer flagServer = flagServers.get(flagServers.size() - 1);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(cal.get(Calendar.DAY_OF_WEEK), cal);
        String dayEnd = String.format("%02d", flagServer.getDayEnd());
        String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(cal.getTime().getTime());
        int yearEnd = flagServer.getYearEnd();

        String date = this.getResources().getString(R.string.date_format_03, dayOfWeekEnd.toLowerCase(), dayEnd, monthEnd, yearEnd);

        return date;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == guestBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                guestText.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
                guestsNumber.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.box_qty_guests));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                guestText.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_200));
                guestsNumber.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.box_qty_guests_pressed));
            }
        }

        return false;
    }
}
