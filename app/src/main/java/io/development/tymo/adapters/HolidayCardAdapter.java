package io.development.tymo.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import io.development.tymo.R;

public class HolidayCardAdapter extends RecyclerView.Adapter<HolidayCardAdapter.MyViewHolder> {

    private List<String> holidayList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView text1;

        public MyViewHolder(View view) {
            super(view);

            text1 = (TextView) view.findViewById(R.id.text1);
        }
    }

    public HolidayCardAdapter(List<String> holidayList) {
        this.holidayList = holidayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_holiday, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String holiday_name = holidayList.get(position);

        holder.text1.setText(holiday_name);

    }

    @Override
    public int getItemCount() {
        return holidayList.size();
    }

}