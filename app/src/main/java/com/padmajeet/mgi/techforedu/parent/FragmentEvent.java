package com.padmajeet.mgi.techforedu.parent;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.padmajeet.mgi.techforedu.parent.model.Event;
import com.padmajeet.mgi.techforedu.parent.model.EventType;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentEvent extends Fragment {
    Gson gson;
    View view;
    String event_Date =null;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    CollectionReference eventCollectionRef = db.collection("Event");
    CollectionReference eventTypeCollectionRef = db.collection("EventType");
    private ArrayList<EventType> eventTypeList=new ArrayList<>();
    private ArrayList<Event> eventList=new ArrayList<>();
    private LinearLayout llNoList;
    private Student loggedInUserStudent;
    private String academicYearId;
    private Event event;
    private EventType eventType;
    private String eventTypeName;
    private RecyclerView rvEvent;
    private RecyclerView.Adapter eventAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Parent loggedInUser;
    private SweetAlertDialog pDialog;
    private ListenerRegistration eventListener;
    public FragmentEvent() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        String parntJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(parntJson, Parent.class);
        academicYearId = sessionManager.getString("academicYearId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_event, container, false);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.event));
        rvEvent = view.findViewById(R.id.rvEvent);
        layoutManager = new LinearLayoutManager(getContext());
        rvEvent.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        getEventTypeList();
        return view;
    }

    private void getEventTypeList(){
        if(eventTypeList.size()!=0){
            eventTypeList.clear();
        }

        if(pDialog !=null && !pDialog.isShowing()) {
            pDialog.show();
        }
        eventTypeCollectionRef
            .whereEqualTo("instituteId",loggedInUser.getInstituteId())
            .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                        // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                        EventType eventType=documentSnapshot.toObject(EventType.class);
                        eventType.setId(documentSnapshot.getId());
                        eventTypeList.add(eventType);
                    }
                    if(eventTypeList.size() > 0){
                        getEventList();
                    }else{
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        rvEvent.setVisibility(View.GONE);
                        llNoList.setVisibility(View.VISIBLE);
                    }

                }
            });
    }
    private void  getEventList() {
        if(eventList.size()!=0){
            eventList.clear();
        }


        eventListener = eventCollectionRef
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("recipientType", "P")
                .orderBy("fromDate", Query.Direction.DESCENDING)
                .orderBy("toDate", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (eventList != null) {
                            eventList.clear();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            event=documentSnapshot.toObject(Event.class);
                            event.setId(documentSnapshot.getId());
                            for(EventType eventType:eventTypeList){
                                if(eventType.getId().equals(event.getTypeId())){
                                    if(TextUtils.isEmpty(event.getBatchId()) || (event.getBatchId().equals(loggedInUserStudent.getCurrentBatchId()))){
                                        eventList.add(event);
                                    }
                                }
                            }
                        }
                        System.out.println("event Size -"+eventList.size());
                        if(eventList.size()!=0) {
                            eventAdapter = new EventAdapter(eventList);
                            rvEvent.setAdapter(eventAdapter);
                            eventAdapter.notifyDataSetChanged();
                            rvEvent.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        }
                        else{
                            rvEvent.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                        if(pDialog!=null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]


    }

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {
        private List<Event> eventList;

        public EventAdapter(List<Event> eventList) {
            this.eventList = eventList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_event, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            final Event event = eventList.get(position);

            holder.name.setText("" + event.getName());
            if(TextUtils.isEmpty(event.getDressCode())){
                holder.dressCode.setVisibility(View.GONE);
            }else{
                holder.dressCode.setText("" + event.getDressCode());
            }
            for(EventType eventType:eventTypeList){
                if(eventType.getId().equals(event.getTypeId())){
                    String imageUrl=eventType.getImageUrl();
                    if(TextUtils.isEmpty(imageUrl)){
                        Glide.with(getContext())
                                .load(R.drawable.no_image)
                                .fitCenter()
                                .apply(RequestOptions.circleCropTransform())
                                .into(holder.ivProfilePic);
                    }else{
                        Glide.with(getContext())
                                .load(imageUrl)
                                .fitCenter()
                                .apply(RequestOptions.circleCropTransform())
                                .into(holder.ivProfilePic);
                    }
                    break;
                }
            }
            String imageUrl=event.getTypeId();

            if (event.getFromDate() != null) {
                event_Date = Utility.formatDateToString(event.getFromDate().getTime());
            }
            if (event.getToDate() != null) {
                event_Date = event_Date + " to " + Utility.formatDateToString(event.getToDate().getTime());
            }
            if (event_Date != null) {
                holder.date.setText("" + event_Date);
            }
            if(event != null) {
                holder.tvCategory.setText("" + event.getCategory());
                System.out.println("event " + event.getCategory());
                if (event.getCategory().equals("R")) {
                    holder.tvCategory.setBackgroundColor(getResources().getColor(R.color.main_blue_color));
                }else{
                    holder.tvCategory.setVisibility(View.GONE);
                }
            }
            holder.tvCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String txt = holder.tvCategory.getText().toString().trim();
                    if (txt.equals("R")) {
                        holder.tvCategory.setText("Need/Require Response");
                    } else if (txt.equals("Need/Require Response")) {
                        holder.tvCategory.setText("R");
                    }
                }
            });
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    gson = Utility.getGson();
                    String selectedEvent = gson.toJson(event);
                    bundle.putString("selectedEvent", selectedEvent);
                    FragmentEventDetails fragmentEventDetails = new FragmentEventDetails();
                    fragmentEventDetails.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentEventDetails);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView name,dressCode, date,tvCategory;
            public ImageView ivProfilePic;
            public View row;

            public MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.tvName);
                dressCode = view.findViewById(R.id.tvDressCode);
                date = view.findViewById(R.id.tvDate);
                ivProfilePic=view.findViewById(R.id.ivProfilePic);
                tvCategory=view.findViewById(R.id.tvCategory);
                row = view;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventListener != null) {
            eventListener.remove();
        }
    }
}

