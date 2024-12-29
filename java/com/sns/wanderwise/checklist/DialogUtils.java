package com.sns.wanderwise.checklist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.android.material.button.MaterialButton;
import com.sns.wanderwise.utils.WeatherBottomSheetDialogFragment;
import android.os.Bundle;
import java.util.ArrayList;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.sns.wanderwise.R;

public class DialogUtils {

    // Create and show a progress dialog using Material components
    public static Dialog showProgressDialog(Context context, String message) {
        Dialog dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);

        TextView messageText = view.findViewById(R.id.dialogprogressMessage);
        messageText.setText(message);

        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    public static Dialog showRomeoProgressDialog(Context context, String message) {
        Dialog dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);

        TextView messageText = view.findViewById(R.id.dialogprogressMessage);
        messageText.setText(message);

        dialog.setContentView(view);
        dialog.setCancelable(false);

        return dialog;
    }
 
    public static Dialog showAddDestinationDialog(final Context context, final DialogUtils.OnAddDestinationListener listener) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_destination, null);
    
    builder.setView(view);

    final TextInputEditText destinationInput = view.findViewById(R.id.destination_input);
    final RadioGroup radioGroup = view.findViewById(R.id.radio_group_destination);
    final Button add = view.findViewById(R.id.add);

    builder.setTitle("Add Destination");

    // Set the dialog to be cancelable
    builder.setCancelable(true);

    // Define the action for the custom "Add" button
    add.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newDestination = destinationInput.getText().toString().trim();
            int selectedId = radioGroup.getCheckedRadioButtonId();

            // Check if destination is empty
            if (newDestination.isEmpty()) {
                Toast.makeText(context, "Destination cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if no RadioButton is selected
            if (selectedId == -1) {
                Toast.makeText(context, "Please select a travel type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the selected RadioButton text
            RadioButton selectedRadioButton = view.findViewById(selectedId);
            String selectedType = selectedRadioButton.getText().toString();

            // Call the listener with both destination and type
            if (listener != null) {
                listener.onAddDestination(newDestination + " - " + selectedType);
            }
        }
    });

    // Remove the automatic buttons by not setting positive and negative buttons
    // builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {...}); // Remove this line
    // builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {...}); // Remove this line

    AlertDialog dialog = builder.create();
    dialog.show();
    return dialog;
}




    public static void showUserInfoDialog(Context context, String username, final DialogUtils.OnSignOutListener listener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.account, null);

        builder.setView(view);
        builder.setCancelable(true);

        MaterialButton signOutButton = view.findViewById(R.id.acountSignOut);
        TextView User = view.findViewById(R.id.acountUser);
        User.setText(username);
        AlertDialog dialog = builder.create();
        // Set an OnClickListener for the sign-out button
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSignOut(); // Trigger the sign-out action
                }
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }

    // Listener interface for sign-out action
    public interface OnSignOutListener {
        void onSignOut();
    }

    // Listener interface for adding a new destination
    public interface OnAddDestinationListener {
        void onAddDestination(String destination);
    }
}

