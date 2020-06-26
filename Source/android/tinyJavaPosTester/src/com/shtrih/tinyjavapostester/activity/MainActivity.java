package com.shtrih.tinyjavapostester.activity;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.tinyjavapostester.FirmwareUpdaterObserverImpl;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.R;
import com.shtrih.tinyjavapostester.task.OpenDayTask;
import com.shtrih.tinyjavapostester.task.PrintDuplicateReceiptTask;
import com.shtrih.tinyjavapostester.task.PrintReceiptTask;
import com.shtrih.tinyjavapostester.task.PrintXReportTaskKKM;
import com.shtrih.tinyjavapostester.task.PrintZReportTaskKKM;
import com.shtrih.tinyjavapostester.databinding.ActivityMainBinding;
import com.shtrih.tinyjavapostester.task.ConnectToBluetoothDeviceTask;


public class MainActivity extends AppCompatActivity {

    public static final String PROTOCOL = "0";
    public static final String TIMEOUT = "3000";
    public static final String ORDER_RESPONSE = "order_response";

    private AppCompatCheckBox chbFastConnect;
    private AppCompatCheckBox chbScocFirmwareUpdate;

    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = ViewModelProviders.of(this).get(MainViewModel.class);
        binding.setVm(model);
        binding.setActivity(this);
        final SharedPreferences pref = this.getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
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

    public void printXReport(View v) {
        new PrintXReportTaskKKM(this, model).execute();
    }

    public void printZReport(View v) {
        new PrintZReportTaskKKM(this, model).execute();
    }


    public void printReceipt(View view) {
        new PrintReceiptTask(this, model).execute();

    }

    public void printReceiptCopy(View view) {
        new PrintDuplicateReceiptTask(this, model).execute();
    }

    public void openDay(View view) {
        new OpenDayTask(this, model).execute();
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