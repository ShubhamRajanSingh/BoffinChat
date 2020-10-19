package com.example.boffinchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private String receiverUserId,currentState,sendUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton,declineRequestButton;
    private DatabaseReference userRef,chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        receiverUserId=getIntent().getExtras().get("visitUserId").toString();
        notificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");

        userProfileImage=(CircleImageView) findViewById(R.id.visit_profle_image);
        userProfileName=(TextView) findViewById(R.id.visit_username);
        userProfileStatus=(TextView) findViewById(R.id.vist_profile_status);
        SendMessageRequestButton=(Button) findViewById(R.id.send_message_request_button);
        declineRequestButton=(Button) findViewById(R.id.decline_message_request_button);
        currentState="new";
        sendUserId=mAuth.getCurrentUser().getUid();



        retriveUserInfo();



    }

    private void retriveUserInfo() {

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("image")){

                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.mipmap.profile_round).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatrequest();

                }
                else{


                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();



                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageChatrequest();

                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void manageChatrequest() {

        chatRequestRef.child(sendUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(receiverUserId)){
                    String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){

                        currentState="request_sent";
                        SendMessageRequestButton.setText("Cancel Chat Request");

                    }
                    else if(request_type.equals("received")){
                        currentState="request_received";
                        SendMessageRequestButton.setText("Accept Chat Request");
                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setEnabled(true);
                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });

                    }
                }
                else{

                    contactsRef.child(sendUserId).addListenerForSingleValueEvent(new ValueEventListener() {


                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            if(dataSnapshot.hasChild(receiverUserId)){
                                currentState="friends";
                                SendMessageRequestButton.setText("Remove Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
            if(!sendUserId.equals(receiverUserId)){
                 SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         SendMessageRequestButton.setEnabled(false);
                         if(currentState.equals("new")){
                             sendChatRequest();
                         }

                         if(currentState.equals("request_sent")){

                             cancelChatRequest();
                         }
                         if(currentState.equals("request_received")){

                             acceptChatRequest();
                         }
                         if(currentState.equals("friends")){

                             removeSpecificContact();
                         }
                     }


                 });
            }
            else{
                SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void removeSpecificContact() {
        contactsRef.child(sendUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    contactsRef.child(receiverUserId).child(sendUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                currentState="new";
                                SendMessageRequestButton.setText("Send Request");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);
                            }

                        }
                    });

                }

            }
        });


    }

    private void acceptChatRequest() {

        contactsRef.child(sendUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    contactsRef.child(receiverUserId).child(sendUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

//                                contactsRef.child(sendUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                        if(task.isSuccessful()){
//                                            contactsRef.child(receiverUserId).child(sendUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                 SendMessageRequestButton.setEnabled(true);
//                                                 currentState="friends";
//                                                 SendMessageRequestButton.setText("Remove Contacts");
//                                                 declineRequestButton.setVisibility(View.INVISIBLE);
//                                                 declineRequestButton.setEnabled(false);
//                                                }
//                                            });
//                                        }
//                                    }
//                                });

                                                 cancelChatRequest();

                                                 SendMessageRequestButton.setEnabled(true);
                                                 currentState="friends";
                                                 SendMessageRequestButton.setText("Remove Contacts");
                                                 declineRequestButton.setVisibility(View.INVISIBLE);
                                                 declineRequestButton.setEnabled(false);
                            }


                        }
                    });
                }


            }
        });

    }

    private void cancelChatRequest() {
        chatRequestRef.child(sendUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    chatRequestRef.child(receiverUserId).child(sendUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                currentState="new";
                                SendMessageRequestButton.setText("Send Request");
                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);
                            }

                        }
                    });

                }

            }
        });

    }

    private void sendChatRequest() {

        chatRequestRef.child(sendUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserId).child(sendUserId)
                                    .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){




                                        SendMessageRequestButton.setEnabled(true);
                                        currentState="request_sent";
                                        SendMessageRequestButton.setText("Cancel Chat Request");
                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                        declineRequestButton.setEnabled(false);
                                    }
                                }
                            });
                        }

                    }
                });
    }
}
