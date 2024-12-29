package com.sns.wanderwise.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {
    private Context context;
    private static final String PASTEBIN_URL = "https://pastebin.com/raw/sNsBVFkL"; 
    
    public WeatherService(Context context) {
        this.context = context;
    }
    public void fetchWeatherForCity(final String cityName, final WeatherFetcher.WeatherFetchListener listener) {
        new FetchApiKeyTask(cityName, listener).execute(PASTEBIN_URL);
    }
    private class FetchApiKeyTask extends AsyncTask<String, Void, String> {

        private String cityName;
        private WeatherFetcher.WeatherFetchListener listener;

        public FetchApiKeyTask(String cityName, WeatherFetcher.WeatherFetchListener listener) {
            this.cityName = cityName;
            this.listener = listener;
        }
         @Override
        protected String doInBackground(String... urls) {
            String apiKey = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                connection.disconnect();

                // Parse the JSON response to get the API key
                JSONObject jsonObject = new JSONObject(result.toString());
                apiKey = jsonObject.getString("apiKey");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return apiKey;
        }
         @Override
        protected void onPostExecute(String apiKey) {
            if (apiKey != null) {
                // Now fetch the weather data using the retrieved API key
                WeatherFetcher weatherFetcher = new WeatherFetcher(context);
                weatherFetcher.fetchWeather(cityName, apiKey, new WeatherFetcher.WeatherFetchListener() {
                        @Override
                        public void onWeatherFetched(String weather, double tempCelsius, double windSpeed, String cityName, String country, int timezone, String humidity, int pressure) {
                            listener.onWeatherFetched(weather, tempCelsius, windSpeed, cityName, country, timezone, humidity, pressure);
                        }

                        @Override
                        public void onError(Exception e) {
                            listener.onError(e);
                        }
                    });
            } else {
                // Handle the case where the API key could not be retrieved
                Toast.makeText(context, "Failed to fetch API key. Please try again.", Toast.LENGTH_LONG).show();
                Log.e("WeatherService", "Failed to fetch API key.");
            }
        }
    }
}
