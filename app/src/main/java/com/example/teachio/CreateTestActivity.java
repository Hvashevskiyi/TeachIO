package com.example.teachio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class CreateTestActivity extends AppCompatActivity {

    private Spinner spinnerLessonType;
    private Button btnSaveTest;

    private Spinner spinnerExistingSubjects;
    private EditText editTextTestName;

    private int userId;
    private int selectedLessonTypeId = -1;
    private List<LessonType> lessonTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "ID учителя не был передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        spinnerLessonType = findViewById(R.id.spinnerLessonType);


        spinnerExistingSubjects = findViewById(R.id.spinnerExistingSubjects);
        editTextTestName = findViewById(R.id.editTextTestName);

        new LoadLessonTypesTask().execute();

        spinnerLessonType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                LessonType selectedLessonType = lessonTypes.get(position);
                selectedLessonTypeId = selectedLessonType.getId();


                // Загружаем предметы после выбора типа урока
                new LoadSubjectsTask().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Ничего не делаем в этом случае
            }
        });


        btnSaveTest = findViewById(R.id.btnSaveTest);
        btnSaveTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызываем метод для отправки запроса на сохранение теста
                saveTest();
            }
        });
    }

    private void saveTest() {
        String testName = editTextTestName.getText().toString().trim();
        if (testName.isEmpty()) {
            Toast.makeText(this, "Введите название теста", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем выбранный предмет из спиннера
        Subject selectedSubject = (Subject) spinnerExistingSubjects.getSelectedItem();
        int selectedSubjectId = selectedSubject.getId();

        // Отправляем запрос на сервер для проверки существования теста
        String url = Config.BASE_URL + "/check_test_existence?Name=" + testName + "&IdS=" + selectedSubjectId;
        new CheckTestExistenceTask().execute(url);
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

    private class LoadSubjectsTask extends AsyncTask<Void, Void, List<Subject>> {
        @Override
        protected List<Subject> doInBackground(Void... params) {
            List<Subject> subjectsList = new ArrayList<>();

            try {
                String url = Config.BASE_URL + "/get_subjects?IdLT=" + selectedLessonTypeId + "&IdU=" + userId;
                String result = makeGetRequest(url);

                JSONObject jsonResponse = new JSONObject(result);
                JSONArray subjectsArray = jsonResponse.getJSONArray("subjects");

                for (int i = 0; i < subjectsArray.length(); i++) {
                    JSONObject subjectJson = subjectsArray.getJSONObject(i);
                    Subject subject = new Subject(subjectJson.getInt("id"), subjectJson.getString("name"));
                    subjectsList.add(subject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return subjectsList;
        }

        @Override
        protected void onPostExecute(List<Subject> result) {
            if (result != null && !result.isEmpty()) {
                ArrayAdapter<Subject> subjectsAdapter = new ArrayAdapter<>(CreateTestActivity.this, android.R.layout.simple_spinner_item, result);
                subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerExistingSubjects.setAdapter(subjectsAdapter);

                // Разрешаем взаимодействие с элементами интерфейса
                enableTestCreationUI(true);
            } else {
                // Отключаем взаимодействие с элементами интерфейса
                enableTestCreationUI(false);
            }
        }
    }

    private void enableTestCreationUI(boolean enable) {
        editTextTestName.setEnabled(enable);
        btnSaveTest.setEnabled(enable);
        int visibility = enable ? View.VISIBLE : View.GONE;
        editTextTestName.setVisibility(visibility);
        btnSaveTest.setVisibility(visibility);
        spinnerExistingSubjects.setVisibility(visibility);
    }

    private class CheckTestExistenceTask extends AsyncTask<String, Void, String> {
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
            // Обработка результата проверки
            handleCheckResult(result);
        }
    }

    private void handleCheckResult(String result) {
        try {
            JSONObject jsonResponse = new JSONObject(result);

            // Check if the test exists
            boolean testExists = jsonResponse.optBoolean("test_exists", false);

            if (testExists) {
                // Тест уже существует
                Toast.makeText(this, "Такой тест уже существует", Toast.LENGTH_SHORT).show();
            } else {
                // Тест не существует, можно добавить
                int newTestId = jsonResponse.optInt("new_test_id", 0);

                // Перейти на новую активность, передав ID теста
                Intent intent = new Intent(CreateTestActivity.this, CreateQuestionActivity.class);
                intent.putExtra("test_id", newTestId);
                startActivity(intent);

                Toast.makeText(this, "Тест успешно сохранен с ID: " + newTestId, Toast.LENGTH_SHORT).show();
                // Здесь можно добавить код для сохранения теста в базе данных
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing exception
            Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

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
}
