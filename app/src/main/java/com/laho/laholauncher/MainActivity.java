package com.laho.laholauncher;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewPager mViewPager;
    int cellHeight;
    int NUMBER_OF_ROWS = 5;
    int DRAWER_PEEK_HEİGHT = 100;
    int numRow = 0, numColumn = 0;

    List<AppObject> installedAppList = new ArrayList<>();
    GridView mDrawerGridView;
    BottomSheetBehavior mBottomSheetBehavior;
    AppObject mAppDrag = null;
    ViewPagerAdapter mViewPagerAdapter;
    String PREFS_NAME = "LahoPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        getPermissions();
        getData();

        final LinearLayout mtopDrawerLayout = findViewById(R.id.topDrawerLayout);
        mtopDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                DRAWER_PEEK_HEİGHT = mtopDrawerLayout.getHeight();
                initializeHome();
                initializeDrawer();
            }
        });
        ImageButton mSettings = findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
    }




    private void initializeHome() {
        ArrayList<PagerObject> pagerAppList = new ArrayList<>();
        ArrayList<AppObject> appList1 = new ArrayList<>();
        ArrayList<AppObject> appList2 = new ArrayList<>();
        ArrayList<AppObject> appList3 = new ArrayList<>();
        for(int i=0; i<numRow*numColumn;i++){
            appList1.add(new AppObject("","",getResources().getDrawable(R.drawable.ic_launcher_foreground),false));
        }
        for(int i=0;i<numRow*numColumn;i++){
            appList2.add(new AppObject("","",getResources().getDrawable(R.drawable.ic_launcher_foreground),false));
        }
        for(int i=0;i<numRow*numColumn;i++){
            appList3.add(new AppObject("","",getResources().getDrawable(R.drawable.ic_launcher_foreground),false));
        }
        pagerAppList.add(new PagerObject(appList1));
        pagerAppList.add(new PagerObject(appList2));
        pagerAppList.add(new PagerObject(appList3));

        cellHeight = (getDisplayContentHeight() - DRAWER_PEEK_HEİGHT) / numRow ;

        mViewPager = findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(this,pagerAppList, cellHeight, numColumn);
        mViewPager.setAdapter(mViewPagerAdapter);
    }




    private void initializeDrawer() {
        View mBottomSheet = findViewById(R.id.bottomSheet);
        mDrawerGridView = findViewById(R.id.drawerGrid);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setPeekHeight(DRAWER_PEEK_HEİGHT);

        installedAppList = getInstalledAppList();
        mDrawerGridView.setAdapter(new AppAdapter(this,installedAppList,cellHeight));

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if(mAppDrag != null){
                    return;
                }
                if(newState == BottomSheetBehavior.STATE_COLLAPSED && mDrawerGridView.getChildAt(0).getY() != 0){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                if(newState == BottomSheetBehavior.STATE_DRAGGING && mDrawerGridView.getChildAt(0).getY() != 0){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    public void itemPress(AppObject app){
        if(mAppDrag!=null && !app.getName().equals("")){
            Toast.makeText(this, "Hücre dolu!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(mAppDrag!=null && !app.getIsAppInDrawer()){

            app.setPackageName(mAppDrag.getPackageName());
            app.setName(mAppDrag.getName());
            app.setImage(mAppDrag.getImage());
            app.setIsAppInDrawer(false);
            if(!mAppDrag.getIsAppInDrawer()){
                mAppDrag.setPackageName("");
                mAppDrag.setName("");
                mAppDrag.setImage(getResources().getDrawable(R.drawable.ic_launcher_foreground));
                mAppDrag.setIsAppInDrawer(false);
            }
            mAppDrag = null;
            mViewPagerAdapter.notifyGridChanged();
            return;
        }else {
            Intent launchAppIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            if (launchAppIntent != null) {
                getApplicationContext().startActivity(launchAppIntent);
            }
        }
    }

    public void itemLongPress(AppObject app){
        collapseDrawer();
        mAppDrag = app;
    }

    private void collapseDrawer() {
        mDrawerGridView.setY(DRAWER_PEEK_HEİGHT);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }





    private List<AppObject> getInstalledAppList() {
        List<AppObject> list = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> untreatedAppList = getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);

        for(ResolveInfo app: untreatedAppList){
            String appName = app.activityInfo.loadLabel(getPackageManager()).toString();
            String appPackageName = app.activityInfo.packageName;
            Drawable appImage = app.activityInfo.loadIcon(getPackageManager());
            AppObject temp = new AppObject(appPackageName,appName,appImage,true);
            if(!list.contains(temp)){
                list.add(temp);
            }
        }

        return list;
    }

    private int getDisplayContentHeight() {
        final WindowManager windowManager = getWindowManager();
        final Point  size = new Point();
        int screenHegiht = 0, actionBarHeight = 0, statusBarHeight = 0, resourceId, contentTop;
        if(getActionBar()!=null){
            actionBarHeight = getActionBar().getHeight();
        }
        resourceId = getResources().getIdentifier("status_bar_height","dimen", "android");
        if(resourceId > 0){
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        contentTop = (findViewById(android.R.id.content)).getTop();
        windowManager.getDefaultDisplay().getSize(size);
        screenHegiht = size.y;

        return  screenHegiht - contentTop - actionBarHeight - statusBarHeight;
    }



    private void getData(){
        ImageView mHomeScreenImage = findViewById(R.id.homeScreenImage);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String imageUri = sharedPreferences.getString("imageUri", null);
        int numRow = sharedPreferences.getInt("numRow",5);
        int numColumn = sharedPreferences.getInt("numColumn",4);

        if(this.numRow != numRow || this.numColumn != numColumn ) {
            this.numColumn = numColumn;
            this.numRow = numRow;
            initializeHome();
        }

        if(imageUri!=null){
            mHomeScreenImage.setImageURI(Uri.parse(imageUri));
        }
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }
}