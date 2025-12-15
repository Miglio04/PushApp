package com.example.pushapp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.pushapp.R;
import com.example.pushapp.utils.WorkoutViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private WorkoutViewModel workoutViewModel;
    private View miniPlayerView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 2. Inizializza il ViewModel
        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        // 3. Gestisci gli insets per il padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), 0);
            return insets;
        });

        // Trova il NavHostFragment e il NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Collega la BottomNavigationView al NavController
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Mini-player
        miniPlayerView = findViewById(R.id.workout_miniplayer);
        setupMiniPlayer();
    }

    private void setupMiniPlayer() {
        TextView miniTitle = miniPlayerView.findViewById(R.id.mini_title);
        Button resumeButton = miniPlayerView.findViewById(R.id.mini_resume_button);
        Button discardButton = miniPlayerView.findViewById(R.id.mini_discard_button);

        // Hide mini-player when workout is not in progress or when WorkoutFragment is active
        workoutViewModel.isWorkoutInProgress().observe(this, inProgress -> {
            boolean isWorkoutOnTop = navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.nav_workouts;
            boolean show = inProgress != null && inProgress && !isWorkoutOnTop;
            miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) {
                miniTitle.setText(title);
            }
        });

        resumeButton.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.nav_workouts) {
                return;
            }
            navController.navigate(R.id.nav_workouts);
        });

        discardButton.setOnClickListener(v -> {
            workoutViewModel.stopWorkout();
        });
    }

}
