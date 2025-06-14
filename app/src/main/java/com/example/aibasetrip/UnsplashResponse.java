package com.example.aibasetrip;

// UnsplashResponse.java

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UnsplashResponse {
    @SerializedName("results")
    public List<UnsplashPhoto> results;
}

class UnsplashPhoto {
    @SerializedName("urls")
    public Urls urls;
}

class Urls {
    @SerializedName("regular")
    public String regular;
}
