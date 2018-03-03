package io.development.tymo.view_holder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.util.Calendar;

import io.development.tymo.R;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.activities.ReminderActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.FriendResquestAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FriendRequest;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.models.FriendRequestModel;
import io.development.tymo.models.MyRemindersModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


public class MyRemindersHolder extends BaseViewHolder<MyRemindersModel> implements View.OnClickListener {
    private LinearLayout reminderItemBox;
    private TextView text1, text2, text3;
    private Context context;
    private Object object;
    private RefreshLayoutPlansCallback callback;

    private FirebaseAnalytics mFirebaseAnalytics;


    public MyRemindersHolder(ViewGroup parent, final Context context, RefreshLayoutPlansCallback callback) {
        super(parent, R.layout.list_item_reminder);

        reminderItemBox = $(R.id.reminderItemBox);
        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        this.context = context;
        this.callback = callback;

        reminderItemBox.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void onClick(View v) {
        if (v == reminderItemBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderItemBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(context, ReminderActivity.class);
            intent.putExtra("type_reminder", 1);
            intent.putExtra("reminder_show", new ReminderWrapper((ReminderServer) object));
            context.startActivity(intent);
        }
    }


    @Override
    public void setData(MyRemindersModel reminder) {
        object = reminder.getActivity();
        text1.setText(reminder.getText1());
        text2.setText(reminder.getText2());
        text3.setText(reminder.getText3());

        if(reminder.getText1().matches(""))
            text1.setVisibility(View.GONE);
        else
            text1.setVisibility(View.VISIBLE);

    }

    public interface RefreshLayoutPlansCallback {

        void refreshLayout();
    }


}
