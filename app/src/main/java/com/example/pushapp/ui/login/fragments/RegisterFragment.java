package com.example.pushapp.ui.login.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
// import androidx.navigation.Navigation; // Non serve più qui perché lo switch è gestito dall'Activity

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

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

    private UserViewModel userViewModel;

    // Launcher per il risultato del Google Sign-In
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

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(UserViewModel.class);

        userViewModel.clearLiveData();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Google Configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

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

    private void initializeViews(View view) {
        etEmail = view.findViewById(R.id.etEmailRegister);
        etPassword = view.findViewById(R.id.etPasswordRegister);
        etConfirmPassword = view.findViewById(R.id.etConfirmPasswordRegister);

        tvEmailError = view.findViewById(R.id.tvEmailError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);
        tvConfirmError = view.findViewById(R.id.tvConfirmPasswordError);

        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoogle = view.findViewById(R.id.btnGoogle);

        // Overlay
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        tvLoadingText = view.findViewById(R.id.tvLoadingText);
    }

    public void observeSessionLiveData(){
        userViewModel.getSessionLiveData().observe(getViewLifecycleOwner(), result -> {
           if(result == null) return;
           if (result.isRegistrationError()) {
               showLoading(false, null);
               Result.Error.RegistrationError error = (Result.Error.RegistrationError) result;
               Toast.makeText(requireContext(), "Registration error: " + error.getMessage(), Toast.LENGTH_LONG).show();
               userViewModel.clearSessionLiveData();
           } else if (result.isSessionSuccess()) {
               Log.d(TAG, "Registration successful");
           }
        });
    }

    public void observeUserLiveData(){
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), result -> {
            if(result == null) return;
            showLoading(false, null);
            if (result.isUserSuccess()) {
                Log.d(TAG, "Local database success");
                showSuccessDialog(false);
            } else if (result.isLocalDatabaseError()) {
                Log.d(TAG, "Local database error");
                Result.Error.LocalDatabaseError error = (Result.Error.LocalDatabaseError) result;
                // fatal exception
                Toast.makeText(requireContext(), "Local database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                userViewModel.clearUserLiveData();
            }
        });
    }

    private void handleRegistration() {
        resetErrors();

        // email validation should be moved into a separate method
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

        userViewModel.registerWithEmailAndPassword(email, password);
    }

    // to be moved into sessionRepository
    private void signInWithGoogle() {
        showLoading(true, "Connecting to Google...");

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // to be moved into sessionRepository
    private void firebaseAuthWithGoogle(String idToken) {
        if(tvLoadingText != null) tvLoadingText.setText("Authenticating...");
        userViewModel.registerWithGoogle(idToken);

        /*AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            createUserProfile(user.getUid(), user.getEmail(), true);
                        }
                    } else {
                        showLoading(false, null);
                        Toast.makeText(requireContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });*/
    }

    // to be moved into sessionRepository
    // method only used by Google Sign up
    /*private void createUserProfile(String uid, String email, boolean isGoogle) {
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
                    Toast.makeText(requireContext(), "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }*/

    private void showSuccessDialog(boolean isGoogle) {
        Log.d(TAG, "Showing success dialog");
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
        if (getContext() == null) return;

        Intent intent = new Intent(requireContext(), QuestionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
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
