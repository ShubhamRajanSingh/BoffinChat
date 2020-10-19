package com.example.boffinchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccount;
    private EditText userEmail,userPassword;
    private TextView alreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        alreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

    }

    private void createNewAccount() {

        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please Enter an Email.",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please Enter an Password.",Toast.LENGTH_SHORT).show();

        }

        else{
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){



                        String currentUserID=mAuth.getCurrentUser().getUid();
                        rootRef.child("Users").child(currentUserID).setValue("");
                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this,"Account Created successfully",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this,"Error:"+ message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }
    }


    private void SendUserToMainActivity() {

        Intent mainIntent=new Intent(RegisterActivity.this, MainActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {

        createAccount=(Button) findViewById(R.id.register_button);
        loadingBar=new ProgressDialog(this);
        userEmail=(EditText) findViewById(R.id.register_email);
        userPassword=(EditText) findViewById(R.id.register_password);
        alreadyHaveAccountLink=(TextView)findViewById(R.id.already_have_account_link);

    }
}
