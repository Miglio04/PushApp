package com.example.pushapp.ui.main;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.pushapp.R;
import com.example.pushapp.repositories.FirebaseCallback;
import com.example.pushapp.utils.UserViewModel;
import com.example.pushapp.utils.WorkoutViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private WorkoutViewModel workoutViewModel;
    private UserViewModel userViewModel;
    private View miniPlayerView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inizializza i ViewModel
        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Carica i dati dell'utente all'avvio
        userViewModel.loadUserData();

        // Gestisci gli insets per il padding (Fix deprecated)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Trova il NavHostFragment e il NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Collega la BottomNavigationView al NavController
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

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
            if (navController == null) return;
            
            boolean isWorkoutOnTop = navController.getCurrentDestination() != null && 
                                   navController.getCurrentDestination().getId() == R.id.nav_workouts;
            boolean show = inProgress != null && inProgress && !isWorkoutOnTop;
            miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) {
                miniTitle.setText(title);
            }
        });

        resumeButton.setOnClickListener(v -> {
            if (navController == null) return;

            if (navController.getCurrentDestination() != null && 
                navController.getCurrentDestination().getId() == R.id.nav_workouts) {
                return;
            }
            navController.navigate(R.id.nav_workouts);
        });

        discardButton.setOnClickListener(v -> {
            workoutViewModel.stopWorkout(new FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Workout scartato con successo
                }

                @Override
                public void onError(Exception e) {
                    // Gestisci l'errore se necessario
                }
            });
        });
    }

}
