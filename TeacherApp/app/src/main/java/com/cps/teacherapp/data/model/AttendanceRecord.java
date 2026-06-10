package com.cps.teacherapp.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

// @Entity му кажува на Room: "оваа класа е табела во базата"
// ЗОШТО tableName? За да го контролираме точното име во SQLite
@Entity(tableName = "attendance_records")
public class AttendanceRecord {

    // AUTO_INCREMENT еквивалент — Room сам доделува ID
    @PrimaryKey(autoGenerate = true)
    public int id;

    // @ColumnInfo = колона во табелата
    // @SerializedName = JSON клуч за Retrofit (backend очекува "student_id")
    @ColumnInfo(name = "student_id")
    @SerializedName("student_id")
    public String studentId;

    @ColumnInfo(name = "student_name")
    @SerializedName("student_name")
    public String studentName;

    @ColumnInfo(name = "course_name")
    @SerializedName("course_name")
    public String courseName;

    // Зачувуваме Unix timestamp (ms) — лесен за споредба и сортирање
    @ColumnInfo(name = "tapped_at")
    @SerializedName("tapped_at")
    public long tappedAt;

    // false = локален запис, уште не испратен до серверот
    // true  = синхронизиран
    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @ColumnInfo(name = "session_id")
    @SerializedName("session_id")
    public String sessionId;

    // Конструктор за нов запис (при NFC tap)
    public AttendanceRecord(String studentId, String studentName,
                            String courseName, String sessionId) {
        this.studentId   = studentId;
        this.studentName = studentName;
        this.courseName  = courseName;
        this.sessionId   = sessionId;
        this.tappedAt    = System.currentTimeMillis(); // моментален timestamp
        this.isSynced    = false; // секогаш почнува несинхронизиран
    }
}