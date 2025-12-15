package com.example.pushapp.ui.login;

import com.example.pushapp.ui.main.MainActivity;
import com.example.pushapp.R;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tabRegister = findViewById(R.id.tabRegister);

        // Find the Forgot Password TextView (Make sure ID matches your XML)
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 2. Click on "Sign Up" tab (Switch to Register screen)
        tabRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(0, 0); // No animation
            finish();
        });

        // 2.5 Click on "Forgot Password" (New functionality)
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // 3. Click Login Button
        btnLogin.setOnClickListener(v -> {
            // Reset Errors
            resetErrors();

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            boolean hasError = false;

            // --- Email Validation ---
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(etEmail, tvEmailError, getString(R.string.please_enter_a_valid_email));
                hasError = true;
            }

            // --- Password Validation ---
            if (password.isEmpty() || password.length() < 8) {
                showError(etPassword, tvPasswordError, getString(R.string.password_is_too_short_min_8_chars));
                hasError = true;
            }

            // --- IF EVERYTHING IS VALID ---
            if (!hasError) {
                // SUCCESS: Show the nice Dialog
                showSuccessDialog();
            }
        });
    }

    // Helper method to show red error border and text
    private void showError(EditText field, TextView errorText, String message) {
        field.setBackgroundResource(R.drawable.bg_input_error);
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    // Helper method to reset UI to normal state
    private void resetErrors() {
        etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        tvEmailError.setVisibility(View.GONE);
        etPassword.setBackgroundResource(R.drawable.bg_input_outline);
        tvPasswordError.setVisibility(View.GONE);
    }

    // === SUCCESS DIALOG ===
    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);

        // Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false); // User must click the button

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        Button btnOkay = dialog.findViewById(R.id.btnOkay);

        tvTitle.setText(R.string.welcome_back);
        tvMessage.setText(R.string.login_successful);

        btnOkay.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}