package io.development.tymo.model_server;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserPushNotification implements Serializable {
    private static final long serialVersionUID = 37L;

    private String name_device;
    private String token;
    private String email;
    private String id_device;
    private String last_login_date_time;

    public void setToken(String token) {
        this.token = token;
    }

    public void setName(String name_device) {
        this.name_device = name_device;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIdDevice(String id_device) {
        this.id_device = id_device;
    }

    public String getEmail() {
        return email;
    }

}
