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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.ScoreCard;
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
public class FragmentScoreCard extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference scoreCardCollectionRef = db.collection("ScoreCard");
    private CollectionReference examSeriesCollectionRef = db.collection("ExamSeries");
    private CollectionReference examCollectionRef = db.collection("Exam");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private ListenerRegistration subjectListener;
    private ListenerRegistration examSeriesListener;
    private LinearLayout llNoList;
    private RecyclerView rvScoreCard;
    private RecyclerView.Adapter scoreCardAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<ScoreCard> scoreCardList = new ArrayList<>();
    private List<ExamSeries> examSeriesList = new ArrayList<>();
    private List<Exam> examList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private Gson gson;
    private Student loggedInUserStudent;
    private String academicYearId;
    private SweetAlertDialog pDialog;
    private String SET_LABEL = "Subject Wise Score";
    private List<String> subjectName = new ArrayList<>();

    private class ScoreDetails{
        private String subject;
        private String date;
        private String time;
        private float totalMarks;
        private float marks;
        private String result;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public float getTotalMarks() {
            return totalMarks;
        }

        public void setTotalMarks(float totalMarks) {
            this.totalMarks = totalMarks;
        }

        public float getMarks() {
            return marks;
        }

        public void setMarks(float marks) {
            this.marks = marks;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    public FragmentScoreCard() {
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
        String parentJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        academicYearId = sessionManager.getString("academicYearId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_score_card, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.scoreCard));
        // get the list view
        llNoList = view.findViewById(R.id.llNoList);
        rvScoreCard = view.findViewById(R.id.rvScoreCard);
        layoutManager = new LinearLayoutManager(getContext());
        rvScoreCard.setLayoutManager(layoutManager);
        // preparing list data
        getExamSeriesOfBatch();
    }
    private void  getExamSeriesOfBatch() {
        if(loggedInUserStudent != null && academicYearId != null) {
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
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                ExamSeries examSeries = documentSnapshot.toObject(ExamSeries.class);
                                examSeries.setId(documentSnapshot.getId());
                                examSeriesList.add(examSeries);
                            }
                            if (examSeriesList.size() != 0) {
                                getAllExamOfExamSeriesOfBatch();
                            } else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                rvScoreCard.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            //academicYearId, loggedInUserStudent == null
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
                                getScoreCardOfStudent();
                            } else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                rvScoreCard.setVisibility(View.GONE);
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
    public void getScoreCardOfStudent(){
        if(loggedInUserStudent != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            scoreCardCollectionRef
                    .whereEqualTo("studentId", loggedInUserStudent.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (scoreCardList.size() > 0) {
                                scoreCardList.clear();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                ScoreCard scoreCard = document.toObject(ScoreCard.class);
                                scoreCard.setId(document.getId());
                                scoreCardList.add(scoreCard);
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            if (scoreCardList.size() > 0) {
                                scoreCardAdapter = new ExamSeriesAdapter(examSeriesList);
                                rvScoreCard.setAdapter(scoreCardAdapter);
                                rvScoreCard.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvScoreCard.setVisibility(View.GONE);
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
    class ExamSeriesAdapter extends RecyclerView.Adapter<ExamSeriesAdapter.MyViewHolder> {
        private List<ExamSeries> examSeriesList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvESName,tvESDate,tvStatus;
            public BarChart chartSubjectWiseScore;
            public RecyclerView rvScoreCardExam;
            public MyViewHolder(View view) {
                super(view);
                tvESName = view.findViewById(R.id.tvESName);
                tvESDate = view.findViewById(R.id.tvESDate);
                tvStatus = view.findViewById(R.id.tvStatus);
                chartSubjectWiseScore = view.findViewById(R.id.chartSubjectWiseScore);
                rvScoreCardExam = view.findViewById(R.id.rvScoreCardExam);
                RecyclerView.LayoutManager layoutManagerForExam = new LinearLayoutManager(getContext());
                rvScoreCardExam.setLayoutManager(layoutManagerForExam);
            }
        }

        public ExamSeriesAdapter(List<ExamSeries> examSeriesList) {
            this.examSeriesList = examSeriesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_score_card_exam_series, parent, false);
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
            List<Exam> filterExamList = new ArrayList<>();
            for(Exam exam:examList){
                if(examSeries.getId().equals(exam.getExamSeriesId())){
                    filterExamList.add(exam);
                }
            }
            List<ScoreDetails> filteredScoreDetailsList = new ArrayList<>();
            for(ScoreCard scoreCard:scoreCardList){
                for(Exam exam:filterExamList){
                    if(scoreCard.getExamId().equals(exam.getId())){
                        ScoreCard sc = scoreCard;
                        ScoreDetails scoreDetails = new ScoreDetails();
                        scoreDetails.setSubject(exam.getSubjectId());
                        scoreDetails.setDate(Utility.formatDateToString(exam.getDate().getTime()));
                        scoreDetails.setTime(exam.getFromPeriod()+" to "+exam.getToPeriod());
                        scoreDetails.setTotalMarks(exam.getTotalMarks());
                        scoreDetails.setMarks(scoreCard.getMarks());
                        scoreDetails.setResult(scoreCard.getResult());
                        filteredScoreDetailsList.add(scoreDetails);
                    }
                }
            }
            // Chart settings
            //createChartData();
            if(filteredScoreDetailsList.size() > 0){
                ArrayList<BarEntry> values = new ArrayList<>();
                subjectName.clear();
                for (int i = 0; i < filteredScoreDetailsList.size(); i++) {
                    int x = i;
                    float y = filteredScoreDetailsList.get(i).getMarks();
                    values.add(new BarEntry(x, y));
                    subjectName.add(filteredScoreDetailsList.get(i).getSubject());
                }
                System.out.println("filteredScoreDetailsList =>"+filteredScoreDetailsList.size());
                System.out.println("filterExamList =>"+filterExamList.size());
                BarDataSet dataSet = new BarDataSet(values, SET_LABEL);
                BarData data = new BarData(dataSet);
                ValueFormatter xAxisFormatter = new DayAxisValueFormatter(holder.chartSubjectWiseScore);
                XAxis xAxis = holder.chartSubjectWiseScore.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f); // only intervals of 1 day
                xAxis.setLabelCount(filteredScoreDetailsList.size());
                xAxis.setValueFormatter(xAxisFormatter);
                YAxis y = holder.chartSubjectWiseScore.getAxisLeft();
                y.setAxisMaxValue(filteredScoreDetailsList.get(0).getTotalMarks());
                y.setAxisMinValue(0);

                holder.chartSubjectWiseScore.getAxisRight().setEnabled(false);
                holder.chartSubjectWiseScore.getDescription().setEnabled(false);
                holder.chartSubjectWiseScore.setDrawGridBackground(false);
                holder.chartSubjectWiseScore.setData(data);
                holder.chartSubjectWiseScore.invalidate();

                RecyclerView.Adapter examAdapter;

                examAdapter = new ExamAdapter(filteredScoreDetailsList);
                holder.rvScoreCardExam.setAdapter(examAdapter);
                holder.rvScoreCardExam.setVisibility(View.VISIBLE);
                holder.chartSubjectWiseScore.setVisibility(View.VISIBLE);
            }else{
                holder.rvScoreCardExam.setVisibility(View.GONE);
                holder.chartSubjectWiseScore.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return examSeriesList.size();
        }
    }
    public class DayAxisValueFormatter extends ValueFormatter {
        private final BarLineChartBase<?> chart;
        public DayAxisValueFormatter(BarLineChartBase<?> chart) {
            this.chart = chart;
        }
        @Override
        public String getFormattedValue(float value) {
            return subjectName.get((int) value);
        }
    }
    class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.MyViewHolder> {
        private List<ScoreDetails> filteredScoreDetailsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvDate,tvTime,tvMarks,tvTotalMarks,tvResult;
            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvDate = view.findViewById(R.id.tvDate);
                tvTime = view.findViewById(R.id.tvTime);
                tvTotalMarks = view.findViewById(R.id.tvTotalMarks);
                tvMarks = view.findViewById(R.id.tvMarks);
                tvResult = view.findViewById(R.id.tvResult);
            }
        }

        public ExamAdapter(List<ScoreDetails> filteredScoreDetailsList) {
            this.filteredScoreDetailsList = filteredScoreDetailsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_score_card, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final ScoreDetails scoreDetails = filteredScoreDetailsList.get(position);
            holder.tvName.setText(scoreDetails.getSubject());
            holder.tvDate.setText(scoreDetails.getDate());
            holder.tvTime.setText(scoreDetails.getTime());
            holder.tvTotalMarks.setText(""+scoreDetails.getTotalMarks());
            holder.tvMarks.setText(""+scoreDetails.getMarks());
            if(scoreDetails.getResult().equalsIgnoreCase("FAIL")){
                holder.tvResult.setTextColor(getResources().getColor(R.color.colorRed));
            }else{
                holder.tvResult.setTextColor(getResources().getColor(R.color.colorGreenLight));
            }
            holder.tvResult.setText(""+scoreDetails.getResult());
        }

        @Override
        public int getItemCount() {
            return filteredScoreDetailsList.size();
        }
    }
}
