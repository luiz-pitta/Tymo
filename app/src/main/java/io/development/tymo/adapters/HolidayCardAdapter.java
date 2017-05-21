package io.development.tymo.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.models.CompareModel;
import io.development.tymo.models.cards.ActivityCard;
import io.development.tymo.models.cards.Flag;
import io.development.tymo.models.cards.FreeTime;
import io.development.tymo.models.cards.Reminder;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;

import static android.content.Context.MODE_PRIVATE;

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