package io.development.tymo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import io.development.tymo.utils.DateFormat;
import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackButton, icon2, profilePhoto;
    private TextView mTitle, profilePhotoText, age, confirmationButton;
    private EditText name, description, url;
    private EditText locationWhereLives, professionWhereWorks, whereStudied;
    private TextView birthDate, gender, birthDateFacebook, genderFacebook, locationWhereLivesFacebook, professionWhereWorksFacebook, whereStudiedFacebook;
    private Switch facebookMessengerSwitch;
    private DateFormat dateFormat;

    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Cloudinary cloudinary;
    private UploadCloudinary uploadCloudinary;
    private InputStream inputStream;
    private String oldUrl = "";

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserWrapper userWrapper = (UserWrapper)getIntent().getSerializableExtra("user_about");
        user = userWrapper.getUser();

        if(user.getFromFacebook()) {
            setContentView(R.layout.activity_about_edit_facebook);
            setLayoutFacebook();
        }
        else {
            setContentView(R.layout.activity_about_edit);
            setLayoutNormal();
        }

        mSubscriptions = new CompositeSubscription();
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

        name.setText(user.getName());
        confirmationButton.setText(R.string.save_updates);
        description.setText(user.getDescription());
        url.setText(user.getUrl());

        if(!user.getPhoto().matches("")) {
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
        }else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);


        icon2.setVisibility(View.INVISIBLE);

        mBackButton.setOnClickListener(this);
        profilePhotoText.setOnClickListener(this);
        profilePhoto.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);

        mTitle.setText(getResources().getString(R.string.about));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void updateAbout(User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);
        finish();
        //Toast.makeText(this, ServerMessage.getServerMessage(this, response.getMessage()), Toast.LENGTH_LONG).show();
        //RELATIONSHIP_UPDATED_SUCCESSFULLY
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    private String getGender(String gender){
        if(gender.matches("male"))
            return getResources().getString(R.string.register_male);
        else
            return getResources().getString(R.string.register_female);
    }

    public void setLayoutFacebook(){
        birthDateFacebook = (TextView) findViewById(R.id.birthDate);
        genderFacebook = (TextView) findViewById(R.id.gender);
        locationWhereLivesFacebook = (TextView) findViewById(R.id.locationWhereLives);
        professionWhereWorksFacebook = (TextView) findViewById(R.id.whereWorks);
        whereStudiedFacebook = (TextView) findViewById(R.id.whereStudied);
        facebookMessengerSwitch = (Switch) findViewById(R.id.facebookMessengerSwitch);
        age = (TextView) findViewById(R.id.age);

        dateFormat = new DateFormat(this);

        if(user.getYearBorn() != 0) {
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
        }else
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

    public void setLayoutNormal(){
        birthDate = (TextView) findViewById(R.id.birthDate);
        gender = (TextView) findViewById(R.id.gender);
        locationWhereLives = (EditText) findViewById(R.id.locationWhereLives);
        professionWhereWorks = (EditText) findViewById(R.id.whereWorks);
        whereStudied = (EditText) findViewById(R.id.whereStudied);
        age = (TextView) findViewById(R.id.age);

        dateFormat = new DateFormat(this);

        Calendar calendar = Calendar.getInstance();
        calendar.set(user.getYearBorn(),user.getMonthBorn()-1,user.getDayBorn());

        String d= new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String m= dateFormat.formatMonthLowerCase(calendar.get(Calendar.MONTH)+1);
        String date = getResources().getString(R.string.date_format_10, d, m, String.valueOf(user.getYearBorn()));

        birthDate.setText(date);
        gender.setText(getGender(user.getGender()));
        locationWhereLives.setText(user.getLivesIn());
        professionWhereWorks.setText(user.getWorksAt());
        whereStudied.setText(user.getStudiedAt());

        LocalDate birthdate = new LocalDate (user.getYearBorn(),user.getMonthBorn(),user.getDayBorn());
        LocalDate now = new LocalDate();
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        age.setText(getResources().getString(R.string.about_years, period.getYears()));

    }

    public void updateFacebook(){
        if(!name.getText().toString().matches(user.getName()))
            user.setModifyFacebookName(true);

        user.setName(name.getText().toString());
        user.setDescription(description.getText().toString());
        user.setUrl(url.getText().toString());
        inputStream = TymoApplication.getInstance().getInputStreamer();

        if(inputStream != null) {
            setProgress(true);
            user.setModifyFacebookPhoto(true);
            uploadCloudinary.execute(user);
        }
        else
            updateAbout(user);
    }

    public void updateNormal(){
        user.setName(name.getText().toString());
        user.setDescription(description.getText().toString());
        user.setUrl(url.getText().toString());
        user.setLivesIn(locationWhereLives.getText().toString());
        user.setWhereWork(professionWhereWorks.getText().toString());
        user.setWhereStudy(whereStudied.getText().toString());
        inputStream = TymoApplication.getInstance().getInputStreamer();

        if(inputStream != null) {
            setProgress(true);
            uploadCloudinary.execute(user);
        }
        else
            updateAbout(user);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        else if(view == profilePhoto || view == profilePhotoText) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "profilePhoto" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startCropImageActivity(null);
        }
        else if(view == confirmationButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            if(user.getFromFacebook()) {
                updateFacebook();
            }
            else {
                updateNormal();
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
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
                if(bitmap != null)
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
                if(oldUrl.length() < 26 && !oldUrl.matches(""))
                    cloudinary.uploader().destroy(oldUrl, ObjectUtils.asMap("invalidate", true));

                mUser.setPhoto((String) uploadResult.get("secure_url"));
            } catch (Exception e) {
                Toast.makeText(AboutActivity.this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
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
        mSubscriptions.unsubscribe();
    }

}
