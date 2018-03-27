package io.development.tymo.model_server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlagServer implements Serializable {
    private static final long serialVersionUID = 3L;

    private double rank_points, popularity_points;

    private String email_invited;
    private String creator;
    private String name_inviter;
    private String creator_email;
    private long id;

    private long date_time_now;

    private User user;
    private int count_guest;
    private int know_creator;
    private int favorite_creator;
    private int count_my_contacts;
    private int count_my_favorites;
    private int participates;

    private int status; // -1 = already happened ; 0 = is happening ; 1 = will happen

    private String title;
    private String text;
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
    private boolean date_start_empty;
    private boolean date_end_empty;
    private boolean time_start_empty;
    private boolean time_end_empty;

    private int minute_card;
    private int hour_card;
    private int minute_end_card;
    private int hour_end_card;

    private long date_time_creation;
    private long date_time_start;
    private long date_time_end;
    private long last_date_time;

    private boolean time_start_empty_card;
    private boolean time_end_empty_card;
    private boolean time_start_empty_temp;
    private boolean time_end_empty_temp;

    private int repeat_type;
    private int repeat_qty;
    private List<Integer> repeat_list_accepted = new ArrayList<>();

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
        this.time_start_empty_card = flagServer.getTimeStartEmptyCard();
        this.time_end_empty_card = flagServer.getTimeEndEmptyCard();

        this.repeat_list_accepted = flagServer.getRepeatListAccepted();

        this.title = flagServer.getTitle();
        this.text = flagServer.getText();
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
        this.last_date_time = flagServer.getLastDateTime();
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

        this.rank_points = flagServer.getRankPoints();
        this.popularity_points = flagServer.getPopularityPoints();

        this.date_time_now = flagServer.getDateTimeNow();

        this.date_end_empty = flagServer.getDateEndEmpty();
        this.date_start_empty = flagServer.getDateStartEmpty();
        this.time_end_empty = flagServer.getTimeEndEmpty();
        this.time_start_empty = flagServer.getTimeStartEmpty();
    }

    public void setTimeStartEmptyTemp(boolean time_start_empty_temp) {
        this.time_start_empty_temp = time_start_empty_temp;
    }

    public boolean getTimeStartEmptyTemp() {
        return time_start_empty_temp;
    }

    public void setTimeEndEmptyTemp(boolean time_end_empty_temp) {
        this.time_end_empty_temp = time_end_empty_temp;
    }

    public boolean getTimeEndEmptyTemp() {
        return time_end_empty_temp;
    }

    public void setDateStartEmpty(boolean date_start_empty) {
        this.date_start_empty = date_start_empty;
    }

    public boolean getDateStartEmpty() {
        return date_start_empty;
    }

    public void setDateEndEmpty(boolean date_end_empty) {
        this.date_end_empty = date_end_empty;
    }

    public boolean getDateEndEmpty() {
        return date_end_empty;
    }

    public void setTimeStartEmpty(boolean time_start_empty) {
        this.time_start_empty = time_start_empty;
    }

    public boolean getTimeStartEmpty() {
        return time_start_empty;
    }

    public void setTimeEndEmpty(boolean time_end_empty) {
        this.time_end_empty = time_end_empty;
    }

    public boolean getTimeEndEmpty() {
        return time_end_empty;
    }

    public double getRankPoints() {
        return rank_points;
    }

    public double getPopularityPoints() {
        return popularity_points;
    }

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public long getDateTimeNow() {
        return date_time_now;
    }

    public void setCountMyContacts(int count_my_contacts) {
        this.count_my_contacts = count_my_contacts;
    }

    public void setCountMyFavorites(int count_my_favorites) {
        this.count_my_favorites = count_my_favorites;
    }

    public void setLastDateTime(long last_date_time) {
        this.last_date_time = last_date_time;
    }

    public long getLastDateTime() {
        return last_date_time;
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

    public String getText() {
        return text;
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

    public void setText(String text) {
        this.text = text;
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

    public int getRepeatType() {
        return repeat_type;
    }

    public int getRepeatQty() {
        return repeat_qty;
    }

    public void setTimeStartEmptyCard(boolean time_start_empty_card) {
        this.time_start_empty_card = time_start_empty_card;
    }

    public void setTimeEndEmptyCard(boolean time_end_empty_card) {
        this.time_end_empty_card = time_end_empty_card;
    }

    public boolean getTimeStartEmptyCard() {
        return time_start_empty_card;
    }

    public boolean getTimeEndEmptyCard() {
        return time_end_empty_card;
    }

    public void setRepeatListAccepted(List<Integer> repeat_list_accepted) {
        this.repeat_list_accepted.addAll(repeat_list_accepted);
    }

    public List<Integer> getRepeatListAccepted() {
        return repeat_list_accepted;
    }
}
