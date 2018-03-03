package io.development.tymo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import io.development.tymo.R;
import io.development.tymo.model_server.IconServer;

public class CustomizeAddActivityAdapter extends RecyclerView.Adapter<CustomizeAddActivityAdapter.ViewHolder> {
    private ArrayList<IconServer> icons;
    private Context context;

    public CustomizeAddActivityAdapter(Context context, ArrayList<IconServer> icons) {
        this.icons = icons;
        this.context = context;
    }

    @Override
    public CustomizeAddActivityAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_icon, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomizeAddActivityAdapter.ViewHolder viewHolder, int i) {
        IconServer icon = icons.get(i);

        Glide.clear(viewHolder.pieceIcon);
        Glide.with(context)
                .load(icon.getUrl())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.pieceIcon);
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    public IconServer getItem(int position) {
        return icons.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView pieceIcon;

        public ViewHolder(View view) {
            super(view);
            pieceIcon = (ImageView)view.findViewById(R.id.pieceIcon);
        }
    }

}