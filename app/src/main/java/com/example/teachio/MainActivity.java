package com.example.teachio;

import android.content.Intent;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLogin;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegistration = findViewById(R.id.textViewRegistration);

        buttonLogin.setOnClickListener(view -> onLoginButtonClick());

        textViewRegistration.setOnClickListener(view -> {
            Intent registrationIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(registrationIntent);
        });
    }

    private void onLoginButtonClick() {
        String login = editTextLogin.getText().toString();
        String password = editTextPassword.getText().toString();
        Log.d(login, password);
        if (login.isEmpty() || password.isEmpty() || password.length() < 8) {
            showToast("Invalid login or password");
        } else {
            String url = Config.BASE_URL + "/login";
            JSONObject postData = createLoginJson(login, password);
            new LoginTask().execute(url, postData.toString());
        }
    }

    private JSONObject createLoginJson(String login, String password) {
        JSONObject postData = new JSONObject();
        try {
            postData.put("login", login);
            postData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postData;
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

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

                try (OutputStream os = urlConnection.getOutputStream()) {
                    os.write(postData.getBytes());
                    os.flush();
                }

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        return response.toString();
                    }
                } else {
                    return "HTTP Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("LoginTask", "Error making HTTP request: " + e.getMessage());

                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                showToast("Null response from server");
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    showToast("Authentication failed: " + errorMessage);
                } else if (jsonResponse.has("user_type")) {
                    String userType = jsonResponse.getString("user_type");

                    if ("Teacher".equals(userType)) {
                        // Если пользователь - учитель, запускаем TeacherActivity
                        Intent intent = new Intent(MainActivity.this, TeacherActivity.class);

                        // Передаем Id пользователя в новую активность
                        int userId = jsonResponse.getInt("user_id");
                        intent.putExtra("user_id", userId);

                        startActivity(intent);
                    } else if ("Student".equals(userType)) {
                        // Если пользователь - учитель, запускаем TeacherActivity
                        Intent intent = new Intent(MainActivity.this, StudentActivity.class);

                        // Передаем Id пользователя в новую активность
                        int userId = jsonResponse.getInt("user_id");
                        intent.putExtra("user_id", userId);

                        startActivity(intent);}
                    else {
                        // Для других типов пользователей можете выполнить другие действия
                        showToast("Authentication successful. User type: " + userType);
                    }
                } else {
                    showToast("Unknown response from server");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("LoginTask", "Error parsing JSON response: " + result);
                showToast("Error parsing JSON response");
            }
        }
    }
}
