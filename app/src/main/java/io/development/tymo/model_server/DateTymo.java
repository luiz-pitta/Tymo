package io.development.tymo.model_server;

public class DateTymo {

    private int day;
    private int month;
    private int year;
    private int minute;
    private int hour;
    private int minute_end;
    private int hour_end;
    private long date_time_now;
    private long date_time;
    private long date_time_end;
    private String id_device;
    private String current_version_app;

    private double lat_current;
    private double lng_current;

    public void setLatCurrent(double lat_current) {
        this.lat_current = lat_current;
    }

    public void setLngCurrent(double lng_current) {
        this.lng_current = lng_current;
    }

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public long getDateTimeNow() {
        return date_time_now;
    }

    public void setDateTime(long date_time) {
        this.date_time = date_time;
    }

    public void setDateTimeEnd(long date_time_end) {
        this.date_time_end = date_time_end;
    }

    public long getDateTime() {
        return date_time;
    }

    public long getDateTimeEnd() {
        return date_time_end;
    }

    public void setIdDevice(String id_device) {
        this.id_device = id_device;
    }

    public void setCurrentVersionApp(String current_version_app) {
        this.current_version_app = current_version_app;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getMinute() {
        return minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinuteEnd() {
        return minute_end;
    }

    public int getHourEnd() {
        return hour_end;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinuteEnd(int minute) {
        this.minute_end = minute;
    }

    public void setHourEnd(int hour) {
        this.hour_end = hour;
    }

}
