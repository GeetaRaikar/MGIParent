package com.padmajeet.mgi.techforedu.parent;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Message;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.ALARM_SERVICE;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */

public class FragmentMessage extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference batchFacultyCollectionRef=db.collection("BatchFaculty");
    private CollectionReference messageCollectionRef = db.collection("Message");
    private List<Message> messageList=new ArrayList<>();
    private RecyclerView rvMessage;
    private RecyclerView.Adapter messageAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Gson gson;
    private Student loggedInUserStudent;
    private String academicYearId;
    private String instituteId;
    private SweetAlertDialog pDialog;
    private LinearLayout llNoList;
    private String studentBatchId;
    private String studentId;
    private Parent loggedInUser;
    private ListenerRegistration messageListener;
    int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_green_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        String loggedInUserJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(loggedInUserJson,Parent.class);
        academicYearId = sessionManager.getString("academicYearId");
        instituteId=sessionManager.getString("instituteId");

        pDialog=Utility.createSweetAlertDialog(getContext());
        studentBatchId = loggedInUserStudent.getCurrentBatchId();
        studentId = loggedInUserStudent.getId();
    }
    public FragmentMessage() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.message));
        llNoList = view.findViewById(R.id.llNoList);
        rvMessage = view.findViewById(R.id.rvMessage);
        layoutManager = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(layoutManager);
        getAllMessages();
    }
    private void getAllMessages() {
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        messageListener = messageCollectionRef
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("recipientType", "P")
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (messageList.size() != 0) {
                            messageList.clear();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Message message = documentSnapshot.toObject(Message.class);
                            message.setId(documentSnapshot.getId());

                            if (message.getCategory().equals("A")) {//Messages of All class
                                messageList.add(message);
                            } else if (message.getCategory().equals("C")) {//Specific to the student's class

                                List<String> batchIdList = message.getBatchIdList();
                                for (String batchId : batchIdList) {
                                    if (batchId.equals(studentBatchId)) {
                                        messageList.add(message);
                                        break;
                                    }
                                }
                            } else if (message.getCategory().equals("S")) {//Specific to the student
                                List<String> recipientIdList = message.getRecipientIdList();
                                for (String recipientId : recipientIdList) {
                                    if (recipientId.equals(studentId)) {
                                        messageList.add(message);
                                        break;
                                    }
                                }
                            }
                        }

                        if (messageList.size() != 0) {
                            messageAdapter = new MessageAdapter(messageList);
                            rvMessage.setAdapter(messageAdapter);
                            rvMessage.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvMessage.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
        private List<Message> messageList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvCreatorType,tvSubjectHeader,tvDate;
            LinearLayout llImage;
            View row;

            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvCreatorType = view.findViewById(R.id.tvCreatorType);
                tvSubjectHeader = view.findViewById(R.id.tvSubjectHeader);
                llImage = view.findViewById(R.id.llImage);
                tvDate = view.findViewById(R.id.tvDate);
                row = view;
            }
        }


        public MessageAdapter(List<Message> messageList) {
            this.messageList = messageList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_message, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Message message = messageList.get(position);
            holder.tvSubject.setText("" + message.getSubject());
            holder.tvDate.setText("" +Utility.formatDateToString(message.getCreatedDate().getTime()));
            int colorCode = position%5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            holder.tvSubjectHeader.setText(""+message.getSubject().toUpperCase().charAt(0));
            if(message.getCreatorType().equals("A")){
                holder.tvCreatorType.setText("Admin");
            }
            else if(message.getCreatorType().equals("F")){
                holder.tvCreatorType.setText("Teacher");
            }

            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    gson = Utility.getGson();
                    String selectedMessage = gson.toJson(message);
                    bundle.putString("selectedMessage", selectedMessage);
                    FragmentMessageDetails fragmentMessageDetails = new FragmentMessageDetails();
                    fragmentMessageDetails.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentMessageDetails);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}
