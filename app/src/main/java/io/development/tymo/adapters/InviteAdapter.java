package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import io.development.tymo.models.InviteModel;
import io.development.tymo.view_holder.InviteViewHolder;


public class InviteAdapter extends RecyclerArrayAdapter<InviteModel> {

    Context context;

    public InviteAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new InviteViewHolder(parent, context);
    }

}
