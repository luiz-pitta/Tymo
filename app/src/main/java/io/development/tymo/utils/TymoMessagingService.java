package io.development.tymo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.development.tymo.R;
import io.development.tymo.activities.ContactsActivity;
import io.development.tymo.activities.FriendRequestActivity;
import io.development.tymo.activities.InviteActivity;
import io.development.tymo.activities.MainActivity;

public class TymoMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> map = remoteMessage.getData();
        String type = map.get("type");

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, type);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        boolean notification = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION, false);

        if(notification) {
            if (type.matches("engagement")) {
                if (map.get("activated").matches("true")) {
                    sendNotificationEngagement(map.get("title"), map.get("text"));
                }
            } else {
                String name = map.get("name");
                int number_solicitation = Integer.valueOf(map.get("n_solicitation"));

                if (type.matches("people"))
                    sendNotificationPeople(name, number_solicitation);
                else if (type.matches("peopleAccept")) {
                    sendNotificationPeopleAccept(name, number_solicitation);
                } else if (type.matches("invite")) {
                    sendNotificationInvite(number_solicitation);
                } else if (type.matches("inviteAccept")) {
                    sendNotificationInviteAccept(map.get("title"), name, number_solicitation);
                } else if (type.matches("cancel")) {
                    sendNotificationCancel(map.get("title"), name, number_solicitation);
                }
            }
        }
    }

    public void sendNotificationInvite(int qty) {

        Intent intent = new Intent(this, InviteActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        mBuilder.setContentTitle(getString(R.string.app_name));
        bigTextStyle.setBigContentTitle(getString(R.string.app_name));

        if (qty == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_3_title));
            bigTextStyle.bigText(getString(R.string.push_notification_3_title));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_4_title, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_4_title, qty));
        }

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.INVITE, mBuilder.build());
    }

    public void sendNotificationCancel(String title, String name, int type) {

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(title)
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);

        if (type == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_5_text_4, name));
            bigTextStyle.bigText(getString(R.string.push_notification_5_text_4, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_5_text_3, name));
            bigTextStyle.bigText(getString(R.string.push_notification_5_text_3, name));
        }

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.CANCEL, mBuilder.build());
    }

    public void sendNotificationInviteAccept(String title, String name, int qty) {

        Intent intent = new Intent(this, InviteActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(title)
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);

        if (qty == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_5_text_1, name));
            bigTextStyle.bigText(getString(R.string.push_notification_5_text_1, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_5_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_5_text_2, qty));
        }

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.INVITE_ACCEPT, mBuilder.build());
    }

    public void sendNotificationEngagement(String title, String text) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        if (title.matches("")) {
            title = getString(R.string.app_name);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(title)
                        .setContentIntent(pi)
                        .setAutoCancel(true);


        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);

        mBuilder.setContentText(text);
        bigTextStyle.bigText(text);

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.ENGAGEMENT, mBuilder.build());
    }

    public void sendNotificationPeopleAccept(String name, int qty) {

        String email_user = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString(Constants.EMAIL, "");
        Intent intent = new Intent(this, ContactsActivity.class);
        intent.putExtra("email_contacts", email_user);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(getString(R.string.push_notification_1_title))
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.push_notification_1_title));

        if (qty == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_1_text_1, name));
            bigTextStyle.bigText(getString(R.string.push_notification_1_text_1, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_1_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_1_text_2, qty));
        }

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.PEOPLE_ACCEPT, mBuilder.build());
    }

    public void sendNotificationPeople(String name, int qty) {

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FriendRequestActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(getString(R.string.push_notification_2_title))
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.push_notification_2_title));

        if (qty == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_2_text_1, name));
            bigTextStyle.bigText(getString(R.string.push_notification_2_text_1, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_2_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_2_text_2, qty));
        }

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.PEOPLE, mBuilder.build());
    }
}
