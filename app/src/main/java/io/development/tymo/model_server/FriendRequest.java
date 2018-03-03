package io.development.tymo.model_server;

public class FriendRequest {

    private String email;
    private String email_friend;
    private int status;
    private long date_time_now;

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public long getDateTimeNow() {
        return date_time_now;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailFriend(String email_friend) {
        this.email_friend = email_friend;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
