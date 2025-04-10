package com.example.shoppimobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.ProductManageAdapter;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.repository.ProductRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageProductActivity extends AppCompatActivity implements ProductManageAdapter.OnProductClickListener {

    private static final String TAG = "ManageProductActivity";
    
    private RecyclerView recyclerViewProducts;
    private ProductManageAdapter adapter;
    private ProductRepository productRepository;
    private List<Product> productList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyView;
    private FloatingActionButton fabAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_product);
        
        // Initialize views
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Set up RecyclerView
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductManageAdapter(productList, this);
        recyclerViewProducts.setAdapter(adapter);
        
        // Initialize repository
        productRepository = new ProductRepository(this);
        
        // Set up FAB
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManageProductActivity.this, ProductAddUpdateActivity.class);
            startActivity(intent);
        });
        
        // Load products
        loadProducts();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload products when returning to this activity
        loadProducts();
    }
    
    private void loadProducts() {
        showLoading(true);
        
        Call<List<Product>> call = productRepository.getProducts("", "");
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    // Show empty view if no products
                    if (productList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ManageProductActivity.this, 
                            "Failed to load products", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ManageProductActivity.this, 
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load products: " + t.getMessage());
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Error loading products. Please try again.");
            }
        });
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewProducts.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerViewProducts.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onProductClick(Product product) {
        // Open ProductAddUpdateActivity with product details for editing
        Intent intent = new Intent(ManageProductActivity.this, ProductAddUpdateActivity.class);
        intent.putExtra("PRODUCT_ID", product.getId());
        startActivity(intent);
    }
}