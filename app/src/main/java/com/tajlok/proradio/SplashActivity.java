package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        loadTheme();
        setTheme(StaticProperty.ThemeId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        Context context = this;

        new Thread(() -> {
            try {
                ImageBuffer.LoadRadio();

                loadAB();

                String data = (String) getIntent().getSerializableExtra("data");
                if (data != null) {
                    Thread.sleep(1000);
                }


                Intent intentMain = new Intent(context, MainActivity.class);


                if (data != null) {
                    String idStr = data.substring(data.lastIndexOf("=") + 1);

                    if (data.contains("radio_id")) {
                        intentMain.putExtra("showRadioId", idStr);
                    } else if (data.contains("playlist_id")) {
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

    private void loadAB() {
        SharedPreferences preferences = getSharedPreferences("abTest", Context.MODE_PRIVATE);

        if (!preferences.contains("themeAB")) {
            try {

                JSONObject json = Api.SendGet(StaticProperty.apiWeb + "/percent/get_random/");

                System.out.println(json);

                if (json.getInt("scen_id") == 1) {
                    StaticProperty.ThemeAB = 1;
                } else if (json.getInt("scen_id") == 2) {
                    StaticProperty.ThemeAB = 2;
                }

                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("themeAB", StaticProperty.ThemeAB);
                editor.apply();

            } catch (Exception ignored) {

            }
        }

        loadTheme();
    }

    private void loadTheme() {
        SharedPreferences preferences = getSharedPreferences("abTest", Context.MODE_PRIVATE);

        int themeAb = preferences.getInt("themeAB", 1);
        switch (themeAb) {
            case 1:
                StaticProperty.ThemeId = R.style.Theme_ProRadioA;
                break;
            case 2:
                StaticProperty.ThemeId = R.style.Theme_ProRadioB;
                break;
        }
    }
}
