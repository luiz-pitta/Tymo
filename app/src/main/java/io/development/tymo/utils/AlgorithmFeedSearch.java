package io.development.tymo.utils;

import android.content.Context;
import android.location.LocationManager;

import java.util.Calendar;

import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.models.search.ActivitySearch;
import io.development.tymo.models.search.FlagSearch;
import io.development.tymo.models.search.ReminderSearch;

public class AlgorithmFeedSearch {

    public static int runAlgorithmFeedSearch(Object c1, Object c2, boolean orderByProximity, boolean orderByPopularity, boolean orderByDateHour, Context context) {
        ActivityServer activityServer;
        FlagServer flagServer;
        ReminderServer reminderServer;

        int start_hour = 0, start_minute = 0, start_hour2 = 0, start_minute2 = 0;
        int start_day = 0, start_month = 0, start_year = 0, start_day2 = 0, start_month2 = 0, start_year2 = 0;
        int end_hour = 0, end_minute = 0, end_hour2 = 0, end_minute2 = 0;
        int end_day = 0, end_month = 0, end_year = 0, end_day2 = 0, end_month2 = 0, end_year2 = 0;

        int count_interests = 0, count_interests2 = 0;
        int count_my_contacts = 0, count_my_contacts2 = 0;
        int count_my_favorites = 0, count_my_favorites2 = 0;
        int count_my_contacts_final = 0, count_my_contacts_final2 = 0;
        int know_creator = 0, know_creator2 = 0, favorite_creator = 0, favorite_creator2 = 0;

        long start_date_time = 0, start_date_time2 = 0;
        long end_date_time = 0, end_date_time2 = 0;
        long creation_time = 0, creation_time2 = 0;
        long time_left_to_start = 0, time_left_to_start2 = 0;
        long time_left_to_end = 0, time_left_to_end2 = 0;
        long created_for_how_long = 0, created_for_how_long2 = 0;

        double distance = -1, distance2 = -1;
        double lat = -500, lng = -500, lat2 = -500, lng2 = -500;

        Calendar calendar = Calendar.getInstance();
        long nowTime = calendar.getTimeInMillis();

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (c1 instanceof ActivityServer || c1 instanceof ActivitySearch) {
            if (c1 instanceof ActivityServer)
                activityServer = (ActivityServer) c1;
            else
                activityServer = ((ActivitySearch) c1).getActivityServer();

            count_interests = activityServer.getCountInterests();
            count_my_contacts = activityServer.getCountMyContacts();
            count_my_favorites = activityServer.getCountMyFavorites();
            count_my_contacts_final = count_my_contacts + count_my_favorites;
            know_creator = activityServer.getKnowCreator();
            favorite_creator = activityServer.getFavoriteCreator();

            start_hour = activityServer.getHourStart();
            start_minute = activityServer.getMinuteStart();
            start_day = activityServer.getDayStart();
            start_month = activityServer.getMonthStart();
            start_year = activityServer.getYearStart();
            end_hour = activityServer.getHourEnd();
            end_minute = activityServer.getMinuteEnd();
            end_day = activityServer.getDayEnd();
            end_month = activityServer.getMonthEnd();
            end_year = activityServer.getYearEnd();

            creation_time = activityServer.getDateTimeCreation();
            created_for_how_long = nowTime - creation_time;

            start_date_time = activityServer.getDateTimeStart();
            time_left_to_start = start_date_time - nowTime;

            end_date_time = activityServer.getDateTimeEnd();
            time_left_to_end = end_date_time - nowTime;

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !(activityServer.getLat() == -500 || (activityServer.getLat() == 0 && activityServer.getLng() == 0))) {
                lat = TymoApplication.getInstance().getLatLng().get(0);
                lng = TymoApplication.getInstance().getLatLng().get(1);
                distance = Utilities.distance(activityServer.getLat(), activityServer.getLng(), lat, lng);
            }
        }
        else if (c1 instanceof FlagServer || c1 instanceof FlagSearch) {
            if (c1 instanceof FlagServer)
                flagServer = (FlagServer) c1;
            else
                flagServer = ((FlagSearch) c1).getFlagServer();

            count_my_contacts = flagServer.getCountMyContacts();
            count_my_favorites = flagServer.getCountMyFavorites();
            count_my_contacts_final = count_my_contacts + count_my_favorites;
            know_creator = flagServer.getKnowCreator();
            favorite_creator = flagServer.getFavoriteCreator();

            start_hour = flagServer.getHourStart();
            start_minute = flagServer.getMinuteStart();
            start_day = flagServer.getDayStart();
            start_month = flagServer.getMonthStart();
            start_year = flagServer.getYearStart();
            end_hour = flagServer.getHourEnd();
            end_minute = flagServer.getMinuteEnd();
            end_day = flagServer.getDayEnd();
            end_month = flagServer.getMonthEnd();
            end_year = flagServer.getYearEnd();

            creation_time = flagServer.getDateTimeCreation();
            created_for_how_long = nowTime - creation_time;

            start_date_time = flagServer.getDateTimeStart();
            time_left_to_start = start_date_time - nowTime;

            end_date_time = flagServer.getDateTimeEnd();
            time_left_to_end = end_date_time - nowTime;
        }
        else if (c1 instanceof ReminderServer || c1 instanceof ReminderSearch) {
            if (c1 instanceof ReminderServer)
                reminderServer = (ReminderServer) c1;
            else
                reminderServer = ((ReminderSearch) c1).getReminderServer();

            start_hour = reminderServer.getHourStart();
            start_minute = reminderServer.getMinuteStart();
            start_day = reminderServer.getDayStart();
            start_month = reminderServer.getMonthStart();
            start_year = reminderServer.getYearStart();
            end_hour = start_hour;
            end_minute = start_minute;
            end_day = start_day;
            end_month = start_month;
            end_year = start_year;

            creation_time = reminderServer.getDateTimeCreation();
            created_for_how_long = nowTime - creation_time;

            start_date_time = reminderServer.getDateTimeStart();
            time_left_to_start = start_date_time - nowTime;

            end_date_time = reminderServer.getDateTimeStart();
            time_left_to_end = end_date_time - nowTime;
        }

