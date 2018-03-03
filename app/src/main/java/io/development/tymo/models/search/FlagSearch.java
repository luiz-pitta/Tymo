package io.development.tymo.models.search;

import io.development.tymo.model_server.FlagServer;

public class FlagSearch {
    private String text1;
    private String text2;
    private String text3;
    private boolean available;
    private FlagServer flagServer;


    public FlagSearch(String text1, String text2, String text3, boolean available, FlagServer flagServer) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.available = available;
        this.flagServer = flagServer;
    }

    public FlagServer getFlagServer() {
        return flagServer;
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

    public boolean getAvailable() {
        return available;
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

    public void setAvailable(boolean available) {
        this.available = available;
    }
}