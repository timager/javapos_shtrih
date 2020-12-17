package com.shtrih.tinyjavapostester.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class AlertUtil {
    public static void showAlertOk(Activity activity, String title, String description, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("ОК", (dialog, which) -> {
                    dialog.dismiss();
                    okListener.onClick(dialog, which);
                })
                .create().show();
    }
}
