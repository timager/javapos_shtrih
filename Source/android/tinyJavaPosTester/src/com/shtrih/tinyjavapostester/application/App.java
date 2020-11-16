package com.shtrih.tinyjavapostester.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.shtrih.tinyjavapostester.util.ToastUtil;

public class App extends Application {
    private final String SHAR_PREF_KEY = "common_shar_pref";

    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        ToastUtil.init(this);
    }

    public SharedPreferences getApplicationSharPref() {
        return getSharedPreferences(SHAR_PREF_KEY, MODE_PRIVATE) ;
    }
}
