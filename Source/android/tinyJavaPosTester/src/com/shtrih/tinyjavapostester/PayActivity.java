package com.shtrih.tinyjavapostester;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.shtrih.tinyjavapostester.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;


public class PayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pay);
        Intent intent = getIntent();
        TextView dataTextField = findViewById(R.id.textView2);
        if (intent.getData() != null) {
            try {
                JSONObject data = getJsonFromUrl(intent.getData());
                dataTextField.setText(data.toString());
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
}
