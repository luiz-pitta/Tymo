package io.development.tymo.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.development.tymo.R;

public class SelectionWeekDaysViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.day)
    public TextView day;

    public SelectionWeekDaysViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }
}