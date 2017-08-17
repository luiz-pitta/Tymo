package io.development.tymo.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReminderShowFragment extends Fragment {

    private TextView tittleText, dateHourText, repeatText;
    private LinearLayout repeatBox;
    private FirebaseAnalytics mFirebaseAnalytics;

    private DateFormat dateFormat;

    public static Fragment newInstance(String text) {
        ReminderShowFragment fragment = new ReminderShowFragment();
        return fragment;
    }

    public ReminderShowFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateFormat = new DateFormat(getActivity());

        tittleText = (TextView) view.findViewById(R.id.title);
        dateHourText = (TextView) view.findViewById(R.id.dateHourText);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);
        repeatText = (TextView) view.findViewById(R.id.repeatText);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setLayout(ReminderServer reminderServer, ArrayList<ReminderServer> reminderServers) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart());

        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
        String dayStart = String.format("%02d", reminderServer.getDayStart());
        String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        int yearStart = reminderServer.getYearStart();
        String hourStart = String.format("%02d", reminderServer.getHourStart());
        String minuteStart = String.format("%02d", reminderServer.getMinuteStart());

        dateHourText.setText(this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));

        tittleText.setText(reminderServer.getTitle());

        if (reminderServer.getRepeatType() == 0)
            repeatBox.setVisibility(View.GONE);
        else {
            String repeatly;
            switch (reminderServer.getRepeatType()) {
                case Constants.DAYLY:
                    repeatly = getActivity().getString(R.string.repeat_daily);
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

            repeatText.setText(getActivity().getString(R.string.repeat_text, repeatly, getLastActivity(reminderServers)));
        }
    }

    private String getLastActivity(ArrayList<ReminderServer> reminderServers) {

        Collections.sort(reminderServers, new Comparator<ReminderServer>() {
            @Override
            public int compare(ReminderServer c1, ReminderServer c2) {
                int day = 0, month = 0, year = 0;
                int day2 = 0, month2 = 0, year2 = 0;

                day = c1.getDayStart();
                month = c1.getMonthStart();
                year = c1.getYearStart();

                day2 = c2.getDayStart();
                month2 = c2.getMonthStart();
                year2 = c2.getYearStart();

                if (year < year2)
                    return -1;
                else if (year > year2)
                    return 1;
                else if (month < month2)
                    return -1;
                else if (month > month2)
                    return 1;
                else if (day < day2)
                    return -1;
                else if (day > day2)
                    return 1;
                else
                    return 0;

            }
        });

        ReminderServer reminderServer = reminderServers.get(reminderServers.size() - 1);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart());

        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(cal.get(Calendar.DAY_OF_WEEK), cal);
        String dayStart = String.format("%02d", reminderServer.getDayStart());
        String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(cal.getTime().getTime());
        int yearStart = reminderServer.getYearStart();

        String date = this.getResources().getString(R.string.date_format_3, dayOfWeekStart.toLowerCase(), dayStart, monthStart, yearStart);

        return date;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
