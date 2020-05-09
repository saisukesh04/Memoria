package com.example.memoria.newMemory;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewMemoryActivity extends AppCompatActivity {

    @BindView(R.id.radioButton1) RadioButton image;
    @BindView(R.id.radioButton2) RadioButton video;
    @BindView(R.id.radioButton3) RadioButton text;
    @BindView(R.id.radioButton4) RadioButton location;
    @BindView(R.id.radioGroup) RadioGroup radioGroup;
    @BindView(R.id.newMemoryContinue) Button newMemoryContinue;

    private FirebaseStorage mStorage;
    private StorageReference storagePath;
    private Uri videoUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memory);

        ButterKnife.bind(this);

        mStorage= FirebaseStorage.getInstance();
        newMemoryContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(NewMemoryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(NewMemoryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    selectVideo();
                }
            }
        });
    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            Intent testIntent = new Intent(NewMemoryActivity.this, StoreMemory.class);
            testIntent.putExtra("URI-vid", videoUri.toString());
            startActivity(testIntent);
            //uploadUri(videoUri);
        } else {
            Toast.makeText(NewMemoryActivity.this, "Please select a file", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadUri(Uri uri) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading File...");
        storagePath = mStorage.getReference().child("Memories");

        storagePath.child(String.valueOf(System.currentTimeMillis())).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = storagePath.getDownloadUrl().toString();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewMemoryActivity.this, "File has not uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

            }
        });

    }
}
