package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatisticsActivity extends AppCompatActivity {

    private int userId;
    private Button btnSendStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        userId = getIntent().getIntExtra("user_id", -1);


        btnSendStatistics = findViewById(R.id.btnSendStatistics);

        btnSendStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверяем, что userId корректен (не равен -1)
                if (userId != -1) {
                    // Вызываем функцию для получения статистики
                    new GetStatisticsTask().execute(userId);
                } else {
                    Toast.makeText(StatisticsActivity.this, "Некорректный ID пользователя", Toast.LENGTH_SHORT).show();
                    // Если userId некорректен, может быть нужно предпринять какие-то дополнительные действия
                }
            }
        });



    }

    private class GetStatisticsTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            int userId = params[0];
            String apiUrl = Config.BASE_URL + "/teacher_tests_average/" + userId;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Проверяем код ответа сервера
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Если код ответа OK, читаем данные
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    return stringBuilder.toString();
                } else {
                    // Если код ответа не OK, возвращаем ошибку
                    Log.e("StatisticsActivity", "Server response code: " + responseCode);
                    return null;
                }
            } catch (IOException e) {
                Log.e("StatisticsActivity", "Error retrieving statistics", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Обработка полученных данных
                Toast.makeText(StatisticsActivity.this, "Статистика успешно отправлена", Toast.LENGTH_SHORT).show();
                Log.d("StatisticsActivity", result);
            } else {
                Toast.makeText(StatisticsActivity.this, "Ошибка отправки статистики", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
