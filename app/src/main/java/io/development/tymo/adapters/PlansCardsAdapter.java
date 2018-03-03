package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.security.InvalidParameterException;

import io.development.tymo.models.cards.ActivityCard;
import io.development.tymo.models.cards.BirthdayCard;
import io.development.tymo.models.cards.Flag;
import io.development.tymo.models.cards.FreeTime;
import io.development.tymo.models.cards.HolidayCard;
import io.development.tymo.models.cards.Reminder;
import io.development.tymo.view_holder.ActivityViewHolder;
import io.development.tymo.view_holder.BirthdayViewHolder;
import io.development.tymo.view_holder.FlagViewHolder;
import io.development.tymo.view_holder.FreeTimeViewHolder;
import io.development.tymo.view_holder.HolidayViewHolder;
import io.development.tymo.view_holder.ReminderViewHolder;


public class PlansCardsAdapter extends RecyclerArrayAdapter<Object> {
    public static final int TYPE_INVALID = 0;
    public static final int ACT = 1;
    public static final int FREE = 2;
    public static final int REMIND = 3;
    public static final int BIRTHDAY = 4;
    public static final int FLAG = 5;
    public static final int HOLIDAY = 6;
    private Context context;

    public PlansCardsAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getViewType(int position) {
        if(getItem(position) instanceof ActivityCard){
            return ACT;
        }else if (getItem(position) instanceof FreeTime){
            return FREE;
        }else if (getItem(position) instanceof Reminder){
            return REMIND;
        }else if (getItem(position) instanceof BirthdayCard){
            return BIRTHDAY;
        }else if (getItem(position) instanceof Flag){
            return FLAG;
        }else if (getItem(position) instanceof HolidayCard){
            return HOLIDAY;
        }
        return TYPE_INVALID;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case ACT:
                return new ActivityViewHolder(parent,context);
            case FREE:
                return new FreeTimeViewHolder(parent,context);
            case REMIND:
                return new ReminderViewHolder(parent,context);
            case BIRTHDAY:
                return new BirthdayViewHolder(parent, context);
            case FLAG:
                return new FlagViewHolder(parent, context);
            case HOLIDAY:
                return new HolidayViewHolder(parent, context);
            default:
                throw new InvalidParameterException();
        }
    }
}