package com.padmajeet.mgi.techforedu.parent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivitySplashScreen extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Gson gson = Utility.getGson();
    SweetAlertDialog pDialog;
    SessionManager sessionManager;
    DocumentReference parentDocRef;
    Parent loggedInUser;
    String loggedInUserId;
    String parentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sessionManager = new SessionManager(ActivitySplashScreen.this);
        parentId = sessionManager.getString("loggedInUserId");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!TextUtils.isEmpty(parentId)) {
                    validateParent(parentId);
                }
                else {
                    Intent i = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    startActivity(i);
                    finish();
                }
            }
        }, 1000);
    }

    private void validateParent(String documentId) {
        parentDocRef = db.document("Parent/" + documentId);
        parentDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        System.out.println("Data -key -" + documentSnapshot.getId() + " value -" + documentSnapshot.getData());
                        loggedInUserId = documentSnapshot.getId();
                        loggedInUser = documentSnapshot.toObject(Parent.class);
                        System.out.println("Data -key -" + documentSnapshot.getId() + " value -" + loggedInUser);
                        System.out.println("loggedInUser -" + loggedInUser.getFirstName());
                        SessionManager sessionManager = new SessionManager(ActivitySplashScreen.this);
                        String sessionLoginStr = sessionManager.getString("loggedInUser");
                        Parent sessionUser = gson.fromJson(sessionLoginStr, Parent.class);

                        if (loggedInUser != null && loggedInUser.getStatus().equals("A")) {
                            sessionManager.putString("loggedInUser", gson.toJson(loggedInUser));
                            sessionManager.putString("loggedInUserId", loggedInUserId);
                            sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                            Intent intent = new Intent(ActivitySplashScreen.this, ActivityHome.class);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent i = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            startActivity(i);
                            finish();
                        }

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Intent i = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        startActivity(i);
                        finish();
                    }
                });

    }

}
