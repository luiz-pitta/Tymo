package io.development.tymo.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UpdateButton {
    private boolean selected;
    TextView text;
    ImageView button;
    View view;

    public UpdateButton(boolean selected, TextView text, ImageView button, View view) {
        this.selected = selected;
        this.text = text;
        this.button = button;
        this.view = view;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public boolean getSelected(){
        return this.selected;
    }

    public void updateButtonColor(Context context, int textColor, int buttonColor, int viewColor) {
        if(text != null)
            text.setTextColor(ContextCompat.getColor(context,textColor));

        if(button != null)
            button.setColorFilter(ContextCompat.getColor(context,buttonColor));

        if(view != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(ContextCompat.getDrawable(context, viewColor));
            } else {
                view.setBackgroundDrawable(ContextCompat.getDrawable(context, viewColor));
            }
        }
    }
}
