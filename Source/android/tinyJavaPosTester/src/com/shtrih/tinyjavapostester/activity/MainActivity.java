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
        toConnect(address);
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

    private String getMessageFromTrace(StackTraceElement[] lines) {
        String result = "";
        for (StackTraceElement line : lines) {
            result += line + "\n";
        }
        return result;
    }

    private void createTransaction(final Exception exception) {
        String kkmNumber = "";
        long receiptNumber = 0;
        try {
            kkmNumber = model.getPrinter().getPhysicalDeviceName();
            receiptNumber = model.getPrinter().getReceiptNumber();
        } catch (Exception e) {
            showMessage(getMessageFromTrace(e.getStackTrace()));
        }
        final TransactionBody transactionBody = createTransactionBody(response.getOrder(), deepLinkData, kkmNumber, receiptNumber);
        NetworkService.getInstance(this).getApi().createTransaction(transactionBody).enqueue(new Callback<TransactionResponse>() {
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

    private TransactionBody createTransactionBody(OrderResponse.Order order, JSONObject deepLinkData, String kkmNumber, long receiptNumber) {
        try {
            if (isFullSale()) {
                return TransactionBody.createSaleTransactionBody(order, deepLinkData, kkmNumber, receiptNumber);
            } else if (isPartitionSale()) {
                return TransactionBody.createPartitionSaleTransactionBody(order, deepLinkData, kkmNumber, receiptNumber);
            } else if (isRefundService()) {
                return TransactionBody.createRefundServiceTransactionBody(order, deepLinkData, kkmNumber, receiptNumber);
            } else if (isRefundTransaction()) {
                return TransactionBody.createRefundTransactionTransactionBody(order, deepLinkData, kkmNumber, receiptNumber);
            } else if (isRefundByReason()) {
                return TransactionBody.createRefundByReasonTransactionBody(order, deepLinkData, kkmNumber, receiptNumber);
            }
        } catch (Exception e) { }

        return null;
    }

    private boolean isFullSale() throws JSONException {
        return isSale()
                && getDeepLinkSumPaymentSale() == getOrderSum();

    }

    private boolean isPartitionSale() throws JSONException {
        return isSale()
                && getDeepLinkSumPaymentSale() != getOrderSum();
    }

    private double getDeepLinkSumPaymentSale() throws JSONException {
        JSONObject operationData = deepLinkData.getJSONObject("operation_data");

        double paymentCash = operationData.getDouble("payment_cash");
        double paymentCard = operationData.getDouble("payment_card");

        return paymentCash + paymentCard;
    }

    private double getOrderSum() {
        return response.getOrder().getOrderAmount();
    }

    private boolean isRefundService() {
        return isRefund()
                && deepLinkData.optJSONObject("operation_data") != null
                && deepLinkData.optJSONObject("operation_data").opt("servs") != null;
    }

    private boolean isRefundTransaction() {
        return isRefund()
                && deepLinkData.optJSONObject("operation_data") != null
                && deepLinkData.optJSONObject("operation_data").opt("transactions") != null;
    }

    private boolean isRefundByReason() {
        return isRefund()
                && deepLinkData.optJSONObject("operation_data") != null
                && deepLinkData.optJSONObject("operation_data").opt("claim") != null;
    }

    private boolean isSale() {
        return deepLinkData.optInt("operation_type") == 1;
    }

    private boolean isRefund() {
        return deepLinkData.optInt("operation_type") == 2;
    }

    private void sendResultToApi(Exception exception, String uuid) {
        ConfirmBody confirmBody = new ConfirmBody();
        confirmBody.setPackUuid(uuid);
        if (exception != null) {
            confirmBody.setMessage(exception.getMessage());
            showMessage("ПРОИЗОШЛА ОШИБКА: \n" + exception.getMessage());
            NetworkService.getInstance(this).getApi().sendReceiptError(confirmBody).enqueue(new Callback<ErrorResponse>() {
                @Override
                public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                    showMessage("Результат с ошибкой отправлен");
                }

                @Override
                public void onFailure(Call<ErrorResponse> call, Throwable t) {
                    showMessage("Произошла ошибка при отправке результата печати: " + t.getMessage());
                }
            });
        } else {
            NetworkService.getInstance(this).getApi().sendReceiptConfirm(confirmBody).enqueue(new Callback<ConfirmResponse>() {
                @Override
                public void onResponse(Call<ConfirmResponse> call, Response<ConfirmResponse> response) {
                    showMessage("Успешный результат отправлен");
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