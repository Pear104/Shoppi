package com.example.shoppimobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoppimobile.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentResultActivity extends AppCompatActivity {

    private TextView resultTitle, resultMessage;
    private Button btnViewOrder, btnContinueShopping;
    private boolean isSuccessful;
    private String orderId;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        isSuccessful = getIntent().getBooleanExtra("EXTRA_PAYMENT_SUCCESSFUL", false);
        orderId = getIntent().getStringExtra("ORDER_ID");
        setup();

        setupButtons(isSuccessful, orderId);
    }

    private void setup() {
        findViewById(R.id.result_icon).setBackgroundResource(
                isSuccessful ? R.drawable.ic_payment_success : R.drawable.ic_payment_failed);

        resultTitle = findViewById(R.id.result_title);
        resultMessage = findViewById(R.id.result_message);
        btnViewOrder = findViewById(R.id.btn_view_order);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);

        if (isSuccessful) {
            resultTitle.setText("Payment Successful!");
            resultMessage.setText("Your order has been placed successfully. You will receive a confirmation email shortly.");
        } else {
            resultTitle.setText("Payment Failed");
            resultMessage.setText("We were unable to process your payment. Please try again or use a different payment method.");
        }

    }

    private void setupButtons(boolean isSuccessful, String orderId) {
        if (isSuccessful) {
            btnViewOrder.setText("View Order Details");
            btnViewOrder.setOnClickListener(v -> {
                Intent intent = new Intent(PaymentResultActivity.this, DetailOrderActivity.class);
                intent.putExtra("ORDER_ID", orderId);
                startActivity(intent);
                finish();
            });
        } else {
            btnViewOrder.setText("Try Again");
            btnViewOrder.setOnClickListener(v -> finish());
        }

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}