package io.development.tymo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import io.development.tymo.R;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.activities.NextCommitmentsActivity;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.IconServer;
import io.development.tymo.network.NetworkUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author rwondratschek
 */
public class NotificationSyncJob extends Job {

    public static final String TAG = "job_notification_tag";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {

        PersistableBundleCompat extras = params.getExtras();

        Gson gson = new Gson();
        ArrayList<ActivityOfDay> list_json;
        String json = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString("ListActDay", "");
        if(!json.matches("")) {
            list_json = gson.fromJson(json, new TypeToken<ArrayList<ActivityOfDay>>(){}.getType());
            int position = extras.getInt("position_act", 0);
            int qty = isNotificationEnable(list_json, position);

            if(qty > 0)
                sendNotificationNextActivity(qty);
        }

        return Result.SUCCESS;

    }

    private int isNotificationEnable(ArrayList<ActivityOfDay> list_json, int position){
        boolean notification = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION, true);
        boolean notification1 = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_ACT, true);
        boolean notification2 = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_FLAG, true);
        boolean notification3 = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_REMINDER, true);

        int qty = list_json.get(position).getCommitmentSameHour();

        if(!notification)
            return 0;

        if(notification1 && notification2 && notification3)
            return qty;

        for(int i=0;i<list_json.size();i++){
            ActivityOfDay activityOfDay = list_json.get(i);
            int type = activityOfDay.getType();

            switch (type){
                case Constants.ACT:
                    if(!notification1)
                        qty--;
                    break;
                case Constants.FLAG:
                    if(!notification2)
                        qty--;
                    break;
                case Constants.REMINDER:
                    if(!notification3)
                        qty--;
                    break;
            }
        }

        return qty;
    }

    private void sendNotificationNextActivity(int qty) {
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), NextCommitmentsActivity.class), 0);

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        android.support.v4.app.NotificationCompat.BigTextStyle bigTextStyle = new android.support.v4.app.NotificationCompat.BigTextStyle();


        if (qty == 1) {
            mBuilder.setContentText(getContext().getString(R.string.push_notification_6_text_1));
            bigTextStyle.bigText(getContext().getString(R.string.push_notification_6_text_1));
            mBuilder.setContentTitle(getContext().getString(R.string.push_notification_6_title_1));
            bigTextStyle.setBigContentTitle(getContext().getString(R.string.push_notification_6_title_1));
        } else {
            mBuilder.setContentText(getContext().getString(R.string.push_notification_6_text_2, qty));
            bigTextStyle.bigText(getContext().getString(R.string.push_notification_6_text_2, qty));
            mBuilder.setContentTitle(getContext().getString(R.string.push_notification_6_title_2));
            bigTextStyle.setBigContentTitle(getContext().getString(R.string.push_notification_6_title_2));
        }

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.NEXT_COMMITMENT, mBuilder.build());
    }
}