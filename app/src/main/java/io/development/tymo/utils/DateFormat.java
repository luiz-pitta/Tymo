package io.development.tymo.utils;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

import io.development.tymo.R;

public class DateFormat {
    private Context context;

    public DateFormat(Context context) {
        this.context = context;
    }

    public String formatMonthShort(int month) {
        switch (month) {
            case 2:
                return context.getResources().getString(R.string.february).substring(0,3).toLowerCase();
            case 3:
                return context.getResources().getString(R.string.march).substring(0,3).toLowerCase();
            case 4:
                return context.getResources().getString(R.string.april).substring(0,3).toLowerCase();
            case 5:
                return context.getResources().getString(R.string.may).substring(0,3).toLowerCase();
            case 6:
                return context.getResources().getString(R.string.june).substring(0,3).toLowerCase();
            case 7:
                return context.getResources().getString(R.string.july).substring(0,3).toLowerCase();
            case 8:
                return context.getResources().getString(R.string.august).substring(0,3).toLowerCase();
            case 9:
                return context.getResources().getString(R.string.september).substring(0,3).toLowerCase();
            case 10:
                return context.getResources().getString(R.string.october).substring(0,3).toLowerCase();
            case 11:
                return context.getResources().getString(R.string.november).substring(0,3).toLowerCase();
            case 12:
                return context.getResources().getString(R.string.december).substring(0,3).toLowerCase();
            default:
                return context.getResources().getString(R.string.january).substring(0,3).toLowerCase();
        }
    }

    public String formatMonthLowerCase(int month) {
        switch (month) {
            case 2:
                return context.getResources().getString(R.string.february).toLowerCase();
            case 3:
                return context.getResources().getString(R.string.march).toLowerCase();
            case 4:
                return context.getResources().getString(R.string.april).toLowerCase();
            case 5:
                return context.getResources().getString(R.string.may).toLowerCase();
            case 6:
                return context.getResources().getString(R.string.june).toLowerCase();
            case 7:
                return context.getResources().getString(R.string.july).toLowerCase();
            case 8:
                return context.getResources().getString(R.string.august).toLowerCase();
            case 9:
                return context.getResources().getString(R.string.september).toLowerCase();
            case 10:
                return context.getResources().getString(R.string.october).toLowerCase();
            case 11:
                return context.getResources().getString(R.string.november).toLowerCase();
            case 12:
                return context.getResources().getString(R.string.december).toLowerCase();
            default:
                return context.getResources().getString(R.string.january).toLowerCase();
        }
    }

    public String formatMonth(int month) {
        switch (month) {
            case 2:
                return context.getResources().getString(R.string.february);
            case 3:
                return context.getResources().getString(R.string.march);
            case 4:
                return context.getResources().getString(R.string.april);
            case 5:
                return context.getResources().getString(R.string.may);
            case 6:
                return context.getResources().getString(R.string.june);
            case 7:
                return context.getResources().getString(R.string.july);
            case 8:
                return context.getResources().getString(R.string.august);
            case 9:
                return context.getResources().getString(R.string.september);
            case 10:
                return context.getResources().getString(R.string.october);
            case 11:
                return context.getResources().getString(R.string.november);
            case 12:
                return context.getResources().getString(R.string.december);
            default:
                return context.getResources().getString(R.string.january);
        }
    }

    public String formatDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return context.getResources().getString(R.string.sunday).substring(0,3);
            case 2:
                return context.getResources().getString(R.string.monday).substring(0,3);
            case 3:
                return context.getResources().getString(R.string.tuesday).substring(0,3);
            case 4:
                return context.getResources().getString(R.string.wednesday).substring(0,3);
            case 5:
                return context.getResources().getString(R.string.thursday).substring(0,3);
            case 6:
                return context.getResources().getString(R.string.friday).substring(0,3);
            case 7:
                return context.getResources().getString(R.string.saturday).substring(0,3);
            default:
                return context.getResources().getString(R.string.monday).substring(0,3);
        }
    }

    public String todayTomorrowYesterdayCheck(int dayOfWeek, Calendar calendar) {
        Calendar todayCalendar = Calendar.getInstance();
        Calendar yesterdayCalendar = Calendar.getInstance();
        Calendar tomorrowCalendar = Calendar.getInstance();

        yesterdayCalendar.add(Calendar.DATE, -1);
        tomorrowCalendar.add(Calendar.DATE, 1);

        if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) && calendar.get(Calendar.DATE) == todayCalendar.get(Calendar.DATE)) {
            return context.getResources().getString(R.string.today);
        } else if (calendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == yesterdayCalendar.get(Calendar.MONTH) && calendar.get(Calendar.DATE) == yesterdayCalendar.get(Calendar.DATE)) {
            return context.getResources().getString(R.string.yesterday);
        } else if (calendar.get(Calendar.YEAR) == tomorrowCalendar.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == tomorrowCalendar.get(Calendar.MONTH) && calendar.get(Calendar.DATE) == tomorrowCalendar.get(Calendar.DATE)) {
            return context.getResources().getString(R.string.tomorrow);
        } else {
            return formatDayOfWeek(dayOfWeek);
        }
    }
}
