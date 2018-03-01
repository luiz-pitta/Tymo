package io.development.tymo.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.christophesmet.android.views.colorpicker.ColorPickerView;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.imitator.ToggleImitator;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.CustomizeAddActivityAdapter;
import io.development.tymo.adapters.SectionedGridRecyclerViewAdapter;
import io.development.tymo.fragments.FlagEditFragment;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.CategoryServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.IconServer;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.DateFormat;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static io.development.tymo.utils.Validation.validateFields;

public class AddPart1Activity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {

    private Rect rect;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DateFormat dateFormat;
    private ArrayList<IconServer> iconList;
    private ActivityServer activityServer;
    private Calendar calendarStart;

    private TextView customizeApplyButton, customizeCleanButton, confirmationButton, repeatMax, repeatAddText, repeatText;
    private TextView titleMax, dateStart, dateEnd, timeStart, timeEnd, locationText, locationTextAdd;
    private EditText titleEditText, descriptionEditText, whatsAppEditText, repeatEditText;
    private ImageView cubeLowerBoxIcon, cubeUpperBoxIcon, pieceIcon, customizeCubeLowerBoxIcon, customizeCubeUpperBoxIcon, customizePieceIcon;
    private ImageView mBackButton, repeatAddIcon;
    private ImageView clearDateStart, clearDateEnd, clearTimeStart, clearTimeEnd;
    private ImageView locationIconAdd, locationIcon, locationIcon2;
    private RelativeLayout pieceBox, locationBox, repeatAdd;
    private LinearLayout repeatBox, repeatNumberBox, locationBoxAdd;
    private ColorPickerView customizeColorPicker;
    private MaterialSpinner spinnerRepeatPicker;
    private View viewClicked;

