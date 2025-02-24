package com.example.shoppi.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName == null) {
                displayName = currentUser.getPhoneNumber();
            }
            if (displayName == null) {
                displayName = currentUser.getEmail();
            }
            mText.setValue("Hello "+ displayName);
        } else {
            mText.setValue("Not signed in");
        }
    }

    public LiveData<String> getText() {
        return mText;
    }
}