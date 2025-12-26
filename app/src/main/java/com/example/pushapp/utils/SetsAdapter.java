package com.example.pushapp.utils;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pushapp.R;
import com.example.pushapp.models.Serie; // <-- USA IL NUOVO MODELLO

import java.util.List;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetViewHolder> {

    private List<Serie> series;

    // 1. Interfaccia per comunicare con l'esterno (il Fragment o un altro Adapter)
    public interface OnSetInteractionListener {
        void onSetUpdated(int position, double newWeight, int newReps);
        void onSetDeleted(int position);
    }

    private final OnSetInteractionListener listener;

    // 2. Il costruttore ora accetta una lista di 'Serie' e il listener
    public SetsAdapter(List<Serie> series, OnSetInteractionListener listener) {
        this.series = series;
        this.listener = listener;
    }

    // Metodo per aggiornare i dati dall'esterno
    public void setSeries(List<Serie> newSeries) {
        this.series = newSeries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_set_row, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Serie serie = series.get(position);

        holder.setNumber.setText("Set " + serie.getSerieNumber());
        // Usa i campi corretti dal modello 'Serie'
        holder.setDetails.setText(serie.getTargetWeight() + "kg x " + serie.getTargetReps() + " reps");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                // Passiamo la posizione corrente aggiornata
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    showEditDialog(holder.itemView.getContext(), series.get(currentPos), currentPos);
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    // Notifica l'evento di eliminazione
                    listener.onSetDeleted(currentPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return series != null ? series.size() : 0;
    }

    private void showEditDialog(Context context, Serie serie, int position) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputWeight = new EditText(context);
        inputWeight.setHint("Peso (kg)");
        inputWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputWeight.setText(String.valueOf(serie.getTargetWeight())); // Usa getTargetWeight
        layout.addView(inputWeight);

        final EditText inputReps = new EditText(context);
        inputReps.setHint("Ripetizioni");
        inputReps.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputReps.setText(String.valueOf(serie.getTargetReps())); // Usa getTargetReps
        layout.addView(inputReps);

        new AlertDialog.Builder(context)
                .setTitle("Modifica Serie")
                .setView(layout)
                .setPositiveButton("Conferma", (dialog, which) -> {
                    try {
                        double newWeight = Double.parseDouble(inputWeight.getText().toString());
                        int newReps = Integer.parseInt(inputReps.getText().toString());

                        // 3. Notifica l'evento di modifica invece di cambiare il modello direttamente
                        if (listener != null) {
                            listener.onSetUpdated(position, newWeight, newReps);
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Input non valido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {
        final TextView setNumber;
        final TextView setDetails;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        public SetViewHolder(View itemView) {
            super(itemView);
            setNumber = itemView.findViewById(R.id.set_number);
            setDetails = itemView.findViewById(R.id.set_details);
            btnEdit = itemView.findViewById(R.id.btn_edit_set);
            btnDelete = itemView.findViewById(R.id.btn_delete_set);
        }
    }
}
