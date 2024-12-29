package com.sns.wanderwise;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.ListView;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import com.sns.wanderwise.checklist.ChecklistItem;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.util.SparseBooleanArray;

public class SavedItemsActivity extends AppCompatActivity {

    private ArrayList<ChecklistItem> savedItems;
    private TextView totalWeightTextView; // TextView to display the total weight
    private ListView listView;
    private ImageView selectAllButton, deleteButton;
    private ArrayAdapter<String> adapter;
    private SparseBooleanArray selectedPositions; // To track selected items
    private boolean isAllSelected = false; // Flag to track if all items are selected
    private String travelMode; // Declare travelMode variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        // Initialize the views
        totalWeightTextView = findViewById(R.id.totalWeightTextView);
        listView = findViewById(R.id.savedItemsListView);
        selectAllButton = findViewById(R.id.selectAllButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Get the current destination and travel mode from the Intent
        String currentDestination = getIntent().getStringExtra("current");
        travelMode = getIntent().getStringExtra("travel_mode"); // Retrieve travelMode

        // Load saved items for the current destination
        savedItems = loadSavedItems(currentDestination);

        if (savedItems != null && !savedItems.isEmpty()) {
            displaySavedItems(savedItems); // Display items along with weight
            updateTotalWeight(savedItems); // Update the total weight
        } else {
            Toast.makeText(
                            this,
                            "No saved items found for " + currentDestination,
                            Toast.LENGTH_SHORT)
                    .show();
        }

        // Set up the select all button with toggle behavior
        selectAllButton.setOnClickListener(v -> toggleSelectAllItems());

        // Set up the delete button
        deleteButton.setOnClickListener(v -> deleteSelectedItems());
    }

    private ArrayList<ChecklistItem> loadSavedItems(String destination) {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedItems", MODE_PRIVATE);
        Gson gson = new Gson();
        String key = "selectedItems_" + destination; // Use destination-specific key
        String json = sharedPreferences.getString(key, null);
        Log.d(
                "LoadDebug",
                "Loaded items for " + destination + ": " + json); // Debug log for loaded data

        // Deserialize JSON into an ArrayList
        Type type = new TypeToken<ArrayList<ChecklistItem>>() {}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }

    private void displaySavedItems(ArrayList<ChecklistItem> items) {
        selectedPositions = new SparseBooleanArray(); // Initialize selected positions

        // Custom ArrayAdapter to modify item appearance dynamically
        adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_multiple_choice,
                        getItemNamesWithWeight(items)) {
                    @NonNull
                    @Override
                    public View getView(
                            int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        // Get the default view
                        View view = super.getView(position, convertView, parent);

                        // Modify the appearance of CheckedTextView
                        if (view instanceof CheckedTextView) {
                            CheckedTextView checkedTextView = (CheckedTextView) view;
                            checkedTextView.setTypeface(
                                    null, Typeface.NORMAL); // Set text style to normal
                            checkedTextView.setTextSize(14); // Set text size
                            checkedTextView.setPadding(50, 8, 50, 8);
                        }
                        return view;
                    }
                };

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
    }

    private void toggleSelectAllItems() {
        int itemCount = listView.getCount();
        if (isAllSelected) {
            // Deselect all items if they are already selected
            for (int i = 0; i < itemCount; i++) {
                listView.setItemChecked(i, false);
            }
            isAllSelected = false; // Update flag
            selectAllButton.setImageResource(R.drawable.select_all_ic); // Set to unselected image
        } else {
            // Select all items if they are not all selected
            for (int i = 0; i < itemCount; i++) {
                listView.setItemChecked(i, true);
            }
            isAllSelected = true; // Update flag
            selectAllButton.setImageResource(R.drawable.deselect_ic); // Set to selected image
        }
    }

    private List<String> getItemNamesWithWeight(ArrayList<ChecklistItem> items) {
        List<String> names = new ArrayList<>();
        for (ChecklistItem item : items) {
            // Convert weight from grams to kilograms for display
            double weightInKg = item.getWeight() / 1000.0; // Convert grams to kilograms
            names.add(item.getQuantity() + " " + item.getName() + "\nWeight: " + weightInKg + "kg");
        }
        return names;
    }

    private void updateTotalWeight(ArrayList<ChecklistItem> items) {
        double totalWeight = 0;

        // Calculate total weight in grams
        for (ChecklistItem item : items) {
            totalWeight += item.getWeight()
            /** item.getQuantity() */
            ;
        }

        // Convert total weight to kilograms
        double totalWeightInKg = totalWeight / 1000.0;

        // Update the TextView with the total weight in kilograms

        if ("Air transportation".equals(travelMode)) {
            totalWeightTextView.setText(
                    "Total Weight: " + String.format("%.2f", totalWeightInKg) + " kg/20kg");
        } else {
            totalWeightTextView.setText(
                    "Total Weight: " + String.format("%.2f", totalWeightInKg) + " kg");
        }
        // Check for weight limit warning for air transportation
        if ("Air transportation".equals(travelMode) && totalWeightInKg > 20.0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Warning")
                    .setMessage(
                            "The total weight exceeds 20 kg. Please review your items or else you need to pay another baggage allowance.")
                    .setPositiveButton("Confirm", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void deleteSelectedItems() {
        // Check if there are any items in the list
        if (savedItems.isEmpty()) {
            showToast("No items available to delete.");
            return; // Exit early if the list is empty
        }

        ArrayList<ChecklistItem> itemsToDelete = new ArrayList<>();

        // Get selected items using getCheckedItemPositions
        SparseBooleanArray checkedPositions = listView.getCheckedItemPositions();

        // Iterate over all positions in the ListView
        for (int i = 0; i < checkedPositions.size(); i++) {
            int position = checkedPositions.keyAt(i);
            if (checkedPositions.get(position)) {
                itemsToDelete.add(savedItems.get(position)); // Add selected items to the list
            }
        }

        // Check if there are any items selected
        if (itemsToDelete.isEmpty()) {
            showToast("No items selected for deletion.");
            return; // Exit early if no items were selected
        }

        // Show confirmation dialog only if there are items to delete
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete the selected items?")
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {
                            savedItems.removeAll(
                                    itemsToDelete); // Remove selected items from the list
                            saveUpdatedItems(); // Save the updated list back to SharedPreferences
                            updateTotalWeight(savedItems); // Update the total weight after deletion
                            displaySavedItems(savedItems); // Refresh the display
                            showToast("Selected items deleted successfully.");
                        })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveUpdatedItems() {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedItems", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String key = "selectedItems_" + getIntent().getStringExtra("current");

        // Convert updated list to JSON and save
        String updatedJson = gson.toJson(savedItems);
        editor.putString(key, updatedJson);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
