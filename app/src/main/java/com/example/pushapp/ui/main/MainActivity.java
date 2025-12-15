package com.example.pushapp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.R;
import com.example.pushapp.ui.main.fragments.FoodFragment;
import com.example.pushapp.ui.main.fragments.HomeFragment;
import com.example.pushapp.ui.main.fragments.SocialFragment;
import com.example.pushapp.ui.main.fragments.StatsFragment;
import com.example.pushapp.ui.main.fragments.TrainingDaysFragment;
import com.example.pushapp.ui.main.fragments.TrainingsFragment;
import com.example.pushapp.utils.WorkoutViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

//Aggiunto per test su Training Days e Workout
import com.example.pushapp.ui.main.fragments.WorkoutFragment;

// Questo è il codice CORRETTO e UNICO per la MainActivity
public class MainActivity extends AppCompatActivity {

    private WorkoutViewModel workoutViewModel;
    private View miniPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Collega il file XML (la scatola vuota)
        setContentView(R.layout.activity_main);
        // 2. Inizializza il ViewModel
        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        // 3. Gestisci gli insets per il padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set HomeFragment as the default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {

            // Evita trnsizioni inutili, se l'item selezionato è già attivo
            if (item.getItemId() == bottomNavigationView.getSelectedItemId()) {
                return false;
            }

            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_stats) {
                selectedFragment = new StatsFragment();
            } else if (itemId == R.id.nav_trainings) {
                selectedFragment = new TrainingsFragment();
            } else if (itemId == R.id.nav_food) {
                selectedFragment = new FoodFragment();
            } else if (itemId == R.id.nav_social){
                //MODIFICA PER TEST SU TRAINING DAYS E WORKOUT
                selectedFragment = new TrainingDaysFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            // Return false for items without a fragment to prevent selection
            return false;
        });

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
            Fragment top = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            boolean isWorkoutOnTop = top instanceof WorkoutFragment;
            boolean show = inProgress != null && inProgress && !isWorkoutOnTop;
            miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) {
                miniTitle.setText(title);
            }
        });

        resumeButton.setOnClickListener(v -> {
            Fragment workoutFragment = getSupportFragmentManager().findFragmentByTag(WorkoutFragment.TAG);
            if (workoutFragment != null && workoutFragment.isVisible()) {
                return;
            }

            boolean popped = getSupportFragmentManager().popBackStackImmediate(WorkoutFragment.TAG, 0);

            if (!popped) {
                if (workoutFragment == null) {
                    workoutFragment = WorkoutFragment.newInstance(workoutViewModel.getWorkoutTitle().getValue(), "");
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, workoutFragment, WorkoutFragment.TAG)
                        .addToBackStack(WorkoutFragment.TAG)
                        .commit();
            }
        });

        discardButton.setOnClickListener(v -> {
            workoutViewModel.stopWorkout();
        });
    }

}
