package com.shtrih.tinyjavapostester.task;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.command.DeviceMetrics;
import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.tinyjavapostester.JposConfig;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;

import java.util.HashMap;
import java.util.Map;

import jpos.FiscalPrinterConst;
import jpos.JposConst;

import static com.shtrih.tinyjavapostester.activity.MainActivity.PROTOCOL;

public class AutoConnectBluetoothDeviceTask extends AsyncTask<Void, Void, String> {

    private final AbstractActivity parent;
    private final String address;
    private final FirmwareUpdateObserver observer;
    private final String timeout;
    private final boolean fastConnect;
    private final boolean scocFirmwareAutoupdate;
    private final MainViewModel model;

    private long startedAt;
    private long doneAt;

    private String text;

    private ProgressDialog dialog;

    public AutoConnectBluetoothDeviceTask(AbstractActivity parent, String address, FirmwareUpdateObserver observer, String timeout, boolean fastConnect, boolean scocFirmwareAutoupdate, MainViewModel model) {
        this.parent = parent;

        this.address = address;
        this.observer = observer;
        this.timeout = timeout;
        this.fastConnect = fastConnect;
        this.scocFirmwareAutoupdate = scocFirmwareAutoupdate;
        this.model = model;
    }

    private int oldOrientation;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        oldOrientation = parent.getRequestedOrientation();
        parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        dialog = ProgressDialog.show(parent, "Connecting to device", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Void... params) {
        ShtrihFiscalPrinter printer = model.getPrinter();
        try {
            if (printer.getState() != JposConst.JPOS_S_CLOSED) {
                printer.close();
            }

            startedAt = System.currentTimeMillis();
            Map<String, String> props = new HashMap<>();
            props.put("portName", "SHTRIH");
            props.put("portType", "3");
            props.put("portClass", "com.shtrih.fiscalprinter.port.BluetoothPort");
            props.put("protocolType", PROTOCOL);
            props.put("fastConnect", fastConnect ? "1" : "0");
            props.put("capScocUpdateFirmware", scocFirmwareAutoupdate ? "1" : "0");
            props.put("byteTimeout", timeout);
            props.put("searchByPortEnabled", "1");

            JposConfig.configure("ShtrihFptr", parent.getApplicationContext(), props);
            printer.open("ShtrihFptr");
            printer.claim(Integer.parseInt(timeout));
            printer.setDeviceEnabled(true);
            model.ScocUpdaterStatus.set("");
            printer.setParameter3(SmFptrConst.SMFPTR_DIO_PARAM_FIRMWARE_UPDATE_OBSERVER, observer);
            doneAt = System.currentTimeMillis();
            String[] lines = new String[1];
            printer.getData(FiscalPrinterConst.FPTR_GD_PRINTER_ID, null, lines);
            String serialNumber = lines[0];
            DeviceMetrics deviceMetrics = printer.readDeviceMetrics();
            text = deviceMetrics.getDeviceName() + " " + serialNumber;
            return null;

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        dialog.dismiss();

        if (result == null)
            parent.showMessage(text + "\nSuccess " + (doneAt - startedAt) + " ms");
        else
            parent.showMessage(result);

        parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        parent.toConnect(address);
    }
}
