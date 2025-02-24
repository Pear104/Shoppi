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

import com.example.shoppi.authentication.SignInActivity;
import com.example.shoppi.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

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

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Button signOutButton = binding.signOutButton; // Binding đã tự động tạo biến này từ id trong XML
        signOutButton.setOnClickListener(v -> signOut());

        return root;
    }

    private void signOut() {
        mAuth.signOut(); // Đăng xuất khỏi Firebase
        Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

        // Chuyển hướng về SignInActivity
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
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