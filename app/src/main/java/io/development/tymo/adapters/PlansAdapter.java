package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.models.WeekModel;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.view_holder.CubeViewHolder;
import io.development.tymo.view_holder.FeedFlagViewHolder;
import io.development.tymo.view_holder.PlansViewHolder;


public class PlansAdapter extends RecyclerArrayAdapter<WeekModel> {

    private Context context;
    private int screen;
    private CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback;
    private User friend;
    private boolean free;

    public PlansAdapter(Context context, int screen, CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback, User usr, boolean free) {
        super(context);
        this.context = context;
        this.screen = screen;
        this.callback = callback;
        this.friend = usr;
        this.free = free;
    }

    private boolean isInThePast(int y1, int m1, int d1, int y2, int m2, int d2) {
        LocalDate end = new LocalDate(y1, m1, d1);
        LocalDate start = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        return timePeriod.getDays() < 0;
    }

    @Override
    public void onViewAttachedToWindow(BaseViewHolder holder) {
        WeekModel week = getAllData().get(holder.getAdapterPosition());
        Calendar before12Months = Calendar.getInstance();
        before12Months.add(Calendar.MONTH, -12);

        boolean isPast3Months = isInThePast(week.getYear(), week.getMonth(), week.getDay(),
                before12Months.get(Calendar.YEAR), before12Months.get(Calendar.MONTH) + 1, before12Months.get(Calendar.DAY_OF_MONTH));

        if(isPast3Months){
            PlansViewHolder viewHolder = (PlansViewHolder)holder;
            viewHolder.setBefore3Months();
        }
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlansViewHolder(parent, context, screen, callback, friend, free);
    }

}
