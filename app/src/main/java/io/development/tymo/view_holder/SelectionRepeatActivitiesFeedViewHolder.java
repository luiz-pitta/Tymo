package io.development.tymo.view_holder;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.development.tymo.R;

public class SelectionRepeatActivitiesFeedViewHolder extends RecyclerView.ViewHolder {

    private boolean state = false;

    @BindView(R.id.text1)
    public TextView text1;

    @BindView(R.id.repeatBox)
    public RelativeLayout repeatBox;

    public SelectionRepeatActivitiesFeedViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        repeatBox.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
    }
}