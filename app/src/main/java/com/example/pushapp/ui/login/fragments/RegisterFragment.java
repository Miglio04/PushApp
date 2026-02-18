package com.example.pushapp.ui.login.fragments;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.ui.login.QuestionsActivity;

import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * Fragment responsible for user registration functionality.
 * Supports email/password registration and Google Sign-In integration.
 * Handles validation, loading states, and navigation upon success.
 */
public class RegisterFragment extends Fragment {

    private EditText etEmail, etPassword, etConfirmPassword;
    private TextView tvEmailError, tvPasswordError, tvConfirmError;
    private AppCompatButton btnRegister;
    private AppCompatButton btnGoogle;

    private LinearLayout loadingOverlay;
    private TextView tvLoadingText;

    private GoogleSignInClient mGoogleSignInClient;

    private UserViewModel userViewModel;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    showLoading(false, null);
                }

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        showLoading(false, null);
                        Toast.makeText(requireContext(), "Google Error: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
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

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> signInWithGoogle());
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
        btnGoogle = view.findViewById(R.id.btnGoogle);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        tvLoadingText = view.findViewById(R.id.tvLoadingText);
    }

    /**
     * Observes session LiveData for registration errors.
     */
    public void observeSessionLiveData(){
        userViewModel.getSessionLiveData().observe(getViewLifecycleOwner(), result -> {
           if(result == null) return;
           if (result.isRegistrationError()) {
               showLoading(false, null);
               Result.Error.RegistrationError error = (Result.Error.RegistrationError) result;
               Toast.makeText(requireContext(), "Registration error: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
                Result.Error.LocalDatabaseError error = (Result.Error.LocalDatabaseError) result;
                Toast.makeText(requireContext(), "Local database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                userViewModel.clearUserLiveData();
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
     * Initiates the Google Sign-In flow.
     */
    private void signInWithGoogle() {
        showLoading(true, getString(R.string.connecting_to_google));

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    /**
     * Delegates Google authentication to the ViewModel using the ID token.
     *
     * @param idToken The Google ID token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        if(tvLoadingText != null) tvLoadingText.setText(getString(R.string.authenticating));
        userViewModel.registerWithGoogle(idToken);
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
                btnGoogle.setEnabled(false);
                if (tvLoadingText != null && message != null) {
                    tvLoadingText.setText(message);
                }
            } else {
                loadingOverlay.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                btnGoogle.setEnabled(true);
            }
        }
    }

    /**
     * Displays an error message for a specific input field.
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