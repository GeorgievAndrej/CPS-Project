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
