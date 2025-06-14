package com.example.aibasetrip;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import android.database.Cursor;
import java.util.ArrayList;

public class History extends AppCompatActivity {

    HistoryDBHelper db;
    ListView historyList;
    ArrayList<TripModel> tripModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGray));
        }
        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        historyList = findViewById(R.id.historyList);
        db = new HistoryDBHelper(this);
        tripModels = new ArrayList<>();

        Cursor res = db.getAllTrips();
        if (res.getCount() == 0) {
            tripModels.add(new TripModel("No Trip", "No History", "N/A"));
        } else {
            while (res.moveToNext()) {
                String name = res.getString(1);
                String json = res.getString(2);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray dayPlans = jsonObject.getJSONArray("dayPlans");
                    JSONArray hotels = jsonObject.getJSONArray("hotels");

                    StringBuilder planDetails = new StringBuilder();
                    for (int i = 0; i < dayPlans.length(); i++) {
                        JSONObject day = dayPlans.getJSONObject(i);
                        planDetails.append("Day ").append(day.getInt("day"))
                                .append(" - ").append(day.getString("date")).append("\n");
                        JSONArray activities = day.getJSONArray("activities");
                        for (int j = 0; j < activities.length(); j++) {
                            JSONObject act = activities.getJSONObject(j);
                            planDetails.append(" • ").append(act.getString("time")).append(" - ")
                                    .append(act.getString("title")).append(" @ ")
                                    .append(act.getString("location")).append("\n");
                        }
                        planDetails.append("\n");
                    }

                    JSONObject hotel = hotels.getJSONObject(0); // first hotel
                    String hotelInfo = hotel.getString("name") + " - " +
                            hotel.getString("location") + "\n₹" +
                            hotel.getInt("price");

                    tripModels.add(new TripModel(name, planDetails.toString(), hotelInfo));

                } catch (Exception e) {
                    e.printStackTrace();
                    tripModels.add(new TripModel(name, "Error parsing JSON", ""));
                }
            }
        }

        HistoryAdapter adapter = new HistoryAdapter(this, tripModels);
        historyList.setAdapter(adapter);
    }
}
