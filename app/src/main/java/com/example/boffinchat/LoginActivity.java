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


public class LoginActivity extends AppCompatActivity {


    private Button loginButton,phoneLoginButton;
    private EditText  userEmail,userPassword;
    private TextView  needNewAccountLink,forgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        InitializeFields();

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }

    private void allowUserToLogin() {


        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please Enter an Email.",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please Enter an Password.",Toast.LENGTH_SHORT).show();

        }

        else{

            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if(task.isSuccessful()){


                        SendUserToMainActivity();
                        Toast.makeText(LoginActivity.this,"Logged in Successfully",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                    else{
                        String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error:"+ message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                }
            });

        }
    }

    private void SendUserToMainActivity() {

        Intent mainIntent=new Intent(LoginActivity.this, MainActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void InitializeFields() {

        loginButton=(Button) findViewById(R.id.login_button);
        phoneLoginButton=(Button) findViewById(R.id.phone_login_button);
        userEmail=(EditText) findViewById(R.id.login_email);
        userPassword=(EditText) findViewById(R.id.login_password);
        needNewAccountLink=(TextView)findViewById(R.id.need_new_account_link);

        loadingBar=new ProgressDialog(this);
    }



    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(LoginActivity.this, MainActivity.class);
        startActivity(loginIntent);
    }
    private void SendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
