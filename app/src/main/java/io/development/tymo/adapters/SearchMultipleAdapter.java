package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.security.InvalidParameterException;

import io.development.tymo.model_server.User;
import io.development.tymo.models.search.ActivitySearch;
import io.development.tymo.models.search.FlagSearch;
import io.development.tymo.models.search.PersonSearch;
import io.development.tymo.models.search.ReminderSearch;
import io.development.tymo.view_holder.ActivitySearchViewHolder;
import io.development.tymo.view_holder.FlagSearchViewHolder;
import io.development.tymo.view_holder.PersonSearchViewHolder;
import io.development.tymo.view_holder.ReminderSearchViewHolder;


public class SearchMultipleAdapter extends RecyclerArrayAdapter<Object> {
    public static final int TYPE_INVALID = 0;
    public static final int FLAG = 1;
    public static final int REMINDER = 2;
    public static final int ACTIVITY = 3;
    public static final int PERSON = 4;
    private Context context;

    public SearchMultipleAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getViewType(int position) {
        if(getItem(position) instanceof FlagSearch){
            return FLAG;
        }else if (getItem(position) instanceof ReminderSearch){
            return REMINDER;
        }else if (getItem(position) instanceof ActivitySearch){
            return ACTIVITY;
        }else if (getItem(position) instanceof User){
            return PERSON;
        }
        return TYPE_INVALID;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case FLAG:
                return new FlagSearchViewHolder(parent,context);
            case REMINDER:
                return new ReminderSearchViewHolder(parent,context);
            case ACTIVITY:
                return new ActivitySearchViewHolder(parent,context);
            case PERSON:
                return new PersonSearchViewHolder(parent, context);
            default:
                throw new InvalidParameterException();
        }
    }
}
