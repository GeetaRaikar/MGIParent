package com.padmajeet.mgi.techforedu.parent;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Feedback;
import com.padmajeet.mgi.techforedu.parent.model.FeedbackCategory;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentFeedBack extends Fragment {
    private View view=null;
    private Gson gson;
    private String loggedInUserId;
    private Student loggedInUserStudent;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference feedBackCollectionRef=db.collection("Feedback");
    private CollectionReference feedBackCategoryCollectionRef=db.collection("FeedbackCategory");
    private Feedback feedback;
    private FeedbackCategory feedbackCategory;
    private List<Feedback> feedBackList=new ArrayList<>();
    private List<FeedbackCategory> feedbackCategoryList=new ArrayList<>();
    private FeedbackCategory selectedFeedBackCategory;
    private RecyclerView rvFeedBack;
    private RecyclerView.Adapter feedBackAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LinearLayout llNoList;
    private Spinner spFeedBackCategory;
    private EditText etFeedBack;
    private ImageView ivSubmit;
    private ArrayAdapter<String> adaptor;
    private String instituteId;
    private List<String> nameList = new ArrayList<>();
    private int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_green_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};
    private Fragment currentFragment;
    private SweetAlertDialog pDialog;
    private FloatingActionButton fab;

    public FragmentFeedBack() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        loggedInUserId= sessionManager.getString("loggedInUserId");
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
        currentFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_feed_back, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.noteToTeacher));
        rvFeedBack = view.findViewById(R.id.rvFeedBack);
        layoutManager = new LinearLayoutManager(getContext());
        rvFeedBack.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);

        getFeedBackCategory();
        getFeedBack();

        fab = view.findViewById(R.id.addFeedback);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBottomSheet();
                bottomSheetDialog.show();
            }
        });
        return view;
    }

    BottomSheetDialog bottomSheetDialog;

    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_feed_back, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(view);
            ivSubmit=view.findViewById(R.id.ivSubmit);
            spFeedBackCategory=view.findViewById(R.id.spFeedBackCategory);
            etFeedBack=view.findViewById(R.id.etFeedBack);

            if(feedbackCategoryList.size()!=0) {
                adaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nameList);
                adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spFeedBackCategory.setAdapter(adaptor);

                spFeedBackCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedFeedBackCategory = feedbackCategoryList.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            ivSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strFeedback = etFeedBack.getText().toString().trim();
                    if (TextUtils.isEmpty(strFeedback)) {
                        etFeedBack.setError("Please enter valid note");
                        etFeedBack.requestFocus();
                        return;
                    }else{
                        if(Utility.isTextValid(strFeedback)){
                            etFeedBack.setError("Please enter valid note");
                            etFeedBack.requestFocus();
                            return;
                        }
                    }
                    if(selectedFeedBackCategory!=null) {
                        feedback = new Feedback();
                        feedback.setFeedbackCategoryId(selectedFeedBackCategory.getId());
                        feedback.setFeedback(strFeedback);
                        feedback.setReviewerType("P");
                        feedback.setReviewerId(loggedInUserId);
                        feedback.setBatchId(loggedInUserStudent.getCurrentBatchId());
                        feedback.setCreatorId(loggedInUserId);
                        feedback.setModifierId(loggedInUserId);
                        feedback.setCreatorType("P");
                        feedback.setModifierType("P");

                        addFeedback();
                        etFeedBack.setText("");
                    }
                }
            });
        }
    }

    private void addFeedback() {
        bottomSheetDialog.dismiss();
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        feedBackCollectionRef
                .add(feedback)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                        getFeedBack();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });
        // [END add_document]

    }

    private void getFeedBackCategory(){
        if(instituteId != null) {
            if(pDialog != null && !pDialog.isShowing()){
                pDialog.show();
            }
            feedBackCategoryCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    feedbackCategory = document.toObject(FeedbackCategory.class);
                                    feedbackCategory.setId(document.getId());
                                    System.out.println("FeedbackCategory -" + feedbackCategory.getCategory());
                                    feedbackCategoryList.add(feedbackCategory);
                                    nameList.add(feedbackCategory.getCategory());
                                }
                                if(feedbackCategoryList.size() == 0){
                                    fab.hide();
                                }else{
                                    fab.show();
                                }
                            } else {
                                fab.show();
                            }
                        }
                    });
        }else{
            //instituteId == null
        }

    }
    private void getFeedBack() {
        if(loggedInUserStudent != null && loggedInUserId != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            if (feedBackList.size() != 0) {
                feedBackList.clear();
            }
            feedBackCollectionRef
                    .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                    .whereEqualTo("reviewerId", loggedInUserId)
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            System.out.println("Feedback  -" + task.getResult().size());
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    feedback = document.toObject(Feedback.class);
                                    feedback.setId(document.getId());
                                    feedBackList.add(feedback);
                                }
                                System.out.println("FeedBack  -" + feedBackList.size());
                                if (feedBackList.size() != 0) {
                                    feedBackAdapter = new FeedBackAdapter(feedBackList);
                                    rvFeedBack.setAdapter(feedBackAdapter);
                                    rvFeedBack.setVisibility(View.VISIBLE);
                                    llNoList.setVisibility(View.GONE);
                                } else {
                                    rvFeedBack.setVisibility(View.GONE);
                                    llNoList.setVisibility(View.VISIBLE);
                                }
                            } else {
                                rvFeedBack.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            //loggedInUserId , loggedInUserStudent == null
        }
    }

    class FeedBackAdapter extends RecyclerView.Adapter<FeedBackAdapter.MyViewHolder> {
        private List<Feedback> feedBackList;

        private FeedbackCategory updatedFeedbackCategory;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvFeedbackCategory, tvFeedback,tvDate,tvSubjectHeader;
            public LinearLayout llImage;
            public ImageView ivEditNote,ivDeleteNote;

            public MyViewHolder(View view) {
                super(view);
                tvFeedbackCategory = view.findViewById(R.id.tvFeedbackCategory);
                tvFeedback = view.findViewById(R.id.tvFeedback);
                tvDate = view.findViewById(R.id.tvDate);
                llImage = view.findViewById(R.id.llImage);
                tvSubjectHeader= view.findViewById(R.id.tvSubjectHeader);
                ivEditNote = view.findViewById(R.id.ivEditNote);
                ivDeleteNote = view.findViewById(R.id.ivDeleteNote);
            }
        }


        public FeedBackAdapter(List<Feedback> feedBackList) {
            this.feedBackList = feedBackList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_feed_back, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Feedback feedBack = feedBackList.get(position);
            holder.tvFeedback.setText(""+feedBack.getFeedback());
            int colorCode = position%5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            for(int i=0;i<feedbackCategoryList.size();i++){
                if(feedbackCategoryList.get(i).getId().equals(feedBack.getFeedbackCategoryId())){
                    holder.tvFeedbackCategory.setText(""+feedbackCategoryList.get(i).getCategory());
                    holder.tvSubjectHeader.setText(""+feedbackCategoryList.get(i).getCategory().toUpperCase().charAt(0));
                    break;
                }
            }
            holder.tvDate.setText(""+Utility.formatDateToString(feedBack.getCreatedDate().getTime()));

            holder.ivEditNote.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogLayout = inflater.inflate(R.layout.dialog_edit_feed_back, null);
                    final EditText etFeedBack = dialogLayout.findViewById(R.id.etFeedBack);
                    etFeedBack.setText("" + feedBack.getFeedback());
                    Spinner spFeedBackCategory = dialogLayout.findViewById(R.id.spFeedBackCategory);
                    ArrayAdapter<String> adaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nameList);
                    adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spFeedBackCategory.setAdapter(adaptor);
                    int categorySpinnerPos=0;
                    for(int i=0;i<feedbackCategoryList.size();i++){
                        if(feedbackCategoryList.get(i).getId().equals(feedBack.getFeedbackCategoryId())){
                            categorySpinnerPos = i;
                            break;
                        }
                    }
                    spFeedBackCategory.setSelection(categorySpinnerPos);



                    spFeedBackCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            updatedFeedbackCategory = feedbackCategoryList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setConfirmText("Update")
                            .setCustomView(dialogLayout)
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    String updatedNote = etFeedBack.getText().toString().trim();
                                    if(TextUtils.isEmpty(updatedNote)){
                                        etFeedBack.setError("Please enter valid note");
                                        etFeedBack.requestFocus();
                                        return;
                                    }else{
                                        if(Utility.isTextValid(updatedNote)){
                                            etFeedBack.setError("Please enter valid note");
                                            etFeedBack.requestFocus();
                                            return;
                                        }
                                    }
                                    feedBack.setFeedback(updatedNote);
                                    feedBack.setFeedbackCategoryId(updatedFeedbackCategory.getId());
                                    feedBack.setModifiedDate(new Date());
                                    feedBack.setModifierId(loggedInUserId);
                                    feedBack.setModifierType("P");
                                    //TODO
                                    //feedBack.setStatus("O");
                                    final SweetAlertDialog pDialog;
                                    pDialog = Utility.createSweetAlertDialog(getContext());
                                    pDialog.show();
                                    feedBackCollectionRef.document(feedBack.getId()).set(feedBack).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (pDialog != null) {
                                                    pDialog.dismiss();
                                                }
                                                sDialog.dismissWithAnimation();
                                                getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                                            } else {
                                                Toast.makeText(getContext(), "Update unsuccessful", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });

            holder.ivDeleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setConfirmText("Confirm")
                            .setContentText("Delete note permanently?")
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    feedBackCollectionRef.document(feedBack.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sDialog.dismissWithAnimation();
                                                    getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    sDialog.dismissWithAnimation();
                                                    Toast.makeText(getContext(), "Delete unsuccessful", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return feedBackList.size();
        }
    }

}
