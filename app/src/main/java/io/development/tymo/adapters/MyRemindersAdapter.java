package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import io.development.tymo.models.FriendRequestModel;
import io.development.tymo.models.MyRemindersModel;
import io.development.tymo.view_holder.FriendRequestViewHolder;
import io.development.tymo.view_holder.MyRemindersHolder;


public class MyRemindersAdapter extends RecyclerArrayAdapter<MyRemindersModel> {

    Context context;

    public MyRemindersAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyRemindersHolder(parent, context);
    }

}
