package com.example.aibasetrip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Dashboard extends AppCompatActivity {

    Button btnNewPlan, viewTripButton, btnHistory,btnGoals;
    TextView smartTripInfo;
    SwipeRefreshLayout swipeRefreshLayout;
    TripDatabaseHelper dbHelper;
    TextView welcometext;
    // Shared DB helper instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Set status bar color for modern look
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);

// Change overflow icon color to white
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        // Initialize UI components
        swipeRefreshLayout = findViewById(R.id.refresh);


        btnNewPlan = findViewById(R.id.btnNewPlan);
        btnHistory = findViewById(R.id.btnHistory);
        viewTripButton = findViewById(R.id.viewTripButton);
        smartTripInfo = findViewById(R.id.smartTripInfo);
        welcometext = findViewById(R.id.welcomeText);
        btnGoals = findViewById(R.id.btnGoals);
        dbHelper = new TripDatabaseHelper(this);

        // Swipe to refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            refreshTripInfo();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Load trip data on start
        refreshTripInfo();

        // Animate Smart Trip card
        CardView smartTripCard = findViewById(R.id.smartTripCard);
        smartTripCard.setAlpha(0f);
        smartTripCard.animate().alpha(1f).setDuration(800).start();

        // Button listeners
        btnNewPlan.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, activity_new_trip.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, History.class);
            startActivity(intent);
        });

        btnGoals.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this,TripChecklistActivity.class);
            startActivity(intent);
        });


        viewTripButton.setOnClickListener(v -> shareTripDetails());

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = prefs.getString("username", "Guest"); // "Guest" is default if not found
        welcometext.setText( "Welcome " +name+"  👋");

        Button exportPdfButton = findViewById(R.id.exportPdfButton);
        exportPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to generate PDF
                generateTripPDF(); // This method should generate the PDF
            }
        });
    }
    public void generateTripPDF() {
        // Create a new PDF document
        PdfDocument pdfDocument = new PdfDocument();

        // Set up a page info (A4 size)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Get the canvas to draw on the page
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Set text properties
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);

        int startY = 50; // Starting position for drawing text on the canvas

        Cursor cursor = dbHelper.getAllTrips();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Retrieve trip data in JSON format
                    String json = cursor.getString(cursor.getColumnIndexOrThrow("trip_json"));

                    if (json == null || json.trim().isEmpty()) {
                        Toast.makeText(this, "Trip data is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject obj = new JSONObject(json);
                    JSONArray dayPlans = obj.getJSONArray("dayPlans");
                    JSONArray hotels = obj.getJSONArray("hotels");
                    int totalCost = obj.getInt("totalCost");

                    // Trip Title
                    canvas.drawText("Trip Plan", 100, startY, paint);
                    startY += 30;

                    // Day-wise activities
                    for (int i = 0; i < dayPlans.length(); i++) {
                        JSONObject dayPlan = dayPlans.getJSONObject(i);
                        String day = dayPlan.getString("date");
                        canvas.drawText("Day " + (i + 1) + ": " + day, 100, startY, paint);
                        startY += 20;

                        JSONArray activities = dayPlan.getJSONArray("activities");
                        for (int j = 0; j < activities.length(); j++) {
                            JSONObject activity = activities.getJSONObject(j);
                            canvas.drawText("  - " + activity.getString("title") + " (" + activity.getString("time") + ")", 120, startY, paint);
                            startY += 20;
                            canvas.drawText("    Location: " + activity.getString("location"), 140, startY, paint);
                            startY += 20;
                            canvas.drawText("    Description: " + activity.getString("description"), 140, startY, paint);
                            startY += 20;
                            canvas.drawText("    Cost: ₹" + activity.getInt("cost"), 140, startY, paint);
                            startY += 30; // Adding space after each activity
                        }
                    }

                    // Hotel details
                    canvas.drawText("Hotels:", 100, startY, paint);
                    startY += 20;
                    for (int i = 0; i < hotels.length(); i++) {
                        JSONObject hotel = hotels.getJSONObject(i);
                        canvas.drawText("  - " + hotel.getString("name"), 120, startY, paint);
                        startY += 20;
                        canvas.drawText("    Location: " + hotel.getString("location"), 140, startY, paint);
                        startY += 20;
                        canvas.drawText("    Description: " + hotel.getString("description"), 140, startY, paint);
                        startY += 20;
                        canvas.drawText("    Price: ₹" + hotel.getInt("price"), 140, startY, paint);
                        startY += 20;
                        canvas.drawText("    Rating: " + hotel.getDouble("rating") + " stars", 140, startY, paint);
                        startY += 30; // Adding space after each hotel
                    }

                    // Total cost
                    canvas.drawText("Total Trip Cost: ₹" + totalCost, 100, startY, paint);

                    // End of content
                    startY += 40;

                    // Finish the page
                    pdfDocument.finishPage(page);

                    // Save the PDF to a file
                    try {
                        // Set the output file path
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "trip_details.pdf");

                        // Create file output stream
                        FileOutputStream outputStream = new FileOutputStream(file);

                        // Write the PDF document to the output stream
                        pdfDocument.writeTo(outputStream);
                        outputStream.close();

                        // Show a success message
                        Toast.makeText(this, "PDF saved to Documents folder", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No trip found to generate PDF.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (JSONException e) {
                Log.e("TripJSON", "Error parsing trip data: " + e.getMessage());
                Toast.makeText(this, "Error parsing trip data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "No trip found to generate PDF.", Toast.LENGTH_SHORT).show();
        }

        // Close the PDF document
        pdfDocument.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.menu_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        else if (id == R.id.menu_more) {
            showDialogMenu();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void refreshTripInfo() {
        Cursor cursor = dbHelper.getAllTrips();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String json = cursor.getString(cursor.getColumnIndexOrThrow("trip_json"));
                    JSONObject obj = new JSONObject(json);
                    JSONArray hotels = obj.getJSONArray("hotels");
                    String location = hotels.getJSONObject(0).getString("location");
                    int cost = obj.getInt("totalCost");

                    smartTripInfo.setText("Suggested: " + location + ", ₹" + cost);
                } else {
                    smartTripInfo.setText("No saved trip found.");
                }
            } catch (JSONException e) {
                smartTripInfo.setText("Error reading saved trip.");
                Toast.makeText(this, "Could not parse saved trip", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else {
            smartTripInfo.setText("No saved trip found.");
        }
    }

    private void shareTripDetails() {
        Cursor cursor = dbHelper.getAllTrips();

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String json = cursor.getString(cursor.getColumnIndexOrThrow("trip_json"));

                    if (json == null || json.trim().isEmpty()) {
                        Toast.makeText(this, "Trip data is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject obj = new JSONObject(json);
                    JSONArray dayPlans = obj.getJSONArray("dayPlans");
                    JSONArray hotels = obj.getJSONArray("hotels");
                    int totalCost = obj.getInt("totalCost");

                    StringBuilder tripDetails = new StringBuilder();

                    // Day-wise activity details
                    for (int i = 0; i < dayPlans.length(); i++) {
                        JSONObject dayPlan = dayPlans.getJSONObject(i);
                        String day = dayPlan.getString("date");
                        tripDetails.append("Day ").append(i + 1).append(": ").append(day).append("\n");

                        JSONArray activities = dayPlan.getJSONArray("activities");
                        for (int j = 0; j < activities.length(); j++) {
                            JSONObject activity = activities.getJSONObject(j);
                            tripDetails.append("  - ").append(activity.getString("title"))
                                    .append(" (").append(activity.getString("time")).append(")\n")
                                    .append("    Location: ").append(activity.getString("location")).append("\n")
                                    .append("    Description: ").append(activity.getString("description")).append("\n")
                                    .append("    Cost: ₹").append(activity.getInt("cost")).append("\n\n");
                        }
                    }

                    // Hotel details
                    tripDetails.append("Hotels:\n");
                    for (int i = 0; i < hotels.length(); i++) {
                        JSONObject hotel = hotels.getJSONObject(i);
                        tripDetails.append("  - ").append(hotel.getString("name")).append("\n")
                                .append("    Location: ").append(hotel.getString("location")).append("\n")
                                .append("    Description: ").append(hotel.getString("description")).append("\n")
                                .append("    Price: ₹").append(hotel.getInt("price")).append("\n")
                                .append("    Rating: ").append(hotel.getDouble("rating")).append(" stars\n\n");
                    }

                    // Total cost
                    tripDetails.append("Total Trip Cost: ₹").append(totalCost).append("\n");

                    // Share
                    String shareText = "Here's my AI Trip Plan:\n\n" + tripDetails;

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My AI Trip Plan");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(shareIntent, "Share Trip via"));

                } else {
                    Toast.makeText(this, "No trip found to share.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("TripJSON", "Error parsing trip data: " + e.getMessage());
                Toast.makeText(this, "Error parsing trip data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else {
            Toast.makeText(this, "No trip found to share.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDialogMenu() {
        final String[] options = {"Help", "Feedback", "About"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Toast.makeText(getApplicationContext(), "Help selected", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Toast.makeText(getApplicationContext(), "Feedback selected", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Toast.makeText(getApplicationContext(), "About selected", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

}
