package io.development.tymo.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;

public class Utilities {

    public static final String PREFS_NAME = "AOP_PREFS";
    public static final String PREFS_KEY = "AOP_PREFS_String";
    public static final String PREFS_KEY2 = "AOP_PREFS_String2";
    public static final int DEFAULT_POSITION = 0;

    public final static int TYPE_PLANS = 0;
    public final static int TYPE_COMPARE = 1;
    public final static int TYPE_FRIEND = 2;

    public final static int MONDAY = 0;
    public final static int TUESDAY = 1;
    public final static int WEDNESDAY = 2;
    public final static int THURSDAY = 3;
    public final static int FRIDAY = 4;
    public final static int SATURDAY = 5;
    public final static int SUNDAY = 6;

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float spToPixels(Context context, float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return sp*scaledDensity;
    }

    public static float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px/scaledDensity;
    }

    public static String convertAccent(String letter){
        switch (letter){
            case "a":
                return "[aàáâãäå]";
            case "c":
                return "[cç]";
            case "e":
                return "[eèéêë]";
            case "i":
                return "[iìíîï]";
            case "n":
                return "[nñ]";
            case "o":
                return "[oòóôõöø]";
            case "s":
                return "[sß]";
            case "u":
                return "[uùúûü]";
            case "y":
                return "[yÿ]";
            case "(":
                return "[(]";
            case ")":
                return "[)]";
            case "*":
                return "[*]";
            case "?":
                return "[?]";
            case ".":
                return "[.]";
            case "|":
                return "[|]";
            case "{":
                return "[{]";
            case "}":
                return "[}]";
            case "^":
                return "[^]";
            case "[":
                return "";
            case "]":
                return "";
            default:
                return letter;
        }
    }

    public static void buildAlertMessageNoGps(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text1.setVisibility(View.GONE);

        text2.setText(context.getResources().getString(R.string.gps_enable_text));
        buttonText1.setText(context.getResources().getString(R.string.no));
        buttonText2.setText(context.getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(context, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static boolean isStartedFinishedToday(int day, int day2){
        return day == day2;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);

        return dist * 60 * 1.1515 * 1.609344;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static boolean isActivityInWeekDay(List<Integer> list, int d1, int m1, int y1, int d2,int m2, int y2){
        int j, day_week, n_days;
        Calendar calendar = Calendar.getInstance();
        calendar.set(y1,m1-1,d1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(y2,m2-1,d2);

        LocalDate start = new LocalDate(y1, m1, d1);
        LocalDate end = new LocalDate(y2, m2, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        n_days = timePeriod.getDays()+1;

        for(j=0;j<n_days;j++){
            day_week = calendar.get(Calendar.DAY_OF_WEEK);
            if(isInDayWeek(list, day_week))
                return true;
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        return false;
    }

    private static boolean isInDayWeek(List<Integer> list, int day_week){
        int i;
        boolean actInWeek = false;
        for(i=0;i<list.size();i++){
            int day = list.get(i);
            switch (day) {
                case SUNDAY:
                    if(day_week == Calendar.SUNDAY)
                        actInWeek = true;
                    break;

                case MONDAY:
                    if(day_week == Calendar.MONDAY)
                        actInWeek = true;
                    break;

                case TUESDAY:
                    if(day_week == Calendar.TUESDAY)
                        actInWeek = true;
                    break;

                case WEDNESDAY:
                    if(day_week == Calendar.WEDNESDAY)
                        actInWeek = true;
                    break;

                case THURSDAY:
                    if(day_week == Calendar.THURSDAY )
                        actInWeek = true;
                    break;

                case FRIDAY:
                    if(day_week == Calendar.FRIDAY)
                        actInWeek = true;
                    break;

                case SATURDAY:
                    if(day_week == Calendar.SATURDAY)
                        actInWeek = true;
                    break;
            }
        }
        return actInWeek;
    }

    public static boolean isActivityInRange(int ds, int ms, int df, int mf, int d){
        if(ms == mf) {
            if (ds > d || df < d)
                return false;
            else
                return true;
        }
        else{
            if(d >= ds || d <= df)
                return true;
            else
                return false;
        }
    }
}
