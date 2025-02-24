package com.example.shoppi.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppi.MainActivity;
import com.example.shoppi.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String mVerificationId;
    EditText phoneNumberField;
    Button sendOtpButton;
    EditText otpField;
    View otpForm;
    Button verifyButton;
    Button resendButton;
    private ProgressBar loadingSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();

        phoneNumberField = findViewById(R.id.phoneNumberField);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        otpField = findViewById(R.id.otpField);
        otpForm = findViewById(R.id.otpForm);
        verifyButton = findViewById(R.id.verifyButton);
        resendButton = findViewById(R.id.resendButton);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        sendOtpButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberField.getText().toString();
            loadingSpinner.setVisibility(View.VISIBLE);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Số điện thoại dạng +84xxxxxxxxxx
                    60,                 // Timeout (giây)
                    TimeUnit.SECONDS,
                    this,               // Activity
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            signInWithPhoneAuthCredential(credential);
                        }
                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Log.d("OTP",  e.getMessage().toString());
                            Toast.makeText(OtpActivity.this, "Xác thực thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            Toast.makeText(OtpActivity.this, "Mã OTP đã gửi!", Toast.LENGTH_SHORT).show();
                            mVerificationId = verificationId;
                            loadingSpinner.setVisibility(View.GONE);
                            phoneNumberField.setVisibility(View.GONE);
                            sendOtpButton.setVisibility(View.GONE);
                            otpField.setVisibility(View.VISIBLE);
                            otpForm.setVisibility(View.VISIBLE);
                            // Hiển thị trường nhập OTP và nút xác nhận
                        }
                    });
        });

        verifyButton.setOnClickListener(v -> {
            String otp = otpField.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
            signInWithPhoneAuthCredential(credential);
        });

        resendButton.setOnClickListener(v -> {
            sendOtpButton.performClick();
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
//                        Toast.makeText(OtpActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OtpActivity.this, "Xác thực thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}