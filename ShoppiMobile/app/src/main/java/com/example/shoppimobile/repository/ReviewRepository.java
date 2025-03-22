package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.Review;
import com.example.shoppimobile.service.ReviewService;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ReviewRepository {
    private ReviewService reviewService;
    private Context context;

    public ReviewRepository(Context context) {
        this.context = context;
        Retrofit retrofit = APIClient.getClient(context);
        reviewService = retrofit.create(ReviewService.class);
    }

    public Call<Review> createReview(Review review) {
        return reviewService.createReview(review);
    }

    public Call<Review> getReview(String orderItemId) {
        return reviewService.getReview(orderItemId);
    }

    public Call<Review> updateReview(String reviewId, Review review) {
        return reviewService.updateReview(reviewId, review);
    }
}