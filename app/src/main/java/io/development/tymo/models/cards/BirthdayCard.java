package io.development.tymo.models.cards;

import io.development.tymo.model_server.Birthday;

public class BirthdayCard {

    private Birthday birthday;

    public BirthdayCard(Birthday birthday) {
        this.birthday = birthday;
    }

    public Birthday getBirthday() {
        return birthday;
    }
}