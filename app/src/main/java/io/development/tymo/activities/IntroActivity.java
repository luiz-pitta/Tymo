package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.animations.IViewTranslation;
import io.development.tymo.R;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.CustomSlide;
import io.development.tymo.utils.CustomSlide2;
import io.development.tymo.utils.CustomSlide3;
import io.development.tymo.utils.CustomSlide4;
import io.development.tymo.utils.CustomSlideWelcome;


public class IntroActivity extends MaterialIntroActivity {

    private boolean settings = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);

        SharedPreferences mSharedPreferences;
        mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        boolean intro = mSharedPreferences.getBoolean(Constants.INTRO, false);
        settings = getIntent().getBooleanExtra("settings", false);

        if(!settings && intro) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.INTRO,true);
        editor.apply();

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new CustomSlideWelcome());

        addSlide(new CustomSlide());

        addSlide(new CustomSlide2());

        addSlide(new CustomSlide3());

        addSlide(new CustomSlide4());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, getClass().getSimpleName(), null /* class override */);

    }

    @Override
    public void onFinish() {

        super.onFinish();
        if(settings)
            finish();
        else
            startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}