package io.development.tymo.model_server;

public class DateTymo {

    private int day;
    private int month;
    private int year;
    private int minute;
    private int hour;
    private int minute_end;
    private int hour_end;
    private String id_device;

    public void setIdDevice(String id_device) {
        this.id_device = id_device;
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
