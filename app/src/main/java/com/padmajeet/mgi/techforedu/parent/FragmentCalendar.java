 package com.padmajeet.mgi.techforedu.parent;


 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;

 import com.google.firebase.firestore.CollectionReference;
 import com.google.firebase.firestore.DocumentSnapshot;
 import com.google.firebase.firestore.EventListener;
 import com.google.firebase.firestore.FirebaseFirestore;
 import com.google.firebase.firestore.FirebaseFirestoreException;
 import com.google.firebase.firestore.ListenerRegistration;
 import com.google.firebase.firestore.Query;
 import com.google.firebase.firestore.QuerySnapshot;
 import com.google.gson.Gson;
 import com.padmajeet.mgi.techforedu.parent.model.Calendar;
 import com.padmajeet.mgi.techforedu.parent.model.Student;
 import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
 import com.padmajeet.mgi.techforedu.parent.util.Utility;

 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;

 import androidx.annotation.Nullable;
 import androidx.fragment.app.Fragment;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;
 import cn.pedant.SweetAlert.SweetAlertDialog;


 /**
  * A simple {@link Fragment} subclass.
  */
 public class FragmentCalendar extends Fragment {

     private View view = null;
     private Gson gson;
     private Student loggedInUserStudent;
     private LinearLayout llNoList;
     private List<Calendar> calendarList = new ArrayList<>();
     Bundle bundle = new Bundle();
     String academicYearId;
     private FirebaseFirestore db = FirebaseFirestore.getInstance();
     private CollectionReference calendarCollectionRef = db.collection("Calendar");
     private Calendar calendar;
     private RecyclerView rvCalendar;
     private RecyclerView.Adapter calendarAdapter;
     private RecyclerView.LayoutManager layoutManager;
     private String instituteId;
     int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_green_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};
     private ListenerRegistration calendarListener;

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         SessionManager sessionManager = new SessionManager(getContext());
         gson = Utility.getGson();
         String studentJson = sessionManager.getString("loggedInUserStudent");
         loggedInUserStudent = gson.fromJson(studentJson, Student.class);
         academicYearId = sessionManager.getString("academicYearId");
         instituteId=sessionManager.getString("instituteId");
         System.out.println("Student -"+loggedInUserStudent.getFirstName());
         System.out.println("academicYearId -"+academicYearId);
         System.out.println("instituteId -"+instituteId);
     }

     public FragmentCalendar() {
         // Required empty public constructor
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_calendar, container, false);
         ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.academicCalendar));
         rvCalendar = view.findViewById(R.id.rvCalendar);
         layoutManager = new LinearLayoutManager(getContext());
         rvCalendar.setLayoutManager(layoutManager);
         llNoList = view.findViewById(R.id.llNoList);
         getCalendarOfBatch();
         return view;
     }

     private void getCalendarOfBatch() {

         final SweetAlertDialog pDialog;
         pDialog = Utility.createSweetAlertDialog(getContext());
         pDialog.show();
         calendarListener = calendarCollectionRef
                 .whereEqualTo("academicYearId",academicYearId)
                 .whereEqualTo("batchId",loggedInUserStudent.getCurrentBatchId())
                 .orderBy("fromDate", Query.Direction.ASCENDING)
                 .orderBy("toDate", Query.Direction.ASCENDING)
                 .addSnapshotListener(new EventListener<QuerySnapshot>() {
                     @Override
                     public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                         if (e != null) {
                             return;
                         }
                         if(calendarList.size()!=0){
                             calendarList.clear();
                         }
                         if (pDialog != null) {
                             pDialog.dismiss();
                         }
                         for (DocumentSnapshot document:queryDocumentSnapshots.getDocuments()) {
                             calendar = document.toObject(Calendar.class);
                             calendar.setId(document.getId());
                             calendarList.add(calendar);
                         }
                         System.out.println("Calendar  -" + calendarList.size());
                         if (calendarList.size() != 0) {
                             calendarAdapter = new CalendarAdapter(calendarList);
                             rvCalendar.setAdapter(calendarAdapter);
                             calendarAdapter.notifyDataSetChanged();
                             rvCalendar.setVisibility(View.VISIBLE);
                             llNoList.setVisibility(View.GONE);
                         } else {
                             rvCalendar.setVisibility(View.GONE);
                             llNoList.setVisibility(View.VISIBLE);
                         }
                     }
                 });
         // [END get_all_users]

     }

     class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {
         private List<Calendar> calendarList;

         public class MyViewHolder extends RecyclerView.ViewHolder {
             public TextView tvEvent, tvDate,tvDaysOfMonth,tvMonth;
             public LinearLayout llImage;

             public MyViewHolder(View view) {
                 super(view);
                 tvEvent = view.findViewById(R.id.tvEvent);
                 tvDate = view.findViewById(R.id.tvDate);
                 tvDaysOfMonth=view.findViewById(R.id.tvDaysOfMonth);
                 tvMonth=view.findViewById(R.id.tvMonth);
                 llImage=view.findViewById(R.id.llImage);
             }
         }


         public CalendarAdapter(List<Calendar> calendarList) {
             this.calendarList = calendarList;
         }

         @Override
         public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
             View itemView = LayoutInflater.from(parent.getContext())
                     .inflate(R.layout.row_calendar, parent, false);

             return new MyViewHolder(itemView);
         }

         @Override
         public void onBindViewHolder(MyViewHolder holder, int position) {
             final Calendar calendar = calendarList.get(position);
             holder.tvEvent.setText("" + calendar.getEvent());
             int colorCode=position%5;
             holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
             String eventDate = null;
             if (calendar.getFromDate() != null) {
                 eventDate = Utility.formatDateToString(calendar.getFromDate().getTime());
                 Format formatter = new SimpleDateFormat("MMM");
                 holder.tvMonth.setText(""+formatter.format(calendar.getFromDate()).toUpperCase());
                 formatter = new SimpleDateFormat("dd");
                 holder.tvDaysOfMonth.setText(""+formatter.format(calendar.getFromDate()));

             }
             if (calendar.getToDate() != null) {
                 eventDate = eventDate + " to " + Utility.formatDateToString(calendar.getToDate().getTime());
             }
             if (eventDate != null) {
                 holder.tvDate.setText("" + eventDate);
             }
         }

         @Override
         public int getItemCount() {
             return calendarList.size();
         }
     }

     @Override
     public void onStop() {
         super.onStop();
         if (calendarListener != null) {
             calendarListener.remove();
         }
     }

 }
