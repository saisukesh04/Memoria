package com.example.memoria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.example.memoria.newMemory.NewMemoryActivity;
import com.example.memoria.signup.SettingsActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public static FirebaseAuth mAuth;
    private DatabaseReference mRef;
    public String userId = null;
    public static String userName;
    public static String mainProfileImage;
    public static Uri mainImageURI = null;

    private SharedPreferences modeSetting;
    private SharedPreferences.Editor modeEdit;
    private boolean nightMode;

    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modeSetting = getSharedPreferences("ModeSetting",0);
        modeEdit = modeSetting.edit();
        nightMode = modeSetting.getBoolean("NightMode",false);

        if(nightMode) {
            Toast.makeText(this, "Dark Mode On", Toast.LENGTH_SHORT).show();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        mAuth = FirebaseAuth.getInstance();

        mInterstitialAd = new InterstitialAd(this);
        MobileAds.initialize(this, getString(R.string.ad_app_id));
        loadInterstitialAd();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton addMemoryFAB = findViewById(R.id.addMemoryFAB);
        addMemoryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.i("Advertisement Info: ", "The interstitial ad wasn't loaded yet.");
                    startActivity(new Intent(MainActivity.this, NewMemoryActivity.class));
                }
            }
        });

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("Error", "Loading Advertisement");
            }

            @Override
            public void onAdClosed() {
                startActivity(new Intent(MainActivity.this, NewMemoryActivity.class));
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void loadInterstitialAd() {
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_id_test));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            case R.id.action_change_mode:
                if(!nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    nightMode = true;
                    modeEdit.putBoolean("NightMode",true);
                    modeEdit.apply();
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    nightMode = false;
                    modeEdit.putBoolean("NightMode",false);
                    modeEdit.apply();
                }
                return true;

            case R.id.action_logout:
                mAuth.signOut();
                Toast.makeText(this, "You have successfully Signed-out", Toast.LENGTH_SHORT).show();
                sendToLogin();
                return true;

            default: return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();

        if (currentUser == null) {
            sendToLogin();
        }else{
            userId = mAuth.getCurrentUser().getUid();

            mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(userId)){

                        Map<String,String> retrieveMap = (Map<String, String>) dataSnapshot.child(userId).getValue();
                        mainProfileImage = retrieveMap.get("Image");
                        mainImageURI = Uri.parse(mainProfileImage);
                        userName = retrieveMap.get("Name");
                    }else{
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this,"Error" + databaseError,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendToLogin() {
        userName = null;
        mainProfileImage = null;
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
