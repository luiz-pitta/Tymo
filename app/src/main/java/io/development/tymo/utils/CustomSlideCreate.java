package io.development.tymo.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import agency.tango.materialintroscreen.SlideFragment;
import io.development.tymo.R;

public class CustomSlideCreate extends SlideFragment {
    private CheckBox checkBox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_tutorial_create, container, false);
        return view;
    }

    @Override
    public int backgroundColor() {
        return R.color.white;
    }

    @Override
    public int buttonsColor() {
        return R.color.grey_400;
    }

    @Override
    public boolean canMoveFurther() {
        return true;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.error);
    }
}