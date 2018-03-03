package io.development.tymo.models.cards;

import io.development.tymo.model_server.ActivityServer;

public class ActivityCard {
    private String time;
    private String icon;
    private int background;
    private int background_light;
    private ActivityServer activityServer;
    private boolean act_as_flag_red;

    public ActivityCard(String time, String icon, int background, int background_light, ActivityServer activityServer, boolean act_as_flag_red) {
        this.time = time;
        this.icon = icon;
        this.background = background;
        this.background_light = background_light;
        this.activityServer = activityServer;
        this.act_as_flag_red = act_as_flag_red;
    }

    public String getTime() {
        return time;
    }

    public String getIcon() {
        return icon;
    }

    public ActivityServer getActivityServer() {
        return activityServer;
    }

    public int getBackground() {
        return background;
    }

    public int getBackgroundLight() {
        return background_light;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public void setBackgroundLight(int background_light) { this.background_light = background_light; }

    public void setTime(String time) {
        this.time = time;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setActAsFlagRed(boolean act_as_flag_red) {
        this.act_as_flag_red = act_as_flag_red;
    }

    public boolean isActAsFlagRed() {
        return act_as_flag_red;
    }
}