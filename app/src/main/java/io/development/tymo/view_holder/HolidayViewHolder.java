package io.development.tymo.view_holder;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.rebound.SpringSystem;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import io.development.tymo.R;
import io.development.tymo.models.cards.HolidayCard;


public class HolidayViewHolder extends BaseViewHolder<HolidayCard> {
    public ImageView icon;
    private Context mContext;
    private Rect rect;

    public HolidayViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card_special);
        icon = $(R.id.icon);
        this.mContext = context;

        new Actor.Builder(SpringSystem.create(), $(R.id.card))
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
    public void setData(HolidayCard card){
        icon.setImageResource(R.drawable.ic_holiday);
    }
}
