package io.development.tymo.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;


public class UpdateButtonController {

    private List<UpdateButton> mList;
    private boolean multiple;
    private Context context;


    public UpdateButtonController(Context context) {
        mList = new ArrayList<>();
        this.multiple = false;
        this.context = context;

    }

    public void attach(boolean selected, TextView text, ImageView button, View view){
        mList.add(new UpdateButton(selected, text, button, view));
    }

    //In case you want to select multiple
    public void setMultiple(boolean multiple){
        this.multiple = multiple;
    }

    public void unselectAll() {
        int i;
        for (i = 0; i < mList.size(); i++) {
            mList.get(i).updateButtonColor(context, R.color.grey_400, R.color.grey_400, R.drawable.bg_shape_oval_grey_400_corners);
            mList.get(i).setSelected(false);
        }
    }

    public List<Boolean> getSelected() {
        int i;
        List<Boolean> list = new ArrayList<>();
        for (i = 0; i < mList.size(); i++) {
            if(mList.get(i).getSelected())
                list.add(true);
            else
                list.add(false);
        }
        return list;
    }

    public boolean checkAllUnselected() {
        int i;
        boolean all = true;
        for (i = 0; i < mList.size(); i++) {
            if(mList.get(i).getSelected())
                all = false;
        }
        return all;
    }

    public void updateAll(int position, int textColor, int buttonColor, int viewColor) {
        int i;

        if(multiple){
            if (!mList.get(position).getSelected()) {
                mList.get(position).updateButtonColor(context, textColor, buttonColor, viewColor);
                mList.get(position).setSelected(true);
            } else {
                mList.get(position).updateButtonColor(context, R.color.grey_400, R.color.grey_400, R.drawable.bg_shape_oval_grey_400_corners);
                mList.get(position).setSelected(false);
            }
        }else {
            for (i = 0; i < mList.size(); i++) {
                if (i == position) {
                    mList.get(i).updateButtonColor(context, textColor, buttonColor, viewColor);
                    mList.get(i).setSelected(true);
                } else {
                    mList.get(i).updateButtonColor(context, R.color.grey_400, R.color.grey_400, R.drawable.bg_shape_oval_grey_400_corners);
                    mList.get(i).setSelected(false);
                }
            }
        }
    }
}
