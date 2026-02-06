package com.example.pushapp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Aggiunto per feedback utente

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.pushapp.R;
// import com.example.pushapp.repositories.FirebaseCallback; // Non serve più qui per il cancel
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.example.pushapp.viewModels.WorkoutViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private WorkoutViewModel workoutViewModel;
    private UserViewModel userViewModel;
    private View miniPlayerView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inizializza i ViewModel (Factory aggiornata con 4 parametri)
        workoutViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(WorkoutViewModel.class);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(UserViewModel.class);

        // 2. Carica i dati dell'utente all'avvio
        userViewModel.fetchUser();

        // 3. Gestisci gli insets per il padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // 4. Setup NavController e BottomBar
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // --- LOGICA NUOVA: Gestione visualizzazione BottomBar ---
            // Opzionale: Se vuoi nascondere la BottomBar mentre ti alleni (come il miniplayer)
            /*
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.nav_workouts) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
            */
        }

        // 5. Setup Mini-player (esistente, ma aggiornato)
        miniPlayerView = findViewById(R.id.workout_miniplayer);
        setupMiniPlayer();

        // ====================================================================
        //  PARTE NUOVA: ANTI-CRASH E RIPRISTINO
        // ====================================================================

        // A. Controlliamo se c'è una sessione salvata nelle SharedPreferences
        workoutViewModel.checkRestoredSession();

        // B. Osserviamo se c'è un workout attivo.
        // Questo serve sia per il ripristino automatico all'avvio, sia per la navigazione normale.
        workoutViewModel.isWorkoutInProgress().observe(this, isInProgress -> {
            if (Boolean.TRUE.equals(isInProgress)) {
                // Se c'è un allenamento attivo E non siamo già nella schermata di workout...
                if (navController != null && navController.getCurrentDestination() != null) {
                    if (navController.getCurrentDestination().getId() != R.id.nav_workouts) {
                        // ...ci andiamo subito! (Ripristino automatico dell'utente)
                        navController.navigate(R.id.nav_workouts);
                    }
                }
            }
        });
    }

    private void setupMiniPlayer() {
        TextView miniTitle = miniPlayerView.findViewById(R.id.mini_title);
        Button resumeButton = miniPlayerView.findViewById(R.id.mini_resume_button);
        Button discardButton = miniPlayerView.findViewById(R.id.mini_discard_button);

        // --- Logica Visibilità MiniPlayer (Invariata) ---
        // Si mostra SOLO se c'è un workout in corso E NON siamo nella schermata workout
        workoutViewModel.isWorkoutInProgress().observe(this, inProgress -> {
            if (navController == null) return;

            // Verifica se siamo già nel fragment del workout (usa l'ID del tuo nav_graph: nav_workouts)
            boolean isWorkoutOnTop = navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.nav_workouts;

            boolean show = inProgress != null && inProgress && !isWorkoutOnTop;
            miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        // Listener per nascondere il miniplayer appena entriamo nel workout fragment
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                boolean inProgress = Boolean.TRUE.equals(workoutViewModel.isWorkoutInProgress().getValue());
                boolean isWorkoutOnTop = destination.getId() == R.id.nav_workouts;
                boolean show = inProgress && !isWorkoutOnTop;
                miniPlayerView.setVisibility(show ? View.VISIBLE : View.GONE);
            });
        }

        // Titolo
        workoutViewModel.getWorkoutTitle().observe(this, title -> {
            if (title != null) {
                miniTitle.setText(title);
            }
        });

        // Tasto "Resume" (Riprendi)
        resumeButton.setOnClickListener(v -> {
            if (navController == null) return;
            // Se non siamo già lì, naviga al workout
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() != R.id.nav_workouts) {
                navController.navigate(R.id.nav_workouts);
            }
        });

        // Tasto "Discard" (Scarta/Annulla)
        // --- MODIFICA: Usiamo il nuovo metodo cancelWorkout() ---
        discardButton.setOnClickListener(v -> {
            // Chiamiamo il metodo che pulisce la sessione e resetta i LiveData
            workoutViewModel.cancelWorkout();

            // Non serve callback complessa: i LiveData (isWorkoutInProgress) diventeranno false
            // e gli observer sopra nasconderanno automaticamente il miniplayer.
            Toast.makeText(MainActivity.this, "Allenamento annullato", Toast.LENGTH_SHORT).show();
        });
    }
}