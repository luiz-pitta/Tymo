package io.development.tymo.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class Validation {

    public static boolean validateFields(String name){

        if (TextUtils.isEmpty(name) || name.matches("")) {

            return false;

        } else {

            return true;
        }
    }

    public static boolean validateEmail(String string) {

        if (TextUtils.isEmpty(string) || !Patterns.EMAIL_ADDRESS.matcher(string).matches()) {

            return false;

        } else {

            return  true;
        }
    }

    public static boolean validatePasswordSize(String text){

        if (text.length() < 8) {

            return false;

        } else {

            return true;
        }
    }
}