    private int repeat_type = 0;
    private int repeat_qty = -1;
    private double lat = -500;
    private double lng = -500;
    private static final int PLACE_PICKER_REQUEST = 1020;
    private PlacePicker.IntentBuilder builder = null;
    private Intent placePicker = null;
    private String location = "";
    private int day_start, month_start, year_start;
    private int day_end, month_end, year_end;
    private int minutes_start, hour_start;
    private int minutes_end, hour_end;
    private int d, m, y;
    private boolean locationNotWorking = false;
    private boolean first_open = true;
    private String urlIcon = Constants.IC_ADD_CUBE_URL;
    private boolean error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_create_step_1);

        mSubscriptions = new CompositeDisposable();

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        cubeLowerBoxIcon = (ImageView) findViewById(R.id.cubeLowerBoxIcon);
        cubeUpperBoxIcon = (ImageView) findViewById(R.id.cubeUpperBoxIcon);
        pieceIcon = (ImageView) findViewById(R.id.pieceIcon);
        pieceBox = (RelativeLayout) findViewById(R.id.pieceBox);
        titleEditText = (EditText) findViewById(R.id.title);
        titleMax = (TextView) findViewById(R.id.titleMax);
        descriptionEditText = (EditText) findViewById(R.id.description);
        dateStart = (TextView) findViewById(R.id.dateStart);
        dateEnd = (TextView) findViewById(R.id.dateEnd);
        timeStart = (TextView) findViewById(R.id.timeStart);
        timeEnd = (TextView) findViewById(R.id.timeEnd);
        clearDateStart = (ImageView) findViewById(R.id.clearDateStart);
        clearDateEnd = (ImageView) findViewById(R.id.clearDateEnd);
        clearTimeStart = (ImageView) findViewById(R.id.clearTimeStart);
        clearTimeEnd = (ImageView) findViewById(R.id.clearTimeEnd);
        repeatAdd = (RelativeLayout) findViewById(R.id.repeatAdd);
        repeatAddIcon = (ImageView) findViewById(R.id.repeatAddIcon);
        repeatAddText = (TextView) findViewById(R.id.repeatAddText);
        repeatBox = (LinearLayout) findViewById(R.id.repeatBox);
        repeatNumberBox = (LinearLayout) findViewById(R.id.repeatNumberBox);
        repeatEditText = (EditText) findViewById(R.id.repeatEditText);
        repeatMax = (TextView) findViewById(R.id.repeatMax);
        locationBoxAdd = (LinearLayout) findViewById(R.id.locationBoxAdd);
        locationIconAdd = (ImageView) findViewById(R.id.locationIconAdd);
        locationTextAdd = (TextView) findViewById(R.id.locationTextAdd);
        locationBox = (RelativeLayout) findViewById(R.id.locationBox);
        locationIcon = (ImageView) findViewById(R.id.locationIcon);
        locationIcon2 = (ImageView) findViewById(R.id.locationIcon2);
        locationText = (TextView) findViewById(R.id.locationText);
        whatsAppEditText = (EditText) findViewById(R.id.whatsAppGroupLink);
        spinnerRepeatPicker = (MaterialSpinner) findViewById(R.id.repeatPicker);
        repeatText = (TextView) findViewById(R.id.repeatText);

        mBackButton.setOnClickListener(this);
        pieceBox.setOnClickListener(this);
        confirmationButton.setOnClickListener(this);
        dateStart.setOnClickListener(this);
        dateEnd.setOnClickListener(this);
        timeStart.setOnClickListener(this);
        timeEnd.setOnClickListener(this);
        clearDateStart.setOnClickListener(this);
        clearDateEnd.setOnClickListener(this);
        clearTimeStart.setOnClickListener(this);
        clearTimeEnd.setOnClickListener(this);
        repeatAdd.setOnClickListener(this);
        locationBoxAdd.setOnClickListener(this);
        locationBox.setOnClickListener(this);
        locationBox.setOnLongClickListener(this);
        locationIcon2.setOnClickListener(this);

        mBackButton.setOnTouchListener(this);
        repeatAdd.setOnTouchListener(this);
        locationBoxAdd.setOnTouchListener(this);
        locationBox.setOnTouchListener(this);
        locationIcon2.setOnTouchListener(this);

        titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
        confirmationButton.setText(R.string.advance);

        clearDateStart.setVisibility(View.GONE);
        clearDateEnd.setVisibility(View.GONE);
        clearTimeStart.setVisibility(View.GONE);
        clearTimeEnd.setVisibility(View.GONE);
        repeatBox.setVisibility(View.GONE);
        repeatText.setVisibility(View.GONE);

        dateFormat = new DateFormat(this);

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                titleMax.setText(getString(R.string.title_max_caract, titleEditText.length()));
            }
        });

        repeatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int limit;
                int numberS;
                String maxText;

                if (String.valueOf(s).matches("")) {
                    numberS = 0;
                } else {
                    numberS = Integer.valueOf(String.valueOf(s));
                }

                if (repeat_type == 2) {
                    limit = 53;
                    maxText = getString(R.string.repeat_max_time_2);
                } else if (repeat_type == 3) {
                    limit = 12;
                    maxText = getString(R.string.repeat_max_time_3);
                } else {
                    limit = 365;
                    maxText = getString(R.string.repeat_max_time_1);
                }

                if (numberS > limit) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatMax.setText(maxText);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                    repeatMax.setText(getString(R.string.repeat_max_time));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int limit;
                int numberS;
                String maxText;

                if (String.valueOf(s).matches("")) {
                    numberS = 0;
                } else {
                    numberS = Integer.valueOf(String.valueOf(s));
                }

                if (repeat_type == 2) {
                    limit = 53;
                    maxText = getString(R.string.repeat_max_time_2);
                } else if (repeat_type == 3) {
                    limit = 12;
                    maxText = getString(R.string.repeat_max_time_3);
                } else {
                    limit = 365;
                    maxText = getString(R.string.repeat_max_time_1);
                }

                if (numberS > limit) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatMax.setText(maxText);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                    repeatMax.setText(getString(R.string.repeat_max_time));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int limit;
                int numberS;
                String maxText;

                if (String.valueOf(s).matches("")) {
                    numberS = 0;
                } else {
                    numberS = Integer.valueOf(String.valueOf(s));
                }

                if (repeat_type == 2) {
                    limit = 53;
                    maxText = getString(R.string.repeat_max_time_2);
                } else if (repeat_type == 3) {
                    limit = 12;
                    maxText = getString(R.string.repeat_max_time_3);
                } else {
                    limit = 365;
                    maxText = getString(R.string.repeat_max_time_1);
                }

                if (numberS > limit) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatMax.setText(maxText);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                    repeatMax.setText(getString(R.string.repeat_max_time));
                }
            }
        });

        new Actor.Builder(SpringSystem.create(), pieceBox)
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

        getIcons();

        calendarStart = Calendar.getInstance();

        day_start = -1;
        month_start = -1;
        year_start = -1;
        day_end = -1;
        month_end = -1;
        year_end = -1;
        minutes_start = -1;
        minutes_end = -1;
        hour_start = -1;
        hour_end = -1;

        error = false;

        spinnerRepeatPicker.setItems(getResources().getStringArray(R.array.array_repeat_type));
        spinnerRepeatPicker.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                repeat_type = position;

                int limit;
                int numberS;
                String maxText;

                if (repeatEditText.getText().toString().matches("")) {
                    numberS = 0;
                } else {
                    numberS = Integer.parseInt(repeatEditText.getText().toString());
                }

                if (repeat_type == 2) {
                    limit = 53;
                    maxText = getString(R.string.repeat_max_time_2);
                } else if (repeat_type == 3) {
                    limit = 12;
                    maxText = getString(R.string.repeat_max_time_3);
                } else {
                    limit = 365;
                    maxText = getString(R.string.repeat_max_time_1);
                }

                if (numberS > limit) {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.red_600));
                    repeatMax.setText(maxText);
                } else {
                    repeatMax.setTextColor(ContextCompat.getColor(repeatMax.getContext(), R.color.grey_400));
                    repeatMax.setText(getString(R.string.repeat_max_time));
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "repeatPicker" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if (position != 0) {
                    repeatNumberBox.setVisibility(View.VISIBLE);
                } else {
                    repeatNumberBox.setVisibility(View.GONE);
                    repeatEditText.setText("");
                }
            }
        });

        builder = new PlacePicker.IntentBuilder();

        try {
            placePicker = builder.build(this);
        } catch (Exception e) {
            locationNotWorking = true;
        }

        locationBoxAdd.setVisibility(View.VISIBLE);
        locationBox.setVisibility(View.GONE);
        locationText.setText("");
        repeatAdd.setVisibility(View.VISIBLE);
        repeatBox.setVisibility(View.GONE);
        repeatNumberBox.setVisibility(View.GONE);

        activityServer = new ActivityServer();

        activityServer.setCubeColor(getResources().getColor(R.color.deep_purple_400));
        activityServer.setCubeColorUpper(getResources().getColor(R.color.deep_purple_400));
        activityServer.setCubeIcon(urlIcon);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20, getClass().getName().length()), null /* class override */);
    }

    private void getIcons() {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getIcons()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseIcons, this::handleError));
    }

    private void handleResponseIcons(ArrayList<IconServer> icons) {
        setProgress(false);
        iconList = new ArrayList<>();
        iconList.addAll(icons);
        Collections.sort(iconList, new Comparator<IconServer>() {
            @Override
            public int compare(IconServer c1, IconServer c2) {
                String u1 = c1.getCategory();
                String u2 = c2.getCategory();

                if (u1.matches("Tymo") && !u2.matches("Tymo"))
                    return -1;
                else if (!u1.matches("Tymo") && u2.matches("Tymo"))
                    return 1;
                else if (u1.matches("Outros") && !u2.matches("Outros"))
                    return 1;
                else if (!u1.matches("Outros") && u2.matches("Outros"))
                    return -1;
                else
                    return 0;
            }
        });

    }

    private void handleError(Throwable error) {
        if (!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        String day = new SimpleDateFormat("dd", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String month = new SimpleDateFormat("MM", getResources().getConfiguration().locale).format(calendar.getTime().getTime());
        String date = day + "/" + month + "/" + year;


        if (viewClicked == dateStart) {
            day_start = dayOfMonth;
            month_start = monthOfYear;
            year_start = year;
            dateStart.setText(date);
        } else {
            day_end = dayOfMonth;
            month_end = monthOfYear;
            year_end = year;
            dateEnd.setText(date);
        }

        if (dateStart.getText().toString().matches("")) {
            clearDateStart.setVisibility(View.GONE);
        } else {
            clearDateStart.setVisibility(View.VISIBLE);
        }

        if (dateEnd.getText().toString().matches("")) {
            clearDateEnd.setVisibility(View.GONE);
        } else {
            clearDateEnd.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = String.format("%02d", hourOfDay);
        String minuteString = String.format("%02d", minute);
        String time = hourString + ":" + minuteString;

        if (viewClicked == timeStart) {
            hour_start = hourOfDay;
            minutes_start = minute;
            timeStart.setText(time);
        } else {
            hour_end = hourOfDay;
            minutes_end = minute;
            timeEnd.setText(time);
        }

        if (timeStart.getText().toString().matches("")) {
            clearTimeStart.setVisibility(View.GONE);
        } else {
            clearTimeStart.setVisibility(View.VISIBLE);
        }

        if (timeEnd.getText().toString().matches("")) {
            clearTimeEnd.setVisibility(View.GONE);
        } else {
            clearTimeEnd.setVisibility(View.VISIBLE);
        }

    }

    private boolean validateDateTime(Calendar calendarStart, Calendar calendarEnd) {
        if (calendarEnd.getTimeInMillis() < calendarStart.getTimeInMillis()) {
            return false;
        }

        return true;
    }

    public List<Integer> getDateFromView() {
        List<Integer> list = new ArrayList<>();

        list.add(day_start);
        list.add(month_start);
        list.add(year_start);

        list.add(day_end);
        list.add(month_end);
        list.add(year_end);

        list.add(minutes_start);
        list.add(hour_start);

        list.add(minutes_end);
        list.add(hour_end);

        return list;
    }

    public String getLocationFromView() {
        return locationText.getText().toString();
    }

    public List<Double> getLatLngFromView() {
        List<Double> list = new ArrayList<>();

        list.add(lat);
        list.add(lng);
        return list;
    }

    public List<Integer> getRepeatFromView() {
        List<Integer> list = new ArrayList<>();
        String temp = repeatEditText.getText().toString();
        if (repeat_type == 0)
            repeat_qty = -1;
        else if (temp.length() > 0)
            repeat_qty = Integer.parseInt(temp);

        list.add(repeat_type);
        list.add(repeat_qty);
        return list;
    }

    private ArrayList<CategoryServer> getIconCategory(ArrayList<CategoryServer> iconCategory, ArrayList<IconServer> icons) {
        int i, j = 0;

        for (i = 0; i < icons.size(); i++) {
            CategoryServer category = new CategoryServer();

            if (i == 0) {
                category.setName(icons.get(i).getCategory());
                category.setIconsQty(1);
                category.setPosition(0);
                iconCategory.add(category);
            } else if (icons.get(i).getCategory().matches(icons.get(i - 1).getCategory())) {
                iconCategory.get(j).setIconsQty(iconCategory.get(j).getIconsQty() + 1);
            } else {
                j++;
                category.setName(icons.get(i).getCategory());
                category.setIconsQty(1);
                category.setPosition(iconCategory.get(j - 1).getPosition() + iconCategory.get(j - 1).getIconsQty());
                iconCategory.add(category);
            }
        }

        return iconCategory;
    }

    private void createDialogSelectIcon() {
        LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.activity_customize_piece, null);
        customizeColorPicker = (ColorPickerView) customView.findViewById(R.id.colorpicker);
        customizeCubeLowerBoxIcon = (ImageView) customView.findViewById(R.id.cubeLowerBoxIcon);
        customizeCubeUpperBoxIcon = (ImageView) customView.findViewById(R.id.cubeUpperBoxIcon);
        customizePieceIcon = (ImageView) customView.findViewById(R.id.pieceIcon);
        ImageView closeButton = (ImageView) customView.findViewById(R.id.closeButton);
        RecyclerView recyclerIcons = (RecyclerView) customView.findViewById(R.id.recyclerIcons);

        customizeColorPicker.setDrawDebug(false);

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(customView, false)
                .build();

        customizeApplyButton = (TextView) customView.findViewById(R.id.applyButton);
        customizeApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cubeLowerBoxIcon.setColorFilter((int) customizeCubeLowerBoxIcon.getTag());
                cubeUpperBoxIcon.setColorFilter((int) customizeCubeUpperBoxIcon.getTag());
                cubeLowerBoxIcon.setTag(customizeCubeLowerBoxIcon.getTag());
                cubeUpperBoxIcon.setTag(customizeCubeUpperBoxIcon.getTag());

                Glide.clear(pieceIcon);
                Glide.with(AddPart1Activity.this)
                        .load(urlIcon)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(pieceIcon);

                if (activityServer != null) {
                    activityServer.setCubeColor((int) customizeCubeLowerBoxIcon.getTag());
                    activityServer.setCubeColorUpper((int) customizeCubeUpperBoxIcon.getTag());
                    activityServer.setCubeIcon(urlIcon);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "customizeApplyButtonPickIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        closeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    closeButton.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.grey_400));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeButton.setColorFilter(ContextCompat.getColor(dialog.getContext(), R.color.grey_200));
                }

                return false;
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        customizeCleanButton = (TextView) customView.findViewById(R.id.cleanButton);
        customizeCleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activityServer != null) {
                    customizeCubeLowerBoxIcon.setColorFilter(activityServer.getCubeColor());
                    customizeCubeUpperBoxIcon.setColorFilter(activityServer.getCubeColorUpper());
                    customizeCubeLowerBoxIcon.setTag(activityServer.getCubeColor());
                    customizeCubeUpperBoxIcon.setTag(activityServer.getCubeColorUpper());
                    urlIcon = activityServer.getCubeIcon();
                } else {
                    customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(customView.getContext(), R.color.deep_purple_400));
                    customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(customView.getContext(), R.color.deep_purple_400_light));
                    customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400));
                    customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400_light));
                    urlIcon = Constants.IC_ADD_CUBE_URL;
                }

                Glide.clear(customizePieceIcon);
                Glide.with(AddPart1Activity.this)
                        .load(urlIcon)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(customizePieceIcon);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "customizeCleanButtonPickIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        recyclerIcons.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 5);
        recyclerIcons.setLayoutManager(layoutManager);
        recyclerIcons.setNestedScrollingEnabled(false);

        CustomizeAddActivityAdapter adapter = new CustomizeAddActivityAdapter(this, iconList);

        ArrayList<CategoryServer> iconCategory = new ArrayList<>();
        List<Integer> positionsNull = new ArrayList<>();

        iconCategory = getIconCategory(iconCategory, iconList);

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();

        //Sections
        for (int i = 0; i < iconCategory.size(); i++) {
            sections.add(new SectionedGridRecyclerViewAdapter.Section(iconCategory.get(i).getPosition(), iconCategory.get(i).getName()));
            if (i == 0)
                positionsNull.add(0);
            else
                positionsNull.add(iconCategory.get(i - 1).getPosition() + iconCategory.get(i - 1).getIconsQty() + i);
        }

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(this, R.layout.list_item_section, R.id.textIcon, recyclerIcons, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        recyclerIcons.setAdapter(mSectionedAdapter);

        RecyclerItemClickListener.OnItemClickListener onItemClickListener = new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, MotionEvent e) {

                if (!positionsNull.contains(position)) {
                    for (int i = 0; i < positionsNull.size(); i++) {
                        if (position < positionsNull.get(i)) {
                            urlIcon = adapter.getItem(position - i).getUrl();
                            break;
                        }
                    }

                    Glide.clear(customizePieceIcon);
                    Glide.with(AddPart1Activity.this)
                            .load(urlIcon)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(customizePieceIcon);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "RecyclerItemPickIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


            }

            @Override
            public void onLongItemClick(View view, int position, MotionEvent e) {
            }
        };

        recyclerIcons.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerIcons, onItemClickListener)
        );

        customizeColorPicker.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {

                if (first_open && (cubeUpperBoxIcon.getTag() != null || (activityServer != null && activityServer.getCubeIcon().contains("http")))) {

                    if (activityServer != null) {
                        customizeCubeLowerBoxIcon.setColorFilter(activityServer.getCubeColor());
                        customizeCubeUpperBoxIcon.setColorFilter(activityServer.getCubeColorUpper());
                        customizeCubeLowerBoxIcon.setTag(activityServer.getCubeColor());
                        customizeCubeUpperBoxIcon.setTag(activityServer.getCubeColorUpper());
                    } else {
                        customizeCubeLowerBoxIcon.setColorFilter((int) cubeLowerBoxIcon.getTag());
                        customizeCubeUpperBoxIcon.setColorFilter((int) cubeUpperBoxIcon.getTag());
                        customizeCubeLowerBoxIcon.setTag(cubeLowerBoxIcon.getTag());
                        customizeCubeUpperBoxIcon.setTag(cubeUpperBoxIcon.getTag());
                    }
                    first_open = false;
                } else {
                    if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.red_A700)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.red_A700));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.red_A700_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.red_A700));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.red_A700_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.pink_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.pink_900)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_900));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_900_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_900));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.pink_900_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.purple_500)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.purple_500));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.purple_500_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.purple_500));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.purple_500_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_800)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_800));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_800_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_800));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_800_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.blue_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.blue_800)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_800));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_800_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_800));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_800_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_800)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_800));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_800_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_800));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.cyan_800_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.green_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.green_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.green_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.green_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.green_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.lime_600)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.lime_600));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.lime_600_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.lime_600));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.lime_600_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.deep_orange_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_orange_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_orange_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_orange_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_orange_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.brown_400)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_400));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_400_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_400));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_400_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.brown_700)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_700));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_700_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_700));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.brown_700_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.grey_500)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.grey_500));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.grey_500_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.grey_500));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.grey_500_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_500)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_500));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_500_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_500));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_500_light));
                    } else if (color == ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_900)) {
                        customizeCubeLowerBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_900));
                        customizeCubeUpperBoxIcon.setColorFilter(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_900_light));
                        customizeCubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_900));
                        customizeCubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.blue_grey_900_light));
                    }
                }
            }
        });

        if (activityServer == null || !activityServer.getCubeIcon().contains("http")) {
            if (cubeUpperBoxIcon.getTag() == null) {
                urlIcon = Constants.IC_ADD_CUBE_URL;
                cubeLowerBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400));
                cubeUpperBoxIcon.setTag(ContextCompat.getColor(AddPart1Activity.this, R.color.deep_purple_400_light));
            } else {
                cubeLowerBoxIcon.setTag(cubeLowerBoxIcon.getTag());
                cubeUpperBoxIcon.setTag(cubeUpperBoxIcon.getTag());
            }

            Glide.clear(customizePieceIcon);
            Glide.with(AddPart1Activity.this)
                    .load(urlIcon)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(customizePieceIcon);
        } else {
            urlIcon = activityServer.getCubeIcon();
            cubeLowerBoxIcon.setTag(activityServer.getCubeColor());
            cubeUpperBoxIcon.setTag(activityServer.getCubeColorUpper());

            Glide.clear(customizePieceIcon);
            Glide.with(AddPart1Activity.this)
                    .load(urlIcon)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(customizePieceIcon);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                first_open = true;
            }
        });

        dialog.show();
    }

    private void register() {

        List<Integer> date;
        List<Integer> repeat;
        List<Double> latLng;
        boolean dateStartEmpty = false, dateEndEmpty = false, timeStartEmpty = false, timeEndEmpty = false;
        String title = titleEditText.getText().toString();

        date = getDateFromView();
        repeat = getRepeatFromView();
        location = getLocationFromView();
        latLng = getLatLngFromView();

        if (date.get(0) == -1) {
            dateStartEmpty = true;
        } else {
            dateStartEmpty = false;
        }

        if (date.get(3) == -1) {
            dateEndEmpty = true;
            date.set(3, date.get(0));
            date.set(4, date.get(1));
            date.set(5, date.get(2));
        } else {
            dateEndEmpty = false;
        }

        if (date.get(6) == -1) {
            timeStartEmpty = true;
            date.set(6, 0);
            date.set(7, 0);
        } else {
            timeStartEmpty = false;
        }

        if (date.get(8) == -1) {
            timeEndEmpty = true;
            date.set(8, 59);
            date.set(9, 23);
        } else {
            timeEndEmpty = false;
        }

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();

        calendarStart.set(date.get(2), date.get(1), date.get(0));
        calendarEnd.set(date.get(5), date.get(4), date.get(3));
        boolean validateDate = validateDateTime(calendarStart, calendarEnd);

        calendarStart.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
        calendarEnd.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));
        boolean validateTime = validateDateTime(calendarStart, calendarEnd);

        int err = 0;
        if (!validateFields(title)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_title_required, Toast.LENGTH_LONG).show();
        } else if (dateStartEmpty) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_start_required, Toast.LENGTH_LONG).show();
        } else if (!validateDate) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_date_end_before_start, Toast.LENGTH_LONG).show();
        } else if (!validateTime) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_time_end_before_start, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) != 0 && repeat.get(1) <= 0)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_required, Toast.LENGTH_LONG).show();
        } else if ((repeat.get(0) == 1 && repeat.get(1) > 365) || (repeat.get(0) == 2 && repeat.get(1) > 53) || (repeat.get(0) == 3 && repeat.get(1) > 12)) {
            err++;
            Toast.makeText(getApplicationContext(), R.string.validation_field_repetitions_min_max, Toast.LENGTH_LONG).show();
        } else if (!isActivityReadyRegister(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0))) {
            err++;
            Toast.makeText(getApplicationContext(), getErrorMessage(date.get(2), date.get(1), date.get(0), date.get(5), date.get(4), date.get(3), repeat.get(0)), Toast.LENGTH_LONG).show();
        } else {

        }

        if (err == 0) {

            int repeat_type = repeat.get(0);
            int repeat_qty = repeat.get(1);
            List<Integer> day_list_start = new ArrayList<>();
            List<Integer> month_list_start = new ArrayList<>();
            List<Integer> year_list_start = new ArrayList<>();
            List<Integer> day_list_end = new ArrayList<>();
            List<Integer> month_list_end = new ArrayList<>();
            List<Integer> year_list_end = new ArrayList<>();
            List<Long> date_time_list_start = new ArrayList<>();
            List<Long> date_time_list_end = new ArrayList<>();

            if (repeat_type > 0) {
                int repeat_adder = getRepeatAdder(repeat_type);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);

                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.clear(Calendar.MINUTE);
                cal2.clear(Calendar.SECOND);
                cal2.clear(Calendar.MILLISECOND);

                cal.set(date.get(2), date.get(1), date.get(0), date.get(7), date.get(6));
                cal2.set(date.get(5), date.get(4), date.get(3), date.get(9), date.get(8));

                for (int i = 0; i < repeat_qty; i++) {
                    day_list_start.add(cal.get(Calendar.DAY_OF_MONTH));
                    month_list_start.add(cal.get(Calendar.MONTH) + 1);
                    year_list_start.add(cal.get(Calendar.YEAR));
                    day_list_end.add(cal2.get(Calendar.DAY_OF_MONTH));
                    month_list_end.add(cal2.get(Calendar.MONTH) + 1);
                    year_list_end.add(cal2.get(Calendar.YEAR));

                    date_time_list_start.add(cal.getTimeInMillis());
                    date_time_list_end.add(cal2.getTimeInMillis());

                    if (repeat_type == Constants.MONTHLY) {
                        cal.add(Calendar.MONTH, 1);
                        cal2.add(Calendar.MONTH, 1);
                    } else {
                        cal.add(Calendar.DAY_OF_WEEK, repeat_adder);
                        cal2.add(Calendar.DAY_OF_WEEK, repeat_adder);
                    }
                }

            }

            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
            String creator = mSharedPreferences.getString(Constants.EMAIL, "");

            activityServer.setCreator(creator);
            activityServer.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            activityServer.setTitle(title);
            activityServer.setWhatsappGroupLink("");

            d = date.get(0);
            m = date.get(1);
            y = date.get(2);

            activityServer.setDateStartEmpty(dateStartEmpty);
            activityServer.setDateEndEmpty(dateEndEmpty);
            activityServer.setTimeStartEmpty(timeStartEmpty);
            activityServer.setTimeEndEmpty(timeEndEmpty);
            activityServer.setDayStart(date.get(0));
            activityServer.setMonthStart(date.get(1) + 1);
            activityServer.setYearStart(date.get(2));
            activityServer.setDayEnd(date.get(3));
            activityServer.setMonthEnd(date.get(4) + 1);
            activityServer.setYearEnd(date.get(5));
            activityServer.setMinuteStart(date.get(6));
            activityServer.setHourStart(date.get(7));
            activityServer.setMinuteEnd(date.get(8));
            activityServer.setHourEnd(date.get(9));

            activityServer.setDateTimeCreation(Calendar.getInstance().getTimeInMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.set(activityServer.getYearStart(), activityServer.getMonthStart() - 1, activityServer.getDayStart(), activityServer.getHourStart(), activityServer.getMinuteStart());
            activityServer.setDateTimeStart(calendar.getTimeInMillis());

            calendar.set(activityServer.getYearEnd(), activityServer.getMonthEnd() - 1, activityServer.getDayEnd(), activityServer.getHourEnd(), activityServer.getMinuteEnd());
            activityServer.setDateTimeEnd(calendar.getTimeInMillis());

            activityServer.setRepeatType(repeat.get(0));
            activityServer.setRepeatQty(repeat.get(1));
            activityServer.setDayListStart(day_list_start);
            activityServer.setMonthListStart(month_list_start);
            activityServer.setYearListStart(year_list_start);
            activityServer.setDayListEnd(day_list_end);
            activityServer.setMonthListEnd(month_list_end);
            activityServer.setYearListEnd(year_list_end);

            activityServer.setDateTimeListStart(date_time_list_start);
            activityServer.setDateTimeListEnd(date_time_list_end);

            activityServer.setLat(latLng.get(0));
            activityServer.setLng(latLng.get(1));

            Intent register = new Intent(AddPart1Activity.this, AddPart2Activity.class);

            ActivityWrapper wrapper = new ActivityWrapper(activityServer);
            register.putExtra("act_wrapper", wrapper);
            startActivity(register);
            overridePendingTransition(R.anim.push_left_enter, R.anim.push_left_exit);

        } else {
            error = true;
        }
    }

    private int getRepeatAdder(int type) {
        switch (type) {
            case Constants.DAYLY:
                return 1;
            case Constants.WEEKLY:
                return 7;
            default:
                return 0;
        }
    }

    private boolean isActivityReadyRegister(int y1, int m1, int d1, int y2, int m2, int d2, int period) {
        LocalDate start = new LocalDate(y1, m1 + 1, d1);
        LocalDate end = new LocalDate(y2, m2 + 1, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        if (timePeriod.getDays() > 15)
            return false;

        switch (period) {
            case 1:
                if (timePeriod.getDays() > 0)
                    return false;
            case 2:
                if (timePeriod.getDays() > 6)
                    return false;
            case 3:
                if (timePeriod.getDays() > 29)
                    return false;
            default:
                return true;
        }
    }

    private String getErrorMessage(int y1, int m1, int d1, int y2, int m2, int d2, int period) {
        LocalDate start = new LocalDate(y1, m1 + 1, d1);
        LocalDate end = new LocalDate(y2, m2 + 1, d2);
        Period timePeriod = new Period(start, end, PeriodType.days());
        if (timePeriod.getDays() > 15)
            return getResources().getString(R.string.validation_field_act_max_lenght_days);

        switch (period) {
            case 1:
                return getResources().getString(R.string.validation_field_act_max_lenght_days_daily);
            case 2:
                return getResources().getString(R.string.validation_field_act_max_lenght_days_weekly);
            case 3:
                return getResources().getString(R.string.validation_field_act_max_lenght_days_monthly);
            default:
                return "";
        }
    }


    public void setProgress(boolean progress) {
        if (progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void createDialogLocation(String adr) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        text1.setText(this.getResources().getString(R.string.popup_message_naming_activity_local_title));
        text2.setText(this.getResources().getString(R.string.popup_message_naming_activity_local_text));
        buttonText1.setText(this.getResources().getString(R.string.cancel));
        buttonText2.setText(this.getResources().getString(R.string.customize));
        editText.setText(adr);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
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
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancelDialogLocation" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmDialogLocation" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                location = editText.getText().toString();
                if (!location.matches(""))
                    locationText.setText(location);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onClick(View v) {
        viewClicked = v;
        if (v == pieceBox) {
            if (iconList.size() > 0)
                createDialogSelectIcon();
        } else if (v == confirmationButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirmationButtonEdit" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            register();
        } else if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            onBackPressed();
        } else if (v == clearDateStart || v == clearDateEnd || v == clearTimeStart || v == clearTimeEnd) {

            if (v == clearDateStart) {
                day_start = -1;
                month_start = -1;
                year_start = -1;
                dateStart.setText("");
                clearDateStart.setVisibility(View.GONE);
            } else if (v == clearDateEnd) {
                day_end = -1;
                month_end = -1;
                year_end = -1;
                dateEnd.setText("");
                clearDateEnd.setVisibility(View.GONE);
            } else if (v == clearTimeStart) {
                hour_start = -1;
                minutes_start = -1;
                timeStart.setText("");
                clearTimeStart.setVisibility(View.GONE);
            } else if (v == clearTimeEnd) {
                minutes_end = -1;
                hour_end = -1;
                timeEnd.setText("");
                clearTimeEnd.setVisibility(View.GONE);
            }

        } else if (v == repeatAdd) {
            findViewById(R.id.progressRepeatAdd).setVisibility(View.VISIBLE);
            repeatAddIcon.setVisibility(View.INVISIBLE);
            repeatAddText.setVisibility(View.INVISIBLE);

            repeatAdd.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progressRepeatAdd).setVisibility(View.GONE);
                    repeatAdd.setVisibility(View.GONE);
                    repeatBox.setVisibility(View.VISIBLE);
                }
            }, 700);

        } else if ((v == locationBox || v == locationBoxAdd) && !locationNotWorking) {
            setProgress(true);

            if (v == locationBoxAdd) {
                locationBoxAdd.setVisibility(View.GONE);
                locationBox.setVisibility(View.VISIBLE);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mapText" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (lat != -500) {
                LatLng latLng = new LatLng(lat, lng);
                LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                builder = new PlacePicker.IntentBuilder().setLatLngBounds(latLngBounds);

                try {
                    placePicker = builder.build(this);
                } catch (Exception e) {
                }
            }

            startActivityForResult(placePicker, PLACE_PICKER_REQUEST);
        } else if (v == locationIcon2) {
            findViewById(R.id.progressLocation).setVisibility(View.VISIBLE);
            locationText.setVisibility(View.INVISIBLE);
            locationIcon.setVisibility(View.INVISIBLE);
            locationIcon2.setVisibility(View.INVISIBLE);

            locationIcon2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    locationText.setText("");
                    lat = -500;
                    lng = -500;
                    locationBoxAdd.setVisibility(View.VISIBLE);
                    locationText.setVisibility(View.VISIBLE);
                    locationIcon.setVisibility(View.VISIBLE);
                    locationIcon2.setVisibility(View.VISIBLE);
                    locationBox.setVisibility(View.GONE);
                    findViewById(R.id.progressLocation).setVisibility(View.GONE);
                }
            }, 700);

        } else if (v == timeStart) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (hour_start != -1)
                tpd.setStartTime(hour_start, minutes_start);

            tpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");

        } else if (v == timeEnd) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );

            if (hour_end != -1)
                tpd.setStartTime(hour_end, minutes_end);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "timeEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            tpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            tpd.show(getFragmentManager(), "Timepickerdialog");
        } else if (v == dateStart) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    year_start,
                    month_start,
                    day_start
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateStart" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        } else if (v == dateEnd) {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    year_end,
                    month_end,
                    day_end
            );

            dpd.setMinDate(now);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "dateEnd" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            dpd.setAccentColor(ContextCompat.getColor(this, R.color.deep_purple_400));
            dpd.show(getFragmentManager(), "Datepickerdialog2");
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mBackButton) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBackButton.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == repeatAdd) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                repeatAddText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                repeatAddIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                repeatAddText.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                repeatAddIcon.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == locationBoxAdd) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationTextAdd.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_400));
                locationIconAdd.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_400));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationTextAdd.setTextColor(ContextCompat.getColor(this, R.color.deep_purple_200));
                locationIconAdd.setColorFilter(ContextCompat.getColor(this, R.color.deep_purple_200));
            }
        } else if (view == locationBox) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationText.setTextColor(ContextCompat.getColor(this, R.color.grey_600));
                locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationText.setTextColor(ContextCompat.getColor(this, R.color.grey_400));
                locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        } else if (view == locationIcon2) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                locationIcon2.setColorFilter(ContextCompat.getColor(this, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                locationIcon2.setColorFilter(ContextCompat.getColor(this, R.color.grey_400));
            }
        }

        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == locationBox && lat != -500) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "locationText" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            createDialogLocation(locationText.getText().toString());
        }
        return true;
    }

}
