package com.example.shoppimobile.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private Button buttonViewOrders, buttonLogout;

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
        buttonLogout = findViewById(R.id.buttonLogout);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", "");
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String role = sharedPreferences.getString("role", "");
        String id = sharedPreferences.getString("id", "");

        User.Role userRole = User.Role.CUSTOMER;
        if (role.equals("admin")) {
            userRole = User.Role.ADMIN;
        }

        User user = new User(
                id,
                email,
                name,
                userRole
        );

        textViewName.setText(user.getName());
        textViewEmail.setText(user.getEmail());
        textViewRole.setText(user.getRole());
        textViewId.setText("ID: " + user.getId());


        Button viewCart = findViewById(R.id.buttonViewOrders);
        Button logout = findViewById(R.id.buttonLogout);

        viewCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderActivity.class);
            startActivity(intent);
        });

        logout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            startActivity(intent);
        });
    }

}