package io.development.tymo.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.util.List;

import io.development.tymo.R;
import io.development.tymo.models.PersonModel;
import io.development.tymo.view_holder.SelectionDialogViewHolder;
import io.development.tymo.view_holder.SelectionTagViewHolder;

/**
 * Created by davidecirillo on 13/03/16.
 */
public class SelectionTagAdapter extends MultiChoiceAdapter<SelectionTagViewHolder> {

    private List<String> tagList;
    private Context context;

    public SelectionTagAdapter(List<String> tagList, Context context) {
        this.tagList = tagList;
        this.context = context;
    }

    @Override
    public SelectionTagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectionTagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_tag, parent, false));
    }


    @Override
    public void onBindViewHolder(SelectionTagViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String tag = tagList.get(position);
        holder.text1.setText(tag);
    }


    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    protected void setActive(View view, boolean state, int position) {

        ImageView checkBoxActivated  = (ImageView) view.findViewById(R.id.checkBoxActivated);
        RelativeLayout tagBox  = (RelativeLayout) view.findViewById(R.id.tagBox);

        if(checkBoxActivated != null){
            if(state){
                checkBoxActivated.setVisibility(View.VISIBLE);
                tagBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
            }else{
                checkBoxActivated.setVisibility(View.GONE);
                tagBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        }
    }

    @Override
    protected View.OnClickListener defaultItemViewClickListener(SelectionTagViewHolder holder, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }

    public void clearData() {
        int size = tagList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                tagList.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    public void swap(List<String> newTagList){
        clearData();
        if(newTagList.size() > 0) {
            tagList.addAll(newTagList);
            notifyItemRangeInserted(0,newTagList.size());
        }
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

}
