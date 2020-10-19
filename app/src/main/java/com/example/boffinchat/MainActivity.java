package com.example.boffinchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


     private Toolbar mToolbar;
     private ViewPager myViewPager;
     private TabLayout myTabLayout;
     private TabsAccessorAdapter myTabAccessorAdapter;

     private FirebaseAuth mAuth;
     private DatabaseReference rootRef;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        rootRef= FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("BoffinChat");
        myViewPager=(ViewPager) findViewById(R.id.main_tab_pager);
        myTabAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);

       myTabLayout=(TabLayout) findViewById(R.id.main_tabs);
       myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser=mAuth.getCurrentUser();
        if(currentuser==null){
            SendUserToLoginActivity();
        }
        else{

            updateUserStatus("online");
            verifyUserExistance();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentuser=mAuth.getCurrentUser();
        if(currentuser!=null){
            updateUserStatus("offline");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentuser=mAuth.getCurrentUser();
        if(currentuser!=null){
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistance() {
            String currentUserId=mAuth.getCurrentUser().getUid();
            rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if((dataSnapshot.child("name").exists())){
                        Toast.makeText(MainActivity.this,"Namaste :)",Toast.LENGTH_SHORT).show();
                    }
                    else{
                       SendUserToSettingsActivity();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);


        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);


        if(item.getItemId()==R.id.main_logout_option){

            updateUserStatus("offline");

             mAuth.signOut();
            SendUserToLoginActivity();
        }

        if(item.getItemId()==R.id.main_create_group_option){
             requestNewGroup();
        }

        if(item.getItemId()==R.id.main_settings_option){
            SendUserToSettingsActivity();

        }

        if(item.getItemId()==R.id.main_find_friends_option){

            SendUserToFindFriendActivity();
        }

        return true;
    }

    private void SendUserToFindFriendActivity() {
        Intent loginIntent=new Intent(MainActivity.this, FindFriendsActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }

    private void requestNewGroup() {

        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");
        final EditText groupNameField=new EditText(MainActivity.this);

        groupNameField.setHint("e.g Boffin Chat");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName=groupNameField.getText().toString();

                if(groupName.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please write Group Name",Toast.LENGTH_SHORT).show();
                }
                else{

                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {

        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupName+" group is created successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToLoginActivity(){
        Intent loginIntent=new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToSettingsActivity(){
        Intent settingsIntent=new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(settingsIntent);

    }

 private void updateUserStatus(String state){
        String saveCurrentTime,saveCurrentDate;
     String currentUserId=mAuth.getCurrentUser().getUid();

     Calendar calendar=Calendar.getInstance();
     SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
     saveCurrentDate=currentDate.format(calendar.getTime());
     SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
     saveCurrentTime=currentTime.format(calendar.getTime());
     HashMap<String, Object> onlineStateMap = new HashMap<>();
     onlineStateMap.put("time", saveCurrentTime);
     onlineStateMap.put("date", saveCurrentDate);
     onlineStateMap.put("state", state);

     rootRef.child("Users").child(currentUserId).child("userState")
             .updateChildren(onlineStateMap);

 }




 }












