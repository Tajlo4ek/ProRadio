package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    ImageButton startStopBtn2;

    LinearLayout groupPlay;
    ViewPager viewPager;

    List<Radio> radioList;

    boolean isNeedShowPanel;
    Radio nowPLay;

    AnimationDrawable loadAnim;
    AnimationDrawable loadAnim2;

    ImageButton btnLike;
    ImageButton btnLike2;

    boolean isShowMoreInfo = false;
    int nowShowId = -1;

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

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        radioList = ImageBuffer.GetRadioList();


        tbRadioName = (TextView) findViewById(R.id.tbRadioName);
        tbRadioName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (nowPLay == null) {
                    return;
                }

                isShowMoreInfo = true;

                viewPager.setVisibility(View.GONE);
                findViewById(R.id.tab_layout).setVisibility(View.GONE);
                findViewById(R.id.groupPlay).setVisibility(View.GONE);

                ImageView radioImage = findViewById(R.id.imageView2);
                TextView radioName = findViewById(R.id.tbRadioName2);

                radioImage.setVisibility(View.VISIBLE);
                radioName.setVisibility(View.VISIBLE);
                findViewById(R.id.groupPlay2).setVisibility(View.VISIBLE);

                radioImage.setImageDrawable(ImageBuffer.GetImage(nowPLay.getCoverUrl()));
                radioName.setText(nowPLay.getName());
            }
        });

        groupPlay = (LinearLayout) findViewById(R.id.groupPlay);
        groupPlay.setVisibility(View.INVISIBLE);

        findViewById(R.id.btnShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowPLay == null) {
                    return;
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://proradio.ru/showradio/" + nowPLay.getId());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Поделиться"));
            }
        });

        View.OnClickListener startStopListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    pausePlay();
                } else {
                    startPlay();
                }
            }
        };

        startStopBtn = (ImageButton) findViewById(R.id.btnStartStop);
        startStopBtn2 = (ImageButton) findViewById(R.id.btnStartStop2);
        startStopBtn.setOnClickListener(startStopListener);
        startStopBtn2.setOnClickListener(startStopListener);

        MainActivity context = this;


        View.OnClickListener likeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowPLay == null) {
                    return;
                }

                nowPLay.setUserLike(!nowPLay.getUserLike());
                btnLike.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
                btnLike2.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
                saveLiked();

                ViewRadioAdapter adapter = (ViewRadioAdapter) viewPager.getAdapter();
                if (adapter != null) {
                    adapter.SetChanged();
                    adapter.notifyDataSetChanged();
                }

            }
        };

        btnLike = (ImageButton) findViewById(R.id.btnLike);
        btnLike2 = (ImageButton) findViewById(R.id.btnLike2);
        btnLike.setOnClickListener(likeListener);


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

        String showId = (String) getIntent().getSerializableExtra("showId");
        if (showId != null) {
            try {
                ShowMoreInfo(Integer.parseInt(showId));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void ShowMoreInfo(int radioId) {
        isShowMoreInfo = true;

        viewPager.setVisibility(View.GONE);
        findViewById(R.id.tab_layout).setVisibility(View.GONE);
        findViewById(R.id.groupPlay).setVisibility(View.GONE);

        ImageView radioImage = findViewById(R.id.imageView2);
        TextView radioName = findViewById(R.id.tbRadioName2);

        radioImage.setVisibility(View.VISIBLE);
        radioName.setVisibility(View.VISIBLE);
        findViewById(R.id.groupPlay2).setVisibility(View.VISIBLE);

        for (Radio radio : radioList) {
            if (radio.getId() == radioId) {

                Context context = this;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Drawable img = ImageBuffer.GetImage(radio.getCoverUrl());

                        new Handler(context.getMainLooper())
                                .post(
                                        () -> radioImage.setImageDrawable(img == null ? getDrawable(R.drawable.error) : img));

                    }
                }).start();
                radioName.setText(radio.getName());
                nowShowId = radioId;
                break;
            }
        }


    }

    @Override
    public void onBackPressed() {
        if (isShowMoreInfo) {
            isShowMoreInfo = false;

            viewPager.setVisibility(View.VISIBLE);
            findViewById(R.id.tab_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.groupPlay).setVisibility(View.VISIBLE);

            ImageView radioImage = findViewById(R.id.imageView2);
            TextView radioName = findViewById(R.id.tbRadioName2);

            radioImage.setVisibility(View.GONE);
            radioName.setVisibility(View.GONE);
            findViewById(R.id.groupPlay2).setVisibility(View.GONE);
            showIsPlayed();
        } else {
            super.onBackPressed();
        }

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
        mediaPlayer.release();
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
        startStopBtn2.setImageResource(R.drawable.play);
        showIsPlayed();
    }

    private void startPlay() {
        mediaPlayer.start();
        startStopBtn.setImageResource(R.drawable.pause);
        startStopBtn2.setImageResource(R.drawable.pause);
        showIsPlayed();

        if (loadAnim != null) {
            loadAnim.stop();
        }

        if (loadAnim2 != null) {
            loadAnim2.stop();
        }
    }

    private void showIsPlayed() {
        groupPlay.setVisibility(!isShowMoreInfo && isNeedShowPanel ? View.VISIBLE : View.GONE);
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
            startStopBtn2.setImageDrawable(null);

            startStopBtn.setBackgroundResource(R.drawable.load_anim);
            startStopBtn2.setBackgroundResource(R.drawable.load_anim);

            loadAnim = (AnimationDrawable) startStopBtn.getBackground();
            loadAnim.start();

            loadAnim2 = (AnimationDrawable) startStopBtn2.getBackground();
            loadAnim2.start();

            btnLike.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
            btnLike2.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
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


    }
}