package com.example.pushapp.ui.login;

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

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tabLogin = findViewById(R.id.tabLogin);

        // 2. Click on "Login" tab (Switch back to Login screen)
        tabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // No animation for tab switch effect
                finish();
            }
        });

        // 3. Click on "Create Account"
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                boolean hasError = false;

                // Reset Errors (Clear previous error states)
                etEmail.setBackgroundResource(R.drawable.bg_input_outline);
                tvEmailError.setVisibility(View.GONE);
                etPassword.setBackgroundResource(R.drawable.bg_input_outline);
                tvPasswordError.setVisibility(View.GONE);

                // --- Email Validation ---
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setBackgroundResource(R.drawable.bg_input_error);
                    tvEmailError.setText(R.string.ops_invalid_email_address );
                    tvEmailError.setVisibility(View.VISIBLE);
                    hasError = true;
                }

                // --- Password Validation ---
                if (password.isEmpty() || password.length() < 8) {
                    etPassword.setBackgroundResource(R.drawable.bg_input_error);
                    tvPasswordError.setText(R.string.password_must_be_at_least_8_characters);
                    tvPasswordError.setVisibility(View.VISIBLE);
                    hasError = true;
                }

                // --- IF EVERYTHING IS VALID ---
                if (!hasError) {
                    // Show the Success Dialog and then go to QuestionsActivity
                    showSuccessDialog();
                }
            }
        });
    }

    // --- Helper Method: Show Success Dialog ---
    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);

        // Make the background transparent to show rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false); // User must click the button

        // Init Dialog Views
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        Button btnOkay = dialog.findViewById(R.id.btnOkay);

        // Set Text
        tvTitle.setText(R.string.great);
        tvMessage.setText(R.string.account_created_successfully);

        // Handle Click
        btnOkay.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(RegisterActivity.this, QuestionsActivity.class);
            startActivity(intent);
            finish(); // Close registration so user can't go back
        });

        dialog.show();
    }
}