package com.shtrih.tinyjavapostester.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.shtrih.tinyjavapostester.shar_pref.SharedPreferenceKey;

public class App extends Application {
    private final String SHAR_PREF_KEY = "common_shar_pref";

    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public SharedPreferences getApplicationSharPref() {
        return getSharedPreferences(SHAR_PREF_KEY, MODE_PRIVATE) ;
    }

    public static void saveCashierName(String name) {
        app.getApplicationSharPref().edit().putString(SharedPreferenceKey.CASHIER_NAME, name).apply();
    }

    public static String getCashierName() {
        return app.getApplicationSharPref().getString(SharedPreferenceKey.CASHIER_NAME, "");
    }
}
