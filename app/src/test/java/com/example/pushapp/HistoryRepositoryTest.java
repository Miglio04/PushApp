package com.example.pushapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.pushapp.models.Result;
import com.example.pushapp.models.Routine;
import com.example.pushapp.models.Serie;
import com.example.pushapp.models.WorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.repositories.HistoryCallback;
import com.example.pushapp.repositories.HistoryLocalDataSource;
import com.example.pushapp.repositories.HistoryRemoteDataSource;
import com.example.pushapp.repositories.HistoryRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe di test per il repository della cronologia allenamenti.
 * Verifica la logica di sincronizzazione tra database locale (Room) e remoto (Firestore).
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryRepositoryTest {

    /**
     * Regola per eseguire i task di Architecture Components in modo sincrono durante i test.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private HistoryLocalDataSource localDataSource;

    @Mock
    private HistoryRemoteDataSource remoteDataSource;

    @Mock
    private Observer<Result> resultObserver;

    private HistoryRepository repository;

    /**
     * Configura l'ambiente di test prima di ogni esecuzione.
     */
    @Before
    public void setUp() {
        repository = new HistoryRepository(localDataSource, remoteDataSource);
    }

    /**
     * Pulisce le risorse e rimuove gli observer dopo ogni test.
     */
    @After
    public void tearDown() {
        if (repository.getHistoryList().hasObservers()) {
            repository.getHistoryList().removeObserver(resultObserver);
        }
    }

    /**
     * Verifica che il fallimento del salvataggio locale impedisca l'upload remoto.
     */
    @Test
    public void saveWorkoutSession_WhenLocalFails_DoesNotUploadToRemote() {
        HistorySessionWithExercises mockSession = new HistorySessionWithExercises();
        mockSession.exercises = new ArrayList<>();
        Runnable mockCallback = mock(Runnable.class);

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onFailureFromLocal(new Exception("Database error"));
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        repository.saveWorkoutSession(mockSession, mockCallback);

        verify(remoteDataSource, never()).uploadWorkoutSession(any(), any());
        verify(mockCallback, never()).run();

        Result result = repository.getHistoryList().getValue();
        assertThat(result, instanceOf(Result.Error.class));
    }

    /**
     * Verifica che il sistema completi il salvataggio locale anche se l'upload remoto fallisce.
     */
    @Test
    public void saveWorkoutSession_WhenRemoteFails_StillSavesLocally() {
        HistorySessionWithExercises mockSession = new HistorySessionWithExercises();
        mockSession.exercises = new ArrayList<>();
        Runnable mockCallback = mock(Runnable.class);

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onSuccessSaveLocal();
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(1);
            callback.onFailureFromRemote(new Exception("Network error"));
            return null;
        }).when(remoteDataSource).uploadWorkoutSession(any(), any());

        repository.saveWorkoutSession(mockSession, mockCallback);

        verify(localDataSource).saveSession(any(), any(), any(), any());
        verify(remoteDataSource).uploadWorkoutSession(any(), any());
        verify(mockCallback).run();
    }

    /**
     * Verifica che l'eliminazione venga richiesta correttamente sia al DB locale che remoto.
     */
    @Test
    public void deleteSession_WhenLocalFails_StillTriesRemote() {
        String sessionId = "session_123";
        repository.deleteSession(sessionId);

        verify(localDataSource).deleteSession(eq(sessionId), any());
        verify(remoteDataSource).deleteSession(eq(sessionId), any());
    }

    /**
     * Verifica che un errore nel caricamento locale scateni il corretto stato di errore nel LiveData.
     */
    @Test
    public void fetchHistoryData_WhenLocalFails_PostsError() {
        Exception localError = new Exception("Local fetch failed");

        doAnswer(invocation -> {
            repository.onFailureFromLocal(localError);
            return null;
        }).when(localDataSource).getAllHistory();

        repository.fetchHistoryData();

        Result result = repository.getHistoryList().getValue();
        assertThat(result, instanceOf(Result.Error.class));
        assertThat(((Result.Error) result).getMessage(), equalTo("Local fetch failed"));
    }

    /**
     * Verifica che i dati locali rimangano disponibili anche in caso di timeout della rete.
     */
    @Test
    public void fetchHistoryData_WhenRemoteFails_LocalDataStillAvailable() {
        List<HistorySessionWithExercises> localData = createMockHistoryList(2);

        doAnswer(invocation -> {
            repository.onSuccessHistoryListFromLocal(localData);
            return null;
        }).when(localDataSource).getAllHistory();

        doAnswer(invocation -> {
            repository.onFailureFromRemote(new Exception("Network timeout"));
            return null;
        }).when(remoteDataSource).fetchHistoryFromRemote();

        repository.getHistoryList().observeForever(resultObserver);
        repository.fetchHistoryData();

        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(resultObserver, atLeastOnce()).onChanged(captor.capture());

        Result successResult = captor.getAllValues().stream()
                .filter(r -> r instanceof Result.HistorySuccess)
                .findFirst()
                .orElse(null);

        assertNotNull(successResult);
        assertThat(((Result.HistorySuccess) successResult).getData(), hasSize(2));
    }

    /**
     * Verifica il corretto mapping dei campi da una Routine a una nuova Sessione storica.
     */
    @Test
    public void createNewWorkoutSession_WithValidRoutine_MapsAllFieldsCorrectly() {
        Routine routine = new Routine();
        routine.setName("Push Day");
        routine.setUserId("user_123");
        routine.setRoutineId("routine_abc");

        List<WorkoutExercise> exercises = new ArrayList<>();
        WorkoutExercise ex1 = new WorkoutExercise("ex_01", 0);
        ex1.setWorkoutExerciseId("work_ex_01");

        List<Serie> series1 = new ArrayList<>();
        series1.add(new Serie(1, 10, 50.0));
        series1.add(new Serie(2, 8, 55.0));
        series1.add(new Serie(3, 6, 60.0));
        ex1.setSeries(series1);
        exercises.add(ex1);

        routine.setWorkoutExercises(exercises);

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull("Il risultato non deve essere null", result);
        assertThat(result.session.getName(), equalTo("Push Day"));
        assertThat(result.session.getUserId(), equalTo("user_123"));
        assertThat(result.exercises, hasSize(1));

        String historyId = result.exercises.get(0).historyWorkoutExercise.getHistoryExerciseId();
        assertNotNull(historyId);
        assertThat(historyId, not(isEmptyString()));

        List<com.example.pushapp.models.history.HistorySerie> historySeries = result.exercises.get(0).historySeries;
        assertThat(historySeries, hasSize(3));
        assertThat("Le reps devono essere 0", historySeries.get(0).getReps(), equalTo(0));
        assertThat("Il peso deve essere 0.0", historySeries.get(0).getWeight(), equalTo(0.0));
    }

    /**
     * Verifica che il recupero dei dati locali aggiorni correttamente il LiveData.
     */
    @Test
    public void onSuccessHistoryListFromLocal_UpdatesLiveDataWithCorrectContent() {
        List<HistorySessionWithExercises> mockList = createMockHistoryList(3);
        repository.getHistoryList().observeForever(resultObserver);
        repository.onSuccessHistoryListFromLocal(mockList);

        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(resultObserver).onChanged(captor.capture());

        Result result = captor.getValue();
        assertThat(result, instanceOf(Result.HistorySuccess.class));

        List<HistorySessionWithExercises> resultData = ((Result.HistorySuccess) result).getData();
        assertThat("Should have 3 sessions", resultData, hasSize(3));
        assertThat("Should be the same list", resultData, equalTo(mockList));
    }

    /**
     * Verifica che il messaggio di errore locale venga postato correttamente nel LiveData.
     */
    @Test
    public void onFailureFromLocal_PostsErrorWithCorrectMessage() {
        String expectedMessage = "Database connection failed";
        Exception testException = new Exception(expectedMessage);

        repository.onFailureFromLocal(testException);

        Result result = repository.getHistoryList().getValue();
        assertThat(result, instanceOf(Result.Error.class));
        assertThat(((Result.Error) result).getMessage(), equalTo(expectedMessage));
    }

    /**
     * Verifica che l'oggetto sessione inviato al server sia identico a quello fornito.
     */
    @Test
    public void saveWorkoutSession_PassesExactSessionToRemote() {
        HistorySessionWithExercises originalSession = createMockSession("Test Session");
        Runnable mockCallback = mock(Runnable.class);

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onSuccessSaveLocal();
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        repository.saveWorkoutSession(originalSession, mockCallback);

        ArgumentCaptor<HistorySessionWithExercises> captor = ArgumentCaptor.forClass(HistorySessionWithExercises.class);
        verify(remoteDataSource).uploadWorkoutSession(captor.capture(), any());

        assertThat("Should pass the same session object", captor.getValue(), equalTo(originalSession));
    }

    /**
     * Verifica che sia possibile creare una sessione valida anche senza esercizi.
     */
    @Test
    public void createNewWorkoutSession_WithEmptyExerciseList_ReturnsValidSession() {
        Routine routine = new Routine();
        routine.setName("Rest Day");
        routine.setUserId("user_123");
        routine.setWorkoutExercises(new ArrayList<>());

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull("Should create session even with no exercises", result);
        assertThat("Session name should match", result.session.getName(), equalTo("Rest Day"));
        assertThat("Exercises list should be empty", result.exercises, hasSize(0));
    }

    /**
     * Verifica la gestione robusta di esercizi privi di serie.
     */
    @Test
    public void createNewWorkoutSession_WithExerciseWithoutSeries_HandlesGracefully() {
        Routine routine = new Routine();
        routine.setName("Cardio Day");
        routine.setUserId("user_123");

        List<WorkoutExercise> exercises = new ArrayList<>();
        WorkoutExercise ex = new WorkoutExercise();
        ex.setApiExerciseId("ex_cardio");
        ex.setWorkoutExerciseId("work_ex_cardio");
        ex.setSeries(new ArrayList<>());
        exercises.add(ex);

        routine.setWorkoutExercises(exercises);

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull("Should handle exercise without series", result);
        assertThat("Should have 1 exercise", result.exercises, hasSize(1));
        assertThat("Exercise should have no series", result.exercises.get(0).historySeries, hasSize(0));
    }

    /**
     * Verifica la gestione di liste di esercizi nulle trattandole come vuote.
     */
    @Test
    public void createNewWorkoutSession_WithNullExerciseList_ReturnsValidSession() {
        Routine routine = new Routine();
        routine.setName("Unnamed Workout");
        routine.setUserId("user_123");
        routine.setWorkoutExercises(null);

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull("Should handle null exercise list", result);
        assertThat("Exercises should be empty", result.exercises, hasSize(0));
    }

    /**
     * Verifica che il repository possa gestire stringhe di nomi molto lunghe.
     */
    @Test
    public void createNewWorkoutSession_WithVeryLongRoutineName_TruncatesOrHandles() {
        String veryLongName = "A".repeat(500);
        Routine routine = new Routine();
        routine.setName(veryLongName);
        routine.setUserId("user_123");
        routine.setWorkoutExercises(new ArrayList<>());

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull("Should handle very long names", result);
    }

    /**
     * Verifica che i caratteri speciali nel nome della routine vengano preservati.
     */
    @Test
    public void createNewWorkoutSession_WithSpecialCharactersInName_HandlesCorrectly() {
        String nameWithSpecialChars = "Push/Pull 💪 Day #1 <test>";
        Routine routine = new Routine();
        routine.setName(nameWithSpecialChars);
        routine.setUserId("user_123");
        routine.setWorkoutExercises(new ArrayList<>());

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertNotNull("Should handle special characters", result);
        assertThat("Name should be set", result.session.getName(), not(isEmptyString()));
    }

    /**
     * Verifica che una risposta remota vuota non alteri il database locale.
     */
    @Test
    public void onSuccessHistoryFromRemote_WithEmptyList_DoesNotUpdateLocal() {
        List<HistorySessionWithExercises> emptyList = new ArrayList<>();
        repository.onSuccessHistoryFromRemote(emptyList);
        verify(localDataSource, never()).updateHistoryFromRemote(any());
    }

    /**
     * Verifica la resistenza a crash in caso di risposta remota nulla.
     */
    @Test
    public void onSuccessHistoryFromRemote_WithNullList_DoesNotCrash() {
        repository.onSuccessHistoryFromRemote(null);
        verify(localDataSource, never()).updateHistoryFromRemote(any());
    }

    /**
     * Verifica che l'eliminazione gestisca correttamente identificativi nulli.
     */
    @Test
    public void deleteSession_WithNullSessionId_HandlesGracefully() {
        repository.deleteSession(null);
        verify(localDataSource).deleteSession(isNull(), any());
        verify(remoteDataSource).deleteSession(isNull(), any());
    }

    /**
     * Verifica che l'eliminazione gestisca correttamente identificativi vuoti.
     */
    @Test
    public void deleteSession_WithEmptySessionId_HandlesGracefully() {
        repository.deleteSession("");
        verify(localDataSource).deleteSession(eq(""), any());
        verify(remoteDataSource).deleteSession(eq(""), any());
    }

    /**
     * Verifica che la richiesta di dati per i grafici venga inoltrata al data source locale.
     */
    @Test
    public void fetchGraphData_WithNullExerciseName_StillCallsDataSource() {
        repository.fetchGraphData(null, HistoryRepository.StatMetric.MAX_WEIGHT, null);
        verify(localDataSource).getGraphData(isNull(), any(), any());
    }

    /**
     * Verifica che il callback di successo venga eseguito solo dopo la conferma locale e remota.
     */
    @Test
    public void saveWorkoutSession_CallbackExecutedOnSuccess() {
        HistorySessionWithExercises mockSession = createMockSession("Workout");
        Runnable mockCallback = mock(Runnable.class);

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onSuccessSaveLocal();
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(1);
            callback.onSuccessSaveLocal();
            return null;
        }).when(remoteDataSource).uploadWorkoutSession(any(), any());

        repository.saveWorkoutSession(mockSession, mockCallback);

        verify(mockCallback, times(1)).run();
    }

    /**
     * Verifica che molteplici observer ricevano correttamente le notifiche dal repository.
     */
    @Test
    public void multipleObservers_AllReceiveUpdates() {
        Observer<Result> observer1 = mock(Observer.class);
        Observer<Result> observer2 = mock(Observer.class);

        repository.getHistoryList().observeForever(observer1);
        repository.getHistoryList().observeForever(observer2);

        List<HistorySessionWithExercises> mockList = createMockHistoryList(1);
        repository.onSuccessHistoryListFromLocal(mockList);

        verify(observer1).onChanged(any(Result.class));
        verify(observer2).onChanged(any(Result.class));

        repository.getHistoryList().removeObserver(observer1);
        repository.getHistoryList().removeObserver(observer2);
    }

    /**
     * Verifica che ogni nuova sessione generata abbia un ID univoco globale.
     */
    @Test
    public void createNewWorkoutSession_GeneratesUniqueSessionIds() {
        Routine routine = new Routine();
        routine.setName("Workout");
        routine.setUserId("user_123");
        routine.setWorkoutExercises(new ArrayList<>());

        HistorySessionWithExercises session1 = repository.createNewWorkoutSessionWithoutTemplate(routine);
        HistorySessionWithExercises session2 = repository.createNewWorkoutSessionWithoutTemplate(routine);
        HistorySessionWithExercises session3 = repository.createNewWorkoutSessionWithoutTemplate(routine);

        assertThat("Session IDs should be unique",
                session1.session.getHistorySessionId(), not(equalTo(session2.session.getHistorySessionId())));
        assertThat("Session IDs should be unique",
                session2.session.getHistorySessionId(), not(equalTo(session3.session.getHistorySessionId())));
    }

    /**
     * Verifica che il caricamento dati attivi entrambi i data source.
     */
    @Test
    public void fetchHistoryData_CallsBothSources_Verification() {
        repository.fetchHistoryData();
        verify(localDataSource).getAllHistory();
        verify(remoteDataSource).fetchHistoryFromRemote();
    }

    /**
     * Verifica che una routine nulla non permetta la creazione di una sessione.
     */
    @Test
    public void createNewWorkoutSession_ReturnsNull_WhenRoutineIsNull() {
        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(null);
        assertNull("Should return null for null routine", result);
    }

    /**
     * Verifica che la mancanza di userId impedisca la creazione della sessione.
     */
    @Test
    public void createNewWorkoutSession_ReturnsNull_WhenUserIdIsNull() {
        Routine routine = new Routine();
        routine.setUserId(null);
        routine.setName("Test");

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);
        assertNull("Should return null for null userId", result);
    }

    /**
     * Verifica che uno userId vuoto impedisca la creazione della sessione.
     */
    @Test
    public void createNewWorkoutSession_ReturnsNull_WhenUserIdIsEmpty() {
        Routine routine = new Routine();
        routine.setUserId("");
        routine.setName("Test");

        HistorySessionWithExercises result = repository.createNewWorkoutSessionWithoutTemplate(routine);
        assertNull("Should return null for empty userId", result);
    }

    /**
     * Verifica che il successo locale attivi immediatamente la sincronizzazione remota.
     */
    @Test
    public void saveWorkoutSession_WhenLocalSuccess_ThenUploadsToRemote() {
        HistorySessionWithExercises mockSession = createMockSession("Test");

        doAnswer(invocation -> {
            HistoryCallback callback = invocation.getArgument(3);
            callback.onSuccessSaveLocal();
            return null;
        }).when(localDataSource).saveSession(any(), any(), any(), any());

        repository.saveWorkoutSession(mockSession, () -> {});

        verify(remoteDataSource).uploadWorkoutSession(eq(mockSession), any());
        verify(localDataSource, atLeastOnce()).getAllHistory();
    }

    /**
     * Verifica che l'eliminazione chiami entrambi i data source.
     */
    @Test
    public void deleteSession_CallsBothSources_Verification() {
        String sessionId = "id_da_eliminare";
        repository.deleteSession(sessionId);

        verify(localDataSource).deleteSession(eq(sessionId), any());
        verify(remoteDataSource).deleteSession(eq(sessionId), any());
    }

    /**
     * Verifica che i dati remoti aggiornino il database locale.
     */
    @Test
    public void onSuccessHistoryFromRemote_WithData_UpdatesLocalDatabase() {
        List<HistorySessionWithExercises> remoteData = createMockHistoryList(3);
        repository.onSuccessHistoryFromRemote(remoteData);
        verify(localDataSource).updateHistoryFromRemote(remoteData);
    }

    /**
     * Crea una lista mockata di sessioni per i test.
     */
    private List<HistorySessionWithExercises> createMockHistoryList(int count) {
        List<HistorySessionWithExercises> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createMockSession("Session " + i));
        }
        return list;
    }

    /**
     * Crea un oggetto sessione mockato per i test.
     */
    private HistorySessionWithExercises createMockSession(String name) {
        HistorySessionWithExercises session = new HistorySessionWithExercises();
        session.exercises = new ArrayList<>();
        return session;
    }
}