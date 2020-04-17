package com.example.hackernewsreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
    Button button;

    int loadedNews = 0;
    String[] idList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.loadMoreButton);

        listView = findViewById(R.id.newsListView);
        newsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, newsList);

        listView.setAdapter(newsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startNewsActivity(urlList.get(position));
            }
        });

        DownloadTask newsTask = new DownloadTask();
        button.setAlpha(0.4f);
        Toast.makeText(this, "Loading your news...", Toast.LENGTH_LONG).show();
        newsTask.execute();
    }

    public void loadNews(View view) {
        DownloadTask newsTask = new DownloadTask();
        Toast.makeText(this, "Loading your news...", Toast.LENGTH_LONG).show();

        view.setAlpha(0.4f);
        view.setEnabled(false);

        newsTask.execute();
    }

    public void startNewsActivity(String newsUrl) {
//        Log.i("News URL", newsUrl);
        Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
        intent.putExtra("newsUrl", newsUrl);
        startActivity(intent);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            if (idList == null) {

                Request request = new Request.Builder()
                        .url(NEWS_LIST)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String s = response.body().string();
                    s = s.substring(2, s.length() - 2);
                    idList = s.split(", ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (int i = loadedNews; i < loadedNews + 20; i++) {
                callHackerNewsDetail(idList[i]);
                Log.i("News Order", Integer.toString(i));
            }
            loadedNews += 20;
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            newsListAdapter.notifyDataSetChanged();

            button.setAlpha(1.0f);
            button.setEnabled(true);
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
}