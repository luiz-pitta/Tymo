package io.development.tymo.model_server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReminderServer implements Serializable {
    private static final long serialVersionUID = 5L;

    private String text;
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
    private boolean date_time_alert;

    private long date_time_creation;
    private long date_time_start;
    private long date_time_end;
    private long last_date_time;

    private int minute_card;
    private int hour_card;
    private int minute_end_card;
    private int hour_end_card;

    private boolean time_start_empty_card;
    private boolean time_end_empty_card;

    private int repeat_type;
    private int repeat_qty;
    private List<Integer> repeat_list_accepted = new ArrayList<>();

    public ReminderServer() {
    }

    public ReminderServer(ReminderServer reminderServer) {
        this.minute_card = reminderServer.getMinuteCard();
        this.hour_card = reminderServer.getHourCard();
        this.minute_end_card = reminderServer.getMinuteEndCard();
        this.hour_end_card = reminderServer.getHourEndCard();
        this.time_start_empty_card = reminderServer.getTimeStartEmptyCard();
        this.time_end_empty_card = reminderServer.getTimeEndEmptyCard();

        this.repeat_list_accepted = reminderServer.getRepeatListAccepted();

        this.id = reminderServer.getId();

        this.text = reminderServer.getText();

        this.date_time_creation = reminderServer.getDateTimeCreation();
        this.date_time_start = reminderServer.getDateTimeStart();
        this.date_time_end = reminderServer.getDateTimeEnd();
        this.last_date_time = reminderServer.getLastDateTime();
        this.day_start = reminderServer.getDayStart();
        this.month_start = reminderServer.getMonthStart();
        this.year_start = reminderServer.getYearStart();
        this.day_end = reminderServer.getDayEnd();
        this.month_end = reminderServer.getMonthEnd();
        this.year_end = reminderServer.getYearEnd();
        this.minute_start = reminderServer.getMinuteStart();
        this.hour_start = reminderServer.getHourStart();
        this.minute_end = reminderServer.getMinuteEnd();
        this.hour_end = reminderServer.getHourEnd();

        this.repeat_qty = reminderServer.getRepeatQty();
        this.repeat_type = reminderServer.getRepeatType();

        this.date_end_empty = reminderServer.getDateEndEmpty();
        this.date_start_empty = reminderServer.getDateStartEmpty();
        this.time_end_empty = reminderServer.getTimeEndEmpty();
        this.time_start_empty = reminderServer.getTimeStartEmpty();
    }
    
    public void setDateTimeAlert(boolean date_time_alert) {
        this.date_time_alert = date_time_alert;
    }

    public boolean getDateTimeAlert() {
        return date_time_alert;
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

    public void setId(long id) {
        this.id = id;
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

    public String getText() {
        return text;
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

    public void setLastDateTime(long last_date_time) {
        this.last_date_time = last_date_time;
    }

    public long getLastDateTime() {
        return last_date_time;
    }

    public void setText(String text) {
        this.text = text;
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

    public int getRepeatType() {
        return repeat_type;
    }

    public int getRepeatQty() {
        return repeat_qty;
    }

    public void setTimeStartEmptyCard(boolean time_start_empty_card) {
        this.time_start_empty_card = time_start_empty_card;
    }

    public void setTimeEndEmptyCard(boolean time_end_empty_card) {
        this.time_end_empty_card = time_end_empty_card;
    }

    public boolean getTimeStartEmptyCard() {
        return time_start_empty_card;
    }

    public boolean getTimeEndEmptyCard() {
        return time_end_empty_card;
    }

    public int getMinuteCard() {
        return minute_card;
    }

    public int getHourCard() {
        return hour_card;
    }

    public int getMinuteEndCard() {
        return minute_end_card;
    }

    public int getHourEndCard() {
        return hour_end_card;
    }

    public void setMinuteCard(int time) {
        this.minute_card = time;
    }

    public void setMinuteEndCard(int time) {
        this.minute_end_card = time;
    }

    public void setHourCard(int time) {
        this.hour_card = time;
    }

    public void setHourEndCard(int time) {
        this.hour_end_card = time;
    }

    public void setRepeatListAccepted(List<Integer> repeat_list_accepted) {
        this.repeat_list_accepted.addAll(repeat_list_accepted);
    }

    public List<Integer> getRepeatListAccepted() {
        return repeat_list_accepted;
    }
}
