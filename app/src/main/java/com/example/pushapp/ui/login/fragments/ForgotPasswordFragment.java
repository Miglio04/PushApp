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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private TextView tvError;
    private LinearLayout loadingOverlay;
    private FirebaseAuth mAuth;
    private UserViewModel userViewModel;

    public ForgotPasswordFragment() {
        // Costruttore vuoto richiesto
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(UserViewModel.class);
        userViewModel.clearLiveData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate del layout per questo fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeSessionLiveData();

        // Collegamento viste
        etEmail = view.findViewById(R.id.etEmailReset);
        tvError = view.findViewById(R.id.tvResetError);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        AppCompatButton btnReset = view.findViewById(R.id.btnResetPassword);
        TextView btnBack = view.findViewById(R.id.btnBack); // Assumendo che sia una TextView o ImageButton

        // Click: Torna indietro (Navigation)
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Click: Invia Mail
        btnReset.setOnClickListener(v -> sendResetEmail());
    }

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

    private void sendResetEmail() {
        // Pulisce errori precedenti
        etEmail.setBackgroundResource(R.drawable.bg_input_outline);
        tvError.setVisibility(View.GONE);

        String email = etEmail.getText().toString().trim();

        // Validazione Input
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setBackgroundResource(R.drawable.bg_input_error);
            // Nota: R.string.please_enter_a_valid_email dovrebbe essere usato con getString(),
            // qui ho messo il testo fisso per sicurezza come nel tuo codice originale
            tvError.setText("Please enter a valid email address");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        userViewModel.sendPasswordResetEmail(email);

    }


    // --- POPUP 1: MAIL INVIATA (SUCCESSO) ---
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
                // Torna al login usando Navigation
                Navigation.findNavController(getView()).navigateUp();
            });
        }

        dialog.show();
    }

    // --- POPUP 2: UTENTE NON TROVATO (ERRORE) ---
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

        // Bottone "Registrati Ora"
        Button btnRegister = view.findViewById(R.id.btnRegister);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                dialog.dismiss();
                // Vai alla registrazione (Action definita nel nav graph)
                // Assicurati che l'ID corrisponda a quello nel nav_graph_auth.xml
                // Se non hai un action diretta da Forgot a Register, torna al login e poi vai a register,
                // oppure aggiungi l'action nel graph.

                // Opzione sicura: Torna indietro (Login) e l'utente andrà su Register
                Navigation.findNavController(getView()).navigateUp();

                // Opzione diretta (Se hai aggiunto l'action nel graph):
                // Navigation.findNavController(getView()).navigate(R.id.action_forgotPasswordFragment_to_registerFragment);
            });
        }

        // Bottone "Riprova"
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
