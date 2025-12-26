package com.example.pushapp.utils;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Training;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class TrainingsRecyclerViewAdapter
        extends RecyclerView.Adapter<TrainingsRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<Training> trainings;

    public TrainingsRecyclerViewAdapter(ArrayList<Training> trainings) {
        this.trainings = trainings;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText textInputName;
        private final TextInputEditText textInputDescription;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        // tells if the user is editing the text fields in the card
        private boolean isEditing = false;


        public ViewHolder(View view) {
            super(view);
            textInputName = view.findViewById(R.id.text_view_name);
            textInputDescription = view.findViewById(R.id.text_view_description);
            editButton = view.findViewById(R.id.edit_image_button);
            deleteButton = view.findViewById(R.id.delete_image_button);
        }

        public TextInputEditText getTextViewName() {
            return textInputName;
        }

        public TextInputEditText getTextViewDescription() {
            return textInputDescription;
        }

        public ImageButton getEditButton() {
            return editButton;
        }

        public ImageButton getDeleteButton() {
            return deleteButton;
        }

        public boolean isEditing() {
            return isEditing;
        }

        public void setEditing(boolean editing) {
            isEditing = editing;
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

        viewHolder.getTextViewName().setText(trainings.get(position).getName());
        viewHolder.getTextViewDescription().setText(trainings.get(position).getDescription());

        viewHolder.getEditButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                handleEditButtonClick(viewHolder);
            }});

        viewHolder.getDeleteButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                handleDeleteButtonClick(viewHolder);
            }});

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                handleCardClick(viewHolder);
            }});
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return trainings.size();
    }

    private void handleEditButtonClick(ViewHolder viewHolder){
        if(viewHolder.isEditing()) {
            viewHolder.getTextViewName().setFocusable(false);
            viewHolder.getTextViewDescription().setFocusable(false);
            viewHolder.getTextViewName().setFocusableInTouchMode(false);
            viewHolder.getTextViewDescription().setFocusableInTouchMode(false);
            viewHolder.setEditing(false);
            viewHolder.getEditButton().setImageResource(R.drawable.edit);

        } else {
            viewHolder.getTextViewName().setFocusable(true);
            viewHolder.getTextViewDescription().setFocusable(true);
            viewHolder.getTextViewName().setFocusableInTouchMode(true);
            viewHolder.getTextViewDescription().setFocusableInTouchMode(true);
            viewHolder.setEditing(true);
            viewHolder.getEditButton().setImageResource(R.drawable.check);
        }
    }

    private void handleDeleteButtonClick(ViewHolder viewHolder) {
        new MaterialAlertDialogBuilder(viewHolder.itemView.getContext())
                .setTitle(R.string.confirm_operation)
                .setMessage(R.string.are_you_sure_delete)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    private void handleCardClick(ViewHolder viewholder){
        NavController navController = Navigation.findNavController(viewholder.itemView);
        Bundle id = new Bundle();
        id.putString("trainingId", trainings.get(viewholder.getBindingAdapterPosition()).getId());
        navController.navigate(R.id.nav_training_to_training_days, id);

    }
}
