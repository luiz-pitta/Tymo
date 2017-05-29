package io.development.tymo.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhenShowFragment extends Fragment {

    private TextView dateHourText;
    private TextView locationText, repeatText;
    private RelativeLayout locationBox;
    private LinearLayout repeatBox;
    private FirebaseAnalytics mFirebaseAnalytics;

    private DateFormat dateFormat;

    public static Fragment newInstance(String text) {
        WhenShowFragment fragment = new WhenShowFragment();
        return fragment;
    }

    public WhenShowFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_act_where_when, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateFormat = new DateFormat(getActivity());

        dateHourText = (TextView) view.findViewById(R.id.dateHourText);
        locationText = (TextView) view.findViewById(R.id.locationText);
        repeatText = (TextView) view.findViewById(R.id.repeatText);
        locationBox = (RelativeLayout) view.findViewById(R.id.locationBox);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);

        ShowActivity showActivity = (ShowActivity)getActivity();

        if(!showActivity.getActivity().getLocation().matches("")) {
            locationBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = null;
                    if(showActivity.getActivity().getLat() != -500) {
                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                                "geo:" + showActivity.getActivity().getLat() +
                                        "," + showActivity.getActivity().getLng() +
                                        "?q=" + showActivity.getActivity().getLat() +
                                        "," + showActivity.getActivity().getLng() +
                                        "(" + showActivity.getActivity().getLocation() + ")"));
                    }else {

                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                                "http://maps.google.co.in/maps?q=" + showActivity.getActivity().getLocation()));
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "locationBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_find_application), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        setLayout(showActivity.getActivity(), showActivity.getActivityServers());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setLayout(ActivityServer activityServer, ArrayList<ActivityServer> activityServers){
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
        calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
        String dayStart = String.format("%02d", activityServer.getDayStart());
        String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        int yearStart = activityServer.getYearStart();
        String hourStart = String.format("%02d", activityServer.getHourStart());
        String minuteStart = String.format("%02d", activityServer.getMinuteStart());
        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
        String dayEnd = String.format("%02d", activityServer.getDayEnd());
        String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
        int yearEnd = activityServer.getYearEnd();
        String hourEnd = String.format("%02d", activityServer.getHourEnd());
        String minuteEnd = String.format("%02d", activityServer.getMinuteEnd());

        if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
            if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                dateHourText.setText(this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
            } else {
                dateHourText.setText(this.getResources().getString(R.string.date_format_5, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
            }
        } else {
            dateHourText.setText(this.getResources().getString(R.string.date_format_6, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
        }

        if(activityServer.getLocation() == null || activityServer.getLocation().matches(""))
            locationBox.setVisibility(View.GONE);
        else
            locationText.setText(activityServer.getLocation());

        if(activityServer.getRepeatType() == 0)
            repeatText.setText(getResources().getString(R.string.repeat_not));
        else {
            String repeatly;
            switch (activityServer.getRepeatType()){
                case Constants.DAYLY:
                    repeatly = getActivity().getString(R.string.repeat_dayly);
                    break;
                case Constants.WEEKLY:
                    repeatly = getActivity().getString(R.string.repeat_weekly);
                    break;
                case Constants.MONTHLY:
                    repeatly = getActivity().getString(R.string.repeat_monthly);
                    break;
                default:
                    repeatly = "";
                    break;
            }

            if(activityServer.getRepeatType() == 5)
                repeatText.setText(getActivity().getString(R.string.repeat_text_imported_google_calendar));
            else
                repeatText.setText(getActivity().getString(R.string.repeat_text, repeatly, getLastActivity(activityServers)));
        }
    }

    private String getLastActivity(ArrayList<ActivityServer> activityServers){

        Collections.sort(activityServers, new Comparator<ActivityServer>() {
            @Override
            public int compare(ActivityServer c1, ActivityServer c2) {
                ActivityServer activityServer;
                int day = 0, month= 0, year = 0;
                int day2 = 0, month2 = 0, year2 = 0;

                day = c1.getDayStart();
                month = c1.getMonthStart();
                year = c1.getYearStart();

                day2 = c2.getDayStart();
                month2 = c2.getMonthStart();
                year2 = c2.getYearStart();


                if(year < year2)
                    return -1;
                else if(year > year2)
                    return 1;
                else if(month < month2)
                    return -1;
                else if (month > month2)
                    return 1;
                else if(day < day2)
                    return -1;
                else if(day > day2)
                    return 1;
                else
                    return 0;

            }
        });

        ActivityServer activityServer = activityServers.get(activityServers.size()-1);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(activityServer.getYearStart(), activityServer.getMonthStart()-1, activityServer.getDayStart());

        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(cal.get(Calendar.DAY_OF_WEEK), cal);
        String dayEnd = String.format("%02d", activityServer.getDayEnd());
        String monthEnd = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(cal.getTime().getTime());
        int yearEnd = activityServer.getYearEnd();

        String date = this.getResources().getString(R.string.date_format_3, dayOfWeekEnd, dayEnd, monthEnd, yearEnd);

        return date;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
