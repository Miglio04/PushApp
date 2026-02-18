package com.example.pushapp.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;
import com.example.pushapp.ui.login.AuthActivity;
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private UserViewModel userViewModel;
    private TrainingViewModel trainingViewModel;
    private HistoryViewModel historyViewModel;

    private TextView profileInitial, profileFullName, profileEmailTop;
    private TextView tvDetailEmail, tvDetailGender, tvDetailAge, tvDetailHeight, tvDetailWeight;

    private TextView txtKpiWorkouts, txtKpiStreak, txtKpiVolume;
    private MaterialButton btnLogout;
    private MaterialButton btnToggleTheme;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupViewModel();
        setupLogout();
        setupThemeToggle();
    }

    private void initializeViews(View view) {
        profileInitial = view.findViewById(R.id.profileInitial);
        profileFullName = view.findViewById(R.id.profileFullName);
        profileEmailTop = view.findViewById(R.id.profileEmailTop);

        tvDetailEmail = view.findViewById(R.id.tvDetailEmail);
        tvDetailGender = view.findViewById(R.id.tvDetailGender);
        tvDetailAge = view.findViewById(R.id.tvDetailAge);
        tvDetailHeight = view.findViewById(R.id.tvDetailHeight);
        tvDetailWeight = view.findViewById(R.id.tvDetailWeight);

        txtKpiWorkouts = view.findViewById(R.id.txtKpiWorkouts);
        txtKpiStreak = view.findViewById(R.id.txtKpiStreak);
        txtKpiVolume = view.findViewById(R.id.txtKpiVolume);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnToggleTheme = view.findViewById(R.id.btnToggleTheme);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory(requireContext());

        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);
        trainingViewModel = new ViewModelProvider(this, factory).get(TrainingViewModel.class);
        historyViewModel = new ViewModelProvider(this, factory).get(HistoryViewModel.class);

        // 1. Dati Utente
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.UserSuccess) {
                User user = ((Result.UserSuccess) result).getData();
                updateUserUi(user);
            }
        });

        historyViewModel.getKpiStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                if (txtKpiWorkouts != null) {
                    txtKpiWorkouts.setText(String.valueOf(stats.getWorkoutsMonth()));
                }
                if (txtKpiStreak != null) {
                    txtKpiStreak.setText(String.valueOf(stats.getCurrentStreak()));
                }
                if (txtKpiVolume != null) {
                    txtKpiVolume.setText(formatVolume(stats.getVolumeMonth()));
                }
            }
        });

        historyViewModel.fetchHistory();
    }

    private void updateUserUi(User user) {
        if (user == null) return;

        String name = user.getName() != null ? user.getName() : "";
        String surname = user.getSurname() != null ? user.getSurname() : "";

        if (profileFullName != null) profileFullName.setText(name + " " + surname);
        if (profileEmailTop != null) profileEmailTop.setText(user.getEmail());

        if (!name.isEmpty() && profileInitial != null) {
            profileInitial.setText(name.substring(0, 1).toUpperCase());
        }

        if (tvDetailEmail != null) tvDetailEmail.setText(user.getEmail());
        if (tvDetailGender != null) tvDetailGender.setText(user.getGender() != null ? user.getGender() : "-");
        if (tvDetailAge != null) tvDetailAge.setText(user.getAge() + " years");
        if (tvDetailHeight != null) tvDetailHeight.setText(user.getHeight() + " cm");
        if (tvDetailWeight != null) tvDetailWeight.setText(user.getWeight() + " kg");
    }

    private void setupLogout() {
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked");
                userViewModel.logout();
                trainingViewModel.resetLocalDatabase();
                userViewModel.resetLocalDatabase();
                historyViewModel.resetLocalDatabase();

                Intent intent = new Intent(getActivity(), AuthActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finishAffinity();
                }
            });
        }
    }

    private void setupThemeToggle() {
        if (btnToggleTheme != null) {
            int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            boolean isNightMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            if (isNightMode) {
                btnToggleTheme.setIconResource(R.drawable.sun);
                btnToggleTheme.setText(R.string.light_theme);
            } else {
                btnToggleTheme.setIconResource(R.drawable.moon);
                btnToggleTheme.setText(R.string.dark_theme);
            }

            btnToggleTheme.setOnClickListener(v -> {
                if (isNightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            });
        }
    }

    private String formatVolume(Double volume) {
        if (volume == null || volume == 0) return "0";
        if (volume >= 1000000) return String.format(java.util.Locale.US, "%.1fM", volume / 1000000.0);
        if (volume >= 1000) return String.format(java.util.Locale.US, "%.1fk", volume / 1000.0);
        return String.valueOf(volume.intValue());
    }
}
