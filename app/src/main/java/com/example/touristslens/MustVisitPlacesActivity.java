package com.example.touristslens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MustVisitPlacesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<FactsInfo> mFactsInfo;
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_places);

        mVideoView = findViewById(R.id.videoview);
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.monument));
        mVideoView.start();

        Toolbar custom_toolbar = findViewById(R.id.custom_toolbar4);
        setSupportActionBar(custom_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //when back button is pressed go to Mainactivity.class
        custom_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("nav","clicked");
                startActivity( new Intent(MustVisitPlacesActivity.this,MainPageActivity.class)
                        .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        mFactsInfo = new ArrayList<>();

        mRecyclerView = findViewById(R.id.facts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        AssetManager assetManager = getAssets();

        String monuments[]={"TajMahal","IndiaGate","QutubMinar"};
        for(String monument : monuments)
        {
            try {

                InputStream inputStream = assetManager.open(monument+".jpg");
                Drawable d = Drawable.createFromStream(inputStream, null);
                mFactsInfo.add(new FactsInfo(d,monument));

            } catch (IOException e) {
                Log.e("FileException",e.getMessage());
            }
        }

        MustVisitPlacesViewAdapter adapter = new MustVisitPlacesViewAdapter(MustVisitPlacesActivity.this,mFactsInfo);
        mRecyclerView.setAdapter(adapter);

    }
    @Override
    public void onBackPressed() {
        startActivity( new Intent(MustVisitPlacesActivity.this,MainActivity.class)
                .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
