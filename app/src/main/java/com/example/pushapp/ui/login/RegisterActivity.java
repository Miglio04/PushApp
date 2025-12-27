package com.example.pushapp.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.pushapp.R;
import com.example.pushapp.ui.login.QuestionsActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // UI Components
    private EditText etEmail, etPassword, etConfirmPassword;
    private TextView tvEmailError, tvPasswordError, tvConfirmError;
    private AppCompatButton btnRegister;
    private AppCompatButton btnGoogle;

    // Loading Components
    private LinearLayout loadingOverlay;
    private TextView tvLoadingText;

    // Firebase & Google
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeViews();

        // Google Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 1. Tab Login
        TextView tabLogin = findViewById(R.id.tabLogin);
        if (tabLogin != null) {
            tabLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // 2. Register Button
        btnRegister.setOnClickListener(v -> handleRegistration());

        // 3. Google Button
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmailRegister);
        etPassword = findViewById(R.id.etPasswordRegister);
        etConfirmPassword = findViewById(R.id.etConfirmPasswordRegister);

        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmError = findViewById(R.id.tvConfirmPasswordError);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Overlay
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvLoadingText = findViewById(R.id.tvLoadingText);
    }

    // ==========================================
    // EMAIL REGISTRATION LOGIC
    // ==========================================
    private void handleRegistration() {
        resetErrors();

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvEmailError, "Please enter a valid email address.");
            isValid = false;
        }
        if (password.isEmpty() || password.length() < 6) {
            showError(etPassword, tvPasswordError, "Password must be at least 6 characters.");
            isValid = false;
        }
        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, tvConfirmError, "Passwords do not match.");
            isValid = false;
        }

        if (!isValid) return;

        showLoading(true, "Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            createUserProfile(user.getUid(), user.getEmail(), false);
                        }
                    } else {
                        showLoading(false, null);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            showError(etEmail, tvEmailError, "This email is already registered.");
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                            Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ==========================================
    // GOOGLE REGISTRATION LOGIC
    // ==========================================
    private void signInWithGoogle() {
        showLoading(true, "Connecting to Google...");

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    showLoading(false, null);
                }

                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        showLoading(false, null);
                        Toast.makeText(this, "Google Error: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        if(tvLoadingText != null) tvLoadingText.setText("Authenticating...");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            createUserProfile(user.getUid(), user.getEmail(), true);
                        }
                    } else {
                        showLoading(false, null);
                        Toast.makeText(RegisterActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserProfile(String uid, String email, boolean isGoogle) {
        if (tvLoadingText != null) tvLoadingText.setText("Saving profile...");
        
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("createdAt", FieldValue.serverTimestamp());
        user.put("workoutPlans", new ArrayList<String>());
        user.put("weightProgress", new ArrayList<Double>());
        user.put("currentTrainingPlan", "");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created in Firestore for UID: " + uid);
                    showLoading(false, null);
                    showSuccessDialog(isGoogle);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile", e);
                    showLoading(false, null);
                    Toast.makeText(RegisterActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ==========================================
    // UTILITY: POPUP & LOADING
    // ==========================================
    private void showSuccessDialog(boolean isGoogle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        if (tvTitle != null) tvTitle.setText("Welcome!");

        if (tvMessage != null) {
            if (isGoogle) {
                tvMessage.setText("Account connected via Google.\nYou are ready to setup your profile.");
            } else {
                tvMessage.setText("Account created successfully.\nYou are ready to setup your profile.");
            }
        }

        if (btnAction != null) {
            btnAction.setText("START SETUP");
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                goToQuestionsActivity();
            });
        }

        dialog.show();
    }

    private void goToQuestionsActivity() {
        Intent intent = new Intent(RegisterActivity.this, QuestionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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

    private void showError(EditText field, TextView errorText, String message) {
        if (field != null) field.setBackgroundResource(R.drawable.bg_input_error);
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private void resetErrors() {
        if (etEmail != null) etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        if (tvEmailError != null) tvEmailError.setVisibility(View.GONE);
        if (etPassword != null) etPassword.setBackgroundResource(R.drawable.bg_input_outline);
        if (tvPasswordError != null) tvPasswordError.setVisibility(View.GONE);
        if (etConfirmPassword != null) etConfirmPassword.setBackgroundResource(R.drawable.bg_input_outline);
        if (tvConfirmError != null) tvConfirmError.setVisibility(View.GONE);
    }
}