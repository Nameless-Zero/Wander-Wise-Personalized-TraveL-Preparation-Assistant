package com.sns.wanderwise;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.sns.wanderwise.utils.NetworkUtils;

public class Register extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        getWindow().setEnterTransition(new MaterialFadeThrough());
        getWindow().setExitTransition(new MaterialFadeThrough());
   
        usernameEditText = findViewById(R.id.registerUsername);
        emailEditText = findViewById(R.id.registerEmail);
        passwordEditText = findViewById(R.id.registerPassword);
        confirmPasswordEditText = findViewById(R.id.registerConfirmPassword);
        register = findViewById(R.id.register);

        register.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
    final String ign = usernameEditText.getText().toString().trim();
    final String username = emailEditText.getText().toString().trim();
    final String password = passwordEditText.getText().toString().trim();
    String confirmPassword = confirmPasswordEditText.getText().toString().trim();

    if (ign.isEmpty()) {
        Toast.makeText(Register.this, "Please enter a username", Toast.LENGTH_SHORT).show();
    } else if (ign.length() < 4) { // Example requirement: username must be at least 4 characters long
        Toast.makeText(Register.this, "Username must be at least 4 characters long", Toast.LENGTH_SHORT).show();
    } else if (username.isEmpty()) {
        Toast.makeText(Register.this, "Please enter an email", Toast.LENGTH_SHORT).show();
    } else if (password.isEmpty()) {
        Toast.makeText(Register.this, "Please enter a password", Toast.LENGTH_SHORT).show();
    } else if (confirmPassword.isEmpty()) {
        Toast.makeText(Register.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
    } else if (password.length() < 6) {
        Toast.makeText(Register.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
    } else if (!password.equals(confirmPassword)) {
        Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
    } else {
        showProgressDialogAndCheckNetwork(ign, username, password);
    }
}

    private void showProgressDialogAndCheckNetwork(String ign, String username, String password) {
        // Inflate custom view with CircularProgressIndicator
        LayoutInflater inflater = LayoutInflater.from(Register.this);
        View progressView = inflater.inflate(R.layout.progress_dialog, null);
        CircularProgressIndicator progressIndicator = progressView.findViewById(R.id.progress_indicator);

        // Create a MaterialAlertDialogBuilder with the progress indicator
        MaterialAlertDialogBuilder progressDialogBuilder = new MaterialAlertDialogBuilder(Register.this)
                .setTitle("Checking Information...")
                .setView(progressView)
                .setCancelable(false);

        // Show the progress dialog
        AlertDialog dialog = progressDialogBuilder.create();
        dialog.show();

        // Check for internet availability
        NetworkUtils.checkInternetAvailability(Register.this, new NetworkUtils.InternetCheckCallback() {
            @Override
            public void onResult(boolean isAvailable) {
                dialog.dismiss(); // Dismiss the progress dialog

                if (isAvailable) {
                    // Save user data and navigate to Login activity
                    Usermanager.registerUser(Register.this, ign, username, password);
                    startActivity(new Intent(Register.this, Login.class));
                    Toast.makeText(Register.this, "Registration Complete", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(Register.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
