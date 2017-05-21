package io.development.tymo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.view_holder.SelectionDialogViewHolder;


public class SelectionPeopleAdapter extends MultiChoiceAdapter<SelectionDialogViewHolder> {

    private List<User> personList;
    private final Context context;

    public SelectionPeopleAdapter(List<User> personList, Context context) {
        this.personList = personList;
        this.context = context;
    }

    @Override
    public SelectionDialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectionDialogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_profile, parent, false));
    }


    @Override
    public void onBindViewHolder(SelectionDialogViewHolder holder, int position) {
        User person = personList.get(position);
        super.onBindViewHolder(holder, position);
        holder.text1.setText(person.getName());

        if(!person.getPhoto().matches("")) {
            Glide.clear(holder.profilePhoto);
            Glide.with(context)
                    .load(person.getPhoto())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(holder.profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            holder.profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
        else
            holder.profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);


    }


    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    protected void setActive(View view, boolean state, int position) {

        ImageView checkBoxActivated  = (ImageView) view.findViewById(R.id.checkBoxActivated);
        RelativeLayout peopleBox  = (RelativeLayout) view.findViewById(R.id.peopleBox);

        if(checkBoxActivated != null){
            if(state){
                checkBoxActivated.setVisibility(View.VISIBLE);
                peopleBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
            }else{
                checkBoxActivated.setVisibility(View.GONE);
                peopleBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        }
    }

    @Override
    protected View.OnClickListener defaultItemViewClickListener(SelectionDialogViewHolder holder, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
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
