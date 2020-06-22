package com.shtrih.tinyjavapostester;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.shtrih.tinyjavapostester.databinding.ActivityMainBinding;


public class PayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_pay);
        Intent intent = getIntent();
        TextView text1 = findViewById(R.id.textView);
        TextView text2 = findViewById(R.id.textView2);
        String str = intent.getStringExtra("url");
        str = str == null ? "Нет ссылки" : str;
        text1.setText(str);
        if(intent.getData() != null){
            text2.setText(intent.getData().toString());
        }else{
            text2.setText("Нет данных");
        }
    }
}
