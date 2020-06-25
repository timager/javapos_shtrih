package com.shtrih.tinyjavapostester.cashboxtasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.shtrih.tinyjavapostester.MainActivity;
import com.shtrih.tinyjavapostester.MainViewModel;

public class PrintXReportTaskKKM extends AsyncTask<Void, Void, String> {
    private MainActivity parent;
    private long StartedAt;
    private long doneAt;
    private ProgressDialog dialog;
    private MainViewModel model;

    public PrintXReportTaskKKM(MainActivity parent, MainViewModel model) {
        this.parent = parent;
        this.model = model;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = ProgressDialog.show(parent, "Печать X-отчета", "Пожалуйста, подождите", true);
    }

    @Override
    protected String doInBackground(Void... params) {

        StartedAt = System.currentTimeMillis();

        try {
            model.getPrinter().resetPrinter();
            model.getPrinter().printXReport();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            doneAt = System.currentTimeMillis();
        }
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        dialog.dismiss();
        if (result == null)
            parent.showMessage("Успешно выполнено: " + (doneAt - StartedAt) + " ms");
        else
            parent.showMessage(result);
    }
}