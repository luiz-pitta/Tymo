package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import io.development.tymo.models.InviteModel;
import io.development.tymo.models.NotificationModel;
import io.development.tymo.view_holder.InviteViewHolder;
import io.development.tymo.view_holder.NotificationViewHolder;


public class NotificationAdapter extends RecyclerArrayAdapter<NotificationModel> {

    Context context;

    public NotificationAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationViewHolder(parent, context);
    }

}
