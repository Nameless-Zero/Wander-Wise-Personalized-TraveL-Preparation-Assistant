package com.sns.wanderwise.utils;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.google.android.material.dialog.MaterialAlertDialogBuilder; // Import MaterialAlertDialogBuilder
import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFetcher {

    private Context context;

    public WeatherFetcher(Context context) {
        this.context = context;
    }

    public interface WeatherFetchListener {
        void onWeatherFetched(String weather, double tempCelsius, double windSpeed, String cityName, String country, int timezone, String humidity, int pressure);
        void onError(Exception e);
    }

    public void fetchWeather(String cityName, String apiKey, WeatherFetchListener listener) {
        new FetchWeatherTask(context, cityName, apiKey, listener).execute();
    }

    private static class FetchWeatherTask extends AsyncTask<String, Void, String> {
        private Context context;
        private String cityName;
        private String apiKey;
        private WeatherFetchListener listener;
        private AlertDialog progressDialog; // Keep the type as AlertDialog
        private Exception error;

        public FetchWeatherTask(Context context, String cityName, String apiKey, WeatherFetchListener listener) {
            this.context = context;
            this.cityName = cityName;
            this.apiKey = apiKey;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            // Show custom AlertDialog as progress dialog
            progressDialog = new MaterialAlertDialogBuilder(context)
                    .setTitle("Loading")
                    .setMessage("Fetching weather data...")
                    .setCancelable(false) // Prevent dialog from being dismissed
                    .create(); // Create the AlertDialog
            progressDialog.show(); // Show the dialog
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=" + apiKey;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine);
                    }
                    in.close();
                    return result.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Dismiss the progress dialog when the task is complete
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss(); // Dismiss the AlertDialog
            }

            if (result != null && listener != null) {
                try {
                    // Parse JSON data
                    JSONObject jsonObject = new JSONObject(result);

                    // Extract data
                    String weather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempCelsius = jsonObject.getJSONObject("main").getDouble("temp");
                    double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
                    String cityName = jsonObject.getString("name");
                    String country = jsonObject.getJSONObject("sys").getString("country");
                    int timezone = jsonObject.getInt("timezone") / 3600; // Convert seconds to hours
                    String humidity = jsonObject.getJSONObject("main").getString("humidity");
                    int pressure = jsonObject.getJSONObject("main").getInt("pressure");

                    // Pass data to listener
                    listener.onWeatherFetched(weather, tempCelsius, windSpeed, cityName, country, timezone, humidity, pressure);

                } catch (JSONException e) {
                    listener.onError(e);
                }

            } else if (listener != null) {
                listener.onError(error);
            }
        }
    }
}
