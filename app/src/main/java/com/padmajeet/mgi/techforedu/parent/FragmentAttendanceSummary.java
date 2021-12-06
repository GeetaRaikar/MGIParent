package com.padmajeet.mgi.techforedu.parent;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.parent.model.Attendance;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAttendanceSummary extends Fragment {
    private Gson gson;
    private Student loggedInUserStudent;
    private SessionManager sessionManager;
    private List<Attendance> attendanceList;
    private List<Subject> subjectList;
    private LinearLayout rlAttendanceSummary;
    private LinearLayout llNoList;
    private PieChart chart;
    private TableLayout tblSubject;
    private List<SubjectAttendance> subjectAttendanceList= new ArrayList<>();
    private String academicYearId,instituteId;

    public FragmentAttendanceSummary() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson=sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent=gson.fromJson(studentJson, Student.class);
        academicYearId= sessionManager.getString("academicYearId");
        instituteId=loggedInUserStudent.getInstituteId();
        System.out.println("instituteId - "+instituteId);
        System.out.println("academicYearId - "+academicYearId);
        String attendanceListJson = sessionManager.getString("attendanceList");
        System.out.println("attendanceListJson - "+attendanceListJson);
        attendanceList = gson.fromJson(attendanceListJson,new TypeToken<List<Attendance>>(){}.getType());
        String subjectListJson = sessionManager.getString("subjectList");
        System.out.println("subjectListJson - "+subjectListJson);
        subjectList = gson.fromJson(subjectListJson,new TypeToken<List<Subject>>(){}.getType());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.attendanceSummary));
        rlAttendanceSummary = view.findViewById(R.id.rlAttendanceSummary);
        tblSubject = view.findViewById(R.id.tblSubject);
        chart = view.findViewById(R.id.chartAttendanceSummary);
        llNoList = view.findViewById(R.id.llNoList);

        //chart.setCenterTextTypeface(tfLight);
        //chart.setCenterText(generateCenterSpannableText());

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        //chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);

        //chart.setMaxAngle(180f); // HALF CHART
        //chart.setRotationAngle(180f);
        chart.setCenterTextOffset(0, -20);

        chart.animateY(1400, Easing.EaseInOutQuad);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);


        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        //chart.setEntryLabelTypeface(tfRegular);
        chart.setEntryLabelTextSize(12f);
        if(attendanceList==null){
            rlAttendanceSummary.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
        else{
            getSubjectsOfBatch();
        }
    }

    private class SubjectAttendance{
        String subjectCode;
        String subjectName;
        String totalClasses;
        String presentClasses;
    }
    private void getSubjectsOfBatch() {
        System.out.println("Result SubjectService- "+subjectList.size());
        if(subjectList.size() == 0){
            rlAttendanceSummary.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
        else{
            ArrayList<String> subjectNameList = new ArrayList<>();
            ArrayList<PieEntry> attendancePercList = new ArrayList<>();
            System.out.println("Subject Count - "+subjectList.size());
            addSubjectRows();
            for(Subject subject:subjectList) {
                subjectNameList.add(subject.getName());
                System.out.println("Subject Name - "+subject.getName());
                int totalPeriodCount = 0;
                int totalPresentCount = 0;
                for (Attendance attendance : attendanceList) {
                    if(attendance.getSubjectId().equals(subject.getId())){
                        totalPeriodCount++;
                        System.out.println("Subject Matched ");
                        if(attendance.getStatus().equalsIgnoreCase("P")){
                            totalPresentCount++;
                            System.out.println("present Matched ");
                        }
                    }
                }
                float percentage=0;
                if(totalPeriodCount!=0) {
                    percentage = (totalPresentCount * 100.0f) / totalPeriodCount;
                    attendancePercList.add(new PieEntry(percentage,subject.getCode()));
                    System.out.println(percentage+"  "+subject.getName());
                }
                SubjectAttendance subjectAttendance = new SubjectAttendance();
                subjectAttendance.subjectCode=subject.getCode();
                subjectAttendance.subjectName=subject.getName();
                subjectAttendance.totalClasses=""+totalPeriodCount;
                subjectAttendance.presentClasses=""+totalPresentCount;

                subjectAttendanceList.add(subjectAttendance);
            }
            if(attendanceList.size() > 0) {
                llNoList.setVisibility(View.GONE);
                rlAttendanceSummary.setVisibility(View.VISIBLE);
                addSubjectRows();
                //PieEntry pieEntry = new PieEntry(attendancePercList);
                PieDataSet dataSet = new PieDataSet(attendancePercList, "- Subjects");
                dataSet.setSliceSpace(3f);
                dataSet.setSelectionShift(5f);
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                //dataSet.setSelectionShift(0f);

                PieData data = new PieData(dataSet);
                data.setValueFormatter(new PercentFormatter());
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);
                chart.setData(data);

                chart.invalidate();
            }else{
                llNoList.setVisibility(View.VISIBLE);
                rlAttendanceSummary.setVisibility(View.GONE);
            }
        }
    }

    private void addSubjectRows() {
        for (SubjectAttendance subjectAttendance:subjectAttendanceList){
            TableRow row = new TableRow(getContext());
            row.setWeightSum(1);
            TextView tvCode = new TextView(getContext());
            tvCode.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvCode.setText(subjectAttendance.subjectCode);
            tvCode.setPadding(5,5,5,5);
            row.addView(tvCode);

            TextView tvName = new TextView(getContext());
            tvName.setText(subjectAttendance.subjectName);
            tvName.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvName.setPadding(5,5,5,5);
            row.addView(tvName);

            TextView tvTotal = new TextView(getContext());
            tvTotal.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvTotal.setText(subjectAttendance.totalClasses);
            tvTotal.setPadding(5,5,5,5);
            row.addView(tvTotal);

            TextView tvPresent = new TextView(getContext());
            tvPresent.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvPresent.setText(subjectAttendance.presentClasses);
            tvPresent.setPadding(5,5,5,5);
            row.addView(tvPresent);

            tblSubject.addView(row);
        }
    }
}
