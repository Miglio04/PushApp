package com.example.pushapp.repositories;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pushapp.api.NinjaApiService;
import com.example.pushapp.models.Exercise;
import com.example.pushapp.models.api.ExerciseApiModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseAPIDataSourceTest {

    @Mock
    private NinjaApiService apiService;

    @Mock
    private ExerciseCallback callback;

    @Mock
    private Call<List<ExerciseApiModel>> mockCall;

    private ExerciseAPIDataSource dataSource;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        dataSource = new ExerciseAPIDataSource(apiService);
        dataSource.setCallback(callback);
    }

    @Test
    public void fetchAllExercises_callsApiForEveryMuscle() {
        // Arrange
        when(apiService.getExercises(anyString(), anyString())).thenReturn(mockCall);

        // Act
        dataSource.fetchAllExercises();

        // Assert
        // There are 16 muscles in the list
        verify(apiService, times(16)).getExercises(anyString(), anyString());
    }

    @Test
    public void fetchAllExercises_onSuccess_notifiesCallback() {
        // Arrange
        // We need to capture the callbacks for all 16 calls and simulate success
        when(apiService.getExercises(anyString(), anyString())).thenReturn(mockCall);

        doAnswer(invocation -> {
            Callback<List<ExerciseApiModel>> cb = invocation.getArgument(0);

            // Create dummy API response
            List<ExerciseApiModel> apiModels = new ArrayList<>();
            ExerciseApiModel model = new ExerciseApiModel();
            model.setName("Push Up");
            model.setMuscle("chest");
            model.setDifficulty("beginner");
            apiModels.add(model);

            cb.onResponse(mockCall, Response.success(apiModels));
            return null;
        }).when(mockCall).enqueue(any());

        // Act
        dataSource.fetchAllExercises();

        // Assert
        // Since we have 16 muscles, and we respond to all of them immediately,
        // the completion check inside DataSource should eventually trigger onSuccessFromRemote

        ArgumentCaptor<ArrayList<Exercise>> captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(callback).onSuccessFromRemote(captor.capture());

        // We mocked 1 response per muscle (16 muscles), so we expect 16 * 1 = 16 exercises
        assertEquals(16, captor.getValue().size());
        assertEquals("Push Up", captor.getValue().get(0).getName());
    }

    @Test
    public void fetchAllExercises_onFailure_notifiesCallbackOfFailure() {
        // Arrange
        when(apiService.getExercises(anyString(), anyString())).thenReturn(mockCall);

        // Simulate FAILURE for all calls
        doAnswer(invocation -> {
            Callback<List<ExerciseApiModel>> cb = invocation.getArgument(0);
            cb.onFailure(mockCall, new Exception("API Error"));
            return null;
        }).when(mockCall).enqueue(any());

        // Act
        dataSource.fetchAllExercises();

        // Assert
        verify(callback).onFailureFromRemote(any(Exception.class));
    }
}

