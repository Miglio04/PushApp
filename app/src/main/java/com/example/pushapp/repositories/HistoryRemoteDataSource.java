package com.example.pushapp.repositories;

import com.example.pushapp.models.history.HistorySerie;
import com.example.pushapp.models.history.HistorySession;
import com.example.pushapp.models.history.HistoryWorkoutExercise;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.models.roomModels.helpers.HistoryWorkoutExerciseWithSeries;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data source for handling history-related operations with the remote Firestore database.
 * Manages uploading, fetching, and deleting workout history sessions in the cloud.
 */
public class HistoryRemoteDataSource {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private HistoryCallback callback;

    public HistoryRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Sets the callback interface for receiving asynchronous operation results.
     *
     * @param callback The callback implementation.
     */
    public void setHistoryCallback(HistoryCallback callback) {
        this.callback = callback;
    }

    /**
     * Uploads a completed workout session to Firestore.
     * Maps the session, exercises, and series objects to a nested Map structure for storage.
     *
     * @param sessionToSave  The session object to upload.
     * @param remoteCallback The callback to notify on failure (success is silent here).
     */
    public void uploadWorkoutSession(HistorySessionWithExercises sessionToSave, HistoryCallback remoteCallback) {
        if (auth.getCurrentUser() == null) {
            if (remoteCallback != null) remoteCallback.onFailureFromRemote(new Exception("User not authenticated"));
            return;
        }
        if (sessionToSave == null || sessionToSave.session == null) {
            if (callback != null) callback.onFailureFromRemote(new Exception("Session to save is null"));
            return;
        }

        Map<String, Object> sessionMap = new HashMap<>();
        HistorySession session = sessionToSave.session;
        sessionMap.put("historySessionId", session.getHistorySessionId());
        sessionMap.put("userId", session.getUserId());
        sessionMap.put("name", session.getName());
        sessionMap.put("startTime", session.getStartTime());
        sessionMap.put("endTime", session.getEndTime());
        sessionMap.put("duration", session.getDuration());

        List<Map<String, Object>> exercisesListMap = new ArrayList<>();
        if (sessionToSave.exercises != null) {
            for (HistoryWorkoutExerciseWithSeries exWithSeries : sessionToSave.exercises) {
                Map<String, Object> exMap = new HashMap<>();
                HistoryWorkoutExercise exercise = exWithSeries.historyWorkoutExercise;
                exMap.put("historyExerciseId", exercise.getHistoryExerciseId());
                exMap.put("userId", exercise.getUserId());
                exMap.put("historySessionId", exercise.getHistorySessionId());
                exMap.put("exerciseName", exercise.getExerciseName());
                exMap.put("orderIndex", exercise.getOrderIndex());
                List<Map<String, Object>> seriesListMap = new ArrayList<>();
                if (exWithSeries.historySeries != null) {
                    for (HistorySerie serie : exWithSeries.historySeries) {
                        Map<String, Object> serieMap = new HashMap<>();
                        serieMap.put("historySerieId", serie.getHistorySerieId());
                        serieMap.put("userId", serie.getUserId());
                        serieMap.put("historyExerciseId", serie.getHistoryExerciseId());
                        serieMap.put("setNumber", serie.getSetNumber());
                        serieMap.put("weight", serie.getWeight());
                        serieMap.put("reps", serie.getReps());
                        seriesListMap.add(serieMap);
                    }
                }
                exMap.put("series", seriesListMap);
                exercisesListMap.add(exMap);
            }
        }

        sessionMap.put("exercises", exercisesListMap);

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("historySessions")
                .document(session.getHistorySessionId())
                .set(sessionMap)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    if (remoteCallback != null) remoteCallback.onFailureFromRemote(e);
                });
    }

    /**
     * Fetches the user's workout history from Firestore.
     * Retrieving sessions ordered by start time descending.
     * Parses the documents into local history models and notifies the callback.
     */
    public void fetchHistoryFromRemote() {
        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("historySessions")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<HistorySessionWithExercises> resultList = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            HistorySessionWithExercises item = parseDocumentToHistoryObject(doc);
                            resultList.add(item);
                        } catch (Exception e) {
                            resultList.add(null);
                        }
                    }

                    if (callback != null) {
                        callback.onSuccessHistoryFromRemote(resultList);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailureFromRemote(e);
                });
    }

    /**
     * Deletes a specific history session from Firestore.
     *
     * @param sessionId The ID of the session to delete.
     * @param listener  The listener to notify upon failure.
     */
    public void deleteSession(String sessionId, OnFailureListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) listener.onFailure(new Exception("User not authenticated"));
            return;
        }

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("historySessions")
                .document(sessionId)
                .delete()
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Parses a Firestore document snapshot into a HistorySessionWithExercises object.
     * Reconstructs the session, exercises, and series hierarchy from the document data.
     *
     * @param doc The DocumentSnapshot to parse.
     * @return The populated HistorySessionWithExercises object.
     */
    private HistorySessionWithExercises parseDocumentToHistoryObject(DocumentSnapshot doc) {
        HistorySession session = new HistorySession();
        session.setHistorySessionId(Objects.requireNonNull(doc.getString("historySessionId")));
        session.setUserId(Objects.requireNonNull(doc.getString("userId")));
        session.setName(doc.getString("name"));
        Long start = doc.getLong("startTime");
        session.setStartTime((start != null) ? start : 0);
        Long end = doc.getLong("endTime");
        session.setEndTime((end != null) ? end : 0);
        Long dur = doc.getLong("duration");
        session.setDuration((dur != null) ? dur : 0);

        List<HistoryWorkoutExerciseWithSeries> exercisesWithSeries = new ArrayList<>();

        List<Map<String, Object>> exMaps = (List<Map<String, Object>>) doc.get("exercises");
        if (exMaps != null) {
            for (Map<String, Object> exMap : exMaps) {
                HistoryWorkoutExercise ex = new HistoryWorkoutExercise();
                ex.setHistoryExerciseId((String) Objects.requireNonNull(exMap.get("historyExerciseId")));
                ex.setUserId((String) Objects.requireNonNull(exMap.get("userId")));
                ex.setHistorySessionId ((String) Objects.requireNonNull(exMap.get("historySessionId")));
                ex.setExerciseName((String) exMap.get("exerciseName"));
                Long order = (Long) exMap.get("orderIndex");
                ex.setOrderIndex((order != null) ? order.intValue() : 0);

                List<HistorySerie> seriesList = new ArrayList<>();
                List<Map<String, Object>> sMaps = (List<Map<String, Object>>) exMap.get("series");
                if (sMaps != null) {
                    for (Map<String, Object> sMap : sMaps) {
                        HistorySerie s = new HistorySerie();
                        s.setHistorySerieId((String) Objects.requireNonNull(sMap.get("historySerieId")));
                        s.setUserId((String) Objects.requireNonNull(sMap.get("userId")));
                        s.setHistoryExerciseId((String) Objects.requireNonNull(sMap.get("historyExerciseId")));
                        Long sNum = (Long) sMap.get("setNumber");
                        s.setSetNumber((sNum != null) ? sNum.intValue() : 0);
                        Double w = (Double) sMap.get("weight");
                        s.setWeight((w != null) ? w : 0.0);
                        Long r = (Long) sMap.get("reps");
                        s.setReps((r != null) ? r.intValue() : 0);
                        seriesList.add(s);
                    }
                }

                HistoryWorkoutExerciseWithSeries exWrapper = new HistoryWorkoutExerciseWithSeries();
                exWrapper.historyWorkoutExercise = ex;
                exWrapper.historySeries = seriesList;

                exercisesWithSeries.add(exWrapper);
            }
        }

        HistorySessionWithExercises result = new HistorySessionWithExercises();
        result.session = session;
        result.exercises = exercisesWithSeries;

        return result;
    }

    /**
     * Interface definition for a callback to be invoked when a remote operation fails.
     */
    public interface OnFailureListener {
        void onFailure(Exception e);
    }

}