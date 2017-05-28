package io.development.tymo.view_holder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.models.search.FlagSearch;


public class FlagSearchViewHolder extends BaseViewHolder<FlagSearch> implements View.OnClickListener{
    private ImageView icon;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private Context mContext;
    private RelativeLayout itemBox;
    private FlagServer flagServer;

    private FirebaseAnalytics mFirebaseAnalytics;

    public FlagSearchViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.list_item_search);
        icon = $(R.id.itemIcon);
        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        itemBox = $(R.id.ItemBox);
        this.mContext = context;

        text4.setVisibility(View.GONE);
        $(R.id.actionIcon).setVisibility(View.GONE);
        $(R.id.moreVerticalIcon).setVisibility(View.GONE);
        $(R.id.pieceBox).setVisibility(View.GONE);
        $(R.id.profilePhotoBox).setVisibility(View.INVISIBLE);

        itemBox.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void setData(final FlagSearch item){

        if(item.getAvailable()) {
            icon.setImageResource(R.drawable.ic_flag_available);
            text1.setTextColor(mContext.getResources().getColor(R.color.flag_available));
        }
        else {
            icon.setImageResource(R.drawable.ic_flag_unavailable);
            text1.setTextColor(mContext.getResources().getColor(R.color.flag_unavailable));
        }

        text1.setText(item.getText1());
        text2.setText(item.getText2());
        text3.setText(item.getText3());
        flagServer = item.getFlagServer();
    }

    @Override
    public void onClick(View view) {
        if(view == itemBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(mContext, FlagActivity.class);
            myIntent.putExtra("type_flag", 1);
            myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
            mContext.startActivity(myIntent);
        }
    }
}
