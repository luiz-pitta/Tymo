package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.ReminderFragmentAdapter;
import io.development.tymo.fragments.ReminderEditFragment;
import io.development.tymo.fragments.ReminderShowFragment;
import io.development.tymo.model_server.ActivityOfDay;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.NotificationSyncJob;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateFields;

public class ReminderActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton, icon2, icon1, deleteIcon;
    private TextView m_title, reminderCardText, reminderCardTime, editButton;
    private RelativeLayout bottomBarBox;
    private TextView confirmationButton;
    private LinearLayout buttonsBar;

    private ArrayList<ReminderServer> reminderServers;

    private int d, m, y;
    private boolean edit = false;

    private FragmentNavigator mNavigator;

    private CompositeDisposable mSubscriptions;

    private FirebaseAnalytics mFirebaseAnalytics;

    private int type;
    private ReminderWrapper reminderWrapper;

    private boolean error;
    private TextView requiredText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        mSubscriptions = new CompositeDisposable();

        buttonsBar = (LinearLayout) findViewById(R.id.buttonsBar);
        icon2 = (ImageView) findViewById(R.id.icon2);
        icon1 = (ImageView) findViewById(R.id.icon1);
        deleteIcon = (ImageView) findViewById(R.id.deleteButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        bottomBarBox = (RelativeLayout) findViewById(R.id.confirmationButtonBar);
        m_title = (TextView) findViewById(R.id.text);
        requiredText = (TextView) findViewById(R.id.requiredText);
        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        reminderCardTime = (TextView) findViewById(R.id.reminderCardTime);
        reminderCardText = (TextView) findViewById(R.id.reminderCardText);
        editButton = (TextView) findViewById(R.id.editButton);


        reminderCardText.setText("");
        reminderCardTime.setText("");

        error = false;

        requiredText.setVisibility(View.GONE);

        type = getIntent().getIntExtra("type_reminder", 0);

        mNavigator = new FragmentNavigator(getFragmentManager(), new ReminderFragmentAdapter(), R.id.contentBox);
        mNavigator.setDefaultPosition(type);
        mNavigator.onCreate(savedInstanceState);
        mNavigator.showFragment(type);

        if (type == 0) {
            m_title.setText(getResources().getString(R.string.create_reminder));
            confirmationButton.setText(R.string.confirm);
            buttonsBar.setVisibility(View.GONE);
            icon2.setVisibility(View.INVISIBLE);
            icon1.setVisibility(View.GONE);
            reminderWrapper = (ReminderWrapper) getIntent().getSerializableExtra("reminder_edit");
            if (reminderWrapper == null)
                reminderWrapper = (ReminderWrapper) getIntent().getSerializableExtra("reminder_free_time");
            else {
                edit = true;
                m_title.setText(getResources().getString(R.string.edit_reminder));
                confirmationButton.setText(R.string.save_updates);
            }

            if (reminderWrapper != null) {
                ReminderServer reminderServer = reminderWrapper.getReminderServer();
                setReminderInformation(reminderServer.getId());
                setProgress(true);

                String hourString = String.format("%02d", reminderServer.getHourStart());
                String minuteString = String.format("%02d", reminderServer.getMinuteStart());
                String time;

                time = hourString + ":" + minuteString;

                if (reminderServer.getTitle() != null && !reminderServer.getTitle().matches(""))
                    reminderCardText.setText(reminderServer.getTitle());

                reminderCardTime.setText(time);
            }
        } else {
            m_title.setText(getResources().getString(R.string.reminder));
            reminderWrapper = (ReminderWrapper) getIntent().getSerializableExtra("reminder_show");
            if (reminderWrapper != null) {
                ReminderServer reminderServer = reminderWrapper.getReminderServer();

                String hourString = String.format("%02d", reminderServer.getHourStart());
                String minuteString = String.format("%02d", reminderServer.getMinuteStart());
                String time;

                time = hourString + ":" + minuteString;

                reminderCardText.setText(reminderServer.getTitle());
                reminderCardTime.setText(time);

                setReminderInformation(reminderWrapper.getReminderServer().getId());
                setProgress(true);
            }
            bottomBarBox.setVisibility(View.GONE);
            icon1.setVisibility(View.GONE);
            icon2.setImageResource(R.drawable.ic_edit);
        }

        mBackButton.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);
        confirmationButton.setOnClickListener(this);
        //icon1.setOnClickListener(this);
        icon2.setOnClickListener(this);
        editButton.setOnClickListener(this);
        editButton.setOnTouchListener(this);
        deleteIcon.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    public ReminderServer getReminder() {
        if (reminderWrapper != null)
            return reminderWrapper.getReminderServer();
        else
            return null;
    }

    public void setReminderCardText(String text) {
        reminderCardText.setText(text);
    }

    public void setReminderCardTime(String time) {
        reminderCardTime.setText(time);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        }
        else if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (!edit)
                register();
            else {
                if (getReminder().getRepeatType() > 0) {
                    List<Integer> date = ((ReminderEditFragment) mNavigator.getFragment(0)).getDateFromView();
                    int d = date.get(0);
                    int m = date.get(1) + 1;
                    int y = date.get(2);

                    if (sameDay(y, m, d, getReminder().getYearStart(), getReminder().getMonthStart(), getReminder().getDayStart()))
                        edit_reminder(false);
                    else
                        createDialogEditWithRepeat();
                } else
                    edit_reminder(false);
            }
        } else if (v == editButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "editButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(ReminderActivity.this, ReminderActivity.class);
            myIntent.putExtra("reminder_edit", reminderWrapper);
            startActivity(myIntent);
            finish();
        } else if (v == deleteIcon) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            createDialogRemove(getReminder().getRepeatType() > 0);
        }
    }

    private void createDialogRemove(boolean repeat) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);
        RadioGroup radioGroup = (RadioGroup) customView.findViewById(R.id.radioGroup);
        AppCompatRadioButton allRadioButton = (AppCompatRadioButton) customView.findViewById(R.id.allRadioButton);

        editText.setVisibility(View.GONE);

        allRadioButton.setText(getResources().getString(R.string.delete_plans_answer_all));

        if (repeat) {
            radioGroup.setVisibility(View.VISIBLE);
            radioGroup.setOrientation(LinearLayout.VERTICAL);
            buttonText1.setText(getResources().getString(R.string.cancel));
            buttonText2.setText(getResources().getString(R.string.confirm));
            text2.setVisibility(View.VISIBLE);
            text1.setText(getResources().getString(R.string.delete_plans_question_text_1));
            text2.setText(getResources().getString(R.string.delete_plans_question_text_2));
        } else {
            buttonText1.setText(getResources().getString(R.string.no));
            buttonText2.setText(getResources().getString(R.string.yes));
            text2.setVisibility(View.GONE);
            text1.setVisibility(View.VISIBLE);
            text1.setText(getResources().getString(R.string.delete_plans_question_text_3));
        }

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonID;
                View radioButton;
                int idx = -1;

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "deleteReminder" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (repeat) {
                    radioButtonID = radioGroup.getCheckedRadioButtonId();
                    radioButton = radioGroup.findViewById(radioButtonID);
                    idx = radioGroup.indexOfChild(radioButton);
                }

                ActivityServer activityServer = new ActivityServer();
                activityServer.setId(idx);
                activityServer.setVisibility(Constants.REMINDER);

                deleteFlagActReminder(getReminder().getId(), activityServer);

                dg.dismiss();
            }
        });

        dg.show();
    }

    private void deleteFlagActReminder(long id, ActivityServer activityServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit().deleteActivity(id, activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm, this::handleError));
    }

    private void handleDeleteIgnoreConfirm(Response response) {
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //ACTIVITY_DELETED_SUCCESSFULLY e WITHOUT_NOTIFICATION
        finish();
    }

    private void register() {

        String title = ((ReminderEditFragment) mNavigator.getFragment(0)).getTitleFromView();
        List<Integer> date = ((ReminderEditFragment) mNavigator.getFragment(0)).getDateFromView();
        List<Integer> repeat = ((ReminderEditFragment) mNavigator.getFragment(0)).getRepeatFromView();


        int err = 0;

        if (!validateFields(title)) {

            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_title_required, Toast.LENGTH_LONG).show();
        } else if (date.size() == 0 || date.get(0) == -1 || date.get(3) == -1) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_hour_required, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) != 0 && repeat.get(1) < 0)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
        } else if (repeat.get(1) == 0 || repeat.get(1) > 30) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_min_max, Toast.LENGTH_LONG).show();
        }
        if (err == 0) {

            int repeat_type = repeat.get(0);
            int repeat_qty = repeat.get(1);
            List<Integer> day_list_start = new ArrayList<>();
            List<Integer> month_list_start = new ArrayList<>();
            List<Integer> year_list_start = new ArrayList<>();
            List<Long> date_time_list_start = new ArrayList<>();

            if (repeat_type > 0) {
                int repeat_adder = getRepeatAdder(repeat_type);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                cal.set(date.get(2), date.get(1), date.get(0), date.get(4), date.get(3));

                for (int i = 0; i < repeat_qty; i++) {
                    day_list_start.add(cal.get(Calendar.DAY_OF_MONTH));
                    month_list_start.add(cal.get(Calendar.MONTH) + 1);
                    year_list_start.add(cal.get(Calendar.YEAR));

                    date_time_list_start.add(cal.getTimeInMillis());

                    if (repeat_type == Constants.MONTHLY)
                        cal.add(Calendar.MONTH, 1);
                    else
                        cal.add(Calendar.DAY_OF_WEEK, repeat_adder);

                }

            }

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String creator = mSharedPreferences.getString(Constants.EMAIL, "");
            ReminderServer reminderServer = new ReminderServer();
            reminderServer.setTitle(title);

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            reminderServer.setCreator(creator);
            reminderServer.setDayStart(date.get(0));
            reminderServer.setMonthStart(date.get(1) + 1);
            reminderServer.setYearStart(date.get(2));
            reminderServer.setMinuteStart(date.get(3));
            reminderServer.setHourStart(date.get(4));

            reminderServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart(), reminderServer.getHourStart(), reminderServer.getMinuteStart());
            reminderServer.setDateTimeStart(calendar.getTimeInMillis());

            reminderServer.setRepeatType(repeat.get(0));
            reminderServer.setRepeatQty(repeat.get(1));
            reminderServer.setDayListStart(day_list_start);
            reminderServer.setMonthListStart(month_list_start);
            reminderServer.setYearListStart(year_list_start);

            reminderServer.setDateTimeListStart(date_time_list_start);

            registerProcess(reminderServer);

        } else {
            error = true;
            ((ReminderEditFragment) mNavigator.getFragment(0)).updateError(error);
            //requiredText.setVisibility(View.VISIBLE);
            showSnackBarMessage(getResources().getString(R.string.validation_field_required_fill_correctly));
        }
    }

    private int getFutureActivities(List<Integer> date) {
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

        ArrayList<ReminderServer> list = new ArrayList<>();
        for (int i = 0; i < reminderServers.size(); i++) {
            ReminderServer rem = reminderServers.get(i);
            if (!isDatePrior(rem.getDayStart(), rem.getMonthStart(), rem.getYearStart(), getReminder().getDayStart(), getReminder().getMonthStart(), getReminder().getYearStart()))
                list.add(rem);
        }

        return list.size();
    }

    private boolean isDatePrior(int d1, int m1, int y1, int d2, int m2, int y2) {
        if (y1 < y2)
            return true;
        else if (y1 == y2) {
            if (m1 < m2)
                return true;
            else if (m1 == m2)
                if (d1 < d2)
                    return true;
        }
        return false;
    }

    private void edit_reminder(boolean repeat) {

        String title = ((ReminderEditFragment) mNavigator.getFragment(0)).getTitleFromView();
        List<Integer> date = ((ReminderEditFragment) mNavigator.getFragment(0)).getDateFromView();
        List<Integer> repeat_single = ((ReminderEditFragment) mNavigator.getFragment(0)).getRepeatFromView();

        int err = 0;
        int repeat_type = getReminder().getRepeatType();
        boolean repeat_single_changed = false;

        if (repeat_type == 0 && repeat_type != repeat_single.get(0)) {
            repeat_type = repeat_single.get(0);
            repeat_single_changed = true;
            repeat = true;
            if ((repeat_single.get(0) != 0 && repeat_single.get(1) < 0)) {
                err++;
                Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
            } else if (repeat_single.get(1) == 0 || repeat_single.get(1) > 30) {
                err++;
                Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_min_max, Toast.LENGTH_LONG).show();
            }
        }

        if (err == 0) {

            ActivityServer activityServer = new ActivityServer();
            activityServer.setTitle(title);
            int repeat_left = -1;

            List<Integer> day_list_start = new ArrayList<>();
            List<Integer> month_list_start = new ArrayList<>();
            List<Integer> year_list_start = new ArrayList<>();
            List<Long> date_time_list_start = new ArrayList<>();

            if (repeat_type > 0) {
                int repeat_adder = getRepeatAdder(repeat_type);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                cal.set(date.get(2), date.get(1), date.get(0), date.get(4), date.get(3));

                if (!repeat_single_changed)
                    repeat_left = getFutureActivities(date);
                else
                    repeat_left = repeat_single.get(1);

                for (int i = 0; i < repeat_left; i++) {
                    day_list_start.add(cal.get(Calendar.DAY_OF_MONTH));
                    month_list_start.add(cal.get(Calendar.MONTH) + 1);
                    year_list_start.add(cal.get(Calendar.YEAR));

                    date_time_list_start.add(cal.getTimeInMillis());

                    if (repeat_type == Constants.MONTHLY)
                        cal.add(Calendar.MONTH, 1);
                    else
                        cal.add(Calendar.DAY_OF_WEEK, repeat_adder);

                }

            }

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            activityServer.setId(getReminder().getId());

            activityServer.setDayStart(date.get(0));
            activityServer.setMonthStart(date.get(1) + 1);
            activityServer.setYearStart(date.get(2));
            activityServer.setMinuteStart(date.get(3));
            activityServer.setHourStart(date.get(4));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar.getTimeInMillis());

            activityServer.setDayListStart(day_list_start);
            activityServer.setMonthListStart(month_list_start);
            activityServer.setYearListStart(year_list_start);

            activityServer.setDateTimeListStart(date_time_list_start);

            if (repeat_type > 0) {
                if (sameDay(y, m+1, d, getReminder().getYearStart(), getReminder().getMonthStart(), getReminder().getDayStart())) // So editar os dados
                    activityServer.setVisibility(1);
                else
                    activityServer.setVisibility(2);
            } else
                activityServer.setVisibility(0);


            if (repeat) { //ver se edita só esse ou todas repetições
                activityServer.setRepeatType(repeat_type);
                activityServer.setRepeatQty(repeat_left); //tamanho que sobra com as atividades futuras
            } else {
                activityServer.setRepeatType(0);
                activityServer.setRepeatQty(-1);
            }

            if (!repeat_single_changed)
                editReminder(activityServer);
            else
                editReminderRepeatSingle(activityServer);


        } else {
            error = true;
            //requiredText.setVisibility(View.VISIBLE);
        }
    }

    private boolean sameDay(int y1, int m1, int d1, int y2, int m2, int d2) {
        return d1 == d2 && m1 == m2 && y1 == y2;
    }

    private int getRepeatAdder(int type) {
        switch (type) {
            case Constants.DAYLY:
                return 1;
            case Constants.WEEKLY:
                return 7;
            default:
                return 0;
        }
    }

    private void registerProcess(ReminderServer reminderServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerReminder(reminderServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void setReminderInformation(long id) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getFlagReminder(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleReminderInformation, this::handleError));
    }

    private void handleReminderInformation(Response response) {
        setProgress(false);
        reminderServers = response.getMyCommitReminder();

        ReminderShowFragment reminderShowFragment = (ReminderShowFragment) mNavigator.getFragment(1);
        if (reminderShowFragment != null)
            reminderShowFragment.setLayout(getReminder(), reminderServers);

    }

    private void editReminder(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editReminder(getReminder().getId(), activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void editReminderRepeatSingle(ActivityServer activityServer) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().editReminderRepeatSingle(getReminder().getId(), activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int year2 = c2.get(Calendar.YEAR);

        if(getReminder()!=null) {
            if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
                getActivityStartToday();
        }

        Intent intent = new Intent();
        intent.putExtra("d", d);
        intent.putExtra("m", m);
        intent.putExtra("y", y);
        setResult(RESULT_OK, intent);
        finish();

    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    private void showSnackBarMessage(String message) {

        if (findViewById(android.R.id.content) != null) {

            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    private void createDialogEditWithRepeat() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);
        RadioGroup radioGroup = (RadioGroup) customView.findViewById(R.id.radioGroup);

        editText.setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.popup_message_edit_commitments_with_repetitions));
        text2.setVisibility(View.GONE);
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.confirm));

        radioGroup.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
        text2.setText(getResources().getString(R.string.popup_message_edit_select_which_one_to_modify));

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonID;
                View radioButton;
                int idx = -1;

                radioButtonID = radioGroup.getCheckedRadioButtonId();
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);

                edit_reminder(idx == 1);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.popup_message_edit_commitments_with_repetitions) + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dg.dismiss();
            }
        });

        dg.show();
    }

    private void getActivityStartToday(){
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        Query query = new Query();
        query.setEmail(email);
        query.setDay(day);
        query.setMonth(month);
        query.setYear(year);
        query.setHourStart(hour);
        query.setMinuteStart(minute);

        setNotifications(query);
    }

    private void setNotifications(Query query) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getActivityStartToday(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseToday, this::handleErrorToday));
    }

    private void handleResponseToday(Response response) {

        JobManager mJobManager = JobManager.instance();
        if(mJobManager.getAllJobRequestsForTag(NotificationSyncJob.TAG).size() > 0)
            mJobManager.cancelAllForTag(NotificationSyncJob.TAG);

        ArrayList<Object> list = new ArrayList<>();
        ArrayList<ActivityOfDay> list_notify = new ArrayList<>();

        if (response.getMyCommitAct() != null) {
            ArrayList<ActivityServer> activityServers = response.getMyCommitAct();
            for(int i=0;i<activityServers.size();i++){
                list.add(activityServers.get(i));
            }
        }
        if (response.getMyCommitFlag() != null) {
            ArrayList<FlagServer> flagServers = response.getMyCommitFlag();
            for(int i=0;i<flagServers.size();i++){
                list.add(flagServers.get(i));
            }
        }
        if (response.getMyCommitReminder() != null) {
            list.addAll(response.getMyCommitReminder());
        }

        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(Object c1, Object c2) {
                ActivityServer activityServer;
                FlagServer flagServer;
                ReminderServer reminderServer;
                int start_hour = 0, start_minute = 0;
                int start_hour2 = 0, start_minute2 = 0;
                int end_hour = 0, end_minute = 0;
                int end_hour2 = 0, end_minute2 = 0;
                int day = 0, day2 = 0;
                int month = 0, month2 = 0;
                int year = 0, year2 = 0;

                // Activity
                if (c1 instanceof ActivityServer) {
                    activityServer = (ActivityServer) c1;
                    start_hour = activityServer.getHourStart();
                    start_minute = activityServer.getMinuteStart();
                    end_hour = activityServer.getHourEnd();
                    end_minute = activityServer.getMinuteEnd();

                    day = activityServer.getDayStart();
                    month = activityServer.getMonthStart();
                    year = activityServer.getYearStart();
                }
                // Flag
                else if (c1 instanceof FlagServer) {
                    flagServer = (FlagServer) c1;
                    start_hour = flagServer.getHourStart();
                    start_minute = flagServer.getMinuteStart();
                    end_hour = flagServer.getHourEnd();
                    end_minute = flagServer.getMinuteEnd();

                    day = flagServer.getDayStart();
                    month = flagServer.getMonthStart();
                    year = flagServer.getYearStart();
                }
                // Reminder
                else if (c1 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c1;
                    start_hour = reminderServer.getHourStart();
                    start_minute = reminderServer.getMinuteStart();

                    day = reminderServer.getDayStart();
                    month = reminderServer.getMonthStart();
                    year = reminderServer.getYearStart();
                }

                // Activity
                if (c2 instanceof ActivityServer) {
                    activityServer = (ActivityServer) c2;
                    start_hour2 = activityServer.getHourStart();
                    start_minute2 = activityServer.getMinuteStart();
                    end_hour2 = activityServer.getHourEnd();
                    end_minute2 = activityServer.getMinuteEnd();

                    day2 = activityServer.getDayStart();
                    month2 = activityServer.getMonthStart();
                    year2 = activityServer.getYearStart();
                }
                // Flag
                else if (c2 instanceof FlagServer) {
                    flagServer = (FlagServer) c2;
                    start_hour2 = flagServer.getHourStart();
                    start_minute2 = flagServer.getMinuteStart();
                    end_hour2 = flagServer.getHourEnd();
                    end_minute2 = flagServer.getMinuteEnd();

                    day2 = flagServer.getDayStart();
                    month2 = flagServer.getMonthStart();
                    year2 = flagServer.getYearStart();
                }
                // Reminder
                else if (c2 instanceof ReminderServer) {
                    reminderServer = (ReminderServer) c2;
                    start_hour2 = reminderServer.getHourStart();
                    start_minute2 = reminderServer.getMinuteStart();

                    day2 = reminderServer.getDayStart();
                    month2 = reminderServer.getMonthStart();
                    year2 = reminderServer.getYearStart();

                }

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
                else if (start_hour < start_hour2)
                    return -1;
                else if (start_hour > start_hour2)
                    return 1;
                else if (start_minute < start_minute2)
                    return -1;
                else if (start_minute > start_minute2)
                    return 1;
                else if (end_hour < end_hour2)
                    return -1;
                else if (end_hour > end_hour2)
                    return 1;
                else if (end_minute < end_minute2)
                    return -1;
                else if (end_minute > end_minute2)
                    return 1;
                else
                    return 0;

            }
        });

        for (int i = 0; i < list.size(); i++) {
            // Activity
            if (list.get(i) instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) list.get(i);
                list_notify.add(new ActivityOfDay(activityServer.getTitle(), activityServer.getMinuteStart(), activityServer.getHourStart(), Constants.ACT,
                        activityServer.getDayStart(),activityServer.getMonthStart(),activityServer.getYearStart()));
            }
            // Flag
            else if (list.get(i) instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) list.get(i);
                list_notify.add(new ActivityOfDay(flagServer.getTitle(), flagServer.getMinuteStart(), flagServer.getHourStart(), Constants.FLAG,
                        flagServer.getDayStart(),flagServer.getMonthStart(),flagServer.getYearStart()));
            }
            // Reminder
            else if (list.get(i) instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) list.get(i);
                list_notify.add(new ActivityOfDay(reminderServer.getTitle(), reminderServer.getMinuteStart(), reminderServer.getHourStart(), Constants.REMINDER,
                        reminderServer.getDayStart(),reminderServer.getMonthStart(),reminderServer.getYearStart()));
            }
        }

        int time_exact;
        long time_to_happen;
        Calendar c3 = Calendar.getInstance();

        for (int i = 0; i < list_notify.size(); i++) {
            PersistableBundleCompat extras = new PersistableBundleCompat();
            extras.putInt("position_act", i);

            PersistableBundleCompat extras2 = new PersistableBundleCompat();
            extras2.putInt("position_act", i);
            extras2.putBoolean("day_before", true);

            int j = i;
            int count_same = 0;
            ActivityOfDay activityOfDay = list_notify.get(i);
            ActivityOfDay activityOfDayNext = list_notify.get(j);

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            c1.set(Calendar.DAY_OF_MONTH, activityOfDay.getDay());
            c1.set(Calendar.MONTH, activityOfDay.getMonth()-1);
            c1.set(Calendar.YEAR, activityOfDay.getYear());
            c1.set(Calendar.HOUR_OF_DAY, activityOfDay.getHourStart());
            c1.set(Calendar.MINUTE, activityOfDay.getMinuteStart());
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
            c2.set(Calendar.MONTH, activityOfDayNext.getMonth()-1);
            c2.set(Calendar.YEAR, activityOfDayNext.getYear());
            c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
            c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);

            while(activityOfDayNext !=null && c1.getTimeInMillis() == c2.getTimeInMillis()) {
                j++;
                count_same++;
                if(j < list_notify.size()) {
                    activityOfDayNext = list_notify.get(j);
                    c2.set(Calendar.DAY_OF_MONTH, activityOfDayNext.getDay());
                    c2.set(Calendar.MONTH, activityOfDayNext.getMonth()-1);
                    c2.set(Calendar.YEAR, activityOfDayNext.getYear());
                    c2.set(Calendar.HOUR_OF_DAY, activityOfDayNext.getHourStart());
                    c2.set(Calendar.MINUTE, activityOfDayNext.getMinuteStart());
                    c2.set(Calendar.SECOND, 0);
                    c2.set(Calendar.MILLISECOND, 0);
                }
                else
                    activityOfDayNext = null;
            }
            activityOfDay.setCommitmentSameHour(count_same);

            time_exact = (int)(c1.getTimeInMillis()-c3.getTimeInMillis())/(1000*60);
            if(time_exact >= Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT) {
                c1.add(Calendar.MINUTE, -Constants.MINUTES_NOTIFICATION_BEFORE_START_COMMITMENT);
                time_to_happen = c1.getTimeInMillis()-c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            if(time_exact >= 1440) {
                c1.add(Calendar.MINUTE, -1380);
                time_to_happen = c1.getTimeInMillis()-c3.getTimeInMillis();
                new JobRequest.Builder(NotificationSyncJob.TAG)
                        .setExact(time_to_happen)
                        .setExtras(extras2)
                        .setPersisted(true)
                        .build()
                        .schedule();
            }

            i=j-1;
        }

        if (list_notify.size() > 0) {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).edit();
            Gson gson = new Gson();
            String json = gson.toJson(list_notify);
            editor.putString("ListActDay", json);
            editor.apply();
        }
    }

    private void handleErrorToday(Throwable error) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        }
        else if (view == editButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                editButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        }

        return false;
    }
}
