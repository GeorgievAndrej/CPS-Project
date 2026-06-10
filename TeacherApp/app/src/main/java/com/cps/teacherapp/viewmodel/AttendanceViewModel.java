package com.cps.teacherapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.cps.teacherapp.data.model.AttendanceRecord;
import com.cps.teacherapp.repository.AttendanceRepository;
import java.util.List;
import com.cps.teacherapp.data.sync.SyncCallback;

public class AttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;
    private final LiveData<List<AttendanceRecord>> allRecords;

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        repository = new AttendanceRepository(application);
        allRecords = repository.getAllRecords();
    }

    public LiveData<List<AttendanceRecord>> getAllRecords() {
        return allRecords;
    }

    public void insertIfNotDuplicate(AttendanceRecord record,
                                     Runnable onDuplicate,
                                     Runnable onInserted) {
        repository.insertIfNotDuplicate(record, onDuplicate, onInserted);
    }

    public void syncAllPending(String token, SyncCallback callback) {
        repository.syncUnsynced(token, callback);
    }
}