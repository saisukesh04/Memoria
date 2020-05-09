package com.example.memoria.signup;

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
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.memoria.MainActivity;
import com.example.memoria.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import static com.example.memoria.MainActivity.mAuth;
import static com.example.memoria.MainActivity.mainImageURI;
import static com.example.memoria.MainActivity.mainProfileImage;
import static com.example.memoria.MainActivity.userName;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.profileImage) CircleImageView profileImage;
    @BindView(R.id.username) TextInputEditText username;
    @BindView(R.id.saveBtn) Button saveBtn;

    private StorageReference storageReference;
    private StorageReference imagePath;
    private DatabaseReference mRef;
    private String userId;

    private Uri downloadUrl;
    private String usernameText;
    private ProgressDialog savingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        boolean firstTime = intent.getBooleanExtra("firstTime", false);

        mRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        savingProgress = new ProgressDialog(SettingsActivity.this);
        userId = mAuth.getCurrentUser().getUid();

        if(userName == null || firstTime) {
            Toast.makeText(SettingsActivity.this, "Please complete the setup", Toast.LENGTH_SHORT).show();
        }else {
            username.setText(userName);
            Glide.with(SettingsActivity.this).load(mainProfileImage).into(profileImage);
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else{
                        bringImageSelection();
                    }
                }else{
                    bringImageSelection();
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usernameText = username.getText().toString();
                if(!TextUtils.isEmpty(usernameText) && mainImageURI != null){
                    savingProgress.setMessage("Saving details. Please wait...");
                    savingProgress.show();

                    imagePath = storageReference.child("Profile_Images").child("Users").child(userId+".jpg");
                    UploadTask uploadTask = imagePath.putFile(mainImageURI);
                    storeInFirebase(uploadTask,usernameText);

                }else if(TextUtils.isEmpty(usernameText)){
                    Toast.makeText(SettingsActivity.this,"Please choose a USERNAME",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SettingsActivity.this,"Please choose a Profile Picture",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeInFirebase(UploadTask uploadTask,final String userName) {
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(SettingsActivity.this,"Error: " + exception,Toast.LENGTH_SHORT).show();
                //TODO: Add loading message
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("SettingsActivity","Upload Successful");
            }
        });
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imagePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    downloadUrl = task.getResult();
                    Log.i("Information SA:","The URL : " + downloadUrl.toString());

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("Name",usernameText);
                    userMap.put("Image",downloadUrl.toString());

                    mRef.child("Users").child(userId).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                savingProgress.dismiss();
                                Toast.makeText(SettingsActivity.this,"Changes Saved",Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();

                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this,"(FIREBASE Error): "+error,Toast.LENGTH_SHORT).show();
                                //TODO: Add loading message
                            }
                        }
                    });
                }
            }
        });
    }

    private void bringImageSelection() {
        saveBtn.setAlpha(1);
        saveBtn.setEnabled(true);
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SettingsActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                profileImage.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this,error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
