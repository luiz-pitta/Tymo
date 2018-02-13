package io.development.tymo.view_holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.ReminderActivity;
import io.development.tymo.adapters.PlansAdapter;
import io.development.tymo.adapters.PlansCardsAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.Birthday;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.Holiday;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.models.WeekModel;
import io.development.tymo.models.cards.ActivityCard;
import io.development.tymo.models.cards.BirthdayCard;
import io.development.tymo.models.cards.Flag;
import io.development.tymo.models.cards.FreeTime;
import io.development.tymo.models.cards.HolidayCard;
import io.development.tymo.models.cards.Reminder;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;


public class PlansViewHolder extends BaseViewHolder<WeekModel> {
    private Context context;
    private int screen;
    private TextView dayNumber, dayText, dayMonth;
    private EasyRecyclerView mRecyclerView;
    private PlansCardsAdapter adapter;
    private CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback;
    private LinearLayout dayBox;
    private FlagServer flagServer;
    private ReminderServer reminderServer;
    private String email;

    private FirebaseAnalytics mFirebaseAnalytics;
    private User friend;
    private boolean free;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Object obj = adapter.getItem(0);
            Activity activity = (Activity) context;
            FreeTime freeTime = (FreeTime) obj;
            PlansAdapter plansAdapter = getOwnerAdapter();
            WeekModel weekModel = plansAdapter.getItem(getAdapterPosition());
            DateTymo dateTymo = new DateTymo();

            dateTymo.setDay(weekModel.getDay());
            dateTymo.setMonth(weekModel.getMonth());
            dateTymo.setYear(weekModel.getYear());

            dateTymo.setHour(Integer.valueOf(freeTime.getTime().substring(0, 2)));
            dateTymo.setMinute(Integer.valueOf(freeTime.getTime().substring(3, 5)));
            dateTymo.setHourEnd(Integer.valueOf(freeTime.getTime().substring(6, 8)));
            dateTymo.setMinuteEnd(Integer.valueOf(freeTime.getTime().substring(9, 11)));

            Calendar now = Calendar.getInstance();
            Calendar day_card = Calendar.getInstance();
            day_card.set(weekModel.getYear(), weekModel.getMonth() - 1, weekModel.getDay());

            boolean show = !isInThePast(weekModel.getYear(), weekModel.getMonth(), weekModel.getDay(),
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

            CreatePopUpDialogFragment createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                    CreatePopUpDialogFragment.Type.CUSTOM, dateTymo,
                    screen, friend);

