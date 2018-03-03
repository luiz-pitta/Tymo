package io.development.tymo.models.cards;

import io.development.tymo.model_server.FlagServer;

public class Flag {
    private String time;
    private int icon;
    private boolean free, paint;
    private FlagServer flagServer;
    private boolean act_as_flag_red;

    public Flag(String time, int icon, boolean free, FlagServer flagServer, boolean paint, boolean act_as_flag_red) {
        this.time = time;
        this.icon = icon;
        this.free = free;
        this.flagServer = flagServer;
        this.paint = paint;
        this.act_as_flag_red = act_as_flag_red;
    }

    public FlagServer getFlagServer() {
        return flagServer;
    }

    public String getTime() {
        return time;
    }

    public boolean getFree() {
        return free;
    }

    public boolean getPaint() {
        return paint;
    }

    public int getIcon() {
        return icon;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setActAsFlagRed(boolean act_as_flag_red) {
        this.act_as_flag_red = act_as_flag_red;
    }

    public boolean isActAsFlagRed() {
        return act_as_flag_red;
    }
}