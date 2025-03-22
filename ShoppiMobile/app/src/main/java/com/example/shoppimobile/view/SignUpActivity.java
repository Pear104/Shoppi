package com.example.shoppimobile.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoppimobile.R;
import com.example.shoppimobile.model.AuthResponseDTO;
import com.example.shoppimobile.model.SignInRequestDTO;
import com.example.shoppimobile.model.SignUpRequestDTO;
import com.example.shoppimobile.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    EditText emailField, passwordField,nameField, confirmPasswordField;
    TextView signInButton;
    Button signUpButton;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        authRepository = new AuthRepository(this);
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);

        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> signUp());

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }

    private void signUp() {
        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(SignUpActivity.this,
                    "Please enter blank fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this,
                    "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!confirmPassword.equals(password)) {
            Toast.makeText(SignUpActivity.this,
                    "Password doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<AuthResponseDTO> call = authRepository.signUp(new SignUpRequestDTO(email, password, name));

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
                    Toast.makeText(SignUpActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();  // Optional
                } else {
                    if (response.code() == 403) {
                        Toast.makeText(SignUpActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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