package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ImageBuffer {

    private static final HashMap<String, Drawable> images = new HashMap<>();

    public static Drawable GetImage(String url) {
        if (images.containsKey(url)) {
            return images.get(url);
        }

        Drawable image = LoadImageFromWebOperations(url);

        images.put(url, image);

        return image;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable LoadImageFromWebOperations(String url) {

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
            return ResourcesCompat.getDrawable(Resources.getSystem(), R.drawable.error, null);
        }
    }
}
