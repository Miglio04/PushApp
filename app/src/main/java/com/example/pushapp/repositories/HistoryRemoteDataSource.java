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

public class HistoryRemoteDataSource {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private HistoryCallback callback;

    public HistoryRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void setHistoryCallback(HistoryCallback callback) {
        this.callback = callback;
    }

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
                            e.printStackTrace();
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

    public void deleteSession(String sessionId) {
        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("historySessions")
                .document(sessionId)
                .delete();
    }

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
                        s.setWeight((w != null) ? (Double) w : 0.0);
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
}