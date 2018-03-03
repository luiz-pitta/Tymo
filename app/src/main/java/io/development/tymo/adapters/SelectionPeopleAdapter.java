package io.development.tymo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.view_holder.FooterViewHolder;
import io.development.tymo.view_holder.SelectionDialogViewHolder;
import io.development.tymo.view_holder.SelectionTagViewHolder;


public class SelectionPeopleAdapter extends MultiChoiceAdapter<RecyclerView.ViewHolder> {

    private List<User> personList;
    private final Context context;

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public SelectionPeopleAdapter(List<User> personList, Context context) {
        personList = setOrderContacts(personList);
        this.personList = personList;
        this.context = context;
    }

    private List<User> setOrderContacts(List<User> users) {

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                String name1 = c1.getName();
                String name2 = c2.getName();

                if (name1.compareTo(name2) > 0)
                    return 1;
                else if (name1.compareTo(name2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        return users;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM)
            return new SelectionDialogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_profile, parent, false));
        else
            return new FooterViewHolder(LayoutInflater.from (parent.getContext ()).inflate (R.layout.footer_list_items_selects, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(holder instanceof SelectionDialogViewHolder) {
            User person = personList.get(position);
            SelectionDialogViewHolder  selectionDialogViewHolder = (SelectionDialogViewHolder)holder;
            selectionDialogViewHolder.text1.setText(person.getName());

            if (!person.getPhoto().matches("")) {
                Glide.clear(selectionDialogViewHolder.profilePhoto);
                Glide.with(context)
                        .load(person.getPhoto())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new BitmapImageViewTarget(selectionDialogViewHolder.profilePhoto) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                selectionDialogViewHolder.profilePhoto.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else
                selectionDialogViewHolder.profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);
        }


    }


    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    public void setActive(View view, boolean state) {

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

    private boolean isPositionFooter (int position) {
        return position == personList.size ();
    }

    @Override
    public int getItemViewType (int position) {
        if(isPositionFooter (position))
            return TYPE_FOOTER;

        return TYPE_ITEM;
    }

    private void clearData() {
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
        return personList.size() + 1;
    }

}
