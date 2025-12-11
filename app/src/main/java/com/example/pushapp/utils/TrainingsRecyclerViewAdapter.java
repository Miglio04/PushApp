package com.example.pushapp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;

import java.util.ArrayList;

public class TrainingsRecyclerViewAdapter
        extends RecyclerView.Adapter<TrainingsRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<Training> trainings;

    public TrainingsRecyclerViewAdapter(ArrayList<Training> trainings) {
        this.trainings = trainings;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewDescription;


        public ViewHolder(View view) {
            super(view);
            textViewName= (TextView) view.findViewById(R.id.text_view_name);
            textViewDescription= (TextView) view.findViewById(R.id.text_view_description);
        }

        public TextView getTextViewName() {
            return textViewName;
        }

        public TextView getTextViewDescription() {
            return textViewDescription;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.training_card_view, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextViewName().setText(trainings.get(position).getName());
        viewHolder.getTextViewDescription().setText(trainings.get(position).getDescription());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return trainings.size();
    }
}
