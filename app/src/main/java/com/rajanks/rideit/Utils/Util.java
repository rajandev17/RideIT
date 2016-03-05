package com.rajanks.rideit.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.net.InetAddress;

/**
 * Created by rajan ks on 04-03-2016.
 */
public class Util {

    //<editor-fold desc="Constants">
    public final static String ApplicationTag = "RideIt";
    public final static String FontsRootPath = "fonts/";
    //</editor-fold>

    //<editor-fold desc="Properties">
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;
    public static ProgressDialog progressBar;
    public static boolean isShowing = false;
    //</editor-fold>

    //<editor-fold desc="SharedPreferences - Generic">
    public static void saveValueWithKey(Activity context, String key, String value) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveValueWithKey(Activity context, String key, int value) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void saveValueWithKey(Activity context, String key, boolean value) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void saveValueWithKey(Activity context, String key, Float value) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void clearCacheForPref(Activity context) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public static int getIntValueForKey(Activity context, String key, int defaultValue) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static String getStringValueForKey(Activity context, String key, String defaultValue) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static boolean getBoolValueForKey(Activity context, String key, boolean defaultValue) {
        sharedPreferences = context.getSharedPreferences(ApplicationTag, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    //</editor-fold>

    //<editor-fold desc="Toast - Long and Short Durations">
    public static void showLongToast(Activity context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Activity context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    //</editor-fold>

    //<editor-fold desc="Progress Dialog with Message">
    public static void showProgressDialog(Activity context, String message) {
        progressBar = new ProgressDialog(context);
        isShowing = true;
        progressBar.setCancelable(false);
        progressBar.setMessage(message);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    public static void hideProgressBar(Activity context) {
        if (isShowing) {
            progressBar.dismiss();
            isShowing = false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Internet Availability Check - Need Manifest Permissions">
    //Manifest Permissions to be added
    // <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    public static boolean isInternetAccessible() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Fonts - Typeface">
    public static Typeface getFontWithName(Activity context, String fontName) {
        String assetPath = FontsRootPath + fontName + ".ttf";
        return Typeface.createFromAsset(context.getAssets(), assetPath);
    }
    //</editor-fold>

    //<editor-fold desc="Verifying and Enabling GPS">
    public static void checkAndEnableGPS(Activity context) {
        try {
            int off = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (off == 0) {
                Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(onGPS);
            }
        } catch (Settings.SettingNotFoundException snfe) {

        }
    }
    //</editor-fold>
}
