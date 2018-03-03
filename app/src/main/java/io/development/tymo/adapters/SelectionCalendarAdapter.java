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

public class SelectionCalendarAdapter extends MultiChoiceAdapter<RecyclerView.ViewHolder> {

    private List<String> tagList;
    private Context context;

    public SelectionCalendarAdapter(List<String> tagList, Context context) {
        this.tagList = tagList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectionTagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_tag, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SelectionTagViewHolder selectionTagViewHolder = (SelectionTagViewHolder)holder;
        String tag = tagList.get(position);
        selectionTagViewHolder.text1.setText(tag);

    }

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
        return tagList.size();
    }

}
