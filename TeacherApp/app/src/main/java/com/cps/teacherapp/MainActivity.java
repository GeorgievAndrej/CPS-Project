package com.cps.teacherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cps.teacherapp.storage.TokenManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvStatus;
    private Button btnStartSession, btnLogout;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        btnStartSession = findViewById(R.id.btnStartSession);
        btnLogout = findViewById(R.id.btnLogout);

        String name = tokenManager.getFullName();
        tvWelcome.setText("Добредојде, " + (name != null ? name : "Наставник") + "!");

        btnStartSession.setOnClickListener(v -> {
            tvStatus.setText("🟢 Сесијата е активна — чека NFC тапови...");
            btnStartSession.setEnabled(false);
        });

        btnLogout.setOnClickListener(v -> {
            tokenManager.clearAll();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}