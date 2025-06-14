package com.example.aibasetrip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.DayViewHolder> {

    Context context;
    List<DayPlan> dayList;

    public ItineraryAdapter(Context context, List<DayPlan> dayList) {
        this.context = context;
        this.dayList = dayList;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_plan, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayPlan dayPlan = dayList.get(position);
        holder.tvDay.setText("Day " + dayPlan.day + " - " + dayPlan.date);

        SpannableStringBuilder finalText = new SpannableStringBuilder();
        for (Activity act : dayPlan.activities) {
            finalText.append("• ").append(act.time).append(" - ").append(act.title).append("\n")
                    .append("  ").append(act.description).append("\n");

            // Create clickable location span
            SpannableString clickableLocation = new SpannableString("  Location: " + act.location);
            clickableLocation.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    String geoUri = "geo:0,0?q=" + Uri.encode(act.location);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                    intent.setPackage("com.google.android.apps.maps");
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Google Maps not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(true);
                }
            }, 0, clickableLocation.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            finalText.append(clickableLocation).append("\n");
            finalText.append("  Cost: ₹").append(String.valueOf(act.cost)).append("\n\n");
        }

        holder.tvActivities.setText(finalText);
        holder.tvActivities.setMovementMethod(LinkMovementMethod.getInstance());
        holder.tvActivities.setHighlightColor(Color.TRANSPARENT);

        if (!dayPlan.activities.isEmpty()) {
            String location = dayPlan.activities.get(0).location;
            fetchImageForLocation(location, holder);
        }
    }

    private void fetchImageForLocation(String location, DayViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.unsplash.com/search/photos?query=" + location +
                "&per_page=1&client_id=deXNIDC37VRSmC0NAPlIUC-nC9ItN81b0m-XAu9V1Po"; // replace with your Unsplash API key

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseData);
                        org.json.JSONArray results = json.getJSONArray("results");
                        if (results.length() > 0) {
                            String imageUrl = results.getJSONObject(0)
                                    .getJSONObject("urls")
                                    .getString("regular");

                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                Glide.with(context).load(imageUrl).into(holder.imgLocation);
                            });
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvActivities;
        ImageView imgLocation;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvActivities = itemView.findViewById(R.id.tvActivities);
            imgLocation = itemView.findViewById(R.id.imgLocation);
        }
    }
}
