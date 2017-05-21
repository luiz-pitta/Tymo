package io.development.tymo.models;

import io.development.tymo.model_server.User;

public class FriendRequestModel {
    private String text1;
    private String text2;
    private String text3;
    private String email;
    private String photo;
    private User user;
    private boolean requestAccepted;

    public FriendRequestModel(String text1, String text2, String text3, String email, String photo, User user) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.photo = photo;
        this.email = email;
        this.user = user;
        this.requestAccepted = false;
    }

    public User getUser() {
        return user;
    }

    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public String getEmail() {
        return email;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
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

    public String getPhoto() {
        return photo;
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

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}