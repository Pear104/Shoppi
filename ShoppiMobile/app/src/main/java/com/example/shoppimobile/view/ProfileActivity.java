package com.example.shoppimobile.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoppimobile.R;
import com.example.shoppimobile.model.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewName, textViewEmail, textViewRole, textViewId;
    private Button buttonViewOrders,buttonViewDashboard,buttonManage, buttonLogout;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewRole = findViewById(R.id.textViewRole);
        textViewId = findViewById(R.id.textViewId);
        buttonViewOrders = findViewById(R.id.buttonViewOrders);
        buttonViewDashboard = findViewById(R.id.buttonViewDashboard);
        buttonManage = findViewById(R.id.buttonManage);
        buttonLogout = findViewById(R.id.buttonLogout);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", "");
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String role = sharedPreferences.getString("role", "");
        String id = sharedPreferences.getString("id", "");

        User.Role userRole = User.Role.CUSTOMER;
        Log.d(TAG, role);
        if (role.toLowerCase().equals("admin")) {
            userRole = User.Role.ADMIN;
            buttonManage.setVisibility(View.VISIBLE);
            buttonViewDashboard.setVisibility(View.VISIBLE);
        }

        User user = new User(id, email, name, userRole);

        textViewName.setText(user.getName());
        textViewEmail.setText(user.getEmail());
        textViewRole.setText(user.getRole());
        textViewId.setText("ID: " + user.getId());

        buttonViewDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        buttonManage.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ManageProductActivity.class);
            startActivity(intent);
        });

        buttonViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            startActivity(intent);
        });
    }

}