package com.example.teachio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeacherActivity extends AppCompatActivity {

    private Button btnCabinet, btnCreateTest, btnCreateSubject;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        userId = getIntent().getIntExtra("user_id", -1);
        if (userId != -1) {
            String url = Config.BASE_URL + "/get_username/" + userId;
            new GetUsernameTask().execute(url);
        }

        btnCabinet = findViewById(R.id.btnCabinet);
        btnCabinet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCabinet();
            }
        });
        findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызываем onBackPressed для завершения текущей активности
                onBackPressed();
            }
        });

        btnCreateTest = findViewById(R.id.btnCreateTest);

        btnCreateTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия на кнопку "Создать тест"
                openCreateTestActivity();
            }
        });

        btnCreateSubject = findViewById(R.id.btnCreateSubject);

        btnCreateSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия на кнопку "Создать тест"
                openCreateSubjectActivity();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Переопределяем метод, чтобы вместо стандартного поведения вызвать finish()
        finish();
    }

    private void openCabinet() {
        Intent intent = new Intent(TeacherActivity.this, CabinetActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }
    private void openCreateTestActivity() {
        Intent intent = new Intent(TeacherActivity.this, CreateTestActivity.class);
        intent.putExtra("user_id", userId);
        // Вы можете добавить дополнительные данные в интент, если это необходимо
        startActivity(intent);
    }

    private void openCreateSubjectActivity() {
        Intent intent = new Intent(TeacherActivity.this, CreateSubjectActivity.class);
        intent.putExtra("user_id", userId);
        // Вы можете добавить дополнительные данные в интент, если это необходимо
        startActivity(intent);
    }

    public void openStatisticsActivity(View view) {
        Intent intent = new Intent(TeacherActivity.this, StatisticsActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private class GetUsernameTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlStr = params[0];

            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();
                    return response.toString();
                } else {
                    return "HTTP Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    Toast.makeText(TeacherActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                } else if (jsonResponse.has("username")) {
                    String username = jsonResponse.getString("username");
                    // Устанавливаем имя пользователя в ActionBar
                    getSupportActionBar().setTitle("Здравствуйте, " + username);
                } else {
                    Toast.makeText(TeacherActivity.this, "Unknown response from server", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(TeacherActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
