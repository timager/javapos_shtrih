package com.shtrih.tinyjavapostester.util;

import android.app.Application;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class ToastUtil {
    private static Application app;

    public static void init(Application application) {
        app = application;
    }

    public static void showMessage(String message) {
        Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
    }

    public static void showMessage(@StringRes Integer resId) {
        showMessage(app.getString(resId));
    }
}
