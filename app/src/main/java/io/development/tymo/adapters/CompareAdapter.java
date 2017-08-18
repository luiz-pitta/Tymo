package io.development.tymo.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.activities.ReminderActivity;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.FreeTimeServer;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.ReminderWrapper;
import io.development.tymo.models.CompareModel;
import io.development.tymo.models.cards.ActivityCard;
import io.development.tymo.models.cards.Flag;
import io.development.tymo.models.cards.FreeTime;
import io.development.tymo.models.cards.Reminder;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;

import static android.content.Context.MODE_PRIVATE;

public class CompareAdapter extends RecyclerView.Adapter<CompareAdapter.CompareUserViewHolder> {

    private List<CompareModel> compareList;
    private List<Integer> dateList;
    private static Context context;
    private SharedPreferences settings;
    private boolean free;
    private CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback;

    public class CompareUserViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePhoto;
        private TextView profileName;
        private EasyRecyclerView mRecyclerView;
        private PlansCardsAdapter adapter;
        private FirebaseAnalytics mFirebaseAnalytics;

        public CompareUserViewHolder(View view) {
            super(view);
            profilePhoto = (ImageView) view.findViewById(R.id.profilePhoto);
            profileName = (TextView) view.findViewById(R.id.profileName);
            mRecyclerView = (EasyRecyclerView) view.findViewById(R.id.dayCardBox);

            mRecyclerView.setProgressView(R.layout.progress_loading_list);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

            if (free)
                mRecyclerView.setEmptyView(R.layout.empty_free_time);

            profileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openFriend(getAdapterPosition());
                }
            });

            profilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openFriend(getAdapterPosition());
                }
            });

            adapter = new PlansCardsAdapter(view.getContext());

            mRecyclerView.setAdapterWithProgress(adapter);

            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, mRecyclerView.getRecyclerView(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, MotionEvent e) {
                    Object obj = adapter.getItem(position);
                    FlagServer flagServer;
                    ReminderServer reminderServer;

                    SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                    String email = mSharedPreferences.getString(Constants.EMAIL, "");

                    Activity activity = (Activity) context;
                    CreatePopUpDialogFragment createPopUpDialogFragment = new CreatePopUpDialogFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "card" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


                    if (!free) {
                        if (obj instanceof Reminder){
                            reminderServer = ((Reminder) obj).getReminderServer();

                            Intent myIntent = new Intent(context, ReminderActivity.class);
                            myIntent.putExtra("type_reminder", 1);
                            myIntent.putExtra("reminder_show", new ReminderWrapper(reminderServer));
                            context.startActivity(myIntent);

                            bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "reminderOpen" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        }
                        else if (obj instanceof Flag && ((Flag) obj).getFlagServer().getType()){
                            flagServer = ((Flag) obj).getFlagServer();

                            Intent myIntent = new Intent(context, FlagActivity.class);
                            myIntent.putExtra("type_flag", 1);
                            myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                            context.startActivity(myIntent);

                            bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagOpen" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        }
                        else if (obj instanceof Flag && !((Flag) obj).getFlagServer().getType() && email.equals(((Flag) obj).getFlagServer().getCreator())){
                            flagServer = ((Flag) obj).getFlagServer();

                            Intent myIntent = new Intent(context, FlagActivity.class);
                            myIntent.putExtra("type_flag", 1);
                            myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                            context.startActivity(myIntent);

                            bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flagOpen" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        }
                        else {
                            createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                                    CreatePopUpDialogFragment.Type.CUSTOM, obj, Utilities.TYPE_COMPARE, null);

                            createPopUpDialogFragment.setCallback(callback);
                            createPopUpDialogFragment.show(activity.getFragmentManager(), "custom");
                        }
                    }
                    else {
                        Calendar now = Calendar.getInstance();

                        FreeTime freeTime = (FreeTime) obj;
                        DateTymo dateTymo = new DateTymo();

                        dateTymo.setDay(dateList.get(0));
                        dateTymo.setMonth(dateList.get(1));
                        dateTymo.setYear(dateList.get(2));

                        dateTymo.setHour(Integer.valueOf(freeTime.getTime().substring(0, 2)));
                        dateTymo.setMinute(Integer.valueOf(freeTime.getTime().substring(3, 5)));
                        dateTymo.setHourEnd(Integer.valueOf(freeTime.getTime().substring(6, 8)));
                        dateTymo.setMinuteEnd(Integer.valueOf(freeTime.getTime().substring(9, 11)));
                        createPopUpDialogFragment = CreatePopUpDialogFragment.newInstance(
                                CreatePopUpDialogFragment.Type.CUSTOM, dateTymo,
                                Utilities.TYPE_COMPARE, null);

                        if(!freeTime.isInPast()) {
                            createPopUpDialogFragment.setCallback(callback);
                            createPopUpDialogFragment.setAloneInCompare(getItemCount() == 1);
                            createPopUpDialogFragment.show(activity.getFragmentManager(), "custom");
                        }else {
                            createDialogMessage();
                        }
                    }


                }

                @Override
                public void onLongItemClick(View view, int position, MotionEvent e) {

                }
            }));

            view.findViewById(R.id.dayDate).setVisibility(View.GONE);
        }

        private void openFriend(int position){
            if(position > 0) {
                String name = compareList.get(position).getName();
                String email = compareList.get(position).getEmail();
                Intent myIntent = new Intent(context, FriendProfileActivity.class);
                myIntent.putExtra("name", name);
                myIntent.putExtra("friend_email", email);
                context.startActivity(myIntent);
            }
        }
    }

    public CompareAdapter(List<CompareModel> compareList, Context context, boolean free, CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback) {
        this.compareList = compareList;
        this.context = context;
        this.dateList = new ArrayList<>();
        this.free = free;
        this.callback = callback;
    }

    @Override
    public CompareUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_plans, parent, false);

        itemView.getLayoutParams().height = (int) Utilities.convertDpToPixel(100, context);
        settings = itemView.getContext().getSharedPreferences(Utilities.PREFS_NAME, MODE_PRIVATE);

        return new CompareUserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CompareUserViewHolder holder, int position) {
        CompareModel compare = compareList.get(position);

        if (!compare.getPhoto().matches("")) {
            Glide.clear(holder.profilePhoto);
            Glide.with(context)
                    .load(compare.getPhoto())
                    .asBitmap()
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(holder.profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            holder.profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else
            holder.profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

        holder.profileName.setText(fullNameToShortName(compare.getName()));

        holder.adapter.clear();
        if (!free)
            holder.adapter.addAll(getPlansItemData(compare.getActivities(), compare.isInPast()));
        else
            holder.adapter.addAll(getPlansItemData(compare.getFree(), compare.isInPast()));
    }

    @Override
    public int getItemCount() {
        return compareList.size();
    }

    private String fullNameToShortName(String fullName) {
        String[] fullNameSplited = fullName.split(" ");

        return fullNameSplited[0];
    }

    public void clear() {
        int size = compareList.size();
        compareList.clear();
        notifyItemRangeRemoved(0, size);
    }

    private void createDialogMessage() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        LinearLayout button1 = (LinearLayout) customView.findViewById(R.id.button1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        button1.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);

        Dialog dg = new Dialog(context, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        text1.setText(R.string.free_time_past_dialog_text_1);
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

    public void setDateList(List<Integer> list) {
        dateList.addAll(list);
    }

    private List<Object> getPlansItemData(List<Object> objectList, boolean inPast) {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < objectList.size(); i++) {
            Object object = objectList.get(i);
            if (object instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) object;
                String hour_start = String.format("%02d", activityServer.getHourCard());
                String minute_start = String.format("%02d", activityServer.getMinuteCard());
                String hour_end = String.format("%02d", activityServer.getHourEndCard());
                String minute_end = String.format("%02d", activityServer.getMinuteEndCard());
                String time;
                boolean act_as_red_flag = false;

                if (activityServer.getHourCard() == activityServer.getHourEndCard() && activityServer.getMinuteCard() == activityServer.getMinuteEndCard()) {
                    time = hour_start + ":" + minute_start + "\n" + "-";
                } else {
                    time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;
                }

                String email_user = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString(Constants.EMAIL, "");

                if (!activityServer.getEmailInvited().equals(email_user) && activityServer.getParticipates() == 0) {
                    if (activityServer.getKnowCreator() == 0 && activityServer.getVisibility() > 0) //não é meu contato e visibilidade é somente amigos ou privada
                        act_as_red_flag = true;
                    else if (activityServer.getVisibility() == 2 && activityServer.getKnowCreator() > 0) // é meu contato e visibilidade é privada
                        act_as_red_flag = true;
                }
                if (!act_as_red_flag)
                    list.add(new ActivityCard(time, activityServer.getCubeIcon(), activityServer.getCubeColor(), activityServer.getCubeColorUpper(), activityServer, false));
                else {
                    FlagServer flagServer = new FlagServer();
                    flagServer.setType(false);
                    flagServer.setTitle(context.getResources().getString(R.string.flag_unavailable));
                    flagServer.setDayStart(activityServer.getDayStart());
                    flagServer.setMonthStart(activityServer.getMonthStart());
                    flagServer.setYearStart(activityServer.getYearStart());
                    flagServer.setDayEnd(activityServer.getDayEnd());
                    flagServer.setMonthEnd(activityServer.getMonthEnd());
                    flagServer.setYearEnd(activityServer.getYearEnd());
                    flagServer.setMinuteStart(activityServer.getMinuteStart());
                    flagServer.setHourStart(activityServer.getHourStart());
                    flagServer.setMinuteEnd(activityServer.getMinuteEnd());
                    flagServer.setHourEnd(activityServer.getHourEnd());
                    list.add(new Flag(time, R.drawable.ic_flag, false, flagServer, false, act_as_red_flag));
                }
            } else if (object instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) object;
                String hour_start = String.format("%02d", flagServer.getHourCard());
                String minute_start = String.format("%02d", flagServer.getMinuteCard());
                String hour_end = String.format("%02d", flagServer.getHourEndCard());
                String minute_end = String.format("%02d", flagServer.getMinuteEndCard());
                String time;
                boolean act_as_red_flag = false;

                if (flagServer.getHourCard() == flagServer.getHourEndCard() && flagServer.getMinuteCard() == flagServer.getMinuteEndCard()) {
                    time = hour_start + ":" + minute_start + "\n" + "-";
                } else {
                    time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;
                }

                if (flagServer.getType() && flagServer.getParticipates() == 0)
                    act_as_red_flag = true;

                if (!act_as_red_flag)
                    list.add(new Flag(time, R.drawable.ic_flag, flagServer.getType(), flagServer, false, false));
                else {
                    FlagServer flag = new FlagServer();
                    flag.setType(false);
                    flag.setTitle(context.getResources().getString(R.string.flag_menu_unavailable));
                    flag.setDayStart(flagServer.getDayStart());
                    flag.setMonthStart(flagServer.getMonthStart());
                    flag.setYearStart(flagServer.getYearStart());
                    flag.setDayEnd(flagServer.getDayEnd());
                    flag.setMonthEnd(flagServer.getMonthEnd());
                    flag.setYearEnd(flagServer.getYearEnd());
                    flag.setMinuteStart(flagServer.getMinuteStart());
                    flag.setHourStart(flagServer.getHourStart());
                    flag.setMinuteEnd(flagServer.getMinuteEnd());
                    flag.setHourEnd(flagServer.getHourEnd());
                    list.add(new Flag(time, R.drawable.ic_flag, false, flag, false, act_as_red_flag));
                }
            } else if (object instanceof ReminderServer) {
                ReminderServer reminderServer = (ReminderServer) object;
                String hour_start = String.format("%02d", reminderServer.getHourStart());
                String minute_start = String.format("%02d", reminderServer.getMinuteStart());
                String time;

                time = hour_start + ":" + minute_start;

                list.add(new Reminder(reminderServer.getTitle(), time, reminderServer));
            } else if (object instanceof FreeTimeServer) {
                FreeTimeServer freeTimeServer = (FreeTimeServer) object;
                String hour_start = String.format("%02d", freeTimeServer.getHourStart());
                String minute_start = String.format("%02d", freeTimeServer.getMinuteStart());
                String hour_end = String.format("%02d", freeTimeServer.getHourEnd());
                String minute_end = String.format("%02d", freeTimeServer.getMinuteEnd());
                String time = hour_start + ":" + minute_start + "\n" + hour_end + ":" + minute_end;

                list.add(new FreeTime(time, inPast));
            }
        }
        return list;
    }
}