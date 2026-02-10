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

public class HistoryRemoteDataSource {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private HistoryCallback callback;

    public HistoryRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void setCallback(HistoryCallback callback) {
        this.callback = callback;
    }

    public void uploadSession(HistorySession session, List<HistoryWorkoutExercise> exercises, List<HistorySerie> series) {
        if (auth.getCurrentUser() == null) return;

        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("sessionId", session.sessionId);
        sessionMap.put("name", session.name);
        sessionMap.put("startTime", session.startTime);
        sessionMap.put("endTime", session.endTime);
        sessionMap.put("duration", session.duration);

        List<Map<String, Object>> exercisesListMap = new ArrayList<>();

        for (HistoryWorkoutExercise ex : exercises) {
            Map<String, Object> exMap = new HashMap<>();
            exMap.put("historyExerciseId", ex.historyExerciseId);
            exMap.put("sessionId", ex.sessionId);
            exMap.put("exerciseName", ex.exerciseName);
            exMap.put("orderIndex", ex.orderIndex);

            List<Map<String, Object>> seriesListMap = new ArrayList<>();
            for (HistorySerie s : series) {
                if (s.historyExerciseId.equals(ex.historyExerciseId)) {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("historySetId", s.historySetId);
                    sMap.put("historyExerciseId", s.historyExerciseId);
                    sMap.put("setNumber", s.setNumber);
                    sMap.put("weight", s.weight);
                    sMap.put("reps", s.reps);
                    sMap.put("isPersonalRecord", s.isPersonalRecord);
                    seriesListMap.add(sMap);
                }
            }
            exMap.put("series", seriesListMap);
            exercisesListMap.add(exMap);
        }

        sessionMap.put("exercises", exercisesListMap);

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("history_sessions")
                .document(session.sessionId)
                .set(sessionMap)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailureFromRemote(e);
                });
    }

    public void fetchHistoryFromRemote() {
        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("history_sessions")
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
                .collection("history_sessions")
                .document(sessionId)
                .delete();
    }

    private HistorySessionWithExercises parseDocumentToHistoryObject(DocumentSnapshot doc) {
        HistorySession session = new HistorySession();
        session.sessionId = doc.getString("sessionId");
        session.name = doc.getString("name");
        Long start = doc.getLong("startTime");
        session.startTime = (start != null) ? start : 0;
        Long end = doc.getLong("endTime");
        session.endTime = (end != null) ? end : 0;
        Long dur = doc.getLong("duration");
        session.duration = (dur != null) ? dur : 0;

        List<HistoryWorkoutExerciseWithSeries> exercisesWithSeries = new ArrayList<>();

        List<Map<String, Object>> exMaps = (List<Map<String, Object>>) doc.get("exercises");
        if (exMaps != null) {
            for (Map<String, Object> exMap : exMaps) {
                HistoryWorkoutExercise ex = new HistoryWorkoutExercise();
                ex.historyExerciseId = (String) exMap.get("historyExerciseId");
                ex.sessionId = (String) exMap.get("sessionId");
                ex.exerciseName = (String) exMap.get("exerciseName");
                Long order = (Long) exMap.get("orderIndex");
                ex.orderIndex = (order != null) ? order.intValue() : 0;

                List<HistorySerie> seriesList = new ArrayList<>();
                List<Map<String, Object>> sMaps = (List<Map<String, Object>>) exMap.get("series");
                if (sMaps != null) {
                    for (Map<String, Object> sMap : sMaps) {
                        HistorySerie s = new HistorySerie();
                        s.historySetId = (String) sMap.get("historySetId");
                        s.historyExerciseId = (String) sMap.get("historyExerciseId");
                        Long sNum = (Long) sMap.get("setNumber");
                        s.setNumber = (sNum != null) ? sNum.intValue() : 0;
                        Double w = (Double) sMap.get("weight");
                        s.weight = (w != null) ? (Double) w : 0.0;
                        Long r = (Long) sMap.get("reps");
                        s.reps = (r != null) ? r.intValue() : 0;
                        Boolean pr = (Boolean) sMap.get("isPersonalRecord");
                        s.isPersonalRecord = (pr != null) ? pr : false;

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