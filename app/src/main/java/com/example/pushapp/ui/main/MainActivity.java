package com.example.pushapp.ui.main;

import android.os.Bundle;
import android.util.Log;
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
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.models.Result;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WorkoutViewModel workoutViewModel;
    private UserViewModel userViewModel;
    private HistoryViewModel historyViewModel;
    private TrainingViewModel trainingViewModel;
    private View miniPlayerView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        workoutViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(WorkoutViewModel.class);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(UserViewModel.class);

        historyViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(HistoryViewModel.class);
        trainingViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(TrainingViewModel.class);


        userViewModel.fetchUser();
        historyViewModel.fetchHistory();
        workoutViewModel.checkRestoredSession();
        Result result = userViewModel.getSessionLiveData().getValue();

        if (result != null && result.isSessionSuccess()) {
            String userId = ((Result.SessionSuccess) result).getData().getUserId();
            trainingViewModel.fetchTrainings(userId);
        } else{
            Log.d(TAG, "Errore nella fetch dell'utente");
        }


        setupWindowInsets();
        setupNavigation();

        miniPlayerView = findViewById(R.id.workout_miniplayer);
        setupMiniPlayer();

        observeWorkoutStatus();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
    }

    private void observeWorkoutStatus() {
        workoutViewModel.isWorkoutInProgress().observe(this, isInProgress -> {
            if (Boolean.TRUE.equals(isInProgress)) {
                Log.d("MainActivity", "Active workout detected.");
            }
        });
    }

    private void setupMiniPlayer() {
        TextView miniTitle = miniPlayerView.findViewById(R.id.mini_title);
        Button resumeButton = miniPlayerView.findViewById(R.id.mini_resume_button);
        Button discardButton = miniPlayerView.findViewById(R.id.mini_discard_button);

        workoutViewModel.isWorkoutInProgress().observe(this, inProgress -> {
            if (navController == null) return;

            boolean isWorkoutOnTop = navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.nav_workouts;

            boolean show = (inProgress != null && inProgress) && !isWorkoutOnTop;
            miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) miniTitle.setText(title);
        });

        resumeButton.setOnClickListener(v -> {
            if (navController != null) navController.navigate(R.id.nav_workouts);
        });

        discardButton.setOnClickListener(v -> {
            workoutViewModel.finishWorkout(() -> {
                // Rimosso Toast.makeText: l'azione ora è silenziosa e pulita
                Log.d("MainActivity", "Workout saved from MiniPlayer");
            });
        });
    }
}