package io.development.tymo.model_server;

public class ActivityOfDay {

    private String title;

    private int day_start;
    private int month_start;
    private int year_start;
    private int minute_start;
    private int hour_start;

    private int type;
    private int commitment_same_hour;

    public ActivityOfDay(String title, int minute_start, int hour_start, int type, int day_start, int month_start, int year_start){
        this.title = title;
        this.minute_start = minute_start;
        this.hour_start = hour_start;
        this.type = type;
        this.day_start= day_start;
        this.month_start= month_start;
        this.year_start= year_start;
        this.commitment_same_hour = 1;
    }


    public String getTitle() {
        return title;
    }

    public int getDay() {
        return day_start;
    }

    public int getMonth() {
        return month_start;
    }

    public int getYear() {
        return year_start;
    }

    public int getMinuteStart() {
        return minute_start;
    }

    public int getHourStart() {
        return hour_start;
    }

    public int getType() {
        return type;
    }

    public int getCommitmentSameHour() {
        return commitment_same_hour;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMinuteStart(int minute_start) {
        this.minute_start = minute_start;
    }

    public void setHourStart(int hour_start) {
        this.hour_start = hour_start;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCommitmentSameHour(int commitment_same_hour) {
        this.commitment_same_hour = commitment_same_hour;
    }
}
