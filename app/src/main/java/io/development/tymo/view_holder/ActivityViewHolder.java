package io.development.tymo.view_holder;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import io.development.tymo.R;
import io.development.tymo.models.cards.ActivityCard;


public class ActivityViewHolder extends BaseViewHolder<ActivityCard> {

    private Context mContext;
    public ImageView icon;
    public TextView time;
    public LinearLayout box, cardBox;


    public ActivityViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card);
        icon = $(R.id.icon);
        time = $(R.id.timeBox);
        box = $(R.id.iconBox);
        cardBox = $(R.id.cardBox);
        this.mContext = context;
    }

    @Override
    public void setData(ActivityCard card){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cardBox.setBackground(null);
        } else {
            cardBox.setBackgroundDrawable(null);
        }
        box.setBackgroundColor(card.getBackground());
        Glide.clear(icon);
        Glide.with(mContext)
                .load(card.getIcon())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(icon);
        if(!card.getIcon().contains("ic_google"))
            icon.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
        else
            icon.setColorFilter(ContextCompat.getColor(mContext,R.color.transparent));

        time.setText(card.getTime());
        time.setBackgroundColor(card.getBackgroundLight());
    }
}
