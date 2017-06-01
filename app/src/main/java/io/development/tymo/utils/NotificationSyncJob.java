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

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import io.development.tymo.R;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.model_server.IconServer;
import io.development.tymo.network.NetworkUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author rwondratschek
 */
public class NotificationSyncJob extends Job {

    public static final String TAG = "job_notification_tag";

    @Override
    @NonNull
    protected Result onRunJob(final Params params) {

        sendNotificationNextActivity("","");

        return Result.SUCCESS;
    }

    public void sendNotificationNextActivity(String title, String text) {
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), 0);

        if (title.matches("")) {
            title = getContext().getString(R.string.app_name);
        }

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(title)
                        .setContentIntent(pi)
                        .setAutoCancel(true);


        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher));

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        android.support.v4.app.NotificationCompat.BigTextStyle bigTextStyle = new android.support.v4.app.NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);

        mBuilder.setContentText(text);
        bigTextStyle.bigText(text);

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.ENGAGEMENT, mBuilder.build());
    }
}
