package com.cps.teacherapp.network;

import com.cps.teacherapp.network.models.LoginRequest;
import com.cps.teacherapp.network.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/login.php")
    Call<LoginResponse> login(@Body LoginRequest request);
}