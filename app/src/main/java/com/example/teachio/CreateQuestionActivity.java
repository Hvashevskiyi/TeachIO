package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateQuestionActivity extends AppCompatActivity {

    private TextView textViewQuestionNumber;
    private EditText editTextQuestion;
    private EditText editTextCorrectAnswer;
    private EditText editTextCost;
    private Button btnFinishCreation;
    private Button btnNextQuestion;
    private int testId;

    private int questionNumber = 1; // начинаем с первого вопроса

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_question);
        getSupportActionBar().setTitle("Вопрос №" + questionNumber);

        // Установка Toolbar
        testId = getIntent().getIntExtra("test_id", -1); // -1 - значение по умолчанию, если test_id не передан

        editTextQuestion = findViewById(R.id.editTextQuestion);
        editTextCorrectAnswer = findViewById(R.id.editTextCorrectAnswer);
        editTextCost = findViewById(R.id.editTextCost);
        btnFinishCreation = findViewById(R.id.btnFinishCreation);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        updateQuestionNumber();

        btnFinishCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Реализация сохранения вопроса в базе данных и завершение активности
                saveQuestion();
                finish();
            }
        });

        btnNextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Реализация сохранения вопроса в базе данных и переход к следующему вопросу
                saveQuestion();
                clearFields();
                questionNumber++;

                updateQuestionNumber();
            }
        });
    }

    private void updateQuestionNumber() {
        getSupportActionBar().setTitle("Вопрос №" + questionNumber);
    }

    private void clearFields() {
        editTextQuestion.getText().clear();
        editTextCorrectAnswer.getText().clear();
        editTextCost.getText().clear();
    }

    private void saveQuestion() {
        String questionText = editTextQuestion.getText().toString().trim();
        String correctAnswer = editTextCorrectAnswer.getText().toString().trim();
        String cost = editTextCost.getText().toString().trim();

        if (questionText.isEmpty() || correctAnswer.isEmpty() || cost.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Отправить запрос на сервер для сохранения вопроса
        new SaveQuestionTask().execute(questionText, correctAnswer, cost);
    }

    private class SaveQuestionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String questionText = params[0];
            String correctAnswer = params[1];
            String cost = params[2];

            try {
                String url = Config.BASE_URL + "/create_question";
                String postData = "text=" + questionText +
                        "&correctAnswer=" + correctAnswer + "&cost=" + cost + "&test_id=" + testId;
                return makePostRequest(url, postData);
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        private String makePostRequest(String urlStr, String postData) throws IOException {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            byte[] outputInBytes = postData.getBytes("UTF-8");
            OutputStream os = urlConnection.getOutputStream();
            os.write(outputInBytes);
            os.close();

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
        }

        @Override
        protected void onPostExecute(String result) {
            // Обработка результата сохранения вопроса
            handleSaveResult(result);
        }
    }

    private void handleSaveResult(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean success = jsonResult.getBoolean("success");

            if (success) {
                Toast.makeText(this, "Вопрос успешно сохранен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка при сохранении вопроса", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
