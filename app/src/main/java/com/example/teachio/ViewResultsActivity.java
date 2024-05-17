package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teachio.Config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ViewResultsActivity extends AppCompatActivity {

    private int userId;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        userId = getIntent().getIntExtra("user_id", -1);
        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        fetchRatings();
    }

    private void fetchRatings() {
        new FetchRatingsTask().execute(userId);
    }

    private class FetchRatingsTask extends AsyncTask<Integer, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Integer... params) {
            int userId = params[0];
            List<String> ratingsList = new ArrayList<>();

            try {
                URL url = new URL(Config.BASE_URL + "/get_ratings/" + userId);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    JSONObject json = new JSONObject(stringBuilder.toString());
                    JSONArray data = json.getJSONArray("data");

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        int mark = item.getInt("mark");
                        String testName = item.getString("test_name");

                        String formattedItem = "Тест: " + testName + "\n" +
                                "Оценка: " + mark;

                        ratingsList.add(formattedItem);
                    }

                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ViewResultsActivity", "Error: " + e.getMessage());
            }

            return ratingsList;
        }

        @Override
        protected void onPostExecute(List<String> ratingsList) {
            super.onPostExecute(ratingsList);
            if (ratingsList != null) {
                adapter.addAll(ratingsList);
                adapter.notifyDataSetChanged();
            } else {
                Log.e("ViewResultsActivity", "Error: Unable to fetch ratings from the server");
            }
        }
    }
}
