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
    private MyRemindersHolder.RefreshLayoutPlansCallback callback;

    public MyRemindersAdapter(Context context, MyRemindersHolder.RefreshLayoutPlansCallback callback) {
        super(context);
        this.context = context;
        this.callback = callback;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyRemindersHolder(parent, context, callback);
    }

}
