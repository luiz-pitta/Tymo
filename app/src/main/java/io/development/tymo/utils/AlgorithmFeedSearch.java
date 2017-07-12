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

    public static int runAlgorithmFeedSearch(Object c1, Object c2) {
        ActivityServer activityServer;
        FlagServer flagServer;
        ReminderServer reminderServer;

        double rank_points = 0, rank_points2 = 0;


        if (c1 instanceof ActivityServer || c1 instanceof ActivitySearch) {
            if (c1 instanceof ActivityServer)
                activityServer = (ActivityServer) c1;
            else
                activityServer = ((ActivitySearch) c1).getActivityServer();

            rank_points = activityServer.getRankPoints();
        }
        else if (c1 instanceof FlagServer || c1 instanceof FlagSearch) {
            if (c1 instanceof FlagServer)
                flagServer = (FlagServer) c1;
            else
                flagServer = ((FlagSearch) c1).getFlagServer();

            rank_points = flagServer.getRankPoints();
        }
        else if (c1 instanceof ReminderServer || c1 instanceof ReminderSearch) {
            if (c1 instanceof ReminderServer)
                reminderServer = (ReminderServer) c1;
            else
                reminderServer = ((ReminderSearch) c1).getReminderServer();

            rank_points = reminderServer.getRankPoints();
        }

        if (c2 instanceof ActivityServer || c2 instanceof ActivitySearch) {
            if (c2 instanceof ActivityServer)
                activityServer = (ActivityServer) c2;
            else
                activityServer = ((ActivitySearch) c2).getActivityServer();

            rank_points2 = activityServer.getRankPoints();
        }
        else if (c2 instanceof FlagServer || c2 instanceof FlagSearch) {
            if (c2 instanceof FlagServer)
                flagServer = (FlagServer) c2;
            else
                flagServer = ((FlagSearch) c2).getFlagServer();

            rank_points2 = flagServer.getRankPoints();
        }
        else if (c2 instanceof ReminderServer || c2 instanceof ReminderSearch) {
            if (c2 instanceof ReminderServer)
                reminderServer = (ReminderServer) c2;
            else
                reminderServer = ((ReminderSearch) c2).getReminderServer();

            rank_points2 = reminderServer.getRankPoints();
        }


        if (rank_points > rank_points2)
            return -1;
        else if (rank_points < rank_points2)
            return 1;
        else
            return 0;
    }

}
