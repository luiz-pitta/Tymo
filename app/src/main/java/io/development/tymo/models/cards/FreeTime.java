package io.development.tymo.models.cards;

public class FreeTime {
    private String time;
    private boolean inPast = false;

    public FreeTime(String time, boolean inPast) {
        this.time = time;
        this.inPast = inPast;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isInPast() {
        return inPast;
    }

    public void setInPast(boolean inPast) {
        this.inPast = inPast;
    }
}