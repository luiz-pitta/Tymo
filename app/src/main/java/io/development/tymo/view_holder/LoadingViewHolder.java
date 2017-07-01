package io.development.tymo.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.development.tymo.R;

public class LoadingViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.progressBar1)
    public ProgressBar progressBar;

    public LoadingViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }
}