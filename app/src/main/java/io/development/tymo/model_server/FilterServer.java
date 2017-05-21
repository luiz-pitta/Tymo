package io.development.tymo.model_server;


import com.cunoraz.tagview.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FilterServer implements Serializable {
    private static final long serialVersionUID = 10L;

    private boolean proximity;
    private boolean date_hour;
    private boolean popularity;

    private boolean isFilterFilled = false;

    private String query;

    private int day_start;
    private int month_start;
    private int year_start;
    private int day_end;
    private int month_end;
    private int year_end;
    private int minute_start;
    private int hour_start;
    private int minute_end;
    private int hour_end;

    private ArrayList<String> tags = new ArrayList<>();
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<User> friends_info = new ArrayList<>();
    private ArrayList<Integer> week_days = new ArrayList<>();

    private double lat;
    private double lng;
    private String location;

    public FilterServer() {
    }

    public void setQuery(String query) {
        this.query = "";
        this.query = this.query.concat("(?i).*");
        this.query = this.query.concat(query);
        this.query = this.query.concat(".*");
    }

    public String getQuery() {
        return query;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFilterFilled() {
        return isFilterFilled;
    }

    public void setFilterFilled(boolean isFilterFilled) {
        this.isFilterFilled = isFilterFilled;
    }

    public boolean getPopularity() {
        return popularity;
    }

    public boolean getProximity() {
        return proximity;
    }

    public boolean getDateHour() {
        return date_hour;
    }

    public void setPopularity(boolean popularity) {
        this.popularity = popularity;
    }

    public void setProximity(boolean proximity) {
        this.proximity = proximity;
    }

    public void setDateHour(boolean date_hour) {
        this.date_hour = date_hour;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getDayStart() {
        return day_start;
    }

    public int getMonthStart() {
        return month_start;
    }

    public int getYearStart() {
        return year_start;
    }

    public int getDayEnd() {
        return day_end;
    }

    public int getMonthEnd() {
        return month_end;
    }

    public int getYearEnd() {
        return year_end;
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

    public void setDayStart(int date) {
        this.day_start = date;
    }

    public void setMonthStart(int date) {
        this.month_start = date;
    }

    public void setYearStart(int date) {
        this.year_start = date;
    }

    public void setDayEnd(int date) {
        this.day_end = date;
    }

    public void setMonthEnd(int date) {
        this.month_end = date;
    }

    public void setYearEnd(int date) {
        this.year_end = date;
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

    public void addTags(List<Tag> tag) {
        int i;
        for(i=0;i<tag.size();i++)
            tags.add(tag.get(i).text);
    }

    public void addFriends(List<User> user) {
        int i;
        friends_info.addAll(user);
        for(i=0;i<user.size();i++)
            friends.add(user.get(i).getEmail());
    }

    public void addWeekDays(Collection<Integer> week) {
        Iterator it = week.iterator();
        while (it.hasNext()) {
            Integer i = (Integer)it.next();
            week_days.add(i);
        }
    }

    public List<Integer> getWeekDays() {
        return week_days;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<User> getFriends() {
        return friends_info;
    }
}
