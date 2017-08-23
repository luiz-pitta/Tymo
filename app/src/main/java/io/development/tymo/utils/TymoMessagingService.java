package io.development.tymo.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.development.tymo.R;
import io.development.tymo.activities.ContactsActivity;
import io.development.tymo.activities.FriendRequestActivity;
import io.development.tymo.activities.InviteActivity;
import io.development.tymo.activities.MainActivity;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;

public class TymoMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> map = remoteMessage.getData();
        String type = map.get("type");

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, type + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        boolean notification_push = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getBoolean(Constants.NOTIFICATION_PUSH, true);

        if (type.matches("peopleAccept"))
            updateSearchMessageToActivity();

        if (type.matches("cancel"))
            updateNotificationStartToday();

        if(notification_push) {
            String name = map.get("name");
            switch (type){
                case "people":
                    sendNotificationPeople(name, Integer.valueOf(map.get("n_solicitation")));
                    break;
                case "peopleAccept":
                    sendNotificationPeopleAccept(name, Integer.valueOf(map.get("n_solicitation")));
                    break;
                case "invite":
                    sendNotificationInvite(Integer.valueOf(map.get("n_solicitation")));
                    break;
                case "inviteAccept":
                    sendNotificationInviteAccept(map.get("title"), name, Integer.valueOf(map.get("n_solicitation")));
                    break;
                case "cancel":
                    sendNotificationCancel(map.get("title"), name, Integer.valueOf(map.get("n_solicitation")));
                    break;
                case "adm":
                    sendNotificationAdm(map.get("title"));
                    break;
                case "engagement":
                    if (map.get("activated").matches("true")) {
                        sendNotificationEngagement(map.get("title"), map.get("text"));
                    }
                    break;
            }
        }
    }

    public void sendNotificationInvite(int qty) {

        Intent intent = new Intent(this, InviteActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
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
            mBuilder.setContentText(getString(R.string.push_notification_invitations_text_1));
            bigTextStyle.bigText(getString(R.string.push_notification_invitations_text_1));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_invitations_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_invitations_text_2, qty));
        }

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.INVITE, mBuilder.build());
    }

    public void sendNotificationAdm(String title) {

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(getString(R.string.push_notification_adm))
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.push_notification_adm));

        mBuilder.setContentText(title);
        bigTextStyle.bigText(title);

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.ADM_SET, mBuilder.build());
    }

    public void sendNotificationCancel(String title, String name, int type) {

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
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
            mBuilder.setContentText(getString(R.string.push_notification_canceled_flag, name));
            bigTextStyle.bigText(getString(R.string.push_notification_canceled_flag, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_canceled_act, name));
            bigTextStyle.bigText(getString(R.string.push_notification_canceled_act, name));
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
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
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
            mBuilder.setContentText(getString(R.string.push_notification_invitations_accepted_text_1, name));
            bigTextStyle.bigText(getString(R.string.push_notification_invitations_accepted_text_1, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_invitations_accepted_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_invitations_accepted_text_2, qty));
        }

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.INVITE_ACCEPT, mBuilder.build());
    }

    private void updateSearchMessageToActivity() {
        Intent intent = new Intent("search_update");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateNotificationStartToday() {
        Intent intent = new Intent("notification_update");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendNotificationEngagement(String title, String text) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        if (title.matches("")) {
            title = getString(R.string.app_name);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
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
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(getString(R.string.push_notification_pending_requests_accepted_title))
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.push_notification_pending_requests_accepted_title));

        if (qty == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_pending_requests_accepted_text_1, name));
            bigTextStyle.bigText(getString(R.string.push_notification_pending_requests_accepted_text_1, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_pending_requests_accepted_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_pending_requests_accepted_text_2, qty));
        }

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.PEOPLE_ACCEPT, mBuilder.build());
    }

    public void sendNotificationPeople(String name, int qty) {

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FriendRequestActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_add_cube)
                        .setContentTitle(getString(R.string.push_notification_pending_requests_title))
                        .setContentIntent(pi)
                        .setAutoCancel(true);

        //Vibration
        mBuilder.setVibrate(new long[]{500, 750});

        //Ton
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.push_notification_pending_requests_title));

        if (qty == 1) {
            mBuilder.setContentText(getString(R.string.push_notification_pending_requests_text_1, name));
            bigTextStyle.bigText(getString(R.string.push_notification_pending_requests_text_1, name));
        } else {
            mBuilder.setContentText(getString(R.string.push_notification_pending_requests_text_2, qty));
            bigTextStyle.bigText(getString(R.string.push_notification_pending_requests_text_2, qty));
        }

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        mBuilder.setStyle(bigTextStyle);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.PEOPLE, mBuilder.build());
    }
}
