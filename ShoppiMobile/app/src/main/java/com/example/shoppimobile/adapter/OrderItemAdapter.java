package com.example.shoppimobile.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppimobile.R;
import com.example.shoppimobile.model.CartItem;
import com.example.shoppimobile.model.OrderItem;
import com.example.shoppimobile.view.ProductDetailActivity;
import com.example.shoppimobile.view.ReviewActivity;

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> items;
    private String orderId;

    public OrderItemAdapter() {
        this.items = new ArrayList<>();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
        holder.productImage.setOnClickListener((view) -> {
            // Navigate to ProductDetailActivity
            Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", item.getProduct().getId());
            view.getContext().startActivity(intent);
        });
        Log.d("OrderItemAdapter", "onBindViewHolder: " + item.getReview().getId());

        holder.productName.setOnClickListener((view) -> {
            // Navigate to ReviewActivity
            Intent intent = new Intent(view.getContext(), ReviewActivity.class);
            intent.putExtra("productId", item.getProduct().getId());
            intent.putExtra("orderItemId", item.getId());

            // Check if the user has already reviewed this product
            Log.d("Review", "isUpdate: " + (item.getReview().getId() != null));

            if (item.getReview().getId() != null) {
                intent.putExtra("reviewId", item.getReviewId());
                intent.putExtra("isUpdate", true);
            }


            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productQuantity;
        private final TextView productTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productTotal = itemView.findViewById(R.id.product_total);
        }

        public void bind(OrderItem item) {
            // Set product name
            productName.setText(item.getProduct().getName());

            // Set product price
            productPrice.setText(String.format("%.2f PI", item.getProduct().getPrice()));

            // Set quantity
            productQuantity.setText(String.format("Quantity: %d", item.getQuantity()));

            // Calculate and set total
            double total = item.getProduct().getPrice() * item.getQuantity();
            productTotal.setText(String.format("%.2f PI", total));

            // Load image
            if (item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getProduct().getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
} 