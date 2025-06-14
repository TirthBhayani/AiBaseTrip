package com.example.aibasetrip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    Context context;
    List<Hotel> hotels;

    public HotelAdapter(Context context, List<Hotel> hotels) {
        this.context = context;
        this.hotels = hotels;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel h = hotels.get(position);

        holder.tvName.setText(h.name);

        // Build spannable with clickable location
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(h.description).append("\n");

        // Clickable location
        SpannableString clickableLocation = new SpannableString("Location: " + h.location);
        clickableLocation.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String geoUri = "geo:0,0?q=" + Uri.encode(h.location);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Google Maps not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(true);
            }
        }, 0, clickableLocation.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        sb.append(clickableLocation).append("\n");
        sb.append("Price: ₹").append(String.valueOf(h.price)).append(" | ⭐ ").append(String.valueOf(h.rating));

        holder.tvDetails.setText(sb);
        holder.tvDetails.setMovementMethod(LinkMovementMethod.getInstance());
        holder.tvDetails.setHighlightColor(Color.TRANSPARENT);

        fetchImageForLocation(h.location, holder);
    }

    private void fetchImageForLocation(String location, HotelViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.unsplash.com/search/photos?query=" + location +
                "&per_page=1&client_id=deXNIDC37VRSmC0NAPlIUC-nC9ItN81b0m-XAu9V1Po"; // replace with your own API key

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
                                Glide.with(context).load(imageUrl).into(holder.imgHotel);
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
        return hotels.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        ImageView imgHotel;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvDetails = itemView.findViewById(R.id.tvHotelDetails);
            imgHotel = itemView.findViewById(R.id.imageHotel); // add this ImageView in item_hotel.xml
        }
    }
}
