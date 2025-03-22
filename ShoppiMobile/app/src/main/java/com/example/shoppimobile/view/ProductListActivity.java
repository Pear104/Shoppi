package com.example.shoppimobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.ProductAdapter;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListActivity";
    private ProductRepository productRepository;
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private RecyclerView productRecycler;
    private String query;
    private String category;
    private String categoryId;
    private EditText searchEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_list);
        getSupportActionBar().hide();

        TextView searchTitle = findViewById(R.id.search_title);
        searchEditText = findViewById(R.id.search_edit_text);
        query = getIntent().getStringExtra("QUERY");
        category = getIntent().getStringExtra("CATEGORY");
        categoryId = getIntent().getStringExtra("CATEGORY_ID");
        if (category != null) {
            searchTitle.setText("Result for: " + category);
        } else {
            searchTitle.setText("Result for: " + query);
        }

        setup();
        loadProducts();

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Handle search action here
                query = searchEditText.getText().toString();
                searchTitle.setText("Result for: " + query);
                loadProducts();

                return true; // Consume the event
            }
            return false;
        });
    }

    private void setup() {
        productRepository = new ProductRepository(this);
        productRecycler = findViewById(R.id.products_recycler);
        productAdapter = new ProductAdapter(this, productList, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", ((Product) productList.get(position)).getId());
                startActivity(intent);
            }
        });
        productRecycler.setAdapter(productAdapter);

    }

    private void loadProducts() {
        // Load products from API
        Call<List<Product>> call = productRepository.getProducts(categoryId, query);
        Log.d("API", "Calling URL: " + call.request().url());

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                Log.d("API", response.message());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API", "Zo " + response.body().size());

                    List<Product> products = response.body();
                    productList.clear();
                    productList.addAll(products);
                    productAdapter.notifyDataSetChanged();
                    Log.d("API", "Xong " + response.message());

                } else {
                    Toast.makeText(ProductListActivity.this, "The shop currently has no products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.d("API", t.getMessage());
                Toast.makeText(ProductListActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}