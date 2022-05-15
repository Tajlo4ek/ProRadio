package com.tajlok.proradio;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Api {

    private static final OkHttpClient httpClient = new OkHttpClient();

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static final String UserAgent = "proRadio";


    public static JSONObject SendPost(String url, JSONObject json) {

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", UserAgent)
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("error " + response);
            }

            return new JSONObject(response.body().string());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    public static JSONObject SendPost(String url) {
        return SendPost(url, new JSONObject());
    }

    public static void SendGet(String url, JSONObject data) {

    }
}
