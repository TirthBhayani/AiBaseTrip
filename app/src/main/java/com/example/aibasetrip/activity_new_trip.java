package com.example.aibasetrip;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.*;

public class activity_new_trip extends AppCompatActivity {

    EditText etDestination, etDays, etBudget;
    Spinner spinnerTravellingWith;
    CheckBox cbNature, cbAdventure, cbRelaxation, cbCulture, cbFamilyActivity;
    Button btnCreateTrip;
    ProgressDialog progressDialog;

    final String DEEPSEEK_API_KEY = "sk-c2b99f60238540e284b6b8d97592e6f4"; // replace with your actual key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        etDestination = findViewById(R.id.etDestination);
        etDays = findViewById(R.id.etDays);
        etBudget = findViewById(R.id.etBudget);
        spinnerTravellingWith = findViewById(R.id.spinnerTravellingWith);

        cbNature = findViewById(R.id.cbNature);
        cbAdventure = findViewById(R.id.cbAdventure);
        cbRelaxation = findViewById(R.id.cbRelaxation);
        cbCulture = findViewById(R.id.cbCulture);
        cbFamilyActivity = findViewById(R.id.cbFamilyActivity);

        btnCreateTrip = findViewById(R.id.btnCreateTrip);

        btnCreateTrip.setOnClickListener(v -> generateTrip());
//        btnCreateTrip.setOnClickListener(v -> {
//            String dummyJson = "{ \"dayPlans\": [ " +
//                    "{ \"day\": 1, \"date\": \"April 12, 2025\", \"activities\": [" +
//                    "{ \"time\": \"09:00 AM\", \"title\": \"Visit Central Park\", \"description\": \"Explore the park and enjoy morning nature walk.\", \"location\": \"Central Park\", \"cost\": 100 }," +
//                    "{ \"time\": \"02:00 PM\", \"title\": \"Museum Visit\", \"description\": \"Explore art and history exhibits.\", \"location\": \"City Museum\", \"cost\": 200 }" +
//                    "] }," +
//                    "{ \"day\": 2, \"date\": \"April 13, 2025\", \"activities\": [" +
//                    "{ \"time\": \"10:00 AM\", \"title\": \"Boat Ride\", \"description\": \"Relaxing boat ride on the river.\", \"location\": \"Riverfront\", \"cost\": 150 }," +
//                    "{ \"time\": \"04:00 PM\", \"title\": \"Shopping\", \"description\": \"Local market visit for souvenirs and clothes.\", \"location\": \"Old Bazaar\", \"cost\": 300 }" +
//                    "] }," +
//                    "{ \"day\": 3, \"date\": \"April 14, 2025\", \"activities\": [" +
//                    "{ \"time\": \"08:00 AM\", \"title\": \"Hiking\", \"description\": \"Morning hike through scenic trails.\", \"location\": \"Hill Valley\", \"cost\": 250 }," +
//                    "{ \"time\": \"01:00 PM\", \"title\": \"Local Cuisine Lunch\", \"description\": \"Try traditional dishes at a famous restaurant.\", \"location\": \"Taste of City\", \"cost\": 400 }" +
//                    "] }" +
//                    "], \"hotels\": [" +
//                    "{ \"name\": \"Hotel Paradise\", \"description\": \"Cozy stay near beach with breakfast\", \"price\": 1500, \"rating\": 4.5, \"image\": \"\", \"location\": \"Beach Road\" }," +
//                    "{ \"name\": \"City Inn\", \"description\": \"Budget hotel with free Wi-Fi\", \"price\": 1200, \"rating\": 4.2, \"image\": \"\", \"location\": \"City Center\" }," +
//                    "{ \"name\": \"Luxury Heights\", \"description\": \"Premium hotel with spa and pool\", \"price\": 3200, \"rating\": 4.8, \"image\": \"\", \"location\": \"Skyline Avenue\" }," +
//                    "{ \"name\": \"Green Retreat\", \"description\": \"Eco-friendly resort surrounded by nature\", \"price\": 1800, \"rating\": 4.6, \"image\": \"\", \"location\": \"Forest Edge\" }" +
//                    "], \"totalCost\": 9500 }";
//
//
//            Intent intent = new Intent(activity_new_trip.this, activity_trip_result.class);
//            intent.putExtra("trip_json", dummyJson);
//            startActivity(intent);
//        });

    }

    private void generateTrip() {
        String destination = etDestination.getText().toString().trim();
        String days = etDays.getText().toString().trim();
        String budget = etBudget.getText().toString().trim();
        String travellingWith = spinnerTravellingWith.getSelectedItem().toString();

        StringBuilder interests = new StringBuilder();
        if (cbNature.isChecked()) interests.append("Nature, ");
        if (cbAdventure.isChecked()) interests.append("Adventure, ");
        if (cbRelaxation.isChecked()) interests.append("Relaxation, ");
        if (cbCulture.isChecked()) interests.append("Culture, ");
        if (cbFamilyActivity.isChecked()) interests.append("Family-friendly Activities, ");
        if (interests.length() > 0) interests.setLength(interests.length() - 2); // remove trailing comma

        String prompt = "Create a " + days + "-day travel itinerary for a trip to " + destination +
                " under ₹" + budget + ", travelling with " + travellingWith +
                ". Interests: " + interests + ". Return JSON only in this format: " +
                "{ \"dayPlans\": [ {\"day\": 1, \"date\": \"April 12, 2025\", \"activities\": [{\"time\": \"09:00 AM\", \"title\": \"Visit Park\", \"description\": \"Explore nature\", \"location\": \"Central Park\", \"cost\": 100}] } ]," +
                "\"hotels\": [{\"name\": \"Hotel Paradise\", \"description\": \"Cozy stay\", \"price\": 1500, \"rating\": 4.5, \"image\": \"\", \"location\": \"Beach Road\"}], \"totalCost\": 5000 }";

        progressDialog = new ProgressDialog(activity_new_trip.this);
        progressDialog.setMessage("Generating your itinerary...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Increase timeout
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // JSON body
        String json = "{\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"messages\": [\n" +
                "    { \"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\" }\n" +
                "  ],\n" +
                "  \"temperature\": 0.5,\n" +
                "  \"max_tokens\": 1500\n" +
                "}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("API_RESPONSE", responseBody);

                String tripData = "Could not parse itinerary.";
                if (response.isSuccessful()) {
                    try {
                        // Extract "content" value manually
                        JsonObject obj = new Gson().fromJson(responseBody, JsonObject.class);
                        String content = obj.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("message")
                                .get("content").getAsString();

                        // Clean escape characters
                        content = content.replace("\\n", "\n").replace("\\\"", "\"");

                        int start = content.indexOf("{");
                        int end = content.lastIndexOf("}");
                        if (start != -1 && end != -1) {
                            tripData = content.substring(start, end + 1);
                        } else {
                            tripData = content;
                        }
                    } catch (Exception e) {
                        tripData = "Failed to parse itinerary: " + e.getMessage();
                    }
                } else {
                    tripData = "API Error: " + response.code();
                }

                String finalTrip = tripData;

                runOnUiThread(() -> {
                    progressDialog.dismiss();
//                    showTripDialog(finalTrip);
                    Intent intent = new Intent(activity_new_trip.this, activity_trip_result.class);
                    intent.putExtra("trip_json", finalTrip);
                    startActivity(intent);

                });
            }
        });
    }


    private void showTripDialog(String tripText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AI Trip Itinerary");
        builder.setMessage(tripText);

        builder.setPositiveButton("Copy", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Trip Plan", tripText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Itinerary copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }
}
