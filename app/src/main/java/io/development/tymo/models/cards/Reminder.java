package io.development.tymo.models.cards;

import io.development.tymo.model_server.ReminderServer;

public class Reminder {
    private String text;
    private String time;
    private ReminderServer reminderServer;

    public Reminder(String text, String time, ReminderServer reminderServer) {
        this.time = time;
        this.text = text;
        this.reminderServer = reminderServer;
    }

    public ReminderServer getReminderServer() {
        return reminderServer;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(String time) {
        this.time = time;
    }
}