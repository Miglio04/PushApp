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
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.R;
import com.example.pushapp.ui.login.QuestionsActivity;

import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;

/**
 * Fragment responsible for user registration functionality.
 * Supports email/password registration and Google Sign-In integration.
 * Handles validation, loading states, and navigation upon success.
 */
public class RegisterFragment extends Fragment {

    private EditText etEmail, etPassword, etConfirmPassword;
    private TextView tvEmailError, tvPasswordError, tvConfirmError;
    private AppCompatButton btnRegister;
    private LinearLayout loadingOverlay;
    private TextView tvLoadingText;

    private UserViewModel userViewModel;

    public RegisterFragment() {}

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
     * Inflates the registration layout.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
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

        initializeViews(view);
        observeSessionLiveData();
        observeUserLiveData();

         if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> handleRegistration());
        }
    }

    /**
     * Initializes UI components from the layout.
     *
     * @param view The root view.
     */
    private void initializeViews(View view) {
        etEmail = view.findViewById(R.id.etEmailRegister);
        etPassword = view.findViewById(R.id.etPasswordRegister);
        etConfirmPassword = view.findViewById(R.id.etConfirmPasswordRegister);

        tvEmailError = view.findViewById(R.id.tvEmailError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);
        tvConfirmError = view.findViewById(R.id.tvConfirmPasswordError);

        btnRegister = view.findViewById(R.id.btnRegister);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        tvLoadingText = view.findViewById(R.id.tvLoadingText);
    }

    /**
     * Observes session LiveData for registration errors.
     */
    public void observeSessionLiveData(){
        userViewModel.getSessionLiveData().observe(getViewLifecycleOwner(), result -> {
           if(result == null) return;
           showLoading(false, null);
           if(result.isNetworkError()){
               showErrorDialog(getString(R.string.network_error), true);
               userViewModel.clearSessionLiveData();
           } else if (result.isRegistrationError()) {
               showErrorDialog(getString(R.string.email_already_registered), false);
               userViewModel.clearSessionLiveData();
           }
        });
    }

    /**
     * Observes user LiveData for successful registration or local database errors.
     */
    public void observeUserLiveData(){
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), result -> {
            if(result == null) return;
            showLoading(false, null);
            if (result.isUserSuccess()) {
                showSuccessDialog();
            } else if (result.isLocalDatabaseError()) {
                showErrorDialog(getString(R.string.something_went_wrong), false);
                userViewModel.clearSessionLiveData();
            }
        });
    }

    /**
     * Validates input fields and triggers the email/password registration process.
     */
    private void handleRegistration() {
        resetErrors();

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvEmailError, getString(R.string.please_enter_a_valid_email));
            isValid = false;
        }
        if (password.isEmpty() || password.length() < 6) {
            showError(etPassword, tvPasswordError, getString(R.string.password_must_be_6));
            isValid = false;
        }
        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, tvConfirmError, getString(R.string.passwords_do_not_match));
            isValid = false;
        }

        if (!isValid) return;

        showLoading(true, getString(R.string.creating_account));

        userViewModel.registerWithEmailAndPassword(email, password);
    }

    /**
     * Navigates to the QuestionsActivity for user profiling.
     */
    private void goToQuestionsActivity() {
        if (getContext() == null) return;

        Intent intent = new Intent(requireContext(), QuestionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Displays a success dialog upon registration completion.
     */
    private void showSuccessDialog() {
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

        if (tvTitle != null) tvTitle.setText(getString(R.string.welcome));

        if (tvMessage != null) {
            tvMessage.setText(getString(R.string.account_created_successfully_ready));
        }

        if (btnAction != null) {
            btnAction.setText(getString(R.string.start_setup));
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                goToQuestionsActivity();
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
     * Toggles the visibility of the loading overlay.
     *
     * @param isLoading True to show loading, false to hide.
     * @param message   Optional message to display during loading.
     */
    private void showLoading(boolean isLoading, String message) {
        if (loadingOverlay != null) {
            if (isLoading) {
                loadingOverlay.setVisibility(View.VISIBLE);
                btnRegister.setEnabled(false);
                if (tvLoadingText != null && message != null) {
                    tvLoadingText.setText(message);
                }
            } else {
                loadingOverlay.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
            }
        }
    }

    /**
     * Displays an error message for an invalid input field.
     *
     * @param field     The EditText to highlight.
     * @param errorText The TextView to show the error message.
     * @param message   The error message content.
     */
    private void showError(EditText field, TextView errorText, String message) {
        if (field != null) field.setBackgroundResource(R.drawable.bg_input_error);
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
        if (etConfirmPassword != null) etConfirmPassword.setBackgroundResource(R.drawable.bg_input_outline);
        if (tvConfirmError != null) tvConfirmError.setVisibility(View.GONE);
    }


}