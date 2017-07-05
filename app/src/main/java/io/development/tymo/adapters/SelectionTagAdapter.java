package io.development.tymo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.util.List;

import io.development.tymo.R;
import io.development.tymo.view_holder.FooterViewHolder;
import io.development.tymo.view_holder.SelectionTagViewHolder;

public class SelectionTagAdapter extends MultiChoiceAdapter<RecyclerView.ViewHolder> {

    private List<String> tagList;
    private Context context;

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public SelectionTagAdapter(List<String> tagList, Context context) {
        this.tagList = tagList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM)
            return new SelectionTagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_tag, parent, false));
        else
            return new FooterViewHolder(LayoutInflater.from (parent.getContext ()).inflate (R.layout.footer_list_items_selects, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(holder instanceof SelectionTagViewHolder) {
            SelectionTagViewHolder selectionTagViewHolder = (SelectionTagViewHolder)holder;
            String tag = tagList.get(position);
            selectionTagViewHolder.text1.setText(tag);
        }
    }

    private boolean isPositionFooter (int position) {
        return position == tagList.size ();
    }

    @Override
    public int getItemViewType (int position) {
        if(isPositionFooter (position))
            return TYPE_FOOTER;

        return TYPE_ITEM;
    }


    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    public void setActive(@NonNull View view, boolean state) {

        ImageView checkBoxActivated  = view.findViewById(R.id.checkBoxActivated);
        RelativeLayout tagBox  = view.findViewById(R.id.tagBox);

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

    private void clearData() {
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
        return tagList.size() + 1;
    }

}
