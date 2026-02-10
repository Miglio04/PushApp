package com.example.pushapp.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pushapp.R;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.User;
import com.example.pushapp.ui.login.AuthActivity;
import com.example.pushapp.viewModels.TrainingViewModel;
import com.example.pushapp.viewModels.UserViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private UserViewModel userViewModel;
    private TrainingViewModel trainingViewModel;

    // UI Profile Header
    private TextView profileInitial, profileFullName, profileEmailTop;
    private TextView tvDetailEmail, tvDetailGender, tvDetailAge, tvDetailHeight, tvDetailWeight;
    private MaterialButton btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupViewModel();

        if(btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked");
                userViewModel.clearLiveData();
                userViewModel.logout();
                trainingViewModel.resetLocalDatabase();
                userViewModel.resetLocalDatabase();

                Intent intent = new Intent(getActivity(), AuthActivity.class);
                startActivity(intent);
                getActivity().finishAffinity();
            });
        }
    }

    private void initializeViews(View view) {
        profileInitial = view.findViewById(R.id.profileInitial);
        profileFullName = view.findViewById(R.id.profileFullName);
        profileEmailTop = view.findViewById(R.id.profileEmailTop);

        tvDetailEmail = view.findViewById(R.id.tvDetailEmail);
        tvDetailGender = view.findViewById(R.id.tvDetailGender);
        tvDetailAge = view.findViewById(R.id.tvDetailAge);
        tvDetailHeight = view.findViewById(R.id.tvDetailHeight);
        tvDetailWeight = view.findViewById(R.id.tvDetailWeight);
        btnLogout = view.findViewById(R.id.btnLogout);
    }
    private void setupViewModel() {
        userViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(UserViewModel.class);

        trainingViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(requireContext())).get(TrainingViewModel.class);

        Result result = userViewModel.getUserLiveData().getValue();

        if(result != null && result.isUserSuccess()){
            User user = ((Result.UserSuccess) result).getData();
            if (user != null) {
                String name = user.getName() != null ? user.getName() : "";
                String surname = user.getSurname() != null ? user.getSurname() : "";

                if (profileFullName != null) profileFullName.setText(name + " " + surname);
                if (profileEmailTop != null) profileEmailTop.setText(user.getEmail());

                if (!name.isEmpty() && profileInitial != null) {
                    profileInitial.setText(name.substring(0, 1).toUpperCase());
                }

                // Dettagli nel dropdown utilizzando le stringhe localizzate
                if (tvDetailEmail != null) {
                    tvDetailEmail.setText(getString(R.string.detail_email, user.getEmail()));
                }
                if (tvDetailGender != null) {
                    String gender = (user.getGender() != null && !user.getGender().isEmpty()) ? user.getGender() : "-";
                    tvDetailGender.setText(getString(R.string.detail_gender, gender));
                }
                if (tvDetailAge != null) {
                    tvDetailAge.setText(getString(R.string.detail_age, user.getAge()));
                }
                if (tvDetailHeight != null) {
                    tvDetailHeight.setText(getString(R.string.detail_height, user.getHeight()));
                }
                if (tvDetailWeight != null) {
                    tvDetailWeight.setText(getString(R.string.detail_weight, user.getWeight()));
                }
            }
        }
    }
}