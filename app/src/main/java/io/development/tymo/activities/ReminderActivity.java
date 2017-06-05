package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.ReminderFragmentAdapter;
import io.development.tymo.fragments.ReminderEditFragment;
import io.development.tymo.fragments.ReminderShowFragment;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static io.development.tymo.utils.Validation.validateFields;

public class ReminderActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton, icon2, icon1, deleteIcon;
    private TextView m_title, reminderCardText, reminderCardTime;
    private RelativeLayout bottomBarBox;
    private TextView confirmationButton;
    private LinearLayout buttonsBar;

    private ArrayList<ReminderServer> reminderServers;

    private int d, m, y;
    private boolean edit = false;

    private FragmentNavigator mNavigator;

    private CompositeSubscription mSubscriptions;

    private FirebaseAnalytics mFirebaseAnalytics;

    private int type;
    private ReminderWrapper reminderWrapper;

    private boolean error;
    private TextView requiredText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        mSubscriptions = new CompositeSubscription();

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
        confirmationButton.setOnClickListener(this);
        //icon1.setOnClickListener(this);
        icon2.setOnClickListener(this);
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
        } else if (v == icon2) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "icon2" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
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

        allRadioButton.setText(getResources().getString(R.string.dialog_delete_plans_all));

        if (repeat) {
            radioGroup.setVisibility(View.VISIBLE);
            radioGroup.setOrientation(LinearLayout.VERTICAL);
            buttonText1.setText(getResources().getString(R.string.cancel));
            buttonText2.setText(getResources().getString(R.string.confirm));
            text2.setVisibility(View.VISIBLE);
            text1.setText(getResources().getString(R.string.dialog_delete_plans_text));
            text2.setText(getResources().getString(R.string.dialog_delete_plans_text2));
        } else {
            buttonText1.setText(getResources().getString(R.string.no));
            buttonText2.setText(getResources().getString(R.string.yes));
            text2.setVisibility(View.GONE);
            text1.setVisibility(View.VISIBLE);
            text1.setText(getResources().getString(R.string.dialog_delete_plans_title));
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
            showSnackBarMessage(getResources().getString(R.string.fill_fields_correctly));
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
                if (sameDay(y, m, d, getReminder().getYearStart(), getReminder().getMonthStart(), getReminder().getDayStart())) // So editar os dados
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

        Intent intent = new Intent();
        intent.putExtra("d", d);
        intent.putExtra("m", m);
        intent.putExtra("y", y);
        setResult(RESULT_OK, intent);
        finish();

    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    private void showSnackBarMessage(String message) {

        if (findViewById(android.R.id.content) != null) {

            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
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

        text1.setText(getResources().getString(R.string.dialog_edit_reminder_title));
        text2.setVisibility(View.GONE);
        buttonText1.setText(getResources().getString(R.string.cancel));
        buttonText2.setText(getResources().getString(R.string.confirm));

        radioGroup.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
        text2.setText(getResources().getString(R.string.dialog_edit_reminder_text));

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
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.dialog_edit_reminder_title) + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dg.dismiss();
            }
        });

        dg.show();
    }
}
