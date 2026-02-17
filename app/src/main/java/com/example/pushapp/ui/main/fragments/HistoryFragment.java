package com.example.pushapp.ui.main.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.adapter.HistoryAdapter;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.utils.DeleteDialogHelper;
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnHistoryInteractionListener {
    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    private View emptyStateContainer;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(requireContext())).get(HistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        searchEditText = view.findViewById(R.id.search_edit_text);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        RecyclerView rv = view.findViewById(R.id.history_recycler_view);

        historyAdapter = new HistoryAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(historyAdapter);

        observeData();
        initSearch();
        initFilters();
    }

    @Override
    public void onDeleteClicked(HistorySessionWithExercises session) {
        DeleteDialogHelper.show(
            requireContext(),
            R.string.delete_workout_title,
            R.string.delete_workout_message,
            () -> historyViewModel.deleteSession(session)
        );
    }

    private void observeData() {
        historyViewModel.getHistorySessions().observe(getViewLifecycleOwner(), sessions -> {
            historyAdapter.updateHistory(sessions);
            emptyStateContainer.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void initSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                historyViewModel.searchHistory(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void initFilters() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                historyViewModel.filterByPeriod(HistoryViewModel.FilterPeriod.ALL);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                historyViewModel.filterByPeriod(HistoryViewModel.FilterPeriod.ALL);
            } else if (checkedId == R.id.chip_week) {
                historyViewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_WEEK);
            } else if (checkedId == R.id.chip_month) {
                historyViewModel.filterByPeriod(HistoryViewModel.FilterPeriod.THIS_MONTH);
            }
        });
    }
}
