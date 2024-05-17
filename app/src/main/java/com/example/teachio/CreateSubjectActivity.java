package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CreateSubjectActivity extends AppCompatActivity {

    private EditText editTextSubjectName;
    private Spinner spinnerLessonType;
    private CheckBox checkBoxOpenSubject;
    private EditText editTextPassword;
    private Button buttonSave;
    private int userId;
    private List<LessonType> lessonTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subject);

        editTextSubjectName = findViewById(R.id.editTextSubjectName);
        spinnerLessonType = findViewById(R.id.spinnerLessonType);
        checkBoxOpenSubject = findViewById(R.id.checkBoxOpenSubject);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSave = findViewById(R.id.buttonSave);

        userId = getIntent().getIntExtra("user_id", -1);

        // Load lesson types
        new LoadLessonTypesTask().execute();

        // Set the initial visibility of password EditText based on checkbox state
        updatePasswordVisibility();

        checkBoxOpenSubject.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePasswordVisibility();
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSubject();
            }
        });
    }

    private void updatePasswordVisibility() {
        if (checkBoxOpenSubject.isChecked()) {
            editTextPassword.setVisibility(View.GONE);
        } else {
            editTextPassword.setVisibility(View.VISIBLE);
        }
    }

    private void saveSubject() {
        String name = editTextSubjectName.getText().toString();
        int selectedLessonTypeId = lessonTypes.get(spinnerLessonType.getSelectedItemPosition()).getId();
        boolean isOpenSubject = checkBoxOpenSubject.isChecked();
        String password = isOpenSubject ? "" : editTextPassword.getText().toString();
        String subjectType = isOpenSubject ? "Open" : "Close";

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Введите название предмета", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isOpenSubject && TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Введите пароль для закрытого предмета", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send data to server
        JSONObject postData = new JSONObject();
        try {
            postData.put("Name", name);
            postData.put("IdLT", selectedLessonTypeId);
            postData.put("IdU", userId);
            postData.put("Password", password);
            postData.put("SubjectType", subjectType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String saveSubjectUrl = Config.BASE_URL + "/save_subject";
        new SaveSubjectTask().execute(saveSubjectUrl, postData.toString());
    }

    private class SaveSubjectTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlStr = params[0];
            String postData = params[1];

            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                urlConnection.getOutputStream().write(postData.getBytes());

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
                Log.d("SaveSubjectTask", "Server response: " + result);

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    Log.e("SaveSubjectTask", "Server error: " + errorMessage);
                    Toast.makeText(CreateSubjectActivity.this, "Ошибка сервера: " + errorMessage, Toast.LENGTH_SHORT).show();
                } else if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Subject saved successfully")) {
                    Log.d("SaveSubjectTask", "Subject saved successfully");
                    Toast.makeText(CreateSubjectActivity.this, "Предмет успешно сохранен", Toast.LENGTH_SHORT).show();
                    finish(); // Finish the activity
                } else {
                    Log.e("SaveSubjectTask", "Unknown response from server");
                    Toast.makeText(CreateSubjectActivity.this, "Неизвестный запрос с сервера", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("SaveSubjectTask", "Error parsing JSON response: " + e.getMessage());
                Toast.makeText(CreateSubjectActivity.this, "Ошибка работы с данных", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadLessonTypesTask extends AsyncTask<Void, Void, List<LessonType>> {
        @Override
        protected List<LessonType> doInBackground(Void... params) {
            lessonTypes = new ArrayList<>();

            try {
                String url = Config.BASE_URL + "/get_lessontypes";
                String result = makeGetRequest(url);

                JSONObject jsonResponse = new JSONObject(result);
                JSONArray lessonTypesArray = jsonResponse.getJSONArray("lesson_types");

                for (int i = 0; i < lessonTypesArray.length(); i++) {
                    JSONObject lessonType = lessonTypesArray.getJSONObject(i);
                    LessonType lessonTypeObject = new LessonType(lessonType.getInt("id"), lessonType.getString("name"));
                    lessonTypes.add(lessonTypeObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return lessonTypes;
        }

        @Override
        protected void onPostExecute(List<LessonType> result) {
            updateLessonTypesSpinner(result);
        }
    }

    private void updateLessonTypesSpinner(List<LessonType> lessonTypes) {
        if (lessonTypes != null && !lessonTypes.isEmpty()) {
            ArrayAdapter<LessonType> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lessonTypes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLessonType.setAdapter(adapter);
        } else {
            // Обработка случая, когда lessonTypes пуст или равен null
            Toast.makeText(this, "Список уроков пуст", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to make a GET request
    private String makeGetRequest(String urlStr) {
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
}
