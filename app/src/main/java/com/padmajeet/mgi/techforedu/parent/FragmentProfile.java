package com.padmajeet.mgi.techforedu.parent;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Batch;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Section;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProfile extends Fragment {
    private View view=null;
    private Parent loggedInUser;
    private EditText etMobileNumber, etFatherName, etMotherName, etEmail, etAddress;
    private ImageView ivProfilePic;
    private Button btUpdateProfile;
    boolean isEmailEdited, isFatherNameEdited, isMotherNameEdited, isAddressEdited;
    private Gson gson;
    private String loggedInUserId;
    private Student loggedInUserStudent;
    private TextView tvResetPassword;
    private TextView tvBatchName, tvSectionName, tvStudentName, tvName;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference parentCollectionRef = db.collection("Parent");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference sectionCollectionRef = db.collection("Section");
    private DocumentReference sectionDocRef;
    private DocumentReference batchDocRef;
    private Batch batch;
    private Section section;

    public FragmentProfile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String parentJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(parentJson, Parent.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.profile));

        isEmailEdited = false;
        isFatherNameEdited = false;
        isMotherNameEdited = false;
        isAddressEdited = false;
        btUpdateProfile = view.findViewById(R.id.btUpdateProfile);
        btUpdateProfile.setVisibility(View.INVISIBLE);
        String emailId;
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvBatchName = view.findViewById(R.id.tvBatchName);
        tvSectionName = view.findViewById(R.id.tvSectionName);
        tvName = ((TextView) view.findViewById(R.id.tvName));

        String imageUrl = loggedInUserStudent.getImageUrl();
        System.out.println("imageUrl " + imageUrl);
        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_profile_large)
                .into(ivProfilePic);


        tvStudentName.setText("" + loggedInUserStudent.getFirstName() + " " + loggedInUserStudent.getLastName());
        batchDocRef = batchCollectionRef.document("/" + loggedInUserStudent.getCurrentBatchId());
        batchDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        batch = documentSnapshot.toObject(Batch.class);
                        batch.setId(documentSnapshot.getId());
                        tvBatchName.setText("" + batch.getName());
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


        sectionDocRef = sectionCollectionRef.document("/" + loggedInUserStudent.getCurrentSectionId());
        sectionDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        section = documentSnapshot.toObject(Section.class);
                        section.setId(documentSnapshot.getId());
                        tvSectionName.setText(" - " + section.getName());
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        etMobileNumber = view.findViewById(R.id.etMobileNumber);
        etMobileNumber.setText(loggedInUser.getMobileNumber());

        //System.out.println("Initial Mobile Number "+mobileNum);
        etEmail = view.findViewById(R.id.etEmail);
        emailId = loggedInUser.getEmailId();
        if (TextUtils.isEmpty(emailId)) {
            String emailIdUnavailable = getString(R.string.unavailable);
            etEmail.setHint(emailIdUnavailable);
        } else {
            etEmail.setText(emailId);
        }

        etFatherName = view.findViewById(R.id.etFatherName);

        etFatherName.setText(loggedInUser.getFirstName());

        etAddress = view.findViewById(R.id.etAddress);

        if (!TextUtils.isEmpty(loggedInUser.getAddress())) {
            etAddress.setText(loggedInUser.getAddress());
        }

        tvName.setText(loggedInUser.getFirstName());

        ImageView ivEditEmail = view.findViewById(R.id.ivEditEmail);
        ivEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEmailEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etEmail.setEnabled(true);
                etEmail.requestFocus();
            }
        });

        ImageView ivEditFatherName = view.findViewById(R.id.ivEditFatherName);
        ivEditFatherName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFatherNameEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etFatherName.setEnabled(true);
                etFatherName.requestFocus();
            }
        });

        ImageView ivEditAddress = view.findViewById(R.id.ivEditAddress);
        ivEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddressEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etAddress.setEnabled(true);
                etAddress.requestFocus();
            }
        });

        tvResetPassword = view.findViewById(R.id.tvResetPassword);
        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Reset Password")
                        .setContentText("After password reset you need to login again. Do you want to proceed?")
                        .setConfirmText("Proceed")
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent intent = new Intent(getActivity(), ActivityForgotPassword.class);
                                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                startActivity(intent);
                                //getActivity().finish();
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        btUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });

        return view;
    }

    void updateUserProfile() {
        boolean canSave = true;
        if (isEmailEdited) {
            String updatedEmail = etEmail.getText().toString().trim();
            if (!Utility.isEmailValid(updatedEmail)) {
                etEmail.setError(getString(R.string.errInvalidEmail));
                canSave = false;
            } else {
                loggedInUser.setEmailId(updatedEmail);
            }
        }
        if (isFatherNameEdited) {
            String updatedFatherName = etFatherName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedFatherName)) {
                etFatherName.setError("Enter Father Name");
                etFatherName.requestFocus();
                canSave = false;
            } else {
                loggedInUser.setFirstName(updatedFatherName);
            }
        }
        if (isAddressEdited) {
            String updatedAddress = etAddress.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedAddress)) {
                loggedInUser.setAddress(updatedAddress);
            }
        }

        if (canSave) {
            loggedInUser.setModifiedDate(new Date());
            etEmail.setEnabled(false);
            etFatherName.setEnabled(false);
            etMotherName.setEnabled(false);
            etAddress.setEnabled(false);
            btUpdateProfile.setVisibility(View.GONE);
            //Update
            parentCollectionRef.document(loggedInUserId).set(loggedInUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SessionManager sessionManager = new SessionManager(getContext());
                        String userJson = gson.toJson(loggedInUser);
                        sessionManager.putString("loggedInUser", userJson);
                        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                        View hView = navigationView.getHeaderView(0);
                        TextView nav_user = (TextView) getActivity().findViewById(R.id.tv_nav_name);
                        nav_user.setText(loggedInUser.getFirstName());
                        tvName.setText(loggedInUser.getFirstName());
                    } else {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

}
