package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import io.development.tymo.model_server.User;
import io.development.tymo.models.WeekModel;
import io.development.tymo.utils.CreatePopUpDialogFragment;
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

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlansViewHolder(parent, context, screen, callback, friend, free);
    }

}
