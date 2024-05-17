package com.example.teachio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextLogin;
    private TextView textViewLoginError;
    private EditText editTextName;
    private EditText editTextEmail;
    private TextView textViewEmailError;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Spinner spinnerUserType;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextLogin = findViewById(R.id.editTextLoginRegistration);
        textViewLoginError = findViewById(R.id.textViewLoginError);
        editTextName = findViewById(R.id.editTextNameRegistration);
        editTextEmail = findViewById(R.id.editTextEmailRegistration);
        textViewEmailError = findViewById(R.id.textViewEmailError);
        editTextPassword = findViewById(R.id.editTextPasswordRegistration);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPasswordRegistration);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String login = editTextLogin.getText().toString();
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String userType = spinnerUserType.getSelectedItem().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        textViewLoginError.setVisibility(View.GONE);
        textViewEmailError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Пароль должен содержать минимум 8 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка логина на сервере
        String checkLoginUrl = Config.BASE_URL + "/check_login/" + login;
        new CheckLoginTask().execute(checkLoginUrl, login, name, email, userType, password, confirmPassword);
    }

    private class CheckLoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String checkLoginUrl = params[0];
            String login = params[1];
            String name = params[2];
            String email = params[3];
            String userType = params[4];
            String password = params[5];
            String confirmPassword = params[6];

            try {
                URL url = new URL(checkLoginUrl);
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
                Log.d("CheckLoginTask", "Server response: " + result);

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    Log.e("CheckLoginTask", "Error: " + errorMessage);
                    textViewLoginError.setText(errorMessage);
                    textViewLoginError.setVisibility(View.VISIBLE);
                } else if (jsonResponse.has("login_available") && jsonResponse.getBoolean("login_available")) {
                    Log.d("CheckLoginTask", "Login is available. Proceeding to check email.");
                    // Логин свободен, проверяем почту
                    checkEmailAvailability();
                } else {
                    Log.e("CheckLoginTask", "Unknown response from server");
                    Toast.makeText(RegisterActivity.this, "Unknown response from server", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("CheckLoginTask", "Error parsing JSON response: " + e.getMessage());
                Toast.makeText(RegisterActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void checkEmailAvailability() {
        String checkEmailUrl = Config.BASE_URL + "/check_email/" + editTextEmail.getText().toString();
        new CheckEmailTask().execute(checkEmailUrl);
    }

    private class CheckEmailTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String checkEmailUrl = params[0];

            try {
                URL url = new URL(checkEmailUrl);
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
                Log.d("CheckEmailTask", "Server response: " + result); // Добавьте эту строку

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    Log.e("CheckEmailTask", "Error: " + errorMessage); // Добавьте эту строку
                    textViewEmailError.setText(errorMessage);
                    textViewEmailError.setVisibility(View.VISIBLE);
                } else if (jsonResponse.has("email_available") && jsonResponse.getBoolean("email_available")) {
                    Log.d("CheckEmailTask", "Email is available. Proceeding to registration."); // Добавьте эту строку
                    // Почта свободна, регистрируем пользователя
                    performRegistration();
                } else {
                    Log.e("CheckEmailTask", "Unknown response from server"); // Добавьте эту строку
                    Toast.makeText(RegisterActivity.this, "Unknown response from server", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("CheckEmailTask", "Error parsing JSON response: " + e.getMessage()); // Добавьте эту строку
                Toast.makeText(RegisterActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void performRegistration() {
        String url = Config.BASE_URL + "/register";

        JSONObject postData = new JSONObject();
        try {
            postData.put("login", editTextLogin.getText().toString());
            postData.put("name", editTextName.getText().toString());
            postData.put("email", editTextEmail.getText().toString());
            postData.put("userType", spinnerUserType.getSelectedItem().toString());
            postData.put("password", editTextPassword.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new RegisterTask().execute(url, postData.toString());
    }

    private class RegisterTask extends AsyncTask<String, Void, String> {
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
                Log.d("RegisterTask", "Server response: " + result);

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    Log.e("RegisterTask", "Server error: " + errorMessage);
                    Toast.makeText(RegisterActivity.this, "Server error: " + errorMessage, Toast.LENGTH_SHORT).show();
                } else if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Registration successful")) {
                    Log.d("RegisterTask", "Registration successful. Finishing activity.");
                    // Регистрация успешна, завершаем активность
                    finish();
                } else {
                    Log.e("RegisterTask", "Unknown response from server");
                    Toast.makeText(RegisterActivity.this, "Unknown response from server", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("RegisterTask", "Error parsing JSON response: " + e.getMessage());
                Toast.makeText(RegisterActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
            }
        }



    }
}
