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
import com.example.pushapp.models.roomModels.helpers.HistorySessionWithExercises;
import com.example.pushapp.viewModels.HistoryViewModel;
import com.example.pushapp.viewModels.ViewModelFactory;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnHistoryInteractionListener {
    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    private TextView emptyStateText;
    private TextInputEditText searchEditText;

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

        emptyStateText = view.findViewById(R.id.history_empty_state_text);
        searchEditText = view.findViewById(R.id.search_edit_text);
        RecyclerView rv = view.findViewById(R.id.history_recycler_view);

        emptyStateText.setText("No history found");

        historyAdapter = new HistoryAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(historyAdapter);

        observeData();
        initSearch();
    }

    @Override
    public void onDeleteClicked(HistorySessionWithExercises session) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_workout, null);
        AlertDialog d = new AlertDialog.Builder(requireContext()).setView(v).create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
            Snackbar.make(requireView(), "Session deleted", Snackbar.LENGTH_LONG).show();
        });
        btnCancel.setOnClickListener(view -> d.dismiss());
        d.show();
    }

    private void observeData() {
        historyViewModel.getHistorySessions().observe(getViewLifecycleOwner(), sessions -> {
            historyAdapter.updateHistory(sessions);
            emptyStateText.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
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
