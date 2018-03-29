package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.facebook.rebound.SpringSystem;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateEmail;
import static io.development.tymo.utils.Validation.validateFields;
import static io.development.tymo.utils.Validation.validatePasswordSize;

public class AboutActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener, View.OnTouchListener {

    private ImageView mBackButton, icon2, profilePhoto;
    private TextView mTitle, profilePhotoText, age, confirmationButton;
    private EditText name, description, url;
    private EditText locationWhereLives, professionWhereWorks, whereStudied;
    private TextView birthDate, birthDateFacebook, genderFacebook, locationWhereLivesFacebook, professionWhereWorksFacebook, whereStudiedFacebook;
    private Switch facebookMessengerSwitch;
    private DateFormat dateFormat;
    private int day_start, month_start, year_start, ageInt, gender;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Cloudinary cloudinary;
    private UploadCloudinary uploadCloudinary;
    private InputStream inputStream;
    private String oldUrl = "";
    private MaterialSpinner spinner;
    private Rect rect;
    private RelativeLayout birthDateBar, profilePhotoBox, confirmationBar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserWrapper userWrapper = (UserWrapper) getIntent().getSerializableExtra("user_about");
        user = userWrapper.getUser();

        if (user.getFromFacebook()) {
            setContentView(R.layout.activity_about_edit_facebook);
            setLayoutFacebook();
        } else {
            setContentView(R.layout.activity_about_edit);
            birthDateBar = (RelativeLayout) findViewById(R.id.birthDateBar);
            birthDateBar.setOnClickListener(this);

            final List<String> list = new ArrayList<String>();
            list.add(getResources().getString(R.string.register_not_specified));
            list.add(getResources().getString(R.string.register_male));
            list.add(getResources().getString(R.string.register_female));
            spinner = (MaterialSpinner) findViewById(R.id.genderPicker);
            spinner.setItems(list);
            spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    gender = position;
                    confirmationBar.setVisibility(View.VISIBLE);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "genderPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
            });

            setLayoutNormal();
        }

        confirmationBar = (RelativeLayout) findViewById(R.id.confirmationBar);

        confirmationBar.setVisibility(View.GONE);

        mSubscriptions = new CompositeDisposable();
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(this));
        uploadCloudinary = new UploadCloudinary();

        findViewById(R.id.icon1).setVisibility(View.GONE);

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        icon2 = (ImageView) findViewById(R.id.icon2);
        mTitle = (TextView) findViewById(R.id.text);

        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        url = (EditText) findViewById(R.id.infoToContact);
        profilePhotoText = (TextView) findViewById(R.id.profilePhotoText);
        profilePhotoBox = (RelativeLayout) findViewById(R.id.profilePhotoBox);

        name.setText(user.getName());
        confirmationButton.setText(R.string.save_updates);
        description.setText(user.getDescription());
        url.setText(user.getUrl());

        if (!user.getPhoto().matches("")) {
            String[] split = user.getPhoto().split("/");
            oldUrl = split[split.length - 1];
            oldUrl = oldUrl.substring(0, oldUrl.length() - 4);

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
        } else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);


        icon2.setVisibility(View.INVISIBLE);

        mBackButton.setOnClickListener(this);
        profilePhotoText.setOnClickListener(this);
        profilePhotoBox.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        mBackButton.setOnTouchListener(this);
        profilePhotoText.setOnTouchListener(this);

        mTitle.setText(getResources().getString(R.string.about));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);

        new Actor.Builder(SpringSystem.create(), profilePhotoBox)
                .addMotion(new ToggleImitator(null, 1.0, 0.8), View.SCALE_X, View.SCALE_Y)
                .onTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_UP:
                                if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                                }
                                break;
                            case MotionEvent.ACTION_DOWN:
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                        }
                        return true;
                    }
                })
                .build();

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmationBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmationBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmationBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


    }

    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void updateAbout(User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);
        finish();
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //RELATIONSHIP_UPDATED_SUCCESSFULLY
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    private String getGender(String gender) {
        if (gender.matches("male"))
            return getResources().getString(R.string.register_male);
        else if (gender.matches("female"))
            return getResources().getString(R.string.register_female);
        else
            return getResources().getString(R.string.register_not_specified);
    }

    public void setLayoutFacebook() {
        birthDateFacebook = (TextView) findViewById(R.id.birthDate);
        genderFacebook = (TextView) findViewById(R.id.gender);
        locationWhereLivesFacebook = (TextView) findViewById(R.id.locationWhereLives);
        professionWhereWorksFacebook = (TextView) findViewById(R.id.whereWorks);
        whereStudiedFacebook = (TextView) findViewById(R.id.whereStudied);
        facebookMessengerSwitch = (Switch) findViewById(R.id.facebookMessengerSwitch);
        age = (TextView) findViewById(R.id.age);

        dateFormat = new DateFormat(this);

        if (user.getYearBorn() != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(user.getYearBorn(), user.getMonthBorn() - 1, user.getDayBorn());

            String d = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
            String m = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH) + 1);
            String date = getResources().getString(R.string.date_format_10, d, m, String.valueOf(user.getYearBorn()));

            LocalDate birthdate = new LocalDate(user.getYearBorn(), user.getMonthBorn(), user.getDayBorn());
            LocalDate now = new LocalDate();
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
            age.setText(getResources().getString(R.string.about_years, period.getYears()));
            birthDateFacebook.setText(date);
        } else
            findViewById(R.id.birthDateBox).setVisibility(View.GONE);

        genderFacebook.setText(getGender(user.getGender()));
        locationWhereLivesFacebook.setText(user.getLivesIn());
        professionWhereWorksFacebook.setText(user.getWorksAt());
        whereStudiedFacebook.setText(user.getStudiedAt());
        facebookMessengerSwitch.setChecked(user.isFacebookMessengerEnable());

        facebookMessengerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setFacebookMessengerEnable(isChecked);
            }
        });
    }

    public void setLayoutNormal() {
        birthDate = (TextView) findViewById(R.id.birthDate);
        locationWhereLives = (EditText) findViewById(R.id.locationWhereLives);
        professionWhereWorks = (EditText) findViewById(R.id.whereWorks);
        whereStudied = (EditText) findViewById(R.id.whereStudied);
        age = (TextView) findViewById(R.id.age);

        dateFormat = new DateFormat(this);

        day_start = user.getDayBorn();
        month_start = user.getMonthBorn() - 1;
        year_start = user.getYearBorn();
        ageInt = -1;

        Calendar calendar = Calendar.getInstance();
        calendar.set(user.getYearBorn(), user.getMonthBorn() - 1, user.getDayBorn());

        String d = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String m = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH) + 1);
        String date = getResources().getString(R.string.date_format_10, d, m, String.valueOf(user.getYearBorn()));

        birthDate.setText(date);
        locationWhereLives.setText(user.getLivesIn());
        professionWhereWorks.setText(user.getWorksAt());
        whereStudied.setText(user.getStudiedAt());

        if (user.getGender().matches("male")) {
            gender = 1;
            spinner.setSelectedIndex(1);
        } else if (user.getGender().matches("female")) {
            gender = 2;
            spinner.setSelectedIndex(2);
        } else {
            gender = 0;
            spinner.setSelectedIndex(0);
        }

        LocalDate birthdate = new LocalDate(user.getYearBorn(), user.getMonthBorn(), user.getDayBorn());
        LocalDate now = new LocalDate();
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        age.setText(getResources().getString(R.string.about_years, period.getYears()));

    }

    public void updateFacebook() {
        String et_name = name.getText().toString();

        int err = 0;

        if (!validateFields(et_name)) {
            err++;
            name.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_required_name), Toast.LENGTH_LONG).show();
        }

        if (err == 0) {
            if (!name.getText().toString().equals(user.getName()))
                user.setModifyFacebookName(true);

            user.setName(name.getText().toString());
            user.setDescription(description.getText().toString());
            user.setUrl(url.getText().toString());
            inputStream = TymoApplication.getInstance().getInputStreamer();

            if (inputStream != null) {
                setProgress(true);
                user.setModifyFacebookPhoto(true);
                uploadCloudinary.execute(user);
            } else
                updateAbout(user);
        }
    }

    public void updateNormal() {
        String et_name = name.getText().toString();
        LocalDate birthdate;
        LocalDate now = new LocalDate();
        Period period;

        if (!(day_start == -1 || month_start == -1 || year_start == -1)) {
            birthdate = new LocalDate(year_start, month_start + 1, day_start);
            period = new Period(birthdate, now, PeriodType.yearMonthDay());
            ageInt = period.getYears();
        } else {
            ageInt = -1;
        }

        int err = 0;

        if (!validateFields(et_name)) {
            err++;
            name.setError(getResources().getString(R.string.validation_field_invalid_required_field));
            Toast.makeText(this, getResources().getString(R.string.validation_field_required_name), Toast.LENGTH_LONG).show();
        } else if (ageInt == -1) {
            err++;
            Toast.makeText(this, getResources().getString(R.string.validation_field_birth_date_required), Toast.LENGTH_LONG).show();
        } else if (ageInt < 13) {
            err++;
            Toast.makeText(this, getResources().getString(R.string.validation_field_register_minimum_age), Toast.LENGTH_LONG).show();
        }

        if (err == 0) {

            user.setName(name.getText().toString());
            user.setDescription(description.getText().toString());
            user.setUrl(url.getText().toString());
            user.setLivesIn(locationWhereLives.getText().toString());
            user.setWhereWork(professionWhereWorks.getText().toString());
            user.setWhereStudy(whereStudied.getText().toString());
            user.setDayBorn(day_start);
            user.setMonthBorn(month_start + 1);
            user.setYearBorn(year_start);

            if (gender == 1)
                user.setGender("male");
            else if (gender == 2)
                user.setGender("female");
            else
                user.setGender("not specified");

            inputStream = TymoApplication.getInstance().getInputStreamer();

            if (inputStream != null) {
                setProgress(true);
                uploadCloudinary.execute(user);
            } else
                updateAbout(user);

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);

        day_start = dayOfMonth;
        month_start = monthOfYear;
        year_start = year;

        dateFormat = new DateFormat(this);

        String d = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String m = dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH) + 1);
        String date = getResources().getString(R.string.date_format_10, d, m, String.valueOf(year));

        birthDate.setText(date);

        LocalDate birthdate = new LocalDate(year_start, month_start + 1, day_start);
        LocalDate now = new LocalDate();
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        age.setText(getResources().getString(R.string.about_years, period.getYears()));

        confirmationBar.setVisibility(View.VISIBLE);

    }

    private void createDialogBack() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.back_without_saving));
        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));

        Dialog dg = new Dialog(this, R.style.NewDialog);

        dg.setContentView(customView);
        dg.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dg.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                dg.dismiss();
            }
        });

        dg.show();
    }

    @Override
    public void onClick(View view) {
        if (view == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (confirmationBar.getVisibility() == View.VISIBLE){
                createDialogBack();
            }
            else {
                onBackPressed();
            }
        } else if (view == profilePhotoBox || view == profilePhotoText) {
            confirmationBar.setVisibility(View.VISIBLE);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "profilePhoto" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startCropImageActivity(null);
        } else if (view == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            if (user.getFromFacebook()) {
                updateFacebook();
            } else {
                updateNormal();
            }
        } else if (view == birthDateBar) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "birthdayBox" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Calendar now = Calendar.getInstance();
            if (year_start != -1)
                now.set(year_start, month_start, day_start);
            com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog");
        }
    }

    /**
     * Start crop image activity for the given image.
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .setFixAspectRatio(true)
                .setMaxZoom(8)
                .start(this);
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
                } catch (Exception e) {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
                if (bitmap != null)
                    profilePhoto.setImageBitmap(bitmap);
                else
                    profilePhoto.setImageURI(result.getUri());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UploadCloudinary extends AsyncTask<User, Void, User> {

        protected User doInBackground(User... users) {
            User mUser = users[0];
            try {
                Map options = ObjectUtils.asMap(
                        "transformation", new Transformation().width(600).height(600).crop("limit").quality(10).fetchFormat("png")
                );
                Map uploadResult = cloudinary.uploader().upload(inputStream, options);
                if (oldUrl.length() < 26 && !oldUrl.matches(""))
                    cloudinary.uploader().destroy(oldUrl, ObjectUtils.asMap("invalidate", true));

                mUser.setPhoto((String) uploadResult.get("secure_url"));
            } catch (Exception e) {
                if (!Utilities.isDeviceOnline(AboutActivity.this))
                    Toast.makeText(AboutActivity.this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(AboutActivity.this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
            }
            return mUser;
        }

        protected void onPostExecute(User user) {
            updateAbout(user);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == profilePhotoText) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                profilePhotoText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                profilePhotoText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        }

        return false;
    }

}
