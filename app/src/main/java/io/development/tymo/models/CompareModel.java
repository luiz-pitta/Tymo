package io.development.tymo.models;

import java.util.ArrayList;
import java.util.List;

public class CompareModel {
    private String m_photo;
    private String name, email;
    private List<Object> activities = new ArrayList<>();
    private List<Object> free = new ArrayList<>();


    public CompareModel(String m_photo, String name, String email) {
        this.m_photo = m_photo;
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return m_photo;
    }

    public void setPhoto(String m_photo) {
        this.m_photo = m_photo;
    }

    public void addPlans(Object object) {
        activities.add(object);
    }

    public List<Object> getActivities() {
        return activities;
    }

    public void addFree(Object object) {
        free.add(object);
    }

    public void addAllFree(ArrayList<Object> objects) {
        free.addAll(objects);
    }

    public List<Object> getFree() {
        return free;
    }


}