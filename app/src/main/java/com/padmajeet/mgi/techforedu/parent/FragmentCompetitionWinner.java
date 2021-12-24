package com.padmajeet.mgi.techforedu.parent;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.padmajeet.mgi.techforedu.parent.model.Competition;
import com.padmajeet.mgi.techforedu.parent.model.CompetitionWinner;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCompetitionWinner extends Fragment {

    private ExpandableListView expListView;
    private LinearLayout llNoList;
    private List<CompetitionWinner> competitionWinnerList = new ArrayList<>();
    private CompetitionWinner competitionWinner;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference competitionWinnerCollectionRef = db.collection("CompetitionWinner");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference competitionCollectionRef = db.collection("Competition");
    private ListenerRegistration competitionListener,competitionWinnerListener;
    private List<Competition> competitionList=new ArrayList<>();
    private Competition competition;
    private Student loggedInUserStudent;
    private String loggedInUserStudentId;
    private String academicYearId;
    private Gson gson;
    private List<String> listDataHeader = new ArrayList<String>();
    private HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
    private ExpandableListAdapter listAdapter;
    private Student student;
    private SweetAlertDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        loggedInUserStudentId = sessionManager.getString("loggedInUserStudentId");
        academicYearId = sessionManager.getString("academicYearId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentCompetitionWinner() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_competition_winner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.competitionWinner));
        expListView = view.findViewById(R.id.lvExp);
        llNoList = view.findViewById(R.id.llNoList);
        getCompetitionList();
    }
    private void getCompetitionList(){
        if(academicYearId != null && loggedInUserStudent != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            competitionListener = competitionCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", loggedInUserStudent.getCurrentBatchId())
                    .orderBy("fromDate", Query.Direction.DESCENDING)
                    .orderBy("toDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (competitionList.size() != 0) {
                                competitionList.clear();
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                competition = document.toObject(Competition.class);
                                competition.setId(document.getId());
                                competitionList.add(competition);
                            }
                            if (competitionList.size() > 0) {
                                getCompetitionEventList();
                            } else {
                                expListView.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            //academicYearId , loggedInUserStudent == null
        }
    }

    private void getCompetitionEventList() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        if(competitionList.size()!=0) {
            if (listDataHeader != null) {
                listDataHeader.clear();
            }
            for (int i = 0; i < competitionList.size(); i++) {
                Competition competitionObj = competitionList.get(i);
                //Adding Header Data
                String competitionJson = gson.toJson(competitionObj);
                //System.out.println("Header values of event "+eventObj.getName());
                listDataHeader.add(competitionJson);
                getCompetitionWinnerList(i,competitionObj.getId());
            }
            expListView.setVisibility(View.VISIBLE);
            llNoList.setVisibility(View.GONE);
        }
        else {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            expListView.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }

    private void getCompetitionWinnerList(final int i, final String eventId){
        if (competitionWinnerList.size() != 0) {
            competitionWinnerList.clear();
        }
        competitionWinnerListener = competitionWinnerCollectionRef
                .whereEqualTo("competitionId", eventId)
                .orderBy("rank", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (competitionWinnerList.size() != 0) {
                            competitionWinnerList.clear();
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document:queryDocumentSnapshots.getDocuments()) {
                            competitionWinner = document.toObject(CompetitionWinner.class);
                            competitionWinner.setId(document.getId());
                            competitionWinnerList.add(competitionWinner);
                        }
                        //Collections.sort(competitionWinnerList, CompetitionWinner.competitionWinnerRankComparator);
                        List<String> competitionWinnerNameList = new ArrayList<String>();
                        for (CompetitionWinner competitionWinnerName : competitionWinnerList) {
                            String competitionJson = gson.toJson(competitionWinnerName);
                            competitionWinnerNameList.add(competitionJson);
                        }
                        //System.out.println("competitionWinnerNameList " + competitionWinnerNameList.size());
                        listDataChild.put(listDataHeader.get(i), competitionWinnerNameList);
                        if (listDataChild.size() == competitionList.size()) {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            displayExpandableList();
                        }
                    }
                });
        // [END get_all_users]
    }

    private void displayExpandableList(){

        if(listDataHeader==null){
            expListView.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
        expListView.setVisibility(View.VISIBLE);
        llNoList.setVisibility(View.GONE);
        // setting list adapter
        expListView.setAdapter(listAdapter);
        listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {


            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;

        public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);
            gson = Utility.getGson();
            final CompetitionWinner competitionWinner = gson.fromJson(childText, CompetitionWinner.class);
            System.out.println("competitionWinner "+competitionWinner.getBatchId());
            //Type type = new TypeToken<List<Exam>>(){}.getType();
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.row_competition_winner, null);
            }

            TextView tvRank = convertView.findViewById(R.id.tvRank);
            final TextView tvStudentName = convertView.findViewById(R.id.tvStudentName);
            final LinearLayout llCompetitionWinner = convertView.findViewById(R.id.llCompetitionWinner);
            final ImageView ivProfilePic = convertView.findViewById(R.id.ivProfilePic);
            final TextView tvMessage = convertView.findViewById(R.id.tvMessage);
            tvRank.setText("" + competitionWinner.getRank());
            studentCollectionRef
                    .document("/" + competitionWinner.getStudentId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                student = documentSnapshot.toObject(Student.class);
                                student.setId(documentSnapshot.getId());
                                String studentName = student.getFirstName() + " " + student.getMiddleName() + " " + student.getLastName();
                                if (student.getId().equals(loggedInUserStudentId)) {
                                    tvMessage.setVisibility(View.VISIBLE);
                                    tvMessage.setText("Hurray!!! " + student.getFirstName() + " has won it...");
                                    tvStudentName.setTextColor(getResources().getColor(R.color.colorBlack));
                                }
                                tvStudentName.setText("" + studentName);
                                Glide.with(getContext())
                                        .load(student.getImageUrl())
                                        .fitCenter()
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.ic_student)
                                        .into(ivProfilePic);
                            }
                        })
                    .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if(this._listDataChild.get(this._listDataHeader.get(groupPosition)) == null){
                System.out.println("competitionWinner null");
                return 0;
            }else{
                System.out.println("Size "+this._listDataChild.get(this._listDataHeader.get(groupPosition)).size());
                /*if(this._listDataChild.get(this._listDataHeader.get(groupPosition)).size() == 0){
                    llNoCompetitionWinner
                }*/
                return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            gson = Utility.getGson();
            //Type type = new TypeToken<List<ExamSeries>>(){}.getType();
            final Competition competition = gson.fromJson(headerTitle, Competition.class);
            System.out.println("event expanded" + competition.getName());
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.row_competition, null);
            }
            TextView tvName = convertView.findViewById(R.id.tvName);
            TextView tvDate = convertView.findViewById(R.id.tvDate);
            TextView tvNoCompetitionWinner = convertView.findViewById(R.id.tvNoCompetitionWinner);
            tvName.setText("" + competition.getName());
            String date = Utility.formatDateToString(competition.getFromDate().getTime());
            if (competition.getToDate() != null) {
                date = date +" - "+ Utility.formatDateToString(competition.getToDate().getTime());
            }
            tvDate.setText("" + date);
            final View finalConvertView = convertView;
            ImageView ivProfilePic = finalConvertView.findViewById(R.id.ivProfilePic);
            if(this._listDataChild.get(this._listDataHeader.get(groupPosition)).size() == 0){
                tvNoCompetitionWinner.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (competitionListener != null) {
            competitionListener.remove();
        }
        if (competitionWinnerListener != null) {
            competitionWinnerListener.remove();
        }
    }
}