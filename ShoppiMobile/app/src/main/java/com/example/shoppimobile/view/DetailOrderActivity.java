package com.example.shoppimobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.Api.CreateOrder;
import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.OrderItemAdapter;
import com.example.shoppimobile.model.CartItem;
import com.example.shoppimobile.model.Order;
import com.example.shoppimobile.model.OrderItem;
import com.example.shoppimobile.repository.OrderRepository;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class DetailOrderActivity extends AppCompatActivity {

    private static final String TAG = "OrderActivity";

    private TextView orderStatus,orderId, orderDate, phoneNumber, address;
    private RecyclerView orderItemsRecyclerView;
    private OrderItemAdapter orderItemAdapter;
    private TextView subtotal;
    private TextView shippingFee;
    private TextView total;
    private String ORDER_ID;
    private Button buttonCheckout;

    private OrderRepository orderRepository;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ZaloPaySDK.init(2553, Environment.SANDBOX);
        Intent intent = getIntent();
        ORDER_ID = intent.getStringExtra("ORDER_ID");

        // Initialize views
        orderStatus = findViewById(R.id.order_status);
        orderId = findViewById(R.id.order_id);
        orderDate = findViewById(R.id.order_date);
        phoneNumber = findViewById(R.id.phone_number);
        address = findViewById(R.id.address);
        orderItemsRecyclerView = findViewById(R.id.order_items_recycler_view);
        subtotal = findViewById(R.id.subtotal);
        shippingFee = findViewById(R.id.shipping_fee);
        total = findViewById(R.id.total);
        buttonCheckout = findViewById(R.id.checkout_button);

        // Set up RecyclerView
        orderItemAdapter = new OrderItemAdapter();
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemsRecyclerView.setAdapter(orderItemAdapter);

        // Initialize repository
        orderRepository = new OrderRepository(this);

        // Get order ID from intent
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId != null) {
            loadOrder(orderId);
        }

        buttonCheckout.setOnClickListener((View view) -> {
            CreateOrder orderApi = new CreateOrder();
            Call<Order> call = orderRepository.updateOrder(orderId, new Order("COMPLETED"));
            call.enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {}

                @Override
                public void onFailure(Call<Order> call, Throwable t) {}
            });
            Intent intent1 = new Intent(DetailOrderActivity.this, PaymentResultActivity.class);
            intent1.putExtra("EXTRA_PAYMENT_SUCCESSFUL", true);
            startActivity(intent1);
//            try {
//                String totalString = String.format("%.0f", order.getTotalAmount() * 1000);
//                JSONObject data = orderApi.createOrder(totalString);
//                String code = data.getString("return_code");
//                if (code.equals("1")) {
//                    String token = data.getString("zp_trans_token");
//                    // Get zp_trans_token for move to zalopay app and payment
//                    ZaloPaySDK.getInstance().payOrder(this, token, "demozpdk://app", new PayOrderListener() {
//                        @Override
//                        public void onPaymentSucceeded(String s, String s1, String s2) {
//                            Log.d(TAG, "String s: " + s);
//                            Log.d(TAG, "String s1: " + s1);
//                            Log.d(TAG, "String s2: " + s2);
//                            Log.d(TAG, "String orderId: " + orderId);
//
//                            Call<Order> call = orderRepository.updateOrder(orderId, new Order("COMPLETED"));
//                            call.enqueue(new Callback<Order>() {
//                                @Override
//                                public void onResponse(Call<Order> call, Response<Order> response) {}
//
//                                @Override
//                                public void onFailure(Call<Order> call, Throwable t) {}
//                            });
//                            Intent intent = new Intent(DetailOrderActivity.this, PaymentResultActivity.class);
//                            intent.putExtra("EXTRA_PAYMENT_SUCCESSFUL", true);
//                            intent.putExtra("ORDER_ID", orderId);
//                            startActivity(intent);
//                        }
//
//                        @Override
//                        public void onPaymentCanceled(String s, String s1) {
//                            Intent intent1 = new Intent(DetailOrderActivity.this, PaymentResultActivity.class);
//                            intent.putExtra("EXTRA_PAYMENT_SUCCESSFUL", false);
//                            startActivity(intent1);
//                        }
//
//                        @Override
//                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
//                            Intent intent1 = new Intent(DetailOrderActivity.this, PaymentResultActivity.class);
//                            intent.putExtra("EXTRA_PAYMENT_SUCCESSFUL", false);
//                            startActivity(intent1);
//                        }
//                    });
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                Log.d("Error: ", ex.getMessage());
//            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrder(ORDER_ID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void loadOrder(String orderId) {
        Call<Order> call = orderRepository.getOrder(ORDER_ID);
        call.enqueue(new retrofit2.Callback<Order>() {

            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    order = response.body();
                    displayOrder();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(DetailOrderActivity.this, "Failed to load order", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayOrder() {
        if (order == null) return;

        // Set order status
        orderStatus.setText(order.getStatus());

        // Set background color based on status
        int backgroundResId;
        switch (order.getStatus().toUpperCase()) {
            case "PROCESSING":
                backgroundResId = R.drawable.bg_status_processing;
                break;
            case "SHIPPED":
                backgroundResId = R.drawable.bg_status_shipped;
                break;
            case "COMPLETED":
                backgroundResId = R.drawable.bg_status_delivered;
                break;
            case "CANCELLED":
                backgroundResId = R.drawable.bg_status_cancelled;
                break;
            default: // PENDING
                backgroundResId = R.drawable.bg_status_pending;
                break;
        }
        orderStatus.setBackgroundResource(backgroundResId);

        // Set button
        if (order.getStatus().equals("COMPLETED")) {
            buttonCheckout.setVisibility(View.GONE);
        }

        // Set order ID
        this.orderId.setText(order.getId());

        // Set order date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault());
        orderDate.setText(dateFormat.format(new Date(String.valueOf(order.getCreatedAt()))));

        // Set phone number
        phoneNumber.setText(order.getPhoneNumber());

        // Set address
        address.setText(order.getAddress());

        // Set order ID on adapter for navigating to review screen
        orderItemAdapter.setOrderId(order.getId());

        // Calculate and set subtotal
        double subtotalValue = 0.0;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                subtotalValue += item.getProduct().getPrice() * item.getQuantity();
            }

            // Set order items
            orderItemAdapter.setItems(order.getItems());
        }
        subtotal.setText(String.format("%.2f PI", subtotalValue));

        // Set shipping fee
        double shippingFeeValue = 0.0; // Fixed shipping fee for this example
        shippingFee.setText(String.format("%.2f PI", shippingFeeValue));

        // Calculate and set total
        double totalValue = subtotalValue + shippingFeeValue;
        total.setText(String.format("%.2f PI", totalValue));
    }

}