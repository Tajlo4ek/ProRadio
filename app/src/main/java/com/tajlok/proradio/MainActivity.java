package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final Object locker = new Object();

    MediaPlayer mediaPlayer;
    TextView tbRadioName;
    ImageButton startStopBtn;
    LinearLayout groupPlay;
    ViewPager viewPager;

    List<Radio> radioList;

    boolean isNeedShowPanel;
    Radio nowPLay;
    AnimationDrawable loadAnim;
    ImageButton btnLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("notFirstLoad", Context.MODE_PRIVATE);
        if (!preferences.contains("exist")) {
            Intent intentMain = new Intent(this, FirstStartActivity.class);
            startActivity(intentMain);
            finish();
        }

        tbRadioName = (TextView) findViewById(R.id.tbRadioName);
        tbRadioName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("asd");
            }
        });

        groupPlay = (LinearLayout) findViewById(R.id.groupPlay);
        groupPlay.setVisibility(View.INVISIBLE);

        startStopBtn = (ImageButton) findViewById(R.id.btnStartStop);
        startStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    pausePlay();
                } else {
                    startPlay();
                }
            }
        });

        MainActivity context = this;

        btnLike = (ImageButton) findViewById(R.id.btnLike);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowPLay == null) {
                    return;
                }

                nowPLay.setUserLike(!nowPLay.getUserLike());
                btnLike.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
                saveLiked();

                ViewRadioAdapter adapter = (ViewRadioAdapter) viewPager.getAdapter();
                if (adapter != null) {
                    adapter.SetChanged();
                    adapter.notifyDataSetChanged();
                }

            }
        });


        UpdateData();


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                startPlay();
            }
        });


        isNeedShowPanel = false;
        nowPLay = null;
        showIsPlayed();
    }

    public void UpdateData() {
        UpdateData(null, 0);
    }

    public void UpdateData(SwipeRefreshLayout refreshLayout, int pos) {
        MainActivity context = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();


                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout != null) {
                            refreshLayout.setRefreshing(false);
                        }

                        viewPager.setCurrentItem(pos);
                    }
                });

            }
        });
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlay();
    }

    private void stopPlay() {
        mediaPlayer.stop();
        isNeedShowPanel = false;
        showIsPlayed();
        nowPLay = null;
    }

    private void pausePlay() {
        mediaPlayer.pause();
        isNeedShowPanel = true;
        startStopBtn.setImageResource(R.drawable.play);
        showIsPlayed();
    }

    private void startPlay() {
        mediaPlayer.start();
        startStopBtn.setImageResource(R.drawable.pause);
        showIsPlayed();

        loadAnim.stop();
    }

    private void showIsPlayed() {
        groupPlay.setVisibility(isNeedShowPanel ? View.VISIBLE : View.GONE);
    }

    private void saveLiked() {
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
    }

    public void RunRadio(Radio radio) {

        if (nowPLay != null && nowPLay.getRadioStreamUrl().equals(radio.getRadioStreamUrl())) {
            return;
        }

        stopPlay();
        nowPLay = radio;

        try {
            startStopBtn.setImageDrawable(null);
            startStopBtn.setBackgroundResource(R.drawable.load_anim);
            loadAnim = (AnimationDrawable) startStopBtn.getBackground();
            loadAnim.start();
            btnLike.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
            isNeedShowPanel = true;
            showIsPlayed();

            mediaPlayer.reset();
            mediaPlayer.setDataSource(radio.getRadioStreamUrl());
            mediaPlayer.prepareAsync();
            tbRadioName.setText(String.format("  %s", radio.getName()));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "cant load stream", Toast.LENGTH_SHORT).show();
            stopPlay();
        }
    }

    private void loadData() {
        try {
            List<Radio> radioList = Radio.loadFromUrl("https://newradiobacklast.herokuapp.com/radio_channel");

            SharedPreferences preferences = getSharedPreferences("likeRadio", Context.MODE_PRIVATE);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();

            int radioNum = 0;
            while (preferences.contains("radio" + radioNum)) {
                int id = preferences.getInt("radio" + radioNum, -1);

                for (int i = 0; i < radioList.size(); i++) {
                    if (radioList.get(i).getId() == id) {
                        radioList.get(i).setUserLike(true);
                        break;
                    }
                }
                radioNum++;
            }

            MainActivity context = this;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    int widthDiv2 = size.x / 2;

                    context.radioList = radioList;

                    viewPager = (ViewPager) findViewById(R.id.view_pager);
                    if (viewPager != null) {
                        viewPager.setAdapter(new ViewRadioAdapter(context, radioList, widthDiv2));
                    }

                    TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                    tabLayout.setupWithViewPager(viewPager, true);

                    ViewRadioAdapter adapter = (ViewRadioAdapter) viewPager.getAdapter();
                    if (adapter != null) {
                        adapter.SetChanged();
                        adapter.notifyDataSetChanged();
                    }
                }
            });


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}