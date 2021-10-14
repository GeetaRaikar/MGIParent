package com.padmajeet.mgi.techforedu.parent;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.parent.model.Attendance;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Subject;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentDaywiseAttendance extends Fragment {

    private ListView lvDayWiseAttendance;
    private LinearLayout llNoList;
    private ImageView ivNoData;
    private TextView tvNoData;
    private Gson gson;
    private Parent loggedInUser;
    private String academicYearId, instituteId;
    private List<Attendance> attendanceList;
    private List<Subject> subjectList;
    private HashMap<Integer,List<Attendance>> attendanceHashMap = new HashMap<>();
    private List<List<Attendance>> daywiseAttendanceList = new ArrayList<>();
    private List<Integer> countList = new ArrayList<>();
    private SessionManager sessionManager;
    public FragmentDaywiseAttendance() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson, Parent.class);
        academicYearId= sessionManager.getString("academicYearId");
        instituteId=loggedInUser.getInstituteId();
        System.out.println("FragmentDaywiseAttendance");
        String attendanceListJson = sessionManager.getString("attendanceList");
        System.out.println("attendanceListJson - "+attendanceListJson);
        attendanceList = gson.fromJson(attendanceListJson,new TypeToken<List<Attendance>>(){}.getType());
        String subjectListJson = sessionManager.getString("subjectList");
        System.out.println("subjectListJson - "+subjectListJson);
        subjectList = gson.fromJson(subjectListJson,new TypeToken<List<Subject>>(){}.getType());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daywise_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.daywiseAttendance));
        lvDayWiseAttendance = view.findViewById(R.id.lvDayWiseAttendance);
        llNoList = view.findViewById(R.id.llNoList);
        tvNoData = view.findViewById(R.id.tvNoData);
        ivNoData = view.findViewById(R.id.ivNoData);
        //getAttendanceOfStudent();

        if(attendanceList!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            int k =0;
            for(int i=0;i<attendanceList.size();i++){
                Date attendanceDate = attendanceList.get(i).getDate();
                if(attendanceDate!=null){
                    List<Attendance> tempAttendanceList = new ArrayList<>();
                    tempAttendanceList.add(attendanceList.get(i));
                    System.out.println("First Date - "+attendanceDate);
                    for(int j=i+1;j<attendanceList.size();j++){
                        Date nextDate = attendanceList.get(j).getDate();
                        System.out.println("Next Date - "+nextDate);
                        if(sdf.format(attendanceDate).equals(sdf.format(nextDate))){
                            System.out.println("Matched");
                            tempAttendanceList.add(attendanceList.get(j));
                            i++;
                        }
                        else{
                            break;
                        }
                    }
                    String keyDate = sdf.format(attendanceDate);

                    attendanceHashMap.put(k,tempAttendanceList);
                    daywiseAttendanceList.add(tempAttendanceList);
                    countList.add(k);
                    k++;
                }
            }
            System.out.println("HashMap size - "+attendanceHashMap.size());
            if(daywiseAttendanceList.size()>0){

                AttendanceAdaptor attendanceAdaptor = new AttendanceAdaptor(getContext());
                lvDayWiseAttendance.setAdapter(attendanceAdaptor);
            }
        }
        else{
            lvDayWiseAttendance.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }
