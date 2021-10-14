package com.padmajeet.mgi.techforedu.parent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.parent.model.Parent;
import com.padmajeet.mgi.techforedu.parent.model.Student;
import com.padmajeet.mgi.techforedu.parent.util.NotificationWorker;
import com.padmajeet.mgi.techforedu.parent.util.SessionManager;
import com.padmajeet.mgi.techforedu.parent.util.Utility;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActivityHome extends AppCompatActivity {

    private Gson gson;
    private Parent loggedInUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DrawerLayout drawer;
    SessionManager sessionManager;
    private static final String uniqueWorkName = "com.padmajeet.eduapp.kiddie.parent.util.NotificaionWorker";
    private static final long repeatIntervalMin = 60;
    private static final long flexIntervalMin = 10;
    SweetAlertDialog dialog;
    private Student loggedInUserStudent;
    private NotificationManagerCompat notificationManagerCompat;

    private static final String CHANNEL_ID="Kiddies";
    private static final String CHANNEL_NAME="Kiddies";
    private static final String CHANNEL_DESC="Kiddies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }*/
         /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
          */
         if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
             NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
             channel.setDescription(CHANNEL_DESC);
             NotificationManager manager=getSystemService(NotificationManager.class);
             manager.createNotificationChannel(channel);
         }
        /*System.out.println("onStart ");
        NotificationCompat.Builder mBuilder=
                new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_active)
                .setContentTitle("Hurray! It is working")
                .setContentText("Your first notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat mNotificationMgr= NotificationManagerCompat.from(this);
        mNotificationMgr.notify(1,mBuilder.build());
        System.out.println("mNotificationMgr "+mNotificationMgr);*/



        sessionManager = new SessionManager(getApplicationContext());
        String userJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(userJson, Parent.class);
        String studentJson = sessionManager.getString("loggedInUserStudent");
        loggedInUserStudent = gson.fromJson(studentJson, Student.class);

        sessionManager.putString("isFragmentHome", "false");

        if(loggedInUserStudent != null) {
            setTokenForNotificationFetching();
        }

        //startNotificationWorker();
        View header = navigationView.getHeaderView(0);
        TextView tv_nav_name = header.findViewById(R.id.tv_nav_name);
        TextView tv_nav_mobnum = header.findViewById(R.id.tv_nav_mobnum);
        ImageView ivProfilePic = header.findViewById(R.id.ivProfilePic);
        if (loggedInUser != null) {
            tv_nav_name.setText(loggedInUser.getFirstName());
            tv_nav_mobnum.setText("" + loggedInUser.getMobileNumber());
           /*
            String imageUrl = getString(R.string.imageUrl)+loggedInUser.getImageUrl();
            //System.out.println("imageUrl "+imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_admin)
                    .into(ivProfilePic);
            */
        }


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation view item clicks here.
                int id = menuItem.getItemId();
                navigationView.setCheckedItem(id);
                switch (id) {

                    case R.id.nav_home:
                        replaceFragment(new FragmentHome(), getString(R.string.home));
                        break;
                    case R.id.nav_attendance:
                        replaceFragment(new FragmentAttendance(), getString(R.string.attendance));
                        break;
                    case R.id.nav_feedback:
                        replaceFragment(new FragmentFeedBack(), getString(R.string.feedback));
                        break;
                    case R.id.nav_calender:
                        replaceFragment(new FragmentCalendar(), getString(R.string.calendar));
                        break;
                    case R.id.nav_event:
                        replaceFragment(new FragmentEvent(), getString(R.string.event));
                        break;
                    case R.id.nav_competition_winner:
                        replaceFragment(new FragmentCompetitionWinner(), getString(R.string.competitionWinner));
                        break;
                    case R.id.nav_achievement:
                        replaceFragment(new FragmentAchievements(), getString(R.string.achievement));
                        break;
                    case R.id.nav_student_fees:
                        replaceFragment(new FragmentStudentFees(), getString(R.string.studentFees));
                        break;
                    case R.id.nav_home_work:
                        replaceFragment(new FragmentHomeWork(), getString(R.string.assignment));
                        break;
                    case R.id.nav_message:
                        replaceFragment(new FragmentMessage(), getString(R.string.message));
                        break;
                    case R.id.nav_subject:
                        replaceFragment(new FragmentSubject(), getString(R.string.subject));
                        break;
                    case R.id.nav_holiday:
                        replaceFragment(new FragmentHoliday(), getString(R.string.holiday));
                        break;
                    case R.id.nav_timetable:
                        replaceFragment(new FragmentTimeTable(), getString(R.string.timeTable));
                        break;
                    case  R.id.nav_exam_schedule:
                        replaceFragment(new FragmentExamSeries(), getString(R.string.examSchedule));
                        break;
                    case  R.id.nav_scoreCard:
                        replaceFragment(new FragmentScoreCard(), getString(R.string.scoreCard));
                        break;
                    case R.id.nav_aboutus:
                        replaceFragment(new FragmentAboutUs(), getString(R.string.aboutUs));
                        break;
                    case R.id.nav_appinfo:
                        replaceFragment(new FragmentAppInfo(), getString(R.string.appinfo));
                        break;
                    case R.id.nav_support:
                        replaceFragment(new FragmentSupport(), getString(R.string.support));
                        break;
                    case R.id.nav_logout:

                        dialog = new SweetAlertDialog(ActivityHome.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Logout?")
                                .setContentText("Do you really want to logout from the App? ")
                                .setConfirmText("OK")
                                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                    }
                                })
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                        SessionManager session = new SessionManager(getApplication());
                                        session.remove("loggedInUser");
                                        session.remove("loggedInUserId");
                                        session.remove("academicYear");
                                        session.remove("academicYearId");
                                        session.remove("loggedInUserStudent");
                                        session.remove("loggedInUserStudentId");
                                        //session.clear();
                                        Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        startActivity(intent);
                                        finish();                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                        break;

                }
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });


    }
    private void setTokenForNotificationFetching(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()){
                            String token=task.getResult().getToken();
                            System.out.println("token "+token);
                            /* 27/08/2021
                            Toast.makeText(getApplicationContext(),token,Toast.LENGTH_LONG);
                            if(TextUtils.isEmpty(loggedInUserStudent.getToken())){
                                loggedInUserStudent.setToken(token);
                                db.collection("Student").document(loggedInUserStudent.getId()).set(loggedInUserStudent);
                            }else {
                                if (!loggedInUserStudent.getToken().equalsIgnoreCase(token)) {
                                    loggedInUserStudent.setToken(token);
                                    db.collection("Student").document(loggedInUserStudent.getId()).set(loggedInUserStudent);
                                }
                            }*/
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            getSupportActionBar().setTitle(R.string.profile);
            FragmentProfile fragmentProfile = new FragmentProfile();
            replaceFragment(fragmentProfile, "FRAGMENT_PROFILE");

        }

        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.contentLayout, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private void startNotificationWorker(){
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest notificationPeriodicWorkRequest =new PeriodicWorkRequest.Builder(
                NotificationWorker.class, repeatIntervalMin, TimeUnit.MINUTES, flexIntervalMin, TimeUnit.MINUTES
        ).setConstraints(constraints).build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork( uniqueWorkName, ExistingPeriodicWorkPolicy.REPLACE, notificationPeriodicWorkRequest );
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, drawer)
                || super.onSupportNavigateUp();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("Count "+getSupportFragmentManager().getBackStackEntryCount());
        System.out.println("Which fragment "+getSupportFragmentManager().getPrimaryNavigationFragment());
        FragmentManager mgr = getSupportFragmentManager();
        if(mgr.getBackStackEntryCount() == 0) {
            String isFragmentHome = sessionManager.getString("isFragmentHome");
            if(isFragmentHome.equals("false")){
                replaceFragment(new FragmentHome(), getString(R.string.home));
                sessionManager.putString("isFragmentHome", "true");
            }else{
                replaceFragment(new FragmentHome(), getString(R.string.home));
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Exit App")
                        .setContentText("Do you really want to exit the App? ")
                        .setConfirmText("Ok")
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                FragmentManager manager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                fragmentTransaction.replace(R.id.contentLayout, new FragmentHome()).addToBackStack(null).commit();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                finish();
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        }
    }

}
