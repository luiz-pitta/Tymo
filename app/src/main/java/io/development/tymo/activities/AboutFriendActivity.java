package io.development.tymo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;

public class AboutFriendActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton, profilePhoto;
    private TextView mTitle, age;
    private TextView fullName, description, url;
    private TextView birthDate, gender, locationWhereLives, professionWhereWorks, whereStudied;
    private DateFormat dateFormat;
    private FirebaseAnalytics mFirebaseAnalytics;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserWrapper userWrapper = (UserWrapper)getIntent().getSerializableExtra("user_about_friend");
        user = userWrapper.getUser();

        setContentView(R.layout.activity_about);

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        mTitle = (TextView) findViewById(R.id.text);

        fullName = (TextView) findViewById(R.id.fullName);
        description = (TextView) findViewById(R.id.description);
        url = (TextView) findViewById(R.id.url);

        fullName.setText(user.getName());
        description.setText(user.getDescription());
        url.setText(user.getUrl());

        if(!user.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(this)
                    .load(user.getPhoto())
                    .asBitmap()
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

        mBackButton.setOnClickListener(this);

        mTitle.setText(getResources().getString(R.string.about)+" "+user.getName());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, getClass().getSimpleName(), null /* class override */);

        setLayoutNormal();
    }

    private String getGender(String gender){
        if(gender.matches("male"))
            return getResources().getString(R.string.male);
        else
            return getResources().getString(R.string.female);
    }

    public void setLayoutNormal(){
        birthDate = (TextView) findViewById(R.id.birthDate);
        gender = (TextView) findViewById(R.id.gender);
        locationWhereLives = (TextView) findViewById(R.id.locationWhereLives);
        professionWhereWorks = (TextView) findViewById(R.id.whereWorks);
        whereStudied = (TextView) findViewById(R.id.whereStudied);
        age = (TextView) findViewById(R.id.age);

        dateFormat = new DateFormat(this);

        gender.setText(getGender(user.getGender()));
        locationWhereLives.setText(user.getLivesIn());
        professionWhereWorks.setText(user.getWorksAt());
        whereStudied.setText(user.getStudiedAt());

        if(user.getYearBorn() != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(user.getYearBorn(),user.getMonthBorn()-1,user.getDayBorn());

            String d= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String m= dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH)+1);
            String date = getResources().getString(R.string.date_format_10, d, m, String.valueOf(user.getYearBorn()));

            birthDate.setText(date);

            LocalDate birthdate = new LocalDate(user.getYearBorn(), user.getMonthBorn(), user.getDayBorn());
            LocalDate now = new LocalDate();
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
            age.setText(getResources().getString(R.string.about_years, period.getYears()));
        }else
            findViewById(R.id.birthDateBox).setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }

    }

}
