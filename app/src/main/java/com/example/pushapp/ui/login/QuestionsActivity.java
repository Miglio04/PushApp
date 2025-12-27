package com.example.pushapp.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ViewFlipper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pushapp.R;
import com.example.pushapp.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionsActivity extends AppCompatActivity {

    private static final String TAG = "QuestionsActivity";

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

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false); // Disabilita per evitare click multipli

        String uid = user.getUid();
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        int age = Integer.parseInt(etAge.getText().toString().trim());
        double weight = Double.parseDouble(etWeight.getText().toString().trim());
        int height = Integer.parseInt(etHeight.getText().toString().trim());
        double goalWeight = Double.parseDouble(etGoalWeight.getText().toString().trim());

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton rbSelected = findViewById(selectedGenderId);
        String gender = rbSelected != null ? rbSelected.getText().toString() : "";

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("surname", surname);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("weight", weight);
        userData.put("height", height);
        userData.put("goalWeight", goalWeight);
        userData.put("weightProgress", FieldValue.arrayUnion(weight));

        db.collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data updated successfully");
                    showProfileCompletedDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user data", e);
                    btnNext.setEnabled(true);
                    Toast.makeText(QuestionsActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showProfileCompletedDialog() {
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

        if (tvTitle != null) tvTitle.setText("Profile Completed! ");
        if (tvMessage != null) tvMessage.setText("Your data has been saved.\nYou are ready to start training.");
        if (btnAction != null) {
            btnAction.setText("GO TO HOME");
            btnAction.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(QuestionsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        dialog.show();
    }
}