package io.development.tymo.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class PersonModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int photo;
    private String name;
    private String email;
    private boolean adm;
    private boolean confirmed;

    public PersonModel(int photo, String name, String email) {
        this.photo = photo;
        this.name = name;
        this.email = email;
        this.adm = false;
        this.confirmed = false;
    }

    public int getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean getAdm() {
        return adm;
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdm(boolean adm) {
        this.adm = adm;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setPhoto(int m_photo) {
        this.photo = m_photo;
    }

    public void setName(String m_name) {
        this.name = m_name;
    }

}