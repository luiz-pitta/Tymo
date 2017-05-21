package io.development.tymo.model_server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Holiday {

    private ArrayList<String> holidays = new ArrayList<>();

    private int day;
    private int month;
    private int year;

    public Holiday(){
    }


    public ArrayList<String> getHolidays() {
        return holidays;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public void addHoliday(String holiday) {
        holidays.add(holiday);
    }

    public void setDay(int date) {
        this.day = date;
    }

    public void setMonth(int date) {
        this.month = date;
    }

    public void setYear(int date) {
        this.year = date;
    }
}
