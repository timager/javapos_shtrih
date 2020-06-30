package com.shtrih.tinyjavapostester.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.shtrih.tinyjavapostester.R;
import com.shtrih.tinyjavapostester.Receipt;
import com.shtrih.tinyjavapostester.network.ConfirmBody;
import com.shtrih.tinyjavapostester.network.ConfirmResponse;
import com.shtrih.tinyjavapostester.network.ErrorResponse;
import com.shtrih.tinyjavapostester.network.NetworkService;
import com.shtrih.tinyjavapostester.network.OrderResponse;
import com.shtrih.tinyjavapostester.network.TransactionBody;
import com.shtrih.tinyjavapostester.network.TransactionResponse;
import com.shtrih.tinyjavapostester.task.OpenDayTask;
import com.shtrih.tinyjavapostester.task.PrintDuplicateReceiptTask;
import com.shtrih.tinyjavapostester.task.PrintReceiptTask;
import com.shtrih.tinyjavapostester.task.PrintTextTask;
import com.shtrih.tinyjavapostester.task.PrintXReportTaskKKM;
import com.shtrih.tinyjavapostester.task.PrintZReportTaskKKM;
import com.shtrih.tinyjavapostester.task.listener.Listener;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AbstractActivity {

    public static final String PROTOCOL = "0";
    public static final String TIMEOUT = "3000";
    public static final String ORDER_RESPONSE = "order_response";
    public static final String DEEP_LINK_DATA = "deep_link_data";

    private TextView infoView;
    private Button btnOpenDay;
    private Button btnPrintReceipt;
    private Button btnPrintCopy;
    private Button bthXReport;
    private Button bthZReport;

    private OrderResponse response = null;
    private JSONObject deepLinkData = null;

    @Override
    protected int chooseLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoView = findViewById(R.id.info);
        btnOpenDay = findViewById(R.id.btnOpenDay);
        btnPrintReceipt = findViewById(R.id.btnPrintReceipt);
        btnPrintCopy = findViewById(R.id.btnPrintCopy);
        bthXReport = findViewById(R.id.bthXReport);
        bthZReport = findViewById(R.id.bthZReport);
        Intent intent = getIntent();
        response = (OrderResponse) intent.getSerializableExtra(ORDER_RESPONSE);
        try {
            String json = intent.getStringExtra(DEEP_LINK_DATA);
            if (json != null)
                deepLinkData = new JSONObject(json);
        } catch (JSONException e) {
            showMessage(e.getMessage());
        }
        enableBluetooth();
        toConnect();
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
        Receipt receipt = new Receipt(response.getOrder(), deepLinkData);
        new PrintReceiptTask(this, model, receipt, new Listener<Exception>() {
            @Override
            public void handle(final Exception value) {
                createTransaction(value);
            }
        }).execute();

    }

    private void createTransaction(final Exception exception) {
        final TransactionBody transactionBody = new TransactionBody(response.getOrder(), deepLinkData);
        NetworkService.getInstance().getApi().createTransaction(transactionBody).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                sendResultToApi(exception, transactionBody.getPackUuid());
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                showMessage("Произошла ошибка при создании транзакции: " + t.getMessage());
            }
        });
    }

    private void sendResultToApi(Exception exception, String uuid) {
        ConfirmBody confirmBody = new ConfirmBody();
        confirmBody.setPackUuid(uuid);
        if (exception != null) {
            confirmBody.setMessage(exception.getMessage());
            showMessage("ПРОИЗОШЛА ОШИБКА: \n" + exception.getMessage());
            new PrintTextTask(MainActivity.this, model, "ПРОИЗОШЛА ОШИБКА: \n" + exception.getMessage()).execute();
            NetworkService.getInstance().getApi().sendReceiptError(confirmBody).enqueue(new Callback<ErrorResponse>() {
                @Override
                public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                    showMessage("Результат отправлен");
                }

                @Override
                public void onFailure(Call<ErrorResponse> call, Throwable t) {
                    showMessage("Произошла ошибка при отправке результата печати: " + t.getMessage());
                }
            });
        } else {
            NetworkService.getInstance().getApi().sendReceiptConfirm(confirmBody).enqueue(new Callback<ConfirmResponse>() {
                @Override
                public void onResponse(Call<ConfirmResponse> call, Response<ConfirmResponse> response) {
                    showMessage("Результат отправлен");
                }

                @Override
                public void onFailure(Call<ConfirmResponse> call, Throwable t) {
                    showMessage("Произошла ошибка при отправке результата печати: " + t.getMessage());
                }
            });
        }
    }

    public void printReceiptCopy(View view) {
        new PrintDuplicateReceiptTask(this, model).execute();
    }

    public void openDay(View view) {
        new OpenDayTask(this, model).execute();
    }


    @Override
    protected void onDayOpened(boolean isOpen) {
        if (isOpen) {
            infoView.setText("Смена открыта, касса готова к работе");
            btnOpenDay.setEnabled(false);
            if (response != null)
                btnPrintReceipt.setEnabled(true);
            btnPrintCopy.setEnabled(true);
            bthXReport.setEnabled(true);
            bthZReport.setEnabled(true);
        } else {
            infoView.setText("Необходимо открыть смену");
            btnOpenDay.setEnabled(true);
            if (response != null)
                btnPrintReceipt.setEnabled(false);
            btnPrintCopy.setEnabled(false);
            bthXReport.setEnabled(false);
            bthZReport.setEnabled(false);
        }
    }
}