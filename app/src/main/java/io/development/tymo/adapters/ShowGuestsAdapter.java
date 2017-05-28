package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import io.development.tymo.model_server.User;
import io.development.tymo.view_holder.ShowGuestViewHolder;


public class ShowGuestsAdapter extends RecyclerArrayAdapter<User> {
    private Context context;
    private long idAct;
    private boolean isAdm, isFlag;

    public ShowGuestsAdapter(Context context, long id, boolean adm, boolean flag) {
        super(context);
        this.context = context;
        this.idAct = id;
        this.isAdm = adm;
        this.isFlag = flag;
    }


    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShowGuestViewHolder(parent, context, idAct, isAdm, isFlag);
    }
}
