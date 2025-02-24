package com.example.shoppi.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.shoppi.authentication.SignInActivity;
import com.example.shoppi.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID") // Thay bằng ID từ google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        final TextView textView = binding.textHome;

        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .circleCrop() // Optional: Makes it a circular image
                    .placeholder(android.R.drawable.ic_menu_gallery) // Optional: Placeholder while loading
                    .error(android.R.drawable.ic_menu_close_clear_cancel) // Optional: Error image
                    .into(binding.userPhoto);
        } else {
            // Optional: Set a default image if no photo URL is available
            Glide.with(this)
                    .load(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(binding.userPhoto);
        }

        Button signOutButton = binding.signOutButton;
        signOutButton.setOnClickListener(v -> signOut());

        return root;
    }

    private void signOut() {
        mAuth.signOut(); // Đăng xuất khỏi Firebase

        // Chuyển hướng về màn hình đăng nhập
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        startActivity(intent);
        // if (getActivity() != null) {
        //     getActivity().finish();
        // }
        // Đăng xuất tài khoản khỏi thiết bị
        Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }
}