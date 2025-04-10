package com.example.shoppimobile.view;

import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.ReviewAdapter;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.model.Review;
import com.example.shoppimobile.model.User;
import com.example.shoppimobile.repository.ProductRepository;
import com.example.shoppimobile.model.CartItem;
import com.example.shoppimobile.repository.CartRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView productName, productPrice, productDescription, quantityText, stockText;
    private ImageButton btnIncrease, btnDecrease;
    private ImageView productImage;
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private Product product = new Product();
    private List<Review> reviews = new ArrayList<>();
    private int quantity = 1;
    private ReviewAdapter reviewAdapter;
    private String TAG = "ProductDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);

        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        productDescription = findViewById(R.id.product_description);
        stockText = findViewById(R.id.stock_count);
        RecyclerView reviewRecyclerView = findViewById(R.id.reviews_recycler_view);

        reviewAdapter = new ReviewAdapter(this, reviews);
        reviewRecyclerView.setAdapter(reviewAdapter);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            product.setId(intent.getStringExtra("PRODUCT_ID"));
            loadProductDetails(product.getId());
            updatePrice();
            Log.d(TAG, "onCreate: Review first" + reviews.size());
        }

        // Initialize quantity selector views
        quantityText = findViewById(R.id.quantity_text);
        btnIncrease = findViewById(R.id.btn_increase);
        btnDecrease = findViewById(R.id.btn_decrease);

        // Set up quantity selector listeners
        btnIncrease.setOnClickListener(v -> {
            if (quantity < product.getStock()) {
                quantity++;
                updateQuantityAndPrice();
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPrice();
            }
        });

        // Set up the add to cart button
        Button addToCartButton = findViewById(R.id.fab_add_to_cart);
        addToCartButton.setOnClickListener(v -> addToCart());
    }

    private void updateQuantityAndPrice() {
        quantityText.setText(String.valueOf(quantity));
        updatePrice();
    }

    private void updatePrice() {
        TextView priceView = findViewById(R.id.product_price);
        double totalPrice = product.getPrice() * quantity;
        priceView.setText(String.format("%.2f PI", totalPrice));
    }

    private void loadProductDetails(String productId) {
        Call<Product> call = productRepository.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    // Update the UI with the product details
                    productName.setText(product.getName());
                    productPrice.setText(product.getPrice() + " PI");
                    productDescription.setText(product.getDescription());
                    stockText.setText("(Stock: " + product.getStock() + ")");

                    // Load image
                    Glide.with(ProductDetailActivity.this)
                            .load(product.getImageUrl())
                            .into(productImage);

                    // Load reviews - first clear the current list
                    reviews.clear();

                    // Log the number of reviews coming from the API
                    Log.d(TAG, "Number of reviews from API: " + product.getReviews().size());

                    // Add the reviews from the product
                    if (product.getReviews() != null) {
                        reviews.addAll(product.getReviews());
                    }
                    // Update the adapter with the new reviews
                    reviewAdapter.setReviews(reviews);
                    reviewAdapter.notifyDataSetChanged();

                    // Show/hide no reviews message
                    View noReviewsText = findViewById(R.id.no_reviews_text);
                    if (reviews.isEmpty()) {
                        noReviewsText.setVisibility(View.VISIBLE);
                        findViewById(R.id.reviews_recycler_view).setVisibility(View.GONE);
                    } else {
                        noReviewsText.setVisibility(View.GONE);
                        findViewById(R.id.reviews_recycler_view).setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            "Failed to load product details",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart() {
        Call<CartItem> call = cartRepository.addToCart(new CartItem(quantity, product.getId()));
        call.enqueue(new Callback<CartItem>() {

            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            "Failed to load cart items",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this,
                        "Failed to load cart items",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}