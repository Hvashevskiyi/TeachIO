package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExportResultsActivity extends AppCompatActivity {

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_results);

        // Предполагаем, что у вас есть user_id, который нужно передать на сервер
        userId = getIntent().getIntExtra("user_id", -1);


    }

    public void exportData(View view) {
        new ExportRatingsTask().execute(userId);
    }
    private static class ExportRatingsTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            int userId = params[0];
            try {
                // Замените URL на свой серверный эндпоинт
                URL url = new URL(Config.BASE_URL + "/export_ratings_json/" + userId);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ViewResultsActivity", "Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                // Обработайте результат, который может содержать сообщение об успехе или ошибке
                Log.d("ViewResultsActivity", "Export Result: " + result);
            } else {
                Log.e("ViewResultsActivity", "Export Error: Unable to fetch data from the server");
            }
        }
    }
}
