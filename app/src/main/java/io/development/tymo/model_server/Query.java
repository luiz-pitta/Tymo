package io.development.tymo.model_server;

public class Query {

    private String email;
    private String query;

    private int day;
    private int month;
    private int year;
    private int minute_start;
    private int hour_start;

    private long date_time_now;

    private double lat_current;
    private double lng_current;

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public void setLatCurrent(double lat_current) {
        this.lat_current = lat_current;
    }

    public void setLngCurrent(double lng_current) {
        this.lng_current = lng_current;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setQuery(String query) {
        this.query = "";
        this.query = this.query.concat("(?i).*");
        this.query = this.query.concat(query);
        this.query = this.query.concat(".*");
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMinuteStart(int minute_start) {
        this.minute_start = minute_start;
    }

    public void setHourStart(int hour_start) {
        this.hour_start = hour_start;
    }

}
