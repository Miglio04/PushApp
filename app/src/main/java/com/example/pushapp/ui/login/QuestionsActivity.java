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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import android.widget.ViewFlipper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;
import com.example.pushapp.ui.main.MainActivity;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;

/**
 * Activity for collecting initial user profile information via a multi-step form.
 * Handles input validation, progress tracking, and saving user details (name, age, measurements, goals).
 */
public class QuestionsActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private ProgressBar progressBar;
    private TextView tvStepCounter, tvProgressPercentage;
    private Button btnBack, btnNext;

    private EditText etName, etSurname, etAge, etWeight, etHeight;
    private TextView tvNameError, tvSurnameError, tvAgeError, tvWeightError, tvHeightError;
    private RadioGroup radioGroupGender;
    private TextView tvGenderError;

    private int currentStep = 0;
    private final int TOTAL_STEPS = 4;

    private UserViewModel userViewModel;

    /**
     * Initializes the activity, ViewModels, and UI components.
     * Starts the data collection process by clearing previous user data and fetching the current user context.
     *
     * @param savedInstanceState Saved state from a previous instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplicationContext())).get(UserViewModel.class);
        userViewModel.clearUserLiveData();
        userViewModel.fetchUser();

        initializeViews();
        updateProgress();

        btnBack.setOnClickListener(v -> navigateBack());
        btnNext.setOnClickListener(v -> navigateNext());
    }

    /**
     * Finds and initializes all UI views from the layout resource.
     */
    private void initializeViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        progressBar = findViewById(R.id.progressBar);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        tvNameError = findViewById(R.id.tvNameError);
        tvSurnameError = findViewById(R.id.tvSurnameError);

        radioGroupGender = findViewById(R.id.radioGroupGender);
        tvGenderError = findViewById(R.id.tvGenderError);

        etAge = findViewById(R.id.etAge);
        tvAgeError = findViewById(R.id.tvAgeError);

        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        tvWeightError = findViewById(R.id.tvWeightError);
        tvHeightError = findViewById(R.id.tvHeightError);
    }

    /**
     * Validates the current step's input and advances to the next step.
     * If on the final step, initiates the save process.
     */
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

    /**
     * Navigates back to the previous step in the form, if possible.
     */
    private void navigateBack() {
        if (currentStep > 0) {
            currentStep--;
            viewFlipper.showPrevious();
            updateProgress();
        }
    }

    /**
     * Updates the progress bar and step counter text based on the current step.
     * Adjusts button visibility (Back button) and text (Next vs Finish).
     */
    private void updateProgress() {
        int progress = (currentStep + 1) * 100 / TOTAL_STEPS;
        progressBar.setProgress(progress);
        tvStepCounter.setText(getString(R.string.step_of, currentStep + 1, TOTAL_STEPS));

        tvProgressPercentage.setText(progress + "%");

        if (currentStep == 0) {
            btnBack.setVisibility(View.GONE);
        } else {
            btnBack.setVisibility(View.VISIBLE);
        }

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText(getString(R.string.finish));
        } else {
            btnNext.setText(getString(R.string.next));
        }
    }

    /**
     * Validates input fields for the currently active step.
     *
     * @return true if all inputs for the current step are valid, false otherwise.
     */
    private boolean validateCurrentStep() {
        resetErrors();
        switch (currentStep) {
            case 0: return validateStep1_Name();
            case 1: return validateStep2_Gender();
            case 2: return validateStep3_Age();
            case 3: return validateStep4_Measurements();
            default: return true;
        }
    }

    /**
     * Validates Step 1: Name and Surname inputs.
     * @return true if valid.
     */
    private boolean validateStep1_Name() {
        boolean isValid = true;
        if (TextUtils.isEmpty(etName.getText())) {
            showError(etName, tvNameError, getString(R.string.first_name_is_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(etSurname.getText())) {
            showError(etSurname, tvSurnameError, getString(R.string.last_name_is_required));
            isValid = false;
        }
        return isValid;
    }

    /**
     * Validates Step 2: Gender selection.
     * @return true if valid.
     */
    private boolean validateStep2_Gender() {
        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            showError(null, tvGenderError, getString(R.string.please_select_a_gender));
            return false;
        }
        return true;
    }

    /**
     * Validates Step 3: Age input. Checks for reasonable age range (16-99).
     * @return true if valid.
     */
    private boolean validateStep3_Age() {
        String ageStr = etAge.getText().toString();
        if (TextUtils.isEmpty(ageStr) || Integer.parseInt(ageStr) < 16 || Integer.parseInt(ageStr) > 99) {
            showError(etAge, tvAgeError, getString(R.string.please_valide_age));
            return false;
        }
        return true;
    }

    /**
     * Validates Step 4: Weight and Height inputs.
     * @return true if valid.
     */
    private boolean validateStep4_Measurements() {
        boolean isValid = true;
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();

        if (TextUtils.isEmpty(weightStr) || Double.parseDouble(weightStr) < 20) {
            showError(etWeight, tvWeightError, getString(R.string.check_weight));
            isValid = false;
        }
        if (TextUtils.isEmpty(heightStr) || Integer.parseInt(heightStr) < 100) {
            showError(etHeight, tvHeightError, getString(R.string.check_height));
            isValid = false;
        }
        return isValid;
    }


    /**
     * Displays an error message for a specific input field and highlights it.
     *
     * @param field     The EditText field to highlight (optional).
     * @param errorText The TextView to display the error message.
     * @param message   The error message string.
     */
    private void showError(EditText field, TextView errorText, String message) {
        if (field != null) field.setBackgroundResource(R.drawable.bg_input_error);
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Resets visual error indicators for all input fields.
     */
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
    }

    /**
     * Collects all validated data, updates the User object via ViewModel, and shows success dialog.
     */
    private void saveDataAndShowPopup() {

        btnNext.setEnabled(false);

        com.google.firebase.auth.FirebaseUser firebaseUser =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            btnNext.setEnabled(true);
            return;
        }

        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        int age = Integer.parseInt(etAge.getText().toString().trim());
        double weight = Double.parseDouble(etWeight.getText().toString().trim());
        int height = Integer.parseInt(etHeight.getText().toString().trim());

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton rbSelected = findViewById(selectedGenderId);
        String gender = rbSelected != null ? rbSelected.getText().toString() : "";

        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail());
        user.setName(name);
        user.setSurname(surname);
        user.setAge(age);
        user.setGender(gender);
        user.setWeight(weight);
        user.setHeight(height);

        userViewModel.insertUser(user);

        showProfileCompletedDialog();
    }

    /**
     * Shows a confirmation dialog upon successful profile completion.
     * Redirects the user to the main activity when dismissed.
     */
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

        if (tvTitle != null) tvTitle.setText(getString(R.string.profile_completed));
        if (tvMessage != null) tvMessage.setText(getString(R.string.data_saved));
        if (btnAction != null) {
            btnAction.setText(getString(R.string.go_home));
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