package com.example.pushapp.ui.login;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pushapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private TextView tvEmailError, tvBackToLogin;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();

        // Click on SEND
        btnSend.setOnClickListener(v -> {
            resetUI();
            if (validateEmail()) {
                sendResetEmail();
            }
        });

        // Click on BACK TO LOGIN
        tvBackToLogin.setOnClickListener(v -> {
            finish(); // Closes this activity and goes back to Login
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmailReset);
        tvEmailError = findViewById(R.id.tvEmailErrorReset);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        btnSend = findViewById(R.id.btnSendReset);
    }

    private void resetUI() {
        etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        tvEmailError.setVisibility(View.GONE);
    }

    private boolean validateEmail() {
        String emailInput = etEmail.getText().toString().trim();

        if (emailInput.isEmpty()) {
            etEmail.setBackgroundResource(R.drawable.bg_input_error);
            tvEmailError.setText(R.string.email_required);
            tvEmailError.setVisibility(View.VISIBLE);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            etEmail.setBackgroundResource(R.drawable.bg_input_error);
            // Changed to English resource
            tvEmailError.setText(R.string.ops_invalid_email_address);
            tvEmailError.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private void sendResetEmail() {
        // HERE: Backend Logic (e.g., Firebase auth.sendPasswordResetEmail)
        String email = etEmail.getText().toString().trim();

        // Simulate success
        showSuccessDialog();
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success); // Reusing the same success dialog

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        Button btnOkay = dialog.findViewById(R.id.btnOkay);

        // Text set to English using Resources
        tvTitle.setText(R.string.check_mail_title);
        tvMessage.setText(getString(R.string.email_sent_success));

        btnOkay.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Return to login after clicking OK
        });

        dialog.show();
    }
}