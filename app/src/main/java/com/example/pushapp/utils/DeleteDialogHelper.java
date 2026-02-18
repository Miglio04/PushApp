package com.example.pushapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.example.pushapp.R;

/**
 * Helper class to show a reusable delete confirmation dialog.
 */
public class DeleteDialogHelper {

    /**
     * Callback interface for delete confirmation.
     */
    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed();
    }

    /**
     * Shows a delete confirmation dialog with custom title and message.
     *
     * @param context  The context to use for the dialog.
     * @param titleRes The string resource ID for the title.
     * @param messageRes The string resource ID for the message.
     * @param listener The callback to invoke when delete is confirmed.
     */
    public static void show(Context context, @StringRes int titleRes, @StringRes int messageRes, OnDeleteConfirmedListener listener) {
        show(context, context.getString(titleRes), context.getString(messageRes), listener);
    }

    /**
     * Shows a delete confirmation dialog with custom title and message strings.
     *
     * @param context  The context to use for the dialog.
     * @param title    The title text.
     * @param message  The message text.
     * @param listener The callback to invoke when delete is confirmed.
     */
    public static void show(Context context, String title, String message, OnDeleteConfirmedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDeleteMessage);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteConfirmed();
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

