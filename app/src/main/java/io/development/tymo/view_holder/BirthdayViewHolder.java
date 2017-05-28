package io.development.tymo.view_holder;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import io.development.tymo.R;
import io.development.tymo.models.cards.BirthdayCard;


public class BirthdayViewHolder extends BaseViewHolder<BirthdayCard> {
    public ImageView icon;

    private Context mContext;

    public BirthdayViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card_special);
        icon = $(R.id.icon);
        this.mContext = context;
    }

    @Override
    public void setData(BirthdayCard card){
        icon.setImageResource(R.drawable.ic_balloons);
    }
}
