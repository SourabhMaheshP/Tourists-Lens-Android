package com.example.touristslens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity implements TextWatcher{

    private EditText mName,mEmail,mPassword,mConfirmPassword;
    private Button mRegister;
    private TextInputLayout mNameLayout,mEmailLayout,mPasswordLayout,mCfmPasswordLayout;
    private int name_flag = 0,email_flag = 0,pwd_flag=0,cmf_pwd_flag=0;
    private Spinner mCountry;

    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

//    private ProgressBar mProgressBar;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder mBuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.CustomTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //setting up the custom toolbar
        Toolbar custom_toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(custom_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Create instance of of mAuth
        mAuth = FirebaseAuth.getInstance();

        //Database instance
        dbReference = FirebaseDatabase.getInstance().getReference("Tourists");

        //prompt registered successful
        mBuild  = new AlertDialog.Builder(SignUpActivity.this);

        //when back button is pressed go to Mainactivity.class
        custom_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(SignUpActivity.this,MainActivity.class));
                finish();
            }
        });

//        mProgressBar = findViewById(R.id.loading);

        mRegister = findViewById(R.id.register);
        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirm_password);
        mCountry = findViewById(R.id.country);

        //Layout instance
        mNameLayout = findViewById(R.id.name_layout);
        mEmailLayout = findViewById(R.id.email_layout);
        mPasswordLayout= findViewById(R.id.password_layout);
        mCfmPasswordLayout = findViewById(R.id.confirm_password_layout);

        /**Setting up country spinner*/
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
        mCountry.setAdapter(countryAdapter);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //validate input fields and redirect to save data in firebase
                mProgressDialog = new ProgressDialog(SignUpActivity.this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setTitle("Signing Up");
                mProgressDialog.setMessage("Please wait.");
                mProgressDialog.show();

                if(name_flag == 0 && email_flag == 0 && pwd_flag == 0 && cmf_pwd_flag == 0 && checkEmpty())
                {
                    Log.e("Register","Success");
//                    Toast.makeText(getApplicationContext(),"successful",Toast.LENGTH_LONG).show();
                    //sign up new user in firebase

                    mAuth.createUserWithEmailAndPassword(mEmail.getText().toString().trim(),mPassword.getText().toString())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    mProgressDialog.dismiss();

                                    if (task.isSuccessful()) {

                                        // Sign in success, update UI with the signed-in user's information
                                        Log.e("firebaseSignUp", "createUserWithEmail:success");

                                        //Store tousist's info
                                        TouristsInfo touristsInfo = new TouristsInfo(mName.getText().toString(),
                                                mEmail.getText().toString().trim(),mCountry.getSelectedItem().toString());

                                        dbReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(touristsInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                //prompt user: registered successful
                                                mBuild.setTitle("Registered Successfully!").setIcon(R.drawable.correct)
                                                        .setCancelable(false)
                                                        .setMessage("Please Log in to continue.")
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                //  call log in activity here
                                                                Intent login = new Intent(SignUpActivity.this,MainActivity.class);
                                                                login.putExtra("email",mEmail.getText().toString());
                                                                startActivity(login);
                                                                finish();
                                                            }
                                                        });
                                                mBuild.create().show();
                                            }
                                        });

                                    } else {
                                        //if signin failed
                                       Toast.makeText(SignUpActivity.this,
                                               "Authentication Failed! Please check Internet connection",
                                               Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                }
                else{
                    mProgressDialog.dismiss();
                    Snackbar.make(findViewById(android.R.id.content), "Enter fields to continue",
                            Snackbar.LENGTH_SHORT).setTextColor(Color.YELLOW).show();
                }

            }
        });


        mName.addTextChangedListener(this);
        mEmail.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        mConfirmPassword.addTextChangedListener(this);

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    public void onBackPressed() {
        startActivity( new Intent(SignUpActivity.this,MainActivity.class));
        finish();
    }
    private Boolean check(String name)
    {
        char[] strr = name.toCharArray();
        for (char c:strr)
        {
            if(!Character.isAlphabetic(c) && c != ' ')
                return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
        String name = mName.getText().toString();
        String email = mEmail.getText().toString();


        if(s.toString().equals(name))
        {
            if((name.equals("") || check(name) || name.length()<3))
            {
                mNameLayout.setError("Invalid name!");
                name_flag = 1;
            }
            else {
                mNameLayout.setHelperText(null);
                mNameLayout.setError(null);
                name_flag = 0;
            }
        }
        else if(s.toString().equals(email))
        {
            if( email.equals("") || !email.matches(ApplicationConstants.emailRegEx) )
            {
                mEmailLayout.setError("Invalid email!");
                email_flag = 1;
            }
            else {
                mEmailLayout.setHelperText(null);
                mEmailLayout.setError(null);
                email_flag = 0;
            }
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

                if(s == mPassword.getEditableText())
                {
                    String pwd = mPassword.getText().toString();

                    if( pwd.length() < ApplicationConstants.MAX_INPUT_PWD )
                    {
                        mPasswordLayout.setError("Password too short!");
                        pwd_flag = 1;

                    }
                    else if( mConfirmPassword.getText().toString().length()>0 )
                        mConfirmPassword.setText("");
                    else
                    {
                        mPasswordLayout.setHelperText(null);
                        mPasswordLayout.setError(null);
                        pwd_flag = 0;
                    }
        }
        if(s == mConfirmPassword.getEditableText())
        {
            String cfm_pwd = mConfirmPassword.getText().toString();
            if( !cfm_pwd.equals(mPassword.getText().toString())
                    || cfm_pwd.equals("")
                    || cfm_pwd.length() < ApplicationConstants.MAX_INPUT_PWD )
            {
                mCfmPasswordLayout.setError("Password doesn't match");
                cmf_pwd_flag = 1;
            }
            else {
                mCfmPasswordLayout.setError(null);
                cmf_pwd_flag = 0;
            }
        }
    }

    private Boolean checkEmpty()
    {
        return (   !mName.getText().toString().equals("")
                && !mEmail.getText().toString().equals("")
                && !mPassword.getText().toString().equals("")
                && !mConfirmPassword.getText().toString().equals("")
                ) ;
    }
}
