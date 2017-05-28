package io.development.tymo.model_server;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 2L;

    private int count_common;
    private int count_ask_add;
    private int count_knows;
    private int count_favorite;
    private int i_blocked;
    private int he_blocked;

    private String name;
    private String token;
    private String id_facebook;
    private boolean from_facebook;
    private boolean facebook_messenger_enable;
    private boolean notifications;
    private boolean location_gps;
    private String email;
    private String description;
    private String password;
    private String newPassword;
    private String date_time_account_creation;
    private int day_born;
    private int month_born;
    private int year_born;
    private String gender;
    private String photo;
    private String lives_in;
    private String url;
    private String studied_at;
    private String works_at;
    private String facebook_messenger_link;
    private String last_login_date_time;
    private String last_change_password_date_time;
    private int qty_successfully_logins;
    private int qty_unsuccessfully_logins;
    private int privacy;
    private int invitation;
    private int invited;
    private boolean modify_facebook_name;
    private boolean modify_facebook_photo;
    private boolean adm;
    private boolean creator;
    private List<String> interest = new ArrayList<>();
    private boolean delete = true;

    private List<String> emails = new ArrayList<>();

    public boolean getDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public int getQtySuccessfullyLogins() {
        return qty_successfully_logins;
    }

    public int getIBlocked() {
        return i_blocked;
    }

    public void setIBlocked(int i_blocked) {
        this.i_blocked = i_blocked;
    }

    public void setCountKnows(int count_knows) {
        this.count_knows = count_knows;
        this.count_favorite = count_knows;
        this.count_ask_add = count_knows;
    }

    public void setCountAskAdd(int count_ask_add) {
        this.count_ask_add = count_ask_add;
    }

    public void setCountKnows2(int count_knows) {
        this.count_knows = count_knows;
        this.count_favorite = 0;
        this.count_ask_add = 0;
    }

    public int getHeBlocked() {
        return he_blocked;
    }

    public int getCountAskAdd() {
        return count_ask_add;
    }

    public int getCountKnows() {
        return count_knows;
    }

    public int getInvitation() {
        return invitation;
    }

    public int getInvited() {
        return invited;
    }

    public int getCountCommon() {
        return count_common;
    }

    public int getCountFavorite() {
        return count_favorite;
    }

    public int getDayBorn() {
        return day_born;
    }

    public int getMonthBorn() {
        return month_born;
    }

    public int getYearBorn() {
        return year_born;
    }

    public String getDateTimeAccountCreation() {
        return date_time_account_creation;
    }

    public String getUrl() {
        return url;
    }

    public String getGender() {
        return gender;
    }

    public String getWorksAt() {
        return works_at;
    }

    public String getStudiedAt() {
        return studied_at;
    }

    public String getName() {
        return name;
    }

    public boolean isAdm() {
        return adm;
    }

    public boolean isCreator() {
        return creator;
    }

    public void setAdm(boolean adm) {
        this.adm = adm;
    }

    public void setCreator(boolean creator) {
        this.creator = creator;
    }

    public String getPhoto() {
        return photo;
    }

    public String getIdFacebook() {
        return id_facebook;
    }

    public String getFacebookMessenger() {
        return facebook_messenger_link;
    }

    public String getPassword() {
        return password;
    }

    public void setIdFacebook(String id_facebook) {
        this.id_facebook = id_facebook;
    }

    public String getLivesIn() {
        return lives_in;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFacebookMessengerEnable(boolean facebook_messenger_enable) {
        this.facebook_messenger_enable = facebook_messenger_enable;
    }

    public void setModifyFacebookName(boolean modify_facebook_name) {
        this.modify_facebook_name = modify_facebook_name;
    }

    public void setModifyFacebookPhoto(boolean modify_facebook_photo) {
        this.modify_facebook_photo = modify_facebook_photo;
    }

    public boolean isFacebookMessengerEnable() {
        return facebook_messenger_enable;
    }

    public void setFromFacebook(boolean facebook) {
        this.from_facebook = facebook;
    }

    public boolean getFromFacebook() {
        return from_facebook;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public boolean isLocationGps() {
        return location_gps;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public void setLocationGps(boolean location_gps) {
        this.location_gps = location_gps;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWhereStudy(String where_study) {
        this.studied_at = where_study;
    }

    public void setWhereWork(String where_work) {
        this.works_at = where_work;
    }

    public void setFacebookMessenger(String facebook_messenger) {
        this.facebook_messenger_link = facebook_messenger;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDayBorn(int day_born) {
        this.day_born = day_born;
    }

    public void setMonthBorn(int month_born) {
        this.month_born = month_born;
    }

    public void setYearBorn(int year_born) {
        this.year_born = year_born;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLivesIn(String lives_in) {
        this.lives_in = lives_in;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public void addInterest(String interest) {
        this.interest.add(interest);
    }

    public void addAllInterest(ArrayList<String> list) {
        this.interest.addAll(list);
    }

    public void addEmails(ArrayList<User> list) {
        for(int i = 0; i < list.size(); i++){
            this.emails.add(list.get(i).getEmail());
        }
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }

    public int getPrivacy() {
        return privacy;
    }

    public String getCreatedAt() {
        return date_time_account_creation;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
