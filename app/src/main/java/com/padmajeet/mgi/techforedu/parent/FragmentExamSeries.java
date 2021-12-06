package com.padmajeet.mgi.techforedu.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Exam;
import com.padmajeet.mgi.techforedu.parent.model.ExamSeries;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentExamSeries extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference examSeriesCollectionRef = db.collection("ExamSeries");
    private CollectionReference examCollectionRef = db.collection("Exam");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private ListenerRegistration subjectListener;
    private ListenerRegistration examSeriesListener;
    private LinearLayout llNoList;
    private RecyclerView rvExamSeries;
    private RecyclerView.Adapter examSeriesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<ExamSeries> examSeriesList = new ArrayList<>();
    private List<Exam> examList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private Gson gson;
    private Student loggedInUserStudent;
    private String academicYearId;
    private SweetAlertDialog pDialog;
    private TextView tvExamSeriesName,tvExamSeriesDate;


    public FragmentExamSeries() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if(loggedInUserStudent != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            subjectListener = subjectCollectionRef
                    .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                    .orderBy("createdDate", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            if (subjectList.size() != 0) {
                                subjectList.clear();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Subject subject = document.toObject(Subject.class);
                                subject.setId(document.getId());
                                subjectList.add(subject);
                            }
                        }
                    });
        }else{
            //loggedInUserStudent == null
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        examSeriesListener.remove();
        subjectListener.remove();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        academicYearId = sessionManager.getString("academicYearId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exam_series, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.examSchedule));
        // get the list view
        llNoList = view.findViewById(R.id.llNoList);
        rvExamSeries = view.findViewById(R.id.rvExamSeries);
        layoutManager = new LinearLayoutManager(getContext());
        rvExamSeries.setLayoutManager(layoutManager);
        // preparing list data
        getExamSeriesOfBatch();
    }
    private void  getExamSeriesOfBatch() {
        if(academicYearId != null && loggedInUserStudent != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            examSeriesListener = examSeriesCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (examSeriesList.size() != 0) {
                                examSeriesList.clear();
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                ExamSeries examSeries = documentSnapshot.toObject(ExamSeries.class);
                                examSeries.setId(documentSnapshot.getId());
                                examSeriesList.add(examSeries);
                            }
                            if (examSeriesList.size() != 0) {
                                getAllExamOfExamSeriesOfBatch();
                            } else {
                                rvExamSeries.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            //loggedInUserStudent, academicYearId == null
        }
    }
    public void getAllExamOfExamSeriesOfBatch(){
        if(loggedInUserStudent != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            examCollectionRef
                    .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (examList.size() > 0) {
                                examList.clear();
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Exam exam = document.toObject(Exam.class);
                                exam.setId(document.getId());
                                examList.add(exam);
                            }
                            if (examList.size() > 0) {
                                for (Exam ex : examList) {
                                    for (Subject sub : subjectList) {
                                        if (ex.getSubjectId().equals(sub.getId())) {
                                            ex.setSubjectId(sub.getName());
                                            break;
                                        }
                                    }
                                }
                                examSeriesAdapter = new ExamSeriesAdapter(examSeriesList);
                                rvExamSeries.setAdapter(examSeriesAdapter);
                                rvExamSeries.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvExamSeries.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }
                    });
        }else{
            //loggedInUserStudent == null
        }
    }
    BottomSheetDialog bottomSheetDialog;
    class ExamSeriesAdapter extends RecyclerView.Adapter<ExamSeriesAdapter.MyViewHolder> {
        private List<ExamSeries> examSeriesList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvESName,tvESDate,tvStatus,tvDetails;
            public MyViewHolder(View view) {
                super(view);
                tvESName = view.findViewById(R.id.tvESName);
                tvESDate = view.findViewById(R.id.tvESDate);
                tvStatus = view.findViewById(R.id.tvStatus);
                tvDetails = view.findViewById(R.id.tvDetails);
            }
        }

        public ExamSeriesAdapter(List<ExamSeries> examSeriesList) {
            this.examSeriesList = examSeriesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_exam_series, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final ExamSeries examSeries = examSeriesList.get(position);
            holder.tvESName.setText(""+examSeries.getName());
            String date=Utility.formatDateToString(examSeries.getFromDate().getTime());
            if(examSeries.getToDate() != null){
                date = date + " to " +Utility.formatDateToString(examSeries.getToDate().getTime());
            }
            holder.tvESDate.setText(""+date);
            if(examSeries.getToDate().getTime() > new Date().getTime()){
                holder.tvStatus.setVisibility(View.GONE);
            }else{
                holder.tvStatus.setVisibility(View.VISIBLE);
            }
            holder.tvDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCreateBottomSheet(examSeries);
                    bottomSheetDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return examSeriesList.size();
        }
    }

    public void onCreateBottomSheet(ExamSeries examSeries){
        if(bottomSheetDialog == null){
            View viewBottom = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_view_exam, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(viewBottom);
            tvExamSeriesName = viewBottom.findViewById(R.id.tvExamSeriesName);
            tvExamSeriesDate = viewBottom.findViewById(R.id.tvExamSeriesDate);
            System.out.println("examSeries "+examSeries.getName());
            tvExamSeriesName.setText(""+examSeries.getName());
            String date=Utility.formatDateToString(examSeries.getFromDate().getTime());
            if(examSeries.getToDate() != null){
                date = date + " to " +Utility.formatDateToString(examSeries.getToDate().getTime());
            }
            tvExamSeriesDate.setText(""+date);
            RecyclerView rvExam = viewBottom.findViewById(R.id.rvExam);
            TextView tvNoData = viewBottom.findViewById(R.id.tvNoData);
            RecyclerView.LayoutManager layoutManagerForExam = new LinearLayoutManager(getContext());
            rvExam.setLayoutManager(layoutManagerForExam);
            RecyclerView.Adapter examAdapter;
            List<Exam> examForExamSeries = new ArrayList<>();
            for (Exam exam:examList){
                if(examSeries.getId().equals(exam.getExamSeriesId())) {
                    examForExamSeries.add(exam);
                }
            }
            if(examForExamSeries.size() > 0){
                examAdapter = new ExamAdapter(examForExamSeries);
                rvExam.setAdapter(examAdapter);
                rvExam.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
            }else{
                rvExam.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            }
        }
    }
    class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.MyViewHolder> {
        private List<Exam> examForExamSeries;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvDate,tvTime,tvCutOffMarks,tvTotalMarks;
            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvDate = view.findViewById(R.id.tvDate);
                tvTime = view.findViewById(R.id.tvTime);
                tvCutOffMarks = view.findViewById(R.id.tvCutOffMarks);
                tvTotalMarks = view.findViewById(R.id.tvTotalMarks);
            }
        }

        public ExamAdapter(List<Exam> examForExamSeries) {
            this.examForExamSeries = examForExamSeries;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_exam, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Exam exam = examForExamSeries.get(position);
            holder.tvName.setText("" + exam.getSubjectId());
            holder.tvDate.setText(Utility.formatDateToString(exam.getDate().getTime()));
            holder.tvTime.setText(exam.getFromPeriod()+" - "+exam.getToPeriod());
            holder.tvCutOffMarks.setText(""+exam.getCutOffMarks());
            holder.tvTotalMarks.setText(""+exam.getTotalMarks());

        }

        @Override
        public int getItemCount() {
            return examForExamSeries.size();
        }
    }


}
