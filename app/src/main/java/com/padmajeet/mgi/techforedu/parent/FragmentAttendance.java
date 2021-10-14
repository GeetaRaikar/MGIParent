package com.padmajeet.mgi.techforedu.parent;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.AcademicYear;
import com.padmajeet.mgi.techforedu.parent.model.Attendance;
import com.padmajeet.mgi.techforedu.parent.model.FeeStructure;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.util.HttpManager;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAttendance extends Fragment {

    private TabLayout tabAttendance;
    private ViewPager viewPager;
    private SweetAlertDialog pDialog;
    private Gson gson;
    private Parent loggedInUser;
    private SessionManager sessionManager;
    private AcademicYear currentAcademicYear;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private Student loggedInUserStudent;
    private String academicYearId,instituteId;
    private List<Attendance> attendanceList=new ArrayList<>();
    private List<Subject> subjectList=new ArrayList<>();

    public FragmentAttendance() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        pDialog=Utility.createSweetAlertDialog(getContext());
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        attendanceCollectionRef
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                .whereEqualTo("studentId", loggedInUserStudent.getId())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        attendanceList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Attendance attendance = document.toObject(Attendance.class);
                            attendance.setId(document.getId());
                            attendanceList.add(attendance);
                        }
                        sessionManager.putString("attendanceList",gson.toJson(attendanceList));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        subjectCollectionRef
                .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if(subjectList.size() > 0) {
                            subjectList.clear();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Subject subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
                        System.out.println("Result SubjectService- "+subjectList.size());
                        sessionManager.putString("subjectList",gson.toJson(subjectList));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String parentJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(parentJson, Parent.class);
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        academicYearId = sessionManager.getString("academicYearId");
        instituteId=sessionManager.getString("instituteId");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.attendance));

        tabAttendance = view.findViewById(R.id.tabAttendance);
        viewPager = view.findViewById(R.id.viewPager);
        //setupViewPager();
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new FragmentAttendanceSummary(), getString(R.string.attendanceSummary));
        adapter.addFragment(new FragmentDaywiseAttendance(), getString(R.string.daywiseAttendance));

        viewPager.setAdapter(adapter);
        tabAttendance.setupWithViewPager(viewPager);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("pos - "+position);
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}