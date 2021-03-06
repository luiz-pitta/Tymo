package io.development.tymo.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.Space;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.labo.kaji.swipeawaydialog.SwipeAwayDialogFragment;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.R;
import io.development.tymo.activities.AddPart1Activity;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.ReminderActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.HolidayCardAdapter;
import io.development.tymo.adapters.PersonAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.Birthday;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.Holiday;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.models.cards.ActivityCard;
import io.development.tymo.models.cards.BirthdayCard;
import io.development.tymo.models.cards.Flag;
import io.development.tymo.models.cards.HolidayCard;
import io.development.tymo.models.cards.Reminder;
import io.development.tymo.network.NetworkUtil;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

public class CreatePopUpDialogFragment extends SwipeAwayDialogFragment {

    private static int screen;
    private static User friend;
    private static Object obj;
    private static boolean alone = false;
    private static boolean compare = false;
    private static RefreshLayoutPlansCallback callback;
    private static ArrayList<User> listFriends = new ArrayList<>();

    private interface DialogBuilder {
        @NonNull
        Dialog create(Context context, CreatePopUpDialogFragment fragment);
    }

    public enum Type implements DialogBuilder {
        CUSTOM() {

            private Context mContext;
            private DateFormat dateFormat;
            private CompositeDisposable mSubscriptions;
            private View horizontalLine;
            private LinearLayout buttonsBox,button2,contentBox;
            private ProgressBar contentProgressBar;
            private RelativeLayout cardTitleBox;
            private TextView buttonText2;
            private ImageView buttonIcon2;
            private RecyclerView recyclerView;
            private FlagServer flagServer;
            private ActivityServer activityServer;
            private ReminderServer reminderServer;
            private Space spaceTop,spaceBottom;
            private int invitedList;

            private FirebaseAnalytics mFirebaseAnalytics;

            private void setColorCard(int color) {
                int viewColor = 0;

                if (color == ContextCompat.getColor(mContext, R.color.red_A700))
                    viewColor = R.drawable.bg_dialog_card_red_a700;
                else if (color == ContextCompat.getColor(mContext, R.color.pink_400))
                    viewColor = R.drawable.bg_dialog_card_pink_400;
                else if (color == ContextCompat.getColor(mContext, R.color.pink_900))
                    viewColor = R.drawable.bg_dialog_card_pink_900;
                else if (color == ContextCompat.getColor(mContext, R.color.purple_500))
                    viewColor = R.drawable.bg_dialog_card_purple_500;
                else if (color == ContextCompat.getColor(mContext, R.color.deep_purple_400))
                    viewColor = R.drawable.bg_dialog_card_deep_purple_400;
                else if (color == ContextCompat.getColor(mContext, R.color.deep_purple_800))
                    viewColor = R.drawable.bg_dialog_card_deep_purple_800;
                else if (color == ContextCompat.getColor(mContext, R.color.blue_400))
                    viewColor = R.drawable.bg_dialog_card_blue_400;
                else if (color == ContextCompat.getColor(mContext, R.color.blue_800))
                    viewColor = R.drawable.bg_dialog_card_blue_800;
                else if (color == ContextCompat.getColor(mContext, R.color.cyan_400))
                    viewColor = R.drawable.bg_dialog_card_cyan_400;
                else if (color == ContextCompat.getColor(mContext, R.color.cyan_800))
                    viewColor = R.drawable.bg_dialog_card_cyan_800;
                else if (color == ContextCompat.getColor(mContext, R.color.green_400))
                    viewColor = R.drawable.bg_dialog_card_green_400;
                else if (color == ContextCompat.getColor(mContext, R.color.lime_600))
                    viewColor = R.drawable.bg_dialog_card_lime_600;
                else if (color == ContextCompat.getColor(mContext, R.color.deep_orange_400))
                    viewColor = R.drawable.bg_dialog_card_deep_orange_400;
                else if (color == ContextCompat.getColor(mContext, R.color.brown_400))
                    viewColor = R.drawable.bg_dialog_card_brown_400;
                else if (color == ContextCompat.getColor(mContext, R.color.brown_700))
                    viewColor = R.drawable.bg_dialog_card_brown_700;
                else if (color == ContextCompat.getColor(mContext, R.color.grey_500))
                    viewColor = R.drawable.bg_dialog_card_grey_500;
                else if (color == ContextCompat.getColor(mContext, R.color.blue_grey_500))
                    viewColor = R.drawable.bg_dialog_card_blue_grey_500;
                else if (color == ContextCompat.getColor(mContext, R.color.blue_grey_900))
                    viewColor = R.drawable.bg_dialog_card_blue_grey_900;
                else if (color == R.color.flag_unavailable)
                    viewColor = R.drawable.bg_dialog_card_flag_unavailable;
                else if (color == R.color.flag_available)
                    viewColor = R.drawable.bg_dialog_card_flag_available;
                else if (color == ContextCompat.getColor(mContext, R.color.facebook_dark_blue))
                    viewColor = R.drawable.bg_dialog_card_facebook;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    cardTitleBox.setBackground(ContextCompat.getDrawable(mContext, viewColor));
                } else {
                    cardTitleBox.setBackgroundDrawable(ContextCompat.getDrawable(mContext, viewColor));
                }
            }

            private void setFlagInformation(long id) {
                setProgress(true, false);
                mSubscriptions.add(NetworkUtil.getRetrofit().getFlagReminder(id)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::handleResponse, this::handleError));
            }

