package com.example.myappmtp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ConfirmationDialog {

    // Method to display a confirmation dialog
    public static void show(Context context, DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setMessage("Do you want to send the created image?") // Content of the dialog
                .setPositiveButton("Yes", positiveListener) // Handle when the user clicks Yes
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Dismiss the dialog when clicking No
                    }
                })
                .create()
                .show();
    }
}
