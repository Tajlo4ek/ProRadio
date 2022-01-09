package com.tajlok.proradio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ChoiceLoveActivity extends AppCompatActivity {

    List<Radio> radioList;

    ImageView loadImage;
    AnimationDrawable loadAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chouce_love_activity);

        Context context = this;
        Button startBtn = (Button) findViewById(R.id.btnStart);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences preferences = getSharedPreferences("likeRadio", Context.MODE_PRIVATE);
                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();
                int radioNum = 0;
                for (int i = 0; i < radioList.size(); i++) {
                    if (radioList.get(i).getUserLike()) {
                        editor.putInt("radio" + radioNum, radioList.get(i).getId());
                        radioNum++;
                    }
                }
                editor.apply();

                preferences = getSharedPreferences("notFirstLoad", Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putBoolean("exist", true);
                editor.apply();

                Intent intentMain = new Intent(context, MainActivity.class);
                startActivity(intentMain);
            }
        });

        loadImage = (ImageView) findViewById(R.id.loadGif);
        loadImage.setBackgroundResource(R.drawable.load_anim);
        loadAnim = (AnimationDrawable) loadImage.getBackground();
        loadAnim.start();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
        thread.start();

    }

    private void loadData() {

        try {
            List<Radio> radioList = Radio.loadFromUrl("https://newradiobacklast.herokuapp.com/radio_channel");
            int rowCount = (radioList.size() + 1) / 2;

            ChoiceLoveActivity context = this;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadAnim.stop();
                    loadImage.setVisibility(View.GONE);

                    context.radioList = radioList;
                    TableLayout table = (TableLayout) findViewById(R.id.radioTable);
                    for (int rowInd = 0; rowInd < rowCount; rowInd++) {
                        TableRow tableRow = new TableRow(context);
                        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.MATCH_PARENT));

                        for (int i = rowInd * 2; (i < (rowInd + 1) * 2) && (i < radioList.size()); i++) {
                            tableRow.addView(viewFromRadio(context, radioList.get(i)));
                        }

                        table.addView(tableRow, rowInd);
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View viewFromRadio(Context context, Radio radio) {

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int widthDiv2 = size.x / 2;

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams lineParams = new TableRow.LayoutParams(
                widthDiv2,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.5f);
        root.setLayoutParams(lineParams);

        LinearLayout textLine = new LinearLayout(context);
        textLine.setLayoutParams(new LinearLayout.LayoutParams(
                widthDiv2,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f)
        );

        TextView text = new TextView(context);
        text.setText(radio.getName());
        text.setGravity(Gravity.CENTER);
        text.setTextColor(getResources().getColor(R.color.black));
        text.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.5f
                )
        );

        CheckBox checkBox = new CheckBox(context);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.performClick();
                radio.setUserLike(checkBox.isChecked());
            }
        });

        textLine.addView(checkBox);
        textLine.addView(text);

        ImageView image = new ImageView(context);
        image.setLayoutParams(
                new LinearLayout.LayoutParams(
                        widthDiv2,
                        widthDiv2
                )
        );

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable asd = LoadImageFromWebOperations(radio.getCoverUrl());
                    image.setImageDrawable(asd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        root.addView(image);
        root.addView(textLine);

        return root;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public Drawable LoadImageFromWebOperations(String url) {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();

            Bitmap bmp = BitmapFactory.decodeStream(input);

            if (bmp == null) {
                throw new Exception("img not load");
            }

            return new BitmapDrawable(Resources.getSystem(), bmp);
        } catch (Exception e) {
            return getResources().getDrawable(R.drawable.error);
        }
    }

}