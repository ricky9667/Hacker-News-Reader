package com.example.hackernewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient();

    static final String NEWS_LIST = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
    static final String NEWS_DETAIL_START = "https://hacker-news.firebaseio.com/v0/item/";
    static final String NEWS_DETAIL_END = ".json?print=pretty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        callHackerNewsList();
    }

    private void callHackerNewsList() {
        final Request request = new Request.Builder()
                .url(NEWS_LIST)
                .build();
        client.newCall(request).enqueue(new Callback() {
            // execute: sync, enqueue: async
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String s = response.body().string();
                s = s.substring(2, s.length()-2);
                String[] idList = s.split(", ");

                callHackerNewsDetail(idList[0]);

                Log.i("News ID List", Arrays.toString(idList));
            }
        });
    }

    private void callHackerNewsDetail(String newsId) {

        final Request request = new Request.Builder()
                .url(NEWS_DETAIL_START + newsId + NEWS_DETAIL_END)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                try {
                    JSONObject json = new JSONObject(s);
                    String newsTitle = json.getString("title");
                    String newsUrl = json.getString("url");

                    Log.i("News Title", newsTitle);
                    Log.i("News URL", newsUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}