package com.sns.wanderwise.checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sns.wanderwise.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class ChecklistLocation extends AppCompatActivity {

    public static final String PREFS_NAME = "ChecklistPrefs";
    private SharedPreferences sharedPreferences;
    private ListView destinationListView;
    private FloatingActionButton fab;
    private List<DestinationItem> destinations;
    private DestinationAdapter destinationAdapter;
    private ImageView back;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        setContentView(R.layout.layout_checklist_location);

        destinationListView = findViewById(R.id.list_view);
        fab = findViewById(R.id.fab);
        back = findViewById(R.id.back);
        emptyView = findViewById(R.id.empty_view);
        destinationListView.setEmptyView(emptyView);

        initializeDestinations();
        setupDestinationListView();
        setupFabListener();

        boolean showDialog = getIntent().getBooleanExtra("SHOW_DIALOG", false);
        if (showDialog) {
            showAddDestinationDialog();
        }
    }

    private void initializeDestinations() {
        destinations = new ArrayList<>();
        loadDestinations();
        destinationAdapter = new DestinationAdapter(this, destinations);
        destinationListView.setAdapter(destinationAdapter);
    }

    private void setupDestinationListView() {
        destinationListView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    DestinationItem selectedDestination = destinations.get(position);
                    showChecklistActivity(
                            selectedDestination.getName(),
                            selectedDestination.getTravelType(),
                            selectedDestination.getTravelMode());
                });

        destinationListView.setOnItemLongClickListener(
                (parent, view, position, id) -> {
                    showDeleteConfirmationDialog(position);
                    return true;
                });
    }

    private void setupFabListener() {
        fab.setOnClickListener(view -> showAddDestinationDialog());
        back.setOnClickListener(v -> finish());
    }

    private void showChecklistActivity(String current, String travelType, String travelMode) {
        Intent intent = new Intent(this, checklist.class);
        intent.putExtra("current", current);
        intent.putExtra("travel_type", travelType);
        intent.putExtra("travel_mode", travelMode);

        if ("Air transportation".equals(travelMode)) {
            intent.putExtra("show_airline_dialog", true);
        } else {
            intent.putExtra("show_airline_dialog", false);
        }

        startActivity(intent);
    }

    private void showAddDestinationDialog() {
        View dialogView =
                LayoutInflater.from(this).inflate(R.layout.dialog_add_destination, null, false);
        TextInputLayout inputLayoutCurrent = dialogView.findViewById(R.id.textInputLayoutcurrent);
        AutoCompleteTextView currentLocation = dialogView.findViewById(R.id.current_location_input);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_destination);
        RadioGroup radioGroupModes = dialogView.findViewById(R.id.radio_group_mode);
        MaterialButton addButton = dialogView.findViewById(R.id.add);
        ImageView backButton = dialogView.findViewById(R.id.back);
        LinearProgressIndicator progrees = dialogView.findViewById(R.id.progress_linear);
        progrees.setIndeterminate(true);

        inputLayoutCurrent.setHint("Where are you going?");
        fetchCountries(currentLocation, progrees); // Fetch countries and set suggestions

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

        addButton.setOnClickListener(
                v -> {
                    String current =
                            currentLocation.getText() != null
                                    ? currentLocation.getText().toString().trim()
                                    : "";

                    if (validateInputs(current, inputLayoutCurrent, radioGroup, radioGroupModes)) {
                        MaterialRadioButton selectedTravelTypeButton =
                                dialogView.findViewById(radioGroup.getCheckedRadioButtonId());
                        String selectedTravelType = selectedTravelTypeButton.getText().toString();

                        MaterialRadioButton selectedModeButton =
                                dialogView.findViewById(radioGroupModes.getCheckedRadioButtonId());
                        String selectedMode = selectedModeButton.getText().toString();
                        addDestination(current, selectedTravelType, selectedMode);
                        dialog.dismiss();
                    }
                });

        backButton.setOnClickListener(v -> finish());
        dialog.show();
    }

    private void fetchCountries(
            AutoCompleteTextView currentLocation, LinearProgressIndicator progress) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://countriesnow.space/api/v0.1/countries";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("FETCH_ERROR", "Failed to fetch countries", e);
                                runOnUiThread(
                                        () ->
                                                Toast.makeText(
                                                                ChecklistLocation.this,
                                                                "Failed to fetch data",
                                                                Toast.LENGTH_SHORT)
                                                        .show());
                            }

                            @Override
                            public void onResponse(Call call, Response response)
                                    throws IOException {
                                if (response.isSuccessful()) {
                                    try {
                                        String responseBody = response.body().string();
                                        JSONObject jsonObject = new JSONObject(responseBody);
                                        JSONArray countriesArray = jsonObject.getJSONArray("data");
                                        List<String> suggestions = new ArrayList<>();

                                        for (int i = 0; i < countriesArray.length(); i++) {
                                            JSONObject country = countriesArray.getJSONObject(i);

                                            String countryName = country.getString("country");
                                            suggestions.add(
                                                    countryName); // Add country to the suggestions
                                            // list

                                            // Extract cities array, if present
                                            if (country.has("cities")) {
                                                JSONArray cities = country.getJSONArray("cities");
                                                for (int j = 0; j < cities.length(); j++) {
                                                    String cityName =
                                                            cities.getString(
                                                                    j); // Extract city name
                                                    suggestions.add(cityName); // Add city to the
                                                    // suggestions list
                                                }
                                            }
                                        }

                                        // Update the AutoCompleteTextView with the combined list of
                                        // countries and cities
                                        runOnUiThread(
                                                () -> {
                                                    progress.setVisibility(View.GONE);
                                                    ArrayAdapter<String> adapter =
                                                            new ArrayAdapter<>(
                                                                    ChecklistLocation.this,
                                                                    android.R.layout
                                                                            .simple_dropdown_item_1line,
                                                                    suggestions);
                                                    currentLocation.setAdapter(adapter);
                                                    currentLocation.setThreshold(
                                                            1); // Show suggestions after typing 1
                                                    // character
                                                });

                                    } catch (JSONException e) {
                                        Log.e("JSON_ERROR", "Error parsing JSON", e);
                                        runOnUiThread(
                                                () ->
                                                        Toast.makeText(
                                                                        ChecklistLocation.this,
                                                                        "Error parsing data",
                                                                        Toast.LENGTH_SHORT)
                                                                .show());
                                    }
                                } else {
                                    Log.e("RESPONSE_ERROR", "Response Code: " + response.code());
                                }
                            }
                        });
    }

    private boolean validateInputs(
            String inputText,
            TextInputLayout inputLayout,
            RadioGroup radioGroupTravelType,
            RadioGroup radioGroupModes) {

        if (TextUtils.isEmpty(inputText)) {
            inputLayout.setError("Field cannot be empty");
            return false;
        }
        inputLayout.setError(null);

        if (radioGroupTravelType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a travel type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (radioGroupModes.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a mode of travel", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addDestination(String currentLocation, String travelType, String travelMode) {
        
        for (DestinationItem destination : destinations) {
            if (destination.getName().equalsIgnoreCase(currentLocation)) {
                Toast.makeText(this, "Destination already exists!", Toast.LENGTH_SHORT).show();
                return; 
            }
        }

        // Add the new destination if it doesn't already exist
        destinations.add(new DestinationItem(currentLocation, travelType, travelMode));
        destinationAdapter.notifyDataSetChanged();
        saveDestinations();
        showChecklistActivity(currentLocation, travelType, travelMode);
    }

    private void showDeleteConfirmationDialog(int position) {
        DestinationItem deletedDestination = destinations.get(position);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Destination")
                .setMessage(
                        "Are you sure you want to delete \"" + deletedDestination.getName() + "\"?")
                .setPositiveButton(
                        "Yes",
                        (dialog, which) -> {
                            removeDestinationFromPreferences(position);
                            destinationAdapter.notifyDataSetChanged();
                            Snackbar.make(
                                            destinationListView,
                                            "Destination deleted",
                                            Snackbar.LENGTH_LONG)
                                    .setAction(
                                            "Undo",
                                            v -> {
                                                destinations.add(position, deletedDestination);
                                                saveDestinations();
                                                destinationAdapter.notifyDataSetChanged();
                                            })
                                    .show();
                        })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeDestinationFromPreferences(int position) {
        if (position >= 0 && position < destinations.size()) {
            DestinationItem destinationToDelete = destinations.get(position);
            String destinationName = destinationToDelete.getName();

            // Delete all data associated with this destination
            deleteAllDataForDestination(destinationName);

            // Remove the destination from the list
            destinations.remove(position);

            // Save the updated list of destinations
            saveDestinations();

            Log.d("ChecklistLocation", "Destination and its data removed: " + destinationName);
        } else {
            Log.e("ChecklistLocation", "Invalid position for deletion: " + position);
        }
    }

    private void deleteAllDataForDestination(String destination) {
        // Remove checklist items from SharedPreferences
        SharedPreferences checklistPrefs = getSharedPreferences("ChecklistPrefs", MODE_PRIVATE);
        SharedPreferences.Editor checklistEditor = checklistPrefs.edit();

        int size = checklistPrefs.getInt(destination + "_size", 0);
        if (size > 0) {
            checklistEditor.remove(destination + "_size");
            for (int i = 0; i < size; i++) {
                checklistEditor.remove(destination + "_item_" + i + "_name");
                checklistEditor.remove(destination + "_item_" + i + "_weight");
                checklistEditor.remove(destination + "_item_" + i + "_quantity");
            }
            checklistEditor.apply();
            Log.d("DeleteDebug", "Checklist items for " + destination + " deleted.");
        } else {
            Log.d("DeleteDebug", "No checklist items found for " + destination);
        }

        // Remove saved items from SharedPreferences
        SharedPreferences savedItemsPrefs = getSharedPreferences("SavedItems", MODE_PRIVATE);
        SharedPreferences.Editor savedItemsEditor = savedItemsPrefs.edit();

        String savedItemsKey = "selectedItems_" + destination;
        if (savedItemsPrefs.contains(savedItemsKey)) {
            savedItemsEditor.remove(savedItemsKey);
            savedItemsEditor.apply();
            Log.d("DeleteDebug", "Saved items for destination " + destination + " deleted.");
        } else {
            Log.d("DeleteDebug", "No saved items found for " + destination);
        }
    }

    private void loadDestinations() {
        int count = sharedPreferences.getInt("destination_count", 0);
        destinations.clear();

        // Deserialize the saved destinations from SharedPreferences using Gson
        String destinationsJson = sharedPreferences.getString("destinations", "[]");
        Gson gson = new Gson();
        List<DestinationItem> loadedDestinations =
                gson.fromJson(
                        destinationsJson, new TypeToken<List<DestinationItem>>() {}.getType());

        if (loadedDestinations != null) {
            destinations.addAll(loadedDestinations);
        }

        Log.d("ChecklistLocation", "Total Destinations Loaded: " + destinations.size());
    }

    private void saveDestinations() {
        Gson gson = new Gson();
        String destinationsJson = gson.toJson(destinations);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("destinations", destinationsJson);
        editor.putInt("destination_count", destinations.size());
        editor.apply();

        Log.d("ChecklistLocation", "Destinations Saved. Total: " + destinations.size());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
