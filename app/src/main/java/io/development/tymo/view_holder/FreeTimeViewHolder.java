package io.development.tymo.view_holder;

import android.content.Context;
import android.graphics.Rect;
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
import io.development.tymo.models.cards.FreeTime;

public class FreeTimeViewHolder extends BaseViewHolder<FreeTime> {
    public ImageView icon;
    public LinearLayout box;
    public TextView time;
    public TextView text;
    public LinearLayout iconBox;
    private Context mContext;
    private Rect rect;
    private Actor actor;

    public FreeTimeViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card);

        time = $(R.id.timeBox);
        box = $(R.id.cardBox);
        icon = $(R.id.icon);
        text = $(R.id.infoBox);
        iconBox = $(R.id.iconBox);
        mContext = context;

        actor = new Actor.Builder(SpringSystem.create(), $(R.id.card))
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                })
                .build();


        iconBox.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }

    @Override
    public void setData(FreeTime card){
        time.setText(card.getTime());
        time.setTextColor(ContextCompat.getColor(mContext,R.color.grey_600));
        time.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transparent));
        box.setBackgroundResource(R.drawable.bg_card_free_time);
        icon.setImageResource(0);
    }
}