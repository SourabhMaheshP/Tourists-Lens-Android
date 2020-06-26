package com.example.touristslens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MonumentFactsActivity extends AppCompatActivity {

    ImageView mMonument;
    AssetManager mAsset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_facts);
        final Toolbar toolbar = findViewById(R.id.htab_toolbar);
        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().setTitle("Facts About Monuments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(MonumentFactsActivity.this,MainPageActivity.class)
                        .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                );
                finish();
            }
        });

        mMonument = findViewById(R.id.htab_header);
        mAsset = getAssets();

        final ViewPager viewPager = findViewById(R.id.htab_viewpager);
        setupViewPager(viewPager);


        TabLayout tabLayout = findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.htab_collapse_toolbar);

            collapsingToolbarLayout.setContentScrimColor(Color.rgb( 235, 152, 78 ));
            collapsingToolbarLayout.setStatusBarScrimColor(Color.rgb( 230, 126, 34));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                Log.d("Ontabselected", "onTabSelected: pos: " + tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        assetChangeImage("TajMahal");
                        break;
                    case 1:
                        assetChangeImage("IndiaGate");
                        break;
                    case 2:
                        assetChangeImage("QutubMinar");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
 }
 private void assetChangeImage(String monument)
 {
     try {
         InputStream inputStream = mAsset.open(monument+".jpg");
         Drawable d = Drawable.createFromStream(inputStream, null);
         mMonument.setImageDrawable(d);

     } catch (IOException e) {
         Log.e("FileException",e.getMessage());
     }
 }
    @Override
    public void onBackPressed() {
        startActivity( new Intent(MonumentFactsActivity.this,MainPageActivity.class)
                .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFrag(new DummyFragment("TajMahal"), "Taj Mahal");
        adapter.addFrag(new DummyFragment("IndiaGate"), "India Gate");
        adapter.addFrag(new DummyFragment("QutubMinar"), "Qutub Minar");
        viewPager.setAdapter(adapter);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm, int behavior) {
            super(fm, behavior);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    public static class DummyFragment extends Fragment {

        String mMonument;
        TextView mFacts;
        public DummyFragment() {
        }
        public DummyFragment(String monument) {
            mMonument = monument;
        }
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_facts, container, false);
            mFacts = view.findViewById(R.id.factsinfo);
            mFacts.setMovementMethod(new ScrollingMovementMethod());
            AssetManager assetManager = getActivity().getAssets();
            try {
                InputStream inputStream = assetManager.open("Facts"+mMonument+".txt");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                String text = new String(buffer);
//                text = text.replace("\n","").replace("\r","");
                mFacts.setText(text);
            } catch (IOException e) {
                Log.e("FileException",e.getMessage());
            }
            return view;
        }
    }
}

