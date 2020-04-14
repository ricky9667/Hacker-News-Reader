package com.example.hackernewsreader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    static final String NEWS_LIST = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
    static final String NEWS_DETAIL_START = "https://hacker-news.firebaseio.com/v0/item/";
    static final String NEWS_DETAIL_END = ".json?print=pretty";

    OkHttpClient client = new OkHttpClient();

    ListView listView;
    ArrayList<String> newsList = new ArrayList<>();
    ArrayList<String> urlList = new ArrayList<>();
    ArrayAdapter newsListAdapter;

    int numberOfViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.newsListView);
        newsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, newsList);
        listView.setAdapter(newsListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               startNewsActivity(urlList.get(position));
            }
        });

        callHackerNewsList();
    }

    public void startNewsActivity(String newsUrl) {
//        Log.i("News URL", newsUrl);

        Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
        intent.putExtra("newsUrl", newsUrl);
        startActivity(intent);
    }

    public void resetList(View view) {
        newsListAdapter.notifyDataSetChanged();
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

                int size = 20;
                if (idList.length < size) {
                    size = idList.length;
                }

                for(int i=0; i<size; i++) {
                    callHackerNewsDetail(idList[i]);
                    Log.i("News Order", Integer.toString(i));
                }
//                Log.i("News ID List", Arrays.toString(idList));
            }
        });
    }

    private void callHackerNewsDetail(final String newsId) {

        final Request request = new Request.Builder()
                .url(NEWS_DETAIL_START + newsId + NEWS_DETAIL_END)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String s = response.body().string();

            JSONObject json = new JSONObject(s);
            String newsTitle = json.getString("title");
            String newsUrl = json.getString("url");

            newsList.add(newsTitle);
            urlList.add(newsUrl);

            Log.i("News Title", newsTitle);
            Log.i("News URL", newsUrl);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}