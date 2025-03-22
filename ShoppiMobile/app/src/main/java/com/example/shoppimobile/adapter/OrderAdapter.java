package com.example.shoppimobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.Api.CreateOrder;
import com.example.shoppimobile.R;
import com.example.shoppimobile.model.Order;
import com.example.shoppimobile.view.DetailOrderActivity;
import com.example.shoppimobile.view.OrderActivity;
import com.example.shoppimobile.view.ShippingActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private Context context;
    private SimpleDateFormat dateFormat;

    public OrderAdapter(Context context) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderId;
        private TextView orderStatus;
        private TextView orderDate;
        private TextView orderItemsCount;
        private TextView orderTotal;
        private Button viewDetailsButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            orderStatus = itemView.findViewById(R.id.order_status);
            orderDate = itemView.findViewById(R.id.order_date);
            orderItemsCount = itemView.findViewById(R.id.order_items_count);
            orderTotal = itemView.findViewById(R.id.order_total);
            viewDetailsButton = itemView.findViewById(R.id.view_details_button);
        }

        public void bind(Order order) {
            // Set order ID
            orderId.setText("Order #" + order.getId());

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

            // Set order date
            orderDate.setText(dateFormat.format(new Date(String.valueOf(order.getCreatedAt()))));

            // Set items count
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            orderItemsCount.setText(itemCount + (itemCount == 1 ? " item" : " items"));

            // Set total amount
            orderTotal.setText(String.format("$%.2f", order.getTotalAmount()));

            // Set click listener for view details button
            viewDetailsButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailOrderActivity.class);
                intent.putExtra("ORDER_ID", order.getId());
                context.startActivity(intent);
            });
        }

    }
}
