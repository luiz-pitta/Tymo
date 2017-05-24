package io.development.tymo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;

import io.development.tymo.R;
import io.development.tymo.model_server.UserWrapper;

public class RegisterPart2Activity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton;
    private TextView m_title, m_title2, advanceButton;

    private int visibilityCalendar = 0;
    private UserWrapper wrap;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_part_2);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        m_title2 = (TextView) findViewById(R.id.text2);
        advanceButton = (TextView) findViewById(R.id.advanceButton);

        mBackButton.setOnClickListener(this);
        advanceButton.setOnClickListener(this);

        m_title.setText(getResources().getString(R.string.register));
        m_title2.setText(getResources().getString(R.string.register_steps, 2, 3));

        wrap = (UserWrapper) getIntent().getSerializableExtra("user_wrapper");


        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.visibilityCalendarPicker);
        spinner.setItems(getResources().getStringArray(R.array.array_privacy));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                visibilityCalendar = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "visibilityCalendarPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if (view == advanceButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "advanceButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent register = new Intent(RegisterPart2Activity.this, RegisterPart3Activity.class);
            register.putExtra("user_wrapper", wrap);
            register.putExtra("user_privacy", visibilityCalendar);
            register.putExtra("register_with_facebook", false);
            startActivity(register);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_left_exit_back, R.anim.push_left_enter_back);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
