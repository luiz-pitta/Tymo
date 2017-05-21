package io.development.tymo.model_server;


import java.io.Serializable;

public class ReminderWrapper implements Serializable {
    private static final long serialVersionUID = 5L;
    private ReminderServer reminderServer;

    public ReminderWrapper(ReminderServer reminderServer) {
        this.reminderServer = reminderServer;
    }

    public ReminderServer getReminderServer() {
        return reminderServer;
    }

}