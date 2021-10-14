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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Message;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMessageDetails extends Fragment {
    private TextView tvSubject,tvDate,tvDesc,tvCreatorType,tvClass,tvAttachment;
    private LinearLayout llAttachment;
    Message selectedMessage;
    FirebaseFirestore db= FirebaseFirestore.getInstance();

    CollectionReference batchCollectionRef=db.collection("Batch");
    DocumentReference batchDocRef;
    String[] fileName2;

    public FragmentMessageDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String selectedMessageJson = getArguments().getString("selectedMessage");
        Gson gson = Utility.getGson();
        selectedMessage = gson.fromJson(selectedMessageJson,Message.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvDate = view.findViewById(R.id.tvDate);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvCreatorType = view.findViewById(R.id.tvCreatorType);
        tvClass = view.findViewById(R.id.tvClass);
        llAttachment = view.findViewById(R.id.llAttachment);
        tvAttachment = view.findViewById(R.id.tvAttachment);

        tvSubject.setText(""+selectedMessage.getSubject());
        tvDate.setText("" + Utility.formatDateToString(selectedMessage.getCreatedDate().getTime()));

        if(selectedMessage.getCreatorType().equals("A")){
            tvCreatorType.setText("Admin");
        }
        else if(selectedMessage.getCreatorType().equals("F")){
            tvCreatorType.setText("Teacher");
        }
        tvDesc.setText(""+selectedMessage.getDescription());
        String url = selectedMessage.getAttachmentUrl();
        if(TextUtils.isEmpty(url)){
            llAttachment.setVisibility(View.GONE);
        }else{
            llAttachment.setVisibility(View.VISIBLE);

            String fileName = url.substring(url.lastIndexOf('/') + 1);
            String fileName1 = fileName.substring(0, fileName.lastIndexOf('?'));
            fileName2 = fileName1.split("%2F");
            System.out.println("fileName2 "+fileName2[1]);
            tvAttachment.setText(""+fileName2[1]);
        }

        tvAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadManager downloadManager=(DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri=Uri.parse(url);
                DownloadManager.Request request=new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(getContext(),DIRECTORY_DOWNLOADS,fileName2[1]);
                downloadManager.enqueue(request);
                Toast.makeText(getContext(),"Download started",Toast.LENGTH_SHORT).show();
                /*Show toast after 10 sec.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"Download Complete",Toast.LENGTH_SHORT).show();
                    }
                },10000);

                 */
            }
        });

/*
        batchDocRef = batchCollectionRef.document("/" + selectedMessage.getBatchId());
        batchDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Batch batch = documentSnapshot.toObject(Batch.class);
                        tvClass.setText(""+batch.getName());
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });*/
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
