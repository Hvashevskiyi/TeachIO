package com.example.teachio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuestionsLookActivity extends AppCompatActivity {

    private List<Question> questionList = new ArrayList<>();
    private int questionIndex = 0;
    private int ourCost = 0;
    private int sumCost = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_look);

        // Получение значений из Intent
        int userId = getIntent().getIntExtra("user_id", -1);
        int selectedId = getIntent().getIntExtra("selected_id", -1);

        // Найти элементы управления
        TextView textViewQuestion = findViewById(R.id.textViewQuestion);
        EditText editTextAnswer = findViewById(R.id.editTextAnswer);
        Button buttonSubmitAnswer = findViewById(R.id.buttonSubmitAnswer);

        // Выполнение задачи для получения вопросов
        new GetQuestionsTask().execute(selectedId);

        // Нажатие на кнопку
        // Нажатие на кнопку
        buttonSubmitAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получение текущего вопроса
                Question currentQuestion = questionList.get(questionIndex);

                // Получение ответа пользователя
                String userAnswer = editTextAnswer.getText().toString().trim();

                // Сравнение ответа пользователя с правильным ответом
                if (userAnswer.equals(currentQuestion.getCorrectAnswer())) {
                    // Если ответ правильный, увеличиваем ourCost
                    ourCost += currentQuestion.getCost();
                }

                // Независимо от правильности ответа увеличиваем sumCost
                sumCost += currentQuestion.getCost();

                // Переход к следующему вопросу или завершение теста
                questionIndex++;

                if (questionIndex < questionList.size()) {
                    // Если есть еще вопросы, обновляем текст и очищаем поле ввода
                    textViewQuestion.setText(questionList.get(questionIndex).getText());
                    editTextAnswer.getText().clear();
                } else {
                    showResults();
                    finish();
                    // Если вопросы закончились, завершаем тест
                    Intent intent = new Intent(QuestionsLookActivity.this, SubjectDetailsActivity.class);

// Добавляем данные, которые нужно передать
                    intent.putExtra("user_id", userId); // Замените userId на фактическую переменную или значение
                    intent.putExtra("selected_id", selectedId); // Замените selectedId на фактическую переменную или значение

// Запускаем новую активность
                    startActivity(intent);
                }
            }
        });

    }

    private class GetQuestionsTask extends AsyncTask<Integer, Void, List<Question>> {
        @Override
        protected List<Question> doInBackground(Integer... params) {
            int testId = params[0];

            try {
                String url = Config.BASE_URL + "/get_questions/" + testId;

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
                    if (jsonResponse.has("questions")) {
                        JSONArray jsonQuestions = jsonResponse.getJSONArray("questions");

                        List<Question> questions = new ArrayList<>();

                        for (int i = 0; i < jsonQuestions.length(); i++) {
                            JSONObject jsonQuestion = jsonQuestions.getJSONObject(i);
                            String text = jsonQuestion.getString("text");
                            String correctAnswer = jsonQuestion.getString("correctAnswer");
                            int cost = jsonQuestion.getInt("cost");

                            Question question = new Question(text, correctAnswer, cost);
                            questions.add(question);
                        }

                        return questions;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            if (questions != null) {
                // Вывести Toast с количеством вопросов
                int questionCount = questions.size();
                Toast.makeText(QuestionsLookActivity.this, "Количество вопросов: " + questionCount, Toast.LENGTH_SHORT).show();

                // Загрузить список вопросов
                questionList = questions;

                // Начать с первого вопроса
                if (!questionList.isEmpty()) {
                    ((TextView) findViewById(R.id.textViewQuestion)).setText(questionList.get(0).getText());
                }

            } else {
                Toast.makeText(QuestionsLookActivity.this, "Ошибка при получении вопросов", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showResults() {
        // Проверка на деление на ноль
        if (sumCost != 0) {
            // Рассчет рейтинга
            double ratio = (double) ourCost / sumCost;
            int roundedMark = (int) Math.round(ratio * 10);

            // Отправка данных о рейтинге на сервер
            new AddRatingTask().execute(roundedMark);
        }
    }

    private class AddRatingTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            try {
                String url = Config.BASE_URL + "/add_rating";
                URL serverUrl = new URL(url);

                // Создание JSON-объекта с данными
                JSONObject requestData = new JSONObject();
                requestData.put("user_id", getIntent().getIntExtra("user_id", -1));
                requestData.put("selected_id", getIntent().getIntExtra("selected_id", -1));
                requestData.put("mark", params[0]);

                // Отправка данных на сервер
                HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    outputStream.write(requestData.toString().getBytes());
                }

                // Получение ответа от сервера
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Успешно добавлено
                    // Можете обработать ответ сервера, если необходимо
                } else {
                    // Обработка ошибки
                }

                urlConnection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Вы можете выполнить какие-либо действия после завершения добавления рейтинга
        }
    }
}
