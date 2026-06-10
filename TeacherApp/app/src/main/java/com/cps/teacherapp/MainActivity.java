package com.cps.teacherapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cps.teacherapp.data.model.AttendanceRecord;
import com.cps.teacherapp.data.sync.SyncCallback;
import com.cps.teacherapp.databinding.ActivityMainBinding;
import com.cps.teacherapp.storage.TokenManager;
import com.cps.teacherapp.ui.adapter.AttendanceAdapter;
import com.cps.teacherapp.viewmodel.AttendanceViewModel;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String[][] techLists;

    private static final byte[] CPS_AID = new byte[]{
            (byte)0xF0, 0x43, 0x4C, 0x41, 0x53, 0x53, 0x01
    };

    private static final byte[] SELECT_APDU = buildSelectApdu(CPS_AID);

    private ActivityMainBinding binding;
    private AttendanceViewModel viewModel;
    private AttendanceAdapter adapter;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String sessionId;
    private String authToken;
    private boolean sessionActive = true;

    private String lastTappedId = "";
    private long lastTapTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authToken = TokenManager.getInstance(this).getToken();
        sessionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        binding.tvSessionInfo.setText("Сесија: " + sessionId);

        setupViewModel();
        setupRecyclerView();
        setupNfc();
        setupButtons();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        viewModel.getAllRecords().observe(this, records -> {
            adapter.setData(records);
            binding.tvRecordCount.setText("Присутни: " + records.size());
            long unsynced = records.stream().filter(r -> !r.isSynced).count();
            if (unsynced > 0) {
                binding.tvSyncStatus.setText("⏳ " + unsynced + " несинхронизирани записи");
            } else {
                binding.tvSyncStatus.setText("✅ Сè е синхронизирано");
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AttendanceAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            binding.tvNfcStatus.setText("Овој уред нема NFC");
            binding.tvNfcStatus.setTextColor(0xFFEF5350);
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            binding.tvNfcStatus.setText("Вклучи го NFC во поставки");
            binding.tvNfcStatus.setTextColor(0xFFFFA726);
        }

        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        String[][] techLists = new String[][] {
                new String[] { IsoDep.class.getName() }
        };
    }

    private void setupButtons() {
        binding.btnSync.setOnClickListener(v -> startBulkSync());
        binding.btnEndSession.setOnClickListener(v -> {
            sessionActive = false;
            if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
            binding.tvNfcStatus.setText("Сесијата е завршена");
            binding.tvNfcStatus.setTextColor(0xFFEF5350);
            binding.btnEndSession.setEnabled(false);
            startBulkSync();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && sessionActive) {
            // ЗОШТО null tech lists? Ги прифаќаме СВИ NFC тагови/уреди.
            // Конкретната филтрација ја правиме во onNewIntent со IsoDep.
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techLists);
            Log.d("NFC_DEBUG", "ForegroundDispatch enabled, NFC adapter: " + nfcAdapter);
            binding.tvNfcStatus.setText("NFC слуша...");
            binding.tvNfcStatus.setTextColor(0xFF4CAF50);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("NFC_DEBUG", "onNewIntent called! Action: " + intent.getAction());
        super.onNewIntent(intent);
        if (!sessionActive) return;

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        new Thread(() -> readHceData(tag)).start();
    }

    private void readHceData(Tag tag) {
        Log.d("NFC_DEBUG", "readHceData called, tag: " + tag);
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            runOnUiThread(() -> showFeedback(false, "Несовпаѓање на NFC протокол"));
            return;
        }

        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(SELECT_APDU);
            isoDep.close();

            if (response.length < 2) {
                runOnUiThread(() -> showFeedback(false, "Празен одговор од студент"));
                return;
            }

            byte sw1 = response[response.length - 2];
            byte sw2 = response[response.length - 1];

            if (sw1 != (byte)0x90 || sw2 != (byte)0x00) {
                runOnUiThread(() -> showFeedback(false, "Студентот не е најавен (SW: "
                        + String.format("%02X %02X", sw1, sw2) + ")"));
                return;
            }

            // Отстрани ги последните 2 статус bytes, остатокот е JSON
            byte[] jsonBytes = Arrays.copyOf(response, response.length - 2);
            String jsonStr = new String(jsonBytes, "UTF-8");

            JSONObject data = new JSONObject(jsonStr);

            String studentId   = data.getString("studentId");
            String studentName = data.getString("studentName");
            String course      = data.optString("course", "General");

            long now = System.currentTimeMillis();
            if (studentId.equals(lastTappedId) && (now - lastTapTime) < 2000) {
                return;
            }
            lastTappedId = studentId;
            lastTapTime = now;

            AttendanceRecord record = new AttendanceRecord(studentId, studentName, course, sessionId);

            viewModel.insertIfNotDuplicate(
                    record,
                    () -> runOnUiThread(() -> showFeedback(false, studentName + " веќе е евидентиран!")),
                    () -> runOnUiThread(() -> {
                        showFeedback(true, "✅ " + studentName);
                        vibrateSuccess();
                    })
            );

        } catch (Exception e) {
            Log.e(TAG, "HCE read error", e);
            runOnUiThread(() -> showFeedback(false, "Грешка при читање: " + e.getMessage()));
        }
    }

    private static byte[] buildSelectApdu(byte[] aid) {

        byte[] apdu = new byte[5 + aid.length];
        apdu[0] = 0x00;
        apdu[1] = (byte) 0xA4;
        apdu[2] = 0x04;
        apdu[3] = 0x00;
        apdu[4] = (byte) aid.length;
        System.arraycopy(aid, 0, apdu, 5, aid.length);
        return apdu;
    }

    private void startBulkSync() {
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Нема токен — најави се повторно", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.syncProgressBar.setVisibility(View.VISIBLE);
        binding.btnSync.setEnabled(false);
        binding.tvSyncStatus.setText("Синхронизирање...");

        viewModel.syncAllPending(authToken, new SyncCallback() {
            @Override public void onProgress(int current, int total) {
                runOnUiThread(() -> {
                    binding.syncProgressBar.setMax(total);
                    binding.syncProgressBar.setProgress(current);
                    binding.tvSyncStatus.setText(current + "/" + total + " синхронизирани...");
                });
            }
            @Override public void onComplete(int totalSynced) {
                runOnUiThread(() -> {
                    binding.syncProgressBar.setVisibility(View.GONE);
                    binding.btnSync.setEnabled(true);
                    binding.tvSyncStatus.setText("" + totalSynced + " записи синхронизирани");
                });
            }
            @Override public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    binding.syncProgressBar.setVisibility(View.GONE);
                    binding.btnSync.setEnabled(true);
                    binding.tvSyncStatus.setText("❌ " + errorMessage);
                });
            }
        });
    }

    private void showFeedback(boolean success, String message) {
        Toast.makeText(this, message, success ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("deprecation")
    private void vibrateSuccess() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) v.vibrate(200);
    }
}
