package com.example.touristslens;

import android.net.Uri;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

public class ImageInfo{

    Uri ImageData;
    String Label,date;
    Date timestamp;
    public ImageInfo(Uri imageData, String label, String date) {
        ImageData = imageData;
        Label = label;
        //date manipulation code
        long millisec = new Long(date).longValue();
//        int millisec = new Integer(date).intValue();
        Timestamp ts = new Timestamp(millisec);
        Date d = ts;
        timestamp = d;
        this.date = String.format("%s %tB %<te, %<tY","",d);
    }

    public Uri getImageData() {
        return ImageData;
    }

    public String getLabel() {
        return Label;
    }

    public String getDate() {
        return date;
    }

//    @Override
//    public int compareTo(ImageInfo imageInfo) {
//        return (int) (this.timestamp - imageInfo.timestamp);
//    }
}
class TimeCompare implements Comparator<ImageInfo>{
    @Override
    public int compare(ImageInfo imageInfo, ImageInfo t1) {
        if(imageInfo.timestamp == null ||  t1.timestamp == null)
            return 0;
        else
            return t1.timestamp.compareTo(imageInfo.timestamp);
    }
}