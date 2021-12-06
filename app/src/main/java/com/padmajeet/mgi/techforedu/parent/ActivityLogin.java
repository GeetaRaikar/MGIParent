package com.padmajeet.mgi.techforedu.parent;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.AcademicYear;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Institute;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActivityLogin extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String mobileNumber, password;
    private EditText etMobileNumber, etPassword;
    private Button btnSubmit, btnVerify, btnLogin;
    private LinearLayout llOTP, llLogin;
    private SweetAlertDialog pDialog;
    private TextView tvFor;
    private Parent loggedInUser;
    private String loggedInUserId;
    private Gson gson = Utility.getGson();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference parentCollectionRef = db.collection("Parent");
    private CollectionReference instituteCollectionRef = db.collection("Institute");
    private CollectionReference academicYearCollectionRef = db.collection("AcademicYear");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private DocumentReference studentDocRef;
    private DocumentReference instituteDocRef;
    private Student student;
    private Institute institute;
    private String academicYearId;
    private AcademicYear academicYear;
    private String parentId;
    private SessionManager sessionManager;
    private TextView tvName;
    private ImageView appIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        gson = Utility.getGson();
        sessionManager = new SessionManager(ActivityLogin.this);
        pDialog = Utility.createSweetAlertDialog(ActivityLogin.this);

        tvName = findViewById(R.id.tvName);
        appIcon = findViewById(R.id.appIcon);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnLogin = findViewById(R.id.btnLogin);
        llLogin = findViewById(R.id.llLogin);
        etPassword = findViewById(R.id.etPassword);
        tvFor = findViewById(R.id.tvFor);

        parentId = sessionManager.getString("loggedInUserId");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobileNumber = etMobileNumber.getText().toString().trim();
                if (Utility.isValidPhone(mobileNumber)) {
                    getParentObject();
                } else {
                    etMobileNumber.setError(getString(R.string.errInvalidMobNum));
                    etMobileNumber.requestFocus();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                password = etPassword.getText().toString().trim();
                if (loggedInUser.getPassword().equals(password)) {
                    gson = Utility.getGson();
                    String loggedInAdminStr = gson.toJson(loggedInUser);
                    sessionManager.putString("loggedInUser", loggedInAdminStr);
                    sessionManager.putString("loggedInUserId", loggedInUserId);
                    sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                    Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    startActivity(intent);
                    finish();
                } else {
                    etPassword.setError(getString(R.string.errInvalidPassword));
                    etPassword.requestFocus();
                }
            }
        });
        tvFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.putString("loggedInUser", gson.toJson(loggedInUser));
                sessionManager.putString("loggedInUserId", loggedInUserId);
                Intent intent = new Intent(ActivityLogin.this, ActivityForgotPassword.class);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                startActivity(intent);
                finish();
            }
        });

    }

    private void getParentObject() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }

        parentCollectionRef
                .whereEqualTo("mobileNumber", mobileNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        System.out.println("task "+task.getResult().size());
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                System.out.println("task.getResult().isEmpty()  "+task.getResult().isEmpty());
                                etMobileNumber.setError(getString(R.string.errRegisterMobNum));
                                etMobileNumber.requestFocus();
                                return;
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                loggedInUserId = document.getId();
                                loggedInUser = document.toObject(Parent.class);
                                loggedInUser.setId(loggedInUserId);
                            }
                            if (loggedInUser == null) {
                                System.out.println("loggedInUser  "+loggedInUser.getFirstName());
                                etMobileNumber.setError(getString(R.string.errRegisterMobNum));
                                etMobileNumber.requestFocus();
                                return;
                            } else {
                                getCurrentAcademicYear();
                                if (loggedInUser.getStatus().equals("F")) {
                                    sessionManager.putString("loggedInUser", gson.toJson(loggedInUser));
                                    sessionManager.putString("loggedInUserId", loggedInUserId);
                                    sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                                    Intent intent = new Intent(ActivityLogin.this, ActivityForgotPassword.class);
                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                    startActivity(intent);
                                    finish();
                                } else if (!loggedInUser.getStatus().equals("A")) {
                                    etMobileNumber.setError("Admin has deactivated your account");
                                    etMobileNumber.requestFocus();
                                    return;
                                } else {
                                    btnSubmit.setVisibility(View.GONE);
                                    llLogin.setVisibility(View.VISIBLE);
                                }
                                tvName.setText("Hi " + loggedInUser.getFirstName() + "!");
                                instituteDocRef = instituteCollectionRef.document("/" + loggedInUser.getInstituteId());
                                instituteDocRef
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                institute = documentSnapshot.toObject(Institute.class);
                                                String imageUrl = institute.getLogoImagePath();
                                                if (!TextUtils.isEmpty(imageUrl)) {
                                                    Glide.with(getApplicationContext())
                                                            .load(imageUrl)
                                                            .fitCenter()
                                                            .placeholder(R.drawable.kiddie_logo)
                                                            .into(appIcon);
                                                }

                                            }
                                        });
                                studentDocRef = studentCollectionRef.document("/" + loggedInUser.getStudentId());
                                studentDocRef
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                student = documentSnapshot.toObject(Student.class);
                                                student.setId(documentSnapshot.getId());
                                                System.out.println("loggedInUserStudent  "+student.getFirstName());
                                                if (!TextUtils.isEmpty(student.getFirstName())) {
                                                    String studentJson = gson.toJson(student);
                                                    sessionManager.putString("loggedInUserStudent", studentJson);
                                                    sessionManager.putString("loggedInUserStudentId", student.getId());
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Log.d(TAG, "Error getting documents: ", task.getException());
                            System.out.println("Error getting documents: -" + task.getException());
                        }
                    }
                });

    }

    private void getCurrentAcademicYear() {
        academicYearCollectionRef
                .whereEqualTo("instituteId", loggedInUser.getInstituteId())
                .whereEqualTo("status", "A")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                academicYearId = document.getId();
                                academicYear = document.toObject(AcademicYear.class);
                                academicYear.setId(document.getId());
                                System.out.println("academicYear " + academicYear.getYear());
                            }
                            if (!TextUtils.isEmpty(gson.toJson(academicYear))) {
                                SessionManager session = new SessionManager(getApplicationContext());
                                session.putString("academicYear", gson.toJson(academicYear));
                                session.putString("academicYearId", academicYearId) ;
                            }
                        } else {
                            // Log.d(TAG, "Error getting documents: ", task.getException());
                            System.out.println("Error getting documents: -" + task.getException());
                        }
                    }
                });
    }
}