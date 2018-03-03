package io.development.tymo.model_server;


import java.io.Serializable;

public class ActivityWrapper implements Serializable {
    private static final long serialVersionUID = 4L;
    private ActivityServer activityServer;

    public ActivityWrapper(ActivityServer activityServer) {
        this.activityServer = activityServer;
    }

    public ActivityServer getActivityServer() {
        return activityServer;
    }

}