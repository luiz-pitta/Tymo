/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.development.tymo.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.User;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static android.content.Context.MODE_PRIVATE;

public class FeedZoomMoreAdapter extends RecyclerView.Adapter<FeedZoomMoreAdapter.SimpleViewHolder> {


    private static final int DEFAULT_ITEM_COUNT = 0;

    private DateFormat dateFormat;

    private Context mContext;
    private static List<Object> mItems;
    private List<List<User>> listPeople = new ArrayList<>();
    private int currentItemId = 0;

    private double lat = -500, lng = -500;
    private String distanceText = "";

    private RotateAnimation rotation;
    private TranslateAnimation animation, animation2;

    private String email;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        private final Context mContext;

        private ImageView triangle, flagButton, cubeUpperBoxIcon, photoCreator, pieceIcon, cubeLowerBoxIcon;
        private RelativeLayout pieceBox, cubeLowerBox;
        private LinearLayout textBox, locationBox;
        private TextView textTitle, textDescription, location, date;
        private RecyclerView recyclerView;
        private PersonSmallAdapter adapter;
        private View photoCreatorRing;
        private RelativeLayout photoCreatorRingBox;
        private RelativeLayout addGuestButton;

        public SimpleViewHolder(View view, Context context) {
            super(view);

            mContext = context;

            textBox = (LinearLayout) view.findViewById(R.id.infoBox);
            pieceBox = (RelativeLayout) view.findViewById(R.id.pieceBox);
            triangle = (ImageView) view.findViewById(R.id.triangle);

            location = (TextView) view.findViewById(R.id.location);
            date = (TextView) view.findViewById(R.id.date);
            textTitle = (TextView) view.findViewById(R.id.title);
            textDescription = (TextView) view.findViewById(R.id.description);

            cubeUpperBoxIcon = (ImageView) view.findViewById(R.id.cubeUpperBoxIcon);
            cubeLowerBoxIcon = (ImageView) view.findViewById(R.id.cubeLowerBoxIcon);
            pieceIcon = (ImageView) view.findViewById(R.id.pieceIcon);
            photoCreator = (ImageView) view.findViewById(R.id.photoCreator);
            addGuestButton = (RelativeLayout) view.findViewById(R.id.addGuestButton);
            flagButton = (ImageView) view.findViewById(R.id.flagIcon);
            cubeLowerBox = (RelativeLayout) view.findViewById(R.id.cubeLowerBox);
            locationBox = (LinearLayout) view.findViewById(R.id.locationBox);
            photoCreatorRing = view.findViewById(R.id.photoCreatorRing);
            photoCreatorRingBox = (RelativeLayout) view.findViewById(R.id.photoCreatorRingBox);

            recyclerView = (RecyclerView) view.findViewById(R.id.guestRow);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setItemAnimator(new LandingAnimator());
            recyclerView.setNestedScrollingEnabled(false);

            addGuestButton.setVisibility(View.GONE);
            view.findViewById(R.id.addGuestButtonDivider).setVisibility(View.GONE);

            new Actor.Builder(SpringSystem.create(), pieceBox)
                    .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                    .onTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    })
                    .build();

            pieceBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Object object = mItems.get(getAdapterPosition());
                    FlagServer flagServer;
                    ActivityServer activityServer;
                    Intent myIntent;

                    if(object instanceof FlagServer){
                        flagServer = (FlagServer) object;
                        myIntent = new Intent(context, FlagActivity.class);
                        myIntent.putExtra("type_flag", 1);
                        myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                    }else{
                        activityServer = (ActivityServer) object;
                        myIntent = new Intent(context, ShowActivity.class);
                        myIntent.putExtra("act_show", new ActivityWrapper(activityServer));
                    }

                    context.startActivity(myIntent);
                }
            });
        }
    }

    public FeedZoomMoreAdapter(Context context) {
        this(context, DEFAULT_ITEM_COUNT);
    }

    public FeedZoomMoreAdapter(Context context, int itemCount) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        dateFormat = new DateFormat(context);

        mContext = context;
        mItems = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            addItem(i);
        }

        animation = new TranslateAnimation(0.0f, 0.0f, -Utilities.convertDpToPixel(3, context), Utilities.convertDpToPixel(4, context));
        animation.setDuration(1400);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

        animation2 = new TranslateAnimation(0.0f, 0.0f, Utilities.convertDpToPixel(3, context), -Utilities.convertDpToPixel(4, context));
        animation2.setDuration(1000);
        animation2.setRepeatCount(Animation.INFINITE);
        animation2.setRepeatMode(Animation.REVERSE);

        rotation = new RotateAnimation(-3, 3,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotation.setDuration(1200);
        rotation.setRepeatCount(Animation.INFINITE);
        rotation.setRepeatMode(Animation.REVERSE);
    }

    public void addPeople(List<User> users, int position) {
        listPeople.set(position, users);
        notifyItemChanged(position);
    }

    public int getPeoplePositionSize(int position) {
        if (position < listPeople.size())
            return listPeople.get(position).size();
        else
            return -1;
    }

    public void addItem(Object object) {
        mItems.add(object);
        listPeople.add(new ArrayList<>());
        notifyItemInserted(mItems.size());
    }

    public void addItem(Object object, int position) {
        mItems.add(position, object);
        listPeople.add(position, new ArrayList<>());
        notifyItemInserted(position);
    }

    public void clear() {
        int size = mItems.size();
        for (int i = 0; i < size; i++)
            removeItem(0);
    }

    public void setCurrentItemId(int position) {
        currentItemId = position;
    }

    public int getCurrentItemId() {
        return currentItemId;
    }

    public List<Object> getAllData() {
        return new ArrayList<>(mItems);
    }

    public void removeCurrentItem() {
        removeItem(currentItemId);
    }

    public Object getCurrentItem() {
        if (mItems.size() > 0)
            return mItems.get(currentItemId);
        else
            return null;
    }

    public void removeItem(int position) {
        if (position <= mItems.size()) {
            mItems.remove(position);
            listPeople.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_feed_zoom_more, parent, false), mContext);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public void onViewAttachedToWindow(SimpleViewHolder holder) {
        if(holder.flagButton.getVisibility() == View.VISIBLE){
            holder.pieceBox.startAnimation(animation2);
        }
        else{
            holder.pieceBox.startAnimation(animation2);
        }
        holder.textBox.startAnimation(animation);
        holder.triangle.startAnimation(animation);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        ActivityServer feedCubeModel;
        FlagServer feedFlagModel;
        holder.adapter = new PersonSmallAdapter(listPeople.get(position), mContext);
        holder.recyclerView.setAdapter(holder.adapter);
        if (mItems.get(position) instanceof ActivityServer) {
            holder.textDescription.setVisibility(View.VISIBLE);
            holder.cubeLowerBox.setVisibility(View.VISIBLE);
            holder.locationBox.setVisibility(View.VISIBLE);
            holder.pieceIcon.setVisibility(View.VISIBLE);
            holder.cubeUpperBoxIcon.setVisibility(View.VISIBLE);
            holder.cubeLowerBoxIcon.setVisibility(View.VISIBLE);

            feedCubeModel = (ActivityServer) mItems.get(position);
            holder.textTitle.setText(feedCubeModel.getTitle());
            holder.textTitle.setTextColor(ContextCompat.getColor(mContext, R.color.grey_900));
            holder.textDescription.setText(feedCubeModel.getTitle());
            if (feedCubeModel.getDescription() != null && !feedCubeModel.getDescription().matches(""))
                holder.textDescription.setText(feedCubeModel.getDescription());
            else
                holder.textDescription.setVisibility(View.GONE);

            holder.cubeLowerBox.setVisibility(View.VISIBLE);
            holder.cubeUpperBoxIcon.setVisibility(View.VISIBLE);
            holder.flagButton.setVisibility(View.GONE);

            holder.cubeUpperBoxIcon.setColorFilter(feedCubeModel.getCubeColorUpper());
            holder.cubeLowerBoxIcon.setColorFilter(feedCubeModel.getCubeColor());

            Glide.clear(holder.pieceIcon);
            Glide.with(mContext)
                    .load(feedCubeModel.getCubeIcon())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.pieceIcon);

            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            calendar.set(feedCubeModel.getYearStart(), feedCubeModel.getMonthStart() - 1, feedCubeModel.getDayStart());
            calendar2.set(feedCubeModel.getYearEnd(), feedCubeModel.getMonthEnd() - 1, feedCubeModel.getDayEnd());
            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", feedCubeModel.getDayStart());
            String monthStart = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = feedCubeModel.getYearStart();
            String hourStart = String.format("%02d", feedCubeModel.getHourStart());
            String minuteStart = String.format("%02d", feedCubeModel.getMinuteStart());
            String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
            String dayEnd = String.format("%02d", feedCubeModel.getDayEnd());
            String monthEnd = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
            int yearEnd = feedCubeModel.getYearEnd();
            String hourEnd = String.format("%02d", feedCubeModel.getHourEnd());
            String minuteEnd = String.format("%02d", feedCubeModel.getMinuteEnd());

            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                    holder.date.setText(mContext.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                } else {
                    holder.date.setText(mContext.getResources().getString(R.string.date_format_5, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                }
            } else {
                holder.date.setText(mContext.getResources().getString(R.string.date_format_6, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
            }

            if (!feedCubeModel.getLocation().matches("")) {
                SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                boolean location = mSharedPreferences.getBoolean(Constants.LOCATION, true);

                if (location) {
                    LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && lat != -500 && (feedCubeModel.getLat() != 0 && feedCubeModel.getLng() != 0)) {
                        double distance = Utilities.distance(lat, lng, feedCubeModel.getLat(), feedCubeModel.getLng());
                        if (distance < 1) {
                            distanceText = mContext.getResources().getString(R.string.distance_meters, (int) (distance * 1000)) + " ";
                        } else {
                            distanceText = mContext.getResources().getString(R.string.distance_km, (int) distance) + " ";
                        }
                    } else
                        distanceText = "";

                } else
                    distanceText = "";

                if (!distanceText.matches("")){
                    final SpannableStringBuilder sb = new SpannableStringBuilder(distanceText + feedCubeModel.getLocation());
                    final StyleSpan styleBold = new StyleSpan(Typeface.BOLD);
                    sb.setSpan(styleBold, 0, distanceText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.location.setText(sb);
                }
                else{
                    holder.location.setText(feedCubeModel.getLocation());
                }

            } else {
                holder.locationBox.setVisibility(View.GONE);
                distanceText = "";
            }

            if (!feedCubeModel.getUser().getPhoto().matches("")) {
                Glide.clear(holder.photoCreator);
                Glide.with(mContext)
                        .load(feedCubeModel.getUser().getPhoto())
                        .asBitmap()
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new BitmapImageViewTarget(holder.photoCreator) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                holder.photoCreator.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else
                holder.photoCreator.setImageResource(R.drawable.ic_profile_photo_empty);

            if (feedCubeModel.getFavoriteCreator() > 0) {
                holder.photoCreatorRingBox.setVisibility(View.VISIBLE);
                holder.photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_favorite_zoom_more);
            } else if (feedCubeModel.getKnowCreator() > 0) {
                holder.photoCreatorRingBox.setVisibility(View.VISIBLE);
                holder.photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_my_contact_zoom_more);
            } else if (feedCubeModel.getUser().getEmail().equals(email)) {
                holder.photoCreatorRingBox.setVisibility(View.VISIBLE);
                holder.photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_you_zoom_more);
            } else {
                holder.photoCreatorRingBox.setVisibility(View.INVISIBLE);
            }
        } else {
            feedFlagModel = (FlagServer) mItems.get(position);
            holder.textTitle.setText(mContext.getResources().getString(R.string.flag_available));
            holder.textTitle.setTextColor(ContextCompat.getColor(mContext, R.color.flag_available));
            if (feedFlagModel.getTitle().matches("")) {
                holder.textDescription.setVisibility(View.GONE);
            } else {
                holder.textDescription.setText(feedFlagModel.getTitle());
            }
            holder.cubeLowerBox.setVisibility(View.GONE);
            holder.cubeUpperBoxIcon.setVisibility(View.GONE);
            holder.flagButton.setVisibility(View.VISIBLE);
            holder.locationBox.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            calendar.set(feedFlagModel.getYearStart(), feedFlagModel.getMonthStart() - 1, feedFlagModel.getDayStart());
            calendar2.set(feedFlagModel.getYearEnd(), feedFlagModel.getMonthEnd() - 1, feedFlagModel.getDayEnd());

            String dayOfWeekStart = dateFormat.todayTomorrowYesterdayCheck(calendar.get(Calendar.DAY_OF_WEEK), calendar);
            String dayStart = String.format("%02d", feedFlagModel.getDayStart());
            String monthStart = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            int yearStart = feedFlagModel.getYearStart();
            String hourStart = String.format("%02d", feedFlagModel.getHourStart());
            String minuteStart = String.format("%02d", feedFlagModel.getMinuteStart());
            String dayOfWeekEnd = dateFormat.todayTomorrowYesterdayCheck(calendar2.get(Calendar.DAY_OF_WEEK), calendar2);
            String dayEnd = String.format("%02d", feedFlagModel.getDayEnd());
            String monthEnd = new SimpleDateFormat("MM", mContext.getResources().getConfiguration().locale).format(calendar2.getTime().getTime());
            int yearEnd = feedFlagModel.getYearEnd();
            String hourEnd = String.format("%02d", feedFlagModel.getHourEnd());
            String minuteEnd = String.format("%02d", feedFlagModel.getMinuteEnd());

            if (calendar.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
                if (hourStart.matches(hourEnd) && minuteStart.matches(minuteEnd)) {
                    holder.date.setText(mContext.getResources().getString(R.string.date_format_4, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart));
                } else {
                    holder.date.setText(mContext.getResources().getString(R.string.date_format_5, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, hourEnd, minuteEnd));
                }
            } else {
                holder.date.setText(mContext.getResources().getString(R.string.date_format_6, dayOfWeekStart, dayStart, monthStart, yearStart, hourStart, minuteStart, dayOfWeekEnd, dayEnd, monthEnd, yearEnd, hourEnd, minuteEnd));
            }

            if (!feedFlagModel.getUser().getPhoto().matches("")) {
                Glide.clear(holder.photoCreator);
                Glide.with(mContext)
                        .load(feedFlagModel.getUser().getPhoto())
                        .asBitmap()
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new BitmapImageViewTarget(holder.photoCreator) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                holder.photoCreator.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else
                holder.photoCreator.setImageResource(R.drawable.ic_profile_photo_empty);

            if (feedFlagModel.getFavoriteCreator() > 0) {
                holder.photoCreatorRingBox.setVisibility(View.VISIBLE);
                holder.photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_favorite_zoom_more);
            } else if (feedFlagModel.getKnowCreator() > 0) {
                holder.photoCreatorRingBox.setVisibility(View.VISIBLE);
                holder.photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_my_contact_zoom_more);
            } else if (feedFlagModel.getUser().getEmail().equals(email)) {
                holder.photoCreatorRingBox.setVisibility(View.VISIBLE);
                holder.photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_you_zoom_more);
            } else {
                holder.photoCreatorRingBox.setVisibility(View.INVISIBLE);
            }

        }

        if(holder.flagButton.getVisibility() == View.VISIBLE){
            holder.pieceBox.startAnimation(animation2);
        }
        else{
            holder.pieceBox.startAnimation(animation2);
        }
        holder.textBox.startAnimation(animation);
        holder.triangle.startAnimation(animation);
    }

}
