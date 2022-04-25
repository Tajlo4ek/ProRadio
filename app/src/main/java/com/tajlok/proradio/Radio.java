package com.tajlok.proradio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Radio {

    private String name;
    private Boolean isActive;
    private String coverUrl;
    private Boolean isPopular;
    private String radioStreamUrl;
    private int id;

    private Boolean isUserLike;

    public static Radio fromJson(JSONObject json) throws JSONException {
        Radio radio = new Radio();

        radio.name = json.getString("name");
        radio.isActive = json.getBoolean("is_active");
        radio.coverUrl = json.getString("cover_url");
        radio.isPopular = json.getBoolean("is_popular");
        radio.radioStreamUrl = json.getString("radio_stream_url");
        radio.id = json.getInt("id");
        radio.isUserLike = false;

        return radio;
    }

    public static List<Radio> radioListFromJson(JSONArray json) throws JSONException {
        List<Radio> list = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            list.add(Radio.fromJson(json.getJSONObject(i)));
        }

        return getActive(list);
    }

    public static List<Radio> getActive(List<Radio> list) {
        List<Radio> newList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isActive) {
                newList.add(list.get(i));
            }
        }
        return newList;
    }

    public static List<Radio> loadFromUrl(String url) throws IOException, JSONException {

        InputStream is = (InputStream) new URL(url).getContent();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }

        return radioListFromJson(new JSONArray(result.toString("UTF-8")));
    }


    public String getName() {
        return name;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public Boolean getPopular() {
        return isPopular;
    }

    public String getRadioStreamUrl() {
        return radioStreamUrl;
    }

    public Boolean getUserLike() {
        return isUserLike;
    }

    public void setUserLike(Boolean userLike) {
        isUserLike = userLike;
    }

    public int getId() {
        return id;
    }
}
