package com.shtrih.tinyjavapostester.task;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.tinyjavapostester.JposConfig;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.MainViewModel;

import java.util.HashMap;
import jpos.JposConst;
import jpos.JposException;

import static com.shtrih.tinyjavapostester.activity.MainActivity.PROTOCOL;

public class ConnectToBluetoothDeviceTask extends AsyncTask<Void, Void, String> {

    private final AbstractActivity parent;
    private final String address;
    private final FirmwareUpdateObserver observer;
    private final String timeout;
    private final boolean fastConnect;
    private final boolean scocFirmwareAutoupdate;
    private final MainViewModel model;

    private long startedAt;
    private long doneAt;

    private ProgressDialog dialog;

    public ConnectToBluetoothDeviceTask(AbstractActivity parent, String address, FirmwareUpdateObserver observer, String timeout, boolean fastConnect, boolean scocFirmwareAutoupdate, MainViewModel model) {
        this.parent = parent;

        this.address = address;
        this.observer = observer;
        this.timeout = timeout;
        this.fastConnect = fastConnect;
        this.scocFirmwareAutoupdate = scocFirmwareAutoupdate;
        this.model = model;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = ProgressDialog.show(parent, "Подключение к устройству", "Пожалуйста, подождите", true);
            }
        });    }

    @Override
    protected String doInBackground(Void... params) {

        startedAt = System.currentTimeMillis();
        try {
            ShtrihFiscalPrinter printer = model.getPrinter();
            HashMap<String, String> props = new HashMap<>();
            props.put("portName", address);
            props.put("portType", "3");
            props.put("portClass", "com.shtrih.fiscalprinter.port.BluetoothPort");
            props.put("protocolType", PROTOCOL);
            props.put("fastConnect", fastConnect ? "1" : "0");
            props.put("textReportEnabled", "1");
            props.put("headerMode", "1");
            props.put("capScocUpdateFirmware", scocFirmwareAutoupdate ? "1" : "0");
            props.put("byteTimeout", timeout);

            JposConfig.configure("ShtrihFptr", parent.getApplicationContext(), props);
            if (printer.getState() != JposConst.JPOS_S_CLOSED) {
                printer.close();
            }
            printer.open("ShtrihFptr");
            printer.claim(Integer.parseInt(timeout));
            printer.setDeviceEnabled(true);
            model.ScocUpdaterStatus.set("");
            printer.setParameter3(SmFptrConst.SMFPTR_DIO_PARAM_FIRMWARE_UPDATE_OBSERVER, observer);

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof JposException) {
                return "Ошибка подключения";
            } else {
                return e.getMessage();
            }
        } finally {
            doneAt = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
        if (result == null)
            parent.showMessage("Success " + (doneAt - startedAt) + " ms");
        else
            parent.showMessage(result);

        parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        parent.toConnect(address);

    }
}
