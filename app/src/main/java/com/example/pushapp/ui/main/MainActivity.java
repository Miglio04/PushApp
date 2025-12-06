package com.example.pushapp.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.pushapp.ui.main.fragments.HomeFragment;
import com.example.pushapp.R;
import com.example.pushapp.ui.main.fragments.StatsFragment;
import com.example.pushapp.ui.main.fragments.TrainingDaysFragment;
import com.example.pushapp.ui.main.fragments.TrainingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Questo è il codice CORRETTO e UNICO per la MainActivity
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Collega il file XML (la scatola vuota)
        setContentView(R.layout.activity_main);
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
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_stats) {
                selectedFragment = new StatsFragment();
            } else if (itemId == R.id.nav_trainings) {
                selectedFragment = new TrainingsFragment();
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
    }
}
