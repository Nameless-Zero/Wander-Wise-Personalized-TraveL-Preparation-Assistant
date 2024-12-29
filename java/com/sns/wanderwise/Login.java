package com.sns.wanderwise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.sns.wanderwise.utils.NetworkUtils;

import androidx.appcompat.app.AlertDialog;

public class Login extends AppCompatActivity {

    private TextInputEditText username;
    private TextInputEditText password;
    private MaterialButton login;
    private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isUserLoggedIn()) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.layout_login);

        getWindow().setEnterTransition(new MaterialFadeThrough());
        getWindow().setExitTransition(new MaterialFadeThrough());
   
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);

        setupListeners();
    }

    private void setupListeners() {
        login.setOnClickListener(v ->{
            handleLogin();
        });
        signup.setOnClickListener(v ->{
            navigateToRegister();
        });
    }

    private void handleLogin() {
        final String usernameInput = username.getText().toString().trim();
        final String passwordInput = password.getText().toString().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(Login.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate the custom layout containing the ProgressIndicator
        LayoutInflater inflater = LayoutInflater.from(Login.this);
        View progressView = inflater.inflate(R.layout.progress_dialog, null);
        CircularProgressIndicator progressIndicator = progressView.findViewById(R.id.progress_indicator);

        // Create and show the dialog
        final MaterialAlertDialogBuilder progressDialog = new MaterialAlertDialogBuilder(Login.this);
        progressDialog.setTitle("Checking credentials...");
        progressDialog.setView(progressView);
        progressDialog.setCancelable(false);  // Disable cancel on touch outside
        AlertDialog dialog = progressDialog.create();
        dialog.show();

        // Network check
        NetworkUtils.checkInternetAvailability(Login.this, new NetworkUtils.InternetCheckCallback() {
            @Override
            public void onResult(boolean isAvailable) {
                dialog.dismiss();
                if (isAvailable) {
                    validateLogin(usernameInput, passwordInput);
                } else {
                    Toast.makeText(Login.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void validateLogin(String username, String password) {
        // Dummy UserManager class for validation
        if (Usermanager.validateUser(Login.this, username, password)) {
            // Save the login state
            
            String ign = Usermanager.getUserIGN(Login.this, username); 
            
            saveLoginState(true);
            saveUsername(ign, username);
            
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("IGN", ign);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Login.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }private void navigateToRegister() {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
        finish();
    }
    private void saveLoginState(boolean loggedIn) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.apply();
    }

    private void saveUsername(String ign, String username) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("ign", ign);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        return prefs.getBoolean("loggedIn", false);
    }
}
