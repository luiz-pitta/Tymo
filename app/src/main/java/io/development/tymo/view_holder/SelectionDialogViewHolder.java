
package io.development.tymo.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;
import io.development.tymo.R;

public class SelectionDialogViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text1)
    public TextView text1;

    @BindView(R.id.profilePhoto)
    public ImageView profilePhoto;

    @BindView(R.id.peopleBox)
    public RelativeLayout peopleBox;

    public SelectionDialogViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }
}