package io.development.tymo.models.cards;

import io.development.tymo.model_server.Holiday;

public class HolidayCard {

    private Holiday holiday;

    public HolidayCard(Holiday holiday) {
        this.holiday = holiday;
    }

    public Holiday getHoliday() {
        return holiday;
    }

}