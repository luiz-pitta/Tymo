package io.development.tymo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by luizg on 16/03/2017.
 */

public class GoogleCalendarEvents {

    private static Uri eventsUri = CalendarContract.Events.CONTENT_URI;

    private static final int EVENTS = 1;

    /** Calendars table columns */
    private static final String[] CALENDARS_COLUMNS = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    /** Events table columns */
    private static final String[] EVENTS_COLUMNS = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_DISPLAY_NAME,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.LAST_DATE,
            CalendarContract.Events.EXDATE,
            CalendarContract.Events.RDATE,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.ACCESS_LEVEL,
            CalendarContract.Events.STATUS,
    };

    @Nullable
    public static ArrayList<ArrayList<String>>  readCalendarEvent(Context context) {

        ArrayList<ArrayList<String>> list = new ArrayList<>();
        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        ArrayList<String> nameOfEvent = new ArrayList<String>();
        ArrayList<String> startDates = new ArrayList<String>();
        ArrayList<String> endDates = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> locations = new ArrayList<String>();
        ArrayList<String> repeat = new ArrayList<String>();
        ArrayList<String> ids = new ArrayList<String>();

        Cursor cursor = context.getContentResolver().
                query(
                    eventsUri,
                    EVENTS_COLUMNS,
                    null,
                    null,
                    null
                );

        try {
            cursor.moveToFirst();

            // fetching calendars id
            nameOfEvent.clear();
            startDates.clear();
            endDates.clear();
            descriptions.clear();
            repeat.clear();
            ids.clear();
            locations.clear();
            for (int i = 0; i < cursor.getCount(); i++) {

                String calender_type = cursor.getString(1);
                if(calender_type.contains(email)) {
                    ids.add(cursor.getString(0));
                    nameOfEvent.add(cursor.getString(2));
                    descriptions.add(cursor.getString(3));
                    locations.add(cursor.getString(4));
                    startDates.add(cursor.getString(5));
                    endDates.add(cursor.getString(6));
                    repeat.add(cursor.getString(10));
                }
                cursor.moveToNext();

            }
            cursor.close();
        }catch (NullPointerException e){
            return null;
        }

        list.add(ids);
        list.add(nameOfEvent);
        list.add(descriptions);
        list.add(locations);
        list.add(startDates);
        list.add(endDates);
        list.add(repeat);

        return list;
    }

    @Nullable
    public static ArrayList<ArrayList<String>> readCalendarHolidays(Context context, long time1, long time2) {

        ArrayList<ArrayList<String>> list = new ArrayList<>();
        ArrayList<String> nameOfEvent = new ArrayList<>();
        ArrayList<String> startDates = new ArrayList<>();

        Cursor cursor = context.getContentResolver().
                query(
                        eventsUri,
                        EVENTS_COLUMNS,
                        null,
                        null,
                        null
                );

        try {
            cursor.moveToFirst();

            // fetching calendars id
            nameOfEvent.clear();
            startDates.clear();
            for (int i = 0; i < cursor.getCount(); i++) {

                String calender_type = cursor.getString(1);
                long time = Long.parseLong(cursor.getString(5));
                if(calender_type.contains("Holiday") && (time >= time1 && time <= time2)) {
                    nameOfEvent.add(cursor.getString(2));
                    startDates.add(cursor.getString(6));
                }
                cursor.moveToNext();
            }
            cursor.close();
        }catch (NullPointerException e){
            return null;
        }

        list.add(nameOfEvent);
        list.add(startDates);

        return list;
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
