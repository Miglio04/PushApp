package com.example.pushapp.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pushapp.R;
import com.example.pushapp.ui.profile.ProfileActivity;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Card Streak
        setupStatCard(view, R.id.cardStreak,
                R.drawable.outline_local_fire_department_24,
                "18",
                "Streak",
                R.color.md_theme_primary);

        // 2. Card Workouts
        setupStatCard(view, R.id.cardWorkouts,
                R.drawable.outline_accessibility_new_24,
                "127",
                "Workouts",
                R.color.md_theme_primary);

        // 3. Card Volume
        setupStatCard(view, R.id.cardVolume,
                R.drawable.outline_lightbulb_24,
                "2.5k",
                "Volume",
                R.color.md_theme_primary);

        MaterialCardView btnUserArea = view.findViewById(R.id.btnUserArea);
        btnUserArea.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupStatCard(View rootView, int cardId, int iconResId, String value, String label, int colorResId) {
        View cardContainer = rootView.findViewById(cardId);

        if (cardContainer == null) return;

        MaterialCardView materialCard = (MaterialCardView) cardContainer;
        ImageView icon = materialCard.findViewById(R.id.iconStat);
        TextView tvValue = materialCard.findViewById(R.id.tvValue);
        TextView tvLabel = materialCard.findViewById(R.id.tvLabel);

        if (icon != null) icon.setImageResource(iconResId);
        if (tvValue != null) tvValue.setText(value);
        if (tvLabel != null) tvLabel.setText(label);

        if (getContext() != null) {
            int color = getResources().getColor(colorResId, getContext().getTheme());

            if (icon != null) {
                icon.setColorFilter(color);
            }

            materialCard.setStrokeColor(color);
        }
    }
}
