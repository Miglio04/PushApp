package com.example.pushapp.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.SessionRepository;
import com.example.pushapp.repositories.UserRepository;

public class UserViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public UserViewModelFactory( UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserViewModel(userRepository, sessionRepository);
    }
}