            if (show) {
                createPopUpDialogFragment.setCallback(callback);
                createPopUpDialogFragment.show(activity.getFragmentManager(), "custom");
            } else {
                createDialogMessage(weekModel.getYear(), weekModel.getMonth(), weekModel.getDay(),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
            }
        }
    };

    public PlansViewHolder(ViewGroup parent, Context context, int screen, CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback, User usr, boolean free) {
        super(parent, R.layout.list_item_plans);

        dayNumber = $(R.id.dayNumber);
        dayText = $(R.id.dayText);
        dayMonth = $(R.id.dayMonth);
        dayBox = $(R.id.dayBox);
        mRecyclerView = $(R.id.dayCardBox);

        this.context = context;
        this.screen = screen;
        this.callback = callback;
        this.friend = usr;
        this.free = free;

        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        $(R.id.profilePhotoBox).setVisibility(View.GONE);

        mRecyclerView.setItemAnimator(new LandingAnimator());
        mRecyclerView.setProgressView(R.layout.progress_loading_list);

        if (free) {
            mRecyclerView.setEmptyView(R.layout.empty_free_time);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        adapter = new PlansCardsAdapter(context);

        mRecyclerView.setAdapterWithProgress(adapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, mRecyclerView.getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {
                Object obj = adapter.getItem(position);
                boolean show;

                Activity activity = (Activity) context;
                CreatePopUpDialogFragment createPopUpDialogFragment;

                String type = obj.toString().substring(33, obj.toString().length() - 8);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "CreatePopUpDialogFragment" + type + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (!free) {
                    if (obj instanceof Reminder) {
                        reminderServer = ((Reminder) obj).getReminderServer();

                        Intent myIntent = new Intent(context, ReminderActivity.class);
                        myIntent.putExtra("type_reminder", 1);
                        myIntent.putExtra("reminder_show", new ReminderWrapper(reminderServer));
                        context.startActivity(myIntent);

                        bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderOpen" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    } else if (obj instanceof Flag && ((Flag) obj).getFlagServer().getType()) {
                        flagServer = ((Flag) obj).getFlagServer();

                        Intent myIntent = new Intent(context, FlagActivity.class);
                        myIntent.putExtra("type_flag", 1);
                        myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                        context.startActivity(myIntent);

                        bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagOpen" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    } else if (obj instanceof Flag && !((Flag) obj).getFlagServer().getType() && email.equals(((Flag) obj).getFlagServer().getCreator())) {
                        flagServer = ((Flag) obj).getFlagServer();

                        Intent myIntent = new Intent(context, FlagActivity.class);
                        myIntent.putExtra("type_flag", 1);
                        myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                        context.startActivity(myIntent);

                        bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagOpen" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    } else {
                        createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                                CreatePopUpDialogFragment.Type.CUSTOM, obj, screen, null);

                        createPopUpDialogFragment.setCallback(callback);
                        createPopUpDialogFragment.show(activity.getFragmentManager(), "custom");

                    }
                } else {
                    FreeTime freeTime = (FreeTime) obj;
                    PlansAdapter plansAdapter = getOwnerAdapter();
                    WeekModel weekModel = plansAdapter.getItem(getAdapterPosition());
                    DateTymo dateTymo = new DateTymo();

                    dateTymo.setDay(weekModel.getDay());
                    dateTymo.setMonth(weekModel.getMonth());
                    dateTymo.setYear(weekModel.getYear());

                    dateTymo.setHour(Integer.valueOf(freeTime.getTime().substring(0, 2)));
                    dateTymo.setMinute(Integer.valueOf(freeTime.getTime().substring(3, 5)));
                    dateTymo.setHourEnd(Integer.valueOf(freeTime.getTime().substring(6, 8)));
                    dateTymo.setMinuteEnd(Integer.valueOf(freeTime.getTime().substring(9, 11)));

                    Calendar now = Calendar.getInstance();
                    Calendar day_card = Calendar.getInstance();
                    day_card.set(weekModel.getYear(), weekModel.getMonth() - 1, weekModel.getDay());

                    show = !isInThePast(weekModel.getYear(), weekModel.getMonth(), weekModel.getDay(),
                            now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

                    createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                            CreatePopUpDialogFragment.Type.CUSTOM, dateTymo,
                            screen, friend);

                    if (show) {
                        createPopUpDialogFragment.setCallback(callback);
                        createPopUpDialogFragment.show(activity.getFragmentManager(), "custom");
                    } else {
                        createDialogMessage(weekModel.getYear(), weekModel.getMonth(), weekModel.getDay(),
                                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
                    }
                }

            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {

            }
        }));
    }

    private boolean isInThePast(int y1, int m1, int d1, int y2, int m2, int d2) {
        LocalDate end = new LocalDate(y1, m1, d1);
        LocalDate start = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        return timePeriod.getDays() < 0;
    }

    private void createDialogMessage(int y1, int m1, int d1, int y2, int m2, int d2) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        button1.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        Dialog dg = new Dialog(context, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        String date = String.format("%02d", d1) + "/" + String.format("%02d", m1) + "/" + String.valueOf(y1);
        String dateNow = String.format("%02d", d2) + "/" + String.format("%02d", m2) + "/" + String.valueOf(y2);

        text1.setText(R.string.free_time_past_dialog_text_1);
        text2.setText(context.getString(R.string.free_time_past_dialog_text_2, date, dateNow));
        buttonText2.setText(R.string.try_again);

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

    @Override
    public void setData(WeekModel week) {
        dayNumber.setText(week.getM_day_number());
        dayText.setText(week.getM_day_text());
        dayMonth.setText(week.getM_month_text());
        adapter.clear();

        Calendar before3Months = Calendar.getInstance();
        before3Months.add(Calendar.MONTH, -3);

        boolean isStored = !isInThePast(week.getYear(), week.getMonth(), week.getDay(),
                before3Months.get(Calendar.YEAR), before3Months.get(Calendar.MONTH) + 1, before3Months.get(Calendar.DAY_OF_MONTH));

        if (!free) {
            if (isStored) {
                adapter.addAll(setPlansItemData(week.getActivities(), week.getPaint()));
                mRecyclerView.setEmptyView(R.layout.empty_commitments);

                if (week.getPaint())
                    dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
                else
                    dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            } else {
                dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_50));
                mRecyclerView.setEmptyView(R.layout.empty_commitments_past);
                mRecyclerView.showEmpty();
            }
        } else {
            if (isStored) {
                boolean freeAllDay = false;
                if (week.getFree().size() == 1)
                    freeAllDay = isFreeAllDay((FreeTimeServer) week.getFree().get(0));

                if (!freeAllDay) {
                    mRecyclerView.setEmptyView(R.layout.empty_free_time);
                    adapter.addAll(setPlansItemData(week.getFree(), false));
                    mRecyclerView.getEmptyView().setOnClickListener(null);
                } else {
                    adapter.addAll(setPlansItemData(week.getFree(), false));
                    mRecyclerView.setEmptyView(R.layout.empty_free_all_day);
                    mRecyclerView.getEmptyView().setOnClickListener(onClickListener);
                    new Actor.Builder(SpringSystem.create(), mRecyclerView.getEmptyView())
                            .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                            .onTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return true;
                                }
                            })
                            .build();
                    mRecyclerView.showEmpty();
                }

                if (week.getPaint())
                    dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
                else
                    dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            } else {
                dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_50));
                mRecyclerView.setEmptyView(R.layout.empty_commitments_past);
                mRecyclerView.showEmpty();
            }
        }
    }

    public void setBefore3Months() {
        mRecyclerView.setEmptyView(R.layout.empty_commitments_past);
        mRecyclerView.showEmpty();
    }

    private boolean isFreeAllDay(FreeTimeServer freeTimeServer) {
        int minute_start = freeTimeServer.getMinuteStart();
        int hour_start = freeTimeServer.getHourStart();
        int minute_end = freeTimeServer.getMinuteEnd();
        int hour_end = freeTimeServer.getHourEnd();
        return minute_start == 0 && minute_end == 59
                && hour_start == 0 && hour_end == 23;
    }

    private List<Object> setPlansItemData(List<Object> objectList, boolean paint) {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < objectList.size(); i++) {
            Object object = objectList.get(i);
            if (object instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) object;
                String hour_start = String.format("%02d", activityServer.getHourCard());
                String minute_start = String.format("%02d", activityServer.getMinuteCard());
                String hour_end = String.format("%02d", activityServer.getHourEndCard());
                String minute_end = String.format("%02d", activityServer.getMinuteEndCard());
                String time;

                if ((activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty()) || (activityServer.getTimeStartEmptyCard() && activityServer.getTimeEndEmptyCard())) {
                    time = context.getResources().getString(R.string.suspension_points);
                }
                else if (!activityServer.getTimeStartEmptyCard() && activityServer.getTimeEndEmptyCard()){
                    if (activityServer.getTimeStartEmpty())
                        time = context.getResources().getString(R.string.suspension_points);
                    else
                        time = hour_start + ":" + minute_start + "\n" + context.getResources().getString(R.string.suspension_points);
                }
                else if (activityServer.getTimeStartEmptyCard() && !activityServer.getTimeEndEmptyCard()){
                    if (activityServer.getTimeEndEmpty())
                        time = context.getResources().getString(R.string.suspension_points);
                    else
                        time = context.getResources().getString(R.string.suspension_points) + "\n" + hour_end + ":" + minute_end;
                }
                else{
                    if (activityServer.getTimeEndEmpty())
                        time = hour_start + ":" + minute_start + "\n" + context.getResources().getString(R.string.suspension_points);
                    else if (activityServer.getTimeStartEmpty())
                        time = context.getResources().getString(R.string.suspension_points) + "\n" + hour_end + ":" + minute_end;
                    else
                        time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;
                }

                if ((screen == Utilities.TYPE_FRIEND && activityServer.getParticipates() == 0) &&
                        ((activityServer.getKnowCreator() == 0 && activityServer.getVisibility() > 0) ||
                                (activityServer.getVisibility() == 2 && activityServer.getKnowCreator() > 0))) {

                    FlagServer flagServer = new FlagServer();
                    flagServer.setType(false);
                    flagServer.setTitle(context.getResources().getString(R.string.flag_unavailable));
                    flagServer.setDayStart(activityServer.getDayStart());
                    flagServer.setMonthStart(activityServer.getMonthStart());
                    flagServer.setYearStart(activityServer.getYearStart());
                    flagServer.setDayEnd(activityServer.getDayEnd());
                    flagServer.setMonthEnd(activityServer.getMonthEnd());
                    flagServer.setYearEnd(activityServer.getYearEnd());
                    flagServer.setMinuteStart(activityServer.getMinuteStart());
                    flagServer.setHourStart(activityServer.getHourStart());
                    flagServer.setMinuteEnd(activityServer.getMinuteEnd());
                    flagServer.setHourEnd(activityServer.getHourEnd());
                    list.add(new Flag(time, R.drawable.ic_flag, false, flagServer, paint, true));
                } else
                    list.add(new ActivityCard(time, activityServer.getCubeIcon(), activityServer.getCubeColor(), activityServer.getCubeColorUpper(), activityServer, false));

            } else if (object instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) object;
                String hour_start = String.format("%02d", flagServer.getHourCard());
                String minute_start = String.format("%02d", flagServer.getMinuteCard());
                String hour_end = String.format("%02d", flagServer.getHourEndCard());
                String minute_end = String.format("%02d", flagServer.getMinuteEndCard());
                String time;

                if ((flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) || (flagServer.getTimeStartEmptyCard() && flagServer.getTimeEndEmptyCard())) {
                    time = context.getResources().getString(R.string.suspension_points);
                }
                else if (!flagServer.getTimeStartEmptyCard() && flagServer.getTimeEndEmptyCard()){
                    if (flagServer.getTimeStartEmpty())
                        time = context.getResources().getString(R.string.suspension_points);
                    else
                        time = hour_start + ":" + minute_start + "\n" + context.getResources().getString(R.string.suspension_points);
                }
                else if (flagServer.getTimeStartEmptyCard() && !flagServer.getTimeEndEmptyCard()){
                    if (flagServer.getTimeEndEmpty())
                        time = context.getResources().getString(R.string.suspension_points);
                    else
                        time = context.getResources().getString(R.string.suspension_points) + "\n" + hour_end + ":" + minute_end;
                }
                else{
                    if (flagServer.getTimeEndEmpty())
                        time = hour_start + ":" + minute_start + "\n" + context.getResources().getString(R.string.suspension_points);
                    else if (flagServer.getTimeStartEmpty())
                        time = context.getResources().getString(R.string.suspension_points) + "\n" + hour_end + ":" + minute_end;
                    else
                        time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;
                }

                if (screen == Utilities.TYPE_FRIEND && flagServer.getType() && flagServer.getParticipates() == 0) {
                    FlagServer flag = new FlagServer();
                    flag.setType(false);
                    flag.setTitle(context.getResources().getString(R.string.flag_menu_unavailable));
                    flag.setDayStart(flagServer.getDayStart());
                    flag.setMonthStart(flagServer.getMonthStart());
                    flag.setYearStart(flagServer.getYearStart());
                    flag.setDayEnd(flagServer.getDayEnd());
                    flag.setMonthEnd(flagServer.getMonthEnd());
                    flag.setYearEnd(flagServer.getYearEnd());
                    flag.setMinuteStart(flagServer.getMinuteStart());
                    flag.setHourStart(flagServer.getHourStart());
                    flag.setMinuteEnd(flagServer.getMinuteEnd());
                    flag.setHourEnd(flagServer.getHourEnd());
                    list.add(new Flag(time, R.drawable.ic_flag, false, flag, paint, true));
                } else
                    list.add(new Flag(time, R.drawable.ic_flag, flagServer.getType(), flagServer, paint, false));

            } else if (object instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) object;
                String hour_start = String.format("%02d", reminderServer.getHourStart());
                String minute_start = String.format("%02d", reminderServer.getMinuteStart());
                String hour_end = String.format("%02d", reminderServer.getHourEnd());
                String minute_end = String.format("%02d", reminderServer.getMinuteEnd());
                String time;

                if ((reminderServer.getTimeStartEmpty() && reminderServer.getTimeEndEmpty()) || (reminderServer.getTimeStartEmptyCard() && reminderServer.getTimeEndEmptyCard())) {
                    time = context.getResources().getString(R.string.suspension_points);
                }
                else if (!reminderServer.getTimeStartEmptyCard() && reminderServer.getTimeEndEmptyCard()){
                    if (reminderServer.getTimeStartEmpty())
                        time = context.getResources().getString(R.string.suspension_points);
                    else
                        time = hour_start + ":" + minute_start + "\n" + context.getResources().getString(R.string.suspension_points);
                }
                else if (reminderServer.getTimeStartEmptyCard() && !reminderServer.getTimeEndEmptyCard()){
                    if (reminderServer.getTimeEndEmpty())
                        time = context.getResources().getString(R.string.suspension_points);
                    else
                        time = context.getResources().getString(R.string.suspension_points) + "\n" + hour_end + ":" + minute_end;
                }
                else{
                    if (reminderServer.getTimeEndEmpty())
                        time = hour_start + ":" + minute_start + "\n" + context.getResources().getString(R.string.suspension_points);
                    else if (reminderServer.getTimeStartEmpty())
                        time = context.getResources().getString(R.string.suspension_points) + "\n" + hour_end + ":" + minute_end;
                    else
                        time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;
                }

                list.add(new Reminder(reminderServer.getTitle(), time, reminderServer));
            } else if (object instanceof FreeTimeServer) {
                PlansAdapter plansAdapter = getOwnerAdapter();
                WeekModel weekModel = plansAdapter.getItem(getAdapterPosition());

                Calendar now = Calendar.getInstance();

                boolean inPast = isInThePast(weekModel.getYear(), weekModel.getMonth(), weekModel.getDay(),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

                FreeTimeServer freeTimeServer = (FreeTimeServer) object;
                String hour_start = String.format("%02d", freeTimeServer.getHourStart());
                String minute_start = String.format("%02d", freeTimeServer.getMinuteStart());
                String hour_end = String.format("%02d", freeTimeServer.getHourEnd());
                String minute_end = String.format("%02d", freeTimeServer.getMinuteEnd());
                String time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;

                list.add(new FreeTime(time, inPast));
            } else if (object instanceof Holiday) {
                list.add(new HolidayCard((Holiday) object));
            } else if (object instanceof Birthday) {
                list.add(new BirthdayCard((Birthday) object));
            }
        }

        return list;
    }


}
