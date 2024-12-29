package com.sns.wanderwise;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sns.wanderwise.checklist.ChecklistLocation;
import com.sns.wanderwise.utils.CustomWebViewClient;
import com.sns.wanderwise.utils.CustomWebViewClient2;
import com.sns.wanderwise.utils.WeatherBottomSheetDialogFragment;

public class WeatherActivity extends AppCompatActivity {

    private TextView location;
    private TextView locationDescription;
    private TextView modeTransportation;
    private WebView webView;
    private ImageView back;
    private MaterialButton weather;
    private LinearProgressIndicator progressIndicator;
    private AlertDialog alertDialog;
    private ImageView backDrop;
    private TextView textAnim;

    private Intent intent;
    private String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_more_webview);

        intent = getIntent();
        destination = intent.getStringExtra("key");

        initializeViews();
        setupWebView();

        location.setText("Weather forecast in "+destination);

        back.setOnClickListener(
                v -> {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                });
    }

    private void initializeViews() {
        location = findViewById(R.id.location);
        webView = findViewById(R.id.morewebView);
        modeTransportation = findViewById(R.id.modeTrasnportation);
        back = findViewById(R.id.back);
        weather = findViewById(R.id.weather);
        backDrop = findViewById(R.id.backdrop);
        progressIndicator = findViewById(R.id.progress_linear);
        progressIndicator.setIndeterminate(true);

        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(textAnim, "alpha", 0f, 1f);
        fadeAnimator.setDuration(1000);
        fadeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        fadeAnimator.setRepeatMode(ValueAnimator.REVERSE);

        fadeAnimator.start();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new CustomWebViewClient2(this));

        // Show progress indicator while loading
        webView.setWebViewClient(
                new CustomWebViewClient2(this) {
                    @Override
                    public void onPageStarted(
                            WebView view, String url, android.graphics.Bitmap favicon) {
                        showProgressIndicator(true);
                        // trap.setVisibility(View.GONE);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        showProgressIndicator(false);
                        // trap.setVisibility(View.VISIBLE);
                    }
                });
        webView.loadUrl(
                "https://www.accuweather.com/en/search-locations?query="+destination);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showProgressIndicator(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
