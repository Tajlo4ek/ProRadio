package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        Context context = this;

        new Thread(() -> {
            try {
                ImageBuffer.LoadRadio();

                String data = (String) getIntent().getSerializableExtra("data");
                if (data != null) {
                    Thread.sleep(1000);
                }

                Intent intentMain = new Intent(context, MainActivity.class);


                if (data != null) {
                    String idStr = data.substring(data.lastIndexOf("/") + 1);

                    if (data.contains("showradio")) {
                        intentMain.putExtra("showRadioId", idStr);
                    } else if (data.contains("showplaylist")) {
                        intentMain.putExtra("showPlayListId", idStr);
                    }
                }

                startActivity(intentMain);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        }).start();
    }
}
