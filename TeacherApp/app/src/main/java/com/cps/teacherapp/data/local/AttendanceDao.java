package com.cps.teacherapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.cps.teacherapp.data.model.AttendanceRecord;
import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert
    void insert(AttendanceRecord record);

    @Query("SELECT * FROM attendance_records ORDER BY tapped_at DESC")
    LiveData<List<AttendanceRecord>> getAllRecords();

    @Query("SELECT * FROM attendance_records WHERE is_synced = 0")
    List<AttendanceRecord> getUnsyncedRecords();

    @Query("UPDATE attendance_records SET is_synced = 1 WHERE id = :id")
    void markAsSynced(int id);

    @Query("SELECT COUNT(*) FROM attendance_records WHERE student_id = :studentId " +
            "AND session_id = :sessionId")
    int countByStudentAndSession(String studentId, String sessionId);

    @Query("SELECT COUNT(*) FROM attendance_records WHERE is_synced = 0")
    int getUnsyncedCount();
}