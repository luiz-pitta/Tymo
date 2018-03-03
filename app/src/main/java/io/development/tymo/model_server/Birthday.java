package io.development.tymo.model_server;

import java.util.ArrayList;

public class Birthday {

    private ArrayList<User> users_birthday = new ArrayList<>();

    private int day;
    private int month;
    private int year;

    public Birthday(){
    }


    public ArrayList<User> getUsersBirthday() {
        return users_birthday;
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

    public void addBirthday(User user) {
        users_birthday.add(user);
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
