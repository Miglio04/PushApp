package com.example.pushapp.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.R;
import com.example.pushapp.utils.UserViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private UserViewModel userViewModel;
    
    // UI Profile Header
    private TextView profileInitial, profileFullName, profileEmailTop;
    
    // UI Personal Data Dropdown
    private MaterialCardView cardPersonalData;
    private LinearLayout expandablePersonalData;
    private ImageView expandArrow;
    private TextView tvDetailEmail, tvDetailGender, tvDetailAge, tvDetailHeight, tvDetailWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Gestione corretta dei System Bars (Status Bar) per evitare sovrapposizioni
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        initializeViews();
        setupViewModel();

        // Tasto Back
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });
        }

        // Toggle dropdown logic
        if (cardPersonalData != null) {
            cardPersonalData.setOnClickListener(v -> togglePersonalData());
        }
    }

    private void initializeViews() {
        profileInitial = findViewById(R.id.profileInitial);
        profileFullName = findViewById(R.id.profileFullName);
        profileEmailTop = findViewById(R.id.profileEmailTop);

        cardPersonalData = findViewById(R.id.cardPersonalData);
        expandablePersonalData = findViewById(R.id.expandable_personal_data);
        expandArrow = findViewById(R.id.expand_arrow);

        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailGender = findViewById(R.id.tvDetailGender);
        tvDetailAge = findViewById(R.id.tvDetailAge);
        tvDetailHeight = findViewById(R.id.tvDetailHeight);
        tvDetailWeight = findViewById(R.id.tvDetailWeight);
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.loadUserData();

        userViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                String name = user.getName() != null ? user.getName() : "";
                String surname = user.getSurname() != null ? user.getSurname() : "";
                if (profileFullName != null) profileFullName.setText(name + " " + surname);
                if (profileEmailTop != null) profileEmailTop.setText(user.getEmail());
                
                if (!name.isEmpty() && profileInitial != null) {
                    profileInitial.setText(name.substring(0, 1).toUpperCase());
                }

                // Dettagli nel dropdown utilizzando le stringhe localizzate
                if (tvDetailEmail != null) {
                    tvDetailEmail.setText(getString(R.string.detail_email, user.getEmail()));
                }
                if (tvDetailGender != null) {
                    String gender = (user.getGender() != null && !user.getGender().isEmpty()) ? user.getGender() : "-";
                    tvDetailGender.setText(getString(R.string.detail_gender, gender));
                }
                if (tvDetailAge != null) {
                    tvDetailAge.setText(getString(R.string.detail_age, user.getAge()));
                }
                if (tvDetailHeight != null) {
                    tvDetailHeight.setText(getString(R.string.detail_height, user.getHeight()));
                }
                if (tvDetailWeight != null) {
                    tvDetailWeight.setText(getString(R.string.detail_weight, user.getWeight()));
                }
            }
        });
    }

    private void togglePersonalData() {
        if (expandablePersonalData == null || expandArrow == null) return;

        if (expandablePersonalData.getVisibility() == View.GONE) {
            expandablePersonalData.setVisibility(View.VISIBLE);
            expandArrow.setRotation(180);
        } else {
            expandablePersonalData.setVisibility(View.GONE);
            expandArrow.setRotation(0);
        }
    }
}
