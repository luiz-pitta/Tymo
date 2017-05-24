package io.development.tymo.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import io.development.tymo.R;
import io.development.tymo.activities.AddActivity;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.ReminderActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.HolidayCardAdapter;
import io.development.tymo.adapters.PersonSmallAdapter;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;

public class CreatePopUpDialogFragment extends SwipeAwayDialogFragment {

    private static int screen;
    private static User friend;
    private static Object obj;
    private static RefreshLayoutPlansCallback callback;

    private interface DialogBuilder {
        @NonNull
        Dialog create(Context context, CreatePopUpDialogFragment fragment);
    }

    public enum Type implements DialogBuilder {
        CUSTOM() {

            private Context mContext;
            private DateFormat dateFormat;
            private CompositeSubscription mSubscriptions;
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

                mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::handleDeleteIgnoreConfirm, this::handleError));
            }

            private void handleDeleteIgnoreConfirm(Response response) {
                //Toast.makeText(mContext, ServerMessage.getServerMessage(mContext, response.getMessage()), Toast.LENGTH_LONG).show();
                //ACTIVITY_DELETED_SUCCESSFULLY, RELATIONSHIP_UPDATED_SUCCESSFULLY e WITHOUT_NOTIFICATION

                if (callback != null)
                    callback.refreshLayout();
            }

            private boolean setLayout(ArrayList<User> list, String email, boolean activity) {
                boolean in = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getEmail().matches(email)) {
                        if (list.get(i).getInvitation() == 1) {
                            buttonIcon2.setImageResource(R.drawable.ic_trash);
                            buttonIcon2.setColorFilter(ContextCompat.getColor(mContext, R.color.red_600));
                            buttonText2.setText(mContext.getResources().getString(R.string.remove));
                            buttonText2.setTag(mContext.getResources().getString(R.string.unfit));
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

                if (response.getTags() != null) {
                    if (email.matches(activityServer.getCreator())) {
                        buttonIcon2.setImageResource(R.drawable.ic_trash);
                        buttonIcon2.setColorFilter(ContextCompat.getColor(mContext, R.color.red_600));
                        buttonText2.setText(mContext.getResources().getString(R.string.remove));
                        buttonText2.setTag(mContext.getResources().getString(R.string.remove));
                        buttonText2.setTextColor(ContextCompat.getColor(mContext, R.color.red_600));
                    } else
                        activity_buttons = setLayout(response.getPeople(), email, true);

                    PersonSmallAdapter adapter = new PersonSmallAdapter(response.getPeople(), mContext);
                    recyclerView.setAdapter(adapter);
                } else {
                    if (email.matches(flagServer.getCreator())) {
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

                    PersonSmallAdapter adapter = new PersonSmallAdapter(response.getPeople(), mContext);
                    recyclerView.setAdapter(adapter);
                }
                setProgress(false, activity_buttons);
            }

            private void handleError(Throwable error) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
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
                RadioGroup radioGroup = (RadioGroup) customView.findViewById(R.id.radioGroup);
                AppCompatRadioButton allRadioButton = (AppCompatRadioButton) customView.findViewById(R.id.allRadioButton);

                editText.setVisibility(View.GONE);

                allRadioButton.setText(mContext.getResources().getString(R.string.dialog_delete_plans_all));

                if (repeat) {
                    radioGroup.setVisibility(View.VISIBLE);
                    radioGroup.setOrientation(LinearLayout.VERTICAL);
                    button1.setText(mContext.getResources().getString(R.string.cancel));
                    button2.setText(mContext.getResources().getString(R.string.confirm));
                    text2.setVisibility(View.VISIBLE);
                    text1.setText(mContext.getResources().getString(R.string.dialog_delete_plans_text));
                    text2.setText(mContext.getResources().getString(R.string.dialog_delete_plans_text2));
                } else {
                    button1.setText(mContext.getResources().getString(R.string.no));
                    button2.setText(mContext.getResources().getString(R.string.yes));
                    text2.setVisibility(View.GONE);
                    text1.setVisibility(View.VISIBLE);
                    text1.setText(mContext.getResources().getString(R.string.dialog_delete_plans_title));
                }

                Dialog dg = new Dialog(mContext, R.style.NewDialog);

                dg.setContentView(customView);
                dg.setCanceledOnTouchOutside(true);

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dg.dismiss();
                    }
                });

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int radioButtonID;
                        View radioButton;
                        int idx = -1;

                        if (repeat) {
                            radioButtonID = radioGroup.getCheckedRadioButtonId();
                            radioButton = radioGroup.findViewById(radioButtonID);
                            idx = radioGroup.indexOfChild(radioButton);
                        }

                        ActivityServer activity = new ActivityServer();
                        activity.setId(idx);

                        if (type == Constants.FLAG) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagRemove" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.remove))) {
                                activity.setVisibility(Constants.FLAG);
                                deleteFlagActReminder(flagServer.getId(), activity);
                            } else if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.unfit))) {
                                InviteRequest inviteRequest = new InviteRequest();
                                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                inviteRequest.setEmail(email);
                                inviteRequest.setIdAct(flagServer.getId());
                                inviteRequest.setType(Constants.FLAG);
                                inviteRequest.setStatus(Constants.NO);
                                updateInviteRequest(inviteRequest);
                            }
                        } else if (type == Constants.ACT) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actRemove" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.remove))) {
                                activity.setVisibility(Constants.ACT);
                                deleteFlagActReminder(activityServer.getId(), activity);
                            } else if (buttonText2.getTag().toString().matches(mContext.getResources().getString(R.string.unfit))) {
                                InviteRequest inviteRequest = new InviteRequest();
                                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                                inviteRequest.setEmail(email);
                                inviteRequest.setIdAct(activityServer.getId());
                                inviteRequest.setType(Constants.ACT);
                                inviteRequest.setStatus(Constants.NO);

                                updateInviteRequest(inviteRequest);
                            }
                        } else if (type == Constants.REMINDER) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderRemove" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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

                mSubscriptions = new CompositeSubscription();

                Dialog dialog = new Dialog(context, R.style.NewDialog);

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

                if (!(obj instanceof DateTymo)) {
                    if (obj instanceof Flag) {
                        flagServer = ((Flag) obj).getFlagServer();

                        customView = inflater.inflate(R.layout.dialog_card_flag, null);

                        icon = (ImageView) customView.findViewById(R.id.icon);

                        TextView title = (TextView) customView.findViewById(R.id.title);
                        TextView dateText = (TextView) customView.findViewById(R.id.dateMonthYear);
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

                        buttonText1.setText(context.getResources().getString(R.string.open));

                        title.setText(flagServer.getTitle());

                        String email = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString(Constants.EMAIL, "");
                        boolean isMe = flagServer.getEmailInvited() != null && flagServer.getEmailInvited().matches(email);

                        if (!flagServer.getType()) {
                            setColorCard(R.color.flag_unavailable);
                            icon.setImageResource(R.drawable.ic_flag_unavailable);
                            customView.findViewById(R.id.profilesPhotos).setVisibility(View.GONE);

                            if(screen == Utilities.TYPE_FRIEND || (screen == Utilities.TYPE_COMPARE && !isMe)) {
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
                        calendar.set(flagServer.getYearStart(), flagServer.getMonthStart() - 1, flagServer.getDayStart());
                        calendar2.set(flagServer.getYearEnd(), flagServer.getMonthEnd() - 1, flagServer.getDayEnd());

                        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                        String dayStart = String.format("%02d", flagServer.getDayStart());
                        String monthStart = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                        String hourStart = String.format("%02d", flagServer.getHourStart());
                        String minuteStart = String.format("%02d", flagServer.getMinuteStart());
                        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
                        String dayEnd = String.format("%02d", flagServer.getDayEnd());
                        String monthEnd = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
                        String hourEnd = String.format("%02d", flagServer.getHourEnd());
                        String minuteEnd = String.format("%02d", flagServer.getMinuteEnd());

                        if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                            if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                                dateText.setText(mContext.getResources().getString(R.string.date_format_7, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart));
                            } else {
                                dateText.setText(mContext.getResources().getString(R.string.date_format_8, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart, hourEnd, minuteEnd));
                            }
                        } else {
                            dateText.setText(mContext.getResources().getString(R.string.date_format_9, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, hourEnd, minuteEnd));
                        }

                        buttonText1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(context, FlagActivity.class);
                                myIntent.putExtra("type_flag", 1);
                                myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                                context.startActivity(myIntent);

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagOpen" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
                                    inviteRequest.setIdAct(flagServer.getId());
                                    inviteRequest.setType(Constants.FLAG);
                                    inviteRequest.setStatus(Constants.YES);

                                    updateInviteRequest(inviteRequest);
                                    dialog.dismiss();
                                } else {
                                    createDialogRemove(flagServer.getRepeatType() > 0, Constants.FLAG, dialog);
                                }


                            }
                        });

                        if (flagServer.getType() || screen == Utilities.TYPE_PLANS || (screen == Utilities.TYPE_COMPARE && isMe))
                            setFlagInformation(flagServer.getId());

                    } else if (obj instanceof Reminder) {
                        reminderServer = ((Reminder) obj).getReminderServer();

                        customView = inflater.inflate(R.layout.dialog_card_reminder, null);
                        TextView title = (TextView) customView.findViewById(R.id.title);
                        TextView dateText = (TextView) customView.findViewById(R.id.dateMonthYear);
                        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
                        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
                        button2 = (LinearLayout) customView.findViewById(R.id.button2);
                        buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
                        buttonIcon2 = (ImageView) customView.findViewById(R.id.buttonIcon2);

                        buttonText1.setText(context.getResources().getString(R.string.open));
                        title.setText(reminderServer.getTitle());

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(reminderServer.getYearStart(), reminderServer.getMonthStart() - 1, reminderServer.getDayStart());
                        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                        String dayStart = String.format("%02d", reminderServer.getDayStart());
                        String monthStart = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                        int yearStart = reminderServer.getYearStart();
                        String hourStart = String.format("%02d", reminderServer.getHourStart());
                        String minuteStart = String.format("%02d", reminderServer.getMinuteStart());

                        dateText.setText(mContext.getResources().getString(R.string.date_format_7, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart));

                        button1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(context, ReminderActivity.class);
                                myIntent.putExtra("type_reminder", 1);
                                myIntent.putExtra("reminder_show", new ReminderWrapper(reminderServer));
                                context.startActivity(myIntent);

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderOpen" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                dialog.dismiss();
                            }
                        });

                        button2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                createDialogRemove(reminderServer.getRepeatType() > 0, Constants.REMINDER, dialog);
                            }
                        });

                    } else if (obj instanceof ActivityCard) {
                        activityServer = ((ActivityCard) obj).getActivityServer();

                        customView = inflater.inflate(R.layout.dialog_card_act, null);

                        TextView title = (TextView) customView.findViewById(R.id.title);
                        TextView dateText = (TextView) customView.findViewById(R.id.dateMonthYear);
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
                        calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart());
                        calendar2.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd());

                        String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
                        String dayStart = String.format("%02d", activityServer.getDayStart());
                        String monthStart = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
                        String hourStart = String.format("%02d", activityServer.getHourStart());
                        String minuteStart = String.format("%02d", activityServer.getMinuteStart());
                        String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
                        String dayEnd = String.format("%02d", activityServer.getDayEnd());
                        String monthEnd = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
                        String hourEnd = String.format("%02d", activityServer.getHourEnd());
                        String minuteEnd = String.format("%02d", activityServer.getMinuteEnd());

                        if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                            if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                                dateText.setText(mContext.getResources().getString(R.string.date_format_7, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart));
                            } else {
                                dateText.setText(mContext.getResources().getString(R.string.date_format_8, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart, hourEnd, minuteEnd));
                            }
                        } else {
                            dateText.setText(mContext.getResources().getString(R.string.date_format_9, dayOfWeekStart, dayStart, monthStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, hourEnd, minuteEnd));
                        }

                        buttonText1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent myIntent = new Intent(context, ShowActivity.class);
                                myIntent.putExtra("act_show", new ActivityWrapper(activityServer));
                                context.startActivity(myIntent);

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actOpen" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
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
                                    inviteRequest.setIdAct(activityServer.getId());
                                    inviteRequest.setType(Constants.ACT);
                                    inviteRequest.setStatus(Constants.YES);

                                    updateInviteRequest(inviteRequest);
                                    dialog.dismiss();
                                } else {
                                    createDialogRemove(activityServer.getRepeatType() > 0, Constants.ACT, dialog);
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

                        recyclerView.addItemDecoration(itemDecoration);

                        recyclerView.setLayoutManager(new LinearLayoutManager(context));

                        TextView dateMonthYear = (TextView) customView.findViewById(R.id.dateMonthYear);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(holiday.getYear(), holiday.getMonth(), holiday.getDay());
                        String day = String.format("%02d", holiday.getDay());
                        String month = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH));
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

                        PersonSmallAdapter adapter = new PersonSmallAdapter(birthday.getUsersBirthday(), mContext);
                        recyclerView.setAdapter(adapter);

                        TextView dateMonthYear = (TextView) customView.findViewById(R.id.dateMonthYear);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(birthday.getYear(), birthday.getMonth(), birthday.getDay());
                        String day = String.format("%02d", birthday.getDay());
                        String month = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH));
                        String year = String.valueOf(birthday.getYear());
                        dateMonthYear.setText(context.getResources().getString(R.string.date_format_10, day, month, year));
                    }

                    dialog.setContentView(customView);
                    dialog.setCanceledOnTouchOutside(true);

                    close = (ImageView) customView.findViewById(R.id.closeButton);
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

                    if (screen != Utilities.TYPE_PLANS) {
                        reminderBox.setVisibility(View.GONE);
                        customView.findViewById(R.id.reminderLine).setVisibility(View.GONE);
                    }

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

                            Intent intent = new Intent(context, AddActivity.class);

                            intent.putExtra("act_free", new ActivityWrapper(activityServerFreeTime));

                            if (screen != Utilities.TYPE_PLANS)
                                intent.putExtra("act_free_friend_usr", new UserWrapper(friend));


                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "act_free" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            context.startActivity(intent);
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

                            intent.putExtra("reminder_free_time", new ReminderWrapper(reminderServerFreeTime));

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminder_free_time" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            context.startActivity(intent);
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

                            intent.putExtra("flag_free", new FlagWrapper(flagServerFreeTime));

                            if (screen != Utilities.TYPE_PLANS) {
                                intent.putExtra("flag_free_friend", true);
                                intent.putExtra("flag_free_friend_usr", new UserWrapper(friend));
                            }

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flag_free" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            context.startActivity(intent);
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
        if (object instanceof Flag || object instanceof BirthdayCard || object instanceof ActivityCard)
            f.setSwipeable(false);
        else
            f.setSwipeable(true);
        obj = object;
        screen = s;
        friend = usr;
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Type type = (Type) getArguments().getSerializable("type");
        return type.create(getActivity(), this);
    }

    @Override
    public boolean onSwipedAway(boolean toRight) {
        return false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public interface RefreshLayoutPlansCallback {

        public void refreshLayout();
    }

    public void setCallback(RefreshLayoutPlansCallback cb) {
        callback = cb;
    }
}
