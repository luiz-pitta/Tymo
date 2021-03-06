package io.development.tymo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import io.development.tymo.R;
import io.development.tymo.activities.CommitmentsOfTheDayActivity;
import io.development.tymo.model_server.ActivityOfDay;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;
import static android.content.Context.MODE_PRIVATE;

public class NotificationSyncJob extends Job {

    public static final String TAG = "job_notification_tag";
    private int position_single = -1;

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
            boolean day_before = extras.getBoolean("day_before", false);
            int qty = isNotificationEnable(list_json, position);

            if(qty > 0 && !day_before)
                sendNotificationNextActivity(qty, list_json);
            else if(qty > 0 && day_before) {
                //sendNotificationDayBeforeActivity(qty, list_json);
            }
        }

        return Result.SUCCESS;

    }

    private int isNotificationEnable(ArrayList<ActivityOfDay> list_json, int position){
        boolean notification1 = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_ACT, true);
        boolean notification2 = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_FLAG, true);
        boolean notification3 = getContext().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_REMINDER, true);

        // XXX MUDAR QUANDO ESTIVER FUNCIONANDO CORRETAMENTE
        notification1 = false;
        notification2 = false;
        notification3 = false;

        int qty = list_json.get(position).getCommitmentSameHour();
        int size = qty;

        if(notification1 && notification2 && notification3)
            return qty;

        for(int i=position;i<size;i++){
            ActivityOfDay activityOfDay = list_json.get(i);
            int type = activityOfDay.getType();

            switch (type){
                case Constants.ACT:
                    if(!notification1)
                        qty--;
                    else
                        position_single = i;
                    break;
                case Constants.FLAG:
                    if(!notification2)
                        qty--;
                    else
                        position_single = i;
                    break;
                case Constants.REMINDER:
                    if(!notification3)
                        qty--;
                    else
                        position_single = i;
                    break;
            }
        }

        return qty;
    }

    private void sendNotificationNextActivity(int qty, ArrayList<ActivityOfDay> list_json) {
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), CommitmentsOfTheDayActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext(), DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        android.support.v4.app.NotificationCompat.BigTextStyle bigTextStyle = new android.support.v4.app.NotificationCompat.BigTextStyle();


        if (qty == 1) {
            ActivityOfDay activityOfDay = list_json.get(position_single >= 0 ? position_single : 0);
            String title = activityOfDay.getTitle();
            int type = activityOfDay.getType(); //Constants.ACT, Constants.FLAG e Constants.REM
            mBuilder.setContentText(title);
            bigTextStyle.bigText(title);
            mBuilder.setContentTitle(getContext().getString(R.string.push_notification_start_commitments_title));
            bigTextStyle.setBigContentTitle(getContext().getString(R.string.push_notification_start_commitments_title));
        } else {
            mBuilder.setContentText(getContext().getString(R.string.push_notification_start_commitments_text_2, qty));
            bigTextStyle.bigText(getContext().getString(R.string.push_notification_start_commitments_text_2, qty));
            mBuilder.setContentTitle(getContext().getString(R.string.push_notification_start_commitments_title));
            bigTextStyle.setBigContentTitle(getContext().getString(R.string.push_notification_start_commitments_title));
        }

        position_single = -1;

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.NEXT_COMMITMENT, mBuilder.build());
    }

    private void sendNotificationDayBeforeActivity(int qty, ArrayList<ActivityOfDay> list_json) {
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), CommitmentsOfTheDayActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext(), DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        android.support.v4.app.NotificationCompat.BigTextStyle bigTextStyle = new android.support.v4.app.NotificationCompat.BigTextStyle();


        if (qty == 1) {
            ActivityOfDay activityOfDay = list_json.get(position_single >= 0 ? position_single : 0);
            String title = activityOfDay.getTitle();
            int type = activityOfDay.getType(); //Constants.ACT, Constants.FLAG e Constants.REM
            mBuilder.setContentText(getContext().getString(R.string.push_notification_start_commitments_text_3));
            bigTextStyle.bigText(getContext().getString(R.string.push_notification_start_commitments_text_3));
            mBuilder.setContentTitle(getContext().getString(R.string.push_notification_start_commitments_title_2));
            bigTextStyle.setBigContentTitle(getContext().getString(R.string.push_notification_start_commitments_title_2));
        } else {
            mBuilder.setContentText(getContext().getString(R.string.push_notification_start_commitments_text_4, qty));
            bigTextStyle.bigText(getContext().getString(R.string.push_notification_start_commitments_text_4, qty));
            mBuilder.setContentTitle(getContext().getString(R.string.push_notification_start_commitments_title_2));
            bigTextStyle.setBigContentTitle(getContext().getString(R.string.push_notification_start_commitments_title_2));
        }

        position_single = -1;

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.NEXT_COMMITMENT, mBuilder.build());
    }
}
