package io.development.tymo.utils;

import android.content.Context;
import android.location.LocationManager;

import java.util.Calendar;

import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.User;
import io.development.tymo.models.search.ActivitySearch;
import io.development.tymo.models.search.FlagSearch;
import io.development.tymo.models.search.ReminderSearch;

public class AlgorithmFeedSearch {

    public static int algorithmFeedSearchWhats(Object c1, Object c2, boolean orderByProximity, boolean orderByPopularity, boolean orderByDateHour, Context context) {
        ActivityServer activityServer;
        FlagServer flagServer;

        long start_date_time = 0, start_date_time2 = 0;
        long end_date_time = 0, end_date_time2 = 0;
        long time_left_to_end = 0, time_left_to_end2 = 0;

        double rank_points = 0, rank_points2 = 0;
        double popularity_points = 0, popularity_points2 = 0;
        double distance = -1, distance2 = -1;

        Calendar calendar = Calendar.getInstance();
        long nowTime = calendar.getTimeInMillis();

        if (c1 instanceof ActivityServer || c1 instanceof ActivitySearch) {
            if (c1 instanceof ActivityServer)
                activityServer = (ActivityServer) c1;
            else
                activityServer = ((ActivitySearch) c1).getActivityServer();

            start_date_time = activityServer.getDateTimeStart();

            end_date_time = activityServer.getLastDateTime();
            time_left_to_end = end_date_time - nowTime;

            popularity_points = activityServer.getPopularityPoints();
            rank_points = activityServer.getRankPoints();
            distance = activityServer.getDistance();

        }
        else if (c1 instanceof FlagServer || c1 instanceof FlagSearch) {
            if (c1 instanceof FlagServer)
                flagServer = (FlagServer) c1;
            else
                flagServer = ((FlagSearch) c1).getFlagServer();

            start_date_time = flagServer.getDateTimeStart();

            end_date_time = flagServer.getLastDateTime();
            time_left_to_end = end_date_time - nowTime;

            popularity_points = flagServer.getPopularityPoints();
            rank_points = flagServer.getRankPoints();

        }

        if (c2 instanceof ActivityServer || c2 instanceof ActivitySearch) {
            if (c2 instanceof ActivityServer)
                activityServer = (ActivityServer) c2;
            else
                activityServer = ((ActivitySearch) c2).getActivityServer();

            start_date_time2 = activityServer.getDateTimeStart();

            end_date_time2 = activityServer.getLastDateTime();
            time_left_to_end2 = end_date_time2 - nowTime;

            popularity_points2 = activityServer.getPopularityPoints();
            rank_points2 = activityServer.getRankPoints();
            distance2 = activityServer.getDistance();

        }
        else if (c2 instanceof FlagServer || c2 instanceof FlagSearch) {
            if (c2 instanceof FlagServer)
                flagServer = (FlagServer) c2;
            else
                flagServer = ((FlagSearch) c2).getFlagServer();

            start_date_time2 = flagServer.getDateTimeStart();

            end_date_time2 = flagServer.getLastDateTime();
            time_left_to_end2 = end_date_time2 - nowTime;

            popularity_points2 = flagServer.getPopularityPoints();
            rank_points2 = flagServer.getRankPoints();

        }


        if (time_left_to_end >= 0 && time_left_to_end2 < 0)
            return -1;
        else if (time_left_to_end < 0 && time_left_to_end2 >= 0)
            return 1;
        else if (rank_points > rank_points2)
            return -1;
        else if (rank_points < rank_points2)
            return 1;
        else if (start_date_time > start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return -1;
        else if (start_date_time < start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return 1;
        else if (start_date_time < start_date_time2 && time_left_to_end > 0 && time_left_to_end2 > 0)
            return -1;
        else if (start_date_time > start_date_time2 && time_left_to_end > 0 && time_left_to_end2 > 0)
            return 1;
        else if (distance >= 0 && distance2 < 0 && orderByProximity == true)
            return -1;
        else if (distance < 0 && distance2 >= 0 && orderByProximity == true)
            return 1;
        else if (distance < distance2 && orderByProximity == true)
            return -1;
        else if (distance > distance2 && orderByProximity == true)
            return 1;
        else if (popularity_points > popularity_points2 && orderByPopularity == true)
            return -1;
        else if (popularity_points < popularity_points2 && orderByPopularity == true)
            return 1;
        else if (start_date_time > start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0 && orderByDateHour == true)
            return -1;
        else if (start_date_time < start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0 && orderByDateHour == true)
            return 1;
        else if (start_date_time < start_date_time2 && time_left_to_end > 0 && time_left_to_end2 > 0 && orderByDateHour == true)
            return -1;
        else if (start_date_time > start_date_time2 && time_left_to_end > 0 && time_left_to_end2 > 0 && orderByDateHour == true)
            return 1;
        else
            return 0;
    }

    public static int algorithmSearchMyCommitments(Object c1, Object c2) {
        ActivityServer activityServer;
        FlagServer flagServer;
        ReminderServer reminderServer;

        long start_date_time = 0, start_date_time2 = 0;
        long end_date_time = 0, end_date_time2 = 0;
        long time_left_to_end = 0, time_left_to_end2 = 0;

        Calendar calendar = Calendar.getInstance();
        long nowTime = calendar.getTimeInMillis();

        if (c1 instanceof ActivityServer || c1 instanceof ActivitySearch) {
            if (c1 instanceof ActivityServer)
                activityServer = (ActivityServer) c1;
            else
                activityServer = ((ActivitySearch) c1).getActivityServer();

            start_date_time = activityServer.getDateTimeStart();

            end_date_time = activityServer.getLastDateTime();
            time_left_to_end = end_date_time - nowTime;

        }
        else if (c1 instanceof FlagServer || c1 instanceof FlagSearch) {
            if (c1 instanceof FlagServer)
                flagServer = (FlagServer) c1;
            else
                flagServer = ((FlagSearch) c1).getFlagServer();

            start_date_time = flagServer.getDateTimeStart();

            end_date_time = flagServer.getLastDateTime();
            time_left_to_end = end_date_time - nowTime;

        }
        else if (c1 instanceof ReminderServer || c1 instanceof ReminderSearch) {
            if (c1 instanceof ReminderServer)
                reminderServer = (ReminderServer) c1;
            else
                reminderServer = ((ReminderSearch) c1).getReminderServer();

            start_date_time = reminderServer.getDateTimeStart();

            end_date_time = reminderServer.getDateTimeStart();
            time_left_to_end = end_date_time - nowTime;

        }

        if (c2 instanceof ActivityServer || c2 instanceof ActivitySearch) {
            if (c2 instanceof ActivityServer)
                activityServer = (ActivityServer) c2;
            else
                activityServer = ((ActivitySearch) c2).getActivityServer();

            start_date_time2 = activityServer.getDateTimeStart();

            end_date_time2 = activityServer.getLastDateTime();
            time_left_to_end2 = end_date_time2 - nowTime;

        }
        else if (c2 instanceof FlagServer || c2 instanceof FlagSearch) {
            if (c2 instanceof FlagServer)
                flagServer = (FlagServer) c2;
            else
                flagServer = ((FlagSearch) c2).getFlagServer();

            start_date_time2 = flagServer.getDateTimeStart();

            end_date_time2 = flagServer.getLastDateTime();
            time_left_to_end2 = end_date_time2 - nowTime;

        }
        else if (c2 instanceof ReminderServer || c2 instanceof ReminderSearch) {
            if (c2 instanceof ReminderServer)
                reminderServer = (ReminderServer) c2;
            else
                reminderServer = ((ReminderSearch) c2).getReminderServer();

            start_date_time2 = reminderServer.getDateTimeStart();

            end_date_time2 = reminderServer.getDateTimeStart();
            time_left_to_end2 = end_date_time2 - nowTime;
        }


        if (time_left_to_end >= 0 && time_left_to_end2 < 0)
            return -1;
        else if (time_left_to_end < 0 && time_left_to_end2 >= 0)
            return 1;
        else if (start_date_time > start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return -1;
        else if (start_date_time < start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return 1;
        else if (start_date_time < start_date_time2 && time_left_to_end > 0 && time_left_to_end2 > 0)
            return -1;
        else if (start_date_time > start_date_time2 && time_left_to_end > 0 && time_left_to_end2 > 0)
            return 1;
        else
            return 0;
    }

    public static int algorithmSearchPeople(Object c1, Object c2) {
        User user, user2;

        int priority = 0, priority2 = 0;
        double people_points = 0, people_points2 = 0;

        user = (User) c1;
        user2 = (User) c2;

        priority = user.getCountFavorite() + user.getCountKnows();
        priority2 = user2.getCountFavorite() + user2.getCountKnows();

        people_points = user.getPeoplePoints();
        people_points2 = user2.getPeoplePoints();

        if (priority > priority2)
            return -1;
        else if (priority < priority2)
            return 1;
        else if (people_points > people_points2)
            return -1;
        else if (people_points < people_points2)
            return 1;
        else
            return 0;
    }

}