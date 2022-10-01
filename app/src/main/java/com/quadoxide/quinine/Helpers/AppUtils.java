package com.quadoxide.quinine.Helpers;

import android.content.Context;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.quadoxide.quinine.R;

public class AppUtils {
    //Method to show toast
    public static void showToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    //Method to check if string is neither null nor empty
    public static boolean isStringValid(String text){
        return text!=null && !text.isEmpty();
    }
}
