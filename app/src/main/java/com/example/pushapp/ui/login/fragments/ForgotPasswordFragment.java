package com.example.pushapp.ui.login.fragments;

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
import androidx.navigation.Navigation;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;

/**
 * Fragment handling the password reset flow.
 * Allows users to request a password reset email and handles UI feedback for success or error states.
 */
public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private TextView tvError;
    private LinearLayout loadingOverlay;
    private UserViewModel userViewModel;

    public ForgotPasswordFragment() {}

    /**
     * Initializes the UserViewModel.
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
     * Inflates the layout for the forgot password screen.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view group.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
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

        observeSessionLiveData();

        etEmail = view.findViewById(R.id.etEmailReset);
        tvError = view.findViewById(R.id.tvResetError);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        AppCompatButton btnReset = view.findViewById(R.id.btnResetPassword);
        TextView btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        btnReset.setOnClickListener(v -> sendResetEmail());
    }

    /**
     * Observes the UserViewModel for password reset results.
     * Updates UI based on success, error, or user-not-found states.
     */
    private void observeSessionLiveData() {
        userViewModel.getSessionLiveData().observe(getViewLifecycleOwner(), result -> {
            if(result == null) return;
            if (result.isForgotPasswordError()) {
                Result.Error.ForgotPasswordError error = (Result.Error.ForgotPasswordError) result;
                tvError.setText(error.getMessage());
                tvError.setVisibility(View.VISIBLE);
                loadingOverlay.setVisibility(View.GONE);
                userViewModel.clearSessionLiveData();
            }else if(result.isForgotPasswordSuccess()){
                showSuccessDialog(((Result.PasswordResetSuccess) result).getEmail());
            }else if(result.isUserNotFound()){
                showUserNotFoundDialog();
                userViewModel.clearSessionLiveData();
            }
        });
    }

    /**
     * Validates the email input and triggers the password reset email request.
     */
    private void sendResetEmail() {
        etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        tvError.setVisibility(View.GONE);

        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setBackgroundResource(R.drawable.bg_input_error);
            tvError.setText("Please enter a valid email address");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        userViewModel.sendPasswordResetEmail(email);

    }

    /**
     * Displays a success dialog after the reset email has been sent.
     *
     * @param email The email address to which the link was sent.
     */
    private void showSuccessDialog(String email) {
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

        if (tvTitle != null) tvTitle.setText("Check your Email!");
        if (tvMessage != null) tvMessage.setText("We sent a password reset link to:\n" + email);

        if (btnAction != null) {
            btnAction.setText("BACK TO LOGIN");
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                Navigation.findNavController(getView()).navigateUp();
            });
        }

        dialog.show();
    }

    /**
     * Displays a dialog when the provided email does not correspond to an existing account.
     * Offers navigation to registration or retry.
     */
    private void showUserNotFoundDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_account_not_found, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(true);

        Button btnRegister = view.findViewById(R.id.btnRegister);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                dialog.dismiss();

                Navigation.findNavController(getView()).navigateUp();
            });
        }

        View btnTryAgain = view.findViewById(R.id.btnTryAgain);
        if (btnTryAgain != null) {
            btnTryAgain.setOnClickListener(v -> {
                dialog.dismiss();
                if (etEmail != null) etEmail.requestFocus();
            });
        }

        dialog.show();
    }
}
