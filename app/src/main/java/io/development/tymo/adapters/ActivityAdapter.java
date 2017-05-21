package io.development.tymo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.security.InvalidParameterException;

import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.view_holder.CubeViewHolder;
import io.development.tymo.view_holder.FeedFlagViewHolder;


public class ActivityAdapter extends RecyclerArrayAdapter<Object> {

    public static final int TYPE_INVALID = 0;
    public static final int ACT = 1;
    public static final int FLAG = 2;
    private Context context;

    public ActivityAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getViewType(int position) {
        if(getItem(position) instanceof ActivityServer){
            return ACT;
        }else if (getItem(position) instanceof FlagServer){
            return FLAG;
        }
        return TYPE_INVALID;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case ACT:
                return new CubeViewHolder(parent,this, context);
            case FLAG:
                return new FeedFlagViewHolder(parent, context);
            default:
                throw new InvalidParameterException();
        }
    }

    @Override
    public void onViewAttachedToWindow(BaseViewHolder holder) {
        if (holder instanceof CubeViewHolder)
            ((CubeViewHolder) holder).setAnimation();
        else if(holder instanceof FeedFlagViewHolder)
            ((FeedFlagViewHolder) holder).setAnimation();

    }

}
