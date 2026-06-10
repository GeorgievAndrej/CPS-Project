package com.cps.teacherapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.cps.teacherapp.data.model.AttendanceRecord;

// @Database ги наведува сите Entity класи и верзијата
// ЗОШТО version=1? Ако подоцна менуваме шема, зголемуваме на 2, 3...
// Room ќе знае дека треба миграција
@Database(entities = {AttendanceRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Апстрактен метод — Room го имплементира
    public abstract AttendanceDao attendanceDao();

    // Singleton инстанца
    // ЗОШТО volatile? За thread-safety во multi-threaded средина
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                // Double-check locking pattern
                // ЗОШТО двапати проверуваме? Ако две нишки влезат истовремено,
                // само едната ќе создаде инстанца
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "cps_teacher_db"  // Ова е името на .db фајлот
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}