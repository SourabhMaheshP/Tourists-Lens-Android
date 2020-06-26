package com.example.touristslens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**Class for performing backup activity for current user*/
public class BackupActivity extends AppCompatActivity implements View.OnClickListener{

    /**Firebase objects*/
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;

//    private ProgressDialog mProgressDialog;
    private Button mSignIn,mBackup;
    private TextView mEmail;
    private List<BackupInfo> mBackupInfo;
    private ProgressDialog mProgressDialog;

    private static boolean download_flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        /**Firebsae object instantiation*/
        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("Backups");

        mEmail = findViewById(R.id.backup_email);
        mSignIn = findViewById(R.id.backup_signin);
        mBackup = findViewById(R.id.backup_bt);

        mSignIn.setOnClickListener(this);
        mBackup.setOnClickListener(this);

        Toolbar custom_toolbar = findViewById(R.id.custom_toolbar5);
        setSupportActionBar(custom_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //when back button is pressed go to Mainactivity.class
        custom_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(BackupActivity.this,MainPageActivity.class));
                finish();
            }
        });
        mBackupInfo = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null)
        {
            //the user is currently signed in
            mSignIn.setVisibility(View.GONE);
            mBackup.setVisibility(View.VISIBLE);
            mEmail.setText(mFirebaseUser.getEmail());
        }
        else
        {
            //the user is not signed in
            mSignIn.setVisibility(View.VISIBLE);
            mBackup.setVisibility(View.GONE);
            mEmail.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity( new Intent(BackupActivity.this,MainPageActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backup_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.retrieve_photos)
        {
            //check if we can retrieve photos
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null)
            {
                if(ApplicationConstants.checkInternet(BackupActivity.this))
                {
                    Snackbar.make(findViewById(android.R.id.content), "No internet connection!",
                            Snackbar.LENGTH_SHORT)
                            .setTextColor(Color.RED)
                            .show();
                }
                else
                {
                    download_flag = true;
                    downloadPhotos();
                }

            }
            else
            {
                //[Sign in first]
                startActivity(new Intent(BackupActivity.this,MainActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.backup_signin:
                startActivity(new Intent(BackupActivity.this,MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
            case R.id.backup_bt:
                //Upload all the images to Firebase database storage,
                 // Also provide a background service which will upload service in background
                if(ApplicationConstants.checkInternet(BackupActivity.this))
                {
                    Snackbar.make(findViewById(android.R.id.content), "No internet connection!",
                            Snackbar.LENGTH_SHORT)
                            .setTextColor(Color.RED)
                            .show();
                }
                else
                    createBackup();
                break;
        }
    }

    private void createBackup() {

        String LABEL = "", FILENAME = " ";
        Uri FILEURI = null;

        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File directory = new File(sdcard.getAbsolutePath() + ApplicationConstants.APP_DIR_PATH);
            File files[] = directory.listFiles();

            //Ex. Filename TajMahal122434.jpg
            //We want TajMahal
            //and TAJ122434

            for (File file : files) {
                FILENAME = ApplicationConstants.fileNameWithoutExtension(file.getName()); //ex. TAJ12234325
                LABEL = ApplicationConstants.getParsedLabel(file.getName());
                FILEURI = Uri.fromFile(file);
                upload(FILEURI, LABEL, FILENAME);
//                break; //trying to upload 1 image only, remove break later
            }

        } catch (Exception e) {
            Log.e("FILE ISSUE", e.getMessage());
        }

    }
    private void upload(Uri FileUri,String Label,String fileName) {

        //upload to firebase storage and firebase DB- for metadata abt file
        //db structure
        //backups -> uid -> fileName1
        //               -> fileName2
        Snackbar.make(findViewById(android.R.id.content), "Your Photos will be uploaded",
                Snackbar.LENGTH_SHORT).setTextColor(Color.YELLOW).show();
        if(FileUri != null)
        {
            startService(new Intent(this,UploadPhotoService.class)
                .putExtra(UploadPhotoService.EXTRA_FILE_URI, FileUri.toString())
                .putExtra("Label",Label)
                .putExtra("FileName",fileName)
                .setAction(UploadPhotoService.ACTION_UPLOAD));
        }

    }

    /**[To retrieve photos those are backedup on firebase storage]*
     * after retrieving photos place it them in a folder called backup
     */
    private void downloadPhotos()
    {

        mDbRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.e("downloadflag",download_flag+"");
                if(download_flag)
                {
                    mBackupInfo.clear();
                    Log.e("ondatachange","in");
                    for(DataSnapshot photo : dataSnapshot.getChildren())
                    {
                        BackupInfo info = photo.getValue(BackupInfo.class);
                        Log.e("aboutinfo",info+"");
                        mBackupInfo.add(info);
                    }

                    Log.e("size",mBackupInfo.size()+"");
                    if(!mBackupInfo.isEmpty())
                    {
                        for( int i=0; i < mBackupInfo.size(); i++)
                        {
                            Log.e("data:",mBackupInfo.get(i).FilePath + " "+mBackupInfo.get(i).Label);
                            //use Glide and then save to bitmap in local storage
                            Uri image_uri = Uri.parse(mBackupInfo.get(i).FilePath);
                            String image_label = mBackupInfo.get(i).Label;

                            if(image_uri != null && image_label != null)
                            {
                                //start service for every image [i.e inside loop of urls]
                                startService(new Intent(BackupActivity.this,DownloadPhotoService.class)
                                        .putExtra("image_uri",image_uri.toString())
                                        .putExtra("image_label",image_label)
                                        .setAction(DownloadPhotoService.ACTION_DOWNLOAD)
                                );
                            }
                        }
                        download_flag = false;
                    }
                    else{
                        Log.e("BackupActivity:","No photos download");
                        Snackbar.make(findViewById(android.R.id.content), "You haven't backup your photos!",
                                Snackbar.LENGTH_SHORT).setTextColor(Color.YELLOW).show();

                        download_flag = false;

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
