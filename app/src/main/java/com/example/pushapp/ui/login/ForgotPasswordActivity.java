package com.example.pushapp.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.pushapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private TextView tvError;
    private LinearLayout loadingOverlay;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmailReset);
        tvError = findViewById(R.id.tvResetError);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        AppCompatButton btnReset = findViewById(R.id.btnResetPassword);
        TextView btnBack = findViewById(R.id.btnBack);

        // Click: Torna indietro
        btnBack.setOnClickListener(v -> finish());

        // Click: Invia Mail
        btnReset.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        // Pulisce errori precedenti
        etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        tvError.setVisibility(View.GONE);

        String email = etEmail.getText().toString().trim();

        // Validazione Input
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setBackgroundResource(R.drawable.bg_input_error);
            tvError.setText("@string/please_enter_a_valid_email");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    loadingOverlay.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // SUCCESSO: L'utente esiste -> Mostra Popup Verde
                        showSuccessDialog(email);
                    } else {
                        // ERRORE: Controlliamo se l'utente non esiste
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // Utente non trovato -> Mostra Popup "Registrati"
                            showUserNotFoundDialog();
                        } else {
                            // Altri errori (es. Internet)
                            String error = task.getException() != null ? task.getException().getMessage() : "Error sending email.";
                            tvError.setText(error);
                            tvError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // --- POPUP 1: MAIL INVIATA (SUCCESSO) ---
    private void showSuccessDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        // Controlla ID bottone (btnAction o btnOkay nel tuo XML)
        Button btnAction = view.findViewById(R.id.btnAction);
        // Se nel tuo XML è btnOkay, cambia la riga sopra.

        tvTitle.setText("Check your Email!");
        tvMessage.setText("We sent a password reset link to:\n" + email);

        if (btnAction != null) {
            btnAction.setText("BACK TO LOGIN");
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                finish(); // Torna al login
            });
        }

        dialog.show();
    }

    // --- POPUP 2: UTENTE NON TROVATO (ERRORE) ---
    private void showUserNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Usiamo lo stesso layout "Account Not Found" del Login
        View view = getLayoutInflater().inflate(R.layout.dialog_account_not_found, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(true);

        // Bottone "Registrati Ora"
        Button btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            dialog.dismiss();
            // Vai alla registrazione
            Intent intent = new Intent(ForgotPasswordActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish(); // Chiudi password dimenticata
        });

        // Bottone "Riprova"
        View btnTryAgain = view.findViewById(R.id.btnTryAgain);
        btnTryAgain.setOnClickListener(v -> {
            dialog.dismiss();
            etEmail.requestFocus();
        });

        dialog.show();
    }
}