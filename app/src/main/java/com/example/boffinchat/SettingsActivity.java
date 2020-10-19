package com.example.boffinchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private Button updateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfilImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private static final int gallaryImage=1;
    private StorageReference userProfileImageRef;
    private Toolbar SettingsToolBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        Initialize();



        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateSettings();
            }
        });

        retrieveUserInfo();
        userProfilImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallaryImage);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==gallaryImage && resultCode==RESULT_OK && data!=null){
            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){

                Uri resultUri=result.getUri();
                StorageReference filePath=userProfileImageRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                rootRef.child("Users").child(currentUserId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(SettingsActivity.this,"Image Saved",Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            String message=task.getException().toString();
                                            Toast.makeText(SettingsActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String message=exception.toString();

                        Toast.makeText(SettingsActivity.this,"Error Occurred:"+message,Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }
    }

    private void retrieveUserInfo() {

        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()  && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                    String retrieveUsername=dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                    String retrieveImage=dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUsername);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveImage).into(userProfilImage);
                }
                else if(dataSnapshot.exists()  && dataSnapshot.hasChild("name")){

                    String retrieveUsername=dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus=dataSnapshot.child("status").getValue().toString();


                    userName.setText(retrieveUsername);
                    userStatus.setText(retrieveStatus);

                }
                else{
                    Toast.makeText(SettingsActivity.this,"Please enter your Username and Status",Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void Initialize() {

        updateAccountSettings=(Button) findViewById(R.id.setting_update_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_user_status);
        userProfilImage=(CircleImageView)findViewById(R.id.profile_image);
        SettingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }


    private void SendUserToMainActivity() {

        Intent mainIntent=new Intent(SettingsActivity.this, MainActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void updateSettings() {
       String setUsername=userName.getText().toString();
       String setStatus=userStatus.getText().toString();

       if(TextUtils.isEmpty(setUsername)){
           Toast.makeText(this,"Please write a Username",Toast.LENGTH_SHORT).show();
       }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this,"Please write a Status",Toast.LENGTH_SHORT).show();
        }
        else{

            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name",setUsername);
            profileMap.put("status",setStatus);

            rootRef.child("Users").child(currentUserId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();

                    }
                    else{
                        String message=task.getException().toString();
                        Toast.makeText(SettingsActivity.this,"Error: "+message,Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


    }
}