/*
    private void getAttendanceOfStudent(){
        new AsyncTask<Void,Void,String>(){

            SweetAlertDialog pDialog;
            @Override
            protected void onPreExecute() {
                pDialog = Utility.createSweetAlertDialog(getContext());
                if(pDialog!=null){
                    pDialog.show();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                String url = getString(R.string.baseUrl)+"AttendanceService/getAttendancesOfStudent/"+loggedInUser.getStudentId().getId()+"/"+loggedInUser.getStudentId().getCurrentBatchId().getId()+"/"+currentAcademicYear.getId();
                return new HttpManager().getData(url);
            }

            @Override
            protected void onPostExecute(String result) {
                //super.onPostExecute(s);
                if(pDialog!=null){
                    pDialog.dismiss();
                }
                System.out.println("Result - "+result);
                if(!TextUtils.isEmpty(result)){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    attendanceList = gson.fromJson(result,new TypeToken<List<Attendance>>(){}.getType());
                    sessionManager.putString("attendanceList",result);
                    System.out.println("Main List size - "+attendanceList.size());
                    int k =0;
                    for(int i=0;i<attendanceList.size();i++){
                        Date attendanceDate = attendanceList.get(i).getDate();
                        if(attendanceDate!=null){
                            List<Attendance> tempAttendanceList = new ArrayList<>();
                            tempAttendanceList.add(attendanceList.get(i));
                            System.out.println("First Date - "+attendanceDate);
                            for(int j=i+1;j<attendanceList.size();j++){
                                Date nextDate = attendanceList.get(j).getDate();
                                System.out.println("Next Date - "+nextDate);
                                if(sdf.format(attendanceDate).equals(sdf.format(nextDate))){
                                    System.out.println("Matched");
                                    tempAttendanceList.add(attendanceList.get(j));
                                    i++;
                                }
                                else{
                                    break;
                                }
                            }
                            String keyDate = sdf.format(attendanceDate);

                            attendanceHashMap.put(k,tempAttendanceList);
                            daywiseAttendanceList.add(tempAttendanceList);
                            countList.add(k);
                            k++;
                        }
                    }
                    System.out.println("HashMap size - "+attendanceHashMap.size());
                    if(daywiseAttendanceList.size()>0){

                        AttendanceAdaptor attendanceAdaptor = new AttendanceAdaptor(getContext());
                        lvDayWiseAttendance.setAdapter(attendanceAdaptor);
                    }
                }
                else{
                    lvDayWiseAttendance.setVisibility(View.GONE);
                    llNoList.setVisibility(View.VISIBLE);
                }
            }
        }.execute();

    }
*/
    class AttendanceAdaptor extends ArrayAdapter<Integer>{
        Context context;
        public AttendanceAdaptor(@NonNull Context context) {
            super(context, R.layout.row_daywise_attendance,countList);
            this.context = context;
        }
        private class ViewHolder{
            private TextView date;
            private Button session1,session2,session3,session4,session5,session6,session7;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            System.out.println("position - "+position);
            View row = convertView;
            ViewHolder holder = new ViewHolder();
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.row_daywise_attendance,parent,false);
                holder.date = (TextView) row.findViewById(R.id.date);
                holder.session1 = (Button) row.findViewById(R.id.session1);
                holder.session2 = (Button) row.findViewById(R.id.session2);
                holder.session3 = (Button) row.findViewById(R.id.session3);
                holder.session4 = (Button) row.findViewById(R.id.session4);
                holder.session5 = (Button) row.findViewById(R.id.session5);
                holder.session6 = (Button) row.findViewById(R.id.session6);
                holder.session7 = (Button) row.findViewById(R.id.session7);
                row.setTag(holder);
            }
            else{
                holder = (ViewHolder) row.getTag();
            }
            List<Attendance> currentAttendanceList = attendanceHashMap.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
            String currentDate = sdf.format(currentAttendanceList.get(0).getDate());
            holder.date.setText(""+currentDate);
            for(int i=0;i<currentAttendanceList.size();i++){
                Attendance curAttendance = currentAttendanceList.get(i);
                String subName = "";
                for (Subject subject:subjectList){
                    if(curAttendance.getSubjectId().equals(subject.getId())){
                        subName = subject.getCode();
                        break;
                    }
                }
                switch(i){
                    case 0: holder.session1.setVisibility(View.VISIBLE);
                        holder.session1.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session1.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session1.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 1: holder.session2.setVisibility(View.VISIBLE);
                        holder.session2.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session2.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 2: holder.session3.setVisibility(View.VISIBLE);
                        holder.session3.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session3.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 3: holder.session4.setVisibility(View.VISIBLE);
                        holder.session4.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session4.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session4.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 4: holder.session5.setVisibility(View.VISIBLE);
                        holder.session5.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session5.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session5.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 5: holder.session6.setVisibility(View.VISIBLE);
                        holder.session1.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session6.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session6.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 6: holder.session7.setVisibility(View.VISIBLE);
                        holder.session7.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session7.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session7.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                }
            }
            return row;
        }
    }
}
