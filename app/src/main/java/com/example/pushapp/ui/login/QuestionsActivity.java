package com.example.pushapp.ui.login;

import com.example.pushapp.ui.main.MainActivity;
import com.example.pushapp.R;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;

public class QuestionsActivity extends AppCompatActivity {

    // UI Components
    private ViewFlipper viewFlipper;
    private ProgressBar progressBar;
    private TextView tvStepCounter, tvProgressPercentage;
    private Button btnNext, btnBack;

    // Inputs
    private EditText etName, etSurname, etAge, etWeight, etHeight, etGoalWeight;
    private RadioGroup radioGroupGender;

    // Error TextViews
    private TextView tvNameError, tvSurnameError, tvGenderError, tvAgeError, tvWeightError, tvHeightError, tvGoalError;

    private int currentStep = 0;
    private final int TOTAL_STEPS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // 1. Initialize Views
        initViews();
        updateProgress();

        // 2. Click "Next"
        btnNext.setOnClickListener(v -> {
            // First, reset any visible errors in the current step
            resetErrors(currentStep);

            if (validateStep(currentStep)) {
                if (currentStep < TOTAL_STEPS - 1) {
                    // Go to Next Step
                    currentStep++;
                    viewFlipper.setInAnimation(QuestionsActivity.this, android.R.anim.slide_in_left);
                    viewFlipper.setOutAnimation(QuestionsActivity.this, android.R.anim.slide_out_right);
                    viewFlipper.showNext();
                    updateProgress();
                } else {
                    // LAST STEP: Save and Finish
                    saveAndClose();
                }
            }
        });

        // 3. Click "Back"
        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                resetErrors(currentStep); // Clean up errors before leaving
                currentStep--;
                viewFlipper.setInAnimation(QuestionsActivity.this, android.R.anim.slide_in_left);
                viewFlipper.setOutAnimation(QuestionsActivity.this, android.R.anim.slide_out_right);
                viewFlipper.showPrevious();
                updateProgress();
            }
        });
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        progressBar = findViewById(R.id.progressBar);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // Inputs
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        radioGroupGender = findViewById(R.id.radioGroupGender);

        // Error Views
        tvNameError = findViewById(R.id.tvNameError);
        tvSurnameError = findViewById(R.id.tvSurnameError);
        tvGenderError = findViewById(R.id.tvGenderError);
        tvAgeError = findViewById(R.id.tvAgeError);
        tvWeightError = findViewById(R.id.tvWeightError);
        tvHeightError = findViewById(R.id.tvHeightError);
        tvGoalError = findViewById(R.id.tvGoalError);
    }

    // Update Progress Bar and Text
    private void updateProgress() {
        int progress = (int) (((currentStep + 1) / (float) TOTAL_STEPS) * 100);
        progressBar.setProgress(progress);

        tvStepCounter.setText(getString(R.string.step_of, currentStep + 1, TOTAL_STEPS));

        // --- CORREZIONE QUI SOTTO ---
        // Prima era: tvProgressPercentage.setText(progress + (R.string.percent));
        // Ora usiamo getString() per ottenere il simbolo della percentuale
        tvProgressPercentage.setText(progress + getString(R.string.percent));

        if (currentStep == 0) {
            btnBack.setVisibility(View.GONE);
        } else {
            btnBack.setVisibility(View.VISIBLE);
        }

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText(R.string.finish);
        } else {
            btnNext.setText(R.string.next);
        }
    }

    // Helper to show red border and error text
    private void showError(EditText field, TextView errorText, String message) {
        field.setBackgroundResource(R.drawable.bg_input_error); // Red Border
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    // Reset errors for a specific step
    private void resetErrors(int step) {
        switch (step) {
            case 0:
                etName.setBackgroundResource(R.drawable.bg_input_outline);
                tvNameError.setVisibility(View.GONE);
                etSurname.setBackgroundResource(R.drawable.bg_input_outline);
                tvSurnameError.setVisibility(View.GONE);
                break;
            case 1:
                tvGenderError.setVisibility(View.GONE);
                break;
            case 2:
                etAge.setBackgroundResource(R.drawable.bg_input_outline);
                tvAgeError.setVisibility(View.GONE);
                break;
            case 3:
                etWeight.setBackgroundResource(R.drawable.bg_input_outline);
                tvWeightError.setVisibility(View.GONE);
                etHeight.setBackgroundResource(R.drawable.bg_input_outline);
                tvHeightError.setVisibility(View.GONE);
                break;
            case 4:
                etGoalWeight.setBackgroundResource(R.drawable.bg_input_outline);
                tvGoalError.setVisibility(View.GONE);
                break;
        }
    }

    // Validate Input Fields
    private boolean validateStep(int step) {
        boolean isValid = true;

        switch (step) {
            case 0: // Name & Surname
                if (etName.getText().toString().isEmpty()) {
                    showError(etName, tvNameError, getString(R.string.first_name_is_required));
                    isValid = false;
                }
                if (etSurname.getText().toString().isEmpty()) {
                    showError(etSurname, tvSurnameError, getString(R.string.last_name_is_required));
                    isValid = false;
                }
                break;

            case 1: // Gender
                if (radioGroupGender.getCheckedRadioButtonId() == -1) {
                    tvGenderError.setText(R.string.please_select_a_gender);
                    tvGenderError.setVisibility(View.VISIBLE);
                    isValid = false;
                }
                break;

            case 2: // Age
                if (etAge.getText().toString().isEmpty()) {
                    showError(etAge, tvAgeError, getString(R.string.age_is_required));
                    isValid = false;
                }
                break;

            case 3: // Weight & Height
                if (etWeight.getText().toString().isEmpty()) {
                    showError(etWeight, tvWeightError, getString(R.string.weight_is_required));
                    isValid = false;
                }
                if (etHeight.getText().toString().isEmpty()) {
                    showError(etHeight, tvHeightError, getString(R.string.height_is_required));
                    isValid = false;
                }
                break;

            case 4: // Goal
                if (etGoalWeight.getText().toString().isEmpty()) {
                    showError(etGoalWeight, tvGoalError, getString(R.string.goal_weight_is_required));
                    isValid = false;
                }
                break;
        }
        return isValid;
    }

    // Final Step Logic
    private void saveAndClose() {
        showSuccessDialog();
    }

    // Success Dialog
    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        Button btnOkay = dialog.findViewById(R.id.btnOkay);

        tvTitle.setText(R.string.all_set);
        tvMessage.setText(R.string.profile_created_successfully);

        btnOkay.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(QuestionsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}