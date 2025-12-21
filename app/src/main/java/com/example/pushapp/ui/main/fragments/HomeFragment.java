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
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.R;
import com.example.pushapp.models.User;
import com.example.pushapp.ui.profile.ProfileActivity;
import com.example.pushapp.utils.UserViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private UserViewModel userViewModel;
    private TextView nameTitle;
    private TextView tvAvatarInitial;
    private TextView tvWeightVal;
    private TextView tvWeightDiff;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Utilizziamo l'activity come scope per condividere il ViewModel caricato in MainActivity
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inizializzazione view
        nameTitle = view.findViewById(R.id.nameTitle);
        tvAvatarInitial = view.findViewById(R.id.tvAvatarInitial);
        tvWeightVal = view.findViewById(R.id.tvWeightVal);
        tvWeightDiff = view.findViewById(R.id.tvWeightDiff);
        MaterialCardView btnUserArea = view.findViewById(R.id.btnUserArea);

        // Osserva i dati dell'utente dal ViewModel caricato all'avvio
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Aggiorna Nome e Iniziale Avatar
                if (user.getName() != null && !user.getName().isEmpty()) {
                    nameTitle.setText(user.getName());
                    if (tvAvatarInitial != null) {
                        tvAvatarInitial.setText(user.getName().substring(0, 1).toUpperCase());
                    }
                }

                // Aggiorna il Peso Attuale
                if (tvWeightVal != null) {
                    tvWeightVal.setText(String.format(Locale.getDefault(), "%.1f kg", user.getWeight()));
                }
                
                // Aggiorna la differenza di peso
                if (tvWeightDiff != null) {
                    List<Double> progress = user.getWeightProgress();
                    if (progress != null && progress.size() >= 2) {
                        // Calcola differenza tra l'ultimo (peso attuale) e il penultimo
                        double currentWeight = progress.get(progress.size() - 1);
                        double previousWeight = progress.get(progress.size() - 2);
                        double diff = currentWeight - previousWeight;
                        
                        String sign = diff > 0 ? "+" : "";
                        tvWeightDiff.setText(String.format(Locale.getDefault(), "%s%.1f kg", sign, diff));
                    } else {
                        // Solo un peso o nessuno
                        tvWeightDiff.setText("0.0 kg");
                    }
                }
            }
        });

        // Configurazione delle card statistiche (Dati statici per ora)
        setupStatCard(view, R.id.cardStreak,
                R.drawable.outline_local_fire_department_24,
                "18",
                "Streak",
                R.color.md_theme_primary);

        setupStatCard(view, R.id.cardWorkouts,
                R.drawable.outline_accessibility_new_24,
                "127",
                "Workouts",
                R.color.md_theme_primary);

        setupStatCard(view, R.id.cardVolume,
                R.drawable.outline_lightbulb_24,
                "2.5k",
                "Volume",
                R.color.md_theme_primary);

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
            if (icon != null) icon.setColorFilter(color);
            materialCard.setStrokeColor(color);
        }
    }
}
