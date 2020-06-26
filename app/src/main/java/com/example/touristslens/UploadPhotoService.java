package com.example.touristslens;
/**This Class creates a service which shows the progress of the upload image
 * in notification bar*/

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.touristslens.NotificationTaskService;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadPhotoService extends NotificationTaskService {

    private static final String TAG = "MyUploadService";

    /** Intent Actions **/
    public static final String ACTION_UPLOAD = "action_upload";
//    public static final String UPLOAD_COMPLETED = "upload_completed";
//    public static final String UPLOAD_ERROR = "upload_error";

    /** Intent Extras **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
//    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

    private StorageReference mStorageRef;
    private DatabaseReference mDbRef;


    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference(ApplicationConstants.BACKUP_DATABASE_NAME);
        mDbRef = FirebaseDatabase.getInstance().getReference(ApplicationConstants.BACKUP_DATABASE_NAME);
        // [END get_storage_ref]
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD.equals(intent.getAction())) {
            Uri fileUri = Uri.parse(intent.getStringExtra(EXTRA_FILE_URI));
            String Label = intent.getStringExtra("Label");
            String fileName = intent.getStringExtra("FileName");
            // Make sure we have permission to read the data
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                getContentResolver().takePersistableUriPermission(
//                        fileUri,
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }

            uploadFromUri(fileUri,Label,fileName);
        }

        return START_REDELIVER_INTENT;
    }

    private void uploadFromUri(Uri FileUri,String Label,String FileName) {
        // [START_EXCLUDE]
        taskStarted();
        showProgressNotification("Uploading", 0, 0,R.drawable.upload);
        // [END_EXCLUDE]

        StorageReference fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FileName);
        fileRef.putFile(FileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //1. close progress bar
                            //2. add details to DB
//                            Log.e("ActualFileRef:",fileRef.getDownloadUrl().toString());

                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageUrl = uri.toString();
                                            BackupInfo backupInfo = new BackupInfo(imageUrl, Label);
                                            mDbRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FileName)
                                                    .setValue(backupInfo);

                                            showUploadFinishedNotification(fileRef.getDownloadUrl().toString());
                                            taskCompleted();
                                        }
                                    });
                                }
                            }
//                            BackupInfo backupInfo = new BackupInfo(fileRef.getDownloadUrl().toString(), Label);
//                            mDbRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FileName)
//                                    .setValue(backupInfo);
//
//                            showUploadFinishedNotification(fileRef.getDownloadUrl().toString());
//                            taskCompleted();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showUploadFinishedNotification(null);
                            taskCompleted();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            showProgressNotification("Uploading" ,
                                    taskSnapshot.getBytesTransferred(),
                                    taskSnapshot.getTotalByteCount(),R.drawable.upload);
                        }
                    })
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        Log.d(TAG, "uploadFromUri: upload success");

                        // Request the public download URL
                        return fileRef.getDownloadUrl();
                    }
                });

    }
    private void showUploadFinishedNotification(String downloadUrl) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainPageActivity.class);
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = downloadUrl != null;
        String caption = success ? "Upload Finished" : "Upload Failed";
        showFinishedNotification(caption, intent, success);
    }

}
