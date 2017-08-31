package io.development.tymo.model_server;

public class ActivityOfDay {

    private String title;

    private int day_start;
    private int month_start;
    private int year_start;
    private int minute_start;
    private int hour_start;
    private boolean date_start_empty;
    private boolean date_end_empty;
    private boolean time_start_empty;
    private boolean time_end_empty;

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

    public void setDateStartEmpty(boolean date_start_empty) {
        this.date_start_empty = date_start_empty;
    }

    public boolean getDateStartEmpty() {
        return date_start_empty;
    }

    public void setDateEndEmpty(boolean date_end_empty) {
        this.date_end_empty = date_end_empty;
    }

    public boolean getDateEndEmpty() {
        return date_end_empty;
    }

    public void setTimeStartEmpty(boolean time_start_empty) {
        this.time_start_empty = time_start_empty;
    }

    public boolean getTimeStartEmpty() {
        return time_start_empty;
    }

    public void setTimeEndEmpty(boolean time_end_empty) {
        this.time_end_empty = time_end_empty;
    }

    public boolean getTimeEndEmpty() {
        return time_end_empty;
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
