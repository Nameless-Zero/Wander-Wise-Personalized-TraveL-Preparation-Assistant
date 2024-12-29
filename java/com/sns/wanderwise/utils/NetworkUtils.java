package com.sns.wanderwise.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

public class NetworkUtils {

    public interface InternetCheckCallback {
        void onResult(boolean isAvailable);
    }

    public static void checkInternetAvailability(final Context context, final InternetCheckCallback callback) {
        
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager connectivityManager = 
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    boolean isAvailable = false;
                    if (connectivityManager != null) {
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    }
                    
                    if (callback != null) {
                        callback.onResult(isAvailable);
                    }
                }
            }, 3000); // 5 seconds delay
    }
}

