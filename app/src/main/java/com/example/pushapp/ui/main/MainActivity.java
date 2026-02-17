package com.example.pushapp.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
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
    private static final int ANIMATION_DURATION = 300;
    private WorkoutViewModel workoutViewModel;
    private UserViewModel userViewModel;
    private HistoryViewModel historyViewModel;
    private TrainingViewModel trainingViewModel;
    private View miniPlayerView;
    private NavController navController;
    private boolean isMiniPlayerVisible = false;

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
        userViewModel.getSessionLiveData().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                if (result != null && result.isSessionSuccess()) {
                    userViewModel.getSessionLiveData().removeObserver(this);
                    String userId = ((Result.SessionSuccess) result).getData().getUserId();
                    trainingViewModel.fetchTrainings(userId);
                    trainingViewModel.loadAvailableExercises();
                } else {
                    Log.d(TAG, "Errore nella fetch dell'utente");
                }
            }
        });

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

        // Initial state - hidden
        miniPlayerView.setTranslationY(200f);
        miniPlayerView.setAlpha(0f);

        workoutViewModel.isWorkoutInProgress().observe(this, inProgress -> {
            if (navController == null) return;

            boolean isWorkoutOnTop = navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.nav_workouts;

            boolean shouldShow = (inProgress != null && inProgress) && !isWorkoutOnTop;

            if (shouldShow && !isMiniPlayerVisible) {
                showMiniPlayer();
            } else if (!shouldShow && isMiniPlayerVisible) {
                hideMiniPlayer();
            }
        });

        // Update visibility when navigation changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Boolean inProgress = workoutViewModel.isWorkoutInProgress().getValue();
            boolean isWorkoutScreen = destination.getId() == R.id.nav_workouts;
            boolean shouldShow = (inProgress != null && inProgress) && !isWorkoutScreen;

            if (shouldShow && !isMiniPlayerVisible) {
                showMiniPlayer();
            } else if (!shouldShow && isMiniPlayerVisible) {
                hideMiniPlayer();
            }
        });

        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) miniTitle.setText(title);
        });

        resumeButton.setOnClickListener(v -> {
            // Resume the workout timer before navigating
            workoutViewModel.startWorkoutTimer();
            if (navController != null) {
                navController.navigate(R.id.nav_workouts);
            }
        });

        discardButton.setOnClickListener(v -> {
            workoutViewModel.finishWorkout(() ->
                Log.d("MainActivity", "Workout saved from MiniPlayer")
            );
        });
    }

    private void showMiniPlayer() {
        isMiniPlayerVisible = true;
        miniPlayerView.setVisibility(View.VISIBLE);
        miniPlayerView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(null)
                .start();
    }

    private void hideMiniPlayer() {
        isMiniPlayerVisible = false;
        miniPlayerView.animate()
                .translationY(200f)
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        miniPlayerView.setVisibility(View.GONE);
                    }
                })
                .start();
    }
}