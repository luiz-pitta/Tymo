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
    private int day_end;
    private int month_end;
    private int year_end;
    private int minute_start;
    private int hour_start;
    private int minute_end;
    private int hour_end;
    private boolean date_start_empty;
    private boolean date_end_empty;
    private boolean time_start_empty;
    private boolean time_end_empty;

    private long date_time_creation;
    private long date_time_start;
    private long date_time_end;

    private int repeat_type;
    private int repeat_qty;
    private List<Integer> day_list_start = new ArrayList<>();
    private List<Integer> month_list_start = new ArrayList<>();
    private List<Integer> year_list_start = new ArrayList<>();
    private List<Long> date_time_list_start = new ArrayList<>();
    private List<Integer> day_list_end = new ArrayList<>();
    private List<Integer> month_list_end = new ArrayList<>();
    private List<Integer> year_list_end = new ArrayList<>();
    private List<Long> date_time_list_end = new ArrayList<>();

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

    public void setDateTimeCreation(long date_time_creation) {
        this.date_time_creation = date_time_creation;
    }

    public void setDateTimeStart(long date_time_start) {
        this.date_time_start = date_time_start;
    }

    public void setDateTimeEnd(long date_time_end) {
        this.date_time_end = date_time_end;
    }

    public long getDateTimeCreation() {
        return date_time_creation;
    }

    public long getDateTimeStart() {
        return date_time_start;
    }

    public long getDateTimeEnd() {
        return date_time_end;
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

    public int getDayEnd() {
        return day_end;
    }

    public int getMonthEnd() {
        return month_end;
    }

    public int getYearEnd() {
        return year_end;
    }

    public int getMinuteEnd() {
        return minute_end;
    }

    public int getHourEnd() {
        return hour_end;
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

    public void setDayEnd(int date) {
        this.day_end = date;
    }

    public void setMonthEnd(int date) {
        this.month_end = date;
    }

    public void setYearEnd(int date) {
        this.year_end = date;
    }

    public void setMinuteEnd(int time) {
        this.minute_end = time;
    }

    public void setHourEnd(int time) {
        this.hour_end = time;
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

    public void setDateTimeListStart(List<Long> list) {
        this.date_time_list_start.addAll(list);
    }

    public void setDayListEnd(List<Integer> list) {
        this.day_list_end.addAll(list);
    }

    public void setMonthListEnd(List<Integer> list) {
        this.month_list_end.addAll(list);
    }

    public void setYearListEnd(List<Integer> list) {
        this.year_list_end.addAll(list);
    }

    public void setDateTimeListEnd(List<Long> list) {
        this.date_time_list_end.addAll(list);
    }

    public int getRepeatType() {
        return repeat_type;
    }

    public int getRepeatQty() {
        return repeat_qty;
    }
}
