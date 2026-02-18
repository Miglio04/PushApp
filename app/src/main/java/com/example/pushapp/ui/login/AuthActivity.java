package com.example.pushapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView; // IMPORTANTE

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.ui.main.MainActivity;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.material.tabs.TabLayout;

public class AuthActivity extends AppCompatActivity {

    private final static String TAG = "AuthActivity";
    private NavController navController;
    private LinearLayout headerContainer;
    private TabLayout tabLayout;
    // private UserViewModel userViewModel;
    private TextView tvAuthSubtitle; // Nuova variabile per il sottotitolo

    private UserViewModel userViewModel;

    private LiveData<Result> sessionUserLiveData;

    boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> isLoading);
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(UserViewModel.class);

        sessionUserLiveData = userViewModel.getSessionLiveData();
        userViewModel.fetchSessionUser();
        tryAutomaticLogin();

    }

    private void setupAuthInterface(){
        setContentView(R.layout.activity_auth);

        // Collegamento Viste
        headerContainer = findViewById(R.id.headerContainer);
        tabLayout = findViewById(R.id.tabLayoutAuth);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle); // Colleghiamo la TextView

        // Configurazione Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Ascoltiamo i cambi di schermata
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.forgotPasswordFragment) {
                    // Nascondi tutto su Forgot Password
                    headerContainer.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                } else {
                    // Mostra tutto su Login/Register
                    headerContainer.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);

                    // LOGICA PER CAMBIARE IL TESTO E IL TAB
                    if (destination.getId() == R.id.loginFragment) {
                        // Siamo su LOGIN
                        tvAuthSubtitle.setText("Welcome back!"); // Testo per Login

                        TabLayout.Tab tab = tabLayout.getTabAt(0);
                        if (tab != null && !tab.isSelected()) tab.select();

                    } else if (destination.getId() == R.id.registerFragment) {
                        // Siamo su REGISTER
                        tvAuthSubtitle.setText("Create a new account"); // Testo per Register

                        TabLayout.Tab tab = tabLayout.getTabAt(1);
                        if (tab != null && !tab.isSelected()) tab.select();
                    }
                }
            });
            Log.d(TAG, "onCreate() finita");
        }

        // Listener per i click sui Tab (Switch)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (navController == null) return;
                int currentId = navController.getCurrentDestination().getId();

                if (tab.getPosition() == 0) {
                    if (currentId != R.id.loginFragment) {
                        navController.navigate(R.id.loginFragment, null, getNavOptions());
                    }
                } else if (tab.getPosition() == 1) {
                    if (currentId != R.id.registerFragment) {
                        navController.navigate(R.id.registerFragment, null, getNavOptions());
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void tryAutomaticLogin() {
        sessionUserLiveData.observe(this, result -> {
            if (result != null) {
                sessionUserLiveData.removeObservers(this);
                if(result.isSessionSuccess()) {
                    Log.d("AuthActivity", "Sessione trovata, login automatico");
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    userViewModel.clearSessionLiveData();
                    setupAuthInterface();
                }
                isLoading = false;
            }
        });

    }

    private NavOptions getNavOptions() {
        return new NavOptions.Builder()
                .setEnterAnim(android.R.anim.fade_in)
                .setExitAnim(android.R.anim.fade_out)
                .setPopEnterAnim(android.R.anim.fade_in)
                .setPopExitAnim(android.R.anim.fade_out)
                .setPopUpTo(R.id.loginFragment, false)
                .build();
    }
}
