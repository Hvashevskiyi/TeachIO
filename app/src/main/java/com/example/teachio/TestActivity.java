package com.example.teachio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private ListView listViewLessonType;
    private TextView selectedSubjectTextView;
    private ListView listViewSubjects;
    private Button btnStartTest;
    private int userId;
    private int selectedLessonTypeId = -1;
    private TextView selectedLessonTypeTextView;
    private List<Map<String, Object>> lessonTypes;
    private List<SubjectData> subjectDataList;
    private SubjectData selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        selectedSubjectTextView = findViewById(R.id.textViewSelectedSubject);
        listViewLessonType = findViewById(R.id.listViewLessonType);
        listViewSubjects = findViewById(R.id.listViewSubjects);
        btnStartTest = findViewById(R.id.btnStartTest);
        selectedLessonTypeTextView = findViewById(R.id.textViewSelectedLessonType);
        userId = getIntent().getIntExtra("user_id", -1);
        new LoadLessonTypesTask().execute();

        listViewLessonType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> selectedLessonType = lessonTypes.get(position);
                selectedLessonTypeId = (int) selectedLessonType.get("id");
                String lessonTypeName = selectedLessonType.get("name").toString();
                selectedLessonTypeTextView.setText("Выбранный LessonType:\n" + lessonTypeName);
                if (listViewSubjects.getAdapter() != null) {
                    // Очищаем адаптер перед новым заполнением
                    ((ArrayAdapter<?>) listViewSubjects.getAdapter()).clear();
                }




                // Load subjects after selecting the lesson type
                new LoadSubjectsTask().execute(selectedLessonTypeId);
            }
        });

        // Click handler for selecting a subject
        listViewSubjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Extract the selected subject name
                selectedSubject = (SubjectData) parent.getItemAtPosition(position);
                selectedSubjectTextView.setText("Выбранный Subject:\n" + selectedSubject.getSubjectName());

                // Add code here to handle the selection of the subject, e.g., opening an activity for the test
            }
        });

        btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия на кнопку "Начать тест"

                // Проверяем, выбран ли тип урока
                if (selectedLessonTypeId == -1) {
                    Toast.makeText(TestActivity.this, "Выберите тип урока", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверяем, выбран ли предмет
                if (selectedSubjectTextView.getText().toString().isEmpty()) {
                    Toast.makeText(TestActivity.this, "Выберите предмет", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Запускаем SubjectDetailsActivity и передаем необходимые данные
                Intent intent = new Intent(TestActivity.this, SubjectDetailsActivity.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("lesson_type_name", selectedLessonTypeTextView.getText().toString());
                intent.putExtra("subject_id", selectedSubject.getSubjectId());
                intent.putExtra("subject_name", selectedSubject.getSubjectName());
                intent.putExtra("teacher_name", selectedSubject.getTeacherName());
                startActivity(intent);
            }
        });
    }

    private class LoadLessonTypesTask extends AsyncTask<Void, Void, List<Map<String, Object>>> {
        @Override
        protected List<Map<String, Object>> doInBackground(Void... params) {
            lessonTypes = new ArrayList<>();

            try {
                String url = Config.BASE_URL + "/get_lessontypes";
                String result = makeGetRequest(url);

                JSONObject jsonResponse = new JSONObject(result);
                JSONArray lessonTypesArray = jsonResponse.getJSONArray("lesson_types");

                for (int i = 0; i < lessonTypesArray.length(); i++) {
                    JSONObject lessonType = lessonTypesArray.getJSONObject(i);
                    Map<String, Object> lessonTypeMap = new HashMap<>();
                    lessonTypeMap.put("id", lessonType.getInt("id"));
                    lessonTypeMap.put("name", lessonType.getString("name"));
                    lessonTypes.add(lessonTypeMap);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return lessonTypes;
        }

        @Override
        protected void onPostExecute(List<Map<String, Object>> result) {
            updateLessonTypesListView(result);
        }
    }

    private class LoadSubjectsTask extends AsyncTask<Integer, Void, List<SubjectData>> {
        @Override
        protected List<SubjectData> doInBackground(Integer... params) {
            int selectedLessonTypeId = params[0];
            subjectDataList = new ArrayList<>();

            try {
                String url = Config.BASE_URL + "/get_subjects_by_idlt?IdLT=" + selectedLessonTypeId;
                String result = makeGetRequest(url);

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("subjects")) {
                    JSONArray subjectsArray = jsonResponse.getJSONArray("subjects");

                    for (int i = 0; i < subjectsArray.length(); i++) {
                        JSONObject subjectObject = subjectsArray.getJSONObject(i);
                        int subjectId = subjectObject.getInt("id");
                        String subjectName = subjectObject.getString("name");
                        String teacherName = subjectObject.getString("teacher_name");

                        SubjectData subjectData = new SubjectData(subjectId, subjectName, teacherName);
                        subjectDataList.add(subjectData);
                    }
                } else if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    Log.e("LoadSubjectsTask", "Error: " + errorMessage);
                } else {
                    Log.e("LoadSubjectsTask", "Unknown response from server");
                }
            } catch (JSONException e) {
                Log.e("LoadSubjectsTask", "JSON Error: " + e.getMessage());
                e.printStackTrace();
            }

            return subjectDataList;
        }

        @Override
        protected void onPostExecute(List<SubjectData> result) {
            updateSubjectsListView(result);
        }
    }

    private void updateLessonTypesListView(List<Map<String, Object>> lessonTypes) {
        if (lessonTypes != null && !lessonTypes.isEmpty()) {
            List<String> lessonTypeNames = new ArrayList<>();
            for (Map<String, Object> lessonType : lessonTypes) {
                lessonTypeNames.add(lessonType.get("name").toString());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lessonTypeNames);
            listViewLessonType.setAdapter(adapter);
        } else {
            // Handle the case when lessonTypes is empty or null
            Toast.makeText(this, "Список уроков пуст", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSubjectsListView(List<SubjectData> subjects) {
        if (subjects != null && !subjects.isEmpty()) {
            ArrayAdapter<SubjectData> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    subjects
            );

            listViewSubjects.setAdapter(adapter);
            Log.d("updateSubjectsListView", "Subjects displayed successfully.");
        } else {
            // Handle the case when subjects is empty or null
            Toast.makeText(this, "Список предметов пуст", Toast.LENGTH_SHORT).show();
            Log.e("updateSubjectsListView", "Subjects list is empty or null.");
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
                Log.e("makeGetRequest", "HTTP Error: " + responseCode);
                return "HTTP Error: " + responseCode;
            }
        } catch (IOException e) {
            Log.e("makeGetRequest", "Error: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private static class SubjectData {
        private int subjectId;
        private String subjectName;
        private String teacherName;

        public SubjectData(int subjectId, String subjectName, String teacherName) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.teacherName = teacherName;
        }

        public int getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getTeacherName() {
            return teacherName;
        }

        @Override
        public String toString() {
            return subjectName + "\n" + "Учитель: " + teacherName;
        }
    }
}
