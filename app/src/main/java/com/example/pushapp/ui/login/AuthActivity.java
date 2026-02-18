package com.example.pushapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

/**
 * Activity handling user authentication flow (login, registration, forgot password).
 * Manages navigation between different authentication fragments and automatic login checks.
 */
public class AuthActivity extends AppCompatActivity {
    private NavController navController;
    private LinearLayout headerContainer;
    private TabLayout tabLayout;
    private TextView tvAuthSubtitle;
    private UserViewModel userViewModel;
    private LiveData<Result> sessionUserLiveData;
    boolean isLoading = true;

    /**
     * Initializes the activity, installs the splash screen, and attempts automatic login.
     *
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

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

    /**
     * Configures the authentication UI, including the tab layout for switching between
     * login and registration, and the navigation controller listener for UI updates.
     */
    private void setupAuthInterface(){
        setContentView(R.layout.activity_auth);

        headerContainer = findViewById(R.id.headerContainer);
        tabLayout = findViewById(R.id.tabLayoutAuth);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.forgotPasswordFragment) {
                    headerContainer.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                } else {
                    headerContainer.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);

                    if (destination.getId() == R.id.loginFragment) {
                        tvAuthSubtitle.setText(getString(R.string.welcome_back));
                        TabLayout.Tab tab = tabLayout.getTabAt(0);
                        if (tab != null && !tab.isSelected()) tab.select();

                    } else if (destination.getId() == R.id.registerFragment) {
                        tvAuthSubtitle.setText(getString(R.string.create_new_account));
                        TabLayout.Tab tab = tabLayout.getTabAt(1);
                        if (tab != null && !tab.isSelected()) tab.select();
                    }
                }
            });
        }

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

    /**
     * Checks if a user session is already active.
     * If valid, redirects to MainActivity; otherwise, displays the authentication interface.
     */
    private void tryAutomaticLogin() {
        sessionUserLiveData.observe(this, result -> {
            if (result != null) {
                sessionUserLiveData.removeObservers(this);
                if(result.isSessionSuccess()) {
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

    /**
     * Creates navigation options for smooth transitions between login and registration fragments.
     *
     * @return Configured NavOptions object.
     */
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
