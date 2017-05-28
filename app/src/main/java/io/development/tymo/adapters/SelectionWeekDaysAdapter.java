package io.development.tymo.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.util.List;

import io.development.tymo.R;
import io.development.tymo.view_holder.SelectionWeekDaysViewHolder;

public class SelectionWeekDaysAdapter extends MultiChoiceAdapter<SelectionWeekDaysViewHolder> {

    private List<String> dayList;
    private Context context;

    public SelectionWeekDaysAdapter(List<String> dayList, Context context) {
        this.dayList = dayList;
        this.context = context;
    }

    @Override
    public SelectionWeekDaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectionWeekDaysViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_week_day, parent, false));
    }


    @Override
    public void onBindViewHolder(SelectionWeekDaysViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String day = dayList.get(position);
        holder.day.setText(day);
    }


    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    public void setActive(View view, boolean state){

        TextView textViewActivated  = (TextView) view.findViewById(R.id.day);

        if(textViewActivated != null){
            if(state) {
                textViewActivated.setBackgroundResource(R.drawable.btn_profile_edit);
                textViewActivated.setTextColor(ContextCompat.getColor(context, R.color.white));
            }
            else {
                textViewActivated.setBackgroundResource(R.drawable.btn_profile_admin);
                textViewActivated.setTextColor(ContextCompat.getColor(context, R.color.deep_purple_400));
            }

        }
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

}
