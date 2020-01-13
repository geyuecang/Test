package com.gc.myjni;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import ddd.CC;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyHelper.getInstance().show(MainActivity.this,"我是个通知");
            }
        });
    }
}
