// CabinetActivity.java

package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// CabinetActivity.java

// ... (ваш импорт и прочее)

public class CabinetActivity extends AppCompatActivity {

    private TextView textViewUserId;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private TextView textViewUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet);

        textViewUserId = findViewById(R.id.textViewUserId);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewUserRole = findViewById(R.id.textViewUserRole);

        int userId = getIntent().getIntExtra("user_id", -1);
        if (userId != -1) {
            String url = Config.BASE_URL + "/get_user_info/" + userId;
            new GetUserInfoTask().execute(url);
        }
        findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызываем onBackPressed для завершения текущей активности
                onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Переопределяем метод, чтобы вместо стандартного поведения вызвать finish()
        finish();
    }
    private class GetUserInfoTask extends AsyncTask<String, Void, String> {

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
                    Toast.makeText(CabinetActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                } else if (jsonResponse.has("user_info")) {
                    JSONObject userInfo = jsonResponse.getJSONObject("user_info");

                    // Проверка наличия ключа "id" в объекте userInfo
                    if (userInfo.has("id")) {
                        int userId = userInfo.getInt("id");
                        String userName = userInfo.getString("name");
                        String userEmail = userInfo.getString("email");
                        String userRole = userInfo.getString("userType"); // Используем "userType" вместо "role"

                        // Устанавливаем данные пользователя в текстовые поля
                        textViewUserId.setText(   "ID:    " + userId);
                        textViewUserName.setText( "Имя:   " + userName);
                        textViewUserEmail.setText("Почта: " + userEmail);
                        textViewUserRole.setText( "Роль:  " + userRole);
                    } else {
                        Toast.makeText(CabinetActivity.this, "Пользователь не найден в запросе", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CabinetActivity.this, "Неизвестный запрос с сервера", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(CabinetActivity.this, "Ошибка распаковки данных", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

