package com.sns.wanderwise;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class Transportation extends AppCompatActivity {

    // UI Components
    private WebView webView;
    private LinearProgressIndicator progressIndicator;
    private TextInputEditText currentLocationEditText;
    private Button nextButton;
    private ImageView backButton;

    // Location variables
    private String fromLocation, toLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_transportation);

        // Initialize UI components
        initializeUI();

        // Retrieve intent extras
        retrieveIntentData();

        // Setup WebView
        setupWebView();

        // Setup listeners
        setupListeners();
    }

    // Initialize UI components
    private void initializeUI() {
        webView = findViewById(R.id.webView);
        progressIndicator = findViewById(R.id.progress_linear);
        currentLocationEditText = findViewById(R.id.current);
        nextButton = findViewById(R.id.next);
        backButton = findViewById(R.id.back);
    }

    // Retrieve intent data
    private void retrieveIntentData() {
        Intent intent = getIntent();
        toLocation = intent.getStringExtra("toLocation");
        fromLocation = currentLocationEditText.getText().toString();
    }

    // Setup WebView settings and client
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebViewClient());
    }

    // Setup listeners for user interactions
    private void setupListeners() {
        nextButton.setOnClickListener(v -> {
            fromLocation = currentLocationEditText.getText().toString();
            String destination = getIntent().getStringExtra("toLocation");
            loadUrl(fromLocation, destination);
            progressIndicator.setIndeterminate(true);
        });
        
        backButton.setOnClickListener(v ->{
            finish();
        });

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // Load the URL in WebView based on the provided locations
    private void loadUrl(String locationFrom, String locationTo) {
        if (isValidLocation(locationFrom) && isValidLocation(locationTo)) {
            String url = "https://www.rome2rio.com/map/" + locationFrom + "/" + locationTo + "#trips";
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        } else {
            Toast.makeText(this, "Invalid locations provided", Toast.LENGTH_SHORT).show();
        }
    }

    // Validate if a location is not null or empty
    private boolean isValidLocation(String location) {
        return location != null && !location.trim().isEmpty();
    }

    // Show or hide the progress indicator
    private void showProgressIndicator(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // Custom WebViewClient for handling page load events
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showProgressIndicator(true);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            showProgressIndicator(false);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            showProgressIndicator(false);
            Toast.makeText(Transportation.this, "Failed to load page", Toast.LENGTH_SHORT).show();
            super.onReceivedError(view, request, error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }

    // Handle back press to navigate WebView history or finish activity
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
