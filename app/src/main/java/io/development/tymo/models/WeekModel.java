package io.development.tymo.models;

import java.util.ArrayList;
import java.util.List;

public class WeekModel {
    private String m_day_number;
    private String m_day_text;
    private String m_month_text;
    private int day;
    private int month;
    private int year;
    private boolean paint;
    private List<Object> activities = new ArrayList<>();
    private List<Object> free = new ArrayList<>();

    public WeekModel(String m_day_number, String m_day_text, String m_month_text, int day,int month,int year, boolean paint) {
        this.m_day_number = m_day_number;
        this.m_day_text = m_day_text;
        this.m_month_text = m_month_text;

        this.paint = paint;

        this.day = day;
        this.month = month;
        this.year = year;
    }

    public boolean getPaint() {
        return paint;
    }

    public void setPaint(boolean paint) {
        this.paint = paint;
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

    public String getM_day_number() {
        return m_day_number;
    }

    public void setM_day_number(String m_day_number) {
        this.m_day_number = m_day_number;
    }

    public String getM_day_text() {
        return m_day_text;
    }

    public void setM_day_text(String m_day_text) {
        this.m_day_text = m_day_text;
    }

    public String getM_month_text() {
        return m_month_text;
    }

    public void setM_month_text(String m_month_text) {
        this.m_month_text = m_month_text;
    }

    public void addPlans(Object object) {
        activities.add(object);
    }

    public List<Object> getActivities() {
        return activities;
    }

    public void addFree(Object object) {
        free.add(object);
    }

    public void addAllFree(ArrayList<Object> objects) {
        free.addAll(objects);
    }

    public List<Object> getFree() {
        return free;
    }
}