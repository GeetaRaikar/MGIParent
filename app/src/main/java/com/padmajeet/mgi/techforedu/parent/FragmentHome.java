package com.padmajeet.mgi.techforedu.parent;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.AcademicYear;
import com.padmajeet.mgi.techforedu.parent.model.Batch;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Section;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHome extends Fragment {
    TextView tvParentName,tvStudentName,tvClass;
    ImageView ivProfilePic;

    FirebaseFirestore db= FirebaseFirestore.getInstance();
    CollectionReference studentCollectionRef=db.collection("Student");
    CollectionReference parentCollectionRef=db.collection("Parent");
    CollectionReference batchCollectionRef=db.collection("Batch");
    CollectionReference sectionCollectionRef=db.collection("Section");
    DocumentReference sectionDocRef;
    DocumentReference batchDocRef;
    Batch batch;
    Section section;
    Gson gson;
    Student loggedInUserStudent;
    String LoggedInUserStudentId;
    AcademicYear academicYear;
    String academicYearId;
    String className="";
    Parent loggedInUser;
    GridLayout gridLayout;
    private PublisherAdView mPublisherAdView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String parentJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(parentJson, Parent.class);
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        LoggedInUserStudentId= sessionManager.getString("loggedInUserStudentId");
        String selectedAcademicYearJson = sessionManager.getString("AcademicYear");
        academicYearId = sessionManager.getString("AcademicYearId");
        academicYear =gson.fromJson(selectedAcademicYearJson, AcademicYear.class);

    }

    public FragmentHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
        mPublisherAdView = view.findViewById(R.id.publisherAdView);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        mPublisherAdView.loadAd(adRequest);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvParentName =view.findViewById(R.id.tvParentName);
        tvStudentName =view.findViewById(R.id.tvName);
        tvClass = view.findViewById(R.id.tvClass);

        gridLayout = view.findViewById(R.id.mainGrid);


        setSingleEvent(gridLayout);

        String name=loggedInUser.getFirstName();
        if(TextUtils.isEmpty(loggedInUser.getLastName())){
            name=name+" "+loggedInUser.getLastName();
        }
        tvParentName.setText("Welcome "+name);

        if(loggedInUserStudent != null) {
            String imageUrl = loggedInUserStudent.getImageUrl();
            if (TextUtils.isEmpty(imageUrl)) {
                Glide.with(this)
                        .load(R.drawable.ic_student)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_profile_large)
                        .into(ivProfilePic);
            } else {
                Glide.with(this)
                        .load(imageUrl)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_profile_large)
                        .into(ivProfilePic);
            }
            tvStudentName.setText(""+loggedInUserStudent.getFirstName()+" "+loggedInUserStudent.getLastName());

            batchDocRef = batchCollectionRef.document("/" + loggedInUserStudent.getCurrentBatchId());
            batchDocRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            batch = documentSnapshot.toObject(Batch.class);
                            batch.setId(documentSnapshot.getId());
                            className=batch.getName();

                            sectionDocRef = sectionCollectionRef.document("/" + loggedInUserStudent.getCurrentSectionId());
                            sectionDocRef.get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            section = documentSnapshot.toObject(Section.class);
                                            section.setId(documentSnapshot.getId());
                                            className=className+" - "+section.getName();
                                            tvClass.setText(""+className);
                                        }
                                    })

                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });


                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }



    }

    private void setSingleEvent(GridLayout gridLayout) {
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        Menu menuNav = navigationView.getMenu();
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) gridLayout.getChildAt(i);
            final int id = i;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getContext(), "Clicked at index " + id,Toast.LENGTH_SHORT).show();
                    switch (id) {
                        case 0:
                            menuNav.findItem(R.id.nav_message).setChecked(true);
                            replaceFragment(new FragmentMessage(), getString(R.string.message));
                            break;
                        case 1:
                            menuNav.findItem(R.id.nav_event).setChecked(true);
                            replaceFragment(new FragmentEvent(), getString(R.string.event));
                            break;
                        case 2:
                            menuNav.findItem(R.id.nav_feedback).setChecked(true);
                            replaceFragment(new FragmentFeedBack(), getString(R.string.noteToTeacher));
                            break;
                        case 3:
                            menuNav.findItem(R.id.nav_attendance).setChecked(true);
                            replaceFragment(new FragmentAttendance(), getString(R.string.attendance));
                            break;
                        case 4:
                            menuNav.findItem(R.id.nav_calender).setChecked(true);
                            replaceFragment(new FragmentCalendar(), getString(R.string.academicCalendar));
                            break;
                        case 5:
                            menuNav.findItem(R.id.nav_subject).setChecked(true);
                            replaceFragment(new FragmentSubject(), getString(R.string.subject));
                            break;
                        case 6:
                            menuNav.findItem(R.id.nav_home_work).setChecked(true);
                            replaceFragment(new FragmentHomeWork(), getString(R.string.assignment));
                            break;
                        case 7:
                            menuNav.findItem(R.id.nav_competition_winner).setChecked(true);
                            replaceFragment(new FragmentCompetitionWinner(), getString(R.string.competition));
                            break;
                        case 8:
                            menuNav.findItem(R.id.nav_achievement).setChecked(true);
                            replaceFragment(new FragmentAchievements(), getString(R.string.achievement));
                            break;
                        case 9:
                            menuNav.findItem(R.id.nav_student_fees).setChecked(true);
                            replaceFragment(new FragmentStudentFees(), getString(R.string.fees));
                            break;
                        case 10:
                            menuNav.findItem(R.id.nav_holiday).setChecked(true);
                            replaceFragment(new FragmentHoliday(), getString(R.string.holiday));
                            break;
                        case 11:
                            menuNav.findItem(R.id.nav_support).setChecked(true);
                            replaceFragment(new FragmentSupport(), getString(R.string.contact));
                            break;
                        case 12:
                            menuNav.findItem(R.id.nav_timetable).setChecked(true);
                            replaceFragment(new FragmentTimeTable(), getString(R.string.timeTable));
                            break;
                        case 13:
                            menuNav.findItem(R.id.nav_exam_schedule).setChecked(true);
                            replaceFragment(new FragmentExamSeries(), getString(R.string.examSchedule));
                            break;
                        case 14:
                            menuNav.findItem(R.id.nav_scoreCard).setChecked(true);
                            replaceFragment(new FragmentScoreCard(), getString(R.string.scoreCard));
                            break;
                    }
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.contentLayout, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mPublisherAdView != null) {
            mPublisherAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mPublisherAdView != null) {
            mPublisherAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mPublisherAdView != null) {
            mPublisherAdView.destroy();
        }
        super.onDestroy();
    }

}
