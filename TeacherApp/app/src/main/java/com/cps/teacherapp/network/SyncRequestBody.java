package com.cps.teacherapp.network;

import com.cps.teacherapp.data.model.AttendanceRecord;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SyncRequestBody {

    @SerializedName("records")
    public final List<Map<String, Object>> records;

    public SyncRequestBody(AttendanceRecord record, int courseId) {
        Map<String, Object> item = new HashMap<>();

        item.put("student_external_id", record.studentId);
        item.put("student_name",        record.studentName);

        // TODO: Кога ќе се имплементира избор на курс во UI, овде се праќа вистинскиот ID.
        item.put("course_id",  courseId);

        item.put("tapped_at",  formatTimestamp(record.tappedAt));
        item.put("session_id", record.sessionId);

        this.records = Collections.singletonList(item);
    }

    private static String formatTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
