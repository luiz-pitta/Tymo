package io.development.tymo.model_server;

import java.util.ArrayList;

public class Response {

    private String message;
    private String token;
    private FlagServer flag;
    private ActivityServer activityServer;
    private ReminderServer reminderServer;
    private User user;
    private ArrayList<ActivityServer> whats_going_act;
    private ArrayList<FlagServer> whats_going_flag;
    private ArrayList<ActivityServer> my_commit_act;
    private ArrayList<FlagServer> my_commit_flag;
    private ArrayList<ReminderServer> my_commit_reminder;
    private ArrayList<User> people;
    private ArrayList<User> adms;
    private ArrayList<TagServer> tags;
    private ArrayList<TagServer> interests;
    private IconServer icon;
    private int n_friend_request;
    private int n_all_request_act;
    private int n_all_request_flag;
    private int n_contacts;

    public ActivityServer getActivityServer() {
        return activityServer;
    }

    public FlagServer getFlag() {
        return flag;
    }

    public ReminderServer getReminderServer() {
        return reminderServer;
    }

    public ArrayList<ActivityServer> getWhatsGoingAct() {
        return whats_going_act;
    }

    public ArrayList<ActivityServer> getMyCommitAct() {
        return my_commit_act;
    }

    public ArrayList<FlagServer> getWhatsGoingFlag() {
        return whats_going_flag;
    }

    public ArrayList<FlagServer> getMyCommitFlag() {
        return my_commit_flag;
    }

    public ArrayList<ReminderServer> getMyCommitReminder() {
        return my_commit_reminder;
    }

    public ArrayList<User> getPeople() {
        return people;
    }

    public ArrayList<User> getAdms() {
        return adms;
    }

    public ArrayList<TagServer> getTags() {
        return tags;
    }

    public ArrayList<TagServer> getInterests() {
        return interests;
    }

    public IconServer getIcon() {
        return icon;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public FlagServer getFlagServer() {
        return flag;
    }

    public User getUser() {
        return user;
    }

    public int getNumberFriendRequest() {
        return n_friend_request;
    }

    public int getNumberContacts() {
        return n_contacts;
    }

    public int getNumberInvitationRequest() {
        return n_all_request_flag+n_all_request_act;
    }
}
