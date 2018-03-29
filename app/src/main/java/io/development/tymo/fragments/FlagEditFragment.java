package io.development.tymo.fragments;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.SelectPeopleActivity;
import io.development.tymo.activities.ShowGuestsActivity;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FlagEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener, View.OnTouchListener {

    private RelativeLayout addPersonButton, sendBox, repeatAdd;
    private TextView guestsNumber, addGuestText, guestText, repeatMax;
    private View addGuestButtonDivider;
    private Rect rect;
    private FlagServer flagServer;
    private long last_date_time;
    private boolean isInPast = false, repeat_blocked = false;;

    private DateFormat dateFormat;

    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private ArrayList<User> listPerson = new ArrayList<>();
    private ArrayList<User> listConfirmed = new ArrayList<>();
    private final int GUEST_UPDATE = 37, ADD_GUEST = 39;
    private FirebaseAnalytics mFirebaseAnalytics;

    private LinearLayout guestBox;

    private int repeat_type = 0;
    private int repeat_qty = -1;
    private int send_toAll = 1;
    private boolean isEdit = false;

    ArrayList<User> data = new ArrayList<>();

    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;

    private TextView dateStart, dateEnd, titleMax, sendText, repeatText, repeatLastDate;
    private TextView timeStart, timeEnd, repeatAddText;

    private LinearLayout repeatNumberBox, repeatBox, whoCanInviteBox, profilesPhotos, addGuestBox;
    private EditText repeatEditText, titleEditText;
    private MaterialSpinner spinner, sendPicker;

    private CompositeDisposable mSubscriptions;

    private ImageView addPersonIcon, repeatAddIcon;
    private ImageView clearDateStart, clearDateEnd, clearTimeStart, clearTimeEnd;

    private View viewClicked;

    public static Fragment newInstance(String text) {
        FlagEditFragment fragment = new FlagEditFragment();
        return fragment;
    }

    public FlagEditFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flag_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubscriptions = new CompositeDisposable();

        dateFormat = new DateFormat(getActivity());

        guestsNumber = (TextView) view.findViewById(R.id.guestsNumber);
        titleMax = (TextView) view.findViewById(R.id.titleMax);
        dateStart = (TextView) view.findViewById(R.id.dateStart);
        dateEnd = (TextView) view.findViewById(R.id.dateEnd);
        timeStart = (TextView) view.findViewById(R.id.timeStart);
        timeEnd = (TextView) view.findViewById(R.id.timeEnd);
        clearDateStart = (ImageView) view.findViewById(R.id.clearDateStart);
        clearDateEnd = (ImageView) view.findViewById(R.id.clearDateEnd);
        clearTimeStart = (ImageView) view.findViewById(R.id.clearTimeStart);
        clearTimeEnd = (ImageView) view.findViewById(R.id.clearTimeEnd);
        repeatNumberBox = (LinearLayout) view.findViewById(R.id.repeatNumberBox);
        repeatEditText = (EditText) view.findViewById(R.id.repeatEditText);
        repeatMax = (TextView) view.findViewById(R.id.repeatMax);
        titleEditText = (EditText) view.findViewById(R.id.title);
        whoCanInviteBox = (LinearLayout) view.findViewById(R.id.whoCanInviteBox);
        guestBox = (LinearLayout) view.findViewById(R.id.guestBox);
        guestText = (TextView) view.findViewById(R.id.guestText);
        profilesPhotos = (LinearLayout) view.findViewById(R.id.profilesPhotos);
        sendText = (TextView) view.findViewById(R.id.sendText);
        sendBox = (RelativeLayout) view.findViewById(R.id.sendBox);
        repeatBox = (LinearLayout) view.findViewById(R.id.repeatBox);
        recyclerView = (RecyclerView) view.findViewById(R.id.guestRow);
        addPersonButton = (RelativeLayout) view.findViewById(R.id.addGuestButton);
        addPersonIcon = (ImageView) view.findViewById(R.id.addGuestIcon);
        addGuestText = (TextView) view.findViewById(R.id.addGuestText);
        addGuestButtonDivider = (View) view.findViewById(R.id.addGuestButtonDivider);
        repeatAddIcon = (ImageView) view.findViewById(R.id.repeatAddIcon);
        repeatAddText = (TextView) view.findViewById(R.id.repeatAddText);
        repeatAdd = (RelativeLayout) view.findViewById(R.id.repeatAdd);
        repeatText = (TextView) view.findViewById(R.id.repeatText);
        addGuestBox = (LinearLayout) view.findViewById(R.id.addGuestBox);
        repeatLastDate = (TextView) view.findViewById(R.id.repeatLastDate);

        clearDateStart.setVisibility(View.GONE);
        clearDateEnd.setVisibility(View.GONE);
        clearTimeStart.setVisibility(View.GONE);
        clearTimeEnd.setVisibility(View.GONE);
        repeatBox.setVisibility(View.GONE);
        repeatText.setVisibility(View.GONE);

        addGuestText.setText(getString(R.string.signalize_guest_btn));

        titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }
        });

        repeatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int number;

                if (String.valueOf(s).matches("")) {
                    number = 0;
                } else {
                    number = Integer.valueOf(String.valueOf(s));
                }

                setRepeatLastDate();

                if (number > 500) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatLastDate.setVisibility(View.INVISIBLE);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                }
            }
        });

        new Actor.Builder(SpringSystem.create(), addPersonButton)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {

                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        day_start = -1;
        month_start = -1;
        year_start = -1;
        day_end = -1;
        month_end = -1;
        year_end = -1;
        minutes_start = -1;
        minutes_end = -1;
        hour_start = -1;
        hour_end = -1;

        guestBox.setOnClickListener(this);
        guestBox.setOnTouchListener(this);
        repeatAdd.setOnClickListener(this);
        clearDateStart.setOnClickListener(this);
        clearDateStart.setOnClickListener(this);
        clearDateEnd.setOnClickListener(this);
        clearTimeStart.setOnClickListener(this);
        clearTimeEnd.setOnClickListener(this);
        repeatAdd.setOnTouchListener(this);

        repeatNumberBox.setVisibility(View.GONE);
        guestBox.setVisibility(View.GONE);
        profilesPhotos.setVisibility(View.GONE);
        whoCanInviteBox.setVisibility(View.GONE);

        spinner = (MaterialSpinner) view.findViewById(R.id.repeatPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                repeat_type = position;

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (position == 0) {
                    repeatNumberBox.setVisibility(View.GONE);
                } else {
                    repeatNumberBox.setVisibility(View.VISIBLE);
                }

                setRepeatLastDate();
            }
        });

        sendPicker = (MaterialSpinner) view.findViewById(R.id.sendPicker);
        sendPicker.setItems(getResources().getStringArray(R.array.array_who_will_be_signalized));
        sendPicker.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                send_toAll = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sendPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if (position != 0) {
                    guestBox.setVisibility(View.VISIBLE);
                    profilesPhotos.setVisibility(View.VISIBLE);
                } else {
                    guestBox.setVisibility(View.GONE);
                    profilesPhotos.setVisibility(View.GONE);
                }

            }
        });

        sendPicker.setSelectedIndex(1);
        guestBox.setVisibility(View.VISIBLE);
        profilesPhotos.setVisibility(View.VISIBLE);

        dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        timeEnd.setOnClickListener(this);
        addPersonButton.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        getUser(email);
        addPersonButton.setActivated(false);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

    }

    private void setRepeatLastDate() {
        int qty = 0;
        int repeat_type = this.repeat_type;

        if (!repeat_blocked) {
            if (!repeatEditText.getText().toString().matches(""))
                qty = Integer.parseInt(repeatEditText.getText().toString());
        } else {
            qty = this.repeat_qty;
            repeat_type = this.repeat_type;
        }

        if (qty > 0 && repeat_type > 0 && day_start != -1) {
            repeatNumberBox.setVisibility(View.VISIBLE);
            repeatLastDate.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();

            if (day_end != -1)
                calendar.set(year_end, month_end, day_end);
            else
                calendar.set(year_start, month_start, day_start);

            switch (repeat_type) {
                case Constants.DAILY:
                    calendar.add(Calendar.DAY_OF_WEEK, 1 * qty);
                    last_date_time = calendar.getTimeInMillis();
                    break;
                case Constants.WEEKLY:
                    calendar.add(Calendar.DAY_OF_WEEK, 7 * qty);
                    last_date_time = calendar.getTimeInMillis();
                    break;
                case Constants.MONTHLY:
                    calendar.add(Calendar.MONTH, 1 * qty);
                    last_date_time = calendar.getTimeInMillis();
                    break;
                default:
                    last_date_time = calendar.getTimeInMillis();
                    break;
            }

            String dayOfWeek = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            String month = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int year = calendar.get(Calendar.YEAR);
            String date = this.getResources().getString(R.string.date_format_03, dayOfWeek, day, month, year);
            repeatLastDate.setText(this.getString(R.string.repeat_last_date, date));
        } else {
            repeatLastDate.setVisibility(View.INVISIBLE);
            if (repeat_type == 0) {
                repeatNumberBox.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<User> setOrderGuests(ArrayList<User> users) {

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                String name1 = c1.getName();
                String name2 = c2.getName();

                if (name1.compareTo(name2) > 0)
                    return 1;
                else if (name1.compareTo(name2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getInvitation();
                long id2 = c2.getInvitation();

                if (id1 == 1)
                    return -1;
                else if (id2 == 1)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getCountKnows();
                long id2 = c2.getCountKnows();

                if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                long id1 = c1.getCountFavorite();
                long id2 = c2.getCountFavorite();

                if (id1 > id2)
                    return -1;
                else if (id1 < id2)
                    return 1;
                else
                    return 0;
            }
        });

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User c1, User c2) {
                boolean id1 = c1.isCreator();
                boolean id2 = c2.isCreator();

                if (id1 && !id2)
                    return -1;
                else if (!id1 && id2)
                    return 1;
                else
                    return 0;
            }
        });

        return users;
    }

    private void createDialogMessageAddInPast(int y1, int m1, int d1, int h1, int min1, int y2, int m2, int d2, int h2, int min2) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        button1.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);

        Dialog dg = new Dialog(getActivity(), R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        String date = String.format("%02d", d1) + "/" + String.format("%02d", m1) + "/" + String.valueOf(y1);
        String dateNow = String.format("%02d", d2) + "/" + String.format("%02d", m2) + "/" + String.valueOf(y2);
        String time = String.format("%02d", h1) + ":" + String.format("%02d", min1);
        String timeNow = String.format("%02d", h2) + ":" + String.format("%02d", min2);

        text1.setText(R.string.signalize_past_dialog_text_1);
        text2.setText(getActivity().getString(R.string.signalize_past_dialog_text_2, date + " - " + time, dateNow + " - " + timeNow));
        buttonText2.setText(R.string.close);

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_radius));
                }

                return false;
            }
        });

        dg.show();
    }

    public void setSelectionSendBox(boolean type) {
        if (!type) {
            if (guestBox.getVisibility() == View.GONE) {
                sendBox.setVisibility(View.GONE);
                profilesPhotos.setVisibility(View.GONE);
                sendText.setVisibility(View.GONE);
                whoCanInviteBox.setVisibility(View.GONE);
            } else {
                guestBox.setVisibility(View.GONE);
                profilesPhotos.setVisibility(View.GONE);
                sendText.setVisibility(View.GONE);
                whoCanInviteBox.setVisibility(View.GONE);
                sendBox.setVisibility(View.GONE);
            }
        } else {
            if (send_toAll == 0) {
                sendBox.setVisibility(View.VISIBLE);
                sendText.setVisibility(View.VISIBLE);
                whoCanInviteBox.setVisibility(View.GONE);
            } else {
                guestBox.setVisibility(View.VISIBLE);
                sendBox.setVisibility(View.VISIBLE);
                profilesPhotos.setVisibility(View.VISIBLE);
                sendText.setVisibility(View.VISIBLE);
                whoCanInviteBox.setVisibility(View.GONE);
            }
        }

    }

    private void getUser(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getProfile(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(User user) {
        FlagActivity flagActivity = (FlagActivity) getActivity();
        if (!isEdit) {
            data = new ArrayList<>();
            user.setDelete(false);
            data.add(user);
            User friend = flagActivity.getUserFriend();
            ArrayList<User> list = flagActivity.getListUserCompare();
            if (friend != null) {
                friend.setDelete(false);
                data.add(friend);
            } else if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    User usr = list.get(i);
                    usr.setDelete(false);
                    data.add(usr);
                }
            }

            data = setOrderGuests(data);

            adapter = new PersonAdapter(data, getActivity());
            recyclerView.setAdapter(adapter);
            guestsNumber.setText(String.valueOf(data.size()));
            addPersonButton.setActivated(true);
        }
    }

    private void handleError(Throwable error) {
        if (!Utilities.isDeviceOnline(getActivity()))
            Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == GUEST_UPDATE) {
                FlagActivity flagActivity = (FlagActivity) getActivity();
                flagActivity.refreshItems();
            } else if (requestCode == ADD_GUEST) {
                if (!isEdit) {
                    ArrayList<User> list = new ArrayList<>();
                    PersonModelWrapper wrap =
                            (PersonModelWrapper) intent.getSerializableExtra("guest_objs");

                    list.add(data.get(0));
                    list.addAll(wrap.getItemDetails());
                    for (int i = 0; i < list.size(); i++) {
                        User usr = list.get(i);
                        usr.setDelete(false);
                    }
                    adapter.swap(list);
                    guestsNumber.setText(String.valueOf(list.size()));
                } else {
                    FlagActivity flagActivity = (FlagActivity) getActivity();
                    ActivityServer activityServer = new ActivityServer();
                    SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    PersonModelWrapper wrap =
                            (PersonModelWrapper) intent.getSerializableExtra("guest_objs");


                    if (wrap.getItemDetails().size() > 0) {
                        activityServer.setId(flagActivity.getFlag().getId());
                        activityServer.setVisibility(Constants.FLAG);
                        activityServer.setCreator(mSharedPreferences.getString(Constants.EMAIL, ""));
                        for (int i = 0; i < wrap.getItemDetails().size(); i++)
                            activityServer.addGuest(wrap.getItemDetails().get(i).getEmail());

                        flagActivity.addGuestToFlag(activityServer);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog2");
        if (dpd != null) dpd.setOnDateSetListener(this);

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        if (tpd != null) tpd.setOnTimeSetListener(this);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day + "/" + month + "/" + year;


        if (viewClicked == dateStart) {
            day_start = dayOfMonth;
            month_start = monthOfYear;
            year_start = year;
            dateStart.setText(date);
        } else {
            day_end = dayOfMonth;
            month_end = monthOfYear;
            year_end = year;
            dateEnd.setText(date);
        }

        if (dateStart.getText().toString().matches("")) {
            clearDateStart.setVisibility(View.GONE);
        } else {
            clearDateStart.setVisibility(View.VISIBLE);
        }

        if (dateEnd.getText().toString().matches("")) {
            clearDateEnd.setVisibility(View.GONE);
        } else {
            clearDateEnd.setVisibility(View.VISIBLE);
        }

        setRepeatLastDate();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String time = hourString + ":" + minuteString;

        if (viewClicked == timeStart) {
            hour_start = hourOfDay;
            minutes_start = minute;
            timeStart.setText(time);
        } else {
            hour_end = hourOfDay;
            minutes_end = minute;
            timeEnd.setText(time);
        }

        if (timeStart.getText().toString().matches("")) {
            clearTimeStart.setVisibility(View.GONE);
        } else {
            clearTimeStart.setVisibility(View.VISIBLE);
        }

        if (timeEnd.getText().toString().matches("")) {
            clearTimeEnd.setVisibility(View.GONE);
        } else {
            clearTimeEnd.setVisibility(View.VISIBLE);
        }

    }

    public List<Integer> getDateFromView() {
        List<Integer> list = new ArrayList<>();

        list.add(day_start);
        list.add(month_start);
        list.add(year_start);

        list.add(day_end);
        list.add(month_end);
        list.add(year_end);

        list.add(minutes_start);
        list.add(hour_start);

        list.add(minutes_end);
        list.add(hour_end);

        return list;
    }

    public String getTitleFromView() {
        return titleEditText.getText().toString();
    }

    public int getSendToAll() {
        return send_toAll;
    }

    public List<User> getGuestFromView() {
        return data;
    }

    public boolean getRepeatBlocked() {
        return repeat_blocked;
    }

    public List<Integer> getRepeatFromView() {
        List<Integer> list = new ArrayList<>();
        String temp = repeatEditText.getText().toString();
        if (repeat_type == 0)
            repeat_qty = -1;
        else if (temp.length() > 0)
            repeat_qty = Integer.parseInt(temp);

        list.add(repeat_type);
        list.add(repeat_qty);
        return list;
    }

    @Override
    public void onClick(View v) {
        viewClicked = v;
        if (v == repeatAdd) {
            v.findViewById(R.id.progressRepeatAdd).setVisibility(View.VISIBLE);
            repeatAddIcon.setVisibility(View.INVISIBLE);
            repeatAddText.setVisibility(View.INVISIBLE);

            repeatAdd.postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.findViewById(R.id.progressRepeatAdd).setVisibility(View.GONE);
                    repeatAdd.setVisibility(View.GONE);
                    repeatBox.setVisibility(View.VISIBLE);
                }
            }, 700);
        } else if (v == timeStart) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    FlagEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == timeEnd) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    FlagEditFragment.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            if (hour_end != -1)
                tpd.setStartTime(hour_end, minutes_end);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            tpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == dateStart) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FlagEditFragment.this,
                    year_start,
                    month_start,
                    day_start
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == dateEnd) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    FlagEditFragment.this,
                    year_end,
                    month_end,
                    day_end
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == clearDateStart || v == clearDateEnd || v == clearTimeStart || v == clearTimeEnd) {

            if (v == clearDateStart) {
                day_start = -1;
                month_start = -1;
                year_start = -1;
                dateStart.setText("");
                clearDateStart.setVisibility(View.GONE);
                setRepeatLastDate();
            } else if (v == clearDateEnd) {
                day_end = -1;
                month_end = -1;
                year_end = -1;
                dateEnd.setText("");
                clearDateEnd.setVisibility(View.GONE);
                setRepeatLastDate();
            } else if (v == clearTimeStart) {
                hour_start = -1;
                minutes_start = -1;
                timeStart.setText("");
                clearTimeStart.setVisibility(View.GONE);
            } else if (v == clearTimeEnd) {
                minutes_end = -1;
                hour_end = -1;
                timeEnd.setText("");
                clearTimeEnd.setVisibility(View.GONE);
            }

        } else if (v == guestBox && isEdit) {
            FlagActivity flagActivity = (FlagActivity) getActivity();

            Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
            intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
            intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
            intent.putExtra("is_adm", false);
            intent.putExtra("id_act", flagActivity.getFlag().getId());
            intent.putExtra("is_flag", true);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            startActivityForResult(intent, GUEST_UPDATE);

        } else if (v == addPersonButton || (v == guestBox && !isEdit)) {
            Intent intent = new Intent(getActivity(), SelectPeopleActivity.class);
            ArrayList<String> list = new ArrayList<>();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addPersonButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (!isEdit) {
                for (int i = 0; i < data.size(); i++) {
                    list.add(data.get(i).getEmail());
                }
            } else {
                for (int i = 0; i < listPerson.size(); i++) {
                    list.add(listPerson.get(i).getEmail());
                }
                intent.putExtra("erase_from_list", true);
            }

            intent.putStringArrayListExtra("guest_list", list);

            if (isInPast) {
                Calendar now = Calendar.getInstance();
                Calendar last = Calendar.getInstance();
                last.setTimeInMillis(flagServer.getLastDateTime());

                createDialogMessageAddInPast(last.get(Calendar.YEAR), last.get(Calendar.MONTH) + 1, last.get(Calendar.DAY_OF_MONTH), flagServer.getHourEnd(), flagServer.getMinuteEnd(),
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            } else {
                startActivityForResult(intent, ADD_GUEST);
            }
        }
    }

    public void setLayout(FlagServer flagServer, ArrayList<FlagServer> flagServers, ArrayList<User> users, ArrayList<User> confirmed, boolean edit, boolean free, boolean friend) {

        this.flagServer = flagServer;

        if (recyclerView != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());

            isEdit = edit;

            if (isEdit)
                addGuestBox.setVisibility(View.GONE);
            else
                addGuestBox.setVisibility(View.VISIBLE);

            String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            calendar.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());
            String day2 = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String month2 = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String date = day + "/" + month + "/" + flagServer.getYearStart();
            String date2 = day2 + "/" + month2 + "/" + flagServer.getYearEnd();

            if (flagServer.getHourStart() >= 0) {
                String hourString = String.format("%02d", flagServer.getHourStart());

                String minuteString = String.format("%02d", flagServer.getMinuteStart());

                String hourStringEnd = String.format("%02d", flagServer.getHourEnd());

                String minuteStringEnd = String.format("%02d", flagServer.getMinuteEnd());

                String time = hourString + ":" + minuteString;
                String time2 = hourStringEnd + ":" + minuteStringEnd;

                minutes_start = flagServer.getMinuteStart();
                hour_start = flagServer.getHourStart();
                minutes_end = flagServer.getMinuteEnd();
                hour_end = flagServer.getHourEnd();

                timeStart.setText(time);
                timeEnd.setText(time2);
            }

            last_date_time = flagServer.getLastDateTime();
            day_start = flagServer.getDayStart();
            month_start = flagServer.getMonthStart() - 1;
            year_start = flagServer.getYearStart();
            day_end = flagServer.getDayEnd();
            month_end = flagServer.getMonthEnd() - 1;
            year_end = flagServer.getYearEnd();

            dateStart.setText(date);
            dateEnd.setText(date2);

            titleEditText.setText(flagServer.getTitle());

            isInPast = isFlagInPast(flagServer);

            if (flagServer.getDateStartEmpty()) {
                day_start = -1;
                month_start = -1;
                year_start = -1;
                dateStart.setText("");
                clearDateStart.setVisibility(View.GONE);
            } else {
                clearDateStart.setVisibility(View.VISIBLE);
            }

            if (flagServer.getDateEndEmpty()) {
                day_end = -1;
                month_end = -1;
                year_end = -1;
                dateEnd.setText("");
                clearDateEnd.setVisibility(View.GONE);
            } else {
                clearDateEnd.setVisibility(View.VISIBLE);
            }

            if (flagServer.getTimeStartEmpty()) {
                hour_start = -1;
                minutes_start = -1;
                timeStart.setText("");
                clearTimeStart.setVisibility(View.GONE);
            } else {
                clearTimeStart.setVisibility(View.VISIBLE);
            }

            if (flagServer.getTimeEndEmpty()) {
                minutes_end = -1;
                hour_end = -1;
                timeEnd.setText("");
                clearTimeEnd.setVisibility(View.GONE);
            } else {
                clearTimeEnd.setVisibility(View.VISIBLE);
            }

            if (edit) {
                repeatBox.setVisibility(View.GONE);
                repeatAdd.setVisibility(View.GONE);

                sendBox.setVisibility(View.GONE);
                sendText.setVisibility(View.GONE);

                recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, MotionEvent e) {
                        FlagActivity flagActivity = (FlagActivity) getActivity();

                        Intent intent = new Intent(getActivity(), ShowGuestsActivity.class);
                        intent.putExtra("guest_list_user", new ListUserWrapper(listPerson));
                        intent.putExtra("confirmed_list_user", new ListUserWrapper(listConfirmed));
                        intent.putExtra("is_adm", false);
                        intent.putExtra("id_act", flagActivity.getFlag().getId());
                        intent.putExtra("is_flag", true);

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "guest_list_user" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        startActivityForResult(intent, GUEST_UPDATE);
                    }

                    @Override
                    public void onLongItemClick(View view, int position, MotionEvent e) {
                    }
                }));

                if (flagServer.getType()) {
                    guestBox.setVisibility(View.VISIBLE);
                    profilesPhotos.setVisibility(View.VISIBLE);
                    whoCanInviteBox.setVisibility(View.VISIBLE);

                    listPerson.clear();
                    listConfirmed.clear();
                    listConfirmed.addAll(confirmed);

                    for (int i = 0; i < users.size(); i++) {
                        User usr = users.get(i);
                        usr.setDelete(false);
                        listPerson.add(usr);
                    }

                    listPerson = setOrderGuests(listPerson);

                    adapter = new PersonAdapter(listPerson, getActivity());
                    recyclerView.setAdapter(adapter);
                    guestsNumber.setText(String.valueOf(listPerson.size()));
                    addPersonButton.setActivated(true);
                } else {
                    guestBox.setVisibility(View.GONE);
                    profilesPhotos.setVisibility(View.GONE);
                }

                if (flagServer.getRepeatType() == 0) {
                    repeatAdd.setVisibility(View.VISIBLE);
                    repeatText.setVisibility(View.GONE);
                } else {
                    repeat_blocked = true;
                    String repeatly;
                    switch (flagServer.getRepeatType()) {
                        case Constants.DAILY:
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
                    repeatText.setVisibility(View.VISIBLE);

                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(flagServer.getLastDateTime());
                    String dayOfWeek = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                    day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
                    month = new SimpleDateFormat("MM", this.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                    int year = calendar.get(Calendar.YEAR);
                    date = this.getResources().getString(R.string.date_format_03, dayOfWeek, day, month, year);
                    repeatText.setText(this.getString(R.string.repeat_text, repeatly, date));
                }

            } else if (!free) {
                sendPicker.setSelectedIndex(1);
                send_toAll = 1;
                guestBox.setVisibility(View.VISIBLE);
                profilesPhotos.setVisibility(View.VISIBLE);

                for (int i = 0; i < users.size(); i++) {
                    User usr = users.get(i);
                    usr.setDelete(false);
                    listPerson.add(usr);
                }

                listPerson = setOrderGuests(listPerson);

                adapter = new PersonAdapter(listPerson, getActivity());
                recyclerView.setAdapter(adapter);
                guestsNumber.setText(String.valueOf(listPerson.size()));

                if (flagServer.getRepeatType() == 0) {
                    spinner.setSelectedIndex(flagServer.getRepeatType());
                } else {
                    spinner.setSelectedIndex(flagServer.getRepeatType());
                    repeatNumberBox.setVisibility(View.VISIBLE);
                    repeatEditText.setText(String.valueOf(flagServer.getRepeatQty()));
                }
            } else if (friend) {
                sendPicker.setSelectedIndex(1);
                send_toAll = 1;
                guestBox.setVisibility(View.VISIBLE);
                profilesPhotos.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isFlagInPast(FlagServer flagServer) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        Calendar last = Calendar.getInstance();
        last.setTimeInMillis(flagServer.getLastDateTime());

        boolean isHourBefore = isTimeInBefore(hour + ":" + minute, flagServer.getHourEnd() + ":" + flagServer.getMinuteEnd());
        boolean isDateBefore = isDateInBefore(last.get(Calendar.YEAR), last.get(Calendar.MONTH) + 1, last.get(Calendar.DAY_OF_MONTH), year, month, day);

        return (isHourBefore && isDateBefore) || isDateBefore;
    }

    private boolean isDateInBefore(int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        if (yearEnd < year)
            return false;
        if (year == yearEnd) {
            if (monthOfYearEnd < monthOfYear)
                return false;
            else if (monthOfYearEnd == monthOfYear) {
                if (dayOfMonthEnd <= dayOfMonth)
                    return false;
            }
        }

        return true;
    }

    private boolean isTimeInBefore(String now, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date date1 = sdf.parse(now);
            Date date2 = sdf.parse(time);

            return date1.after(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptions != null)
            mSubscriptions.dispose();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == repeatAdd) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                repeatAddText.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
                repeatAddIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                repeatAddText.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_200));
                repeatAddIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.deep_purple_200));
            }
        } else if (view == guestBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                guestText.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_400));
                guestsNumber.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.box_qty_guests));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                guestText.setTextColor(ContextCompat.getColor(getActivity(), R.color.deep_purple_200));
                guestsNumber.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.box_qty_guests_pressed));
            }
        }

        return false;
    }
}
