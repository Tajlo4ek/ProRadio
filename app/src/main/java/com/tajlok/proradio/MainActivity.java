package com.tajlok.proradio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("likeRadio", Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();

        int radioNum = 0;
        while (preferences.contains("radio" + radioNum)) {
            System.out.println("like id " + preferences.getInt("radio" + radioNum, -1));
            radioNum++;
        }

        System.out.println("asffffffffffffffffff");
    }
}