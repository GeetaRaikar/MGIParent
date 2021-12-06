package com.padmajeet.mgi.techforedu.parent;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Achievement;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAchievements extends Fragment {
    private Gson gson;
    private View view;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference achievementCollectionRef = db.collection("Achievement");
    private ListenerRegistration achievementListener;
    private ArrayList<Achievement> achievementList=new ArrayList<>();
    private LinearLayout llNoList;
    private Student loggedInUserStudent;
    //private String academicYearId;
    private Achievement achievement;
    private RecyclerView rvAchievement;
    private RecyclerView.Adapter achievementAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Parent loggedInUser;
    private SweetAlertDialog pDialog;

    public FragmentAchievements() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);
        String parentJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(parentJson, Parent.class);
        //academicYearId = sessionManager.getString("academicYearId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_achievements, container, false);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.achievement));
        rvAchievement = view.findViewById(R.id.rvAchievement);
        layoutManager = new LinearLayoutManager(getContext());
        rvAchievement.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        getAchievementList();
        return view;
    }

    private void  getAchievementList() {
        if(achievementList.size()!=0){
            achievementList.clear();
        }
        if(loggedInUser != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            achievementListener = achievementCollectionRef
                    .whereEqualTo("instituteId", loggedInUser.getInstituteId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (achievementList.size() != 0) {
                                achievementList.clear();
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                achievement = document.toObject(Achievement.class);
                                achievement.setId(document.getId());
                                if (achievement.getVisibility().equalsIgnoreCase("AP") || achievement.getVisibility().equalsIgnoreCase("ALL")) {
                                    achievementList.add(achievement);
                                } else {
                                    if (achievement.getVisibility().equalsIgnoreCase("P")) {
                                        if (achievement.getBatchIdList().contains(loggedInUserStudent.getCurrentBatchId())) {
                                            achievementList.add(achievement);
                                        }
                                    }
                                }
                            }
                            if (achievementList.size() != 0) {
                                //System.out.println("achievementList -"+achievementList.size());
                                achievementAdapter = new AchievementAdapter(achievementList);
                                rvAchievement.setAdapter(achievementAdapter);
                                rvAchievement.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            } else {
                                rvAchievement.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            }
                        }
                    });
        }else{
            // loggedInUser is not available.
        }

    }

    class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.MyViewHolder> {
        private List<Achievement> achievementList;

        public AchievementAdapter(List<Achievement> achievementList) {
            this.achievementList = achievementList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_achievements, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            final Achievement achievement = achievementList.get(position);
            String sub=achievement.getSubject().trim();
            holder.tvSubject.setText("" + sub);
            if(TextUtils.isEmpty(achievement.getDescription())){
               holder.tvDes.setVisibility(View.GONE);
            }else {
                String des=achievement.getDescription().trim();
                holder.tvDes.setText("" + des);
            }
            holder.tvDate.setText("" + Utility.formatDateTimeToString(achievement.getCreatedDate().getTime()));
            if(achievement.getImageUrlList().size() > 0){
                ViewPagerAdapter adapter = new ViewPagerAdapter(getContext(),achievement.getImageUrlList());
                holder.viewPager.setAdapter(adapter);
                if(adapter.getCount() == 1){
                    holder.dotsIndicator.setVisibility(View.GONE);
                }else {
                    holder.dotsIndicator.setViewPager(holder.viewPager);
                }
            }else{
                holder.viewPager.setVisibility(View.GONE);
                holder.dotsIndicator.setVisibility(View.GONE);
            }


        }

        @Override
        public int getItemCount() {
            return achievementList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvDate, tvDes;
            public ViewPager viewPager;
            public View row;
            public WormDotsIndicator dotsIndicator;


            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvDes = view.findViewById(R.id.tvDesc);
                tvDate = view.findViewById(R.id.tvDate);
                viewPager = view.findViewById(R.id.viewPager);
                dotsIndicator = view.findViewById(R.id.dotsIndicator);
                row = view;
            }
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {
        Context context;
        List<String> imageUrls;

        ViewPagerAdapter(Context context, List<String> imageUrlList) {
            this.context = context;
            this.imageUrls = imageUrlList;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            Glide.with(getContext())
                    .load(imageUrls.get(position))
                    //.placeholder(R.drawable.loading)
                    .fitCenter()
                    .into(imageView);
            container.addView(imageView);

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (achievementListener != null) {
            achievementListener.remove();
        }
    }
}
