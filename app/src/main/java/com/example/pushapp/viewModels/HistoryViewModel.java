package com.example.pushapp.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.repositories.HistoryRepository;

public class HistoryViewModel extends ViewModel {
    private final HistoryRepository repository;
    public HistoryViewModel(HistoryRepository repository) { this.repository = repository; }
    public LiveData<Result> getHistoryList() { return repository.getHistoryList(); }
    public void fetchHistory() { repository.getHistoryList(); }
    public void searchHistory(String query) { repository.searchHistory(query); }
    public void deleteSession(HistorySessionWithExercises wrapper) {
        if (wrapper != null && wrapper.session != null) repository.deleteSession(wrapper.session.historySessionId);
    }
}