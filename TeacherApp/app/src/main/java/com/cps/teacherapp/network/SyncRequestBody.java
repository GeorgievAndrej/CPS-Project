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

/**
 * Wrapper кој го претвора AttendanceRecord (локален Room модел)
 * во JSON структурата која ја очекува backend-от:
 *
 * {
 *   "records": [
 *     {
 *       "student_external_id": "...",   ← backend поле (не student_id)
 *       "student_name":        "...",
 *       "course_id":           1,       ← backend бара int ID (не course_name)
 *       "tapped_at":           "2024-01-15 10:30:00",
 *       "session_id":          "ABC123"
 *     }
 *   ]
 * }
 *
 * ЗОШТО посебна класа наместо директно SerializedName на AttendanceRecord?
 * AttendanceRecord е Room Entity — не сакаме мешање на DB и мрежни грижи.
 * Separation of Concerns: Room модел и мрежен payload се различни нешта.
 */
public class SyncRequestBody {

    @SerializedName("records")
    public final List<Map<String, Object>> records;

    public SyncRequestBody(AttendanceRecord record, int courseId) {
        Map<String, Object> item = new HashMap<>();

        // Backend очекува "student_external_id" — ова е student_id од NFC payload
        item.put("student_external_id", record.studentId);
        item.put("student_name",        record.studentName);

        // Backend очекува int course_id — за сега користиме 1 (default курс).
        // TODO: Кога ќе се имплементира избор на курс во UI, овде се праќа вистинскиот ID.
        item.put("course_id",  courseId);

        // Конвертирај Unix ms → MySQL DATETIME формат
        item.put("tapped_at",  formatTimestamp(record.tappedAt));
        item.put("session_id", record.sessionId);

        this.records = Collections.singletonList(item);
    }

    private static String formatTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
