package io.development.tymo.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;

import io.development.tymo.R;

public class MonthYearPickerDialog extends DialogFragment {

    private static final int MAX_YEAR = 2099;
    private static final int MIN_YEAR = 1970;
    private DatePickerDialog.OnDateSetListener listener;
    private FirebaseAnalytics mFirebaseAnalytics;

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = null;

        Dialog  dialog = new Dialog(getActivity(), R.style.NewDialog);

        Calendar cal = Calendar.getInstance();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        customView = inflater.inflate(R.layout.dialog_month_picker, null);
        final NumberPicker monthPicker = (NumberPicker) customView.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = (NumberPicker) customView.findViewById(R.id.picker_year);
        TextView buttonText1 = (TextView)customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView)customView.findViewById(R.id.buttonText2);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(cal.get(Calendar.MONTH) + 1);
        monthPicker.setDisplayedValues( getActivity().getResources().getStringArray(R.array.array_months_short) );

        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);

        buttonText1.setText(getActivity().getString(R.string.cancel));
        buttonText2.setText(getActivity().getString(R.string.ok));

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthYearPickerDialog.this.getDialog().cancel();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cancel" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "confirm" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                dialog.dismiss();
            }
        });

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }
}
