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
import io.development.tymo.view_holder.SelectionInterestViewHolder;


public class SelectionInterestAdapter extends MultiChoiceAdapter<SelectionInterestViewHolder> {

    private List<String> interestList;
    private Context context;
    private boolean register;

    public SelectionInterestAdapter(List<String> interestList, Context context, boolean register) {
        this.interestList = interestList;
        this.context = context;
        this.register = register;
    }

    @Override
    public SelectionInterestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectionInterestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_choose_tag, parent, false));
    }


    @Override
    public void onBindViewHolder(SelectionInterestViewHolder holder, int position) {
        String tag = interestList.get(position);
        super.onBindViewHolder(holder, position);
        holder.text1.setText(tag);
        if (register){
            holder.text1.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }


    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    public void setActive(View view, boolean state){

        ImageView checkBoxActivated  = (ImageView) view.findViewById(R.id.checkBoxActivated);
        RelativeLayout tagBox  = (RelativeLayout) view.findViewById(R.id.tagBox);

        if(checkBoxActivated != null){
            if(state){
                checkBoxActivated.setVisibility(View.VISIBLE);
                if (register){
                    tagBox.setBackgroundColor(ContextCompat.getColor(context, R.color.white_opacity_10));
                }
                else{
                    tagBox.setBackgroundColor(ContextCompat.getColor(context, R.color.select));
                }
            }else{
                checkBoxActivated.setVisibility(View.GONE);
                tagBox.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            }
        }
    }

    private void clearData() {
        int size = interestList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                interestList.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }

    public void swap(List<String> newTagList){
        clearData();
        if(newTagList.size() > 0) {
            interestList.addAll(newTagList);
            notifyItemRangeInserted(0,newTagList.size());
        }
    }

    @Override
    public int getItemCount() {
        return interestList.size();
    }

}
