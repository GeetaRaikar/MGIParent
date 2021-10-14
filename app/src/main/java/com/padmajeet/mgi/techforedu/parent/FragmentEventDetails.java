package com.padmajeet.mgi.techforedu.parent;


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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Batch;
import com.padmajeet.mgi.techforedu.parent.model.Event;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentEventDetails extends Fragment {

    private TextView tvEvent,tvDate,tvDesc,tvCreatorType,tvClass,tvDressCode,tvAttachment,tvLabelAttachment;
    Event selectedEvent;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    DownloadManager downloadManager;
    RadioGroup rgResponse;
    RadioButton rbMayBe,rbYes,rbNo;
    Button btnSaveResponse,btnUpdateResponse;
    LinearLayout llEventResponse;
    String response="";
    CollectionReference eventCollectionRef=db.collection("Event");
    CollectionReference batchCollectionRef=db.collection("Batch");
    DocumentReference batchDocRef;
    HashMap<String,String> responseMap=new HashMap<String,String>();
    SessionManager sessionManager;
    Parent loggedInUser;
    String url,fileName,fileName1;
    String[] fileName2;

    public FragmentEventDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String selectedEventJson = getArguments().getString("selectedEvent");
        sessionManager = new SessionManager(getContext());
        Gson gson = Utility.getGson();
        selectedEvent = gson.fromJson(selectedEventJson,Event.class);
        String parentJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(parentJson, Parent.class);
        if(selectedEvent.getParentResponses().size() > 0) {
            responseMap.putAll(selectedEvent.getParentResponses());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvEvent = view.findViewById(R.id.tvEvent);
        tvDate = view.findViewById(R.id.tvDate);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvDressCode = view.findViewById(R.id.tvDressCode);
        tvCreatorType = view.findViewById(R.id.tvCreatorType);
        tvAttachment = view.findViewById(R.id.tvAttachment);
        tvLabelAttachment= view.findViewById(R.id.tvLabelAttachment);
        tvClass = view.findViewById(R.id.tvClass);
        rbMayBe = view.findViewById(R.id.rbMayBe);
        rbNo = view.findViewById(R.id.rbNo);
        rbYes = view.findViewById(R.id.rbYes);
        rgResponse = view.findViewById(R.id.rgResponse);
        btnSaveResponse = view.findViewById(R.id.btnSaveResponse);
        btnUpdateResponse = view.findViewById(R.id.btnUpdateResponse);
        llEventResponse=view.findViewById(R.id.llEventResponse);

        if(selectedEvent.getCategory().equals("R")){
            llEventResponse.setVisibility(View.VISIBLE);
            //already response is taken
            if (responseMap.containsKey(loggedInUser.getId())) {
                response=responseMap.get(loggedInUser.getId());
                System.out.println("responseMap"+responseMap);
                System.out.println("response"+response);
                btnSaveResponse.setVisibility(View.GONE);
                btnUpdateResponse.setVisibility(View.VISIBLE);

                if(response.equals("May be")){
                    rgResponse.check(R.id.rbMayBe);
                }

                if(response.equals("No")){
                    rgResponse.check(R.id.rbNo);
                }

                if(response.equals("Yes")){
                    rgResponse.check(R.id.rbYes);
                }

            }
        }
        tvEvent.setText(""+selectedEvent.getName());
        if(TextUtils.isEmpty(selectedEvent.getDressCode())){
            tvDressCode.setVisibility(View.GONE);
        }else{
            tvDressCode.setText(""+selectedEvent.getDressCode());
        }
        tvDate.setText("" + Utility.formatDateToString(selectedEvent.getCreatedDate().getTime()));

        if(selectedEvent.getCreatorType().equals("A")){
            tvCreatorType.setText("Admin");
        }
        else if(selectedEvent.getCreatorType().equals("F")){
            tvCreatorType.setText("Teacher");
        }
        tvDesc.setText(""+selectedEvent.getDescription());
        if(TextUtils.isEmpty(selectedEvent.getAttachmentUrl())){
            tvAttachment.setVisibility(View.GONE);
            tvLabelAttachment.setVisibility(View.GONE);
        }else{
            tvLabelAttachment.setVisibility(View.VISIBLE);
            tvAttachment.setVisibility(View.VISIBLE);
            url = selectedEvent.getAttachmentUrl();
            fileName = url.substring(url.lastIndexOf('/') + 1);
            fileName1 = fileName.substring(0, fileName.lastIndexOf('?'));
            fileName2 = fileName1.split("%2F");
            System.out.println("fileName2 "+fileName2[1]);
            tvAttachment.setText(""+fileName2[1]);
        }
        if(!TextUtils.isEmpty(selectedEvent.getBatchId())) {
            batchDocRef = batchCollectionRef.document("/" + selectedEvent.getBatchId());
            batchDocRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Batch batch = documentSnapshot.toObject(Batch.class);
                            tvClass.setText("" + batch.getName());
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }else{
            tvClass.setVisibility(View.GONE);
        }
        tvAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadManager=(DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri=Uri.parse(url);
                DownloadManager.Request request=new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(getContext(),DIRECTORY_DOWNLOADS,fileName2[1]);
                downloadManager.enqueue(request);
                Toast.makeText(getContext(),"Download started",Toast.LENGTH_SHORT).show();
            }
        });

        rgResponse.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(rbMayBe.isChecked()){
                    response = "May be";
                }

                if(rbNo.isChecked()){
                    response = "No";
                }

                if(rbYes.isChecked()){
                    response = "Yes";
                }
            }
        });

        btnSaveResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(response)){
                    Toast.makeText(getContext(), "Please select any one of above response", Toast.LENGTH_SHORT).show();
                    return;
                }
                responseMap.put(loggedInUser.getId(),response);
                selectedEvent.setParentResponses(responseMap);
                eventCollectionRef.document(selectedEvent.getId())
                        .set(selectedEvent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FragmentEvent fragmentEvent= new FragmentEvent();
                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                fragmentTransaction.replace(R.id.contentLayout, fragmentEvent);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        });
        btnUpdateResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseMap.put(loggedInUser.getId(),response);
                selectedEvent.setParentResponses(responseMap);
                eventCollectionRef.document(selectedEvent.getId())
                        .set(selectedEvent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FragmentEvent fragmentEvent= new FragmentEvent();
                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                fragmentTransaction.replace(R.id.contentLayout, fragmentEvent);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        });
    }

    BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            openFile();
        }
    };

    //TODO
    private void openFile(){
        Toast.makeText(getContext(),"Download Complete",Toast.LENGTH_SHORT).show();
       /* Intent install = new Intent(Intent.ACTION_VIEW);
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        +"/"+fileName2[1];
        System.out.println("File Path - "+filePath);
        File file = new File(filePath);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);
        System.out.println("Type - "+type);

        */
        //if (type == null) {
        //type = "*/*";
        //}
        /*
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);
        System.out.println("data - "+data);
        intent.setDataAndType(data, type);
        startActivity(intent);
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(broadcast, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcast);
    }
}
