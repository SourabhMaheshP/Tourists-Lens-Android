package com.example.touristslens;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.net.URI;
import java.util.Date;

public class FactsInfo {

    Drawable ImageData;
    String Label;
    public FactsInfo(Drawable imageData, String label) {
        ImageData = imageData;
        Label = label;
    }

    public Drawable getImageData() {
        return ImageData;
    }

    public String getLabel() {
        return Label;
    }


}
