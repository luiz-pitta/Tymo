package io.development.tymo.models;


import java.io.Serializable;
import java.util.ArrayList;

import io.development.tymo.model_server.User;

public class PersonModelWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<User> personModels;

    public PersonModelWrapper(ArrayList<User> items) {
        this.personModels = items;
    }

    public ArrayList<User> getItemDetails() {
        return personModels;
    }

}