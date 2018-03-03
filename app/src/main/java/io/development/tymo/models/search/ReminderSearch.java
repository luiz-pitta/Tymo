package io.development.tymo.models.search;

import io.development.tymo.model_server.ReminderServer;

public class ReminderSearch {
    private String text1;
    private String text2;
    private String text3;
    private ReminderServer reminderServer;

    public ReminderSearch(String text1, String text2, String text3, ReminderServer reminderServer) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.reminderServer = reminderServer;
    }

    public ReminderServer getReminderServer() {
        return reminderServer;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public String getText3() {
        return text3;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

}