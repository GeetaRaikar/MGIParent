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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSubject extends Fragment {
    private LinearLayout llNoList;
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private RecyclerView rvSubject;
    private RecyclerView.Adapter subjectAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Student loggedInUserStudent;
    private Gson gson;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private ListenerRegistration subjectListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
    }
    public FragmentSubject() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.subject));
        llNoList = view.findViewById(R.id.llNoList);
        rvSubject = view.findViewById(R.id.rvSubject);
        layoutManager = new LinearLayoutManager(getContext());
        pDialog=Utility.createSweetAlertDialog(getContext());
        rvSubject.setLayoutManager(layoutManager);
        getSubjects();
    }

    private void getSubjects() {
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        subjectListener=subjectCollectionRef
                .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (subjectList.size() != 0) {
                            subjectList.clear();
                        }
                        for (DocumentSnapshot document:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Subject subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
                        if (subjectList.size() != 0) {
                            subjectAdapter = new SubjectAdapter(subjectList);
                            rvSubject.setAdapter(subjectAdapter);
                            rvSubject.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvSubject.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.MyViewHolder> {
        private List<Subject> subjectList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvSubjectType;
            public ImageView ivSubjectPic;
            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvSubjectType = view.findViewById(R.id.tvSubjectType);
                ivSubjectPic = view.findViewById(R.id.ivSubjectPic);
            }
        }

        public SubjectAdapter(List<Subject> subjectList) {
            this.subjectList = subjectList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_subject, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Subject subject = subjectList.get(position);
            holder.tvSubject.setText("" + subject.getName());
            if(TextUtils.isEmpty(subject.getType())){
                holder.tvSubjectType.setVisibility(View.GONE);
            }else{
                holder.tvSubjectType.setText("" + subject.getType());
            }

            String imageUrl = subject.getImageUrl();
            if(!TextUtils.isEmpty(imageUrl)){
                Glide.with(getContext())
                        .load(imageUrl)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.ivSubjectPic);
            }
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }
    }
}
