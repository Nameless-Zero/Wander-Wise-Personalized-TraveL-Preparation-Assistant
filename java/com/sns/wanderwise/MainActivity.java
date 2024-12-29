package com.sns.wanderwise;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.sns.wanderwise.LocationRecycle.LocationAdapter;
import com.sns.wanderwise.LocationRecycle.LocationFetcher;
import com.sns.wanderwise.LocationRecycle.LocationItem;
import com.sns.wanderwise.checklist.ChecklistLocation;
import com.sns.wanderwise.checklist.DialogUtils;
import com.sns.wanderwise.databinding.ActivityMainBinding;
import com.sns.wanderwise.utils.NetworkUtils;
import com.sns.wanderwise.utils.WeatherBottomSheetDialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IGN = "ign";
    private static final String KEY_SPEECH_DONE = "speech_done";
    private TextView uname;
    private MaterialButton create, saved;
    private ImageView acc, weather;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        loadUsername();
        startSpeech();

        acc.setOnClickListener(v -> showUserDialog());
        weather.setOnClickListener(v -> showWeatherDialogGenerator());
        create.setOnClickListener(v ->{
            Intent intent = new Intent(this, ChecklistLocation.class);
                    intent.putExtra("SHOW_DIALOG", true);
                    startActivity(intent);
        });
        saved.setOnClickListener(v ->{
            Intent intent = new Intent(this, ChecklistLocation.class);
                    intent.putExtra("SHOW_DIALOG", false);
                    startActivity(intent);
        });
    }

    private void initializeViews() {
        uname = binding.appname;
        acc = binding.acc;
        weather = binding.weather;
        create = binding.create;
        saved = binding.saved;
    }

    private void loadUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_IGN, getIntent().getStringExtra("IGN"));
        uname.setText("Welcome " + (username != null ? username : ""));
    }

    public void showUserDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ign = prefs.getString(KEY_IGN, getIntent().getStringExtra("IGN"));
        DialogUtils.showUserInfoDialog(this, ign, this::logout);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            Intent intent = new Intent(MainActivity.this, Login.class);
                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        },
                        5000);
    }

    private void showWeatherDialogGenerator() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_transpo, null);

        TextInputEditText et = view.findViewById(R.id.transpo);
        MaterialButton btn = view.findViewById(R.id.generate);
        et.setHint("(eg..Cebu)");
        btn.setText("View weather forecast");
        dialogBuilder.setTitle("Searh for cities");
        Dialog dialog = dialogBuilder.setView(view).create();

        btn.setOnClickListener(
                v -> {
                    String txt = et.getText().toString();

                    if (txt.isEmpty()) {
                        et.setError("cannot be empty");
                    } else {
                        WeatherBottomSheetDialogFragment bottomSheet =
                                new WeatherBottomSheetDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("location", txt);
                        bottomSheet.setArguments(args);
                        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                        dialog.dismiss();
                    }
                });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void startSpeech() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isSpeechDone = prefs.getBoolean(KEY_SPEECH_DONE, false);
        if (isSpeechDone) {
            return;
        }

        String username = prefs.getString(KEY_IGN, getIntent().getStringExtra("IGN"));
        if (username == null || username.isEmpty()) {
            username = "Guest in";
        }

        final String finalUsername = username;
        textToSpeech =
                new TextToSpeech(
                        this,
                        status -> {
                            if (status == TextToSpeech.SUCCESS) {
                                textToSpeech.setLanguage(Locale.US);
                                String message =
                                        "!, welcome! We're excited to help you plan your trip and make sure everything goes smoothly..";
                                textToSpeech.speak(
                                        "Hay " + finalUsername + message,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null);
                                prefs.edit().putBoolean(KEY_SPEECH_DONE, true).apply();
                            }
                        });
    }
}
