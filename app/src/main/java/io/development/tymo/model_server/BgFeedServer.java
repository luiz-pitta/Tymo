package io.development.tymo.model_server;

public class BgFeedServer {

    private String url_bg, url_bg_2;
    private int period, minute_start, hour_start, minute_end, hour_end;

    public void setUrlBg(String url_bg) {
        this.url_bg = url_bg;
    }

    public void setUrlBg2(String url_bg_2) {
        this.url_bg_2 = url_bg_2;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setMinuteStart(int minute_start) {
        this.minute_start = minute_start;
    }

    public void setHourStart(int hour_start) {
        this.hour_start = hour_start;
    }

    public void setMinuteEnd(int minute_end) {
        this.minute_end = minute_end;
    }

    public void setHourEnd(int hour_end) {
        this.hour_end = hour_end;
    }

    public String getUrlBg() {
        return url_bg;
    }

    public String getUrlBg2() {
        return url_bg_2;
    }

    public int getPeriod() {
        return period;
    }

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

}
