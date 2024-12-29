package com.sns.wanderwise.utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.sns.wanderwise.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private MaterialTextView weatherTextView;
    private MaterialTextView tempTextView;
    private MaterialTextView windTextView;
    private MaterialTextView cityNameTextView;
    private MaterialTextView humidityTextView;
    private MaterialTextView pressureTextView;
    private MaterialTextView dateTextView;
    private MaterialTextView weatherInfo;
    private ShapeableImageView weatherIndicatorImageView;
    private WeatherService weatherService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_weather, container, false);
        initializeViews(view);
        setCurrentDateTime();
        weatherService = new WeatherService(requireContext());

        // Fetch weather for the passed location or default to "Cebu City"
        String passedLocation = getPassedLocation();
        fetchWeather(TextUtils.isEmpty(passedLocation) ? "Cebu City" : passedLocation);

        return view;
    }

    private void initializeViews(View view) {
        weatherTextView = view.findViewById(R.id.weatherTextView);
        weatherInfo = view.findViewById(R.id.weatherinfo);
        tempTextView = view.findViewById(R.id.tempTextView);
        windTextView = view.findViewById(R.id.windTextView);
        cityNameTextView = view.findViewById(R.id.cityNameTextView);
        pressureTextView = view.findViewById(R.id.weatherinfoPressure);
        humidityTextView = view.findViewById(R.id.humidityTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        weatherIndicatorImageView = view.findViewById(R.id.tempIndicatorImageView);
    }

    private void setCurrentDateTime() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        dateTextView.setText(dateFormat.format(now) + " " + timeFormat.format(now));
    }

    private String getPassedLocation() {
        Bundle arguments = getArguments();
        return arguments != null ? arguments.getString("location") : null;
    }

    private void fetchWeather(String cityName) {
        weatherService.fetchWeatherForCity(cityName, new WeatherFetcher.WeatherFetchListener() {
            @Override
            public void onWeatherFetched(String weather, double tempCelsius, double windSpeed, String cityName, String country, int timezone, String humidity, int pressure) {
                updateUI(weather, tempCelsius, windSpeed, cityName, country, timezone, humidity, pressure);
            }

            @Override
            public void onError(Exception e) {
                handleFetchError(e);
            }
        });
    }

    private void updateUI(String weather, double tempCelsius, double windSpeed, String cityName, String country, int timezone, String humidity, int pressure) {
        weatherTextView.setText(weather);
        weatherInfo.setText("Temperature: "+String.format("%.2f°C", tempCelsius));
        tempTextView.setText(String.format("%.2f°C", tempCelsius));
        windTextView.setText("Wind: "+windSpeed + " m/s");
        cityNameTextView.setText(cityName + " - " + country);
        humidityTextView.setText("Humidity: "+humidity + "%");
        pressureTextView.setText("Pressure: "+pressure + " hPa");
        updateWeatherIndicator(weather);
    }

   private void handleFetchError(Exception e) {
    e.printStackTrace();
    dismiss();
    dateTextView.setVisibility(View.GONE);
    
    // Ensure we have a valid context before showing the dialog
    if (isAdded()) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage("There was an error fetching the weather data. " +
                        "This could be due to a network issue or an invalid location. " +
                        "Would you like to close this dialog or try fetching the weather again?")
            .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
            .setNegativeButton("Retry", (dialog, which) -> fetchWeather(
                TextUtils.isEmpty(getPassedLocation()) ? "Cebu City" : getPassedLocation()))
            .setCancelable(false)
            .show();
    }
}



    private void updateWeatherIndicator(String weatherDescription) {
        if (weatherDescription.contains("clear") || weatherDescription.contains("sunny")) {
            weatherIndicatorImageView.setImageResource(R.drawable.sunny_ic);
        } else if (weatherDescription.contains("cloud") || weatherDescription.contains("overcast")) {
            weatherIndicatorImageView.setImageResource(R.drawable.cloudy_ic);
        } else if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle") || weatherDescription.contains("storm")) {
            weatherIndicatorImageView.setImageResource(R.drawable.rainy_ic);
        } else {
            weatherIndicatorImageView.setImageResource(R.drawable.sunny_ic);
        }
    }
}
