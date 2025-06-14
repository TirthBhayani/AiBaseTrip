package com.example.aibasetrip;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class activity_trip_result extends AppCompatActivity {

    RecyclerView rvItinerary, rvHotels;
    TextView tvTotalCost;
    Button btnSavePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_result);

        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        tvTotalCost = findViewById(R.id.tvTotalCost);
        rvItinerary = findViewById(R.id.rvItinerary);
        rvHotels = findViewById(R.id.rvHotels);
        btnSavePlan = findViewById(R.id.btnSavePlan);

        btnSavePlan.setOnClickListener(v -> saveTripPlan());

        String tripJson = getIntent().getStringExtra("trip_json");
        if (tripJson == null) {
            Toast.makeText(this, "Trip data missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TripPlanResponse trip = new Gson().fromJson(tripJson, TripPlanResponse.class);
        if (trip == null) {
            Toast.makeText(this, "Parsing error: Invalid trip data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvTotalCost.setText("Total Budget: ₹" + trip.totalCost);

        rvItinerary.setLayoutManager(new LinearLayoutManager(this));
        rvItinerary.setAdapter(new ItineraryAdapter(this, trip.dayPlans));

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        rvHotels.setAdapter(new HotelAdapter(this, trip.hotels));


    }


    // Method to save the trip plan to the database
    private void saveTripPlan() {
        // Get trip data from Intent
        String tripJson = getIntent().getStringExtra("trip_json");

//         Save trip to TripDatabase
        TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);
        boolean success = dbHelper.insertTrip("Trip ", tripJson);

        if (success) {
            Toast.makeText(this, "Trip saved to DB!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error saving trip!", Toast.LENGTH_SHORT).show();
        }

        // Save trip to HistoryDB (if needed)
        HistoryDBHelper historyDBHelper = new HistoryDBHelper(this);
        boolean historySuccess = historyDBHelper.insertTrip("Trip 1", tripJson);
        if (historySuccess) {
            Toast.makeText(this, "Trip saved to history!", Toast.LENGTH_SHORT).show();
        }

        // Final message to the user
        Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show();
    }

    // Method to fetch destination image from Unsplash API

    }

