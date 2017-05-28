package io.development.tymo.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.development.tymo.R;

public class SelectionInterestViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text1)
    public TextView text1;

    @BindView(R.id.tagBox)
    public RelativeLayout tagBox;

    public SelectionInterestViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }
}