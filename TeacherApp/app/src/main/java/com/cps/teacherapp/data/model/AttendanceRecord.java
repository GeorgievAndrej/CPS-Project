package com.cps.teacherapp.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "attendance_records")
public class AttendanceRecord {


    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "student_id")
    @SerializedName("student_id")
    public String studentId;

    @ColumnInfo(name = "student_name")
    @SerializedName("student_name")
    public String studentName;

    @ColumnInfo(name = "course_name")
    @SerializedName("course_name")
    public String courseName;

    @ColumnInfo(name = "tapped_at")
    @SerializedName("tapped_at")
    public long tappedAt;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @ColumnInfo(name = "session_id")
    @SerializedName("session_id")
    public String sessionId;

    public AttendanceRecord(String studentId, String studentName,
                            String courseName, String sessionId) {
        this.studentId   = studentId;
        this.studentName = studentName;
        this.courseName  = courseName;
        this.sessionId   = sessionId;
        this.tappedAt    = System.currentTimeMillis();
        this.isSynced    = false;
    }
}