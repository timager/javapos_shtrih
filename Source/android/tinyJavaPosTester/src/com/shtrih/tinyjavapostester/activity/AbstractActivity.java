package com.shtrih.tinyjavapostester.activity;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.tinyjavapostester.FirmwareUpdaterObserverImpl;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.application.App;
import com.shtrih.tinyjavapostester.databinding.ActivityMainBinding;
import com.shtrih.tinyjavapostester.search.tcp.TcpDeviceSearchActivity;
import com.shtrih.tinyjavapostester.shar_pref.SharedPreferenceKey;
import com.shtrih.tinyjavapostester.task.AutoConnectBluetoothDeviceTask;
import com.shtrih.tinyjavapostester.task.CheckDayOpenedTask;
import com.shtrih.tinyjavapostester.task.ConnectToBluetoothDeviceTask;
import com.shtrih.tinyjavapostester.task.listener.Listener;

import jpos.JposException;

public abstract class AbstractActivity extends AppCompatActivity {

    public static final String TIMEOUT = "10000";
    protected MainViewModel model;
    protected String address;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        address = getApp().getApplicationSharPref().getString(SharedPreferenceKey.ADDRESS, null);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, chooseLayout());
        model = ViewModelProviders.of(this).get(MainViewModel.class);
        binding.setVm(model);
        binding.setActivity(this);
    }

    protected abstract int chooseLayout();

    public void useDayOpened() {
        new CheckDayOpenedTask(this, model, new Listener<Boolean>() {
            @Override
            public void handle(final Boolean value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDayOpened(value);
                    }
                });
            }
        }).execute();
    }

    protected abstract void onDayOpened(boolean isOpen);

    protected void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showMessage("Устройство не поддерживает bluetooth");
        } else {
            mBluetoothAdapter.enable();
        }
    }

    protected void autoConnectBluetoothDevice() {

        new AutoConnectBluetoothDeviceTask(
                this,
                address,
                createFirmwareUpdateObserver(),
                TIMEOUT,
                false,
                false,
                model).execute();
    }

    public void toConnect(String address) {
        boolean isEnabled = checkEnabled();
        if (!isEnabled) {
            if(this.address == null){
                Intent i = new Intent(this, DeviceListActivity.class);
                startActivityForResult(i, DeviceListActivity.REQUEST_CONNECT_BT_DEVICE);
            }else{
                new ConnectToBluetoothDeviceTask(
                        this,
                        address,
                        createFirmwareUpdateObserver(),
                        TIMEOUT,
                        false,
                        false,
                        model).execute();
            }
        } else {
            this.address = address;
            getApp().getApplicationSharPref().edit().putString(SharedPreferenceKey.ADDRESS, address).apply();
            useDayOpened();
        }
    }

    protected boolean checkEnabled() {
        try {
            return model.getPrinter().getDeviceEnabled();
        } catch (JposException e) {
            return false;
        }
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
                            false,
                            false,
                            model).execute();
                } else {
                    toConnect(address);
                }
            case TcpDeviceSearchActivity.REQUEST_SEARCH_TCP_DEVICE:
                if (resultCode == Activity.RESULT_OK) {

                    Bundle extras = data.getExtras();

                    if (extras == null)
                        return;
                    String address = extras.getString("Address");
                    if (address == null)
                        return;

                    this.address = address;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    protected FirmwareUpdateObserver createFirmwareUpdateObserver() {
        return new FirmwareUpdaterObserverImpl(model);
    }

    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected final App getApp() {
        return (App) getApplication();
    }
}