            private void deleteFlagActReminder(long id, ActivityServer activity) {
                if (screen == Utilities.TYPE_PLANS)
                    refreshScreen(mContext);

                mSubscriptions.add(NetworkUtil.getRetrofit().deleteActivity(id, activity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::handleDeleteIgnoreConfirm, this::handleError));
            }

            private void setActivityInformation(long id) {
                setProgress(true, false);
                mSubscriptions.add(NetworkUtil.getRetrofit().getActivity(id)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::handleResponse, this::handleError));
            }

            private void updateInviteRequest(InviteRequest inviteRequest) {
                if (screen == Utilities.TYPE_PLANS)
                    refreshScreen(mContext);

                mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::handleDeleteIgnoreConfirm, this::handleError));
            }

            private void handleDeleteIgnoreConfirm(Response response) {
                //Toast.makeText(mContext, ServerMessage.getServerMessage(mContext, response.getMessage()), Toast.LENGTH_LONG).show();
                //ACTIVITY_DELETED_SUCCESSFULLY, RELATIONSHIP_UPDATED_SUCCESSFULLY e WITHOUT_NOTIFICATION

                if (callback != null) {
                    Calendar c = Calendar.getInstance();
                    int day = c.get(Calendar.DAY_OF_MONTH);
                    int month = c.get(Calendar.MONTH) + 1;
                    int year = c.get(Calendar.YEAR);

                    Calendar c2 = Calendar.getInstance();
                    c2.add(Calendar.DATE, 1);
                    int day2 = c2.get(Calendar.DAY_OF_MONTH);
                    int month2 = c2.get(Calendar.MONTH) + 1;
                    int year2 = c2.get(Calendar.YEAR);

                    int d;
                    int m;
                    int y;

                    callback.refreshLayout(true);

                    updateFeedMessageToActivity(mContext);

                    if (activityServer != null) {
                        d = activityServer.getDayStart();
                        m = activityServer.getMonthStart();
                        y = activityServer.getYearStart();
                    } else if (flagServer != null) {
                        d = flagServer.getDayStart();
                        m = flagServer.getMonthStart();
                        y = flagServer.getYearStart();
                    } else if (reminderServer != null) {
                        d = reminderServer.getDayStart();
                        m = reminderServer.getMonthStart();
                        y = reminderServer.getYearStart();
                    } else {
                        d = day;
                        m = month;
                        y = year;
                    }

                    if ((d == day && m == month && y == year) || (d == day2 && m == month2 && y == year2))
                        updateNotificationStartToday(mContext);
                }
            }

            private boolean setLayout(ArrayList<User> list, String email, boolean activity) {
                boolean in = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getEmail().equals(email)) {
                        if (list.get(i).getInvitation() == 1) {
                            buttonIcon2.setImageResource(R.drawable.ic_trash);
                            buttonIcon2.setColorFilter(ContextCompat.getColor(mContext, R.color.red_600));
                            buttonText2.setText(mContext.getResources().getString(R.string.remove));
                            buttonText2.setTag(mContext.getResources().getString(R.string.undo_unfit));
                            buttonText2.setTextColor(ContextCompat.getColor(mContext, R.color.red_600));
                        } else {
                            buttonIcon2.setImageResource(R.drawable.ic_check);
                            buttonText2.setText(mContext.getResources().getString(R.string.fit));
                            buttonText2.setTextColor(ContextCompat.getColor(mContext, R.color.green_600));
                        }
                        in = true;
                        break;
                    }
                }
                if (activity && !in) {
                    if (activityServer.getInvitationType() == 2) {
                        buttonIcon2.setImageResource(R.drawable.ic_check);
                        buttonText2.setText(mContext.getResources().getString(R.string.fit));
                        buttonText2.setTextColor(ContextCompat.getColor(mContext, R.color.green_600));
                    } else {
                        buttonsBox.setVisibility(View.GONE);
                        horizontalLine.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }

            private void handleResponse(Response response) {
                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                boolean activity_buttons = false;

                if (response.getWhatsGoingAct() != null) {
                    if (email.equals(activityServer.getCreator())) {
                        buttonIcon2.setImageResource(R.drawable.ic_trash);
                        buttonIcon2.setColorFilter(ContextCompat.getColor(mContext, R.color.red_600));
                        buttonText2.setText(mContext.getResources().getString(R.string.remove));
                        buttonText2.setTag(mContext.getResources().getString(R.string.remove));
                        buttonText2.setTextColor(ContextCompat.getColor(mContext, R.color.red_600));
                    } else
                        activity_buttons = setLayout(response.getPeople(), email, true);

                    ArrayList<User> listPerson = new ArrayList<>();

                    for (int i = 0; i < response.getPeople().size(); i++) {
                        User usr = response.getPeople().get(i);
                        usr.setDelete(false);
                        usr.setCreator(usr.getEmail().contains(response.getUser().getEmail()));
                        listPerson.add(usr);
                    }

                    listPerson = setOrderGuests(listPerson);

                    invitedList = listPerson.size();

                    PersonAdapter adapter = new PersonAdapter(listPerson, mContext);
                    recyclerView.setAdapter(adapter);
                } else {
                    if (email.equals(flagServer.getCreator())) {
                        buttonIcon2.setImageResource(R.drawable.ic_trash);
                        buttonIcon2.setColorFilter(ContextCompat.getColor(mContext, R.color.red_600));
                        buttonText2.setText(mContext.getResources().getString(R.string.remove));
                        buttonText2.setTag(mContext.getResources().getString(R.string.remove));
                        buttonText2.setTextColor(ContextCompat.getColor(mContext, R.color.red_600));
                    } else {
                        setLayout(response.getPeople(), email, false);
                        if (!flagServer.getType()) {
                            buttonsBox.setVisibility(View.GONE);
                            horizontalLine.setVisibility(View.GONE);
                        }
                    }

                    ArrayList<User> listPerson = new ArrayList<>();

                    for (int i = 0; i < response.getPeople().size(); i++) {
                        User usr = response.getPeople().get(i);
                        usr.setDelete(false);
                        usr.setCreator(usr.getEmail().contains(response.getUser().getEmail()));
                        listPerson.add(usr);
                    }

                    listPerson = setOrderGuests(listPerson);

                    invitedList = listPerson.size();

                    PersonAdapter adapter = new PersonAdapter(listPerson, mContext);
                    recyclerView.setAdapter(adapter);
                }
                setProgress(false, activity_buttons);
            }

            private void handleError(Throwable error) {
                if (!Utilities.isDeviceOnline(mContext))
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
                //else
                //    Toast.makeText(mContext, mContext.getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
            }

            public void setProgress(boolean progress, boolean buttons) {
                if (progress) {
                    buttonsBox.setVisibility(View.GONE);
                    contentBox.setVisibility(View.GONE);
                    horizontalLine.setVisibility(View.GONE);
                    contentProgressBar.setVisibility(View.VISIBLE);
                } else {
                    if (!buttons) {
                        buttonsBox.setVisibility(View.VISIBLE);
                        horizontalLine.setVisibility(View.VISIBLE);
                    }
                    contentBox.setVisibility(View.VISIBLE);
                    contentProgressBar.setVisibility(View.GONE);
                }
            }

            private void createDialogRemove(boolean repeat, int type, Dialog dialog) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.dialog_message, null);


                TextView text1 = (TextView) customView.findViewById(R.id.text1);
                TextView text2 = (TextView) customView.findViewById(R.id.text2);
                TextView button1 = (TextView) customView.findViewById(R.id.buttonText1);
                TextView button2 = (TextView) customView.findViewById(R.id.buttonText2);
                EditText editText = (EditText) customView.findViewById(R.id.editText);

                editText.setVisibility(View.GONE);

                button1.setText(mContext.getResources().getString(R.string.no));
                button2.setText(mContext.getResources().getString(R.string.yes));
                text2.setVisibility(View.GONE);
                text1.setVisibility(View.VISIBLE);
                text1.setText(mContext.getResources().getString(R.string.delete_plans_question_text_3));

                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                boolean isCreator = type == Constants.FLAG ? flagServer.getCreator().equals(email) : activityServer.getCreator().equals(email);

                if (isCreator && invitedList > 1) {
                    text2.setVisibility(View.VISIBLE);
                    text2.setText(mContext.getResources().getString(R.string.delete_plans_question_text_4));
                }

                Dialog dg = new Dialog(mContext, R.style.NewDialog);

                dg.setContentView(customView);
                dg.setCanceledOnTouchOutside(true);

                button1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            button1.setBackground(null);
                        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                        }

                        return false;
                    }
                });

                button2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            button2.setBackground(null);
                        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            button2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                        }

                        return false;
                    }
                });

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dg.dismiss();
                    }
                });

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityServer activity = new ActivityServer();

                        if (type == Constants.FLAG) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagRemove" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.remove))) {
                                activity.setVisibility(Constants.FLAG);
                                deleteFlagActReminder(flagServer.getId(), activity);
                            } else if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.undo_unfit))) {
                                InviteRequest inviteRequest = new InviteRequest();
                                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                inviteRequest.setEmail(email);
                                inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                                inviteRequest.setIdAct(flagServer.getId());
                                inviteRequest.setType(Constants.FLAG);
                                inviteRequest.setStatus(Constants.NO);
                                updateInviteRequest(inviteRequest);
                            }
                        } else if (type == Constants.ACT) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actRemove" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.remove))) {
                                activity.setVisibility(Constants.ACT);
                                if (activityServer.getIdFacebook() > 0 || activityServer.getIdGoogle() != null)
                                    activity.setInvitationType(1);

                                deleteFlagActReminder(activityServer.getId(), activity);
                            } else if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.undo_unfit))) {
                                InviteRequest inviteRequest = new InviteRequest();
                                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                inviteRequest.setEmail(email);
                                inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                                inviteRequest.setIdAct(activityServer.getId());
                                inviteRequest.setType(Constants.ACT);
                                inviteRequest.setStatus(Constants.NO);

                                updateInviteRequest(inviteRequest);
                            }
                        } else if (type == Constants.REMINDER) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderRemove" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            activity.setVisibility(Constants.REMINDER);
                            deleteFlagActReminder(reminderServer.getId(), activity);
                        }

                        dialog.dismiss();
                        dg.dismiss();
                    }
                });

                dg.show();
            }

            @Override
            public
            @NonNull
            Dialog create(Context context, CreatePopUpDialogFragment fragment) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customView = null;
                ImageView icon, close;

                mContext = context;

                dateFormat = new DateFormat(context);

                mSubscriptions = new CompositeDisposable();

                Dialog dialog = new Dialog(context, R.style.NewDialog);

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

                if (!(obj instanceof DateTymo)) {
                    if (obj instanceof Flag) {
                        flagServer = ((Flag) obj).getFlagServer();

                        customView = inflater.inflate(R.layout.dialog_card_flag, null);

                        icon = (ImageView) customView.findViewById(R.id.icon);

                        TextView title = (TextView) customView.findViewById(R.id.title);
                        TextView dateText = (TextView) customView.findViewById(R.id.dateMonthYear);
                        TextView repeatText = (TextView) customView.findViewById(R.id.repeatText);
                        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
                        recyclerView = (RecyclerView) customView.findViewById(R.id.guestRow);
                        button2 = (LinearLayout) customView.findViewById(R.id.button2);
                        buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
                        buttonIcon2 = (ImageView) customView.findViewById(R.id.buttonIcon2);
                        buttonsBox = (LinearLayout) customView.findViewById(R.id.buttonsBox);
                        contentBox = (LinearLayout) customView.findViewById(R.id.contentBox);
                        contentProgressBar = (ProgressBar) customView.findViewById(R.id.contentProgressBar);
                        cardTitleBox = (RelativeLayout) customView.findViewById(R.id.cardTitleBox);
                        horizontalLine = customView.findViewById(R.id.horizontalLine);

                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                        recyclerView.setItemAnimator(new LandingAnimator());
                        recyclerView.setNestedScrollingEnabled(false);

                        customView.findViewById(R.id.addGuestButton).setVisibility(View.GONE);
                        customView.findViewById(R.id.addGuestButtonDivider).setVisibility(View.GONE);

                        buttonText1.setText(context.getResources().getString(R.string.open));

                        title.setText(flagServer.getTitle());

                        String email = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString(Constants.EMAIL, "");
                        boolean isMe = flagServer.getEmailInvited() != null && flagServer.getEmailInvited().equals(email);

                        if (!flagServer.getType()) {
                            setColorCard(R.color.flag_unavailable);
                            icon.setImageResource(R.drawable.ic_flag_unavailable);
                            customView.findViewById(R.id.profilesPhotos).setVisibility(View.GONE);

                            if (screen == Utilities.TYPE_FRIEND || (screen == Utilities.TYPE_COMPARE && !isMe)) {
                                buttonsBox.setVisibility(View.GONE);
                                horizontalLine.setVisibility(View.GONE);
                            }
                        } else {
                            customView.findViewById(R.id.profilesPhotos).setVisibility(View.VISIBLE);
                            customView.findViewById(R.id.spaceBottom).setVisibility(View.GONE);
                            customView.findViewById(R.id.spaceTop).setVisibility(View.GONE);
                            setColorCard(R.color.flag_available);
                            icon.setImageResource(R.drawable.ic_flag_available);
                        }

                        Calendar calendar = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        Calendar calendar3 = Calendar.getInstance();
                        calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                        calendar2.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());
                        calendar3.setTimeInMillis(flagServer.getLastDateTime());

                        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                        String dayOfWeekStart2 = dateFormat.formatDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
                        String dayStart = String.format("%02d", flagServer.getDayStart());
                        String monthStart = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                        int yearStart = flagServer.getYearStart();
                        String hourStart = String.format("%02d", flagServer.getHourStart());
                        String minuteStart = String.format("%02d", flagServer.getMinuteStart());

                        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
                        String dayOfWeekEnd2 = dateFormat.formatDayOfWeek(calendar2.get(Calendar.DAY_OF_WEEK));
                        String dayEnd = String.format("%02d", flagServer.getDayEnd());
                        String monthEnd = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
                        int yearEnd = flagServer.getYearEnd();
                        String hourEnd = String.format("%02d", flagServer.getHourEnd());
                        String minuteEnd = String.format("%02d", flagServer.getMinuteEnd());

                        String dayLast = String.format("%02d", calendar3.get(Calendar.DAY_OF_MONTH));
                        String monthLast = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar3.getTime().getTime());
                        int yearLast = calendar3.get(Calendar.YEAR);

                        if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_03, dayOfWeekStart, dayStart, monthStart, yearStart));
                        } else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_14, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
                        } else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                        } else if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
                        } else if (!flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty()) {
                            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                                dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_16, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
                            }
                        } else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty()) {
                            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                                dateText.setText(context.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_15, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                            }
                        } else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty()) {
                            if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                                dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                            }
                        } else {
                            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                                if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                                    dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                                } else {
                                    dateText.setText(context.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                                }
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_06, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                            }
                        }

                        if (flagServer.getRepeatType() == 0) {
                            repeatText.setVisibility(View.GONE);
                        } else {
                            repeatText.setVisibility(View.VISIBLE);
                            String date = "";

                            switch (flagServer.getRepeatType()) {
                                case Constants.DAILY:
                                    if (flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_01);
                                    else if (!flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_02, hourStart, minuteStart);
                                    else if (flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_03, hourEnd, minuteEnd);
                                    else if (!flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_04, hourStart, minuteStart, hourEnd, minuteEnd);
                                    break;
                                case Constants.WEEKLY:
                                    if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_01, dayOfWeekStart2);
                                    else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_02, dayOfWeekStart2, hourStart, minuteStart);
                                    else if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_03, dayOfWeekStart2, hourEnd, minuteEnd);
                                    else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_04, dayOfWeekStart2, hourStart, minuteStart, hourEnd, minuteEnd);
                                    else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_05, dayOfWeekStart2, dayOfWeekEnd2);
                                    else if (!flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_06, dayOfWeekStart2, hourStart, minuteStart, dayOfWeekEnd2);
                                    else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_07, dayOfWeekStart2, dayOfWeekEnd2, hourEnd, minuteEnd);
                                    else if (!flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_08, dayOfWeekStart2, hourStart, minuteStart, dayOfWeekEnd2, hourEnd, minuteEnd);
                                    break;
                                case Constants.MONTHLY:
                                    if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_01, dayStart);
                                    else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_02, dayStart, hourStart, minuteStart);
                                    else if (flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_03, dayStart, hourEnd, minuteEnd);
                                    else if (flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_04, dayStart, hourStart, minuteStart, hourEnd, minuteEnd);
                                    else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_05, dayStart, dayEnd);
                                    else if (!flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_06, dayStart, hourStart, minuteStart, dayEnd);
                                    else if (!flagServer.getDateEndEmpty() && flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_07, dayStart, dayEnd, hourEnd, minuteEnd);
                                    else if (!flagServer.getDateEndEmpty() && !flagServer.getTimeStartEmpty() && !flagServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_08, dayStart, hourStart, minuteStart, dayEnd, hourEnd, minuteEnd);
                                    break;
                                default:
                                    break;
                            }

                            dateText.setText(date);
                            repeatText.setText(context.getResources().getString(R.string.date_format_repeat, dayStart, monthStart, yearStart, dayLast, monthLast, yearLast));
                        }

                        buttonText1.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                    buttonText1.setBackground(null);
                                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_bottom_left_radius));
                                }

                                return false;
                            }
                        });

                        button2.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                    button2.setBackground(null);
                                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    button2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_bottom_right_radius));
                                }

                                return false;
                            }
                        });

                        buttonText1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(context, FlagActivity.class);
                                myIntent.putExtra("type_flag", 1);
                                myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                                context.startActivity(myIntent);

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagOpen" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                dialog.dismiss();
                            }
                        });

                        button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (buttonText2.getText().toString().matches(mContext.getResources().getString(R.string.fit))) {
                                    InviteRequest inviteRequest = new InviteRequest();
                                    SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                    String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                    inviteRequest.setEmail(email);
                                    inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                                    inviteRequest.setIdAct(flagServer.getId());
                                    inviteRequest.setType(Constants.FLAG);
                                    inviteRequest.setStatus(Constants.YES);

                                    updateInviteRequest(inviteRequest);
                                    dialog.dismiss();
                                } else {
                                    createDialogRemove(flagServer.getRepeatType() > 0 && flagServer.getCreator().equals(email), Constants.FLAG, dialog);
                                }


                            }
                        });

                        if (flagServer.getType() || screen == Utilities.TYPE_PLANS || (screen == Utilities.TYPE_COMPARE && isMe))
                            setFlagInformation(flagServer.getId());

                    } else if (obj instanceof Reminder) {

                    } else if (obj instanceof ActivityCard) {
                        activityServer = ((ActivityCard) obj).getActivityServer();

                        customView = inflater.inflate(R.layout.dialog_card_act, null);

                        TextView title = (TextView) customView.findViewById(R.id.title);
                        TextView dateText = (TextView) customView.findViewById(R.id.dateMonthYear);
                        TextView repeatText = (TextView) customView.findViewById(R.id.repeatText);
                        TextView description = (TextView) customView.findViewById(R.id.description);
                        TextView locationText = (TextView) customView.findViewById(R.id.locationText);
                        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
                        ImageView cubeLowerBoxIcon = (ImageView) customView.findViewById(R.id.cubeLowerBoxIcon);
                        ImageView cubeUpperBoxIcon = (ImageView) customView.findViewById(R.id.cubeUpperBoxIcon);
                        ImageView pieceIcon = (ImageView) customView.findViewById(R.id.pieceIcon);
                        button2 = (LinearLayout) customView.findViewById(R.id.button2);
                        buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
                        buttonIcon2 = (ImageView) customView.findViewById(R.id.buttonIcon2);
                        cardTitleBox = (RelativeLayout) customView.findViewById(R.id.cardTitleBox);
                        contentBox = (LinearLayout) customView.findViewById(R.id.contentBox);
                        contentProgressBar = (ProgressBar) customView.findViewById(R.id.contentProgressBar);
                        buttonsBox = (LinearLayout) customView.findViewById(R.id.buttonsBox);
                        horizontalLine = customView.findViewById(R.id.horizontalLine);
                        recyclerView = (RecyclerView) customView.findViewById(R.id.guestRow);

                        buttonText1.setText(context.getResources().getString(R.string.open));
                        title.setText(activityServer.getTitle());

                        setColorCard(activityServer.getCubeColor());

                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                        recyclerView.setItemAnimator(new LandingAnimator());
                        recyclerView.setNestedScrollingEnabled(false);

                        customView.findViewById(R.id.addGuestButton).setVisibility(View.GONE);
                        customView.findViewById(R.id.addGuestButtonDivider).setVisibility(View.GONE);

                        Glide.clear(pieceIcon);
                        Glide.with(context)
                                .load(activityServer.getCubeIcon())
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(pieceIcon);

                        cubeUpperBoxIcon.setColorFilter(activityServer.getCubeColorUpper());
                        cubeLowerBoxIcon.setColorFilter(activityServer.getCubeColor());

                        if (activityServer.getDescription() == null || activityServer.getDescription().matches(""))
                            description.setVisibility(View.GONE);
                        else
                            description.setText(activityServer.getDescription());


                        if (activityServer.getLocation() == null || activityServer.getLocation().matches(""))
                            customView.findViewById(R.id.locationBox).setVisibility(View.GONE);
                        else
                            locationText.setText(activityServer.getLocation());

                        Calendar calendar = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        Calendar calendar3 = Calendar.getInstance();
                        calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                        calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());
                        calendar3.setTimeInMillis(activityServer.getLastDateTime());

                        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                        String dayOfWeekStart2 = dateFormat.formatDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
                        String dayStart = String.format("%02d", activityServer.getDayStart());
                        String monthStart = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                        int yearStart = activityServer.getYearStart();
                        String hourStart = String.format("%02d", activityServer.getHourStart());
                        String minuteStart = String.format("%02d", activityServer.getMinuteStart());

                        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
                        String dayOfWeekEnd2 = dateFormat.formatDayOfWeek(calendar2.get(Calendar.DAY_OF_WEEK));
                        String dayEnd = String.format("%02d", activityServer.getDayEnd());
                        String monthEnd = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
                        int yearEnd = activityServer.getYearEnd();
                        String hourEnd = String.format("%02d", activityServer.getHourEnd());
                        String minuteEnd = String.format("%02d", activityServer.getMinuteEnd());

                        String dayLast = String.format("%02d", calendar3.get(Calendar.DAY_OF_MONTH));
                        String monthLast = new SimpleDateFormat("MM", context.getResources().getConfiguration().locale).format(calendar3.getTime().getTime());
                        int yearLast = calendar3.get(Calendar.YEAR);

                        if (activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_03, dayOfWeekStart, dayStart, monthStart, yearStart));
                        } else if (!activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_14, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
                        } else if (activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                        } else if (activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty()) {
                            dateText.setText(context.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
                        } else if (!activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty()) {
                            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                                dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_16, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd));
                            }
                        } else if (!activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty()) {
                            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                                dateText.setText(context.getResources().getString(R.string.date_format_17, dayOfWeekStart, dayStart, monthStart, yearStart, hourEnd, minuteEnd));
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_15, dayOfWeekStart, dayStart, monthStart, yearStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                            }
                        } else if (activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty()) {
                            if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                                dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                            }
                        } else {
                            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                                if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                                    dateText.setText(context.getResources().getString(R.string.date_format_04, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                                } else {
                                    dateText.setText(context.getResources().getString(R.string.date_format_05, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                                }
                            } else {
                                dateText.setText(context.getResources().getString(R.string.date_format_06, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
                            }
                        }

                        if (activityServer.getRepeatType() == 0) {
                            repeatText.setVisibility(View.GONE);
                        } else {
                            repeatText.setVisibility(View.VISIBLE);
                            String date = "";

                            switch (activityServer.getRepeatType()) {
                                case Constants.DAILY:
                                    if (activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_01);
                                    else if (!activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_02, hourStart, minuteStart);
                                    else if (activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_03, hourEnd, minuteEnd);
                                    else if (!activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_daily_04, hourStart, minuteStart, hourEnd, minuteEnd);
                                    break;
                                case Constants.WEEKLY:
                                    if (activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_01, dayOfWeekStart2);
                                    else if (activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_02, dayOfWeekStart2, hourStart, minuteStart);
                                    else if (activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_03, dayOfWeekStart2, hourEnd, minuteEnd);
                                    else if (activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_04, dayOfWeekStart2, hourStart, minuteStart, hourEnd, minuteEnd);
                                    else if (!activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_05, dayOfWeekStart2, dayOfWeekEnd2);
                                    else if (!activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_06, dayOfWeekStart2, hourStart, minuteStart, dayOfWeekEnd2);
                                    else if (!activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_07, dayOfWeekStart2, dayOfWeekEnd2, hourEnd, minuteEnd);
                                    else if (!activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_weekly_08, dayOfWeekStart2, hourStart, minuteStart, dayOfWeekEnd2, hourEnd, minuteEnd);
                                    break;
                                case Constants.MONTHLY:
                                    if (activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_01, dayStart);
                                    else if (activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_02, dayStart, hourStart, minuteStart);
                                    else if (activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_03, dayStart, hourEnd, minuteEnd);
                                    else if (activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_04, dayStart, hourStart, minuteStart, hourEnd, minuteEnd);
                                    else if (!activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_05, dayStart, dayEnd);
                                    else if (!activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_06, dayStart, hourStart, minuteStart, dayEnd);
                                    else if (!activityServer.getDateEndEmpty() && activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_07, dayStart, dayEnd, hourEnd, minuteEnd);
                                    else if (!activityServer.getDateEndEmpty() && !activityServer.getTimeStartEmpty() && !activityServer.getTimeEndEmpty())
                                        date = context.getResources().getString(R.string.date_format_monthly_08, dayStart, hourStart, minuteStart, dayEnd, hourEnd, minuteEnd);
                                    break;
                                default:
                                    break;
                            }

                            dateText.setText(date);
                            repeatText.setText(context.getResources().getString(R.string.date_format_repeat, dayStart, monthStart, yearStart, dayLast, monthLast, yearLast));
                        }

                        buttonText1.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                    buttonText1.setBackground(null);
                                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_bottom_left_radius));
                                }

                                return false;
                            }
                        });

                        button2.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                    button2.setBackground(null);
                                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    button2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_bottom_right_radius));
                                }

                                return false;
                            }
                        });

                        buttonText1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(context, ShowActivity.class);
                                myIntent.putExtra("act_show", new ActivityWrapper(activityServer));
                                context.startActivity(myIntent);

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actOpen" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                dialog.dismiss();
                            }
                        });

                        button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (buttonText2.getText().toString().matches(mContext.getResources().getString(R.string.fit))) {
                                    InviteRequest inviteRequest = new InviteRequest();
                                    SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                    String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                    inviteRequest.setEmail(email);
                                    inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                                    inviteRequest.setIdAct(activityServer.getId());
                                    inviteRequest.setType(Constants.ACT);
                                    inviteRequest.setStatus(Constants.YES);

                                    updateInviteRequest(inviteRequest);
                                    dialog.dismiss();
                                } else {
                                    SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                    String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                    createDialogRemove(activityServer.getRepeatType() > 0 && activityServer.getCreator().equals(email), Constants.ACT, dialog);
                                }
                            }
                        });

                        setActivityInformation(activityServer.getId());
                    } else if (obj instanceof HolidayCard) {
                        Holiday holiday = ((HolidayCard) obj).getHoliday();

                        customView = inflater.inflate(R.layout.dialog_card_holidays, null);

                        HolidayCardAdapter adapter = new HolidayCardAdapter(holiday.getHolidays());

                        recyclerView = (RecyclerView) customView.findViewById(R.id.listHoliday);

                        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(context, R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, context));
                        itemDecoration.setDrawLastItem(false);

                        recyclerView.setAdapter(adapter);

                        //recyclerView.addItemDecoration(itemDecoration);

                        recyclerView.setLayoutManager(new LinearLayoutManager(context));

                        TextView dateMonthYear = (TextView) customView.findViewById(R.id.dateMonthYear);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(holiday.getYear(), holiday.getMonth() - 1, holiday.getDay());
                        String day = String.format("%02d", holiday.getDay());
                        String month = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH) + 1);
                        String year = String.valueOf(holiday.getYear());
                        dateMonthYear.setText(context.getResources().getString(R.string.date_format_10, day, month, year));

                    } else if (obj instanceof BirthdayCard) {
                        Birthday birthday = ((BirthdayCard) obj).getBirthday();

                        customView = inflater.inflate(R.layout.dialog_card_birthdays, null);

                        recyclerView = (RecyclerView) customView.findViewById(R.id.guestRow);

                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                        recyclerView.setItemAnimator(new LandingAnimator());
                        recyclerView.setNestedScrollingEnabled(false);

                        customView.findViewById(R.id.addGuestButton).setVisibility(View.GONE);
                        customView.findViewById(R.id.addGuestButtonDivider).setVisibility(View.GONE);

                        ArrayList<User> listPerson = new ArrayList<>();

                        for (int i = 0; i < birthday.getUsersBirthday().size(); i++) {
                            User usr = birthday.getUsersBirthday().get(i);
                            usr.setDelete(false);
                            listPerson.add(usr);
                        }

                        listPerson = setOrderBirthdayPeople(listPerson);

                        PersonAdapter adapter = new PersonAdapter(listPerson, mContext);
                        recyclerView.setAdapter(adapter);

                        TextView birthdayTitle = (TextView) customView.findViewById(R.id.title);
                        TextView dateMonthYear = (TextView) customView.findViewById(R.id.dateMonthYear);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(birthday.getYear(), birthday.getMonth() - 1, birthday.getDay());
                        String day = String.format("%02d", birthday.getDay());
                        String month = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH) + 1);
                        String year = String.valueOf(birthday.getYear());
                        birthdayTitle.setText(context.getResources().getString(R.string.card_birthday_text_1, birthday.getUsersBirthday().size()));
                        dateMonthYear.setText(context.getResources().getString(R.string.date_format_10, day, month, year));
                    }

                    dialog.setContentView(customView);
                    dialog.setCanceledOnTouchOutside(true);

                    close = (ImageView) customView.findViewById(R.id.closeButton);

                    close.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                close.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.white));
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                close.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.grey_300));
                            }

                            return false;
                        }
                    });

                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                } else {
                    customView = inflater.inflate(R.layout.dialog_card_free_time, null);

                    LinearLayout activityBox = (LinearLayout) customView.findViewById(R.id.activityBox);
                    LinearLayout reminderBox = (LinearLayout) customView.findViewById(R.id.reminderBox);
                    LinearLayout flagBox = (LinearLayout) customView.findViewById(R.id.flagBox);
                    TextView activityText = (TextView) customView.findViewById(R.id.activityText);
                    ImageView activityIcon = (ImageView) customView.findViewById(R.id.activityIcon);
                    TextView reminderText = (TextView) customView.findViewById(R.id.reminderText);
                    ImageView reminderIcon = (ImageView) customView.findViewById(R.id.reminderIcon);
                    TextView flagText = (TextView) customView.findViewById(R.id.flagText);
                    ImageView flagIcon = (ImageView) customView.findViewById(R.id.flagIcon);
                    TextView mainText = (TextView) customView.findViewById(R.id.mainText);

                    if (alone) {
                        reminderBox.setVisibility(View.VISIBLE);
                        customView.findViewById(R.id.reminderLine).setVisibility(View.VISIBLE);
                    } else if (screen != Utilities.TYPE_PLANS) {
                        reminderBox.setVisibility(View.GONE);
                        customView.findViewById(R.id.reminderLine).setVisibility(View.GONE);
                    }

                    if (compare) {
                        mainText.setText(R.string.card_free_time_compare_text);
                    } else {
                        mainText.setText(R.string.card_free_time_text);
                    }

                    activityBox.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                activityBox.setBackground(null);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                activityBox.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_no_radius));
                            }

                            return false;
                        }
                    });

                    reminderBox.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                reminderBox.setBackground(null);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                reminderBox.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_no_radius));
                            }

                            return false;
                        }
                    });

                    flagBox.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                flagBox.setBackground(null);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                flagBox.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_card_bottom_radius));
                            }

                            return false;
                        }
                    });

                    activityBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DateTymo dateTymo = (DateTymo) obj;
                            ActivityServer activityServerFreeTime = new ActivityServer();

                            activityServerFreeTime.setDayStart(dateTymo.getDay());
                            activityServerFreeTime.setMonthStart(dateTymo.getMonth());
                            activityServerFreeTime.setYearStart(dateTymo.getYear());

                            activityServerFreeTime.setDayEnd(dateTymo.getDay());
                            activityServerFreeTime.setMonthEnd(dateTymo.getMonth());
                            activityServerFreeTime.setYearEnd(dateTymo.getYear());

                            activityServerFreeTime.setMinuteStart(dateTymo.getMinute());
                            activityServerFreeTime.setHourStart(dateTymo.getHour());

                            activityServerFreeTime.setMinuteEnd(dateTymo.getMinuteEnd());
                            activityServerFreeTime.setHourEnd(dateTymo.getHourEnd());

                            Intent intent = new Intent(context, AddPart1Activity.class);

                            //intent.putExtra("act_free", new ActivityWrapper(activityServerFreeTime));

                            /*if (screen != Utilities.TYPE_PLANS) {
                                if (friend != null)
                                    intent.putExtra("act_free_friend_usr", new UserWrapper(friend));
                                else if (listFriends.size() > 0) {
                                    intent.putExtra("ListCreateActivityCompare", new UserWrapper(listFriends));
                                }
                            }*/

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "act_free" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            context.startActivity(intent);

                            dialog.dismiss();
                        }
                    });

                    reminderBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DateTymo dateTymo = (DateTymo) obj;
                            ReminderServer reminderServerFreeTime = new ReminderServer();

                            reminderServerFreeTime.setDayStart(dateTymo.getDay());
                            reminderServerFreeTime.setMonthStart(dateTymo.getMonth());
                            reminderServerFreeTime.setYearStart(dateTymo.getYear());
                            reminderServerFreeTime.setMinuteStart(dateTymo.getMinute());
                            reminderServerFreeTime.setHourStart(dateTymo.getHour());

                            Intent intent = new Intent(context, ReminderActivity.class);

                            //intent.putExtra("reminder_free_time", new ReminderWrapper(reminderServerFreeTime));

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminder_free_time" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            context.startActivity(intent);

                            dialog.dismiss();
                        }
                    });

                    flagBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DateTymo dateTymo = (DateTymo) obj;
                            FlagServer flagServerFreeTime = new FlagServer();

                            flagServerFreeTime.setDayStart(dateTymo.getDay());
                            flagServerFreeTime.setMonthStart(dateTymo.getMonth());
                            flagServerFreeTime.setYearStart(dateTymo.getYear());

                            flagServerFreeTime.setDayEnd(dateTymo.getDay());
                            flagServerFreeTime.setMonthEnd(dateTymo.getMonth());
                            flagServerFreeTime.setYearEnd(dateTymo.getYear());

                            flagServerFreeTime.setMinuteStart(dateTymo.getMinute());
                            flagServerFreeTime.setHourStart(dateTymo.getHour());

                            flagServerFreeTime.setMinuteEnd(dateTymo.getMinuteEnd());
                            flagServerFreeTime.setHourEnd(dateTymo.getHourEnd());

                            Intent intent = new Intent(context, FlagActivity.class);

                            //intent.putExtra("flag_free", new FlagWrapper(flagServerFreeTime));

                            /*if (screen != Utilities.TYPE_PLANS) {
                                intent.putExtra("flag_free_friend", true);
                                if (friend != null)
                                    intent.putExtra("flag_free_friend_usr", new UserWrapper(friend));
                                else if (listFriends.size() > 0)
                                    intent.putExtra("ListCreateActivityCompare", new UserWrapper(listFriends));
                            }*/

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flag_free" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            context.startActivity(intent);

                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(customView);
                    dialog.setCanceledOnTouchOutside(true);
                }

                return dialog;
            }
        },
    }


    public static CreatePopUpDialogFragment newInstance(Type type, Object object, int s, User usr) {
        CreatePopUpDialogFragment f = new CreatePopUpDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        f.setArguments(args);
        f.setTiltEnabled(true);
        f.setSwipeable(false);
        obj = object;
        screen = s;
        friend = usr;
        return f;
    }

    private static ArrayList<User> setOrderBirthdayPeople(ArrayList<User> users) {

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

        return users;
    }

    private static ArrayList<User> setOrderGuests(ArrayList<User> users) {

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Type type = (Type) getArguments().getSerializable("type");
        return type.create(getActivity(), this);
    }

    public interface RefreshLayoutPlansCallback {

        void refreshLayout(boolean showRefresh);
    }

    private static void updateFeedMessageToActivity(Context context) {
        Intent intent = new Intent("feed_update");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void updateNotificationStartToday(Context context) {
        Intent intent = new Intent("notification_update");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void refreshScreen(Context context) {
        Intent intent = new Intent("refresh_screen_delete");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void setCallback(RefreshLayoutPlansCallback cb) {
        callback = cb;
    }

    public void setListFriends(ArrayList<User> list) {
        listFriends = list;
    }

    public void setAloneInCompare(boolean a) {
        alone = a;
    }

    public void setFromCompare(boolean compare) {
        this.compare = compare;
    }
}
