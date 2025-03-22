package com.example.shoppimobile.service;

import com.example.shoppimobile.model.SignInRequestDTO;
import com.example.shoppimobile.model.AuthResponseDTO;
import com.example.shoppimobile.model.SignUpRequestDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("auth/login")
    Call<AuthResponseDTO> signIn(@Body SignInRequestDTO signInRequestDTO);

    @POST("auth/register")
    Call<AuthResponseDTO> signUp(@Body SignUpRequestDTO signUpRequestDTO);
}
