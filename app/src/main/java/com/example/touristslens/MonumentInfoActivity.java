package com.example.touristslens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class MonumentInfoActivity extends AppCompatActivity {

    private TextView mInfo;
    private AppCompatImageView mMonument;
    private CollapsingToolbarLayout mCollapsing;
    private Uri mImageUri;
    private String mLabel;
//    private Class mFromActivity;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_info);

        AssetManager assetManager = getAssets();

        mInfo = findViewById(R.id.information);
//        mInfo.setMovementMethod(new ScrollingMovementMethod());
        mInfo.setClickable(true);
        mMonument = findViewById(R.id.info_image);
        mCollapsing = findViewById(R.id.info_collapsing);

        mCollapsing.setContentScrimColor(Color.rgb( 235, 152, 78 ));
        mCollapsing.setStatusBarScrimColor(Color.rgb( 230, 126, 34));

        Toolbar custom_toolbar = findViewById(R.id.info_toolbar);
        setSupportActionBar(custom_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //when back button is pressed go to Mainactivity.class
        custom_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(
                        new Intent(MonumentInfoActivity.this,MainPageActivity.class)
                                .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );
                finish();
            }
        });

        Intent get = getIntent();
        String imageUri = get.getStringExtra("Monument");
        mImageUri = Uri.parse(imageUri);
        mLabel = get.getStringExtra("Label");
        if(!imageUri.equals(""))
            mMonument.setImageURI(Uri.parse(imageUri));
        else
        {
            //if no image in Monument string , load it from asset folder
            InputStream inputStream = null;
            try {
                inputStream = assetManager.open(mLabel+".jpg");
            } catch (IOException e) {
                Log.e("monumentInfo","can't load image");
            }
            Drawable d = Drawable.createFromStream(inputStream, null);
            if(d != null)
                mMonument.setImageDrawable(d);
        }


        mCollapsing.setTitle(""+mLabel);
//        Object c = get.getStringExtra("FromActivity");
//        try {
//            mFromActivity = Class.forName(get.getStringExtra("FromActivity"));
//            Log.e("NoClass",mFromActivity.getClass().toString());
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            Log.e("NoClass",e.getMessage());
//        }

        try {
            InputStream inputStream = assetManager.open(mLabel+".txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String text = new String(buffer);
            text = text.replace("\n","").replace("\r","");
            mInfo.setText(text);
        } catch (IOException e) {
            Log.e("FileException",e.getMessage());
        }

    }
    @Override
    public void onBackPressed() {
        startActivity(
                new Intent(MonumentInfoActivity.this,MainPageActivity.class)
                .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                );
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.infopage_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Log.e("inshare",mImageUri+" "+mLabel);
        if(item.getItemId() == R.id.share_photo)
        {
            //code here
            shareImageToOtherApps();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareImageToOtherApps()
    {
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_TEXT, mLabel+" is one of the famous Monument of India");
        share.putExtra(Intent.EXTRA_STREAM,mImageUri);
        startActivity(Intent.createChooser(share, "Share Monument Information"));
    }
}
