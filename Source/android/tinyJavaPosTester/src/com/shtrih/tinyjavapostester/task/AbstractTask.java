package com.shtrih.tinyjavapostester.task;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.task.message.Message;

import jpos.JposException;

public abstract class AbstractTask extends AsyncTask<Void, Void, String> {

    private final MainActivity parent;

    private long startedAt;
    private long doneAt;
    private ProgressDialog dialog;

    private final MainViewModel model;

    public AbstractTask(MainActivity parent, MainViewModel model) {
        this.parent = parent;
        this.model = model;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Message message = makeMessage();
        parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        dialog = ProgressDialog.show(parent, message.getTitle(), message.getText(), true);
    }

    abstract Message makeMessage();

    @Override
    protected String doInBackground(Void... params) {
        try {
            model.getPrinter().resetPrinter();
            startedAt = System.currentTimeMillis();
            exec(model.getPrinter());
            return null;
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            doneAt = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        dialog.dismiss();

        if (result == null)
            parent.showMessage("Success " + (doneAt - startedAt) + " ms");
        else
            parent.showMessage(result);
        parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    protected abstract void exec(ShtrihFiscalPrinter printer) throws Exception;
}