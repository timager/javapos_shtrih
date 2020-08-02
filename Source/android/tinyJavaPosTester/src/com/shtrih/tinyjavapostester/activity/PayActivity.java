package com.shtrih.tinyjavapostester.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.shtrih.tinyjavapostester.R;
import com.shtrih.tinyjavapostester.network.NetworkService;
import com.shtrih.tinyjavapostester.network.OrderBody;
import com.shtrih.tinyjavapostester.network.OrderResponse;

import org.json.JSONException;
import org.json.JSONObject;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_pay);
        Intent intent = getIntent();
        TextView dataTextField = findViewById(R.id.textView2);
        if (intent.getData() != null) {
            try {
                JSONObject data = getJsonFromUrl(intent.getData());
                dataTextField.setText(intent.getData().toString());
                getOrderDataFromCmdAPI(data);
            } catch (JSONException e) {
                dataTextField.setText(e.getMessage());
            }
        } else {
            dataTextField.setText("Нет данных");
        }
    }

    private JSONObject getJsonFromUrl(Uri url) throws JSONException {
        String json = url.getQuery().replaceAll("[/]+$", "");
        return new JSONObject(json);
    }

    private void getOrderDataFromCmdAPI(final JSONObject deepLinkData) {
        try {
            NetworkService.getInstance().getApi().getOrder(new OrderBody(deepLinkData)).enqueue(new Callback<OrderResponse>() {
                @Override
                public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                    int responseCode = response.code();
                    if (responseCode == 200) {
                        if ((response.body() != null)) {
                            sendDataToMainActivity(response.body(), deepLinkData);
                        } else {
                            showMessage("Ответ на запрос не содержит данных");
                        }
                    } else {
                        showMessage("Неверный ответ на запрос (" + responseCode + ")");
                    }
                }

                @Override
                public void onFailure(Call<OrderResponse> call, Throwable t) {
                    showMessage("Произошла ошибка при запросе (" + t.getLocalizedMessage() + ")");
                }
            });
        } catch (JSONException e) {
            showMessage("Ошибка формата ссылки, не удалось декодировать JSON");
        }
    }

    private void sendDataToMainActivity(OrderResponse data, JSONObject deepLinkData) {
        Intent intent = new Intent(PayActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.ORDER_RESPONSE, data);
        intent.putExtra(MainActivity.DEEP_LINK_DATA, deepLinkData.toString());
        startActivity(intent);
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
