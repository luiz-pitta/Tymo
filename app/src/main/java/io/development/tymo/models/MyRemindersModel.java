package io.development.tymo.models;

public class MyRemindersModel {
    private String text1;
    private String text2;
    private String text3;
    private Object activity;

    public MyRemindersModel(String text1, String text2, Object activity) {
        this.text1 = text1;
        this.text2 = text2;
        this.activity = activity;
    }

    public Object getActivity() {
        return activity;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }
}