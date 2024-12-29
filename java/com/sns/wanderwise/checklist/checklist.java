package com.sns.wanderwise.checklist;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.Button;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sns.wanderwise.MainActivity;
import com.sns.wanderwise.MoreWebview;
import com.sns.wanderwise.R;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sns.wanderwise.SavedItemsActivity;
import com.sns.wanderwise.Transportation;
import com.sns.wanderwise.WeatherActivity;
import com.sns.wanderwise.utils.WeatherBottomSheetDialogFragment;
import java.lang.reflect.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sns.wanderwise.checklist.checklist;
import java.util.ArrayList;
import java.util.List;

public class checklist extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private ListView checklistListView;
    private MaterialCardView Cbooking, Ctravelway, Cattraction;
    private MaterialTextView title, totalWeightText;
    private MaterialButton addItemButton, totalWeightButton;
    private TextInputEditText itemNameInput, itemWeightInput, ItemQuantityInput;
    private ChecklistAdapter checklistAdapter;
    private List<ChecklistItem> checklistItems;
    private ImageView viewSavedItemsButton,
            locationButton,
            selectallButton,
            deleteButton,
            editButton,
            show,
            bagButton,
            modeindicator;
    private MaterialButton toggleImageView;
    private Spinner weightUnitSpinner;
    private String image, currentLocation, destination, travelType, mode, travelMode;
    private LinearLayout essential, showEssential;
    private TextView destinationtype, modelocation, anim;
    private boolean isAllSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist);

        initializeViews();
        setupListeners();

        Intent intent = getIntent();
        image = intent.getStringExtra("image");
        currentLocation = intent.getStringExtra("current");
        destination = intent.getStringExtra("current");
        travelType = intent.getStringExtra("travel_type");
        travelMode = intent.getStringExtra("travel_mode");
        mode = intent.getStringExtra("locationMode");

        sharedPreferences = getSharedPreferences(ChecklistLocation.PREFS_NAME, MODE_PRIVATE);
        checklistItems = loadChecklistItems(destination);

        boolean showDialog = getIntent().getBooleanExtra("SHOW_DIALOG", false);
        if (showDialog) {
            showInitialItemsDialog(travelType);
        }

        checklistAdapter = new ChecklistAdapter(this, checklistItems);
        checklistListView.setAdapter(checklistAdapter);
        checklistListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        title.setText("Travel" + " To " + destination);
        destinationtype.setText("Destination Type: " + travelType);
        modelocation.setText("Transportation mode: " + mode);

        if (travelMode.equals("Air transportation")) {
            modeindicator.setImageResource(R.drawable.air_ic);
            showAirlineDialog();
        } else if (travelMode.equals("Land transportation")) {
            modeindicator.setImageResource(R.drawable.bus_ic);
        } else {
            modeindicator.setImageResource(R.drawable.boat_ic);
            showWaterDialog();
        }
    }

    private void initializeViews() {

        checklistListView = findViewById(R.id.checklist_list_view);
        totalWeightText = findViewById(R.id.total_weight_text);
        title = findViewById(R.id.checklist_title);
        totalWeightButton = findViewById(R.id.total_weight_button);
        toggleImageView = findViewById(R.id.add);
        selectallButton = findViewById(R.id.selectall_button);
        deleteButton = findViewById(R.id.delete);
        editButton = findViewById(R.id.edit_button);
        bagButton = findViewById(R.id.bag_button);
        modeindicator = findViewById(R.id.mode_ic);
        locationButton = findViewById(R.id.location);
        show = findViewById(R.id.show);
        showEssential = findViewById(R.id.showEssentialMenu);
        essential = findViewById(R.id.essentials);
        destinationtype = findViewById(R.id.destinationType);
        modelocation = findViewById(R.id.mode);
        Cbooking = findViewById(R.id.booking);
        Ctravelway = findViewById(R.id.travelways);
        Cattraction = findViewById(R.id.attractions);
        anim = findViewById(R.id.anim);
        viewSavedItemsButton = findViewById(R.id.viewsaveditemsbutton);
        deleteButton.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);
        selectallButton.setVisibility(View.GONE);
        toggleImageView.setText("Add Item");
        essential.setVisibility(View.GONE);
        bagButton.setVisibility(View.GONE);
        totalWeightText.setVisibility(View.GONE);
        startTextViewAnimation(anim);
    }

    private void setupListeners() {

        Animation popupAnim = AnimationUtils.loadAnimation(this, R.anim.popup_anim);
        Animation reversePopupAnim = AnimationUtils.loadAnimation(this, R.anim.reverse_popup_anim);

        totalWeightButton.setOnClickListener(v -> updateTotalWeight(travelMode));
        toggleImageView.setOnClickListener(
                v -> {
                    addItemDialog();
                    updateActionButtonsVisibility();
                    checklistAdapter.clearSelection();
                });

        checklistListView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    checklistAdapter.toggleSelection(position);
                    updateActionButtonsVisibility();
                    deleteButton.startAnimation(popupAnim);
                    selectallButton.startAnimation(popupAnim);
                    editButton.startAnimation(popupAnim);
                    bagButton.startAnimation(popupAnim);
                    view.startAnimation(popupAnim);

                    final int selectedPosition = position;

                    editButton.setOnClickListener(
                            v -> {
                                showEditItemDialog(selectedPosition);
                            });
                });
        selectallButton.setOnClickListener(
                v -> {
                    selectallButton.startAnimation(reversePopupAnim);
                    reversePopupAnim.setAnimationListener(
                            new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    updateActionButtonsVisibility();
                                    toggleSelectAll();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                });

        deleteButton.setOnClickListener(
                v -> {
                    deleteButton.startAnimation(reversePopupAnim);
                    reversePopupAnim.setAnimationListener(
                            new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    // Optionally, you can disable buttons during the animation to
                                    // avoid clicking multiple times
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    // Perform actions after animation ends
                                    removeSelectedItems(); // Remove items
                                    updateActionButtonsVisibility(); // Update button visibility

                                    // Optionally, you can re-enable buttons if disabled during
                                    // animation
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    // You can handle repeating animations if needed
                                }
                            });
                });

        bagButton.setOnClickListener(
                v -> {
                    bagButton.startAnimation(reversePopupAnim);
                    reversePopupAnim.setAnimationListener(
                            new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    // Optionally, disable buttons during animation to prevent
                                    // multiple clicks
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    // Perform actions after animation ends
                                    copyAllSelectedItemsToNextActivity(destination); // Copy items
                                    updateActionButtonsVisibility(); // Update button visibility

                                    // Optionally, you can re-enable buttons if disabled during
                                    // animation
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    // Handle repeating animations if needed
                                }
                            });
                });

        showEssential.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (essential.getVisibility() == View.VISIBLE) {
                            // Collapse essential
                            show.setImageResource(R.drawable.down_ic); // Update icon
                            essential
                                    .animate()
                                    .translationY(-essential.getHeight())
                                    .alpha(0.0f)
                                    .setDuration(300)
                                    .withEndAction(() -> essential.setVisibility(View.GONE))
                                    .start();
                        } else {
                            essential.post(
                                    () -> {
                                        show.setImageResource(R.drawable.up_ic);
                                        essential.setTranslationY(-essential.getHeight());
                                        essential.setAlpha(0.0f);
                                        essential.setVisibility(View.VISIBLE);
                                        updateTotalWeight(travelMode);

                                        essential
                                                .animate()
                                                .translationY(0)
                                                .alpha(1.0f)
                                                .setDuration(300)
                                                .start();
                                    });
                        }
                    }
                });

        locationButton.setOnClickListener(
                v -> {
                    showInitialItemsDialog(travelType);
                    checklistAdapter.clearSelection();
                });
        Cbooking.setOnClickListener(
                v -> {
                    Intent intent = new Intent(checklist.this, WeatherActivity.class);
                    intent.putExtra("key", destination);
                    startActivity(intent);
                    checklistAdapter.clearSelection();
                });

        Ctravelway.setOnClickListener(
                v -> {
                    Intent intent = new Intent(checklist.this, Transportation.class);
                    intent.putExtra("toLocation", destination);
                    startActivity(intent);
                    checklistAdapter.clearSelection();
                });

        Cattraction.setOnClickListener(
                v -> {
                    Intent intent = new Intent(checklist.this, MoreWebview.class);
                    intent.putExtra("key", destination);
                    startActivity(intent);
                    checklistAdapter.clearSelection();
                });
        viewSavedItemsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(this, SavedItemsActivity.class);
                    intent.putExtra("current", destination); // Pass the destination
                    intent.putExtra("travel_mode", travelMode);
                    startActivity(intent);
                    checklistAdapter.clearSelection();
                });
    }

    private void startTextViewAnimation(TextView textView) {
        ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f, 0f);
        fadeInOut.setDuration(2000); // Total duration for fade in and fade out
        fadeInOut.setRepeatMode(ValueAnimator.RESTART);
        fadeInOut.setRepeatCount(ValueAnimator.INFINITE);
        fadeInOut.start();
    }

    private void showEditItemDialog(int position) {
        ChecklistItem item = checklistItems.get(position);

        // Inflate the edit dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        itemNameInput = dialogView.findViewById(R.id.item_name_input);
        itemWeightInput = dialogView.findViewById(R.id.item_weight_input);
        ItemQuantityInput = dialogView.findViewById(R.id.item_quantity_input);
        weightUnitSpinner = dialogView.findViewById(R.id.weight_unit_spinner);
        addItemButton = dialogView.findViewById(R.id.add_item_button);
        addItemButton.setText("Update");

        // Set up the input fields
        itemNameInput.setText(item.getName());
        ItemQuantityInput.setText(String.valueOf(item.getQuantity()));

        // Populate weight units
        List<String> weightUnits = new ArrayList<>();
        weightUnits.add("g");
        weightUnits.add("kg");

        // Set up spinner adapter
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weightUnits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitSpinner.setAdapter(adapter);

        String currentWeightUnit = item.getWeightUnit();
        int weightValue = item.getWeight(); // Get raw weight value from the item
        int quantityValue = item.getQuantity();
        // Debugging: Log the current weight and unit
        Log.d("EditDialog", "Raw weight: " + weightValue + ", Weight unit: " + currentWeightUnit);

        if ("kg".equals(currentWeightUnit)) {
            // Check if weight is greater than or equal to 1000 grams before converting
            if (weightValue >= 1000) {
                weightValue = weightValue / 1000; // Convert grams to kilograms for display
                Log.d("EditDialog", "Converted weight to kg: " + weightValue);
                weightUnitSpinner.setSelection(weightUnits.indexOf("kg"));
            } else {
                // If weight is less than 1000, keep raw value and set unit to grams
                Log.d(
                        "EditDialog",
                        "Weight below 1000, keeping raw value and setting unit to grams");
                weightUnitSpinner.setSelection(weightUnits.indexOf("g"));
            }
        } else {
            // Default to grams if unit is already "g" or unrecognized
            Log.d("EditDialog", "Using weight in grams: " + weightValue);
            weightUnitSpinner.setSelection(weightUnits.indexOf("g"));
        }

        // Set the weight value in the input field
        itemWeightInput.setText(String.valueOf(weightValue / quantityValue));

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Edit Item")
                        .setView(dialogView)
                        .show();

        addItemButton.setOnClickListener(
                v -> {
                    String itemName = itemNameInput.getText().toString().trim();
                    String itemWeightStr = itemWeightInput.getText().toString().trim();
                    String itemQuantityStr = ItemQuantityInput.getText().toString().trim();
                    String selectedWeightUnit = weightUnitSpinner.getSelectedItem().toString();

                    if (validateInputs(itemName, itemWeightStr, itemQuantityStr)) {
                        int weightPerUnit = Integer.parseInt(itemWeightStr);
                        int quantity = Integer.parseInt(itemQuantityStr);

                        // Convert weight to grams if unit is "kg"
                        if ("kg".equals(selectedWeightUnit)) {
                            weightPerUnit *= 1000;
                        }

                        // Calculate total weight
                        int totalWeight = weightPerUnit * quantity;

                        // Update item details
                        item.setName(itemName);
                        item.setWeight(totalWeight); // Use total weight instead of weight per unit
                        item.setQuantity(quantity);
                        item.setWeightUnit(selectedWeightUnit);

                        // Update UI and save changes
                        checklistAdapter.notifyDataSetChanged();
                        updateTotalWeight(travelMode);
                        saveChecklistItems(destination);
                        // checklistAdapter.clearSelection();
                        dialog.dismiss();
                    }
                });
    }

    private void showAirlineDialog() {
        AlertDialog airlineDialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Warning")
                        .setMessage(
                                "1. Avoid bringing sharp objects, flammable materials, or hazardous equipment.\n\n"
                                        + "2. Liquids must fit into a clear, quart-sized zip-top bag and must not be over 3.4 oz (100.6 ml) per container.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .create();
        airlineDialog.show();
    }

    private void showWaterDialog() {
        AlertDialog airlineDialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Warning")
                        .setMessage(
                                "1. Avoid bringing sharp objects, flammable materials, or hazardous equipment.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .create();
        airlineDialog.show();
    }

    private void showWeatherBottomSheet(String location) {
        WeatherBottomSheetDialogFragment bottomSheet = new WeatherBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("location", location);
        bottomSheet.setArguments(args);
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    private void addItemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        addItemButton = dialogView.findViewById(R.id.add_item_button);
        itemNameInput = dialogView.findViewById(R.id.item_name_input);
        itemWeightInput = dialogView.findViewById(R.id.item_weight_input);
        ItemQuantityInput =
                dialogView.findViewById(R.id.item_quantity_input); // Initialize quantity input
        weightUnitSpinner = dialogView.findViewById(R.id.weight_unit_spinner);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this, R.array.weight_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitSpinner.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).show();

        addItemButton.setOnClickListener(
                v -> {
                    String itemName = itemNameInput.getText().toString().trim();
                    String itemWeight = itemWeightInput.getText().toString().trim();
                    String itemQuantity = ItemQuantityInput.getText().toString().trim();

                    if (itemName.isEmpty()) {
                        itemNameInput.setError("Item name required");
                    } else if (itemWeight.isEmpty()) {
                        itemWeightInput.setError("Item weight required");
                    } else if (itemQuantity.isEmpty()) {
                        ItemQuantityInput.setError("Quantity required");
                    } else {
                        addItem(itemName, itemWeight, itemQuantity);
                        dialog.dismiss();
                    }
                });
    }

    private void addItem(String itemName, String itemWeightStr, String quantityStr) {
        String selectedUnit = weightUnitSpinner.getSelectedItem().toString();

        if (validateInputs(itemName, itemWeightStr, quantityStr)) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                int itemWeight = Integer.parseInt(itemWeightStr);

                // Convert weight to grams if the unit is kg
                if (selectedUnit.equals("kg")) {
                    itemWeight *= 1000;
                }

                // Calculate total item weight
                int totalItemWeight = itemWeight * quantity;

                // Check if there is an existing item with the same name
                int existingIndex = getItemIndexByName(itemName);

                if (existingIndex != -1) {
                    // Update the existing item's weight and quantity
                    ChecklistItem existingItem = checklistItems.get(existingIndex);
                    existingItem.setWeight(existingItem.getWeight() + totalItemWeight);
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);

                    checklistAdapter.notifyDataSetChanged();
                    Toast.makeText(
                                    this,
                                    "Updated item in the checklist: " + itemName,
                                    Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Add new item if no match is found
                    ChecklistItem newItem = new ChecklistItem(itemName, totalItemWeight, quantity);
                    checklistItems.add(newItem);

                    checklistAdapter.notifyDataSetChanged();
                    Toast.makeText(this, itemName + " added to the checklist", Toast.LENGTH_SHORT)
                            .show();
                }

                // Update total weight and save changes
                updateTotalWeight(travelMode);
                saveChecklistItems(destination);
                resetInputs();

            } catch (NumberFormatException e) {
                itemWeightInput.setError("Invalid weight or quantity");
            }
        }
    }

    private int getItemIndexByName(String itemName) {
        for (int i = 0; i < checklistItems.size(); i++) {
            if (checklistItems.get(i).getName().equalsIgnoreCase(itemName)) {
                return i; // Match found
            }
        }
        return -1; // No item found with the same name
    }

    private boolean validateInputs(String itemName, String itemWeightStr, String quantityStr) {
        boolean valid = true;

        if (itemName.isEmpty()) {
            itemNameInput.setError("Item name required");
            valid = false;
        }
        if (itemWeightStr.isEmpty()) {
            itemWeightInput.setError("Item weight required");
            valid = false;
        }
        if (quantityStr.isEmpty()) {
            ItemQuantityInput.setError("Quantity required"); // Corrected this line
            valid = false;
        }

        return valid;
    }

    private void resetInputs() {
        itemNameInput.setText("");
        itemWeightInput.setText("");
        weightUnitSpinner.setSelection(0);
    }

    private void updateTotalWeight(String travelMode) {
        int totalWeight = checklistItems.stream().mapToInt(ChecklistItem::getWeight).sum();
        double totalWeightInKg = totalWeight / 1000.0;
        totalWeightText.setText(totalWeightInKg + " kg");
    }

    private List<ChecklistItem> loadChecklistItems(String destination) {
        List<ChecklistItem> items = new ArrayList<>();
        int size = sharedPreferences.getInt(destination + "_size", 0);

        for (int i = 0; i < size; i++) {
            String name = sharedPreferences.getString(destination + "_item_" + i + "_name", null);
            int weight = sharedPreferences.getInt(destination + "_item_" + i + "_weight", 0);
            int quantity = sharedPreferences.getInt(destination + "_item_" + i + "_quantity", 1);

            if (name != null) {
                items.add(new ChecklistItem(name, weight, quantity));
            }
        }
        return items;
    }

    private void saveChecklistItems(String destination) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(destination + "_size", checklistItems.size());
        for (int i = 0; i < checklistItems.size(); i++) {
            ChecklistItem item = checklistItems.get(i);
            editor.putString(destination + "_item_" + i + "_name", item.getName());
            editor.putInt(destination + "_item_" + i + "_weight", item.getWeight());
            editor.putInt(destination + "_item_" + i + "_quantity", item.getQuantity());
        }
        editor.apply();
    }

    private void copyAllSelectedItemsToNextActivity(String destination) {
        SparseBooleanArray selectedPositions = checklistListView.getCheckedItemPositions();
        ArrayList<ChecklistItem> selectedItems = getSelectedItems(selectedPositions);

        if (!selectedItems.isEmpty()) {
            saveSelectedItems(selectedItems, destination);
            removeSelectedItems();
            showToast("Items Added successfully ! ");
            checklistAdapter.clearSelection();
        } else {
            showToast("No items selected.");
        }
    }

    private ArrayList<ChecklistItem> getSelectedItems(SparseBooleanArray selectedPositions) {
        ArrayList<ChecklistItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < selectedPositions.size(); i++) {
            int key = selectedPositions.keyAt(i);
            if (selectedPositions.get(key)) {
                selectedItems.add(checklistItems.get(key));
            }
        }
        return selectedItems;
    }

    private void saveSelectedItems(ArrayList<ChecklistItem> selectedItems, String destination) {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedItems", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String key = "selectedItems_" + destination;

        // Load existing items for the destination
        ArrayList<ChecklistItem> existingItems = loadExistingItems(sharedPreferences, gson, key);

        // Merge or add new items
        for (ChecklistItem newItem : selectedItems) {
            boolean itemExists = false;

            for (int i = 0; i < existingItems.size(); i++) {
                ChecklistItem existingItem = existingItems.get(i);

                if (existingItem.getName().equalsIgnoreCase(newItem.getName())) {
                    // Update weight and quantity for the same item name
                    existingItem.setWeight(existingItem.getWeight() + newItem.getWeight());
                    existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                    itemExists = true;

                    break;
                }
            }

            if (!itemExists) {
                // Add new item if it doesn't exist
                existingItems.add(newItem);
            }
        }

        // Save the updated list back to SharedPreferences
        saveItemsToPreferences(editor, gson, key, existingItems);
    }

    private ArrayList<ChecklistItem> loadExistingItems(
            SharedPreferences sharedPreferences, Gson gson, String key) {
        String existingJson = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<ChecklistItem>>() {}.getType();
        return existingJson != null ? gson.fromJson(existingJson, type) : new ArrayList<>();
    }

    private void saveItemsToPreferences(
            SharedPreferences.Editor editor,
            Gson gson,
            String key,
            ArrayList<ChecklistItem> items) {
        String updatedJson = gson.toJson(items);
        editor.putString(key, updatedJson);
        editor.apply();
    }

    private boolean isItemAlreadySaved(ChecklistItem item, ArrayList<ChecklistItem> existingItems) {
        for (ChecklistItem existingItem : existingItems) {
            if (existingItem.getName().equals(item.getName())) { // Compare unique identifiers
                return true;
            }
        }
        return false;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void removeSelectedItems() {
        SparseBooleanArray selectedPositions = checklistListView.getCheckedItemPositions();
        List<ChecklistItem> itemsToRemove = new ArrayList<>();

        for (int i = 0; i < selectedPositions.size(); i++) {
            int key = selectedPositions.keyAt(i);
            if (selectedPositions.get(key)) {
                itemsToRemove.add(checklistItems.get(key));
            }
        }

        checklistItems.removeAll(itemsToRemove);
        checklistAdapter.notifyDataSetChanged();
        checklistAdapter.clearSelection();
        checklistListView.clearChoices();
        updateTotalWeight(travelMode);
        saveChecklistItems(destination);

        isAllSelected = false;
        selectallButton.setImageResource(R.drawable.select_all_ic);
    }

    private void toggleSelectAll() {
        if (isAllSelected) {

            for (int i = 0; i < checklistItems.size(); i++) {
                checklistListView.setItemChecked(i, false);
            }
            checklistAdapter.clearSelection();
            selectallButton.setImageResource(R.drawable.select_all_ic);
            Toast.makeText(this, "All items deselected", Toast.LENGTH_SHORT).show();
        } else {

            for (int i = 0; i < checklistItems.size(); i++) {
                checklistListView.setItemChecked(i, true);
            }
            checklistAdapter.selectAll();
            selectallButton.setImageResource(R.drawable.deselect_ic);
            Toast.makeText(this, "All items selected", Toast.LENGTH_SHORT).show();
        }

        isAllSelected = !isAllSelected;
        updateActionButtonsVisibility();
    }

    private void showInitialItemsDialog(String travelType) {
        List<ChecklistItem> recommendedItems = new ArrayList<>();

        addInitialItems(
                recommendedItems,
                travelType,
                () -> {
                    View dialogView =
                            LayoutInflater.from(this)
                                    .inflate(R.layout.dialog_recommended_items, null);

                    RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));

                    RecommendationAdapter adapter = new RecommendationAdapter(recommendedItems);
                    recyclerView.setAdapter(adapter);

                    // Add "Select All" Checkbox
                    CheckBox selectAllCheckBox = dialogView.findViewById(R.id.selectAllCheckBox);
                    TextView dtype = dialogView.findViewById(R.id.destination_type);
                    dtype.setText("Destination type: " + travelType);
                    selectAllCheckBox.setOnCheckedChangeListener(
                            (buttonView, isChecked) -> {
                                adapter.selectAll(isChecked);
                            });

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                    bottomSheetDialog.setContentView(dialogView);
                    bottomSheetDialog.show();

                    Button addButton = dialogView.findViewById(R.id.addButton);
                    Button cancelButton = dialogView.findViewById(R.id.cancelButton);

                    addButton.setOnClickListener(
                            v -> {
                                SparseBooleanArray selectedItems = adapter.getSelectedItems();
                                boolean hasSelectedItems = false;

                                for (int i = 0; i < recommendedItems.size(); i++) {
                                    if (selectedItems.get(i)) {
                                        hasSelectedItems = true;

                                        ChecklistItem newItem = recommendedItems.get(i);
                                        boolean itemExists = false;

                                        // Check if the item already exists in the checklist
                                        for (ChecklistItem existingItem : checklistItems) {
                                            if (existingItem
                                                    .getName()
                                                    .equalsIgnoreCase(newItem.getName())) {
                                                // Update the existing item's weight and quantity
                                                existingItem.setWeight(
                                                        existingItem.getWeight()
                                                                + newItem.getWeight());
                                                existingItem.setQuantity(
                                                        existingItem.getQuantity()
                                                                + newItem.getQuantity());
                                                itemExists = true;
                                                break;
                                            }
                                        }

                                        // If the item does not exist, add it to the checklist
                                        if (!itemExists) {
                                            checklistItems.add(newItem);
                                        }
                                    }
                                }

                                if (hasSelectedItems) {
                                    checklistAdapter.notifyDataSetChanged();
                                    updateTotalWeight(travelMode);
                                    saveChecklistItems(destination);
                                    sharedPreferences
                                            .edit()
                                            .putBoolean(destination + "_dialog_shown", true)
                                            .apply();
                                } else {
                                    Toast.makeText(
                                                    this,
                                                    "Please select an item to add.",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                                bottomSheetDialog.dismiss();
                            });

                    cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());
                });
    }

    private void addInitialItems(
            List<ChecklistItem> items, String travelType, Runnable onItemsLoaded) {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

        dialog.show();
        String url = "https://pastebin.com/raw/Z3kq0bVq";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        response -> {
                            dialog.dismiss();
                            try {
                                JSONArray jsonArray = response.getJSONArray(travelType);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject itemObject = jsonArray.getJSONObject(i);
                                    String name = itemObject.getString("name");
                                    int weight = itemObject.optInt("weight", 0);

                                    int quantity = 1;

                                    items.add(new ChecklistItem(name, weight, quantity));
                                }

                                onItemsLoaded.run();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            dialog.dismiss();
                            Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT)
                                    .show();
                        });

        requestQueue.add(jsonObjectRequest);
    }

    private void updateActionButtonsVisibility() {
        int selectedCount = checklistAdapter.getSelectedCount();

        if (selectedCount > 0) {
            deleteButton.setVisibility(View.VISIBLE);
            bagButton.setVisibility(View.VISIBLE);
            selectallButton.setVisibility(View.VISIBLE);

            if (selectedCount == 1) {
                editButton.setVisibility(View.VISIBLE);
            } else {
                editButton.setVisibility(View.GONE);
            }
        } else {

            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            selectallButton.setVisibility(View.GONE);
            bagButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
        finish();
    }
}
