package com.tajlok.proradio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String data = intent.getDataString();

        boolean hasInfo = Intent.ACTION_VIEW.equals(action) && data != null;

        Intent intentMain = new Intent(this, SplashActivity.class);

        if (hasInfo) {
            intentMain.putExtra("data", data);
        }

        startActivity(intentMain);


        finish();
    }
}
