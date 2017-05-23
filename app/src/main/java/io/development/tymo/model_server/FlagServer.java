package io.development.tymo.model_server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlagServer implements Serializable {
    private static final long serialVersionUID = 3L;

    private String email_invited;
    private String creator;
    private String name_inviter;
    private String creator_email;
    private long id;

    private User user;
    private int count_guest;
    private int know_creator;
    private int favorite_creator;
    private int count_my_contacts;
    private int count_my_favorites;
    private int participates;

    private int repeat_id_original_flag;

    private int status; // -1 = already happened ; 0 = is happening ; 1 = will happen

    private String title;
    private boolean type;

    private int day_start;
    private int month_start;
    private int year_start;
    private int day_end;
    private int month_end;
    private int year_end;
    private int minute_start;
    private int hour_start;
    private int minute_end;
    private int hour_end;

    private int minute_card;
    private int hour_card;
    private int minute_end_card;
    private int hour_end_card;

    private long date_time_creation;
    private long date_time_start;
    private long date_time_end;

    private int repeat_type;
    private int repeat_qty;
    private List<Integer> day_list_start = new ArrayList<>();
    private List<Integer> month_list_start = new ArrayList<>();
    private List<Integer> year_list_start = new ArrayList<>();
    private List<Integer> day_list_end = new ArrayList<>();
    private List<Integer> month_list_end = new ArrayList<>();
    private List<Integer> year_list_end = new ArrayList<>();
    private List<Long> date_time_list_start = new ArrayList<>();
    private List<Long> date_time_list_end = new ArrayList<>();

    private Boolean toAll;
    private List<String> guest = new ArrayList();

    private String invite_date;

    public FlagServer() {
    }

    public FlagServer(FlagServer flagServer) {
        this.id = flagServer.getId();
        this.minute_card = flagServer.getMinuteCard();
        this.hour_card = flagServer.getHourCard();
        this.minute_end_card = flagServer.getMinuteEndCard();
        this.hour_end_card = flagServer.getHourEndCard();

        this.title = flagServer.getTitle();
        this.type = flagServer.getType();

        this.count_my_contacts = flagServer.getCountMyContacts();
        this.count_my_favorites = flagServer.getCountMyFavorites();
        this.participates = flagServer.getParticipates();
        this.know_creator = flagServer.getKnowCreator();
        this.favorite_creator = flagServer.getFavoriteCreator();
        this.count_guest = flagServer.getCountGuest();

        this.date_time_creation = flagServer.getDateTimeCreation();
        this.date_time_start = flagServer.getDateTimeStart();
        this.date_time_end = flagServer.getDateTimeEnd();
        this.day_start = flagServer.getDayStart();
        this.month_start = flagServer.getMonthStart();
        this.year_start = flagServer.getYearStart();
        this.day_end = flagServer.getDayEnd();
        this.month_end = flagServer.getMonthEnd();
        this.year_end = flagServer.getYearEnd();
        this.minute_start = flagServer.getMinuteStart();
        this.hour_start = flagServer.getHourStart();
        this.minute_end = flagServer.getMinuteEnd();
        this.hour_end = flagServer.getHourEnd();
        this.status = flagServer.getStatus();

        this.creator = flagServer.getCreator();
        this.creator_email = flagServer.getCreatorEmail();
        this.email_invited = flagServer.getEmailInvited();

        this.repeat_qty = flagServer.getRepeatQty();
        this.repeat_type = flagServer.getRepeatType();

        this.participates = flagServer.getParticipates();
        this.know_creator = flagServer.getKnowCreator();
    }

    public void setCountMyContacts(int count_my_contacts) {
        this.count_my_contacts = count_my_contacts;
    }

    public void setCountMyFavorites(int count_my_favorites) {
        this.count_my_favorites = count_my_favorites;
    }

    public int getCountMyContacts() {
        return count_my_contacts;
    }

    public int getCountMyFavorites() {
        return count_my_favorites;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDateTimeCreation() {
        return date_time_creation;
    }

    public String getInviteDate() {
        return invite_date;
    }

    public String getEmailInvited() {
        return email_invited;
    }

    public int getCountGuest() {
        return count_guest;
    }

    public int getKnowCreator() {
        return know_creator;
    }

    public void setKnowCreator(int know_creator) {
        this.know_creator = know_creator;
    }

    public int getFavoriteCreator() {
        return favorite_creator;
    }

    public int getParticipates() {
        return participates;
    }

    public User getUser() {
        return user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNameInviter() {
        return name_inviter;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatorEmail() {
        return creator_email;
    }

    public boolean getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getDayStart() {
        return day_start;
    }

    public int getMonthStart() {
        return month_start;
    }

    public int getYearStart() {
        return year_start;
    }

    public int getDayEnd() {
        return day_end;
    }

    public int getMonthEnd() {
        return month_end;
    }

    public int getYearEnd() {
        return year_end;
    }

    public int getMinuteStart() {
        return minute_start;
    }

    public int getHourStart() {
        return hour_start;
    }

    public int getMinuteEnd() {
        return minute_end;
    }

    public int getHourEnd() {
        return hour_end;
    }

    public int getMinuteCard() {
        return minute_card;
    }

    public int getHourCard() {
        return hour_card;
    }

    public int getMinuteEndCard() {
        return minute_end_card;
    }

    public int getHourEndCard() {
        return hour_end_card;
    }

    public void setDateTimeCreation(long date_time_creation) {
        this.date_time_creation = date_time_creation;
    }

    public void setDateTimeStart(long date_time_start) {
        this.date_time_start = date_time_start;
    }

    public void setDateTimeEnd(long date_time_end) {
        this.date_time_end = date_time_end;
    }

    public long getDateTimeStart() {
        return date_time_start;
    }

    public long getDateTimeEnd() {
        return date_time_end;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCreatorEmail(String creator_email) {
        this.creator_email = creator_email;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public void setRepeatQty(int repeat_qty) {
        this.repeat_qty = repeat_qty;
    }

    public void setDayStart(int date) {
        this.day_start = date;
    }

    public void setMonthStart(int date) {
        this.month_start = date;
    }

    public void setYearStart(int date) {
        this.year_start = date;
    }

    public void setDayEnd(int date) {
        this.day_end = date;
    }

    public void setMonthEnd(int date) {
        this.month_end = date;
    }

    public void setYearEnd(int date) {
        this.year_end = date;
    }

    public void setMinuteStart(int time) {
        this.minute_start = time;
    }

    public void setMinuteEnd(int time) {
        this.minute_end = time;
    }

    public void setHourStart(int time) {
        this.hour_start = time;
    }

    public void setHourEnd(int time) {
        this.hour_end = time;
    }

    public void setMinuteCard(int time) {
        this.minute_card = time;
    }

    public void setMinuteEndCard(int time) {
        this.minute_end_card = time;
    }

    public void setHourCard(int time) {
        this.hour_card = time;
    }

    public void setHourEndCard(int time) {
        this.hour_end_card = time;
    }

    public void setRepeatType(int repeat_type) {
        this.repeat_type = repeat_type;
    }

    public void addGuest(String name) {
        guest.add(name);
    }

    public void setToAll(Boolean toAll) {
        this.toAll = toAll;
    }

    public void setDateTimeListStart(List<Long> list) {
        this.date_time_list_start.addAll(list);
    }

    public void setDateTimeListEnd(List<Long> list) {
        this.date_time_list_end.addAll(list);
    }

    public void setDayListStart(List<Integer> list) {
        this.day_list_start.addAll(list);
    }

    public void setMonthListStart(List<Integer> list) {
        this.month_list_start.addAll(list);
    }

    public void setYearListStart(List<Integer> list) {
        this.year_list_start.addAll(list);
    }

    public void setDayListEnd(List<Integer> list) {
        this.day_list_end.addAll(list);
    }

    public void setMonthListEnd(List<Integer> list) {
        this.month_list_end.addAll(list);
    }

    public void setYearListEnd(List<Integer> list) {
        this.year_list_end.addAll(list);
    }

    public int getRepeatType() {
        return repeat_type;
    }

    public int getRepeatQty() {
        return repeat_qty;
    }
}
