package io.development.tymo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModel;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.MyViewHolder> {

    private List<User> personList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView personPhoto;
        private TextView personName;
        private ImageView deleteButton, checkButton;

        public MyViewHolder(View view) {
            super(view);
            personPhoto = (ImageView) view.findViewById(R.id.profilePhoto);
            personName = (TextView) view.findViewById(R.id.profileName);
            deleteButton = (ImageView) view.findViewById(R.id.deleteButton);
            checkButton = (ImageView) view.findViewById(R.id.checkButton);

            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    public PersonAdapter(List<User> personList, Context context) {
        this.personList = personList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User person = personList.get(position);

        if(!person.getDelete())
            holder.deleteButton.setVisibility(View.INVISIBLE);
        else
            holder.deleteButton.setVisibility(View.VISIBLE);

        if(person.getInvitation() == 1)
            holder.checkButton.setVisibility(View.VISIBLE);
        else
            holder.checkButton.setVisibility(View.INVISIBLE);

        if(!person.getPhoto().matches("")){
            Glide.clear(holder.personPhoto);
            Glide.with(context)
                    .load(person.getPhoto())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(holder.personPhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            holder.personPhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
        else
            holder.personPhoto.setImageResource(R.drawable.ic_profile_photo_empty);


        holder.personName.setText(person.getName());
    }

    public void clearData() {
        int size = personList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                personList.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    public void swap(List<User> newPersonList){
        clearData();
        if(newPersonList.size() > 0) {
            personList.addAll(newPersonList);
            notifyItemRangeInserted(0,newPersonList.size());
        }
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

}