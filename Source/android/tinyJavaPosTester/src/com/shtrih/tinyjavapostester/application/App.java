package com.shtrih.tinyjavapostester.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.shtrih.tinyjavapostester.shar_pref.AppConst;
import com.shtrih.tinyjavapostester.shar_pref.SharedPreferenceKey;
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

    public static void setCashierName(String cashierName) {
        app.getApplicationSharPref().edit()
                .putString(SharedPreferenceKey.CASHIER_NAME, cashierName)
                .apply();
    }

    public static String getCashierName() {
        return app.getApplicationSharPref().getString(SharedPreferenceKey.CASHIER_NAME, AppConst.DEFAULT_CASHIER_NAME);
    }
}
