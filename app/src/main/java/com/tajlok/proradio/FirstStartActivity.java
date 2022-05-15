package com.tajlok.proradio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class FirstStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            try {
                UUID userId = UUID.randomUUID();

                JSONObject json = new JSONObject();
                json.put("id", userId.toString());
                JSONObject request = Api.SendPost("https://newradiobacklast.herokuapp.com/mobile_user/", json);
                System.out.println(request);

                if (!request.getString("status").equals("ok")) {
                    return;
                }

                request = Api.SendPost("https://newradiobacklast.herokuapp.com/playlist/add/" + userId.toString());

                SharedPreferences preferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userId", userId.toString());
                editor.putInt("lovePlayListId", request.getInt("id"));
                editor.apply();

                System.out.println(userId.toString());
                System.out.println(request.getInt("id"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();


        setContentView(R.layout.first_start_layout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        if (viewPager != null) {
            viewPager.setAdapter(new SimplePagerAdapter(this, viewPager));
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);
    }
}