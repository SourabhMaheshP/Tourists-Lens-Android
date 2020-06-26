package com.example.touristslens;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

public class MainPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**Shared Prefs*/
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    /**Firebase references*/
    private DatabaseReference mDbReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    /**List containing the paths of images to delete*/
    public List<String> uri_path; //only Adapter class performs operations on this list

    /**Layout/view references*/
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private AlertDialog.Builder mBuild;
    private TextView mName,mEmail;
    private Menu mMenu;
    private FloatingActionButton mFab;
    private FrameLayout frameLayout;
    private List<ImageInfo> mImageInfo;
    private RecyclerView mRecyclerView;
    private TextView mAppGreeting;
    private LinearLayout mAppLayoutGreeting;
    private View headerView;

    /**Machine Learning Model reference
     *Model_Info -
     *Model is a CNN model -- classified on three monuments namely, (India Gate, Qutub Minar, Tajmahal)*/
    private MLModel model ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Context context = MainPageActivity.this;

        //Shared Prefs for Greeting user when photo of a "new" monument is clicked
        mSharedPreferences = context.getSharedPreferences("GreetingDialog",context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        uri_path = new ArrayList<>();

        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        //For displaying all photos clicked by a user
        mRecyclerView = findViewById(R.id.recycler_view);
        //spancount = no.of coln
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainPageActivity.this,2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        //list of all photos with their information
        mImageInfo = new ArrayList<>();
        mAppGreeting = findViewById(R.id.appgreeting);
        mAppLayoutGreeting = findViewById(R.id.applayoutgreeting);
        //Next: do here , iterate over images in directory touristlens

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });
        frameLayout = findViewById(R.id.fragment_container);

        /**Alert Dialog for asking user for logout confirmation */
        mBuild  = new AlertDialog.Builder(this);

        /**Navigation Drawer View Instance*/
        mNavigationView = findViewById(R.id.nav_view);
        headerView = mNavigationView.getHeaderView(0);
        mNavigationView.setNavigationItemSelectedListener(this);

        //set Email in nav drawer
        mEmail = headerView.findViewById(R.id.greeting_email);
        mName = headerView.findViewById(R.id.greeting_name);

        //setting up toolbar
        Toolbar custom_toolbar = findViewById(R.id.custom_toolbar2);
        setSupportActionBar(custom_toolbar);
        getSupportActionBar().setTitle("");

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, custom_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.hamcolor));
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setCheckedItem(R.id.gallery);
        model = new MLModel(this);
    } //oncreate

    @Override
    protected void onStart() {
        super.onStart();

        //Update Gallery - i.e upload images from internal storage
        updateUI();

        //Check if the user is signed in and take necessary actions
        mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null)
        {
            //the user is currently signed in*/
            mNavigationView.getMenu().findItem(R.id.acc_signin).setVisible(false);
            mNavigationView.getMenu().findItem(R.id.logout).setVisible(true);
            //set user's name and email
            mEmail.setText(mFirebaseUser.getEmail());
            //For Name - it can be from Google Signed In or Application Signed in */

            mDbReference = FirebaseDatabase.getInstance().getReference().child("Tourists").child(mFirebaseUser.getUid());
           //Listener for retrieving data from firebase database*/
            mDbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (mFirebaseUser.getDisplayName() == null)
                        mName.setText("Hello, "+dataSnapshot.child("Name").getValue().toString()+"!");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if(mFirebaseUser.getDisplayName() != null)
                //user is google signed in
                mName.setText("Hello, "+mFirebaseUser.getDisplayName()+"!");

        }
        else
        {
           //the user is not signed in

            mEmail.setText("Sign In for more options");
            mName.setText("You have not Signed In");
            mName.setTextColor(Color.RED);

            // Set Visibility of Signed In user and Remove Visibility of Logout menu item in nav drawer
            // add click listener to appropriate button
            mNavigationView.getMenu().findItem(R.id.logout).setVisible(false);
            mNavigationView.getMenu().findItem(R.id.acc_signin).setVisible(true);
        }
    }

    //Activity menu panel
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mainpage_menu,menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.delete_photos)
        {
            //delete photos in uri_path
            if(!uri_path.isEmpty())
            {
                //disable delete item
                toggleDeleteMenuItem(false);

                for(String path : uri_path)
                {
                    try {

                        File photo = new File(new URI(path));
                        photo.delete();

                        //broadcast changes here i.e deletion here
                        broadcastImageOperation(photo);
                        updateUI();

                    } catch (URISyntaxException e) {
                        Log.e("mainpage",e.getMessage());
                    }
                }
                //clear uri_paths
                uri_path.clear();
            }
            else{
                Log.e("mainpage", "onOptionsItemSelected: Empty uripath" );
            }

            return true;
        }
        else if(item.getItemId() == R.id.display_backup_img)
        {
            startActivity(new Intent(MainPageActivity.this,DisplayBackupImages.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId())
        {
            case R.id.gallery:
                frameLayout.setVisibility(View.GONE);

                break;
            case R.id.contactus:
                frameLayout.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enterright,R.anim.exitright,R.anim.enterright,R.anim.exitright)
                        .replace(R.id.fragment_container, new ContactFragment())
                        .commit();
                break;

            case R.id.acc_signin:
                startActivity(new Intent(MainPageActivity.this,MainActivity.class));
                finish();
                break;
             case R.id.logout:
                mBuild.setTitle("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                mAuth.signOut();

                                startActivity(
                                        new Intent(MainPageActivity.this,MainActivity.class)
                                                .setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                );
                                finish();
                                //after this authchangelistener is called in oncreate method
                            }
                        })
                        .create()
                        .show();
                break;

            case R.id.monument_facts:
                startActivity(new Intent(MainPageActivity.this,MonumentFactsActivity.class));
                break;

            case R.id.visited_place:
                startActivity(new Intent(MainPageActivity.this, MustVisitPlacesActivity.class));
                break;
            case R.id.backups:
                startActivity(new Intent(MainPageActivity.this,BackupActivity.class));
                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ApplicationConstants.STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                this.startActivityForResult(intent,100);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Image in bitmap after clicked is successful
    /**Main Driver code after image is clicked*/
    /**Calling ML model to classify the clicked image; input = Bitmap*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {

            //[Captured Image in bitmap Format]
            Bitmap captureImage = (Bitmap) data.getExtras().get("data");

            //[Run Machine Learning Model (CNN- classification)]
            model.createInference(captureImage);
            String label = model.getModelOutput();

            //[Save Image to Local Storage]
            saveImageToLocal(captureImage,label);

            //[show pop up for 1st time when user clicks image of monument.]
            //[create sharedprefs for such that we can track the count of image clicked by user]
            if(!mSharedPreferences.contains(label))
            {
                mEditor.putString(label,label);
                mEditor.commit();
                //call pop up dialog method
                showPopUpDialog(label);
            }

           //[getModelOutput returns classified image Label (String)]
//           Toast.makeText(this,model.getModelOutput(),Toast.LENGTH_LONG).show();
        }
    }

    /**For showing pop up box containg information when photo is clicked */
    private void showPopUpDialog(String Label)
    {
        androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(
                MainPageActivity.this,R.style.AlertDialogTheme);

        View view= LayoutInflater.from(MainPageActivity.this).inflate(
                R.layout.layout_dialog,
               findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText("Welcome To "+Label);
        ((TextView) view.findViewById(R.id.textMessage)).setText(Label+" is an iconic place to visit in India.\nIt is one " +
                "of the famous monument of India.");
        ((Button) view.findViewById(R.id.buttonAction)).setText("Read More About "+Label);
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_msg);

        final androidx.appcompat.app.AlertDialog alertDialog=builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                //redirect to info page
                Intent intent = new Intent(MainPageActivity.this,MonumentInfoActivity.class);
                intent.putExtra("Monument","");
                intent.putExtra("Label",Label);
                intent.putExtra("FromActivity", MustVisitPlacesActivity.class.getName());
                startActivity(intent);
            }
        });

        if(alertDialog.getWindow()!=null)
        {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

    }
    /**This method is used to Toggle Delete Menu Item (Enable or Disable)
     * [Enable] - When Images are selected
     * [Disable] - When no images are selected for deletion
     * Default is disabled*/
    protected void toggleDeleteMenuItem(Boolean toggle)
    {
        if(mMenu != null)
        {
            if(toggle == true)
                mMenu.findItem(R.id.delete_photos).setEnabled(true);
            else
                mMenu.findItem(R.id.delete_photos).setEnabled(false);
        }
    }

    /**Update The UI Screen when Images related operation are performed
     * Deletion, New image clicked (returning from camera activity)*/
    protected void updateUI() {

        mImageInfo.clear();
        File sdcard= Environment.getExternalStorageDirectory();
        File directory=new File(sdcard.getAbsolutePath()+ ApplicationConstants.APP_DIR_PATH);
        File files[] = directory.listFiles();

        if(files != null)
        {
            if(files.length != 0)
            {
                //set Delete button
//                if(mMenu != null)
//                    mMenu.findItem(R.id.delete_photos).setEnabled(true);
                mAppLayoutGreeting.setVisibility(View.GONE);
                for (File file : files)
                {
                    String fileName = file.getName();
                    Log.e("filename",fileName);
                    String parseLabel = ApplicationConstants.getParsedLabel(fileName);
                    String date = fileName.substring(0,fileName.indexOf(".jpg"));
                    int digitIndex = ApplicationConstants.findDigitIndex(date);
                    mImageInfo.add(new ImageInfo(Uri.fromFile(file),parseLabel,date.substring(digitIndex)));
                }
            }
            else
            {
                mAppLayoutGreeting.setVisibility(View.VISIBLE);
                mAppGreeting.setText(mAppGreeting.getText().toString());
            }

        }
        Collections.sort(mImageInfo, new TimeCompare());
        AdapterRecycler adapterRecycler = new AdapterRecycler(MainPageActivity.this,mImageInfo);
        mRecyclerView.setAdapter(adapterRecycler);
    }

    //BroadCast Message to all the apps, when image operations are performed
    private void broadcastImageOperation(File file)
    {
        Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    //    @Override
    public void startCamera() {
        //implement open camera here
        //check run time permissions and open camera
        //pass it to ML classify image
        //show pop up with greetings i.e welcome to tajmahal add checkbox to it saying for not showing again
        //save it in firebase storge along with image label
        //show gallery like a card view with dynamically updating page
        //sharing option for this image
        new Permissions(MainPageActivity.this).createPermission(); //camera Permission

    } // [After this method onActivityresult method is called]

    //Method to save image to local memory
    private void saveImageToLocal(Bitmap captureImage,String Label) {

        FileOutputStream outputStream;

        File sdcard= Environment.getExternalStorageDirectory();
        File directory=new File(sdcard.getAbsolutePath()+ ApplicationConstants.APP_DIR_PATH);
        directory.mkdir();

        String filename = Label + System.currentTimeMillis() + ".jpg";
        File outfile=new File(directory,filename);

        try {
            outputStream=new FileOutputStream(outfile);
            captureImage.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            outputStream.flush();
            outputStream.close();

            broadcastImageOperation(outfile);

        } catch (FileNotFoundException e) {
            Log.e("Filenotfound",e.getMessage());

        } catch (IOException e) {
            Log.e("Fileexception",e.getMessage());
        }

        Snackbar.make(findViewById(android.R.id.content), "Image Saved!",
                Snackbar.LENGTH_SHORT).setTextColor(Color.YELLOW).show();
    }
}

//[Extra code]:
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new ContactFragment()).commit();
//            mNavigationView.setCheckedItem(R.id.contactus);
//        }