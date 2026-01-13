package com.example.pushapp.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pushapp.repositories.UserRepository;

public class UserViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository userRepository;

    public UserViewModelFactory( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserViewModel(userRepository);
    }
}