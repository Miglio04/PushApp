package com.example.pushapp.viewModels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.SessionUser;
import com.example.pushapp.repositories.SessionRepository;
import com.example.pushapp.repositories.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link UserViewModel}.
 * Tests authentication flows including email/password and Google Sign-In.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    private MutableLiveData<Result> sessionLiveData;
    private UserViewModel viewModel;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_GOOGLE_TOKEN = "google_id_token_123";

    @Before
    public void setup() {
        sessionLiveData = new MutableLiveData<>();
        MutableLiveData<Result> userLiveData = new MutableLiveData<>();

        when(sessionRepository.getSessionLiveData()).thenReturn(sessionLiveData);
        when(userRepository.getCurrentUser()).thenReturn(userLiveData);

        viewModel = new UserViewModel(userRepository, sessionRepository);
    }

    /**
     * Verifies successful email/password login updates LiveData with SessionSuccess.
     */
    @Test
    public void signInWithEmailAndPassword_success_updatesLiveData() {
        viewModel.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        verify(sessionRepository).signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);

        sessionLiveData.setValue(new Result.SessionSuccess(new SessionUser(TEST_USER_ID, TEST_EMAIL)));

        Result result = viewModel.getSessionLiveData().getValue();
        assertNotNull(result);
        assertTrue(result.isSessionSuccess());
    }

    /**
     * Verifies failed email/password login updates LiveData with LoginError.
     */
    @Test
    public void signInWithEmailAndPassword_failure_updatesLiveData() {
        sessionLiveData.setValue(new Result.Error.LoginError("Invalid credentials"));

        Result result = viewModel.getSessionLiveData().getValue();
        assertNotNull(result);
        assertTrue(result.isLoginError());
    }

    /**
     * Verifies successful email/password registration updates LiveData with SessionSuccess.
     */
    @Test
    public void registerWithEmailAndPassword_success_updatesLiveData() {
        viewModel.registerWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        verify(sessionRepository).registerWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);

        sessionLiveData.setValue(new Result.SessionSuccess(new SessionUser(TEST_USER_ID, TEST_EMAIL)));

        Result result = viewModel.getSessionLiveData().getValue();
        assertNotNull(result);
        assertTrue(result.isSessionSuccess());
    }

    /**
     * Verifies failed registration updates LiveData with RegistrationError.
     */
    @Test
    public void registerWithEmailAndPassword_failure_updatesLiveData() {
        sessionLiveData.setValue(new Result.Error.RegistrationError("Email already in use"));

        Result result = viewModel.getSessionLiveData().getValue();
        assertNotNull(result);
        assertTrue(result.isRegistrationError());
    }

    /**
     * Verifies Google login for existing user updates LiveData with SessionSuccess.
     */
    @Test
    public void loginOnlyWithGoogle_existingUser_success() {
        viewModel.loginOnlyWithGoogle(TEST_GOOGLE_TOKEN);
        verify(sessionRepository).loginOnlyWithGoogle(TEST_GOOGLE_TOKEN);

        sessionLiveData.setValue(new Result.SessionSuccess(new SessionUser(TEST_USER_ID, TEST_EMAIL)));

        Result result = viewModel.getSessionLiveData().getValue();
        assertNotNull(result);
        assertTrue(result.isSessionSuccess());
    }

    /**
     * Verifies Google login for non-registered user returns GoogleUserNotRegistered error.
     */
    @Test
    public void loginOnlyWithGoogle_newUser_returnsNotRegisteredError() {
        SessionUser sessionUser = new SessionUser(TEST_USER_ID, TEST_EMAIL);
        sessionLiveData.setValue(new Result.Error.GoogleUserNotRegistered("User not registered", sessionUser));

        Result result = viewModel.getSessionLiveData().getValue();
        assertNotNull(result);
        assertTrue(result.isGoogleUserNotRegistered());
    }

    /**
     * Verifies Google registration calls repository with ID token.
     */
    @Test
    public void registerWithGoogle_callsRepository() {
        viewModel.registerWithGoogle(TEST_GOOGLE_TOKEN);

        verify(sessionRepository).signInWithGoogle(TEST_GOOGLE_TOKEN);
    }

    /**
     * Verifies logout calls repository method.
     */
    @Test
    public void logout_callsRepository() {
        viewModel.logout();

        verify(sessionRepository).logout();
    }
}

