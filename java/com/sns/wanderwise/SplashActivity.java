package com.sns.wanderwise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class SplashActivity extends AppCompatActivity {

    private static final String LOGIN_PREFS = "LoginPrefs";
    private static final String LOGGED_IN_KEY = "loggedIn";
    private static final String URL_TO_LOAD = "https://www.google.com";
    private static final int DELAY_BEFORE_NAVIGATION = 1000; 

    private WebView splashWebView;
    private LinearProgressIndicator progressBar;
    private boolean isPageLoadedSuccessfully = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashWebView = findViewById(R.id.splashWebView);
        progressBar = findViewById(R.id.progress);
        progressBar.setIndeterminate(true);

        splashWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isPageLoadedSuccessfully) {
                    new Handler().postDelayed(() -> {
                        progressBar.setVisibility(View.GONE);
                        navigateToNextActivity();
                    }, DELAY_BEFORE_NAVIGATION);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                showRetryDialog("Failed to load page. Please try again later.");
                isPageLoadedSuccessfully = false;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                progressBar.setVisibility(View.GONE);
                showRetryDialog("HTTP error: " + errorResponse.getStatusCode());
                isPageLoadedSuccessfully = false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                isPageLoadedSuccessfully = true;
            }
        });

        splashWebView.setWebChromeClient(new WebChromeClient());

        if (isInternetAvailable()) {
            splashWebView.loadUrl(URL_TO_LOAD);
        } else {
            showRetryDialog("No internet connection. Please check your connection and try again.");
        }
    }

    private void navigateToNextActivity() {
        Intent intent = isUserLoggedIn() ? new Intent(SplashActivity.this, MainActivity.class) : new Intent(SplashActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
        return prefs.getBoolean(LOGGED_IN_KEY, false);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            Network[] networks = connectivityManager.getAllNetworks();
            for (Network network : networks) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void showRetryDialog(String message) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Retry", (dialog, which) -> {
                if (isInternetAvailable()) {
                    splashWebView.loadUrl(URL_TO_LOAD);
                } else {
                    showRetryDialog("No internet connection. Please check your connection and try again.");
                }
            })
            .setNegativeButton("Exit", (dialog, which) -> finishAffinity())
            .setCancelable(false)
            .show();
    }
}
