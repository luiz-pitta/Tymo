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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.rebound.SpringSystem;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import io.development.tymo.R;
import io.development.tymo.models.cards.ActivityCard;


public class ActivityViewHolder extends BaseViewHolder<ActivityCard> {

    private Context mContext;
    public ImageView icon;
    public TextView time;
    public LinearLayout box, cardBox;
    private Rect rect;


    public ActivityViewHolder(ViewGroup parent, Context context) {
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
