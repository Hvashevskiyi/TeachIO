package com.example.teachio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class SubjectDetailsActivity extends AppCompatActivity {

    private int userId;
    private int subjectId;
    private String subjectType;
    EditText editTextPassword;
    TextView text;
    private ListView listViewTests;
    private ArrayAdapter<String> testAdapter;
    private TextView textViewSelectedTest;
    private Button buttonStartTest;
    private String selectedTest; // Переменная для хранения выбранного теста
    private static final int REQUEST_CODE_CHOOSE_TEST = 1; // Код запроса для выбора теста

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_details);

        // Инициализация элементов макета
        TextView textViewSubjectName = findViewById(R.id.textViewSubjectName);
        TextView textViewTeacherName = findViewById(R.id.textViewTeacherName);
        TextView textViewLessonType = findViewById(R.id.textViewLessonType);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonBack = findViewById(R.id.buttonBack);
        Button buttonChooseTest = findViewById(R.id.buttonChooseTest);
        text = findViewById(R.id.textViewShowPass);
        listViewTests = findViewById(R.id.listViewTests);
        textViewSelectedTest = findViewById(R.id.textViewSelectedTest);
        buttonStartTest = findViewById(R.id.buttonStartTest);

        // Получение данных из предыдущей активности
        Intent intent = getIntent();
        subjectId = getIntent().getIntExtra("subject_id", -1);
        String subjectName = intent.getStringExtra("subject_name");
        String teacherName = intent.getStringExtra("teacher_name");
        String lessonTypeName = intent.getStringExtra("lesson_type_name");
        userId = getIntent().getIntExtra("user_id", -1);

        // Установка значений в элементы макета
        textViewSubjectName.setText("Название предмета: " + subjectName);
        textViewTeacherName.setText("Имя учителя: " + teacherName);
        textViewLessonType.setText(lessonTypeName);

        // Инициализация адаптера для списка тестов
        testAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewTests.setAdapter(testAdapter);

        // Обработчик нажатия кнопки "Назад"
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Завершаем текущую активность и возвращаемся назад
            }
        });

        // Обработчик нажатия кнопки "Выбрать тест"
        buttonChooseTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = "";

                if ("Open".equals(subjectType)) {
                    new CheckPasswordTask().execute(enteredPassword);
                    Toast.makeText(SubjectDetailsActivity.this, "Тип предмета: Открытый", Toast.LENGTH_SHORT).show();
                } else {
                    enteredPassword = editTextPassword.getText().toString();
                    new CheckPasswordTask().execute(enteredPassword);
                }

                new GetSubjectTypeTask().execute(subjectId);

                // Отображение ListView
                listViewTests.setVisibility(View.VISIBLE);

                // Добавьте код для отображения текста и кнопки
                textViewSelectedTest.setVisibility(View.VISIBLE);
                buttonStartTest.setVisibility(View.VISIBLE);
            }
        });

        // Обработчик нажатия на элемент списка тестов
        listViewTests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTest = testAdapter.getItem(position);
                textViewSelectedTest.setText("Выбран тест: " + selectedTest);
            }
        });

        // Обработчик нажатия кнопки "Запустить тест"
        buttonStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Добавьте код для запуска выбранного теста
                if (selectedTest != null) {
                    // Осуществляем запрос на сервер, чтобы получить selected_id
                    new GetTestIdTask().execute(selectedTest, String.valueOf(subjectId));
                } else {
                    Toast.makeText(SubjectDetailsActivity.this, "Выберите тест перед запуском", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new GetSubjectTypeTask().execute(subjectId);
    }

    private class CheckPasswordTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String enteredPassword = params[0];

            try {
                int subjectId = getIntent().getIntExtra("subject_id", -1);
                String url = Config.BASE_URL + "/get_password/" + subjectId;

                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
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

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String passwordFromDatabase = jsonResponse.getString("password");

                    return enteredPassword.equals(passwordFromDatabase);
                } else {
                    return false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(SubjectDetailsActivity.this, "Пароль верный", Toast.LENGTH_SHORT).show();

                new GetTestsTask().execute(subjectId, userId);
            } else {
                Toast.makeText(SubjectDetailsActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetTestsTask extends AsyncTask<Integer, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Integer... params) {
            int subjectId = params[0];
            int userId = params[1];  // Добавляем user_id

            try {
                String url = Config.BASE_URL + "/get_testis/" + subjectId + "/" + userId;

                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
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

                    // Получаем список тестов из JSON-ответа
                    JSONArray jsonTests = new JSONArray(response.toString());
                    List<String> tests = new ArrayList<>();

                    for (int i = 0; i < jsonTests.length(); i++) {
                        JSONObject jsonTest = jsonTests.getJSONObject(i);
                        String testName = jsonTest.getString("testName");

                        tests.add(testName);
                    }

                    return tests;
                } else {
                    return null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> tests) {
            if (tests != null) {
                // Обновляем данные адаптера
                testAdapter.clear();
                testAdapter.addAll(tests);
                testAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(SubjectDetailsActivity.this, "Ошибка при получении тестов", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class GetSubjectTypeTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int subjectId = params[0];

            String url = Config.BASE_URL + "/get_subject_type/" + subjectId;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
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
            handleSubjectTypeResult(result);
        }
    }

    private void handleSubjectTypeResult(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);

            if (jsonResult.has("error")) {
                String errorMessage = jsonResult.getString("error");
                Toast.makeText(SubjectDetailsActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            } else if (jsonResult.has("subjectType")) {
                subjectType = jsonResult.getString("subjectType");

                Toast.makeText(SubjectDetailsActivity.this, "Тип предмета: " + subjectType, Toast.LENGTH_SHORT).show();

                if ("Open".equals(subjectType)) {
                    editTextPassword.setVisibility(View.GONE);
                    text.setVisibility(View.GONE);
                } else {
                    editTextPassword.setVisibility(View.VISIBLE);
                    text.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(SubjectDetailsActivity.this, "Unknown response from server", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(SubjectDetailsActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetTestIdTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String testName = params[0];
            int subjectId = Integer.parseInt(params[1]);

            try {
                String url = Config.BASE_URL + "/get_test_id/" + subjectId + "/" + testName;

                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
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

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getInt("test_id");
                } else {
                    return -1;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer testId) {
            if (testId != -1) {
                // Используйте testId для запуска теста
                // Создайте Intent для перехода к QuestionsLookActivity
                Intent intent = new Intent(SubjectDetailsActivity.this, QuestionsLookActivity.class);

                // Передайте значения user_id и selected_id в новую активность
                intent.putExtra("user_id", userId);
                intent.putExtra("selected_id", testId);

                // Запустите новую активность
                startActivity(intent);
            } else {
                Toast.makeText(SubjectDetailsActivity.this, "Ошибка при получении test_id", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