        if (c2 instanceof ActivityServer || c2 instanceof ActivitySearch) {
            if (c2 instanceof ActivityServer)
                activityServer = (ActivityServer) c2;
            else
                activityServer = ((ActivitySearch) c2).getActivityServer();

            count_interests2 = activityServer.getCountInterests();
            count_my_contacts2 = activityServer.getCountMyContacts();
            count_my_favorites2 = activityServer.getCountMyFavorites();
            count_my_contacts_final2 = count_my_contacts2 + count_my_favorites2;
            know_creator2 = activityServer.getKnowCreator();
            favorite_creator2 = activityServer.getFavoriteCreator();

            start_hour2 = activityServer.getHourStart();
            start_minute2 = activityServer.getMinuteStart();
            start_day2 = activityServer.getDayStart();
            start_month2 = activityServer.getMonthStart();
            start_year2 = activityServer.getYearStart();
            end_hour2 = activityServer.getHourEnd();
            end_minute2 = activityServer.getMinuteEnd();
            end_day2 = activityServer.getDayEnd();
            end_month2 = activityServer.getMonthEnd();
            end_year2 = activityServer.getYearEnd();

            creation_time2 = activityServer.getDateTimeCreation();
            created_for_how_long2 = nowTime - creation_time2;

            start_date_time2 = activityServer.getDateTimeStart();
            time_left_to_start2 = start_date_time2 - nowTime;

            end_date_time2 = activityServer.getDateTimeEnd();
            time_left_to_end2 = end_date_time2 - nowTime;

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !(activityServer.getLat() == -500 || (activityServer.getLat() == 0 && activityServer.getLng() == 0))) {
                lat2 = TymoApplication.getInstance().getLatLng().get(0);
                lng2 = TymoApplication.getInstance().getLatLng().get(1);
                distance2 = Utilities.distance(activityServer.getLat(), activityServer.getLng(), lat2, lng2);
            }
        }
        else if (c2 instanceof FlagServer || c2 instanceof FlagSearch) {
            if (c2 instanceof FlagServer)
                flagServer = (FlagServer) c2;
            else
                flagServer = ((FlagSearch) c2).getFlagServer();

            count_my_contacts2 = flagServer.getCountMyContacts();
            count_my_favorites2 = flagServer.getCountMyFavorites();
            count_my_contacts_final2 = count_my_contacts2 + count_my_favorites2;
            know_creator2 = flagServer.getKnowCreator();
            favorite_creator2 = flagServer.getFavoriteCreator();

            start_hour2 = flagServer.getHourStart();
            start_minute2 = flagServer.getMinuteStart();
            start_day2 = flagServer.getDayStart();
            start_month2 = flagServer.getMonthStart();
            start_year2 = flagServer.getYearStart();
            end_hour2 = flagServer.getHourEnd();
            end_minute2 = flagServer.getMinuteEnd();
            end_day2 = flagServer.getDayEnd();
            end_month2 = flagServer.getMonthEnd();
            end_year2 = flagServer.getYearEnd();

            creation_time2 = flagServer.getDateTimeCreation();
            created_for_how_long2 = nowTime - creation_time2;

            start_date_time2 = flagServer.getDateTimeStart();
            time_left_to_start2 = start_date_time2 - nowTime;

            end_date_time2 = flagServer.getDateTimeEnd();
            time_left_to_end2 = end_date_time2 - nowTime;
        }
        else if (c2 instanceof ReminderServer || c2 instanceof ReminderSearch) {
            if (c2 instanceof ReminderServer)
                reminderServer = (ReminderServer) c2;
            else
                reminderServer = ((ReminderSearch) c2).getReminderServer();

            start_hour2 = reminderServer.getHourStart();
            start_minute2 = reminderServer.getMinuteStart();
            start_day2 = reminderServer.getDayStart();
            start_month2 = reminderServer.getMonthStart();
            start_year2 = reminderServer.getYearStart();
            end_hour = start_hour;
            end_minute = start_minute;
            end_day = start_day;
            end_month = start_month;
            end_year = start_year;

            creation_time2 = reminderServer.getDateTimeCreation();
            created_for_how_long2 = nowTime - creation_time2;

            start_date_time2 = reminderServer.getDateTimeStart();
            time_left_to_start2 = start_date_time2 - nowTime;

            end_date_time2 = reminderServer.getDateTimeStart();
            time_left_to_end2 = end_date_time2 - nowTime;
        }



        long millis_one_hour = 60 * 60 * 1000;
        long millis_twelve_hours = 12 * millis_one_hour;
        long millis_one_day = 24 * millis_one_hour;
        long millis_one_week = 7 * millis_one_day;
        long millis_one_month = 30 * millis_one_day;
        long millis_three_months = 3 * millis_one_month;

        /*******************************************************************************
         * ALGORITMO DE EXIBIÇÃO DE PEÇAS
         *
         * PRIORIDADE          ATRIBUTO                CONDIÇÃO
         *      1              Presente e Futuro       -
         *      2              Local                   filtrado (Proximidade)
         *      3              Nº de contatos          filtrado (Popularidade)
         *      4              Data/Hora               filtrado (Data e Hora)
         *      5              Criação                 criado há menos de 1 hora
         *      6              Data/Hora               há menos de 1 semana para acontecer
         *      7              Criação                 criado há menos de 12 horas
         *      8              Data/Hora               há menos de 1 mês para acontecer
         *      9              Interesses              -
         *      10             Criação                 criado há menos de 24 horas
         *      11             Nº de contatos          NÃO filtrado (Popularidade)
         *      12             Criador Favorito        -
         *      13             Criador Amigo           -
         *      14             Local                   NÃO filtrado (Proximidade)
         *      15             Data/Hora               NÃO filtrado (Data e Hora)
         *******************************************************************************/

        if (time_left_to_end >= 0 && time_left_to_end2 < 0)
            return -1;
        else if (time_left_to_end < 0 && time_left_to_end2 >= 0)
            return 1;
        else if (start_date_time > start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return -1;
        else if (start_date_time < start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return 1;
        else if (distance >= 0 && distance2 < 0 && orderByProximity == true)
            return -1;
        else if (distance < 0 && distance2 >= 0 && orderByProximity == true)
            return 1;
        else if (distance < distance2 && orderByProximity == true)
            return -1;
        else if (distance > distance2 && orderByProximity == true)
            return 1;
        else if (count_my_contacts_final > count_my_contacts_final2 && orderByPopularity == true)
            return -1;
        else if (count_my_contacts_final < count_my_contacts_final2 && orderByPopularity == true)
            return 1;
        else if (start_date_time < start_date_time2 && orderByDateHour == true)
            return -1;
        else if (start_date_time > start_date_time2 && orderByDateHour == true)
            return 1;
        else if (created_for_how_long < created_for_how_long2 && created_for_how_long < millis_one_hour)
            return -1;
        else if (created_for_how_long > created_for_how_long2 && created_for_how_long2 < millis_one_hour)
            return 1;
        else if (start_date_time < start_date_time2 && time_left_to_start < millis_one_week)
            return -1;
        else if (start_date_time > start_date_time2 && time_left_to_start2 < millis_one_week)
            return 1;
        else if (created_for_how_long < created_for_how_long2 && created_for_how_long < millis_twelve_hours)
            return -1;
        else if (created_for_how_long > created_for_how_long2 && created_for_how_long2 < millis_twelve_hours)
            return 1;
        else if (start_date_time < start_date_time2 && time_left_to_start < millis_one_month)
            return -1;
        else if (start_date_time > start_date_time2 && time_left_to_start2 < millis_one_month)
            return 1;
        else if (count_interests > count_interests2)
            return -1;
        else if (count_interests < count_interests2)
            return 1;
        else if (created_for_how_long < created_for_how_long2 && created_for_how_long < millis_one_day)
            return -1;
        else if (created_for_how_long > created_for_how_long2 && created_for_how_long2 < millis_one_day)
            return 1;
        else if (count_my_contacts_final > count_my_contacts_final2 && orderByPopularity == false)
            return -1;
        else if (count_my_contacts_final < count_my_contacts_final2 && orderByPopularity == false)
            return 1;
        else if (favorite_creator > favorite_creator2)
            return -1;
        else if (favorite_creator < favorite_creator2)
            return 1;
        else if (know_creator > know_creator2)
            return -1;
        else if (know_creator < know_creator2)
            return 1;
        else if (distance >= 0 && distance2 < 0 && orderByProximity == false)
            return -1;
        else if (distance < 0 && distance2 >= 0 && orderByProximity == false)
            return 1;
        else if (distance < distance2 && orderByProximity == false)
            return -1;
        else if (distance > distance2 && orderByProximity == false)
            return 1;
        else if (start_date_time < start_date_time2 && orderByDateHour == false)
            return -1;
        else if (start_date_time > start_date_time2 && orderByDateHour == false)
            return 1;
        else
            return 0;
    }
}
