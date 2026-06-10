package com.cps.teacherapp.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.cps.teacherapp.data.local.AppDatabase;
import com.cps.teacherapp.data.local.AttendanceDao;
import com.cps.teacherapp.data.model.AttendanceRecord;
import com.cps.teacherapp.data.sync.SyncCallback;
import com.cps.teacherapp.network.ApiService;
import com.cps.teacherapp.network.RetrofitClient;
import com.cps.teacherapp.network.SyncRequestBody;
import com.cps.teacherapp.network.models.SyncResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class AttendanceRepository {

    // TODO: Кога се додаде UI за избор на курс, овој ID доаѓа оттаму.
    private static final int DEFAULT_COURSE_ID = 1;

    private final AttendanceDao dao;
    private final ApiService api;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AttendanceRepository(Context context) {
        dao = AppDatabase.getInstance(context).attendanceDao();
        api = RetrofitClient.getInstance().getApi();
    }

    public LiveData<List<AttendanceRecord>> getAllRecords() {
        return dao.getAllRecords();
    }

    public void insertIfNotDuplicate(AttendanceRecord record,
                                     Runnable onDuplicate,
                                     Runnable onInserted) {
        executor.execute(() -> {
            int count = dao.countByStudentAndSession(record.studentId, record.sessionId);
            if (count > 0) {
                if (onDuplicate != null) onDuplicate.run();
            } else {
                dao.insert(record);
                if (onInserted != null) onInserted.run();
            }
        });
    }

    public void syncUnsynced(String token, SyncCallback callback) {
        executor.execute(() -> {
            List<AttendanceRecord> pending = dao.getUnsyncedRecords();
            int total = pending.size();

            if (total == 0) {
                callback.onComplete(0);
                return;
            }

            int synced = 0;

            for (int i = 0; i < pending.size(); i++) {
                AttendanceRecord record = pending.get(i);

                try {
                    // Обвиткај го записот во SyncRequestBody — ги мапира полињата кон backend форматот
                    SyncRequestBody body = new SyncRequestBody(record, DEFAULT_COURSE_ID);

                    Response<SyncResponse> response = api
                            .syncSingleRecord("Bearer " + token, body)
                            .execute();

                    if (response.isSuccessful()) {
                        dao.markAsSynced(record.id);
                        synced++;
                    }
                } catch (Exception e) {
                    // Мрежна грешка — продолжи со следниот запис
                }

                callback.onProgress(i + 1, total);
            }

            callback.onComplete(synced);
        });
    }
}
