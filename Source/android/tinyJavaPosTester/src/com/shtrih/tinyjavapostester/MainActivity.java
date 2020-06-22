package com.shtrih.tinyjavapostester;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.shtrih.fiscalprinter.FontNumber;
import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.command.BeginNonFiscalDocument;
import com.shtrih.fiscalprinter.command.CloseNonFiscal;
import com.shtrih.fiscalprinter.command.DeviceMetrics;
import com.shtrih.fiscalprinter.command.FSReadExpDate;
import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.tinyjavapostester.databinding.ActivityMainBinding;
import com.shtrih.tinyjavapostester.network.ConnectToBluetoothDeviceTask;
import com.shtrih.tinyjavapostester.search.bluetooth.DeviceListActivity;
import com.shtrih.tinyjavapostester.search.tcp.TcpDeviceSearchActivity;

import jpos.JposException;


public class MainActivity extends AppCompatActivity {

    public static final String PROTOCOL = "0";
    public static final String TIMEOUT = "3000";

    private AppCompatCheckBox chbFastConnect;
    private AppCompatCheckBox chbScocFirmwareUpdate;

    private MainViewModel model;
    private EditText nbTextLinesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = ViewModelProviders.of(this).get(MainViewModel.class);
        binding.setVm(model);
        binding.setActivity(this);
        final SharedPreferences pref = this.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
        nbTextLinesCount = findViewById(R.id.nbTextLinesCount);
        chbFastConnect = findViewById(R.id.chbFastConnect);
        restoreAndSaveChangesTo(chbFastConnect, pref, "FastConnect", true);
        chbScocFirmwareUpdate = findViewById(R.id.chbScocFirmwareUpdate);
        restoreAndSaveChangesTo(chbScocFirmwareUpdate, pref, "ScocFirmwareUpdate", true);
    }


    private void restoreAndSaveChangesTo(final CompoundButton edit, final SharedPreferences pref, final String key, final boolean defaultValue) {
        edit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(key, compoundButton.isChecked());
                editor.apply();
            }
        });

        boolean savedValue = pref.getBoolean(key, defaultValue);
        edit.setChecked(savedValue);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case DeviceListActivity.REQUEST_CONNECT_BT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {

                    Bundle extras = data.getExtras();

                    if (extras == null)
                        return;

                    String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    if (address == null)
                        return;

                    new ConnectToBluetoothDeviceTask(
                            this,
                            address,
                            createFirmwareUpdateObserver(),
                            TIMEOUT,
                            chbFastConnect.isChecked(),
                            chbScocFirmwareUpdate.isChecked(),
                            model).execute();
                }
            case TcpDeviceSearchActivity.REQUEST_SEARCH_TCP_DEVICE:
                if (resultCode == Activity.RESULT_OK) {

                    Bundle extras = data.getExtras();

                    if (extras == null)
                        return;

                    String address = extras.getString("Address");

                    if (address == null)
                        return;

                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private FirmwareUpdateObserver createFirmwareUpdateObserver() {
        return new FirmwareUpdaterObserverImpl(model);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connect_ptk) {
            Intent i = new Intent(this, DeviceListActivity.class);
            startActivityForResult(i, DeviceListActivity.REQUEST_CONNECT_BT_DEVICE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void test(View view) {
        new TestTask(this).execute();
    }

    private class TestTask extends AsyncTask<Void, Void, String> {

        private final Activity parent;
        private long startedAt;
        private long doneAt;
        private ProgressDialog dialog;

        public TestTask(Activity parent) {
            this.parent = parent;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(parent, "test", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                ShtrihFiscalPrinter printer = model.getPrinter();
                printer.resetPrinter();
                startedAt = System.currentTimeMillis();
                FSReadExpDate fsCmd = new FSReadExpDate();
                fsCmd.setSysPassword(printer.getUsrPassword());
                printer.executeCommand(fsCmd);
                showMessage(fsCmd.getDate().toString());
                return null;

            } catch (Exception e) {
                showMessage("Text printing failed");
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
                showMessage("Success " + (doneAt - startedAt) + " ms");
            else
                showMessage(result);
        }
    }

    public void printText(View v) {
        String lines = nbTextLinesCount.getText().toString();
        new PrintTextTask(this, lines).execute();
    }


    private class PrintTextTask extends AsyncTask<Void, Void, String> {

        private final Activity parent;
        private final String lines;

        private long startedAt;
        private long doneAt;
        private ProgressDialog dialog;

        public PrintTextTask(Activity parent, String lines) {
            this.parent = parent;
            this.lines = lines;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(parent, "Printing text", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                ShtrihFiscalPrinter printer = model.getPrinter();
                printer.resetPrinter();

                boolean isCashCore = isCashCore(printer);

                startedAt = System.currentTimeMillis();

                if (isCashCore) {
                    BeginNonFiscalDocument cmd = new BeginNonFiscalDocument();
                    cmd.setPassword(printer.getUsrPassword());
                    printer.executeCommand(cmd);
                }
                FontNumber font = new FontNumber(1);
                printer.printText(lines, font);

                if (isCashCore) {
                    CloseNonFiscal cmd = new CloseNonFiscal();
                    cmd.setPassword(printer.getUsrPassword());
                    printer.executeCommand(cmd);
                }

                return null;

            } catch (Exception e) {
                showMessage("Text printing failed");
                return e.getMessage();
            } finally {
                doneAt = System.currentTimeMillis();
            }
        }

        private boolean isCashCore(ShtrihFiscalPrinter printer) throws JposException {
            DeviceMetrics metrics = printer.readDeviceMetrics();
            return metrics.getModel() == 45; // КЯ
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            dialog.dismiss();

            if (result == null)
                showMessage("Success " + (doneAt - startedAt) + " ms");
            else
                showMessage(result);
        }
    }

    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}