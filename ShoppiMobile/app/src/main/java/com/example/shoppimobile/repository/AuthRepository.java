package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.AuthResponseDTO;
import com.example.shoppimobile.model.SignInRequestDTO;
import com.example.shoppimobile.model.SignUpRequestDTO;
import com.example.shoppimobile.service.AuthService;

import retrofit2.Call;
import retrofit2.Retrofit;

public class AuthRepository {
    private AuthService authService;

    public AuthRepository(Context context) {
        Retrofit retrofit = APIClient.getClient(context);
        authService = retrofit.create(AuthService.class);
    }

    public Call<AuthResponseDTO> signIn(SignInRequestDTO signInRequestDTO) {
        return authService.signIn(signInRequestDTO);
    }

    public Call<AuthResponseDTO> signUp(SignUpRequestDTO signUpRequestDTO){
        return authService.signUp(signUpRequestDTO);
    }
}
