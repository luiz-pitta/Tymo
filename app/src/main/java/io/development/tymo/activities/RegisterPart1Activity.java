package io.development.tymo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;

import static io.development.tymo.utils.Validation.validateEmail;
import static io.development.tymo.utils.Validation.validateFields;
import static io.development.tymo.utils.Validation.validatePasswordSize;

public class RegisterPart1Activity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton, photo;
    private TextView m_title, m_title2;

    private int day_start, month_start, year_start, age;

    private DateFormat dateFormat;
    private UserWrapper wrap;

    private EditText name;
    private EditText email;
    private EditText password;
    private TextView birthDay, birthMonth, birthYear, advanceButton;
    private LinearLayout birthdayBox;

    private int gender = 0;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_part_1);

        day_start = -1;
        month_start = -1;
        year_start = -1;
        age = -1;

        dateFormat = new DateFormat(this);

        name = (EditText) findViewById(R.id.name);
        photo = (ImageView) findViewById(R.id.photo);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        birthDay = (TextView) findViewById(R.id.birthDay);
        birthMonth = (TextView) findViewById(R.id.birthMonth);
        birthYear = (TextView) findViewById(R.id.birthYear);
        birthdayBox = (LinearLayout) findViewById(R.id.birthdayBox);
        advanceButton = (TextView) findViewById(R.id.advanceButton);

        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        m_title2 = (TextView) findViewById(R.id.text2);

        mBackButton.setOnClickListener(this);
        birthdayBox.setOnClickListener(this);
        advanceButton.setOnClickListener(this);
        birthDay.setOnClickListener(this);
        birthMonth.setOnClickListener(this);
        birthYear.setOnClickListener(this);
        photo.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);
        advanceButton.setOnTouchListener(this);

        wrap = (UserWrapper) getIntent().getSerializableExtra("user_wrapper");
        if(wrap!=null)
            setLayoutError();

        m_title.setText(getResources().getString(R.string.register));
        m_title2.setText(getResources().getString(R.string.register_steps, 1, 3));

        final List<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.register_not_specified));
        list.add(getResources().getString(R.string.register_male));
        list.add(getResources().getString(R.string.register_female));
        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.genderPicker);
        spinner.setItems(list);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                gender = position;
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "genderPicker" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    private void setLayoutError() {
        User user = wrap.getUser();

        findViewById(R.id.facebookErrorText1).setVisibility(View.VISIBLE);
        findViewById(R.id.facebookErrorText2).setVisibility(View.VISIBLE);

        findViewById(R.id.photoBox).setVisibility(View.GONE);
        password.setVisibility(View.GONE);

        if(!user.getName().matches(""))
            name.setVisibility(View.GONE);

        if(!user.getEmail().matches(""))
            email.setVisibility(View.GONE);

        if(!user.getGender().matches("")) {
            findViewById(R.id.genderText).setVisibility(View.GONE);
            findViewById(R.id.genderPicker).setVisibility(View.GONE);
        }

        if(user.getDayBorn() != 0) {
            findViewById(R.id.birthDateText).setVisibility(View.GONE);
            findViewById(R.id.birthdayBox).setVisibility(View.GONE);
        }

    }

    private void register_error_facebook() {
        User user = wrap.getUser();
        setError();

        String et_name = name.getText().toString();
        String et_email = email.getText().toString();
        String genderStr;

        switch (gender){
            case 1: genderStr = "male"; break;
            case 2: genderStr = "female"; break;
            default: genderStr = "not specified"; break;
        }

        LocalDate birthdate;
        LocalDate now = new LocalDate();
        Period period;

        if (!(day_start == -1 || month_start == -1 || year_start == -1)){
            birthdate = new LocalDate (year_start, month_start+1, day_start);
            period = new Period(birthdate, now, PeriodType.yearMonthDay());
            age = period.getYears();
        }
        else{
            age = -1;
        }

        int err = 0;

        if (!validateFields(et_name) && user.getName().matches("")) {
            err++;
            name.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_required_name), Toast.LENGTH_LONG).show();
        } else if (!validateEmail(et_email) && user.getEmail().matches("")) {
            err++;
            email.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_email_required), Toast.LENGTH_LONG).show();
        }else if(user.getDayBorn() == 0 && age < 13) {
            if (age == -1) {
                err++;
                Toast.makeText(this, getResources().getString(R.string.validation_field_birth_date_required), Toast.LENGTH_LONG).show();
            }
            else if (age < 13) {
                err++;
                Toast.makeText(this, getResources().getString(R.string.validation_field_register_minimum_age), Toast.LENGTH_LONG).show();
            }
        }

        if (err == 0) {

            if(user.getName().matches("")) {
                user.setName(et_name);
                user.setModifyFacebookName(true);
            }

            if(user.getEmail().matches("")) {
                user.setEmail(et_email);
                user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            }

            if(user.getGender().matches(""))
                user.setGender(genderStr);

            if(user.getDayBorn() == 0) {
                user.setDayBorn(day_start);
                user.setMonthBorn(month_start+1);
                user.setYearBorn(year_start);
            }

            Intent register = new Intent(RegisterPart1Activity.this, RegisterPart2Activity.class);

            UserWrapper wrapper = new UserWrapper(wrap.getUser());
            register.putExtra("user_wrapper", wrapper);

            startActivity(register);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);

        }
    }

    private void register() {

        setError();

        String et_name = name.getText().toString();
        String et_email = email.getText().toString();
        String et_password = password.getText().toString();
        String genderStr;

        switch (gender){
            case 1: genderStr = "male"; break;
            case 2: genderStr = "female"; break;
            default: genderStr = "not specified"; break;
        }

        LocalDate birthdate;
        LocalDate now = new LocalDate();
        Period period;

        if (!(day_start == -1 || month_start == -1 || year_start == -1)){
            birthdate = new LocalDate (year_start, month_start+1, day_start);
            period = new Period(birthdate, now, PeriodType.yearMonthDay());
            age = period.getYears();
        }
        else{
            age = -1;
        }

        int err = 0;

        if (!validateFields(et_name)) {
            err++;
            name.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_required_name), Toast.LENGTH_LONG).show();
        }
        else if (!validateEmail(et_email)) {
            err++;
            email.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_email_required), Toast.LENGTH_LONG).show();
        }
        else if (!validateFields(et_password)) {
            err++;
            password.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_required_password), Toast.LENGTH_LONG).show();
        }
        else if (!validatePasswordSize(et_password)) {
            err++;
            password.setError(getResources().getString(R.string.validation_field_password_minimum));
            Toast.makeText(this, getResources().getString(R.string.validation_field_password_minimum), Toast.LENGTH_LONG).show();
        }
        else if (age == -1) {
            err++;
            Toast.makeText(this, getResources().getString(R.string.validation_field_birth_date_required), Toast.LENGTH_LONG).show();
        }
        else if(age < 13){
            err++;
            Toast.makeText(this, getResources().getString(R.string.validation_field_register_minimum_age), Toast.LENGTH_LONG).show();
        }

        if (err == 0) {

            User user = new User();
            user.setName(et_name);
            user.setEmail(et_email);
            user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            user.setPassword(et_password);
            user.setLivesIn("");
            user.setGender(genderStr);
            user.setDayBorn(day_start);
            user.setMonthBorn(month_start+1);
            user.setYearBorn(year_start);
            user.setPhoto("");
            user.setFacebookMessenger("");
            user.setWhereStudy("");
            user.setWhereWork("");
            user.setDescription("");
            user.setUrl("");
            user.setFromFacebook(false);
            user.setIdFacebook("");

            Intent register = new Intent(RegisterPart1Activity.this, RegisterPart2Activity.class);

            UserWrapper wrapper = new UserWrapper(user);
            register.putExtra("user_wrapper", wrapper);

            startActivity(register);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);

        }
    }

    private void setError() {

        name.setError(null);
        email.setError(null);
        password.setError(null);
        birthYear.setError(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,monthOfYear,dayOfMonth);

        day_start = dayOfMonth;
        month_start = monthOfYear;
        year_start = year;

        birthDay.setText(String.format("%02d", day_start));
        birthMonth.setText(dateFormat.formatMonthShort(month_start+1));
        birthYear.setText(String.valueOf(year_start));

    }

    @Override
    public void onClick(View view) {
        if(view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        }
        else if (view == advanceButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "advanceButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if(wrap==null)
                register();
            else
                register_error_facebook();
        }
        else if (view == photo) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "photo" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startCropImageActivity(null);
        }
        else if(view == birthdayBox || view == birthDay || view == birthMonth || view == birthYear) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "birthdayBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();
            if(year_start != -1)
                now.set(year_start, month_start, day_start);
            com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setAccentColor(ContextCompat.getColor(this,R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;

                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);
                    bitmap = CropImage.toOvalBitmap(bitmap);

                    TymoApplication application = TymoApplication.getInstance();
                    application.setInputStreamer(new ByteArrayInputStream(stream.toByteArray()));
                }
                catch (Exception e){
                    Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                }
                if(bitmap != null)
                    photo.setImageBitmap(bitmap);
                else
                    photo.setImageURI(result.getUri());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, getResources().getString(R.string.error_modify_profile_photo), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Start crop image activity for the given image.
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1)
                .setFixAspectRatio(true)
                .setMaxZoom(8)
                .start(this);
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_100));
            }
        }
        else if (view == advanceButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                advanceButton.setTextColor(ContextCompat.getColor(this, R.color.white));
                advanceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_login_advance));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                advanceButton.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_100));
                advanceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_login_advance_pressed));
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");
        if(dpd != null) dpd.setOnDateSetListener(this);
    }

}
