package io.development.tymo.view_holder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import io.development.tymo.R;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.models.search.ActivitySearch;


public class ActivitySearchViewHolder extends BaseViewHolder<ActivitySearch> implements View.OnClickListener{
    private ImageView upper;
    private ImageView center;
    private ImageView icon;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private Context mContext;
    private RelativeLayout itemBox;
    private ActivityServer activityServer;

    private FirebaseAnalytics mFirebaseAnalytics;



    public ActivitySearchViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.list_item_search);
        upper = $(R.id.cubeUpperBoxIcon);
        center = $(R.id.cubeLowerBoxIcon);
        icon = $(R.id.pieceIcon);
        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        itemBox = $(R.id.ItemBox);
        this.mContext = context;

        text4.setVisibility(View.GONE);
        $(R.id.actionIcon).setVisibility(View.GONE);
        $(R.id.moreVerticalIcon).setVisibility(View.GONE);
        $(R.id.itemIcon).setVisibility(View.INVISIBLE);
        $(R.id.profilePhotoBox).setVisibility(View.INVISIBLE);

        itemBox.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void setData(final ActivitySearch item){

        Glide.clear(icon);
        Glide.with(mContext)
                .load(item.getCubeIcon())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(icon);

        upper.setColorFilter(item.getCubeTop());
        center.setColorFilter(item.getCubeCenter());
        text1.setText(item.getText1());
        text2.setText(item.getText2());
        text3.setText(item.getText3());
        activityServer = item.getActivityServer();
    }

    @Override
    public void onClick(View view) {
        if(view == itemBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBox"+ getClass().getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(mContext, ShowActivity.class);
            myIntent.putExtra("act_show", new ActivityWrapper(activityServer));
            mContext.startActivity(myIntent);
        }
    }
}
