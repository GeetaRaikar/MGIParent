package com.padmajeet.mgi.techforedu.parent;


import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Batch;
import com.padmajeet.mgi.techforedu.parent.model.HomeWork;
import com.padmajeet.mgi.techforedu.parent.model.Section;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeWork extends Fragment {

    private View view=null;
    private RecyclerView rvHomeWork;
    private LinearLayout llNoList;
    private RecyclerView.Adapter homeWorkAdapter;
    private RecyclerView.LayoutManager layoutManager;
    SessionManager sessionManager;
    Gson gson;
    private String loggedInUserId;
    private String academicYearId;
    private String schoolId;
    private Student loggedInUserStudent;
    SweetAlertDialog pDialog;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private List<HomeWork> homeWorkList=new ArrayList<>();
    private List<Subject> subjectList=new ArrayList<>();
    DocumentReference batchDocRef;
    CollectionReference homeWorkCollectionRef =db.collection("HomeWork");
    CollectionReference subjectCollectionRef =db.collection("Subject");
    Subject subject;
    HomeWork homeWork;
    Batch selectedBatch;
    Section section;
    long downloadID;
    Batch batch;
    Section selectedSection;
    BottomSheetDialog bottomSheetDialog;
    DatePickerDialog picker;
    DownloadManager downloadManager;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private ListenerRegistration homeworkListener;
    private ListenerRegistration subjectListener;

    @Override
    public void onStart() {
        super.onStart();

        subjectListener = subjectCollectionRef
                .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (subjectList.size() != 0) {
                            subjectList.clear();
                        }
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
                        System.out.println("subjectList "+subjectList.size());
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (subjectListener != null) {
            subjectListener.remove();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        loggedInUserId= sessionManager.getString("loggedInUserId");
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        schoolId = sessionManager.getString("schoolId");
        academicYearId = sessionManager.getString("academicYearId");
        System.out.println("academicYearId  "+academicYearId);
        System.out.println("loggedInUserStudent.getCurrentBatchId()  "+loggedInUserStudent.getCurrentBatchId());
        System.out.println("loggedInUserStudent.getSectionId()  "+loggedInUserStudent.getCurrentSectionId());

    }

    public FragmentHomeWork() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_work, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.assignment));
        rvHomeWork = view.findViewById(R.id.rvHomeWork);
        layoutManager = new LinearLayoutManager(getContext());
        rvHomeWork.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        System.out.println("HomeWork");
        getHomeWork();
        return view;
    }

    private void  getHomeWork(){
        if(homeWorkList.size()!=0){
            homeWorkList.clear();
        }
        pDialog = Utility.createSweetAlertDialog(getContext());
        if(!pDialog.isShowing()){
            pDialog.show();
        }

        homeworkListener=homeWorkCollectionRef
                .whereEqualTo("academicYearId",academicYearId)
                .whereEqualTo("batchId",loggedInUserStudent.getCurrentBatchId())
                .whereEqualTo("sectionId",loggedInUserStudent.getCurrentSectionId())
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .orderBy("dueDate",Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (homeWorkList.size() != 0) {
                            homeWorkList.clear();
                        }
                        System.out.println("queryDocumentSnapshots.getDocumentChanges() "+queryDocumentSnapshots.getDocumentChanges());
                        System.out.println("onStart ");
                        NotificationCompat.Builder mBuilder=
                                new NotificationCompat.Builder(getContext(),"assignment")
                                        .setSmallIcon(R.drawable.ic_notifications_active)
                                        .setContentTitle("Assignment")
                                        .setContentText("New Assignment")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat mNotificationMgr= NotificationManagerCompat.from(getContext());
                        mNotificationMgr.notify(2,mBuilder.build());
                        System.out.println("mNotificationMgr "+mNotificationMgr);
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            System.out.println("queryDocumentSnapshots.getDocumentChanges() "+dc.getType());
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String title = dc.getDocument().getData().get("subjectName").toString();
                                String body = dc.getDocument().getData().get("name").toString();
                                System.out.println("title "+title);
                                System.out.println("body "+body);
                                /*notificationManagerCompat=NotificationManagerCompat.from(getContext());

                                //Method to set Notifications
                                Notification notification=new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_notifications_active)
                                        .setContentTitle(title)
                                        .setContentText(body)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .build();
                                notificationManagerCompat.notify(1,notification);*/

                            }
                        }
                        for (DocumentSnapshot document:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            homeWork = document.toObject(HomeWork.class);
                            homeWork.setId(document.getId());
                            for(int i=0;i<subjectList.size();i++){
                                if(homeWork.getSubjectId().equals(subjectList.get(i).getId())){
                                    homeWork.setSubjectName(subjectList.get(i).getName());
                                    break;
                                }
                            }
                            homeWorkList.add(homeWork);
                        }
                        System.out.println("homeWorkList "+homeWorkList.size());
                        if (homeWorkList.size() != 0) {
                            if(pDialog!=null){
                                pDialog.dismiss();
                            }

                            rvHomeWork.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            homeWorkAdapter = new HomeWorkAdapter(homeWorkList);
                            rvHomeWork.setAdapter(homeWorkAdapter);

                        } else {
                            if(pDialog!=null){
                                pDialog.dismiss();
                            }
                            rvHomeWork.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    class HomeWorkAdapter extends RecyclerView.Adapter<HomeWorkAdapter.MyViewHolder> {
        private List<HomeWork> homeWorkList;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubjectName,tvName, tvCreatedDate,tvDueDate;
            public ImageView ivAttachmentHomeWork,ivSubjectPic;
            View row;

            public MyViewHolder(View view) {
                super(view);
                tvSubjectName = view.findViewById(R.id.tvSubjectName);
                tvName = view.findViewById(R.id.tvName);
                tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
                tvDueDate=view.findViewById(R.id.tvDueDate);
                ivAttachmentHomeWork=view.findViewById(R.id.ivAttachmentHomeWork);
                ivSubjectPic= view.findViewById(R.id.ivSubjectPic);
                row=view;
            }
        }


        public HomeWorkAdapter(List<HomeWork> homeWorkList) {
            this.homeWorkList = homeWorkList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_home_work, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final HomeWork homeWork = homeWorkList.get(position);
            holder.tvSubjectName.setText(""+homeWork.getSubjectName());
            holder.tvName.setText(""+homeWork.getName());
            holder.tvCreatedDate.setText(""+Utility.formatDateToString(homeWork.getCreatedDate().getTime()));
            holder.tvDueDate.setText(""+Utility.formatDateToString(homeWork.getDueDate().getTime()));
            if(TextUtils.isEmpty(homeWork.getAttachmentUrl())){
                holder.ivAttachmentHomeWork.setVisibility(View.GONE);
            }else{
                holder.ivAttachmentHomeWork.setVisibility(View.VISIBLE);
            }
            holder.ivAttachmentHomeWork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = homeWork.getAttachmentUrl();
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    String fileName1 = fileName.substring(0, fileName.lastIndexOf('?'));
                    String[] fileName2 = fileName1.split("%2F");
                    System.out.println("fileName2 "+fileName2[1]);
                    downloadManager=(DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri=Uri.parse(url);
                    DownloadManager.Request request=new DownloadManager.Request(uri);
                    //Restrict the types of networks over which this download may proceed.
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    // Makes download visible in notifications while downloading, but disappears after download completes. Optional.
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(getContext(),DIRECTORY_DOWNLOADS,fileName2[1]);
                    final long enqueue = downloadManager.enqueue(request);

                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

                    getContext().registerReceiver(receiver,
                            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                }
            });

        }


        @Override
        public int getItemCount() {
            return homeWorkList.size();
        }
    }
}
