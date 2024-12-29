package com.sns.wanderwise.utils;

import android.graphics.Bitmap;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.sns.wanderwise.MoreWebview;

public class CustomWebViewClient extends WebViewClient {

    private final MoreWebview activity;
    
    // Constructor accepting the activity
    public CustomWebViewClient(MoreWebview activity) {
        this.activity = activity;
    }

    // Show progress indicator when the page starts loading
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        activity.showProgressIndicator(true);
        super.onPageStarted(view, url, favicon);
    }

    // Hide progress indicator when the page finishes loading
    @Override
    public void onPageFinished(WebView view, String url) {
        activity.showProgressIndicator(false);
        super.onPageFinished(view, url);
    }

    // Handle loading errors
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        activity.showProgressIndicator(false);
       // Toast.makeText(activity, "Failed to load page: " + error.getDescription(), Toast.LENGTH_SHORT).show();
        super.onReceivedError(view, request, error);
    }

    // Optionally, handle URL redirections within the WebView instead of opening in a browser
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true; // Indicating that we are handling the URL loading within the WebView
    }
}
