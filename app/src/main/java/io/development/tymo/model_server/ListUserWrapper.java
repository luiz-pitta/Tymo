package io.development.tymo.model_server;


import java.io.Serializable;
import java.util.ArrayList;

public class ListUserWrapper implements Serializable {
    private static final long serialVersionUID = 399L;
    private ArrayList<User> listUser;

    public ListUserWrapper(ArrayList<User> listUser) {
        this.listUser = listUser;
    }

    public ArrayList<User> getListUser() {
        return listUser;
    }

}