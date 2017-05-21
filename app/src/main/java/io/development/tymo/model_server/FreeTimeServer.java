package io.development.tymo.model_server;

import java.util.ArrayList;
import java.util.List;

public class FreeTimeServer {

    private int minute_start;
    private int hour_start;
    private int minute_end;
    private int hour_end;

    public int getMinuteStart() {
        return minute_start;
    }

    public int getHourStart() {
        return hour_start;
    }

    public int getMinuteEnd() {
        return minute_end;
    }

    public int getHourEnd() {
        return hour_end;
    }

    public void setMinuteStart(int time) {
        this.minute_start = time;
    }

    public void setMinuteEnd(int time) {
        this.minute_end = time;
    }

    public void setHourStart(int time) {
        this.hour_start = time;
    }

    public void setHourEnd(int time) {
        this.hour_end = time;
    }

}
