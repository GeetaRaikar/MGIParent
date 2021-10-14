package com.padmajeet.mgi.techforedu.parent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Period;
import com.padmajeet.mgi.techforedu.parent.model.Staff;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.model.TimeTable;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTimeTable extends Fragment {

    private View view;
    private LinearLayout llNoList;
    private ArrayList<TimeTable> timeTableList = new ArrayList<>();
    private ArrayList<Staff> staffList = new ArrayList<>();
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private ArrayList<Period> periodList = new ArrayList<>();
    private TimeTable timeTable;
    private Subject subject;
    private Period period;
    private Staff staff;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference timeTableCollectionRef = db.collection("TimeTable");
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference periodCollectionRef = db.collection("Period");
    private RecyclerView rvTimeTable;
    private RecyclerView.Adapter timeTableAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Student loggedInUserStudent;
    private String instituteId;
    private String academicYearId;
    private Gson gson;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private ListenerRegistration timeTableListener;
    private Date selectedDate;
    private HorizontalCalendar horizontalCalendar;
    private TextView currentMonthTextView;

    public class TimeTableForPeriod{
        private String subject;
        private String staff;
        private String period;
        private int durationInMin;
        private String fromTime;
        private String toTime;
        private String id;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getStaff() {
            return staff;
        }

        public void setStaff(String staff) {
            this.staff = staff;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public int getDurationInMin() {
            return durationInMin;
        }

        public void setDurationInMin(int durationInMin) {
            this.durationInMin = durationInMin;
        }

        public String getFromTime() {
            return fromTime;
        }

        public void setFromTime(String fromTime) {
            this.fromTime = fromTime;
        }

        public String getToTime() {
            return toTime;
        }

        public void setToTime(String toTime) {
            this.toTime = toTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }
    public FragmentTimeTable() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        pDialog=Utility.createSweetAlertDialog(getContext());
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        staffCollectionRef
                .whereEqualTo("instituteId", instituteId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if(staffList.size() > 0) {
                            staffList.clear();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            staff = document.toObject(Staff.class);
                            staff.setId(document.getId());
                            staffList.add(staff);
                        }
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
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
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
                            subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
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
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        periodCollectionRef
                .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if(periodList.size() > 0) {
                            periodList.clear();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            periodList.add(period);
                        }
                        getTimeTableForBatch();
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
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        academicYearId = sessionManager.getString("academicYearId");
        instituteId=sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.timeTable));
        llNoList = view.findViewById(R.id.llNoList);
        rvTimeTable = view.findViewById(R.id.rvTimeTable);
        layoutManager = new LinearLayoutManager(getContext());
        rvTimeTable.setLayoutManager(layoutManager);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 1);
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR, -1);
        final Calendar defaultDate = Calendar.getInstance();
        //defaultDate.add(Calendar.YEAR, -2);
        //first time loading fragment
        defaultDate.set(Calendar.MILLISECOND, 0);
        defaultDate.set(Calendar.SECOND, 0);
        defaultDate.set(Calendar.MINUTE, 0);
        defaultDate.set(Calendar.HOUR, 0);
        defaultDate.set(Calendar.HOUR_OF_DAY,0);
        selectedDate = defaultDate.getTime();
        horizontalCalendar = new HorizontalCalendar.Builder(getActivity(), R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(7)   // Number of Dates cells shown on screen (default to 5).
                .configure()    // starts configuration.
                    .formatTopText("MMM")   // default to "MMM".
                    .formatMiddleText("dd") // default to "dd".
                    .formatBottomText("EEE")  // default to "EEE".
                    .textSize(10f, 18f, 10f)
                    //.textColor(Color.GRAY, Color.BLUE)    // default to (Color.GRAY, Color.WHITE)
                    .colorTextTop(Color.GRAY, Color.GRAY)
                    .colorTextMiddle(Color.GRAY, Color.BLUE)
                    .colorTextBottom(Color.GRAY, Color.GRAY)
                    .showTopText(true)  // show or hide TopText (default to true).
                    .showBottomText(true)   // show or hide BottomText (default to true).
                    .selectorColor(Color.TRANSPARENT)               // set selection indicator bar's color (default to colorAccent).
                .end()          // ends configuration.
                .defaultSelectedDate(defaultDate)    // Date to be selected at start (default to current day `Calendar.getInstance()`).
                .build();


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                //Toast.makeText(getContext(), DateFormat.getDateInstance().format(date) + " is selected!", Toast.LENGTH_SHORT).show();
                selectedDate = date.getTime();
                System.out.println("selectedDate "+selectedDate);
                getTimeTableForBatch();
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView,
                                         int dx, int dy) {

            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

        System.out.println("selectedDate "+selectedDate);
    }


    private void  getTimeTableForBatch() {
        timeTableCollectionRef
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(timeTableList.size()>0) {
                            timeTableList.clear();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            timeTable = document.toObject(TimeTable.class);
                            timeTable.setId(document.getId());
                            timeTableList.add(timeTable);
                        }
                        System.out.println("timeTableList => "+timeTableList.size());
                        if(timeTableList.size() > 0){
                            ArrayList<TimeTableForPeriod> timeTableForPeriodArrayList = new ArrayList<>();
                            for(TimeTable t:timeTableList) {
                                TimeTableForPeriod timeTableForPeriod = new TimeTableForPeriod();
                                timeTableForPeriod.setId(t.getId());
                                for (Period p : periodList) {
                                    if (p.getId().equals(t.getPeriodId())) {
                                        timeTableForPeriod.setPeriod(p.getNumber());
                                        timeTableForPeriod.setDurationInMin(p.getDuration());
                                        timeTableForPeriod.setFromTime(p.getFromTime());
                                        timeTableForPeriod.setToTime(p.getToTime());
                                        break;
                                    }
                                }
                                for (Subject s : subjectList) {
                                    if (s.getId().equals(t.getSubjectId())) {
                                        timeTableForPeriod.setSubject(s.getName());
                                        break;
                                    }
                                }
                                for (Staff st : staffList) {
                                    if (st.getId().equals(t.getStaffId())) {
                                        String name = st.getFirstName();
                                        if (!TextUtils.isEmpty(st.getLastName())) {
                                            name = name + " " + st.getLastName();
                                        }
                                        timeTableForPeriod.setStaff(name);
                                        break;
                                    }
                                }
                                timeTableForPeriodArrayList.add(timeTableForPeriod);
                            }
                            System.out.println("timeTableForPeriodArrayList => "+timeTableForPeriodArrayList.size());

                            Collections.sort(timeTableForPeriodArrayList, new Comparator<TimeTableForPeriod>() {
                                @Override
                                public int compare(TimeTableForPeriod t1, TimeTableForPeriod t2) {
                                    try {
                                        return new SimpleDateFormat("hh:mm a").parse(t1.getFromTime()).compareTo(new SimpleDateFormat("hh:mm a").parse(t2.getFromTime()));
                                    } catch (ParseException e) {
                                        return 0;
                                    }
                                }
                            });
                            for(int i=0;i<timeTableForPeriodArrayList.size();i++){
                                System.out.println("fromTime "+timeTableForPeriodArrayList.get(i).getFromTime());
                            }
                            timeTableAdapter = new TimeTableAdapter(timeTableForPeriodArrayList);
                            rvTimeTable.setAdapter(timeTableAdapter);
                            rvTimeTable.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        }else{
                            rvTimeTable.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                    }
                });
    }

    class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.MyViewHolder> {
        private List<TimeTableForPeriod> timeTableForPeriodArrayList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvStaff,tvPeriod,tvTime,tvDuration;
            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvStaff = view.findViewById(R.id.tvStaff);
                tvPeriod = view.findViewById(R.id.tvPeriod);
                tvTime = view.findViewById(R.id.tvTime);
                tvDuration = view.findViewById(R.id.tvDuration);
            }
        }

        public TimeTableAdapter(List<TimeTableForPeriod> timeTableForPeriodArrayList) {
            this.timeTableForPeriodArrayList = timeTableForPeriodArrayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_time_table, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final TimeTableForPeriod timeTableForPeriod = timeTableForPeriodArrayList.get(position);
            holder.tvPeriod.setText("" + timeTableForPeriod.getPeriod());
            holder.tvDuration.setText("" + timeTableForPeriod.getDurationInMin());
            holder.tvTime.setText("" + timeTableForPeriod.getFromTime()+" - "+timeTableForPeriod.getToTime());
            holder.tvSubject.setText("" + timeTableForPeriod.getSubject());
            holder.tvStaff.setText("" + timeTableForPeriod.getStaff());
        }

        @Override
        public int getItemCount() {
            return timeTableForPeriodArrayList.size();
        }
    }
}
