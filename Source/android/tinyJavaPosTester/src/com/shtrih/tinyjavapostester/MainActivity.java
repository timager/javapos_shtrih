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
import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.tinyjavapostester.databinding.ActivityMainBinding;
import com.shtrih.tinyjavapostester.network.ConnectToBluetoothDeviceTask;
import com.shtrih.tinyjavapostester.search.bluetooth.DeviceListActivity;
import com.shtrih.tinyjavapostester.search.tcp.TcpDeviceSearchActivity;
import com.shtrih.util.SysUtils;

import org.slf4j.LoggerFactory;

import jpos.JposConst;
import jpos.JposException;


public class MainActivity extends AppCompatActivity {

    public static final String PROTOCOL = "0";
    public static final String TIMEOUT = "3000";
    private org.slf4j.Logger log = LoggerFactory.getLogger(MainActivity.class);
    private ShtrihFiscalPrinter printer = null;


    private AppCompatCheckBox chbFastConnect;
    private AppCompatCheckBox chbScocFirmwareUpdate;

    private MainViewModel model;
    private EditText nbTextLinesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        model = ViewModelProviders.of(this).get(MainViewModel.class);

        LogbackConfig.configure(SysUtils.getFilesPath());

        printer = model.getPrinter();

        binding.setVm(model);
        binding.setActivity(this);

        final SharedPreferences pref = this.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);

        nbTextLinesCount = findViewById(R.id.nbTextLinesCount);
        chbFastConnect = findViewById(R.id.chbFastConnect);
        restoreAndSaveChangesTo(chbFastConnect, pref, "FastConnect", true);

        chbScocFirmwareUpdate = findViewById(R.id.chbScocFirmwareUpdate);
        restoreAndSaveChangesTo(chbScocFirmwareUpdate, pref, "ScocFirmwareUpdate", true);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
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
                            printer,
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_connect_ptk) {
            Intent i = new Intent(this, DeviceListActivity.class);
            startActivityForResult(i, DeviceListActivity.REQUEST_CONNECT_BT_DEVICE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void printText(View v) {

        final int lines = Integer.parseInt(nbTextLinesCount.getText().toString());

        new PrintTextTask(this, lines).execute();
    }

    private class PrintTextTask extends AsyncTask<Void, Void, String> {

        private final Activity parent;
        private final int lines;

        private long startedAt;
        private long doneAt;
        private ProgressDialog dialog;

        public PrintTextTask(Activity parent, int lines) {
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
                printer.resetPrinter();

                boolean isCashCore = isCashCore(printer);

                startedAt = System.currentTimeMillis();

                if (isCashCore) {
                    BeginNonFiscalDocument cmd = new BeginNonFiscalDocument();
                    cmd.setPassword(printer.getUsrPassword());
                    printer.executeCommand(cmd);
                }

                String text = "Мой дядя самых честных правил";

                FontNumber font = new FontNumber(1);

                for (int i = 0; i < lines; i++) {
                    printer.printText(text, font);
                }

                if (isCashCore) {
                    CloseNonFiscal cmd = new CloseNonFiscal();
                    cmd.setPassword(printer.getUsrPassword());
                    printer.executeCommand(cmd);
                }

                return null;

            } catch (Exception e) {
                log.error("Text printing failed", e);
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

    public void disconnect(View v) {
        new DisconnectTask(this).execute();
    }

    private class DisconnectTask extends AsyncTask<Void, Void, String> {

        private final Activity parent;

        private long startedAt;
        private long doneAt;
        private ProgressDialog dialog;

        public DisconnectTask(Activity parent) {
            this.parent = parent;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(parent, "Disconnecting", "Please wait...", true);
        }

        @Override
        protected String doInBackground(Void... params) {

            startedAt = System.currentTimeMillis();

            try {
                if (printer.getState() != JposConst.JPOS_S_CLOSED) {
                    printer.close();
                }

                model.ScocUpdaterStatus.set("");

                return null;

            } catch (Exception e) {
                log.error("Disconnect failed", e);
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

    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        log.debug(message);
    }

}