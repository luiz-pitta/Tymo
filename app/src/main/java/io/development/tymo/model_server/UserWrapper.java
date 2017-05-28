package io.development.tymo.model_server;


import java.io.Serializable;

public class UserWrapper implements Serializable {
    private static final long serialVersionUID = 2L;
    private User user;

    public UserWrapper(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}