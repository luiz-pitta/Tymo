package io.development.tymo.models;

public class ContactModel {
    private String name;
    private String friends_common;
    private String email;
    private int icon;

    public ContactModel(String name, String email, String friends_common, int icon) {
        this.name = name;
        this.email = email;
        this.friends_common = friends_common;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFriendsCommon() {
        return friends_common;
    }

    public int getIcon() {
        return icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFriendsCommon(String friends_common) {
        this.friends_common = friends_common;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}