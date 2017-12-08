package io.development.tymo.view_holder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.models.NotificationModel;
import io.development.tymo.utils.Utilities;


public class NotificationViewHolder extends BaseViewHolder<NotificationModel> {
    private TextView text1,text2,text3, text4;
    private ImageView pieceIcon, cubeLowerBoxIcon, cubeUpperBoxIcon, itemIcon;
    private RelativeLayout pieceBox, itemBox;
    Context context;


    public NotificationViewHolder(ViewGroup parent, final Context context) {
        super(parent, R.layout.list_item_search);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        itemBox = $(R.id.ItemBox);
        pieceIcon = $(R.id.pieceIcon);
        cubeLowerBoxIcon = $(R.id.cubeLowerBoxIcon);
        cubeUpperBoxIcon = $(R.id.cubeUpperBoxIcon);
        itemIcon = $(R.id.itemIcon);
        pieceBox = $(R.id.pieceBox);
        this.context = context;

        itemBox.getLayoutParams().height = (int) Utilities.convertDpToPixel(70, context);

        text2.setVisibility(View.GONE);
        text3.setVisibility(View.GONE);
        $(R.id.actionIcon).setVisibility(View.GONE);
        $(R.id.moreVerticalIcon).setVisibility(View.GONE);
        itemIcon.setVisibility(View.GONE);
        pieceBox.setVisibility(View.GONE);
        $(R.id.profilePhotoBox).setVisibility(View.GONE);
        $(R.id.iconBox).setVisibility(View.GONE);
    }


    @Override
    public void setData(final NotificationModel invite){
        text1.setText(invite.getText1());
        if (invite.getText2().matches("")){
            text2.setVisibility(View.GONE);
        }
        else{
            text2.setText(invite.getText2());
        }
        text4.setText(invite.getText4());

        if (invite.getText4().matches(context.getResources().getString(R.string.commitments_of_the_day_already_happened))){
            text4.setTextColor(context.getResources().getColor(R.color.grey_500));

        }
        else if(invite.getText4().matches(context.getResources().getString(R.string.commitments_of_the_day_is_happening))){
            text4.setTextColor(context.getResources().getColor(R.color.deep_purple_400));
        }
        else{
            text4.setTextColor(context.getResources().getColor(R.color.grey_500));
        }

        if(invite.getActivity() instanceof ActivityServer) {
            Glide.clear(pieceIcon);
            Glide.with(context)
                    .load(invite.getIcon())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(pieceIcon);

            cubeUpperBoxIcon.setColorFilter(invite.getColorUpper());
            cubeLowerBoxIcon.setColorFilter(invite.getColorLower());
            pieceBox.setVisibility(View.VISIBLE);
            itemIcon.setVisibility(View.GONE);
        }
        else if(invite.getActivity() instanceof FlagServer) {
            itemIcon.setVisibility(View.VISIBLE);
            pieceBox.setVisibility(View.GONE);
            if(((FlagServer) invite.getActivity()).getType()){
                itemIcon.setImageResource(R.drawable.ic_flag_available);
                text1.setTextColor(context.getResources().getColor(R.color.flag_available));
            }
            else {
                itemIcon.setImageResource(R.drawable.ic_flag_unavailable);
                text1.setTextColor(context.getResources().getColor(R.color.flag_unavailable));
            }
        }else {
            pieceBox.setVisibility(View.GONE);
            itemIcon.setVisibility(View.VISIBLE);
            itemIcon.setImageResource(R.drawable.ic_reminder);
        }
    }


}
