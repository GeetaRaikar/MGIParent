package com.padmajeet.mgi.techforedu.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.padmajeet.mgi.techforedu.parent.model.FeeComponent;
import com.padmajeet.mgi.techforedu.parent.model.FeeStructure;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.model.StudentFees;
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
public class FragmentStudentFees extends Fragment {
    private Gson gson;
    private Student loggedInUserStudent;
    private String loggedInUserStudentId;
    private LinearLayout llNoList;
    private List<StudentFees> studentFeesList = new ArrayList<>();
    private StudentFees studentFees;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference studentFeesCollectionRef = db.collection("StudentFees");
    private CollectionReference feeStructureCollectionRef = db.collection("FeeStructure");
    private CollectionReference feeComponentCollectionRef = db.collection("FeeComponent");
    private ListenerRegistration studentFeesListener;
    private FeeStructure feeStructure;
    private List<FeeStructure> feeStructureList = new ArrayList<>();
    private RecyclerView rvStudentFees;
    private RecyclerView.Adapter studentFeesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private float totalFees=0;
    private float paidFees=0;
    private TextView tvPendingFees,tvTotalFees,tvTotalAmount;
    private TableLayout tlFeeStructure;
    private int count=1;
    private LinearLayout llStudentFeeDetails,llFeeStructure,llFees;
    private Boolean visible=true;
    private SweetAlertDialog pDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        loggedInUserStudentId= sessionManager.getString("loggedInUserStudentId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentStudentFees() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_fees, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.studentFees));
        llStudentFeeDetails=view.findViewById(R.id.llStudentFeeDetails);
        rvStudentFees=view.findViewById(R.id.rvStudentFees);
        layoutManager = new LinearLayoutManager(getContext());
        rvStudentFees.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        tvPendingFees=view.findViewById(R.id.tvPendingFees);
        tvTotalFees=view.findViewById(R.id.tvTotalFees);
        tvTotalAmount=view.findViewById(R.id.tvTotalAmount);
        tlFeeStructure=view.findViewById(R.id.tlFeeStructure);
        llFeeStructure=view.findViewById(R.id.llFeeStructure);
        llFees=view.findViewById(R.id.llFees);
        getFeeStructure();
        tvTotalFees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visible){
                    tvTotalFees.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
                    llFeeStructure.setVisibility(View.VISIBLE);
                    visible=false;
                }else{
                    tvTotalFees.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);
                    llFeeStructure.setVisibility(View.GONE);
                    visible=true;
                }
            }
        });
    }
    private void getFeeStructure() {
        if(loggedInUserStudent != null) {
            totalFees = 0;
            if (feeStructureList.size() != 0) {
                feeStructureList.clear();
            }

            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }

            feeStructureCollectionRef
                    .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            System.out.print("FeeStructure addOnSuccessListener");
                            if(feeStructureList.size() > 0) {
                                feeStructureList.clear();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                feeStructure = document.toObject(FeeStructure.class);
                                feeStructure.setId(document.getId());
                                feeStructureList.add(feeStructure);
                            }
                            if (feeStructureList.size() != 0) {
                                llStudentFeeDetails.setVisibility(View.VISIBLE);
                                for (FeeStructure feeStructure : feeStructureList) {
                                    totalFees = totalFees + feeStructure.getAmount();
                                }
                                tvTotalFees.setText("₹ " + totalFees);
                                tvTotalAmount.setText("" + totalFees);
                                setFeeStructure();
                            } else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                llFees.setVisibility(View.GONE);
                                llStudentFeeDetails.setVisibility(View.GONE);
                                rvStudentFees.setVisibility(View.GONE);
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
                            rvStudentFees.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                            System.out.print("FeeStructure addOnFailureListener1");
                        }
                    });

        }else{
            //loggedInUserStudent == null
        }

    }

    private void getStudentFees() {
        if(loggedInUserStudentId != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            studentFeesListener = studentFeesCollectionRef
                    .whereEqualTo("studentId", loggedInUserStudentId)
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (studentFeesList.size() != 0) {
                                studentFeesList.clear();
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                studentFees = document.toObject(StudentFees.class);
                                studentFees.setId(document.getId());
                                studentFeesList.add(studentFees);
                            }
                            if (studentFeesList.size() >= 1) {
                                for (StudentFees studentFees : studentFeesList) {
                                    if (studentFees.getAmount() == 0) {
                                        studentFeesList.remove(studentFees);
                                        break;
                                    }
                                }
                            }
                            if (studentFeesList.size() != 0) {
                                System.out.println("StudentFee size - " + studentFeesList.size());
                                for (StudentFees st : studentFeesList) {
                                    paidFees = paidFees + st.getAmount();
                                }
                                studentFeesAdapter = new StudentFeesAdapter(studentFeesList);
                                rvStudentFees.setAdapter(studentFeesAdapter);
                                rvStudentFees.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                llFees.setVisibility(View.GONE);
                                rvStudentFees.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                            float pendingFees = totalFees - paidFees;
                            tvPendingFees.setText("₹ " + pendingFees);
                        }
                    });
        }

    }

    class StudentFeesAdapter extends RecyclerView.Adapter<StudentFeesAdapter.MyViewHolder> {
        private List<StudentFees> studentFeesList;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_student_fees, parent, false);

            return new MyViewHolder(v);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvInstalment, tvPaidFees, tvDate;
            public ImageView ivPaymentOption;


            public MyViewHolder(View itemView) {
                super(itemView);
                tvInstalment=itemView.findViewById(R.id.tvInstalment);
                tvDate=itemView.findViewById(R.id.tvDate);
                tvPaidFees=itemView.findViewById(R.id.tvPaidFees);
                ivPaymentOption=itemView.findViewById(R.id.ivPaymentOption);
            }
        }


        public StudentFeesAdapter(List<StudentFees> studentFeesList) {
            this.studentFeesList = studentFeesList;
        }



        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final StudentFees studentFees = studentFeesList.get(position);
            int size=studentFeesList.size();
            for (int i=1;i<=size;i++){
                if(count==i){
                    holder.tvInstalment.setText("Instalment "+count);
                    break;
                }
            }
            if(studentFees.getPaymentOption().equalsIgnoreCase("Card")){
                Glide.with(getContext())
                        .load(R.drawable.ic_card)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.ivPaymentOption);
            }else {
                if(studentFees.getPaymentOption().equalsIgnoreCase("Cash")) {
                    Glide.with(getContext())
                            .load(R.drawable.ic_cash)
                            .fitCenter()
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.ivPaymentOption);
                }
                else{
                    if(studentFees.getPaymentOption().equalsIgnoreCase("Cheque")) {
                        Glide.with(getContext())
                                .load(R.drawable.ic_check)
                                .fitCenter()
                                .apply(RequestOptions.circleCropTransform())
                                .into(holder.ivPaymentOption);
                    }else{
                        if(studentFees.getPaymentOption().equalsIgnoreCase("NetBanking")) {
                            Glide.with(getContext())
                                    .load(R.drawable.ic_net_banking)
                                    .fitCenter()
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(holder.ivPaymentOption);
                        }else{
                            Glide.with(getContext())
                                    .load(R.drawable.ic_upi)
                                    .fitCenter()
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(holder.ivPaymentOption);
                        }
                    }
                }
            }
            holder.tvDate.setText(""+Utility.formatDateToString(studentFees.getCreatedDate().getTime()));
            holder.tvPaidFees.setText("+ ₹ "+studentFees.getAmount());
            count++;
        }

        @Override
        public int getItemCount() {
            return studentFeesList.size();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (studentFeesListener != null) {
            studentFeesListener.remove();
        }
    }

    private void setFeeStructure(){
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        for (int i = 0; i < feeStructureList.size(); i++) {
            final FeeStructure feeStructure = feeStructureList.get(i);
            TableRow tableRow = new TableRow(getContext());
            View row = getActivity().getLayoutInflater().inflate(R.layout.row_fee_structure, tableRow, true);
            TextView tvFeeComponent = (TextView) row.findViewById(R.id.tvFeeComponent);
            TextView tvAmount= (TextView) row.findViewById(R.id.tvAmount);
            feeComponentCollectionRef.document("/" + feeStructure.getFeeComponentId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            FeeComponent feeComponent = documentSnapshot.toObject(FeeComponent.class);
                            tvFeeComponent.setText("" + feeComponent.getName());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
            tvAmount.setText(""+feeStructure.getAmount());
            tlFeeStructure.addView(tableRow);
        }
        if (pDialog != null) {
            pDialog.dismiss();
        }
        getStudentFees();
    }
}
