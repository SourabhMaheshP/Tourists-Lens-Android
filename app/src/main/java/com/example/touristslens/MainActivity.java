package com.example.touristslens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    //firebase instances
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDbReference;

    //Shared Prefs for caching emails
    //To show recommendation of already used emails.
    //Integrated with AutoCompleteTextView
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    //Various [inputs]
    private AutoCompleteTextView mInputEmail;
    private EditText mInputPassword;
    private TextView mSignup,mSkip;
    private Button mLogin;
    private SignInButton mGoogleSignIn;
    private AlertDialog.Builder mBuild;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.CustomTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = MainActivity.this;
        mSharedPreferences = context.getSharedPreferences("Emails",context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        //firebase instantiation
        mAuth = FirebaseAuth.getInstance();
        // [Configure Google Sign In]
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //prompt registered successful
        mBuild  = new AlertDialog.Builder(MainActivity.this);
        mLogin =  findViewById(R.id.login);
        mInputEmail = findViewById(R.id.inputemail);
        mInputPassword = findViewById(R.id.inputpassword);
        mSignup = findViewById(R.id.sign_up);
        mSkip = findViewById(R.id.skip);
        mGoogleSignIn = findViewById(R.id.google_signin);

        mSignup.setOnClickListener(this);
        mSkip.setOnClickListener(this);
        mGoogleSignIn.setOnClickListener(this);
        mLogin.setOnClickListener(this);

        Intent extra = getIntent();
        String intent_email = extra.getStringExtra("email");
        if(intent_email!=null)
        {
            mInputEmail.setText(intent_email);
            mInputPassword.setFocusableInTouchMode(true);
        }

        mInputEmail.addTextChangedListener(this);
        mInputPassword.addTextChangedListener(this);

        //code for adding suggestions for email - like cookies
        List<String> list_email = new ArrayList<>();
        if(mSharedPreferences != null)
        {
            Map<String,String> get_emails = (Map<String, String>) mSharedPreferences.getAll();
            if(get_emails!=null)
            {
                for (Map.Entry<String,String> entry : get_emails.entrySet())
//                    Log.e("emails","Key = " + entry.getKey() + ", Value = " + entry.getValue());
                    list_email.add(entry.getKey());
            }
        }

        //Setting adapter containing list of emails used already
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.select_dialog_item,list_email);
        mInputEmail.setThreshold(1);
        mInputEmail.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.login:
                //Firabase sign in with email and password

                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setTitle("Signing in");
                mProgressDialog.setMessage("Please Wait..");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                String email = mInputEmail.getText().toString().trim();
                String password = mInputPassword.getText().toString();

                //doing sharedprefs for email suggestions
                if(!mSharedPreferences.contains(email)) {
                    Log.e("in","in");
                    mEditor.putString(email,email); //key- email value- email
                    mEditor.commit();
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this,
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        mProgressDialog.dismiss();
                                        if (task.isSuccessful()) {

                                            Log.e("FirebaseLogin:","Success");
                                            //Calling another activity after successful login
                                            startActivity(new Intent(MainActivity.this,MainPageActivity.class));

                                        } else {

                                            //check internet connection, optional remove after

                                            if(ApplicationConstants.checkInternet(MainActivity.this))
                                            {
                                                Snackbar.make(findViewById(android.R.id.content), "No internet connection!",
                                                        Snackbar.LENGTH_SHORT)
                                                        .setTextColor(Color.RED)
                                                        .show();
                                            }
                                            else
                                            {
                                                //prompt user: incorrect credentials
                                                mBuild.setTitle("Incorrect Credentials!")
                                                        .setIcon(R.drawable.incorrect)
                                                        .setCancelable(false)
                                                        .setMessage("Check your username and password.")
                                                        .setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });
                                                mBuild.create().show();
                                            }

                                        }
                                    }
                                });
                break;
            case R.id.google_signin:
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setTitle("Signing in");
                mProgressDialog.setMessage("Please Wait..");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                signIn();
                break;
            case R.id.sign_up:
                startActivity( new Intent(MainActivity.this,SignUpActivity.class));
                finish();
                break;
            case R.id.skip:
                startActivity( new Intent(MainActivity.this,MainPageActivity.class));
                finish();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, ApplicationConstants.RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == ApplicationConstants.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e("Google sign in failed", e.getMessage());
                mProgressDialog.cancel();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressDialog.cancel();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("signIn:success","success");
                            mDbReference = FirebaseDatabase.getInstance().getReference().child("Tourists").child(
                                    mAuth.getCurrentUser().getUid());
                            mDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() == null)
                                            /**Asking for country if new google sign in user
                                             * Adding country to FirebaseDB*/
                                            startActivity(new Intent(MainActivity.this,GoogleSignInCountryActivity.class));
                                        else
                                            /**Calling Another Activity
                                             * After Successful login*/
                                            startActivity(new Intent(MainActivity.this,MainPageActivity.class));

                                        finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("signIn:failed", task.getException().toString());
                            Snackbar.make(findViewById(android.R.id.content), "Login Failed, Try Again!",
                               Snackbar.LENGTH_LONG)
                             .setTextColor(Color.YELLOW).show();
                        }
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        Log.e("login:",mInputEmail.getText().toString()+","+mInputPassword.getText().toString());
        mToggle(mInputEmail,mInputPassword);
    }
    private void mToggle(EditText mInputEmail , EditText mInputPassword)
    {
        String email = mInputEmail.getText().toString();
        String pwd = mInputPassword.getText().toString();
        if (pwd.length() >= ApplicationConstants.MAX_INPUT_PWD && validateEmail(email)) {
            mLogin.setEnabled(true);
            mLogin.setAlpha(1);
        } else {
            mLogin.setEnabled(false);
            mLogin.setAlpha((float) 0.5);
        }

    }

    /**Method for validating email using Regex*/
    private Boolean validateEmail(String email)
    {
        return email.matches(ApplicationConstants.emailRegEx);
    }
    //validation handle:
    //1. login credentials invalid
    //2. if length password greater than 6, enable login button -- by textwatcher
    //3. handle onclick of signup
}
