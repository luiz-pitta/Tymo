package io.development.tymo.models;

public class NotificationModel {
    private String text1;
    private String text2;
    private String text3;
    private String text4;
    private String icon;
    private int colorUpper;
    private int colorLower;
    private Object activity;

    public NotificationModel(String text1, String text2,  String text4, String icon, int colorUpper, int colorLower, Object activity) {
        this.text1 = text1;
        this.text2 = text2;
        this.text4 = text4;
        this.icon = icon;
        this.colorUpper = colorUpper;
        this.colorLower = colorLower;
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

    public String getText4() {
        return text4;
    }

    public String getIcon() {
        return icon;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getColorLower() {
        return colorLower;
    }

    public void setColorUpper(int colorUpper) {
        this.colorUpper = colorUpper;
    }

    public int getColorUpper() {
        return colorUpper;
    }

    public void setColorLower(int colorLower) {
        this.colorLower = colorLower;
    }
}