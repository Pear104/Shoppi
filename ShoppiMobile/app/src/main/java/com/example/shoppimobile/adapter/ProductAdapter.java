package com.example.shoppimobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoppimobile.R;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.view.ProductDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList = new ArrayList<Product>();
    private OnItemClickListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product productItem = productList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(productItem.getImageUrl())
                .into(holder.product_image);
        holder.product_name.setText(productItem.getName());
        holder.product_price.setText(productItem.getPrice() + " PI");
        holder.product_category.setText(productItem.getCategory().getName());
        holder.product_stock.setText("Stock: "+String.valueOf(productItem.getStock()));
        holder.rating_bar.setRating(productItem.getAvgRating());

        holder.itemView.setOnClickListener((view) -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView product_name, product_price, product_category,product_stock;
        ImageView product_image;
        RatingBar rating_bar;


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            product_stock = itemView.findViewById(R.id.product_stock);
            product_category = itemView.findViewById(R.id.product_category);
            product_image = itemView.findViewById(R.id.product_image);
            product_name = itemView.findViewById(R.id.product_name);
            product_price = itemView.findViewById(R.id.product_price);
            rating_bar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
