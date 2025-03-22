package com.example.shoppimobile.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppimobile.R;
import com.example.shoppimobile.model.AuthResponseDTO;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.model.SignInRequestDTO;
import com.example.shoppimobile.model.SignUpRequestDTO;
import com.example.shoppimobile.repository.AuthRepository;
import com.example.shoppimobile.utils.JwtUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    EditText emailField;
    EditText passwordField;
    Button signInButton;
    TextView signUpButton;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        EdgeToEdge.enable(this);
        authRepository = new AuthRepository(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

//        checkIsUserLoggedIn();
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        signInButton.setOnClickListener(v -> signIn());

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void signIn() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this,
                    "Please enter blank fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(SignInActivity.this,
                    "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<AuthResponseDTO> call = authRepository.signIn(new SignInRequestDTO(email, password));

        call.enqueue(new Callback<AuthResponseDTO>() {
            @Override
            public void onFailure(Call<AuthResponseDTO> call, Throwable t) {
                Log.d("API", t.getMessage());
            }

            @Override
            public void onResponse(Call<AuthResponseDTO> call, Response<AuthResponseDTO> response) {
                if (response.isSuccessful()) {
                    AuthResponseDTO data = response.body();
                    saveData(data);
                    Toast.makeText(SignInActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();  // Optional
                } else {
                    if (response.code() == 403) {
                        Toast.makeText(SignInActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void checkIsUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", null);

        if (!JwtUtils.isJwtExpired(accessToken)) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void saveData(AuthResponseDTO data) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", data.getToken());
        editor.putString("name", data.getUser().getName());
        editor.putString("email", data.getUser().getEmail());
        editor.putString("role", data.getUser().getRole());
        editor.putString("id", data.getUser().getId());
        editor.apply();
    }
}