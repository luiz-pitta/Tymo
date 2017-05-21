package io.development.tymo.models.search;

public class PersonSearch {
    private String text1;
    private String text2;
    private String text3;
    private String email;
    private int icon;

    public PersonSearch(String text1, String text2, String text3, String email, int icon) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.icon = icon;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public String getText3() {
        return text3;
    }

    public int getIcon() {
        return icon;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}