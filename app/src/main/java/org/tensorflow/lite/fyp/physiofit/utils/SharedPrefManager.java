package org.tensorflow.lite.fyp.physiofit.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    // properties
    private static final String BMI = "bmi";
    private static final String HEIGHT = "height";
    private static final String WEIGHT = "weight";


    private SharedPrefManager() {}

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    // setting preferences getters
    public static void setBMI(Context context, String soundQuality) {
        context.getSharedPreferences(context.getPackageName() , Context.MODE_PRIVATE).edit().putString(BMI , soundQuality).apply();
    }

    public static void setHEIGHT(Context context, String recording_Formate) {
        context.getSharedPreferences(context.getPackageName() , Context.MODE_PRIVATE).edit().putString(HEIGHT , recording_Formate).apply();
    }

    public static void setWEIGHT(Context context, String noise_Suppression) {
        context.getSharedPreferences(context.getPackageName() , Context.MODE_PRIVATE).edit().putString(WEIGHT , noise_Suppression).apply();
    }




    // setting preferences setters
    public static String getBMI(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(BMI , "22.2");
    }

    public static String getHEIGHT(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(HEIGHT , "170");
    }

    public static String getWEIGHT(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(WEIGHT , "65");
    }


}
