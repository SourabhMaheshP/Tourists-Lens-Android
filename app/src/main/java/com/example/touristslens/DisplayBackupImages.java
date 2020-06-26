package com.example.touristslens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayBackupImages extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<ImageInfo> mImageInfo;
    private AlertDialog.Builder mBuild;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_backup_images);
        Toolbar custom_toolbar = findViewById(R.id.custom_toolbar6);
        setSupportActionBar(custom_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //when back button is pressed go to Mainactivity.class
        custom_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("nav","clicked");
                startActivity( new Intent(DisplayBackupImages.this,MainPageActivity.class)
                        .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
        mBuild  = new AlertDialog.Builder(DisplayBackupImages.this);
        mRecyclerView = findViewById(R.id.display_backup_img_rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(DisplayBackupImages.this,2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mImageInfo = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showBackupImages();
    }

    private void showBackupImages() {

        File sdcard= Environment.getExternalStorageDirectory();
        File directory=new File(sdcard.getAbsolutePath()+ ApplicationConstants.BACKUP_DIR_PATH);
        File files[] = directory.listFiles();

        if(files != null)
        {
            if(files.length != 0)
            {
                for (File file : files)
                {
                    String fileName = file.getName();
                    Log.e("filename",fileName);
                    String parseLabel = ApplicationConstants.getParsedLabel(fileName);
                    String date = fileName.substring(0,fileName.indexOf(".jpg"));
                    int digitIndex = ApplicationConstants.findDigitIndex(date);
                    mImageInfo.add(new ImageInfo(Uri.fromFile(file),parseLabel,date.substring(digitIndex)));
                }
                Collections.sort(mImageInfo, new TimeCompare());
                AdapterRecycler adapterRecycler = new AdapterRecycler(DisplayBackupImages.this,mImageInfo);
                mRecyclerView.setAdapter(adapterRecycler);
            }
            else
            {
                mBuild.setTitle("No Photos!")
                        .setMessage("You haven't backuped your photos.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                startActivity( new Intent(DisplayBackupImages.this,MainPageActivity.class)
                                        .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        });
                mBuild.create().show();
            }
        }
        else
        {
            mBuild.setTitle("No Photos!")
                    .setMessage("You haven't backuped your photos.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            startActivity( new Intent(DisplayBackupImages.this,MainPageActivity.class)
                                    .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    });
            mBuild.create().show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity( new Intent(DisplayBackupImages.this,MainPageActivity.class)
                .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
