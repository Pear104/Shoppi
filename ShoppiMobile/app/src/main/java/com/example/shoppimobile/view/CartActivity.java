package com.example.shoppimobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.CartAdapter;
import com.example.shoppimobile.model.CartItem;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private CartRepository cartRepository;
    private TextView totalItems;
    private TextView totalAmount;
    private Button checkoutButton;
    private String TAG = "API";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        cartRepository = new CartRepository(this);

        // Initialize views
        recyclerView = findViewById(R.id.cart_items_recycler_view);
        totalItems = findViewById(R.id.total_items);
        totalAmount = findViewById(R.id.total_amount);
        checkoutButton = findViewById(R.id.checkout_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load cart items
        loadCartItems();

        // Setup checkout button
        checkoutButton.setOnClickListener(v -> {
            if (adapter.getItemCount() > 0) {
                Intent intent = new Intent(CartActivity.this, ShippingActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCartItems() {
        try {
            Call<List<CartItem>> call = cartRepository.getCartItems();
            call.enqueue(new Callback<List<CartItem>>() {
                @Override
                public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.setItems(response.body());
                        updateCartSummary();
                    } else {
                        Toast.makeText(CartActivity.this,
                                "Failed to load cart items",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<CartItem>> call, Throwable t) {
                    Toast.makeText(CartActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(CartActivity.this, "System error", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartSummary() {
        List<CartItem> items = adapter.getItems();
        int totalItemCount = 0;
        double totalAmountValue = 0.0;

        for (CartItem item : items) {
            totalItemCount += item.getQuantity();
            totalAmountValue += item.getProduct().getPrice() * item.getQuantity();
        }

        totalItems.setText(String.valueOf(totalItemCount));
        totalAmount.setText(String.format("%.2f", totalAmountValue)+" PI");
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        Call<CartItem> call = cartRepository.updateCartItem(item.getProduct().getId(), new CartItem(newQuantity));
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadCartItems();
                } else {
                    Toast.makeText(CartActivity.this,
                            "Failed to update quantity",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                Toast.makeText(CartActivity.this,
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteItem(CartItem item) {
        Call<Void> call = cartRepository.deleteCartItem(item.getProduct().getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadCartItems();
                } else {
                    Toast.makeText(CartActivity.this,
                            "Failed to delete item",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CartActivity.this,
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Product product = adapter.getItems().get(position).getProduct();
        Intent intent = new Intent(CartActivity.this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }
} 