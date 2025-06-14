package com.example.aibasetrip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    Context context;
    List<TripModel> trips;

    public HistoryAdapter(Context context, List<TripModel> trips) {
        this.context = context;
        this.trips = trips;
    }

    @Override
    public int getCount() {
        return trips.size();
    }

    @Override
    public Object getItem(int i) {
        return trips.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);

        TextView title = view.findViewById(R.id.tripTitle);
        TextView details = view.findViewById(R.id.tripDetails);
        TextView hotel = view.findViewById(R.id.tripHotel);

        TripModel trip = trips.get(position);

        title.setText(trip.getTitle());
        details.setText(trip.getDetails());
        hotel.setText("Hotel: " + trip.getHotel());

        return view;
    }
}
