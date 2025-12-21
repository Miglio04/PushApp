package com.example.pushapp.ui.main;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
        // Nota: Assicurati che nel tuo XML activity_main la root abbia id 'main'
        // Se ti da errore qui, controlla l'ID nel file activity_main.xml
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), 0);
                return insets;
            });
        }

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

        // -----------------------------------------------------------
        // 4. CONTROLLO POPUP BENVENUTO (Nuovo Codice)
        // -----------------------------------------------------------
        // Controlliamo se arriviamo dal Login con il segnale "SHOW_WELCOME"
        boolean showWelcome = getIntent().getBooleanExtra("SHOW_WELCOME", false);

        if (showWelcome) {
            showWelcomeDialog();
            // Rimuoviamo l'extra così se ruoti lo schermo non riesce il popup
            getIntent().removeExtra("SHOW_WELCOME");
        }
    }

    private void setupMiniPlayer() {
        if (miniPlayerView == null) return;

        TextView miniTitle = miniPlayerView.findViewById(R.id.mini_title);
        Button resumeButton = miniPlayerView.findViewById(R.id.mini_resume_button);
        Button discardButton = miniPlayerView.findViewById(R.id.mini_discard_button);

        // Hide mini-player when workout is not in progress or when WorkoutFragment is active
        workoutViewModel.isWorkoutInProgress().observe(this, inProgress -> {
            boolean isWorkoutOnTop = navController != null && navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.nav_workouts;
            boolean show = inProgress != null && inProgress && !isWorkoutOnTop;
            miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) {
                miniTitle.setText(title);
            }
        });

        resumeButton.setOnClickListener(v -> {
            if (navController != null && navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.nav_workouts) {
                return;
            }
            if (navController != null) {
                navController.navigate(R.id.nav_workouts);
            }
        });

        discardButton.setOnClickListener(v -> {
            workoutViewModel.stopWorkout();
        });
    }

    // -----------------------------------------------------------
    // FUNZIONE PER MOSTRARE IL POPUP
    // -----------------------------------------------------------
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Usa lo stesso XML del popup (dialog_success.xml)
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        // Sfondo Trasparente per vedere la Home sotto gli angoli tondi
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // Qui mettiamo true: se clicca fuori il popup sparisce e vede la Home
        dialog.setCancelable(true);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        // ATTENZIONE: Controlla che nel tuo XML dialog_success l'ID sia btnOkay o btnAction
        // Qui uso btnOkay perché lo usavi nel LoginActivity precedente
        Button btnAction = view.findViewById(R.id.btnAction);

        tvTitle.setText("Welcome Back!");
        tvMessage.setText("You are successfully logged in.");

        if (btnAction != null) {
            btnAction.setText("START TRAINING");
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}