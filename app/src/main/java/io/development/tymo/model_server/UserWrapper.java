package io.development.tymo.model_server;


import java.io.Serializable;
import java.util.ArrayList;

public class UserWrapper implements Serializable {
    private static final long serialVersionUID = 2L;
    private User user;
    private ArrayList<User> ListUsers;

    public UserWrapper(User user) {
        this.user = user;
    }

    public UserWrapper(ArrayList<User> ListUsers) {
        this.ListUsers = ListUsers;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<User> getUsers() {
        return ListUsers;
    }

}