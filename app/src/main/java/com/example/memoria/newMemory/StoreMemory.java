package com.example.memoria.newMemory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.memoria.newMemory.NewMemoryActivity.AUDIO_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.IMAGE_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.LOCATION_CODE;
import static com.example.memoria.newMemory.NewMemoryActivity.VIDEO_CODE;

public class StoreMemory extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.memoryVideoView) PlayerView exoPlayerView;
    @BindView(R.id.memoryImageView) ImageView imageView;
    @BindView(R.id.memoryDesc) EditText memoryDesc;
    @BindView(R.id.postButton) Button postButton;
    @BindView(R.id.mapLayout) LinearLayout mapLayout;

    private int type;
    private float size;
    private String uriStr;
    LatLng marker;
    Uri uriFinal;
    SimpleExoPlayer exoPlayer;
    MediaSource mediaSource;
    TrackSelector trackSelector;
    GoogleMap googleMap;

    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private StorageReference storagePath;
    private Uri downloadUrl;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_memory);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        uriStr = intent.getStringExtra("URI");
        type = intent.getIntExtra("type", LOCATION_CODE);
        Log.i("Info: ", "Type" + type);

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        if(type == LOCATION_CODE) {
            marker = bundle.getParcelable("marker");
            mapLayout.setVisibility(View.VISIBLE);
        }

        if (type != LOCATION_CODE) {
            uriFinal = Uri.parse(uriStr);
            size = Float.parseFloat(getSizeFromUri(getApplicationContext(), uriFinal)) / (1024 * 1024);
        } else {
            size = 0;
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
            mapFragment.getMapAsync(this);
        }

        if (size > 10) {
            Toast.makeText(this, "Size of the file exceeds 10MB !", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (type == IMAGE_CODE) {
                imageView.setVisibility(View.VISIBLE);
                Glide.with(StoreMemory.this).load(uriFinal).into(imageView);
            } else if (type == VIDEO_CODE) {
                exoPlayerView.setVisibility(View.VISIBLE);
                playVideoAudio(uriFinal);
            } else if (type == AUDIO_CODE) {
                exoPlayerView.setVisibility(View.VISIBLE);
                playVideoAudio(uriFinal);
            } else if (type == LOCATION_CODE) {
                uriStr = marker.latitude + "," + marker.longitude;
            } else {
                Toast.makeText(StoreMemory.this, "An error has occurred. Please try again", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = memoryDesc.getText().toString();
                if(!TextUtils.isEmpty(desc)){
                    if(type != LOCATION_CODE)
                        uploadMemory(uriFinal, desc);
                    else
                        uploadLocationMemory(uriStr, desc);
                }else{
                    Snackbar.make(v, "Give a description of the memory", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.addMarker(new MarkerOptions().position(marker).title("Your Location")).showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 10));
    }

    private String getSizeFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Audio.Media.SIZE};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void playVideoAudio(Uri uri) {
        trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(StoreMemory.this, trackSelector, loadControl);
        exoPlayerView.setPlayer(exoPlayer);

        String userAgent = Util.getUserAgent(StoreMemory.this, "MemoryVideo");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(StoreMemory.this, userAgent);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    private void uploadMemory(Uri uri, String desc) {

        LinearLayout uploadProgress = findViewById(R.id.uploadProgress);

        uploadProgress.setVisibility(View.VISIBLE);
        String currentUNIX = String.valueOf(System.currentTimeMillis());
        storagePath = storageReference.child("Memories").child(userId + " " + currentUNIX);
        UploadTask uploadMemoryTask = storagePath.putFile(uri);

        uploadMemoryTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(StoreMemory.this, "An error has occurred. Please try again!", Toast.LENGTH_SHORT).show();
                uploadProgress.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("StoreMemory: ", "Memory Upload Successful");
            }
        });
        uploadMemoryTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storagePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                    Log.i("StoreMemory:", "The Memory URL :" + downloadUrl.toString());

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("Description", desc);
                    userMap.put("Link", downloadUrl.toString());
                    userMap.put("TimeStamp", currentUNIX);
                    userMap.put("Username", userId);
                    userMap.put("Type", String.valueOf(type));

                    mRef.child("Memories").child(currentUNIX).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(StoreMemory.this, "Memory Uploaded", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(StoreMemory.this, MainActivity.class));
                                finish();

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(StoreMemory.this, "(FIREBASE Error): " + error, Toast.LENGTH_SHORT).show();
                                uploadProgress.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void uploadLocationMemory(String uriStr, String desc) {

        LinearLayout uploadProgress = findViewById(R.id.uploadProgress);

        uploadProgress.setVisibility(View.VISIBLE);
        String currentUNIX = String.valueOf(System.currentTimeMillis());
        storagePath = storageReference.child("Memories").child(userId + " " + currentUNIX);

        Map<String, String> userMap = new HashMap<>();
        userMap.put("Description", desc);
        userMap.put("Link", uriStr);
        userMap.put("TimeStamp", currentUNIX);
        userMap.put("Username", userId);
        userMap.put("Type", String.valueOf(type));

        mRef.child("Memories").child(currentUNIX).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(StoreMemory.this, "Memory Uploaded", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StoreMemory.this, MainActivity.class));
                    finish();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(StoreMemory.this, "(FIREBASE Error): " + error, Toast.LENGTH_SHORT).show();
                    uploadProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (type == VIDEO_CODE || type == AUDIO_CODE) {
            exoPlayer.release();
            exoPlayer = null;
            mediaSource = null;
            trackSelector = null;
        }
    }
}
