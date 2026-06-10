package com.cps.teacherapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.cps.teacherapp.data.model.AttendanceRecord;
import java.util.List;

// @Dao = Data Access Object
// ЗОШТО интерфејс? Room генерира имплементација автоматски при компајлирање
@Dao
public interface AttendanceDao {

    // @Insert — Room автоматски пишува INSERT INTO SQL
    @Insert
    void insert(AttendanceRecord record);

    // LiveData<List<...>> = магија!
    // Кога базата ќе се промени, UI-от АВТОМАТСКИ се освежува
    // Нема потреба од рачен refresh
    @Query("SELECT * FROM attendance_records ORDER BY tapped_at DESC")
    LiveData<List<AttendanceRecord>> getAllRecords();

    // За bulk sync — само записите кои уште не се на серверот
    @Query("SELECT * FROM attendance_records WHERE is_synced = 0")
    List<AttendanceRecord> getUnsyncedRecords();

    // Откако ќе синхронизираме еден запис, го означуваме
    @Query("UPDATE attendance_records SET is_synced = 1 WHERE id = :id")
    void markAsSynced(int id);

    // Дедупликација — проверка пред insert
    // ЗАШТО? Ако студент тапне двапати во иста сесија, не треба дупликат
    @Query("SELECT COUNT(*) FROM attendance_records WHERE student_id = :studentId " +
            "AND session_id = :sessionId")
    int countByStudentAndSession(String studentId, String sessionId);

    // Број на несинхронизирани за прогрес барот
    @Query("SELECT COUNT(*) FROM attendance_records WHERE is_synced = 0")
    int getUnsyncedCount();
}