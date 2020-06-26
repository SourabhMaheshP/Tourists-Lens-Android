package com.example.touristslens;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

/**File containing all constants and config */
public class ApplicationConstants {

    /**ML model specific constants*/
    public static  final  int NO_OF_CLASSES = 3;
    public static final String MODEL_PATH = "monument_3_RGB.tflite";
    public static final String LABEL_PATH = "labels.txt";
    public static final int reSize = 224;
    /**--------------------------*/
    public final static int MAX_INPUT_PWD = 6;
    public final static String emailRegEx = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    public final static int RC_SIGN_IN = 100;
    public final static int STORAGE_PERMISSION_CODE = 1;
    public final static String APP_DIR_PATH = "/TouristsLens";
    public static final String BACKUP_DIR_PATH = "/TLBackups";
    public static final String BACKUP_DATABASE_NAME = "Backups";
    /**Static methods*/

   public static String getParsedLabel(String imagePath) {

        Log.e("ImageName",imagePath);
        String newImageName = imagePath.substring(0,imagePath.indexOf(".jpg"));
        String Label = "";
        for (char c:newImageName.toCharArray())
        {
            if(Character.isAlphabetic(c))
                Label += c;
            else
                break;
        }
        Log.e("imgLabel",Label);
        return Label;
    }
    public static String fileNameWithoutExtension(String fileName)
    {
        return fileName.substring(0,fileName.indexOf("."));
    }

    public static boolean checkInternet(Activity activity)
    {
        NetworkInfo info = ((ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        //[return null if no internet]
        return info == null;
    }
    //For getting the label of the image from FILE NAME
    //Actual file name - [ TAJMAHAL12234235.jpg ]
    //After this method returns - [ TAJMAHAL ]
    public static int findDigitIndex(String x) {
        char c[] = x.toCharArray();
        for(int i=0;i<x.length();i++)
        {
            if(Character.isDigit(c[i]))
                return i;
        }
        return 0;
    }
}
