package com.example.shoppimobile.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.CategoryAdapter;
import com.example.shoppimobile.adapter.ProductAdapter;
import com.example.shoppimobile.model.CartItem;
import com.example.shoppimobile.model.Category;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.repository.CartRepository;
import com.example.shoppimobile.repository.CategoryRepository;
import com.example.shoppimobile.repository.ProductRepository;
import com.example.shoppimobile.utils.JwtUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ArrayList productList = new ArrayList<Product>();
    private List<Category> categoryList = new ArrayList<Category>();
    private ProductRepository productRepository;
    private ProductAdapter productAdapter;
    private CategoryRepository categoryRepository;
    private CartRepository cartRepository;
    private CategoryAdapter categoryAdapter;
    private String TAG = "API";
    private EditText searchEditText;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        productRepository = new ProductRepository(this);
        categoryRepository = new CategoryRepository(this);
        cartRepository = new CartRepository(this);
        searchEditText = findViewById(R.id.search_edit_text);
        welcomeText = findViewById(R.id.welcomeText);

        checkIsUserLoggedIn();

        setupAdapter();
        setupSearch();
        setupOnClickListeners();

        // Load products from API
        loadCategories();
        loadProducts();
        loadCartItems();
    }

    private void setupSearch() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Handle search action
                String query = searchEditText.getText().toString();
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                intent.putExtra("QUERY", query);
                startActivity(intent);

                return true;
            }
            return false;
        });
    }

    private void setupOnClickListeners() {
        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener((View v) -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        ImageView cartIcon = findViewById(R.id.cartIcon);
        cartIcon.setOnClickListener((View v) -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ImageView chatIcon = findViewById(R.id.chatIcon);
        chatIcon.setOnClickListener((View v) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String role = sharedPreferences.getString("role", "");
            if (role.toLowerCase().equals("admin")) {
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
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
                        TextView cartBadge = findViewById(R.id.cartBadge);
                        cartBadge.setText(String.valueOf(response.body().size()));
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Failed to load cart items",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<CartItem>> call, Throwable t) {
                    Toast.makeText(MainActivity.this,
                            "Network error. Please check your connection.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d("API", "zo: error" + e.getMessage());
        }
    }

    private void loadProducts() {
        // Load products from API
        Call<List<Product>> call = productRepository.getProducts("","");

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    productList.addAll(products);
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "The shop currently has no products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.d("API", t.getMessage());
                Toast.makeText(MainActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        Call<List<Category>> call = categoryRepository.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                Log.d("API", response.message());
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    categoryList.addAll(categories);
                    categoryAdapter.notifyDataSetChanged();
                    Log.d("API", "Xong " + response.message());

                } else {
                    Toast.makeText(MainActivity.this, "The shop currently has no products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.d("API", t.getMessage());
                Toast.makeText(MainActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupAdapter() {
        productAdapter = new ProductAdapter(this, productList, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", ((Product) productList.get(position)).getId());
                startActivity(intent);
            }
        });

        categoryAdapter = new CategoryAdapter(this, categoryList, new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Category category = categoryList.get(position);
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                intent.putExtra("CATEGORY", category.getName());
                intent.putExtra("CATEGORY_ID", category.getId());
                startActivity(intent);
            }

        });

        RecyclerView productRecycler = findViewById(R.id.products_recycler);
        RecyclerView categoryRecycler = findViewById(R.id.categories_recycler);

        // Set up RecyclerView
        productRecycler.setAdapter(productAdapter);
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void checkIsUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", null);
        welcomeText.setText("Hello, " + sharedPreferences.getString("name", ""));

        if (JwtUtils.isJwtExpired(accessToken)) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
}

// [x] Có Database, API
// [x] Có signup, login, google 
// [x] Có liệt kê danh sách sản phẩm
// [x] Có màn hình chi tiết sản phẩm
// [x] Có màn hình cart
// [x] Có màn hình thanh toán
// [x] Có badge icon hiển thị số lượng sản phẩm trong cart
// [x] Có hiển thị bản đồ
// [x] Có chức năng chat
// [x] Có xài MVC, MVP, MVVM