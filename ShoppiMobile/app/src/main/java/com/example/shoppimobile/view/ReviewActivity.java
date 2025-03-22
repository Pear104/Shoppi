package com.example.shoppimobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.shoppimobile.R;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.model.Review;
import com.example.shoppimobile.repository.ProductRepository;
import com.example.shoppimobile.repository.ReviewRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {
    private static final String TAG = "ReviewActivity";

    // UI components
    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice;
    private RatingBar rbRating;
    private TextInputEditText etReview;
    private Button btnSubmitReview, btnUpdateReview, btnCancel;
    private ProgressBar progressBar;

    // Data
    private String productId, orderItemId, reviewId;
    private Product product;
    private Review existingReview;
    private boolean isUpdate = false;

    // Repositories
    private ProductRepository productRepository;
    private ReviewRepository reviewRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize repositories
        productRepository = new ProductRepository(this);
        reviewRepository = new ReviewRepository(this);

        // Initialize UI components
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        rbRating = findViewById(R.id.rb_rating);
        etReview = findViewById(R.id.et_review);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        btnUpdateReview = findViewById(R.id.btn_update_review);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progressBar);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("productId");
            orderItemId = intent.getStringExtra("orderItemId");
            reviewId = intent.getStringExtra("reviewId");
            isUpdate = intent.getBooleanExtra("isUpdate", false);
        }

        // Set up button listeners
        btnSubmitReview.setOnClickListener(v -> submitReview());
        btnUpdateReview.setOnClickListener(v -> updateReview());
        btnCancel.setOnClickListener(v -> finish());

        // Load product details
        loadProductDetails();

        // Check if update mode
        Log.d(TAG, "isUpdate: " + reviewId);
        Log.d(TAG, "isUpdate: " + isUpdate);
        if (isUpdate) {
            btnSubmitReview.setVisibility(View.GONE);
            btnUpdateReview.setVisibility(View.VISIBLE);
            loadExistingReview();
        } else {
            btnSubmitReview.setVisibility(View.VISIBLE);
            btnUpdateReview.setVisibility(View.GONE);
        }
    }

    private void loadProductDetails() {
        showLoading(true);

        productRepository.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    displayProductDetails();
                } else {
                    Toast.makeText(ReviewActivity.this,
                            "Failed to load product details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ReviewActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void displayProductDetails() {
        if (product != null) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format("%.2f PI", product.getPrice()));

            // Load product image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(product.getImageUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image))
                        .into(ivProductImage);
            }
        }
    }

    private void loadExistingReview() {
        showLoading(true);
        Log.d(TAG, "loadExistingReview: " + orderItemId);
        reviewRepository.getReview(orderItemId).enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    existingReview = response.body();
                    displayExistingReview();
                } else {
                    // No existing review, or error
                    Log.e(TAG, "Error loading review: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ReviewActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void displayExistingReview() {
        if (existingReview != null) {
            rbRating.setRating(existingReview.getRating());
            etReview.setText(existingReview.getComment());
        }
    }

    private void submitReview() {
        if (!validateInput()) return;

        showLoading(true);

        // Create review object
        Review review = new Review(productId, orderItemId, (int) rbRating.getRating(), etReview.getText().toString().trim());
        Log.d(TAG, "Review: " + review.toString());


        Call<Review> call = reviewRepository.createReview(review);
        call.enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ReviewActivity.this,
                            "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ReviewActivity.this,
                            "Failed to submit review: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ReviewActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }

    private void updateReview() {
        if (!validateInput()) return;

        showLoading(true);

        // Update review object
        existingReview.setRating((int) rbRating.getRating());
        existingReview.setComment(etReview.getText().toString().trim());
        existingReview.setUpdatedAt(new Date());

        // Get user ID token
        reviewRepository.updateReview(existingReview.getId(), existingReview)
                .enqueue(new Callback<Review>() {
                    @Override
                    public void onResponse(Call<Review> call, Response<Review> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ReviewActivity.this,
                                    "Review updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ReviewActivity.this,
                                    "Failed to update review: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Review> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(ReviewActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Network error: " + t.getMessage());
                    }
                });

    }

    private boolean validateInput() {
        if (rbRating.getRating() < 1) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        String comment = etReview.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please provide a review comment", Toast.LENGTH_SHORT).show();
            etReview.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmitReview.setEnabled(!isLoading);
        btnUpdateReview.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
