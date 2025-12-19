package com.example.pushapp.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.pushapp.R;
import com.example.pushapp.ui.main.MainActivity; // Assicurati che il percorso sia giusto
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError;
    private LinearLayout loadingOverlay;

    // Firebase & Google
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001; // Codice identificativo per Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // --- 1. CONFIGURAZIONE GOOGLE ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase gestisce questo ID in automatico
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();

        // Navigazione verso Sign Up (Registrazione)
        TextView tabRegister = findViewById(R.id.tabRegister);
        tabRegister.setOnClickListener(v -> goToRegister());

        // Password Dimenticata
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Bottone LOGIN CLASSICO
        AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> handleLogin());

        // --- 2. BOTTONE GOOGLE (ORA ATTIVO) ---
        AppCompatButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void initializeViews() {
        // Nota: Assicurati che gli ID nel tuo XML siano etEmail, etPassword, ecc.
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    // --------------------------------------------------------
    // LOGICA GOOGLE SIGN-IN
    // --------------------------------------------------------

    private void signInWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Risultato del lancio dell'intent di Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Login Google riuscito, ora autentichiamo su Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                // Login fallito (es. SHA-1 mancante o utente ha annullato)
                showLoading(false);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Successo! Andiamo alla Home
                        FirebaseUser user = mAuth.getCurrentUser();
                        goToHome(user);
                    } else {
                        // Errore Firebase
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --------------------------------------------------------
    // LOGICA LOGIN CLASSICO (EMAIL/PASSWORD)
    // --------------------------------------------------------

    private void handleLogin() {
        resetErrors();

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showInputError(etEmail, tvEmailError, "Please enter a valid email address.");
            isValid = false;
        }

        if (password.isEmpty()) {
            showInputError(etPassword, tvPasswordError, "Password cannot be empty.");
            isValid = false;
        }

        if (!isValid) return;

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Nota: Non nascondo subito il loading se ha successo, per evitare sfarfallii mentre cambia schermata

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        goToHome(user);
                    } else {
                        showLoading(false);

                        // Gestione Errori Avanzata
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            showUserNotFoundDialog();
                        } else {
                            showInputError(etPassword, tvPasswordError, "Login failed. Check password.");
                        }
                    }
                });
    }

    // --- NAVIGAZIONE COMUNE (USATA SIA DA GOOGLE CHE DA EMAIL) ---
    private void goToHome(FirebaseUser user) {
        showLoading(false);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        // Passiamo il nome utente (se disponibile) per il benvenuto
        String displayName = (user != null) ? user.getDisplayName() : "";
        intent.putExtra("NOME_UTENTE", displayName);
        intent.putExtra("SHOW_WELCOME", true);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    // --- POPUP: ACCOUNT NON TROVATO ---
    private void showUserNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_account_not_found, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(true);

        Button btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            dialog.dismiss();
            goToRegister();
        });

        View btnTryAgain = view.findViewById(R.id.btnTryAgain);
        btnTryAgain.setOnClickListener(v -> {
            dialog.dismiss();
            etEmail.requestFocus();
        });

        dialog.show();
    }

    // --- UI HELPERS ---

    private void showLoading(boolean isLoading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showInputError(EditText field, TextView errorText, String message) {
        field.setBackgroundResource(R.drawable.bg_input_error);
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void resetErrors() {
        etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        tvEmailError.setVisibility(View.GONE);
        etPassword.setBackgroundResource(R.drawable.bg_input_outline);
        tvPasswordError.setVisibility(View.GONE);
    }
}