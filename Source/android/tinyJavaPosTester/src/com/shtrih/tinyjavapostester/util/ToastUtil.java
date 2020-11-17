package com.shtrih.tinyjavapostester.util;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class ToastUtil {
    private static Application app;
    private static Handler mMainHandler = new Handler(Looper.getMainLooper());

    public static void init(Application application) {
        app = application;
    }

    public static void showMessage(String message) {
        mMainHandler.post(() -> {
            Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
        });
    }

    public static void showMessage(@StringRes Integer resId) {
        showMessage(app.getString(resId));
    }
}
