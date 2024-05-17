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

public class StudentActivity extends AppCompatActivity {

    private Button btnCabinet, btnSearch, btnViewResults,  btnExportResults, btnTest;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        userId = getIntent().getIntExtra("user_id", -1);
        if (userId != -1) {
            String url = Config.BASE_URL + "/get_username/" + userId;
            new GetUsernameTask().execute(url);
        }
        btnTest = findViewById(R.id.btnTakeTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTakeTestActivity();
            }
        });

        btnViewResults = findViewById(R.id.btnViewResults);
        btnViewResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewResultsActivity();
            }
        });

        btnExportResults = findViewById(R.id.btnExportResult);
        btnExportResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExportResultsActivity();
            }
        });


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
                onBackPressed();
            }
        });

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });


      
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void openSearch(){
        Intent intent = new Intent(StudentActivity.this, SearchTestActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }
    private void openCabinet() {
        Intent intent = new Intent(StudentActivity.this, CabinetActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void openViewResultsActivity() {
        Intent intent = new Intent(StudentActivity.this, ViewResultsActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }
    private void openExportResultsActivity() {
        Intent intent = new Intent(StudentActivity.this, ExportResultsActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void openTakeTestActivity() {
        // Замените на активность, которая предназначена для прохождения теста
        // например, TestActivity.class
        Intent intent = new Intent(StudentActivity.this, TestActivity.class);
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
                    Toast.makeText(StudentActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                } else if (jsonResponse.has("username")) {
                    String username = jsonResponse.getString("username");
                    getSupportActionBar().setTitle("Здравствуйте, " + username);
                } else {
                    Toast.makeText(StudentActivity.this, "Unknown response from server", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(StudentActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }
    }
}