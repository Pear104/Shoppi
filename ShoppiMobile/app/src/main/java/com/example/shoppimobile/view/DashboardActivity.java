package com.example.shoppimobile.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.model.DashboardStats;
import com.example.shoppimobile.model.RevenueData;
import com.example.shoppimobile.repository.OrderRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    
    private TextView tvTotalRevenue, tvTotalOrders, tvTodayRevenue;
    private LineChart chartRevenue;
    private CardView cardTotalRevenue, cardTotalOrders, cardTodayRevenue;
    private ProgressBar progressBar;
    private OrderRepository orderRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Initialize views
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);
        chartRevenue = findViewById(R.id.chartRevenue);
        cardTotalRevenue = findViewById(R.id.cardTotalRevenue);
        cardTotalOrders = findViewById(R.id.cardTotalOrders);
        cardTodayRevenue = findViewById(R.id.cardTodayRevenue);
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize repository
        orderRepository = new OrderRepository(this);
        
        // Load dashboard data
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        showLoading(true);
        
        // Load statistics
        Call<DashboardStats> statsCall = orderRepository.getDashboardStats();
        statsCall.enqueue(new Callback<DashboardStats>() {
            @Override
            public void onResponse(Call<DashboardStats> call, Response<DashboardStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateStats(response.body());
                } else {
                    Toast.makeText(DashboardActivity.this, 
                            "Failed to load statistics", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DashboardStats> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, 
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load statistics: " + t.getMessage());
            }
        });
        
        // Load revenue data for chart
        Call<List<RevenueData>> revenueCall = orderRepository.getRevenueData();
        revenueCall.enqueue(new Callback<List<RevenueData>>() {
            @Override
            public void onResponse(Call<List<RevenueData>> call, Response<List<RevenueData>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    setupChart(response.body());
                } else {
                    Toast.makeText(DashboardActivity.this, 
                            "Failed to load revenue data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<RevenueData>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(DashboardActivity.this, 
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load revenue data: " + t.getMessage());
            }
        });
    }
    
    private void updateStats(DashboardStats stats) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        
        tvTotalRevenue.setText(currencyFormat.format(stats.getTotalRevenue()));
        tvTotalOrders.setText(String.valueOf(stats.getTotalOrders()));
        tvTodayRevenue.setText(currencyFormat.format(stats.getTodayRevenue()));
    }
    
    private void setupChart(List<RevenueData> revenueDataList) {
        // Convert data for chart
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < revenueDataList.size(); i++) {
            RevenueData data = revenueDataList.get(i);
            entries.add(new Entry(i, data.getAmount()));
            labels.add(data.getDate());
        }
        
        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Daily Revenue");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        // Create line data
        LineData lineData = new LineData(dataSet);
        
        // Format X-axis
        XAxis xAxis = chartRevenue.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);
        
        // Set data to chart
        chartRevenue.getDescription().setEnabled(false);
        chartRevenue.setData(lineData);
        chartRevenue.getLegend().setEnabled(true);
        chartRevenue.animateX(1500);
        chartRevenue.invalidate();
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            cardTotalRevenue.setVisibility(View.INVISIBLE);
            cardTotalOrders.setVisibility(View.INVISIBLE);
            cardTodayRevenue.setVisibility(View.INVISIBLE);
            chartRevenue.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            cardTotalRevenue.setVisibility(View.VISIBLE);
            cardTotalOrders.setVisibility(View.VISIBLE);
            cardTodayRevenue.setVisibility(View.VISIBLE);
            chartRevenue.setVisibility(View.VISIBLE);
        }
    }
}