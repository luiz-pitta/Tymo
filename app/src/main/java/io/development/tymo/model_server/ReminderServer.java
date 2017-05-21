package io.development.tymo.model_server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReminderServer implements Serializable {
    private static final long serialVersionUID = 5L;

    private String title;
    private String creator;
    private long id;

    private int status; // -1 = already happened ; 0 = is happening ; 1 = will happen

    private int day_start;
    private int month_start;
    private int year_start;
    private int minute_start;
    private int hour_start;

    private long date_time_creation;

    private int repeat_type;
    private int repeat_qty;
    private List<Integer> day_list_start = new ArrayList<>();
    private List<Integer> month_list_start = new ArrayList<>();
    private List<Integer> year_list_start = new ArrayList<>();

    public void setDateTimeCreation(long date_time_creation) {
        this.date_time_creation = date_time_creation;
    }

    public long getDateTimeCreation() {
        return date_time_creation;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDayStart() {
        return day_start;
    }

    public int getMonthStart() {
        return month_start;
    }

    public int getYearStart() {
        return year_start;
    }

    public int getMinuteStart() {
        return minute_start;
    }

    public int getHourStart() {
        return hour_start;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setRepeatQty(int repeat_qty) {
        this.repeat_qty = repeat_qty;
    }

    public void setDayStart(int date) {
        this.day_start = date;
    }

    public void setMonthStart(int date) {
        this.month_start = date;
    }

    public void setYearStart(int date) {
        this.year_start = date;
    }

    public void setMinuteStart(int time) {
        this.minute_start = time;
    }

    public void setHourStart(int time) {
        this.hour_start = time;
    }

    public void setRepeatType(int repeat_type) {
        this.repeat_type = repeat_type;
    }

    public void setDayListStart(List<Integer> list) {
        this.day_list_start.addAll(list);
    }

    public void setMonthListStart(List<Integer> list) {
        this.month_list_start.addAll(list);
    }

    public void setYearListStart(List<Integer> list) {
        this.year_list_start.addAll(list);
    }

    public int getRepeatType() {
        return repeat_type;
    }

    public int getRepeatQty() {
        return repeat_qty;
    }
}
