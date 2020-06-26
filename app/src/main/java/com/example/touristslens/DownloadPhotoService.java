package com.example.touristslens;

import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadPhotoService extends NotificationTaskService {

    public static final String ACTION_DOWNLOAD = "action_download";
    private List<BackupInfo> mBackupInfo;

    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference(ApplicationConstants.BACKUP_DATABASE_NAME);
        // [END get_storage_ref]
        mBackupInfo = new ArrayList<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            downloadPhotos(Uri.parse(intent.getStringExtra("image_uri")),intent.getStringExtra("image_label"));
        }

        return START_REDELIVER_INTENT;
    }

    private void downloadPhotos(Uri loadURL, String image_label) {
        // [START_EXCLUDE]
        taskStarted();
        showProgressNotification("Downloading", 0, 0,R.drawable.download);
        // [END_EXCLUDE]
        Picasso.get().load(loadURL).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.e("onBitmapLoaded","loaded");

                FileOutputStream outputStream;

                File sdcard= Environment.getExternalStorageDirectory();
                File directory=new File(sdcard.getAbsolutePath()+ ApplicationConstants.BACKUP_DIR_PATH);
                directory.mkdir();

                String filename = image_label+ System.currentTimeMillis() + ".jpg";
                File outfile=new File(directory,filename);

                try {
                    outputStream=new FileOutputStream(outfile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();

                    Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(outfile));
                    sendBroadcast(intent);

                } catch (FileNotFoundException e) {
                    Log.e("Filenotfound",e.getMessage());

                } catch (IOException e) {
                    Log.e("Fileexception",e.getMessage());
                }
                //[Change code]
                showProgressNotification("Downloading" ,
                        100,
                        100,R.drawable.download);

                showDownloadFinishedNotification(true);
                taskCompleted();


            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e("onBitmapFailed","Failed");
                showDownloadFinishedNotification(false);
                taskCompleted();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.e("onPrepareLoad","prepareload");

            }
        });

    }
    void showDownloadFinishedNotification(Boolean status)
    {
        // Hide the progress notification
        dismissProgressNotification();
        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainPageActivity.class);
        String caption = status ? "Download Finished" : "Download Failed";
        showFinishedNotification(caption, intent, status);
    }
}
