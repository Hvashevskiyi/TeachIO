package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchTestActivity extends AppCompatActivity {

    private EditText etTestId;
    private Button btnSearch;
    private TextView tvTestDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_test);

        etTestId = findViewById(R.id.etTestId);
        btnSearch = findViewById(R.id.btnSearch);
        tvTestDetails = findViewById(R.id.tvTestDetails);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTestById();
            }
        });
    }

    private void searchTestById() {
        String testIdStr = etTestId.getText().toString();

        if (!testIdStr.isEmpty()) {
            int testId = Integer.parseInt(testIdStr);
            String url = Config.BASE_URL + "/get_test_by_id/" + testId;

            new GetTestByIdTask().execute(url);
        } else {
            Toast.makeText(this, "Введите ID теста", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetTestByIdTask extends AsyncTask<String, Void, String> {

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
                    Toast.makeText(SearchTestActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    // Отобразить данные о тесте на экране
                    displayTestDetails(jsonResponse);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SearchTestActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayTestDetails(JSONObject testDetails) {
        try {
            JSONObject testData = testDetails.getJSONObject("test_data");

            // Получаем нужные данные из JSON и отображаем их
            int id = testData.getInt("id");
            String name = testData.getString("name");
            String subjectName = testData.getString("subject_name");

            String details = "ID: " + id + "\n" +
                    "Test Name: " + name + "\n" +
                    "Subject Name: " + subjectName;

            tvTestDetails.setText(details);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SearchTestActivity", "Error displaying test details");
        }
    }

}
