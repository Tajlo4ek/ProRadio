package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    TextView tbRadioName;

    ImageButton startStopBtn;
    ImageButton startStopBtn2;

    Drawable btnBackground;

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
    Radio nowShowRadio = null;

    String userId;
    int lovePlaylistId;

    int sharedPlaylist = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(StaticProperty.ThemeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        if (!preferences.contains("end_ok")) {
            Intent intent = new Intent(this, FirstStartActivity.class);
            startActivity(intent);
            finish();
        }

        userId = preferences.getString("userId", "null");
        lovePlaylistId = preferences.getInt("lovePlayListId", -1);

        System.out.println(userId);
        System.out.println(lovePlaylistId);

        viewPager = findViewById(R.id.view_pager);
        radioList = ImageBuffer.GetRadioList();


        tbRadioName = findViewById(R.id.tbRadioName);
        tbRadioName.setOnClickListener(v -> {

            if (nowPLay == null) {
                return;
            }

            isShowMoreInfo = true;
            nowShowRadio = nowPLay;

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
        });

        groupPlay = findViewById(R.id.groupPlay);
        groupPlay.setVisibility(View.INVISIBLE);

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            if (nowShowRadio == null) {
                return;
            }

            ShareRadio(nowShowRadio.getId());
        });

        View.OnClickListener startStopListener = v -> {
            if (mediaPlayer.isPlaying()) {
                pausePlay();
            } else {
                startPlay();
            }
        };

        startStopBtn = findViewById(R.id.btnStartStop);
        startStopBtn2 = findViewById(R.id.btnStartStop2);
        startStopBtn.setOnClickListener(startStopListener);
        startStopBtn2.setOnClickListener(startStopListener);

        btnBackground = startStopBtn.getBackground();

        View.OnClickListener likeListener = v -> {
            if (nowPLay == null) {
                return;
            }

            nowPLay.setUserLike(!nowPLay.getUserLike());
            btnLike.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
            btnLike2.setImageResource(nowPLay.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
            saveLiked();

            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("playlist_id", lovePlaylistId);
                    json.put("channel_id", nowPLay.getId());
                    JSONObject request = Api.SendPost(
                            nowPLay.getUserLike() ?
                                    "https://newradiobacklast.herokuapp.com/playlist/add_channel" :
                                    "https://newradiobacklast.herokuapp.com/playlist/del_channel",
                            json);
                    System.out.println(request);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }).start();

            ViewRadioAdapter adapter = (ViewRadioAdapter) viewPager.getAdapter();
            if (adapter != null) {
                adapter.SetChanged();
                adapter.notifyDataSetChanged();
            }

        };

        btnLike = findViewById(R.id.btnLike);
        btnLike2 = findViewById(R.id.btnLike2);
        btnLike.setOnClickListener(likeListener);
        btnLike2.setOnClickListener(likeListener);

        String id = (String) getIntent().getSerializableExtra("showPlayListId");
        if (id != null) {
            try {
                sharedPlaylist = Integer.parseInt(id);
            } catch (NumberFormatException ignored) {
            }
        }


        UpdateData();


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(mp -> startPlay());


        isNeedShowPanel = false;
        nowPLay = null;
        showIsPlayed();

        id = (String) getIntent().getSerializableExtra("showRadioId");
        if (id != null) {
            try {
                ShowMoreInfo(Integer.parseInt(id));
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

                nowShowRadio = radio;
                Context context = this;

                new Thread(() -> {
                    Drawable img = ImageBuffer.GetImage(radio.getCoverUrl());

                    new Handler(context.getMainLooper())
                            .post(
                                    () -> radioImage.setImageDrawable(img == null ? getDrawable(R.drawable.error) : img));

                }).start();
                btnLike2.setImageResource(radio.getUserLike() ? R.drawable.liked : R.drawable.not_liked);
                radioName.setText(radio.getName());
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

            nowShowRadio = null;
        } else {
            super.onBackPressed();
        }

    }

    public void UpdateData() {
        UpdateData(null, -1);
    }

    public void UpdateData(SwipeRefreshLayout refreshLayout, int pos) {
        MainActivity context = this;
        Thread thread = new Thread(() -> {
            loadData();

            context.runOnUiThread(() -> {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                if (pos != -1) {
                    viewPager.setCurrentItem(pos);
                }
            });

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
        startStopBtn.setBackground(btnBackground);

        startStopBtn2.setImageResource(R.drawable.pause);
        startStopBtn2.setBackground(btnBackground);

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

    public void RegisterForMenu(View view) {
        view.setOnLongClickListener(v -> {
            showPopupMenu(v);
            return true;
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.share_menu);

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {
                case R.id.item_share_radio:
                    ShareRadio(Integer.parseInt(v.getTag().toString()));
                    return true;
                case R.id.item_share_playlist:
                    SharePlayList(lovePlaylistId);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void ShareRadio(int radioId) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://proradio.su/showradio/" + radioId);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    private void SharePlayList(int playlistId) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://proradio.su/showplaylist/" + playlistId);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Поделиться"));
    }

    private void loadData() {

        SharedPreferences preferences = getSharedPreferences("likeRadio", Context.MODE_PRIVATE);

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

        if (sharedPlaylist != -1) {
            JSONObject list = Api.SendGet("https://newradiobacklast.herokuapp.com/playlist/" + sharedPlaylist);

            Iterator<String> iter = list.keys();
            while (iter.hasNext()) {
                String key = iter.next();

                for (int i = 0; i < radioList.size(); i++) {
                    if (key.equals(radioList.get(i).getId() + "")) {
                        radioList.get(i).setShared(true);
                        break;
                    }
                }
            }
        }

        MainActivity context = this;
        this.runOnUiThread(() -> {

            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            int widthDiv2 = size.x / 2;

            if (viewPager != null) {
                viewPager.setAdapter(new ViewRadioAdapter(context, radioList, widthDiv2));
            }

            TabLayout tabLayout = findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(viewPager, true);

            ViewRadioAdapter adapter = (ViewRadioAdapter) viewPager.getAdapter();
            if (adapter != null) {
                adapter.SetChanged();
                adapter.notifyDataSetChanged();

                if (sharedPlaylist != -1) {
                    viewPager.setCurrentItem(adapter.getCount() - 1);
                }
            }


        });


    }
}