package io.development.tymo.models;

public class InviteModel {
    private String text1;
    private String text2;
    private String text3;
    private String icon;
    private int colorUpper;
    private int colorLower;
    private Object activity;
    private boolean inviteAccepted;

    public InviteModel(String text1, String text2, String text3, String icon, int colorUpper, int colorLower, Object activity) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.icon = icon;
        this.colorUpper = colorUpper;
        this.colorLower = colorLower;
        this.activity = activity;
        this.inviteAccepted = false;
    }

    public Object getActivity() {
        return activity;
    }

    public boolean isInviteAccepted() {
        return inviteAccepted;
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

    public String getIcon() {
        return icon;
    }

    public void setInviteAccepted(boolean inviteAccepted) {
        this.inviteAccepted = inviteAccepted;
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