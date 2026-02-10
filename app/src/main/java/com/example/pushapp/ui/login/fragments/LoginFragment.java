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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.ui.main.MainActivity;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {
    private EditText etEmail, etPassword;
    private TextView tvEmailError, tvPasswordError;
    private LinearLayout loadingOverlay;
    private UserViewModel userViewModel;
    private GoogleSignInClient mGoogleSignInClient;

    // --- Launcher per il risultato del login con Google ---
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(requireContext(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        hideLoading();
                    }
                } else {
                    hideLoading();
                }
            }
    );

    public LoginFragment() {
        // Costruttore vuoto obbligatorio
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(UserViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeUserViewModel();

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        tvEmailError = view.findViewById(R.id.tvEmailError);
        tvPasswordError = view.findViewById(R.id.tvPasswordError);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGoogle = view.findViewById(R.id.btnGoogle);
        TextView tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        btnLogin.setOnClickListener(v -> performLogin());
        btnGoogle.setOnClickListener(v -> {
            showLoading();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
            });
        }
    }

    // provvisorio: non distingue se sono sbagliate le credenziali oppure se l'utente non esiste
    private void observeUserViewModel(){
        userViewModel.getSessionLiveData().observe(getViewLifecycleOwner(), userId -> {
            hideLoading();
            if(userId.isSessionSuccess()){
                showLoginSuccessDialog();
            }else{
                Toast.makeText(requireContext(), ((Result.Error) userId).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        resetErrors();

        // data validation: da spostare in un metodo apposito
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isValid = true;

        if (email.isEmpty()) {
            showError(etEmail, tvEmailError, "Please enter your email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, tvEmailError, "Invalid email format");
            isValid = false;
        }

        if (password.isEmpty()) {
            showError(etPassword, tvPasswordError, "Please enter your password");
            isValid = false;
        }

        if (!isValid) return;

        showLoading();

        userViewModel.signInWithEmailAndPassword(email, password);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // SUCCESSO CON GOOGLE -> Mostra il popup di benvenuto
                        hideLoading();
                        showLoginSuccessDialog();
                    } else {
                        hideLoading();
                        Toast.makeText(requireContext(), "Google Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- NUOVO METODO CHE MOSTRA IL POPUP DI SUCCESSO DEL LOGIN ---
    private void showLoginSuccessDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // Riutilizziamo lo stesso layout della registrazione
        View view = getLayoutInflater().inflate(R.layout.dialog_success, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        // Recuperiamo i componenti del popup per modificarli
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnAction = view.findViewById(R.id.btnAction);

        // Personalizziamo testi e azione per il LOGIN
        if (tvTitle != null) tvTitle.setText("Welcome Back!");
        if (tvMessage != null) tvMessage.setText("You are now successfully logged in.");
        if (btnAction != null) {
            btnAction.setText("GO TO HOME");
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                goToHome(); // Chiamiamo il metodo che va alla MainActivity
            });
        }

        dialog.show();
    }
    // ----------------------------------------------------

    // Il metodo goToHome non prende più "user" come parametro
    private void goToHome() {
        if (getContext() == null) return;
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // --- Il resto dei metodi (showUserNotFound, showError, ecc.) rimangono uguali ---
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
                Navigation.findNavController(getView()).navigate(R.id.registerFragment);
            });
        }
        View btnTryAgain = view.findViewById(R.id.btnTryAgain);
        if (btnTryAgain != null) {
            btnTryAgain.setOnClickListener(v -> {
                dialog.dismiss();
                etEmail.requestFocus();
            });
        }
        dialog.show();
    }

    private void showError(EditText field, TextView errorText, String message) {
        if (field != null) {
            field.setBackgroundResource(R.drawable.bg_input_error);
        }
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
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
}
