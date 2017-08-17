package io.development.tymo.view_holder;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.rebound.SpringSystem;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import io.development.tymo.R;
import io.development.tymo.models.cards.Flag;


public class FlagViewHolder extends BaseViewHolder<Flag> {
    public ImageView icon;
    public TextView time;
    public LinearLayout box, cardBox;
    private Context mContext;
    private Rect rect;

    public FlagViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card);
        icon = $(R.id.icon);
        time = $(R.id.timeBox);
        box = $(R.id.iconBox);
        cardBox = $(R.id.cardBox);
        this.mContext = context;

        new Actor.Builder(SpringSystem.create(), cardBox)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();
    }

    @Override
    public void setData(Flag card){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cardBox.setBackground(null);
        } else {
            cardBox.setBackgroundDrawable(null);
        }

        int color;
        if(card.getFree()) {
            card.setIcon(R.drawable.ic_feed_flag_available);
            color = R.color.flag_available;
        }
        else {
            card.setIcon(R.drawable.ic_feed_flag_unavailable);
            color = R.color.flag_unavailable;
        }
        icon.setImageResource(card.getIcon());
        time.setText(card.getTime());
        time.setTextColor(ContextCompat.getColor(mContext,color));

        if(card.getPaint()) {
            box.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_100));
            time.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_100));
        }else {
            box.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            time.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }
    }
}