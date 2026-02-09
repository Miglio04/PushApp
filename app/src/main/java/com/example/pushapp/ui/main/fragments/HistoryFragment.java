package com.example.pushapp.ui.main.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pushapp.R;
import com.example.pushapp.adapter.HistoryAdapter;
import com.example.pushapp.models.Result;
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnHistoryInteractionListener {
    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    private TextView emptyStateText;
    private TextInputEditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        emptyStateText = root.findViewById(R.id.history_empty_state_text);
        searchEditText = root.findViewById(R.id.search_edit_text);
        RecyclerView rv = root.findViewById(R.id.history_recycler_view);
        emptyStateText.setText("No history found");
        historyAdapter = new HistoryAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(historyAdapter);
        ViewModelFactory factory = new ViewModelFactory(requireContext());
        historyViewModel = new ViewModelProvider(this, factory).get(HistoryViewModel.class);
        observeData();
        initSearch();
        historyViewModel.fetchHistory();
        return root;
    }

    @Override public void onHistoryClicked(HistorySessionWithExercises session) {}

    @Override
    public void onDeleteClicked(HistorySessionWithExercises session) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_workout, null);
        AlertDialog d = new AlertDialog.Builder(requireContext()).setView(v).create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView icon = v.findViewById(R.id.iconError);
        TextView title = v.findViewById(R.id.tvDeleteTitle);
        TextView msg = v.findViewById(R.id.tvErrorMessage);
        Button btnOk = v.findViewById(R.id.btnErrorOk);
        TextView btnCancel = v.findViewById(R.id.btnErrorCancel);

        title.setText("Delete Workout");
        msg.setText("Are you sure you want to delete this session? This action cannot be undone.");
        btnOk.setText("DELETE");
        btnCancel.setText("Cancel");

        btnOk.setOnClickListener(view -> {
            historyViewModel.deleteSession(session);
            d.dismiss();
            // Toast rimosso: eliminazione silenziosa
        });

        btnCancel.setOnClickListener(view -> d.dismiss());
        d.show();
    }

    private void observeData() {
        historyViewModel.getHistoryList().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.HistorySuccess) {
                List<HistorySessionWithExercises> data = ((Result.HistorySuccess) result).getData();
                historyAdapter.updateHistory(data);
                emptyStateText.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }
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
}