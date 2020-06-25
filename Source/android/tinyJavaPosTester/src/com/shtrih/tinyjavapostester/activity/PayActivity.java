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
                dataTextField.setText(data.toString());
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

    private void getOrderDataFromCmdAPI(JSONObject deepLinkData) {
        try {
            NetworkService.getInstance().getApi().getOrder(new OrderBody(deepLinkData)).enqueue(new Callback<OrderResponse>() {
                @Override
                public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                    if (response.body() != null) {
                        showMessage(response.body().getOrder().getPatientEmail());
                    }
                    else {
                        showMessage("произошел жиж");
                    }
                }

                @Override
                public void onFailure(Call<OrderResponse> call, Throwable t) {
                    showMessage(t.getMessage());
                    t.printStackTrace();
                }
            });
        } catch (JSONException e) {
            showMessage(e.getMessage());
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
