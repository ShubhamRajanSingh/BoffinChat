package com.example.boffinchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCode,VerifyButton;
    private EditText inputNumber,inputCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    private String mVerificationId;
    private ProgressDialog loadingBar;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);
        SendVerificationCode=(Button) findViewById(R.id.send_code_button);
        VerifyButton=(Button) findViewById(R.id.verify_button);
        inputNumber=(EditText) findViewById(R.id.phone_number_input);
        inputCode=(EditText)findViewById(R.id.phone_verification_input);
        SendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendVerificationCode.setVisibility(View.INVISIBLE);
                inputCode.setVisibility(View.VISIBLE);
                VerifyButton.setVisibility(View.VISIBLE);

                String phoneNumber=inputNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this,"Please Enter a number with country code",Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callBacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCode.setVisibility(View.INVISIBLE);
                inputCode.setVisibility(View.VISIBLE);
                String verificationCode=inputCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this,"Please Enter the Code",Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("please wait....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                 signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this,"Invalid Phone Number",Toast.LENGTH_SHORT).show();
                SendVerificationCode.setVisibility(View.VISIBLE);
                inputCode.setVisibility(View.INVISIBLE);
                VerifyButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

               loadingBar.dismiss();
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this,"Verification Code Sent",Toast.LENGTH_SHORT).show();


            }
        };


    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"You are logged in successfully :)",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();


                        } else {
                               String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error :"+ message,Toast.LENGTH_SHORT).show();

                            }
                        }

                });
    }

    private void sendUserToMainActivity() {

        Intent mainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }

}
