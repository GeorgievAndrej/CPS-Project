package com.cps.teacherapp.network;

import com.cps.teacherapp.data.model.AttendanceRecord;
import com.cps.teacherapp.network.models.LoginRequest;
import com.cps.teacherapp.network.models.LoginResponse;
import com.cps.teacherapp.network.models.SyncResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/login.php")
    Call<LoginResponse> login(@Body LoginRequest request);

    /*
     * ЗОШТО syncSingleRecord наместо bulk?
     *
     * AttendanceRepository.syncUnsynced() итерира еден по еден со .execute()
     * (блокирачки повик во позадинска нишка) за да го прати прогресот
     * back до UI-от (onProgress callback).
     *
     * Backend-от го прифаќа ист payload — обвиткан во { "records": [...] }
     * со еден елемент. Ова е поедноставно од имплементирање на посебен
     * bulk endpoint со различен response shape.
     */
    @POST("api/attendance.php")
    Call<SyncResponse> syncSingleRecord(
            @Header("Authorization") String bearerToken,
            @Body SyncRequestBody body
    );

    @GET("api/attendance.php")
    Call<List<AttendanceRecord>> getAttendance(
            @Header("Authorization") String bearerToken
    );
}
