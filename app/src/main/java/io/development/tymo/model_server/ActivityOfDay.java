package io.development.tymo.model_server;

public class ActivityOfDay {

    private String title;

    private int minute_start;
    private int hour_start;

    private int type;
    private int commitment_same_hour;

    public ActivityOfDay(){
    }

    public ActivityOfDay(String title, int minute_start, int hour_start, int type){
        this.title = title;
        this.minute_start = minute_start;
        this.hour_start = hour_start;
        this.type = type;
    }


    public String getTitle() {
        return title;
    }

    public int getDay() {
        return minute_start;
    }

    public int getMonth() {
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
