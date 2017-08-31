package io.development.tymo.model_server;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActivityServer implements Serializable {
    private static final long serialVersionUID = 4L;

    private double rank_points, popularity_points, distance;

    private String email_invited;
    private String creator;
    private String name_inviter;
    private String creator_email;
    private long id;
    private long id_facebook;
    private String id_google;

    boolean deleted_activity_imported = false;

    private long date_time_now;

    private User user;
    private int count_guest;
    private int know_creator;
    private int favorite_creator;
    private int count_interests;
    private int count_my_contacts;
    private int count_my_favorites;
    private int participates;

    private long date_time_creation;
    private long date_time_start;
    private long date_time_end;

    private String title;
    private String description;
    private String location;
    private int invitation_type;

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

    private int cube_color;
    private int cube_color_upper;
    private String cube_icon;

    private List<String> tags = new ArrayList();
    private List<String> guest = new ArrayList();
    private List<String> adms = new ArrayList();
    private String whatsapp_group_link;
    private int visibility;

    private double lat;
    private double lng;

    private int status; // -1 = already happened ; 0 = is happening ; 1 = will happen

    private String invite_date;

    private ArrayList<ActivityServer> listRepeatedActvities = new ArrayList<>();

    public ActivityServer() {
    }

    public ActivityServer(ActivityServer activityServer) {
        this.id = activityServer.getId();
        this.minute_card = activityServer.getMinuteCard();
        this.hour_card = activityServer.getHourCard();
        this.minute_end_card = activityServer.getMinuteEndCard();
        this.hour_end_card = activityServer.getHourEndCard();

        this.title = activityServer.getTitle();

        this.invitation_type = activityServer.getInvitationType();
        this.date_time_creation = activityServer.getDateTimeCreation();
        this.date_time_start = activityServer.getDateTimeStart();
        this.date_time_end = activityServer.getDateTimeEnd();
        this.day_start = activityServer.getDayStart();
        this.month_start = activityServer.getMonthStart();
        this.year_start = activityServer.getYearStart();
        this.day_end = activityServer.getDayEnd();
        this.month_end = activityServer.getMonthEnd();
        this.year_end = activityServer.getYearEnd();
        this.minute_start = activityServer.getMinuteStart();
        this.hour_start = activityServer.getHourStart();
        this.minute_end = activityServer.getMinuteEnd();
        this.hour_end = activityServer.getHourEnd();

        this.description = activityServer.getDescription();

        this.cube_color = activityServer.getCubeColor();
        this.cube_color_upper = activityServer.getCubeColorUpper();
        this.cube_icon = activityServer.getCubeIcon();

        this.creator = activityServer.getCreator();
        this.creator_email = activityServer.getCreatorEmail();
        this.email_invited = activityServer.getEmailInvited();
        this.count_interests = activityServer.getCountInterests();
        this.count_my_contacts = activityServer.getCountMyContacts();
        this.count_my_favorites = activityServer.getCountMyFavorites();
        this.status = activityServer.getStatus();

        this.whatsapp_group_link = activityServer.getWhatsappGroupLink();

        this.location = activityServer.getLocation();
        this.lat = activityServer.getLat();
        this.lng = activityServer.getLng();

        this.repeat_qty = activityServer.getRepeatQty();
        this.repeat_type = activityServer.getRepeatType();

        this.participates = activityServer.getParticipates();
        this.visibility = activityServer.getVisibility();
        this.know_creator = activityServer.getKnowCreator();
        this.favorite_creator = activityServer.getFavoriteCreator();
        this.count_guest = activityServer.getCountGuest();

        this.id_google = activityServer.getIdGoogle();
        this.id_facebook = activityServer.getIdFacebook();

        this.rank_points = activityServer.getRankPoints();
        this.popularity_points = activityServer.getPopularityPoints();
        this.distance = activityServer.getDistance();

        this.date_time_now = activityServer.getDateTimeNow();

        this.date_end_empty = activityServer.getDateEndEmpty();
        this.date_start_empty = activityServer.getDateStartEmpty();
        this.time_end_empty = activityServer.getTimeEndEmpty();
        this.time_start_empty = activityServer.getTimeStartEmpty();
    }

    public boolean isDeletedActivityImported() {
        return deleted_activity_imported;
    }

    public void setDeletedActivityImported(boolean deleted_activity_imported) {
        this.deleted_activity_imported = deleted_activity_imported;
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

    public ArrayList<ActivityServer> getListRepeatedActvities() {
        return listRepeatedActvities;
    }

    public double getRankPoints() {
        return rank_points;
    }

    public double getPopularityPoints() {
        return popularity_points;
    }

    public double getDistance() {
        return distance;
    }

    public void setDateTimeNow(long date_time_now) {
        this.date_time_now = date_time_now;
    }

    public long getDateTimeNow() {
        return date_time_now;
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

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getCountGuest() {
        return count_guest;
    }

    public int getKnowCreator() {
        return know_creator;
    }

    public int getCountMyContacts() {
        return count_my_contacts;
    }

    public int getCountMyFavorites() {
        return count_my_favorites;
    }

    public int getCountInterests() {
        return count_interests;
    }

    public void setKnowCreator(int know_creator) {
        this.know_creator = know_creator;
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

    public void setCountMyContacts(int count_my_contacts) {
        this.count_my_contacts = count_my_contacts;
    }

    public void setCountMyFavorites(int count_my_favorites) {
        this.count_my_favorites = count_my_favorites;
    }

    public void setCountInterests(int count_interests) {
        this.count_interests = count_interests;
    }

    public int getFavoriteCreator() {
        return favorite_creator;
    }

    public int getParticipates() {
        return participates;
    }

    public int getRepeatType() {
        return repeat_type;
    }

    public int getRepeatQty() {
        return repeat_qty;
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

    public long getIdFacebook() {
        return id_facebook;
    }

    public void setIdFacebook(long id_facebook) {
        this.id_facebook = id_facebook;
    }

    public String getIdGoogle() {
        return id_google;
    }

    public void setIdGoogle(String id_google) {
        this.id_google = id_google;
    }

    public String getEmailInvited() {
        return email_invited;
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

    public int getVisibility() {
        return visibility;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getWhatsappGroupLink() {
        return whatsapp_group_link;
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

    public int getCubeColor() {
        return cube_color;
    }

    public int getCubeColorUpper() {
        return cube_color_upper;
    }

    public String getCubeIcon() {
        return cube_icon;
    }

    public int getInvitationType() {
        return invitation_type;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
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

    public void setRepeatQty(int repeat_qty) {
        this.repeat_qty = repeat_qty;
    }

    public void setCubeColor(int cube_color) {
        this.cube_color = cube_color;
    }

    public void setCubeColorUpper(int cube_color_upper) {
        this.cube_color_upper = cube_color_upper;
    }

    public void setCubeIcon(String cube_icon) {
        this.cube_icon = cube_icon;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public void addTags(String name) {
        tags.add(name);
    }

    public void addGuest(String name) {
        guest.add(name);
    }

    public void addAdms(String name) {
        adms.add(name);
    }

    public void setInvitationType(int invitation_type) {
        this.invitation_type = invitation_type;
    }

    public void setWhatsappGroupLink(String whatsapp_group_link) {
        this.whatsapp_group_link = whatsapp_group_link;
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

    public void setDateTimeListStart(List<Long> list) {
        this.date_time_list_start.addAll(list);
    }

    public void setDateTimeListEnd(List<Long> list) {
        this.date_time_list_end.addAll(list);
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;

        if (obj instanceof String){
            equal = obj == this.id_google;
        }else if(obj instanceof ActivityServer){
            long id = ((ActivityServer)obj).getId();
            equal = id == this.id;
        }

        return equal;
    }
}
