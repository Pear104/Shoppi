package com.example.shoppimobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppimobile.R;
import com.example.shoppimobile.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems = new ArrayList<>();
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onDeleteItem(CartItem item);
        void onItemClick(int position);
    }

    public CartAdapter(CartItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    public List<CartItem> getItems() {
        return cartItems;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName,productPrice, quantityText;
        private ImageButton btnIncrease, btnDecrease, deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            quantityText = itemView.findViewById(R.id.quantity_text);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(CartItem item) {
            // Load product image using Glide
            Glide.with(itemView.getContext())
                    .load(item.getProduct().getImageUrl())
                    .into(productImage);

            productName.setText(item.getProduct().getName());
            productPrice.setText(String.format("$%.2f", item.getProduct().getPrice()));
            quantityText.setText(String.valueOf(item.getQuantity()));

            btnIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (newQuantity <= 99) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            btnDecrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity >= 1) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));

            deleteButton.setOnClickListener(v -> listener.onDeleteItem(item));
        }
    }
} 