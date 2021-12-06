package com.padmajeet.mgi.techforedu.parent;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityForgotPassword extends AppCompatActivity {
    private Button btnSubmit,btnVerify;
    private String NewPassword;
    private String ReEnterPassword;
    private EditText etNewPassword;
    private EditText etReEnterPassword;
    private TextView reSendOTP;
    private SweetAlertDialog pDialog;
    private Parent loggedInUser;
    private String loggedInUserId;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference parentCollectionRef=db.collection("Parent");
    private FirebaseAuth mAuth;
    private String verificationCode;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Gson gson;
    private PinView pinView;
    private LinearLayout llOTP,llResetPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        pDialog=Utility.createSweetAlertDialog(ActivityForgotPassword.this);
        sessionManager = new SessionManager(ActivityForgotPassword.this);
        gson = Utility.getGson();

        llResetPassword = findViewById(R.id.llResetPassword);
        llOTP = findViewById(R.id.llOTP);
        pinView = findViewById(R.id.pinview);
        etNewPassword = findViewById(R.id.etNewPassword);
        etReEnterPassword = findViewById(R.id.etReEnterPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnVerify = findViewById(R.id.btnVerify);
        reSendOTP = findViewById(R.id.reSendOTP);

        String loggedInUserJson = sessionManager.getString("loggedInUser");
        System.out.println("loggedInUserJson - " + loggedInUserJson);
        loggedInUser = gson.fromJson(loggedInUserJson, Parent.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");

        sendOTP();

        reSendOTP.setPaintFlags(reSendOTP.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = pinView.getText().toString();
                System.out.println(content);
                verifyVerificationCode(content);
            }
        });

        reSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOTP();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                NewPassword = etNewPassword.getText().toString();
                if(NewPassword.length()<6){
                    etNewPassword.setError("Minimum password length should be 6");
                    etNewPassword.requestFocus();
                    return;
                }
                ReEnterPassword = etReEnterPassword.getText().toString();
                if(ReEnterPassword.length()<6){
                    etReEnterPassword.setError("Minimum confirm password length should be 6");
                    etReEnterPassword.requestFocus();
                    return;
                }
                if (loggedInUser != null && NewPassword.equals(ReEnterPassword)) {
                    if(!pDialog.isShowing() && pDialog != null){
                        pDialog.show();
                    }
                    loggedInUser.setPassword(NewPassword);
                    loggedInUser.setStatus("A");
                    loggedInUser.setModifiedDate(new Date());

                    parentCollectionRef.document(loggedInUserId).set(loggedInUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(pDialog.isShowing() && pDialog != null){
                                pDialog.dismiss();
                            }
                            if (task.isSuccessful()) {
                                sessionManager.remove("loggedInUser");
                                sessionManager.remove("loggedInUserId");
                                Toast.makeText(ActivityForgotPassword.this, "Updated Successfully",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ActivityForgotPassword.this, ActivityLogin.class);
                                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                startActivity(intent);
                                finish();
                            } else {
                                System.out.println("Error getting documents: -" + task.getException());
                                Toast.makeText(ActivityForgotPassword.this, "Not successfully updated. Try it again!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Passwords don't match! please enter again!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }

    private void sendOTP() {
        if(loggedInUser != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91" + loggedInUser.getMobileNumber(),                     // Phone number to verify
                    60,                           // Timeout duration
                    TimeUnit.SECONDS,                // Unit of timeout
                    ActivityForgotPassword.this,        // Activity (for callback binding)
                    mCallback);  // OnVerificationStateChangedCallbacks
        }
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            verificationCode = phoneAuthCredential.getSmsCode();
            System.out.println("verificationCode "+verificationCode);
        }
        @Override
        public void onVerificationFailed(FirebaseException e) {
            System.out.println("e.getMessage() "+e.getMessage());
            Toast.makeText(ActivityForgotPassword.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
        }
    };

    private void verifyVerificationCode(String code) {
        if(!pDialog.isShowing() && pDialog != null){
            pDialog.show();
        }
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ActivityForgotPassword.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(pDialog.isShowing() && pDialog != null){
                            pDialog.dismiss();
                        }
                        if (task.isSuccessful()) {
                            llOTP.setVisibility(View.GONE);
                            llResetPassword.setVisibility(View.VISIBLE);
                        } else {
                            //verification unsuccessful.. display an error message
                            String message = "Something is wrong, we will fix it soon...";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }
                            Toast.makeText(ActivityForgotPassword.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
