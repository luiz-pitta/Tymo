package io.development.tymo.fragments;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.TymoApplication;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.adapters.ViewPagerAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FilterServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.models.search.ActivitySearch;
import io.development.tymo.models.search.FlagSearch;
import io.development.tymo.models.search.ReminderSearch;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static io.development.tymo.utils.AlgorithmFeedSearch.algorithmFeedSearchWhats;
import static io.development.tymo.utils.AlgorithmFeedSearch.algorithmSearchMyCommitments;
import static io.development.tymo.utils.AlgorithmFeedSearch.algorithmSearchPeople;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements TabLayout.OnTabSelectedListener {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FilterServer filter;
    private TextView menuTitle;

    private DateFormat dateFormat;

    private String email;

    private List<Object> listWhats = new ArrayList<>();
    private List<Object> listMyCommitment = new ArrayList<>();
    private List<Object> listPeople = new ArrayList<>();

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private final static int PEOPLE = 0, MY_PLANS = 1, WHATS_GOING = 2;

    private ViewPagerAdapter adapter;

    public static Fragment newInstance(String text) {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public SearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateFormat = new DateFormat(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubscriptions = new CompositeDisposable();

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        menuTitle = (TextView) view.findViewById(R.id.menuTitle);
        setupViewPager(viewPager);

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        menuTitle.setText(R.string.search_menu_users);

        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View custom_tab = getActivity().getLayoutInflater().inflate(R.layout.custom_tab_layout, null);
            ImageView imageView = custom_tab.findViewById(R.id.icon);
            switch (i) {
                case 0:
                    imageView.setImageResource(R.drawable.ic_people_three);
                    imageView.setTag(R.drawable.ic_people_three);
                    imageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.ic_my_commitments);
                    imageView.setTag(R.drawable.ic_my_commitments);
                    imageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_400));
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.ic_public);
                    imageView.setTag(R.drawable.ic_public);
                    imageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_400));
                    break;
            }

            tabLayout.getTabAt(i).setCustomView(custom_tab);
        }

        tabLayout.addOnTabSelectedListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

        doSearch(".");
    }

    private void getSearchResults(Query query) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getSearchResults(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void getSearchResultsFilter(String email, FilterServer filterServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getSearchFilter(email, filterServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFilter, this::handleError));
    }

    private void handleResponseFilter(Response response) {
        int i;
        ArrayList<ActivityServer> whats_going_act = response.getWhatsGoingAct();
        ArrayList<FlagServer> whats_going_flagServer = response.getWhatsGoingFlag();
        ArrayList<ActivityServer> my_commit_act = response.getMyCommitAct();
        ArrayList<FlagServer> my_commit_flagServer = response.getMyCommitFlag();
        ArrayList<ReminderServer> my_commit_reminderServer = response.getMyCommitReminder();
        ArrayList<User> people = response.getPeople();

        listWhats.clear();
        listMyCommitment.clear();
        listPeople.clear();

        if (filter.getLat() != -500) {
            for (i = 0; i < whats_going_act.size(); i++) {
                ActivityServer activityServer = whats_going_act.get(i);
                if (activityServer.getLat() != -500
                        && Utilities.distance(activityServer.getLat(), activityServer.getLng(),
                        filter.getLat(), filter.getLng()) > 10) {
                    whats_going_act.remove(activityServer);
                    i--;
                }
            }

            for (i = 0; i < my_commit_act.size(); i++) {
                ActivityServer activityServer = my_commit_act.get(i);
                if (activityServer.getLat() != -500
                        && Utilities.distance(activityServer.getLat(), activityServer.getLng(),
                        filter.getLat(), filter.getLng()) > 10) {
                    my_commit_act.remove(activityServer);
                    i--;
                }
            }

            whats_going_flagServer.clear();
            whats_going_flagServer.clear();
            my_commit_flagServer.clear();
            my_commit_reminderServer.clear();
        }

        if (filter.getWeekDays().size() > 0) {
            List<Integer> listWeek = filter.getWeekDays();

            for (i = 0; i < whats_going_act.size(); i++) {
                ActivityServer activityServer = whats_going_act.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, activityServer.getDayStart(),
                        activityServer.getMonthStart(), activityServer.getYearStart(), activityServer.getDayEnd(),
                        activityServer.getMonthEnd(), activityServer.getYearEnd())) {
                    whats_going_act.remove(activityServer);
                    i--;
                }
            }

            for (i = 0; i < whats_going_flagServer.size(); i++) {
                FlagServer flagServer = whats_going_flagServer.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, flagServer.getDayStart(),
                        flagServer.getMonthStart(), flagServer.getYearStart(), flagServer.getDayEnd(),
                        flagServer.getMonthEnd(), flagServer.getYearEnd())) {
                    whats_going_flagServer.remove(flagServer);
                    i--;
                }
            }

            for (i = 0; i < my_commit_act.size(); i++) {
                ActivityServer activityServer = my_commit_act.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, activityServer.getDayStart(),
                        activityServer.getMonthStart(), activityServer.getYearStart(), activityServer.getDayEnd(),
                        activityServer.getMonthEnd(), activityServer.getYearEnd())) {
                    my_commit_act.remove(activityServer);
                    i--;
                }
            }

            for (i = 0; i < my_commit_flagServer.size(); i++) {
                FlagServer flagServer = my_commit_flagServer.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, flagServer.getDayStart(),
                        flagServer.getMonthStart(), flagServer.getYearStart(), flagServer.getDayEnd(),
                        flagServer.getMonthEnd(), flagServer.getYearEnd())) {
                    my_commit_flagServer.remove(flagServer);
                    i--;
                }
            }

            for (i = 0; i < my_commit_reminderServer.size(); i++) {
                ReminderServer reminderServer = my_commit_reminderServer.get(i);
                if (!Utilities.isActivityInWeekDay(listWeek, reminderServer.getDayStart(),
                        reminderServer.getMonthStart(), reminderServer.getYearStart(), reminderServer.getDayStart(),
                        reminderServer.getMonthStart(), reminderServer.getYearStart())) {
                    my_commit_reminderServer.remove(reminderServer);
                    i--;
                }
            }
        }

        for (i = 0; i < whats_going_act.size(); i++) {
            ActivityServer activityServer = whats_going_act.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", activityServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = activityServer.getYearStart();
            String hourStart = String.format("%02d", activityServer.getHourStart());
            String minuteStart = String.format("%02d", activityServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            listWhats.add(new ActivitySearch(activityServer.getTitle(), date, this.getResources().getString(R.string.created_by, activityServer.getCreator()), activityServer.getCubeColorUpper(), activityServer.getCubeColor(), activityServer.getCubeIcon(), activityServer));
        }

        for (i = 0; i < whats_going_flagServer.size(); i++) {
            FlagServer flagServer = whats_going_flagServer.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", flagServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = flagServer.getYearStart();
            String hourStart = String.format("%02d", flagServer.getHourStart());
            String minuteStart = String.format("%02d", flagServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            listWhats.add(new FlagSearch(flagServer.getTitle(), date, this.getResources().getString(R.string.created_by, flagServer.getCreator()), flagServer.getType(), flagServer));
        }

        for (i = 0; i < my_commit_act.size(); i++) {
            ActivityServer activityServer = my_commit_act.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", activityServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = activityServer.getYearStart();
            String hourStart = String.format("%02d", activityServer.getHourStart());
            String minuteStart = String.format("%02d", activityServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            String creator;

            if (email.equals(activityServer.getCreatorEmail())) {
                creator = this.getResources().getString(R.string.created_by_me);
            } else {
                creator = this.getResources().getString(R.string.created_by, activityServer.getCreator());
            }

            listMyCommitment.add(new ActivitySearch(activityServer.getTitle(), date, creator, activityServer.getCubeColorUpper(), activityServer.getCubeColor(), activityServer.getCubeIcon(), activityServer));
        }

        for (i = 0; i < my_commit_flagServer.size(); i++) {
            FlagServer flagServer = my_commit_flagServer.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", flagServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = flagServer.getYearStart();
            String hourStart = String.format("%02d", flagServer.getHourStart());
            String minuteStart = String.format("%02d", flagServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            String creator;

            if (email.equals(flagServer.getCreatorEmail())) {
                creator = this.getResources().getString(R.string.created_by_me);
            } else {
                creator = this.getResources().getString(R.string.created_by, flagServer.getCreator());
            }

            listMyCommitment.add(new FlagSearch(flagServer.getTitle(), date, creator, flagServer.getType(), flagServer));
        }

        for (i = 0; i < my_commit_reminderServer.size(); i++) {
            ReminderServer reminderServer = my_commit_reminderServer.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", reminderServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = reminderServer.getYearStart();
            String hourStart = String.format("%02d", reminderServer.getHourStart());
            String minuteStart = String.format("%02d", reminderServer.getMinuteStart());

            String date;

            if (hourStart.matches("00") && minuteStart.matches("00")) {
                date = this.getResources().getString(R.string.date_format_3, dayOfWeekStart, dayStart, monthStart, yearStart);
            } else {
                date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);
            }

            listMyCommitment.add(new ReminderSearch(reminderServer.getTitle(), date, this.getResources().getString(R.string.my_reminder), reminderServer));
        }

        Collections.sort(listWhats, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmFeedSearchWhats(c1, c2, filter.getProximity(), filter.getPopularity(), filter.getDateHour(), getActivity());
            }
        });

        Collections.sort(listMyCommitment, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmSearchMyCommitments(c1, c2);
            }
        });

        SearchWhatsFragment searchWhatsFragment = (SearchWhatsFragment) adapter.getItem(WHATS_GOING);
        searchWhatsFragment.setAdapterItens(listWhats);

        SearchMyCommitmentFragment searchMyCommitmentFragment = (SearchMyCommitmentFragment) adapter.getItem(MY_PLANS);
        searchMyCommitmentFragment.setAdapterItens(listMyCommitment);

        for (i = 0; i < people.size(); i++) {
            User user = people.get(i);
            listPeople.add(user);
        }

        Collections.sort(listPeople, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmSearchPeople(c1, c2);
            }
        });

        SearchPeopleFragment searchPeopleFragment = (SearchPeopleFragment) adapter.getItem(PEOPLE);
        searchPeopleFragment.setAdapterItens(listPeople);

    }

    private void handleResponse(Response response) {

        ArrayList<ActivityServer> whats_going_act = response.getWhatsGoingAct();
        ArrayList<FlagServer> whats_going_flagServer = response.getWhatsGoingFlag();
        ArrayList<ActivityServer> my_commit_act = response.getMyCommitAct();
        ArrayList<FlagServer> my_commit_flagServer = response.getMyCommitFlag();
        ArrayList<ReminderServer> my_commit_reminderServer = response.getMyCommitReminder();
        ArrayList<User> people = response.getPeople();

        listWhats.clear();
        listMyCommitment.clear();
        listPeople.clear();

        for (int i = 0; i < whats_going_act.size(); i++) {
            ActivityServer activityServer = whats_going_act.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", activityServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = activityServer.getYearStart();
            String hourStart = String.format("%02d", activityServer.getHourStart());
            String minuteStart = String.format("%02d", activityServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            listWhats.add(new ActivitySearch(activityServer.getTitle(), date, this.getResources().getString(R.string.created_by, activityServer.getCreator()), activityServer.getCubeColorUpper(), activityServer.getCubeColor(), activityServer.getCubeIcon(), activityServer));
        }

        for (int i = 0; i < whats_going_flagServer.size(); i++) {
            FlagServer flagServer = whats_going_flagServer.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", flagServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = flagServer.getYearStart();
            String hourStart = String.format("%02d", flagServer.getHourStart());
            String minuteStart = String.format("%02d", flagServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            listWhats.add(new FlagSearch(flagServer.getTitle(), date, this.getResources().getString(R.string.created_by, flagServer.getCreator()), flagServer.getType(), flagServer));
        }

        Collections.sort(listWhats, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmFeedSearchWhats(c1, c2, false, false, false, getActivity());
            }
        });

        SearchWhatsFragment searchWhatsFragment = (SearchWhatsFragment) adapter.getItem(WHATS_GOING);
        searchWhatsFragment.setAdapterItens(listWhats);

        for (int i = 0; i < my_commit_act.size(); i++) {
            ActivityServer activityServer = my_commit_act.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", activityServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = activityServer.getYearStart();
            String hourStart = String.format("%02d", activityServer.getHourStart());
            String minuteStart = String.format("%02d", activityServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            String creator;

            if (email.equals(activityServer.getCreatorEmail())) {
                creator = this.getResources().getString(R.string.created_by_me);
            } else {
                creator = this.getResources().getString(R.string.created_by, activityServer.getCreator());
            }

            listMyCommitment.add(new ActivitySearch(activityServer.getTitle(), date, creator, activityServer.getCubeColorUpper(), activityServer.getCubeColor(), activityServer.getCubeIcon(), activityServer));
        }

        for (int i = 0; i < my_commit_flagServer.size(); i++) {
            FlagServer flagServer = my_commit_flagServer.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", flagServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = flagServer.getYearStart();
            String hourStart = String.format("%02d", flagServer.getHourStart());
            String minuteStart = String.format("%02d", flagServer.getMinuteStart());

            String date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);

            String creator;

            if (email.equals(flagServer.getCreatorEmail())) {
                creator = this.getResources().getString(R.string.created_by_me);
            } else {
                creator = this.getResources().getString(R.string.created_by, flagServer.getCreator());
            }

            listMyCommitment.add(new FlagSearch(flagServer.getTitle(), date, creator, flagServer.getType(), flagServer));
        }

        for (int i = 0; i < my_commit_reminderServer.size(); i++) {
            ReminderServer reminderServer = my_commit_reminderServer.get(i);

            Calendar calendar = Calendar.getInstance();
            calendar.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", reminderServer.getDayStart());
            String monthStart = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = reminderServer.getYearStart();
            String hourStart = String.format("%02d", reminderServer.getHourStart());
            String minuteStart = String.format("%02d", reminderServer.getMinuteStart());

            String date;

            if (hourStart.matches("00") && minuteStart.matches("00")) {
                date = this.getResources().getString(R.string.date_format_3, dayOfWeekStart, dayStart, monthStart, yearStart);
            } else {
                date = this.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart);
            }

            listMyCommitment.add(new ReminderSearch(reminderServer.getTitle(), date, this.getResources().getString(R.string.my_reminder), reminderServer));
        }

        Collections.sort(listMyCommitment, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmSearchMyCommitments(c1, c2);
            }
        });

        SearchMyCommitmentFragment searchMyCommitmentFragment = (SearchMyCommitmentFragment) adapter.getItem(MY_PLANS);
        searchMyCommitmentFragment.setAdapterItens(listMyCommitment);

        listPeople.addAll(people);

        Collections.sort(listPeople, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                return algorithmSearchPeople(c1, c2);
            }
        });

        SearchPeopleFragment searchPeopleFragment = (SearchPeopleFragment) adapter.getItem(PEOPLE);
        searchPeopleFragment.setAdapterItens(listPeople);

    }

    private void handleError(Throwable error) {
        if (Utilities.isDeviceOnline(getActivity()))
            Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setupViewPager(ViewPager viewPager) {
        if (Build.VERSION.SDK_INT >= 17)
            adapter = new ViewPagerAdapter(getChildFragmentManager(), false);
        else
            adapter = new ViewPagerAdapter(getFragmentManager(), false);

        adapter.addFragment(new SearchPeopleFragment(), getResources().getString(R.string.search_menu_users));
        adapter.addFragment(new SearchMyCommitmentFragment(), getResources().getString(R.string.search_menu_my_commitments));
        adapter.addFragment(new SearchWhatsFragment(), getResources().getString(R.string.search_menu_what_is_going_on));

        viewPager.setAdapter(adapter);
    }


    public void doSearch(String mQuery) {

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "search" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        SearchMyCommitmentFragment searchMyCommitmentFragment = (SearchMyCommitmentFragment) adapter.getItem(MY_PLANS);
        SearchWhatsFragment searchWhatsFragment = (SearchWhatsFragment) adapter.getItem(WHATS_GOING);
        SearchPeopleFragment searchPeopleFragment = (SearchPeopleFragment) adapter.getItem(PEOPLE);

        searchMyCommitmentFragment.showProgress();
        searchPeopleFragment.showProgress();
        searchWhatsFragment.showProgress();

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        mQuery = Utilities.cleanBackSlash(mQuery);

        Query query = new Query();
        query.setEmail(email);
        query.setQuery(mQuery);
        query.setDay(day);
        query.setMonth(month);
        query.setYear(year);
        query.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
        query.setLatCurrent(TymoApplication.getInstance().getLatLng().get(0));
        query.setLngCurrent(TymoApplication.getInstance().getLatLng().get(1));

        getSearchResults(query);
    }

    public void doSearchFilter(FilterServer filterServer) {

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "searchFilter" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        filter = filterServer;

        SearchMyCommitmentFragment searchMyCommitmentFragment = (SearchMyCommitmentFragment) adapter.getItem(MY_PLANS);
        SearchWhatsFragment searchWhatsFragment = (SearchWhatsFragment) adapter.getItem(WHATS_GOING);
        SearchPeopleFragment searchPeopleFragment = (SearchPeopleFragment) adapter.getItem(PEOPLE);

        searchMyCommitmentFragment.showProgress();
        searchPeopleFragment.showProgress();
        searchWhatsFragment.showProgress();

        filter.setLatCurrent(TymoApplication.getInstance().getLatLng().get(0));
        filter.setLngCurrent(TymoApplication.getInstance().getLatLng().get(1));

        if (filter.isFilterFilled())
            getSearchResultsFilter(email, filter);
        else
            doSearch(filter.getQuery());
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        View custom_tab = tabLayout.getTabAt(tab.getPosition()).getCustomView();
        ImageView imageView = custom_tab.findViewById(R.id.icon);
        imageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
        if (imageView.getTag().equals(R.drawable.ic_people_three)) {
            menuTitle.setText(R.string.search_menu_users);
        } else if (imageView.getTag().equals(R.drawable.ic_my_commitments)) {
            menuTitle.setText(R.string.search_menu_my_commitments);
        } else if (imageView.getTag().equals(R.drawable.ic_public)) {
            menuTitle.setText(R.string.search_menu_what_is_going_on);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        View custom_tab = tabLayout.getTabAt(tab.getPosition()).getCustomView();
        ImageView imageView = custom_tab.findViewById(R.id.icon);
        imageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_400));
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptions != null)
            mSubscriptions.dispose();
    }

}
