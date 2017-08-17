package io.development.tymo.view_holder;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.rebound.SpringSystem;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import io.development.tymo.R;
import io.development.tymo.models.cards.Reminder;


public class ReminderViewHolder extends BaseViewHolder<Reminder> {
    public TextView text;
    public LinearLayout cardBox;
    public TextView time;
    public LinearLayout box;
    public Context mContext;
    private Rect rect;

    public ReminderViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card);
        time = $(R.id.timeBox);
        text = $(R.id.infoBox);
        box = $(R.id.iconBox);
        cardBox = $(R.id.cardBox);
        mContext = context;

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

        box.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }

    @Override
    public void setData(Reminder card){
        time.setText(card.getTime());
        text.setText(card.getText());
        time.setTextColor(ContextCompat.getColor(mContext,R.color.black));
        time.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transparent));
        cardBox.setBackgroundResource(R.drawable.ic_reminder);
    }
}
