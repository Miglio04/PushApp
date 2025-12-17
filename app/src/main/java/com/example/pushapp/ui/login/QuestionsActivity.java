package com.example.pushapp.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import android.widget.ViewFlipper;

import androidx.appcompat.app.AlertDialog; // Importante per il popup
import androidx.appcompat.app.AppCompatActivity;

import com.example.pushapp.R;
// Assicurati di avere la tua MainActivity creata o usa il nome corretto
import com.example.pushapp.ui.main.MainActivity;

public class QuestionsActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ProgressBar progressBar;
    private TextView tvStepCounter, tvProgressPercentage;
    private Button btnBack, btnNext;

    // Campi Form
    private EditText etName, etSurname, etAge, etWeight, etHeight, etGoalWeight;
    private TextView tvNameError, tvSurnameError, tvAgeError, tvWeightError, tvHeightError, tvGoalError;
    private RadioGroup radioGroupGender;
    private TextView tvGenderError;

    private int currentStep = 0;
    private final int TOTAL_STEPS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        initializeViews();
        updateProgress();

        btnBack.setOnClickListener(v -> navigateBack());
        btnNext.setOnClickListener(v -> navigateNext());
    }

    private void initializeViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        progressBar = findViewById(R.id.progressBar);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        // Step 1
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        tvNameError = findViewById(R.id.tvNameError);
        tvSurnameError = findViewById(R.id.tvSurnameError);

        // Step 2
        radioGroupGender = findViewById(R.id.radioGroupGender);
        tvGenderError = findViewById(R.id.tvGenderError);

        // Step 3
        etAge = findViewById(R.id.etAge);
        tvAgeError = findViewById(R.id.tvAgeError);

        // Step 4
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        tvWeightError = findViewById(R.id.tvWeightError);
        tvHeightError = findViewById(R.id.tvHeightError);

        // Step 5
        etGoalWeight = findViewById(R.id.etGoalWeight);
        tvGoalError = findViewById(R.id.tvGoalError);
    }

    // --- NAVIGAZIONE ---

    private void navigateNext() {
        if (!validateCurrentStep()) {
            return;
        }

        if (currentStep < TOTAL_STEPS - 1) {
            currentStep++;
            viewFlipper.showNext();
            updateProgress();
        } else if (currentStep == TOTAL_STEPS - 1) {
            // ULTIMO STEP: Invece di chiudere subito, mostriamo il popup!
            saveDataAndShowPopup();
        }
    }

    private void navigateBack() {
        if (currentStep > 0) {
            currentStep--;
            viewFlipper.showPrevious();
            updateProgress();
        }
    }

    private void updateProgress() {
        int progress = (currentStep + 1) * 100 / TOTAL_STEPS;
        progressBar.setProgress(progress);
        tvStepCounter.setText("Step " + (currentStep + 1) + " of " + TOTAL_STEPS);
        tvProgressPercentage.setText(progress + "%");

        if (currentStep == 0) {
            btnBack.setVisibility(View.GONE);
        } else {
            btnBack.setVisibility(View.VISIBLE);
        }

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("FINISH");
        } else {
            btnNext.setText("Next  >");
        }
    }

    // --- VALIDAZIONE ---

    private boolean validateCurrentStep() {
        resetErrors();
        switch (currentStep) {
            case 0: return validateStep1_Name();
            case 1: return validateStep2_Gender();
            case 2: return validateStep3_Age();
            case 3: return validateStep4_Measurements();
            case 4: return validateStep5_Goal();
            default: return true;
        }
    }

    // (Qui ho mantenuto le tue validazioni, copiate per intero per non rompere nulla)
    private boolean validateStep1_Name() {
        boolean isValid = true;
        if (TextUtils.isEmpty(etName.getText())) {
            showError(etName, tvNameError, "First name is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(etSurname.getText())) {
            showError(etSurname, tvSurnameError, "Last name is required.");
            isValid = false;
        }
        return isValid;
    }

    private boolean validateStep2_Gender() {
        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            showError(null, tvGenderError, "Please select a gender.");
            return false;
        }
        return true;
    }

    private boolean validateStep3_Age() {
        String ageStr = etAge.getText().toString();
        if (TextUtils.isEmpty(ageStr) || Integer.parseInt(ageStr) < 16 || Integer.parseInt(ageStr) > 99) {
            showError(etAge, tvAgeError, "Please enter a valid age (16-99).");
            return false;
        }
        return true;
    }

    private boolean validateStep4_Measurements() {
        boolean isValid = true;
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();

        if (TextUtils.isEmpty(weightStr) || Double.parseDouble(weightStr) < 20) {
            showError(etWeight, tvWeightError, "Check weight.");
            isValid = false;
        }
        if (TextUtils.isEmpty(heightStr) || Integer.parseInt(heightStr) < 100) {
            showError(etHeight, tvHeightError, "Check height.");
            isValid = false;
        }
        return isValid;
    }

    private boolean validateStep5_Goal() {
        String goalStr = etGoalWeight.getText().toString();
        if (TextUtils.isEmpty(goalStr) || Double.parseDouble(goalStr) < 20) {
            showError(etGoalWeight, tvGoalError, "Please enter a target weight.");
            return false;
        }
        return true;
    }

    private void showError(EditText field, TextView errorText, String message) {
        if (field != null) field.setBackgroundResource(R.drawable.bg_input_error);
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    private void resetErrors() {
        if(etName!=null) etName.setBackgroundResource(R.drawable.bg_input_outline);
        if(tvNameError!=null) tvNameError.setVisibility(View.GONE);
        if(etSurname!=null) etSurname.setBackgroundResource(R.drawable.bg_input_outline);
        if(tvSurnameError!=null) tvSurnameError.setVisibility(View.GONE);

        if(tvGenderError!=null) tvGenderError.setVisibility(View.GONE);

        if(etAge!=null) etAge.setBackgroundResource(R.drawable.bg_input_outline);
        if(tvAgeError!=null) tvAgeError.setVisibility(View.GONE);

        if(etWeight!=null) etWeight.setBackgroundResource(R.drawable.bg_input_outline);
        if(tvWeightError!=null) tvWeightError.setVisibility(View.GONE);
        if(etHeight!=null) etHeight.setBackgroundResource(R.drawable.bg_input_outline);
        if(tvHeightError!=null) tvHeightError.setVisibility(View.GONE);

        if(etGoalWeight!=null) etGoalWeight.setBackgroundResource(R.drawable.bg_input_outline);
        if(tvGoalError!=null) tvGoalError.setVisibility(View.GONE);
    }

    // --- SALVATAGGIO E POPUP FINALE ---

    private void saveDataAndShowPopup() {
        // 1. QUI SALVERESTI I DATI SU FIREBASE (Lo faremo dopo se vuoi)
        // Esempio: saveUserDataToFirebase(name, age, weight...);

        // 2. MOSTRA IL POPUP DI COMPLETAMENTO
        showProfileCompletedDialog();
    }

    private void showProfileCompletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Usiamo lo stesso layout "dialog_success" che abbiamo fatto per la registrazione
        View view = getLayoutInflater().inflate(R.layout.dialog_success, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false); // L'utente DEVE premere il bottone

        // Troviamo i componenti del layout per cambiare il testo
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnAction = view.findViewById(R.id.btnAction);

        // Qui cambiamo le scritte rispetto alla registrazione
        tvTitle.setText("Profile Completed! ");
        tvMessage.setText("Your data has been saved.\nYou are ready to start training.");
        btnAction.setText("GO TO HOME");

        btnAction.setOnClickListener(v -> {
            dialog.dismiss();

            // VAI ALLA MAIN ACTIVITY (HOME)
            Intent intent = new Intent(QuestionsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}