package com.example.memoria.newMemory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.memoria.R;
import com.example.memoria.maps.MapsActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMemoryActivity extends AppCompatActivity {

    @BindView(R.id.radioButton1) RadioButton image;
    @BindView(R.id.radioButton2) RadioButton video;
    @BindView(R.id.radioButton3) RadioButton text;
    @BindView(R.id.radioButton4) RadioButton location;
    @BindView(R.id.radioGroup) RadioGroup radioGroup;
    @BindView(R.id.newMemoryContinue) Button newMemoryContinue;

    final public static int IMAGE_CODE = 1;
    final public static int VIDEO_CODE = 2;
    final public static int AUDIO_CODE = 3;
    final public static int LOCATION_CODE = 4;

    private FirebaseStorage mStorage;
    private StorageReference storagePath;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memory);

        ButterKnife.bind(this);

        mStorage = FirebaseStorage.getInstance();
        newMemoryContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(NewMemoryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(NewMemoryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    if(selectedId == R.id.radioButton1){
                        selectImage();
                    }else if(selectedId == R.id.radioButton2){
                        selectVideo();
                    }else if(selectedId == R.id.radioButton3){
                        selectAudio();
                    }else if(selectedId == R.id.radioButton4){
                        startActivity(new Intent(NewMemoryActivity.this, MapsActivity.class));
                    }else{
                        Snackbar.make(v, "Please choose any one of the options", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_CODE);
    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_CODE);
    }

    private void selectAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, AUDIO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Intent intent = new Intent(NewMemoryActivity.this, StoreMemory.class);
            intent.putExtra("URI", imageUri.toString());
            intent.putExtra("type", IMAGE_CODE);
            startActivity(intent);
        }else if (requestCode == VIDEO_CODE && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            Intent intent = new Intent(NewMemoryActivity.this, StoreMemory.class);
            intent.putExtra("URI", videoUri.toString());
            intent.putExtra("type", VIDEO_CODE);
            startActivity(intent);
        } else if (requestCode == AUDIO_CODE && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            Intent intent = new Intent(NewMemoryActivity.this, StoreMemory.class);
            intent.putExtra("URI", audioUri.toString());
            intent.putExtra("type", AUDIO_CODE);
            startActivity(intent);
        } else {
            Toast.makeText(NewMemoryActivity.this, "Please select a file", Toast.LENGTH_SHORT).show();
        }
    }
}
