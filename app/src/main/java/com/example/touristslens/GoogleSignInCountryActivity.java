package com.example.touristslens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GoogleSignInCountryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;

    private TextView mGoogleName,mGoogleEmail;
    private Spinner mGoogleCountry;
    private Button mGoogleNext;

    private String mName,mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in_country);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference("Tourists");

        mGoogleName = findViewById(R.id.google_name);
        mGoogleEmail = findViewById(R.id.google_email);
        mGoogleCountry = findViewById(R.id.google_country);

        mGoogleNext = findViewById(R.id.google_next);
        mGoogleNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**Add google signedin user data to firebase database*/
                TouristsInfo touristsInfo = new TouristsInfo(mName, mEmail,
                        mGoogleCountry.getSelectedItem().toString());

                mDbRef.child(mAuth.getCurrentUser().getUid()).setValue(touristsInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(GoogleSignInCountryActivity.this,
                                MainPageActivity.class));
                    }
                });


            }
        });
        mName = mAuth.getCurrentUser().getDisplayName().toUpperCase();
        mEmail = mAuth.getCurrentUser().getEmail();

        mGoogleName.setText(mName);
        mGoogleEmail.setText(mEmail);

        String[] locales = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (String countryCode : locales) {

            Locale obj = new Locale("", countryCode);
            countries.add(obj.getDisplayCountry());

        }
        Collections.sort(countries);
        countries.remove("India");
        countries.add(0,"India");
        mGoogleCountry.setAdapter(countryAdapter);

    }
}
