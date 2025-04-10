package com.example.shoppimobile.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.shoppimobile.R;
import com.example.shoppimobile.model.Order;
import com.example.shoppimobile.repository.CartRepository;
import com.example.shoppimobile.repository.OrderRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class ShippingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ShippingActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private TextView selectedAddressTextView;
    private TextInputEditText phoneInputEditText;
    private Button placeOrderButton;
    private LatLng selectedLocation;
    private String selectedAddress;
    private CartRepository cartRepository;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase Auth
        ZaloPaySDK.init(2553, Environment.SANDBOX);
        cartRepository = new CartRepository(this);
        orderRepository = new OrderRepository(this);

        // Initialize UI components
        selectedAddressTextView = findViewById(R.id.selected_address);
        phoneInputEditText = findViewById(R.id.phone_input);
        placeOrderButton = findViewById(R.id.place_order_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up place order button
        placeOrderButton.setOnClickListener(v -> placeOrder());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up map click listener
        mMap.setOnMapClickListener(latLng -> {
            // Clear previous markers
            mMap.clear();

            // Add marker at the clicked position
            mMap.addMarker(new MarkerOptions().position(latLng).title("Delivery Location"));

            // Save the selected location
            selectedLocation = latLng;

            // Get address from coordinates
            getAddressFromLocation(latLng);
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Zoom to user's current location
            mMap.setOnMyLocationChangeListener(location -> {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                // Remove the listener to prevent continuous updates
                mMap.setOnMyLocationChangeListener(null);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                // Get address lines
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }

                selectedAddress = sb.toString();
                selectedAddressTextView.setText(selectedAddress);
            } else {
                selectedAddress = latLng.latitude + ", " + latLng.longitude;
                selectedAddressTextView.setText(selectedAddress);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
            selectedAddress = latLng.latitude + ", " + latLng.longitude;
            selectedAddressTextView.setText(selectedAddress);
        }
    }

    private void placeOrder() {
        // Validate inputs
        String phoneNumber = phoneInputEditText.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLocation == null || selectedAddress == null) {
            Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        placeOrderButton.setEnabled(false);
        placeOrderButton.setText("Processing...");

        // Create order
        Call<Order> call = orderRepository.createOrder(new Order(selectedAddress, phoneNumber));
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Order order = response.body();
                    Intent intent = new Intent(ShippingActivity.this, OrderActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ShippingActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                }
                placeOrderButton.setEnabled(true);
                placeOrderButton.setText("Place Order");
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(TAG, "Failed to place order" + t.getMessage());
                Toast.makeText(ShippingActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                placeOrderButton.setEnabled(true);
                placeOrderButton.setText("Place Order");
            }
        });
    }
}
