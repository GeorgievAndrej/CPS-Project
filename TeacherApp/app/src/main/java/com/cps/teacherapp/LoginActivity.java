package com.cps.teacherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cps.teacherapp.network.RetrofitClient;
import com.cps.teacherapp.network.models.LoginRequest;
import com.cps.teacherapp.network.models.LoginResponse;
import com.cps.teacherapp.storage.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = TokenManager.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        // Ако веќе е логиран прескокни Login
        if (tokenManager.isLoggedIn()) {
            goToMain();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                tvError.setText("Внеси корисничко име и лозинка");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            doLogin(username, password);
        });
    }

    private void doLogin(String username, String password) {
        btnLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        RetrofitClient.getInstance().getApi()
                .login(new LoginRequest(username, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse body = response.body();
                            tokenManager.saveToken(body.getAccessToken());
                            tokenManager.saveUserInfo(
                                    body.getUser().getUsername(),
                                    body.getUser().getFullName()
                            );
                            goToMain();
                        } else {
                            tvError.setText("Погрешно корисничко име или лозинка");
                            tvError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        tvError.setText("Грешка: Не може да се поврзе со серверот");
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}