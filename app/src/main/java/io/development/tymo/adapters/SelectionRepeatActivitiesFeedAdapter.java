package io.development.tymo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.view_holder.SelectionRepeatActivitiesFeedViewHolder;

public class SelectionRepeatActivitiesFeedAdapter extends MultiChoiceAdapter<RecyclerView.ViewHolder> {

    private ArrayList<Object> ActivitiesList;
    private Context context;

    public SelectionRepeatActivitiesFeedAdapter(ArrayList<Object> ActivitiesList, Context context) {
        this.ActivitiesList = ActivitiesList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectionRepeatActivitiesFeedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_repeat_activity, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SelectionRepeatActivitiesFeedViewHolder selectionRepeatActivitiesFeedViewHolder = (SelectionRepeatActivitiesFeedViewHolder)holder;
        Object activity = ActivitiesList.get(position);
        String dateHourText = "";
        int year_start = 0, month_start = 0, day_start = 0, minute_start = 0, hour_start = 0;
        int year_end = 0, month_end = 0, day_end = 0, minute_end = 0, hour_end = 0;
        if(activity instanceof ActivityServer){
            ActivityServer activityServer = (ActivityServer)activity;

            year_start = activityServer.getYearStart();
            month_start = activityServer.getMonthStart();
            day_start = activityServer.getDayStart();
            minute_start = activityServer.getMinuteStart();
            hour_start = activityServer.getHourStart();

            year_end = activityServer.getYearEnd();
            month_end = activityServer.getMonthEnd();
            day_end = activityServer.getDayEnd();
            minute_end = activityServer.getMinuteEnd();
            hour_end = activityServer.getHourEnd();
        }else if(activity instanceof FlagServer){
            FlagServer flagServer = (FlagServer)activity;

            year_start = flagServer.getYearStart();
            month_start = flagServer.getMonthStart();
            day_start = flagServer.getDayStart();
            minute_start = flagServer.getMinuteStart();
            hour_start = flagServer.getHourStart();

            year_end = flagServer.getYearEnd();
            month_end = flagServer.getMonthEnd();
            day_end = flagServer.getDayEnd();
            minute_end = flagServer.getMinuteEnd();
            hour_end = flagServer.getHourEnd();
        }

        DateFormat dateFormat = new DateFormat(context);
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.set(year_start, month_start - 1, day_start);
        calendar2.set(year_end, month_end - 1, day_end);

        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
        String dayStart = String.format("%02d", day_start);
        String monthStart = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        int yearStart = year_start;
        String hourStart = String.format("%02d", hour_start);
        String minuteStart = String.format("%02d", minute_start);
        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
        String dayEnd = String.format("%02d", day_end);
        String monthEnd = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
        int yearEnd = year_end;
        String hourEnd = String.format("%02d", hour_end);
        String minuteEnd = String.format("%02d", minute_end);

        if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
            if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                dateHourText = context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);
            } else {
                dateHourText = context.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd);
            }
        } else {
            dateHourText = context.getResources().getString(R.string.date_format_06, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd);
        }

        selectionRepeatActivitiesFeedViewHolder.text1.setText(dateHourText);

    }

    @Override
    public void setActive(@NonNull View view, boolean state) {

        ImageView checkBoxActivated  = view.findViewById(R.id.checkBoxActivated);
        RelativeLayout repeatBox  = view.findViewById(R.id.repeatBox);

        if(checkBoxActivated != null){
            if(state){
                checkBoxActivated.setVisibility(View.VISIBLE);
                repeatBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
            }else{
                checkBoxActivated.setVisibility(View.GONE);
                repeatBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        }
    }

    private void clearData() {
        int size = ActivitiesList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++)
                ActivitiesList.remove(0);
            notifyItemRangeRemoved(0, size);
        }
    }

    public void swap(ArrayList<ActivityServer> newActivitiesList){
        clearData();
        if(newActivitiesList.size() > 0) {
            ActivitiesList.addAll(newActivitiesList);
            notifyItemRangeInserted(0,newActivitiesList.size());
        }
    }

    @Override
    public int getItemCount() {
        return ActivitiesList.size();
    }

}
