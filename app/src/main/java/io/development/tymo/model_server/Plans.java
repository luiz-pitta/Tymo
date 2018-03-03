package io.development.tymo.model_server;

import java.util.ArrayList;

public class Plans {

    private String email;
    private ArrayList<String> emails = new ArrayList<>();

    private int d1;
    private int d1f;
    private int d2;
    private int m;
    private int a;
    private int m2;
    private int a2;
    private String id_device;
    private long date_time_now;

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public long getDateTimeNow() {
        return date_time_now;
    }

    public void setIdDevice(String id_device) {
        this.id_device = id_device;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setD1(int d1) {
        this.d1 = d1;
    }

    public void setD1f(int d1f) {
        this.d1f = d1f;
    }

    public void setD2(int d2) {
        this.d2 = d2;
    }

    public void setM(int m) {
        this.m = m;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void setM2(int m2) {
        this.m2 = m2;
    }

    public void setA2(int a2) {
        this.a2 = a2;
    }

    public void addEmails(String email) {
        emails.add(email);
    }

}
