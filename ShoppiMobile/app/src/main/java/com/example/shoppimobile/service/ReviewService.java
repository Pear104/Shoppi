package com.example.shoppimobile.service;

import com.example.shoppimobile.model.Review;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReviewService {
    @POST("reviews")
    Call<Review> createReview(@Body Review review);

    @GET("reviews/order-item/{orderItemId}")
    Call<Review> getReview(@Path("orderItemId") String orderItemId);

    @PUT("reviews/{reviewId}")
    Call<Review> updateReview(@Path("reviewId") String reviewId,@Body Review review);
}