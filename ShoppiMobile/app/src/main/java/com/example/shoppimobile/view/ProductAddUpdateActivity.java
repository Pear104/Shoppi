package com.example.shoppimobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.shoppimobile.R;
import com.example.shoppimobile.model.Category;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.repository.CategoryRepository;
import com.example.shoppimobile.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAddUpdateActivity extends AppCompatActivity {

    private static final String TAG = "ProductAddUpdateActivity";

    // UI components
    private EditText nameEditText, descriptionEditText, priceEditText, stockEditText, imageUrlEditText;
    private Spinner categorySpinner;
    private Button submitButton, previewImageButton;
    private ImageView previewImageView;
    private TextView titleTextView;

    // Data
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private List<Category> categories = new ArrayList<>();
    private Product currentProduct;
    private boolean isEditMode = false;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_add_update);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize repositories
        productRepository = new ProductRepository(this);
        categoryRepository = new CategoryRepository(this);

        // Initialize UI components
        nameEditText = findViewById(R.id.product_name_input);
        descriptionEditText = findViewById(R.id.product_description_input);
        priceEditText = findViewById(R.id.product_price_input);
        stockEditText = findViewById(R.id.product_stock_input);
        imageUrlEditText = findViewById(R.id.product_image_url_input);
        categorySpinner = findViewById(R.id.category_spinner);
        submitButton = findViewById(R.id.submit_button);
        previewImageButton = findViewById(R.id.preview_image_button);
        previewImageView = findViewById(R.id.preview_image_view);
        titleTextView = findViewById(R.id.activity_title);

        // Check if in edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PRODUCT_ID")) {
            isEditMode = true;
            productId = intent.getStringExtra("PRODUCT_ID");
            titleTextView.setText("Update Product");
            submitButton.setText("Update");
            loadProductDetails(productId);
        } else {
            titleTextView.setText("Add New Product");
            submitButton.setText("Create");
        }

        // Load categories for spinner
        loadCategories();

        // Set up button listeners
        submitButton.setOnClickListener(v -> submitProduct());
        previewImageButton.setOnClickListener(v -> previewImage());
    }

    private void loadCategories() {
        Call<List<Category>> call = categoryRepository.getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    setupCategorySpinner();
                } else {
                    Toast.makeText(ProductAddUpdateActivity.this,
                            "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(ProductAddUpdateActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load categories: " + t.getMessage());
            }
        });
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // If in edit mode, select the current category
        if (isEditMode && currentProduct != null && currentProduct.getCategoryId() != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId().equals(currentProduct.getCategoryId())) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void loadProductDetails(String productId) {
        Call<Product> call = productRepository.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    populateFormWithProductData();
                } else {
                    Toast.makeText(ProductAddUpdateActivity.this,
                            "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductAddUpdateActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load product: " + t.getMessage());
            }
        });
    }

    private void populateFormWithProductData() {
        nameEditText.setText(currentProduct.getName());
        descriptionEditText.setText(currentProduct.getDescription());
        priceEditText.setText(String.valueOf(currentProduct.getPrice()));
        stockEditText.setText(String.valueOf(currentProduct.getStock()));
        imageUrlEditText.setText(currentProduct.getImageUrl());

        // Load and show the image
        if (!TextUtils.isEmpty(currentProduct.getImageUrl())) {
            Glide.with(this)
                    .load(currentProduct.getImageUrl())
                    .into(previewImageView);
            previewImageView.setVisibility(View.VISIBLE);
        }

        // Category selection will be handled in setupCategorySpinner()
    }

    private void previewImage() {
        String imageUrl = imageUrlEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(previewImageView);
            previewImageView.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Please enter an image URL first", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitProduct() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from form
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        float price = Float.parseFloat(priceEditText.getText().toString().trim());
        int stock = Integer.parseInt(stockEditText.getText().toString().trim());
        String imageUrl = imageUrlEditText.getText().toString().trim();
        String categoryId = getCategoryIdFromSelection();

        // Create or update product
        Product product = new Product();
        if (isEditMode) {
            product.setId(productId);
        }
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setCategoryId(categoryId);

        if (isEditMode) {
            updateProduct(product);
        } else {
            createProduct(product);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(descriptionEditText.getText())) {
            descriptionEditText.setError("Description is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(priceEditText.getText())) {
            priceEditText.setError("Price is required");
            isValid = false;
        } else {
            try {
                float price = Float.parseFloat(priceEditText.getText().toString());
                if (price <= 0) {
                    priceEditText.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceEditText.setError("Invalid price format");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(stockEditText.getText())) {
            stockEditText.setError("Stock is required");
            isValid = false;
        } else {
            try {
                int stock = Integer.parseInt(stockEditText.getText().toString());
                if (stock < 0) {
                    stockEditText.setError("Stock cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                stockEditText.setError("Invalid stock format");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(imageUrlEditText.getText())) {
            imageUrlEditText.setError("Image URL is required");
            isValid = false;
        }

        if (categorySpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private String getCategoryIdFromSelection() {
        int position = categorySpinner.getSelectedItemPosition();
        if (position >= 0 && position < categories.size()) {
            return categories.get(position).getId();
        }
        return null;
    }

    private void createProduct(Product product) {
        Call<Product> call = productRepository.createProduct(product);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductAddUpdateActivity.this,
                            "Product created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductAddUpdateActivity.this,
                            "Failed to create product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductAddUpdateActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to create product: " + t.getMessage());
            }
        });
    }

    private void updateProduct(Product product) {
        Call<Product> call = productRepository.updateProduct(productId, product);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductAddUpdateActivity.this,
                            "Product updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductAddUpdateActivity.this,
                            "Failed to update product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductAddUpdateActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to update product: " + t.getMessage());
            }
        });
    }
}