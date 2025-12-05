package com.example.pushapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Questo è il codice CORRETTO e UNICO per la MainActivity
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Collega il file XML (la scatola vuota)
        setContentView(R.layout.activity_main);

        // 2. INIZIO TRUCCO: Carica subito il calendario
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    // Qui usiamo l'ID che abbiamo messo nel file XML activity_main.xml
                    .replace(R.id.fragment_container, new com.example.pushapp.ui.stats.StatsFragment())
                    .commit();
        }
    }
}