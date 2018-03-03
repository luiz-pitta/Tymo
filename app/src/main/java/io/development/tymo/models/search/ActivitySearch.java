package io.development.tymo.models.search;

import io.development.tymo.model_server.ActivityServer;

public class ActivitySearch {
    private String text1;
    private String text2;
    private String text3;
    private int cubeTop;
    private int cubeCenter;
    private String cubeIcon;
    private ActivityServer activityServer;

    public ActivitySearch(String text1, String text2, String text3, int cubeTop, int cubeCenter, String cubeIcon, ActivityServer activityServer) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.cubeTop = cubeTop;
        this.cubeCenter = cubeCenter;
        this.cubeIcon = cubeIcon;
        this.activityServer = activityServer;
    }

    public ActivityServer getActivityServer() {
        return activityServer;
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

    public int getCubeTop() {
        return cubeTop;
    }

    public int getCubeCenter() {
        return cubeCenter;
    }

    public String getCubeIcon() {
        return cubeIcon;
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

    public void setCubeTop(int cubeTop) {
        this.cubeTop = cubeTop;
    }

    public void setCubeCenter(int cubeCenter) {
        this.cubeCenter = cubeCenter;
    }

    public void setCubeIcon(String cubeIcon) {
        this.cubeIcon = cubeIcon;
    }

}