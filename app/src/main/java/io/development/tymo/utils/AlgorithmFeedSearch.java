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

    public static int algorithmFeedSearchWhats(Object c1, Object c2, boolean orderByProximity, boolean orderByPopularity, boolean orderByDateHour, Context context) {
        ActivityServer activityServer;
        FlagServer flagServer;

        long start_date_time = 0, start_date_time2 = 0;
        long end_date_time = 0, end_date_time2 = 0;
        long time_left_to_end = 0, time_left_to_end2 = 0;

        double distance = -1, distance2 = -1;
        double lat = -500, lng = -500, lat2 = -500, lng2 = -500;

        double rank_points = 0, rank_points_ue = 0, rank_points_we = 0, rank_points_de = 0, rank_points_range = 0;
        double rank_points2 = 0, rank_points_ue2 = 0, rank_points_we2 = 0, rank_points_de2 = 0, rank_points_range2 = 0;
        double popularity_points = 0, popularity_points2 = 0;
        String rank_points_range_op = "+", rank_points_range_op2 = "+";

        Calendar calendar = Calendar.getInstance();
        long nowTime = calendar.getTimeInMillis();

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (c1 instanceof ActivityServer || c1 instanceof ActivitySearch) {
            if (c1 instanceof ActivityServer)
                activityServer = (ActivityServer) c1;
            else
                activityServer = ((ActivitySearch) c1).getActivityServer();

            start_date_time = activityServer.getDateTimeStart();

            end_date_time = activityServer.getDateTimeEnd();
            time_left_to_end = end_date_time - nowTime;

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !(activityServer.getLat() == -500 || (activityServer.getLat() == 0 && activityServer.getLng() == 0))) {
                lat = TymoApplication.getInstance().getLatLng().get(0);
                lng = TymoApplication.getInstance().getLatLng().get(1);
                distance = Utilities.distance(activityServer.getLat(), activityServer.getLng(), lat, lng);
            }

            popularity_points = activityServer.getPopularityPoints();

            rank_points_ue = activityServer.getRankPointsUe();
            rank_points_we = activityServer.getRankPointsWe();
            rank_points_de = activityServer.getRankPointsDe();
            rank_points_range = Math.pow(activityServer.getRankPointsRangeBase(), distance);
            rank_points_range_op = activityServer.getRankPointsRangeOp();

            switch (rank_points_range_op) {
                case "+":
                    rank_points_we += rank_points_range;
                    break;
                case "*":
                    rank_points_we *= rank_points_range;
                    break;
                default:
                    rank_points_we += rank_points_range;
                    break;
            }

            rank_points = rank_points_ue * rank_points_we * rank_points_de;

        }
        else if (c1 instanceof FlagServer || c1 instanceof FlagSearch) {
            if (c1 instanceof FlagServer)
                flagServer = (FlagServer) c1;
            else
                flagServer = ((FlagSearch) c1).getFlagServer();

            start_date_time = flagServer.getDateTimeStart();

            end_date_time = flagServer.getDateTimeEnd();
            time_left_to_end = end_date_time - nowTime;

            popularity_points = flagServer.getPopularityPoints();

            rank_points_ue = flagServer.getRankPointsUe();
            rank_points_we = flagServer.getRankPointsWe();
            rank_points_de = flagServer.getRankPointsDe();

            rank_points = rank_points_ue * rank_points_we * rank_points_de;

        }

        if (c2 instanceof ActivityServer || c2 instanceof ActivitySearch) {
            if (c2 instanceof ActivityServer)
                activityServer = (ActivityServer) c2;
            else
                activityServer = ((ActivitySearch) c2).getActivityServer();

            start_date_time2 = activityServer.getDateTimeStart();

            end_date_time2 = activityServer.getDateTimeEnd();
            time_left_to_end2 = end_date_time2 - nowTime;

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !(activityServer.getLat() == -500 || (activityServer.getLat() == 0 && activityServer.getLng() == 0))) {
                lat2 = TymoApplication.getInstance().getLatLng().get(0);
                lng2 = TymoApplication.getInstance().getLatLng().get(1);
                distance2 = Utilities.distance(activityServer.getLat(), activityServer.getLng(), lat2, lng2);
            }

            popularity_points2 = activityServer.getPopularityPoints();

            rank_points_ue2 = activityServer.getRankPointsUe();
            rank_points_we2 = activityServer.getRankPointsWe();
            rank_points_de2 = activityServer.getRankPointsDe();
            rank_points_range2 = Math.pow(activityServer.getRankPointsRangeBase(), distance2);
            rank_points_range_op2 = activityServer.getRankPointsRangeOp();

            switch (rank_points_range_op2) {
                case "+":
                    rank_points_we2 += rank_points_range2;
                    break;
                case "*":
                    rank_points_we2 *= rank_points_range2;
                    break;
                default:
                    rank_points_we2 += rank_points_range2;
                    break;
            }

            rank_points2 = rank_points_ue2 * rank_points_we2 * rank_points_de2;

        }
        else if (c2 instanceof FlagServer || c2 instanceof FlagSearch) {
            if (c2 instanceof FlagServer)
                flagServer = (FlagServer) c2;
            else
                flagServer = ((FlagSearch) c2).getFlagServer();

            start_date_time2 = flagServer.getDateTimeStart();

            end_date_time2 = flagServer.getDateTimeEnd();
            time_left_to_end2 = end_date_time2 - nowTime;

            popularity_points2 = flagServer.getPopularityPoints();

            rank_points_ue2 = flagServer.getRankPointsUe();
            rank_points_we2 = flagServer.getRankPointsWe();
            rank_points_de2 = flagServer.getRankPointsDe();

            rank_points2 = rank_points_ue2 * rank_points_we2 * rank_points_de2;

        }


        if (time_left_to_end >= 0 && time_left_to_end2 < 0)
            return -1;
        else if (time_left_to_end < 0 && time_left_to_end2 >= 0)
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
        else if (start_date_time < start_date_time2 && orderByDateHour == true)
            return -1;
        else if (start_date_time > start_date_time2 && orderByDateHour == true)
            return 1;
        else if (start_date_time > start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return -1;
        else if (start_date_time < start_date_time2 && time_left_to_end < 0 && time_left_to_end2 < 0)
            return 1;
        else if (rank_points > rank_points2)
            return -1;
        else if (rank_points < rank_points2)
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

            end_date_time = activityServer.getDateTimeEnd();
            time_left_to_end = end_date_time - nowTime;

        }
        else if (c1 instanceof FlagServer || c1 instanceof FlagSearch) {
            if (c1 instanceof FlagServer)
                flagServer = (FlagServer) c1;
            else
                flagServer = ((FlagSearch) c1).getFlagServer();

            start_date_time = flagServer.getDateTimeStart();

            end_date_time = flagServer.getDateTimeEnd();
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

            end_date_time2 = activityServer.getDateTimeEnd();
            time_left_to_end2 = end_date_time2 - nowTime;

        }
        else if (c2 instanceof FlagServer || c2 instanceof FlagSearch) {
            if (c2 instanceof FlagServer)
                flagServer = (FlagServer) c2;
            else
                flagServer = ((FlagSearch) c2).getFlagServer();

            start_date_time2 = flagServer.getDateTimeStart();

            end_date_time2 = flagServer.getDateTimeEnd();
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

}