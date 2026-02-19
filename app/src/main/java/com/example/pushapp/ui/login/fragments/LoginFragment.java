package com.example.pushapp.ui.login.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pushapp.R;
import com.example.pushapp.ui.main.MainActivity;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;

/**
 * Fragment responsible for user login functionality.
 * Supports email/password authentication and Google Sign-In integration.
 * Handles user input validation, loading states, and navigation upon successful login.
 */
public class LoginFragment extends Fragment {
    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError;
    private LinearLayout loadingOverlay;
    private UserViewModel userViewModel;

    public LoginFragment() {}

    /**
     * Initializes the ViewModel and Google Sign-In client.
     *
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(UserViewModel.class);

        userViewModel.clearLiveData();

    }

    /**
     * Inflates the login layout.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /**
     * Sets up UI references, observers, and button listeners after view creation.
     *
     * @param view               The root view.
     * @param savedInstanceState Saved state bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeUserViewModel();

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvEmailError = view.findViewById(R.id.tvEmailError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        btnLogin.setOnClickListener(v -> performLogin());

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> Navigation.findNavController(v)
                    .navigate(R.id.action_loginFragment_to_forgotPasswordFragment));
        }
    }

    /**
     * Observes the UserViewModel for session updates.
     * Handles successful login, user not found (redirects to registration or retry), and error scenarios.
     */
    private void observeUserViewModel(){
        if (userViewModel == null) return;
        userViewModel.getSessionLiveData().observe(getViewLifecycleOwner(), result -> {
            if(result == null) return;
            hideLoading();
            if(result.isSessionSuccess()){
                showLoginSuccessDialog();
            } else if(result.isNetworkError()){
                showErrorDialog(getString(R.string.network_error), true);
                userViewModel.clearSessionLiveData();
            } else if(result.isLoginError()){
                showErrorDialog(getString(R.string.wrong_credentials), false);
                userViewModel.clearSessionLiveData();
            } else if(result.isUserNotFound()){
                showErrorDialog(getString(R.string.user_not_found), false);
                userViewModel.clearSessionLiveData();
            }
        });
    }

    /**
     * Validates input fields and triggers the email/password login process.
     */
    private void performLogin() {
        resetErrors();

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty()) {
            showError(etEmail, tvEmailError, getString(R.string.please_enter_a_valid_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvEmailError, getString(R.string.ops_invalid_email_address));
            isValid = false;
        }
        if (password.isEmpty()) {
            showError(etPassword, tvPasswordError, getString(R.string.password_must_be_6) );
            isValid = false;
        }

        if (!isValid) return;

        showLoading();

        userViewModel.signInWithEmailAndPassword(email, password);
    }

    /**
     * Navigates to the MainActivity and clears the back stack.
     */
    private void goToHome() {
        if (getContext() == null) return;
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Displays a success dialog upon successful login.
     * Provides navigation to the main application screen.
     */
    private void showLoginSuccessDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_success, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnAction = view.findViewById(R.id.btnAction);

        if (tvTitle != null) tvTitle.setText(getString(R.string.welcome_back));
        if (tvMessage != null) tvMessage.setText(getString(R.string.logged_in));
        if (btnAction != null) {
            btnAction.setText(getString(R.string.go_home));
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                goToHome();
            });
        }

        dialog.show();
    }
    /**
     * Displays a error dialog with the provided message.
     *
     * @param message The error message to display.
     * @param connectionError Whether the error is related to network connectivity, to choose the appropriate layout.
     */
    private void showErrorDialog(String message, Boolean connectionError) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(
                connectionError ? R.layout.dialog_connection_error : R.layout.dialog_generic_error,
                null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(true);

        TextView tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
        }

        Button btnOk = view.findViewById(R.id.btnOk);
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                etEmail.requestFocus();
            });
        }
        dialog.show();
    }

    /**
     * Displays an error message for a specific input field and highlights it.
     *
     * @param field     The EditText to highlight.
     * @param errorText The TextView to show the error message.
     * @param message   The error message content.
     */
    private void showError(EditText field, TextView errorText, String message) {
        if (field != null) {
            field.setBackgroundResource(R.drawable.bg_input_error);
        }
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Resets visual error indicators on input fields.
     */
    private void resetErrors() {
        if (etEmail != null) etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        if (tvEmailError != null) tvEmailError.setVisibility(View.GONE);
        if (etPassword != null) etPassword.setBackgroundResource(R.drawable.bg_input_outline);
        if (tvPasswordError != null) tvPasswordError.setVisibility(View.GONE);
    }

    /**
     * Shows the loading overlay.
     */
    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides the loading overlay.
     */
    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
}