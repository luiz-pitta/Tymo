package io.development.tymo.view_holder;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.activities.AddActivity;
import io.development.tymo.adapters.PlansAdapter;
import io.development.tymo.adapters.PlansCardsAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.Birthday;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.Holiday;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.User;
import io.development.tymo.models.WeekModel;
import io.development.tymo.models.cards.ActivityCard;
import io.development.tymo.models.cards.BirthdayCard;
import io.development.tymo.models.cards.Flag;
import io.development.tymo.models.cards.FreeTime;
import io.development.tymo.models.cards.HolidayCard;
import io.development.tymo.models.cards.Reminder;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static android.content.Context.MODE_PRIVATE;


public class PlansViewHolder extends BaseViewHolder<WeekModel> {
    private Context context;
    private int screen;
    private TextView dayNumber, dayText, dayMonth;
    private EasyRecyclerView mRecyclerView;
    private PlansCardsAdapter adapter;
    private CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback;
    private LinearLayout dayBox;

    private FirebaseAnalytics mFirebaseAnalytics;
    private User friend;
    private boolean free;


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

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        $(R.id.profilePhotoBox).setVisibility(View.GONE);

        mRecyclerView.setItemAnimator(new LandingAnimator());
        mRecyclerView.setProgressView(R.layout.progress_loading_list);
        if(free)
            mRecyclerView.setEmptyView(R.layout.empty_free_time);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        adapter = new PlansCardsAdapter(context);

        mRecyclerView.setAdapterWithProgress(adapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, mRecyclerView.getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {
                Object obj = adapter.getItem(position);
                boolean show = true;

                Activity activity = (Activity) context;
                CreatePopUpDialogFragment createPopUpDialogFragment;

                String type =  obj.toString().substring(33,obj.toString().length()-8);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "CreatePopUpDialogFragment" + type + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (!free)
                    createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                            CreatePopUpDialogFragment.Type.CUSTOM, obj, screen, null);
                else {
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
                    day_card.set(weekModel.getYear(),weekModel.getMonth()-1, weekModel.getDay());

                    show = isOlderThan7Days(weekModel.getYear(),weekModel.getMonth(), weekModel.getDay(),
                            now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));

                    createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                                CreatePopUpDialogFragment.Type.CUSTOM, dateTymo,
                                screen, friend);
                }

                if(show) {
                    createPopUpDialogFragment.setCallback(callback);
                    createPopUpDialogFragment.show(activity.getFragmentManager(), "custom");
                }

            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {

            }
        }));
    }

    private boolean isOlderThan7Days(int y1, int m1, int d1, int y2, int m2, int d2) {
        LocalDate start = new LocalDate(y1, m1, d1);
        LocalDate end = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        return timePeriod.getDays() <= 7;
    }


    @Override
    public void setData(WeekModel week){
        dayNumber.setText(week.getM_day_number());
        dayText.setText(week.getM_day_text());
        dayMonth.setText(week.getM_month_text());
        adapter.clear();

        if(!free)
            adapter.addAll(setPlansItemData(week.getActivities(), week.getPaint()));
        else
            adapter.addAll(setPlansItemData(week.getFree(), false));

        if(week.getPaint())
            dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
        else
            dayBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
    }

    private List<Object> setPlansItemData(List<Object> objectList, boolean paint) {
        List<Object> list = new ArrayList<>();

        for(int i = 0; i < objectList.size(); i++){
            Object object = objectList.get(i);
            if(object instanceof ActivityServer){
                ActivityServer activityServer = (ActivityServer)object;
                String hour_start = String.format("%02d", activityServer.getHourCard());
                String minute_start = String.format("%02d", activityServer.getMinuteCard());
                String hour_end = String.format("%02d", activityServer.getHourEndCard());
                String minute_end = String.format("%02d", activityServer.getMinuteEndCard());
                String time;

                if(activityServer.getHourCard() == activityServer.getHourEndCard() && activityServer.getMinuteCard() == activityServer.getMinuteEndCard())
                    time = hour_start+":"+minute_start+"\n"+"-";
                else
                    time = hour_start+":"+minute_start+"\n"+hour_end+":"+minute_end;

                if( (screen == Utilities.TYPE_FRIEND && activityServer.getParticipates() == 0) &&
                    ( (activityServer.getKnowCreator() == 0 && activityServer.getVisibility() > 0) ||
                      (activityServer.getVisibility() == 2 && activityServer.getKnowCreator() > 0) ) ){

                    FlagServer flagServer = new FlagServer();
                    flagServer.setType(false);
                    flagServer.setTitle(context.getResources().getString(R.string.flag_unavailable_title_dialog));
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
                }else
                    list.add(new ActivityCard(time, activityServer.getCubeIcon(), activityServer.getCubeColor(), activityServer.getCubeColorUpper(), activityServer, false));

            }else if(object instanceof FlagServer){
                FlagServer flagServer = (FlagServer) object;
                String hour_start = String.format("%02d", flagServer.getHourCard());
                String minute_start = String.format("%02d", flagServer.getMinuteCard());
                String hour_end = String.format("%02d", flagServer.getHourEndCard());
                String minute_end = String.format("%02d", flagServer.getMinuteEndCard());
                String time;

                if(flagServer.getHourCard() == flagServer.getHourEndCard() && flagServer.getMinuteCard() == flagServer.getMinuteEndCard())
                    time = hour_start+":"+minute_start+"\n"+"-";
                else
                    time = hour_start+":"+minute_start+"\n"+hour_end+":"+minute_end;


                if(screen == Utilities.TYPE_FRIEND && flagServer.getType() && flagServer.getParticipates() == 0){
                    FlagServer flag = new FlagServer();
                    flag.setType(false);
                    flag.setTitle(context.getResources().getString(R.string.flag_unavailable_title));
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
                }else
                    list.add(new Flag(time,R.drawable.ic_flag, flagServer.getType(), flagServer, paint, false));

            }else if(object instanceof ReminderServer){
                ReminderServer reminderServer = (ReminderServer) object;
                String hour_start = String.format("%02d", reminderServer.getHourStart());
                String minute_start = String.format("%02d", reminderServer.getMinuteStart());
                String time;

                time = hour_start+":"+minute_start;

                list.add(new Reminder(reminderServer.getTitle(),time, reminderServer));
            }else if(object instanceof FreeTimeServer){
                FreeTimeServer freeTimeServer = (FreeTimeServer) object;
                String hour_start = String.format("%02d", freeTimeServer.getHourStart());
                String minute_start = String.format("%02d", freeTimeServer.getMinuteStart());
                String hour_end = String.format("%02d", freeTimeServer.getHourEnd());
                String minute_end = String.format("%02d", freeTimeServer.getMinuteEnd());
                String time = hour_start+":"+minute_start+"\n"+hour_end+":"+minute_end;

                list.add(new FreeTime(time));
            }else if(object instanceof Holiday){
                list.add(new HolidayCard((Holiday)object));
            }else if(object instanceof Birthday){
                list.add(new BirthdayCard((Birthday)object));
            }
        }

        return list;
    }


}
