package com.example.shoppimobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.model.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviews;

    public ReviewAdapter(Context context,List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
//        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.username.setText(review.getUser().getName());
        holder.rating.setRating((float) review.getRating());
        holder.comment.setText(review.getComment());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date date = new Date(review.getCreatedAt().getTime());
        holder.date.setText(dateFormat.format(date));
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView date;
        RatingBar rating;
        TextView comment;

        public ReviewViewHolder(View view) {
            super(view);
            username = view.findViewById(R.id.review_username);
            date = view.findViewById(R.id.review_date);
            rating = view.findViewById(R.id.review_rating);
            comment = view.findViewById(R.id.review_comment);
        }
    }
}

