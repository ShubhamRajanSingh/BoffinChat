package com.example.boffinchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.DragStartHelper;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView  mScrollView;
    private TextView displayTextMessages;
    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,groupNameRef,groupMessageKeyRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName=getIntent().getExtras().get("groupName").toString();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);






        InitializeFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageInfoToDatabase();

                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator=dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){

            String chatDate=(String) (((DataSnapshot)iterator.next()).getValue());
            String chatMessage=(String) (((DataSnapshot)iterator.next()).getValue());
            String chatName=(String) (((DataSnapshot)iterator.next()).getValue());
            String chatTime=(String) (((DataSnapshot)iterator.next()).getValue());



            //    /-------------------------------------------------------------------------\
            //   /---------------------------------------------------------------------------\
            //  /-----------------------------------------------------------------------------\
            //
            //Uncomment the below Line of code and Comment the next line if you want to see the messages with date and time

//            displayTextMessages.append(chatName+":\n"+ chatMessage+"\n"+chatTime+"|"+chatDate+"\n\n\n");
            displayTextMessages.append(chatName+":\n"+ chatMessage+"\n\n\n");







            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }

    }

    private void saveMessageInfoToDatabase() {

        String message=userMessageInput.getText().toString();
        String messageKey=groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {

        }
        else{
            Calendar ccalForDate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM,dd,yyyy");
            currentDate=currentDateFormat.format(ccalForDate.getTime());

            Calendar ccalForTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm:ss a");

            currentTime=currentTimeFormat.format(ccalForTime.getTime());


            HashMap<String,Object> groupMessageKey=new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);



        }
        }


    private void getUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void InitializeFields() {

        mToolbar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton=(ImageButton)findViewById(R.id.send_message_button);
        userMessageInput=(EditText) findViewById(R.id.input_group_message);
        displayTextMessages=(TextView) findViewById(R.id.group_chat_text_display);

        mScrollView=(ScrollView)findViewById(R.id.my_scroll_view);

    }
}
