package com.quranreading.qibladirection;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.quranreading.adapter.DrawerMenuAdapter;
import com.quranreading.ads.AnalyticSingaltonClass;
import com.quranreading.alarms.AlarmHelper;
import com.quranreading.alarms.AlarmReceiverAyah;
import com.quranreading.alarms.AlarmReceiverPrayers;
import com.quranreading.alarms.PrayerTimeUpdateReciever;
import com.quranreading.fragments.HomeFragment;
import com.quranreading.helper.Constants;
import com.quranreading.model.MenuDrawerModel;
import com.quranreading.sharedPreference.LocationPref;
import com.quranreading.sharedPreference.QiblaDirectionPref;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import noman.CommunityGlobalClass;
import noman.community.prefrences.SavePreference;
import noman.hijri.acitivity.CalenderActivity;
import noman.hijri.acitivity.ConverterDialog;
import noman.quran.activity.QuranReadActivity;
import places.activities.PlacesListActivity;
import quran.helper.DBManagerQuran;
import noman.sharedpreference.SurahsSharedPref;

import static com.quranreading.qibladirection.R.id.toolbar_btnMenu;

/**
 * Created by cyber on 11/17/2016.
 */

public class MainActivityNew extends AppCompatActivity implements AdapterView.OnItemClickListener {


    public ActionBarDrawerToggle mDrawerToggle;
    boolean inProcess = false;
    private static Activity activity = null;
    private Context context = this;
    boolean isInterstitialShown = false;
    Spinner s;

    public static final String ACTION_INTERSTITIAL_ADS_SHOW = "show_interstitial";
    private Handler mHandler = new Handler();
    private int TIMER_INTERSTITIAL_AD = 30 * 1000;

    // google ads
    AdView adview;
    ImageView adImage;
    private static final String LOG_TAG = "Ads";
    private final Handler adsHandler = new Handler();
    private int timerValue = 3000, networkRefreshTime = 10000;
    //  InterstitialAd mInterstitialAd;
    private Runnable mRunnableInterstitialAd = new Runnable() {

        @Override
        public void run() {

            // to show ad only when odd occurence
            if (mQiblaDirectionPref.getInterstitialCount() == 1) {
                if (!isInterstitialShown) // only on 1st and 2nd tab and ad not shown once, if show once will not show again
                {
                    mHandler.removeCallbacks(mRunnableInterstitialAd);
                    showInterstitialAd();
                    isInterstitialShown = true;
                } else {
                    mHandler.removeCallbacks(mRunnableInterstitialAd);
                }
            } else {
                mHandler.removeCallbacks(mRunnableInterstitialAd);
            }
        }
    };

    // boolean isAdLoaded = false;
    // final int INTERSTITIAL_REFRESH_TIME = 15000;
   /* Runnable mRunnableInterstitialRefresh = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated methodIndex stub
            if (!isAdLoaded) {
                requestNewInterstitial();
            }
        }
    };*/

    // Defining Variables

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    RelativeLayout layoutImageShare;

    private LocationPref locationPref;
    private QiblaDirectionPref mQiblaDirectionPref;
    int type = 0;

    public static final String ACTIVITY_ABOUT = "about";
    public static final String ACTIVITY_INSTRUCTION = "instruction";
    public static final String ACTIVITY_SELECTION = "selected_activity";

    private List<MenuDrawerModel> drawerListData = new ArrayList<MenuDrawerModel>();
    private DrawerMenuAdapter adapter;

    // public static final int SETTINGS_ACTIVITY = 1;

    public static final int menuDialsC = 1;
    public static final int menuRemoveAdsC = 2;
    public static final int menuSettingsC = 3;
    public static final int menuLanguageC = 4;
    public static final int menuMoreAppsC = 6;
    public static final int menuShareC = 7;
    public static final int menuInstrctC = 8;
    public static final int menuFeedBackC = 9;
    public static final int menuAboutUsC = 10;
    public static final int menuDisclaimerC = 11;
    public static final int menuLogout = 13;
    public static final int menuFaceBookC = 14;
    public static final int menuPrivayC = 12;
    private long lastClick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main_new);
        CommunityGlobalClass.mainActivityNew = this;


        //Community  Global Class
        registerHandlerService();


        //New Screen Added
        sendAnalyticsData("Qibla Home 4.0");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        if ((width == 720 && height == 1280) || (width >= 1080 && height <= 2560) || (width == 540 && height == 960)) {
            // Samsung S3 && s4
            ((GlobalClass) getApplication()).deviceS3 = true;
        } else {
            ((GlobalClass) getApplication()).deviceS3 = false;
        }

        activity = this;
        sendBroadcastStopSurah();// used if alarm is tapped to stop_r sounds

        locationPref = new LocationPref(context);
        mQiblaDirectionPref = new QiblaDirectionPref(context);
        // Show Local Notification of App
        if (mQiblaDirectionPref.getAlarmCount() == 0) {
            AlarmHelper mAlarmHelper = new AlarmHelper(this);
            mAlarmHelper.setAlarmResetTimeReciever(mAlarmHelper.setAlarmTime(PrayerTimeUpdateReciever.ALARM_RESET_PRAYER_HOUR, PrayerTimeUpdateReciever.ALARM_RESET_PRAYER_MINUTES, PrayerTimeUpdateReciever.ALARM_RESET_PRAYER_AM_PM), PrayerTimeUpdateReciever.ALARM_RESET_PRAYER_ID);

            // Set Ayah of the Day Notification
            SurahsSharedPref mSurahsSharedPref = new SurahsSharedPref(this);
            if (mSurahsSharedPref.isAyahNotification()) {
                mAlarmHelper.setAlarmAyahNotification(mAlarmHelper.setAlarmTime(mSurahsSharedPref.getAlarmHours(), mSurahsSharedPref.getAlarmMints(), ""), AlarmReceiverAyah.NOTIFY_AYAH_ALARM_ID);
            } else {

                mAlarmHelper.cancelAlarmAyahNotification(AlarmReceiverAyah.NOTIFY_AYAH_ALARM_ID);
            }
        }


        if (getIntent().getIntExtra("notificationID", -1) > 0) {

            startActivity(new Intent(this, TimingsActivity.class));

        } else if (getIntent().getIntExtra(QuranReadActivity.KEY_EXTRA_AYAH_NO, -1) > 0) {
            Intent intent = new Intent(this, QuranReadActivity.class);

            intent.putExtra(QuranReadActivity.KEY_EXTRA_SURAH_NO, getIntent().getIntExtra(QuranReadActivity.KEY_EXTRA_SURAH_NO, -1));
            intent.putExtra(QuranReadActivity.KEY_EXTRA_AYAH_NO, getIntent().getIntExtra(QuranReadActivity.KEY_EXTRA_AYAH_NO, -1));

            startActivity(intent);
        }


        layoutImageShare = (RelativeLayout) findViewById(R.id.layout_image_share);
        layoutImageShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - lastClick < 3000) {
                    return;
                } else {
                    String body = getResources().getString(R.string.share_msg);
                    shareMessage(getResources().getString(R.string.app_name), body);
                }

            }
        });


        //Setting current hijri date in the toolbar
        getCurrentHijriDate();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container_frame, new HomeFragment()).commit();
        }


        initializeViews(false);


        //Is First Time app Lauunch so add Fav ayash
        if (locationPref.isFirstTimeAppLaunch()) {
            doFirstBookMarksAyah();
            locationPref.setFirstTimeAppLaunch();
        }
    }

    public void initializeViews(boolean isAgainLoaded) {

        initializeMenuList();
        initialize();
        if (isAgainLoaded) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container_frame, new HomeFragment()).commit();
        }
    }


    public void initializeMenuList() {
        drawerListData.clear();
        MenuDrawerModel dataObj;

        dataObj = new MenuDrawerModel(true, false, false, "", 0);
        drawerListData.add(dataObj);

        /*dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.select_dial), menuDialsC);
        drawerListData.add(dataObj);*/


        if (!((GlobalClass) getApplication()).isPurchase) {
            dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.remove_ads), menuRemoveAdsC);
            drawerListData.add(dataObj);
        }
        MenuDrawerModel dataObj4 = new MenuDrawerModel(false, true, false, getResources().getString(R.string.settings), menuSettingsC);
        drawerListData.add(dataObj4);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.languages), menuLanguageC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, false, true, "", 5);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.more_apps), menuMoreAppsC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.share), menuShareC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.instructions), menuInstrctC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.feedback), menuFeedBackC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.about_us), menuAboutUsC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.disclaimer), menuDisclaimerC);
        drawerListData.add(dataObj);

        dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.privacy_text), menuPrivayC);
        drawerListData.add(dataObj);

        //if User login then add
        SavePreference savePreference = new SavePreference();
        CommunityGlobalClass.mSignInRequests = savePreference.getDataFromSharedPreferences();
        if (CommunityGlobalClass.mSignInRequests != null) {
            dataObj = new MenuDrawerModel(false, true, false, getResources().getString(R.string.logout_title), menuLogout);
            drawerListData.add(dataObj);
        }

        dataObj = new MenuDrawerModel(false, false, false, null, menuFaceBookC);
        drawerListData.add(dataObj);
    }

    public void initialize() {
        initDrawer();

        initializeAds();
    }

    private void initializeAds() {
        adview = (AdView) findViewById(R.id.adView);
        adImage = (ImageView) findViewById(R.id.adimg);
        adImage.setVisibility(View.GONE);
        adview.setVisibility(View.GONE);

        if (!((GlobalClass) getApplication()).isPurchase) {
           /* mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial));

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // TODO Auto-generated methodIndex stub
                    super.onAdFailedToLoad(errorCode);

                    isAdLoaded = false;
                    mHandler.removeCallbacks(mRunnableInterstitialRefresh);
                    mHandler.postDelayed(mRunnableInterstitialRefresh, INTERSTITIAL_REFRESH_TIME);

                }

                @Override
                public void onAdLoaded() {
                    // TODO Auto-generated methodIndex stub
                    super.onAdLoaded();
                    isAdLoaded = true;
                }

            });

            requestNewInterstitial();
*/
            if (isNetworkConnected()) {
                this.adview.setVisibility(View.VISIBLE);
            } else {
                this.adview.setVisibility(View.GONE);
            }
            setAdsListener();

            registerReceiver(interstitialAdReciever, new IntentFilter(ACTION_INTERSTITIAL_ADS_SHOW));

        }
    }

   /* private void requestNewInterstitial() {

        AdRequest adRequest = new AdRequest.Builder().build();
        try {
            mInterstitialAd.loadAd(adRequest);
        } catch (Exception e) {
        }
    }*/

    public void initDrawer() {

        // Initializing Toolbar and setting it as the actionbar

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawerList);
        setupDrawer();
        adapter = new DrawerMenuAdapter(MainActivityNew.this, drawerListData);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(this);


        // mDrawerLayout.setDrawerListener(this);
/*
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.on, R.string.off) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        // Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        // calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();*/
    }

    private void setupDrawer() {
        LinearLayout imgHomeBtn = (LinearLayout) findViewById(toolbar_btnMenu);
        imgHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                operateDrawerOnButton(false);
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }


        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //Drawer Handling operation
    public void operateDrawerOnButton(Boolean isBackBtn) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            closeDrawer();
        } else {
            if (!isBackBtn) {
                openDrawer();
            } else {
                finish();
            }

        }
    }

    public void closeDrawer() {
        new Handler().postDelayed(closeDrawerRunnable(), 200);
    }

    public void openDrawer() {
        new Handler().postDelayed(openDrawerRunnable(), 200);
    }

    private Runnable openDrawerRunnable() {
        return new Runnable() {
            @Override
            public void run() {

                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        };
    }

    private Runnable closeDrawerRunnable() {
        return new Runnable() {
            @Override
            public void run() {

                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        };
    }

    // ************** End *************
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        int pos = drawerListData.get(position).getPosition();
        if (pos != 0 && pos != 5) {
            mDrawerLayout.closeDrawer(mDrawerList);
            menuSelection(pos);
        }
    }

    public void menuSelection(Integer position) {

        if (!inProcess) {

            switch (position) {
                case menuDialsC: {
                    inProcess = true;
                    Intent dialsIntent = new Intent(MainActivityNew.this, DialsActivity.class);
                    startActivity(dialsIntent);
                    //    showInterstitialAd();
                }
                break;

                case menuRemoveAdsC: {
                    if (SystemClock.elapsedRealtime() - lastClick < 3000) {
                        return;
                    } else {
                        inProcess = true;
                        sendAnalyticEvent("Remove Ads");
                        startActivity(new Intent(MainActivityNew.this, UpgradeActivity.class));
                    }
                }
                break;

                case menuSettingsC: {
                    AnalyticSingaltonClass.getInstance(MainActivityNew.this).sendEventAnalytics("Settings", "Settings_Drawer");
                    inProcess = true;
                    Intent settingsIntent = new Intent(MainActivityNew.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    //     showInterstitialAd();
                }
                break;

                case menuLanguageC: {
                    inProcess = true;
                    Intent intent = new Intent(MainActivityNew.this, LanguageSelectionActivity.class);
                    startActivity(intent);
                    //    showInterstitialAd();

                }
                break;

                case menuMoreAppsC: {
                    inProcess = true;
                    sendAnalyticEvent("More Apps");

                    String s = "market://search?q=pub:Quran+Reading";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    startActivity(browserIntent);

                }
                break;

                case menuShareC: {
                    inProcess = true;
                    sendAnalyticEvent("Share");
                    String body = getResources().getString(R.string.share_msg);
                    shareMessage(getResources().getString(R.string.app_name), body);
                }
                break;

                case menuInstrctC: {
                    inProcess = true;
                    startActivity(new Intent(getApplicationContext(), AboutInstructionActivity.class).putExtra(ACTIVITY_SELECTION, ACTIVITY_INSTRUCTION));
                    //   showInterstitialAd();
                }
                break;

                case menuFeedBackC: {
                    sendAnalyticEvent("User Feedback");
                    feedBackDailog(false);
                }
                break;

                case menuAboutUsC: {
                    inProcess = true;
                    startActivity(new Intent(getApplicationContext(), AboutInstructionActivity.class).putExtra(ACTIVITY_SELECTION, ACTIVITY_ABOUT));
                    // showInterstitialAd();
                }
                break;

                case menuDisclaimerC: {
                    sendAnalyticEvent("Disclaimer");
                    showDisclaimer();
                }
                break;

                case menuLogout: {
                    sendAnalyticEvent("Logout");
                    showLogoutAlert();
                }
                break;
                case menuPrivayC: {
                    inProcess = true;
                    sendAnalyticEvent("Privacy Policy");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.policy_link)));
                    startActivity(browserIntent);

                }
                break;

                case menuFaceBookC:
                    sendAnalyticEvent("Facebook");
                    String s = "https://www.facebook.com/QiblaConnect/";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    startActivity(browserIntent);
                    break;

                default:
                    return;
            }
        }
    }

    private void showInterstitialAd() {
       /* if (!((GlobalClass) getApplication()).isPurchase) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }*/

        if (CommunityGlobalClass.getInstance().mInterstitialAd.getAd().isLoaded()) {
            CommunityGlobalClass.getInstance().mInterstitialAd.getAd().show();

            Log.e("Ads", "Display Intersitial main");
        }
    }

    public void shareMessage(String subject, String body) {

        // if(saveShareImage())
        // {
        // shareAppWithAppIcon(subject, body);
        // }
        // else
        // {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(shareIntent, "Share via"));

        // }
    }

    public void shareAppWithAppIcon(String subject, String body) {
        String fileName = "ic_launcher.png";
        String completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        // shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    public boolean saveShareImage() {
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.about_app_icon), 100, 100, false);
        File sd = Environment.getExternalStorageDirectory();
        String fileName = "ic_launcher.png";
        File dest = new File(sd, fileName);
        try {
            FileOutputStream out;
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated methodIndex stub
        super.onResume();
        if (CommunityGlobalClass.isQuranModuleOpen && !CommunityGlobalClass.isAdAlreadyShow) {
            //Make Sure Quran Module is in False state
            if (!((GlobalClass) getApplication()).isPurchase) {
                sendBroadcast(new Intent(MainActivityNew.ACTION_INTERSTITIAL_ADS_SHOW));
                Log.e("OnResume", "Ads Display");
            }
            CommunityGlobalClass.isQuranModuleOpen = false;
            CommunityGlobalClass.isAdAlreadyShow = true;
        }

        inProcess = false;
        if (((GlobalClass) getApplication()).isPurchase) {

            adImage.setVisibility(View.GONE);
            adview.setVisibility(View.GONE);
            if (drawerListData.size() > Constants.DRAWER_LIST_LENGTH) {
                drawerListData.remove(2);
                adapter.notifyDataSetChanged();
            }
        } else {
           /* mHandler.removeCallbacks(mRunnableInterstitialAd);
            mHandler.postDelayed(mRunnableInterstitialAd, TIMER_INTERSTITIAL_AD);
            if (!isAdLoaded) {
                mHandler.removeCallbacks(mRunnableInterstitialRefresh);
                mHandler.postDelayed(mRunnableInterstitialRefresh, INTERSTITIAL_REFRESH_TIME);
            }*/

            startAdsCall();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated methodIndex stub
        super.onPause();
        mHandler.removeCallbacks(mRunnableInterstitialAd);
        if (!((GlobalClass) getApplication()).isPurchase) {
            stopAdsCall();
            //  mHandler.removeCallbacks(mRunnableInterstitialRefresh);
        }
        hideKeyboardForce();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated methodIndex stub
        hideKeyboardForce();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated methodIndex stub
        if (!((GlobalClass) getApplication()).isPurchase) {
            destroyAds();
            unregisterReceiver(interstitialAdReciever);


           /* if (mInterstitialAd != null)
                mInterstitialAd.setAdListener(null);
            mInterstitialAd = null;*/
        }
        super.onDestroy();
    }

    public void onCalibrationClick(View v) {
        setCalibrationShown();
    }

    public void showCalibration() {
//        if (!locationPref.isCalibrationShown() && !locationPref.getCityName().equals("")) {
//            RelativeLayout layoutCalib = (RelativeLayout) findViewById(R.id.calibration_layout);
//            layoutCalib.setVisibility(View.VISIBLE);
//        }
    }

    private void setCalibrationShown() {
        RelativeLayout layoutCalib = (RelativeLayout) findViewById(R.id.calibration_layout);
        locationPref.setCalibrationShown();
        layoutCalib.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {

            int count = mQiblaDirectionPref.getExitCount();
            if (count > 2) {
                mQiblaDirectionPref.setExitCount(0);
                if (!((GlobalClass) getApplication()).purchasePref.getRateUs()) {
                    feedBackDailog(true);
                }
            } else {
                mQiblaDirectionPref.setExitCount(count + 1);
                super.onBackPressed();
            }
        }


//        else {
//            int count = mQiblaDirectionPref.getExitCount();
//
//            if (!((GlobalClass) getApplication()).isPurchase && (count != 0 || mQiblaDirectionPref.getRateUs())) {
//                if (count < 2) {
//                    mQiblaDirectionPref.setExitCount(count + 1);
//                } else {
//                    mQiblaDirectionPref.setExitCount(0);
//                }
//                Intent removeAdsDialog = new Intent(MainActivityNew.this, UpgradeActivity.class);
//                removeAdsDialog.putExtra("Exit", true);
//                startActivity(removeAdsDialog);
//            } else if ((count == 0 && !mQiblaDirectionPref.getRateUs()) || (((GlobalClass) getApplication()).isPurchase && !mQiblaDirectionPref.getRateUs())) {
//                mQiblaDirectionPref.setExitCount(count + 1);
//                feedBackDailog(true);
//            } else {
//                super.onBackPressed();
//            }
//        }
    }

    private void feedBackDailog(final boolean isAppClosed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.feedback));
        builder.setMessage(getResources().getString(R.string.feedback_msg));
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((GlobalClass) getApplication()).purchasePref.setRateUs();
                try {
                    Intent mintent = new Intent(Intent.ACTION_VIEW);
                    mintent.setData(Uri.parse("market://details?id=com.quranreading.qibladirection"));
                    startActivity(mintent);
                } catch (Exception e1) {
                    try {
                        Uri uriUrl = Uri.parse("https://market.android.com/details?id=com.quranreading.qibladirection");
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                        finish();
                    } catch (Exception e2) {
                        Toast.makeText(context, "No Application Found to open link", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.later), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (isAppClosed)
                    MainActivityNew.this.finish();
            }
        });

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onHalalPlacesClick(View view) {
        type = 1;
        if (isNetworkConnected()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                if (!inProcess) {
                    inProcess = true;
                    Intent intent = new Intent(this, PlacesListActivity.class);
                    intent.putExtra(PlacesListActivity.EXTRA_PLACE_TYPE, type);
                    startActivity(intent);
                    showInterstitialAd();
                }
            } else {
                LocationPref mLocationPref = new LocationPref(this);
                if (mLocationPref.isHalalPlacesSaved()) {
                    if (!inProcess) {
                        inProcess = true;
                        Intent intent = new Intent(this, PlacesListActivity.class);
                        intent.putExtra(PlacesListActivity.EXTRA_PLACE_TYPE, type);
                        startActivity(intent);
                        showInterstitialAd();
                    }
                } else {
                    providerAlertMessage();
                }
            }
        } else {
            showShortToast(getResources().getString(R.string.toast_network_error), 500, Gravity.BOTTOM);
        }
    }

    private void showShortToast(String message, int milliesTime, int gravity) {

        if (getString(R.string.device).equals("large")) {
            final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, milliesTime);
        }
    }

    public void onMosquesClick(View view) {

        type = 0;
        if (isNetworkConnected()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                if (!inProcess) {
                    inProcess = true;
                    Intent intent = new Intent(this, PlacesListActivity.class);
                    intent.putExtra(PlacesListActivity.EXTRA_PLACE_TYPE, type);
                    startActivity(intent);
                    showInterstitialAd();
                }
            } else {
                LocationPref mLocationPref = new LocationPref(this);

                if (mLocationPref.isMosquePlacesSaved()) {
                    if (!inProcess) {
                        inProcess = true;
                        Intent intent = new Intent(this, PlacesListActivity.class);
                        intent.putExtra(PlacesListActivity.EXTRA_PLACE_TYPE, type);
                        startActivity(intent);
                        showInterstitialAd();
                    }
                } else {
                    providerAlertMessage();
                }
            }
        } else {
            showShortToast(getResources().getString(R.string.toast_network_error), 500, Gravity.BOTTOM);
        }
    }

    private void providerAlertMessage() {

        AlertDialog alertProvider = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.unable_to_find_location));
        builder.setMessage(getResources().getString(R.string.enable_provider));
        builder.setCancelable(false);

        builder.setPositiveButton(getResources().getString(R.string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(settingsIntent);
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });

        builder.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                // TODO Auto-generated methodIndex stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });

        alertProvider = builder.create();
        alertProvider.show();
    }

    private void showDisclaimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.disclaimer));
        builder.setMessage(getResources().getString(R.string.disclaimer_quran_1) + "\n\n" + getResources().getString(R.string.disclaimer_quran_2));

        builder.setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showLogoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.logout_title));
        builder.setNegativeButton(R.string.txt_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage(getResources().getString(R.string.logout_dialog));

        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();
                SavePreference savePreference = new SavePreference();
                savePreference.setDataFromSharedPreferences(null);
                CommunityGlobalClass.mSignInRequests = null;

                //Reactivitvate Drowaer layoyt
                CommunityGlobalClass.mainActivityNew.initializeMenuList();
                CommunityGlobalClass.mainActivityNew.initDrawer();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void finishActivity() {
        if (activity != null) {
            activity.finish();
        }
    }

    private void sendAnalyticsData(String name) {

        AnalyticSingaltonClass.getInstance(this).sendScreenAnalytics(name);
    }

    private void sendAnalyticEvent(String eventAction) {
        AnalyticSingaltonClass.getInstance(this).sendEventAnalytics("Drawer Menu", eventAction);
    }

    private void sendBroadcastStopSurah() {

        Intent intentBroadCast = new Intent(AlarmReceiverPrayers.STOP_SOUND);
        context.sendBroadcast(intentBroadCast);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void hideKeyboardForce() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    ///////////////////////////
    //////////////////////////
    //////////////////////////
    public void onClickAdImage(View view) {

    }

    private boolean isNetworkConnected() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = mgr.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable());
    }

    private Runnable sendUpdatesAdsToUI = new Runnable() {
        public void run() {
            Log.v(LOG_TAG, "Recall");
            updateUIAds();
        }
    };

    private final void updateUIAds() {
        if (isNetworkConnected()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adview.loadAd(adRequest);
        } else {
            timerValue = networkRefreshTime;
            adsHandler.removeCallbacks(sendUpdatesAdsToUI);
            adsHandler.postDelayed(sendUpdatesAdsToUI, timerValue);
        }
    }

    public void startAdsCall() {
        Log.i(LOG_TAG, "Starts");
        if (isNetworkConnected()) {
            this.adview.setVisibility(View.VISIBLE);
        } else {
            this.adview.setVisibility(View.GONE);
        }

        adview.resume();
        adsHandler.removeCallbacks(sendUpdatesAdsToUI);
        adsHandler.postDelayed(sendUpdatesAdsToUI, 0);
    }

    public void stopAdsCall() {
        Log.e(LOG_TAG, "Ends");
        adsHandler.removeCallbacks(sendUpdatesAdsToUI);
        adview.pause();
    }

    public void destroyAds() {
        Log.e(LOG_TAG, "Destroy");
        adview.destroy();
        adview = null;
    }

    private void setAdsListener() {
        adview.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(LOG_TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int error) {
                String message = "onAdFailedToLoad: " + getErrorReason(error);
                Log.d(LOG_TAG, message);
                adview.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(LOG_TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                Log.d(LOG_TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                Log.d(LOG_TAG, "onAdLoaded");
                adview.setVisibility(View.VISIBLE);

            }
        });
    }

    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }
        return errorReason;
    }


    //Donot Show anny ads
    BroadcastReceiver interstitialAdReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated methodIndex stub
            showInterstitialAd();
        }
    };


    public void getCurrentHijriDate() {
        LinearLayout hijriDateClick = (LinearLayout) findViewById(R.id.toolbar_hijri_date);
        hijriDateClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityNew.this, CalenderActivity.class));
            }
        });

        TextView currentHijriDate = (TextView) findViewById(R.id.txt_toolbar_hijr_date);
        String months[] = getResources().getStringArray(R.array.islamic_month_name);
        Calendar _calendarLocal = Calendar.getInstance(Locale.getDefault());
        int month = _calendarLocal.get(Calendar.MONTH);
        int year = _calendarLocal.get(Calendar.YEAR);
        //_calendarLocal.set(Calendar.DAY_OF_WEEK_IN_MONTH,(_calendarLocal.get(Calendar.DAY_OF_WEEK_IN_MONTH)+ 1));
        _calendarLocal.set(Calendar.DAY_OF_MONTH, (_calendarLocal.get(Calendar.DAY_OF_MONTH) - 1)); //For date issue

        //New changes here
        GregorianCalendar gCal = new GregorianCalendar(year, month, _calendarLocal.get(Calendar.DAY_OF_MONTH));

        Calendar uCal = new UmmalquraCalendar();
        uCal.setTime(gCal.getTime());
        Calendar _calendar = uCal;
        year = _calendar.get(Calendar.YEAR);
        month = _calendar.get(Calendar.MONTH);

        //********************************

        //**** Save Hijri current date here with following
        SimpleDateFormat dateFormat = new SimpleDateFormat("", Locale.ENGLISH);
        dateFormat.setCalendar(_calendar);
        dateFormat.applyPattern("d");
        int cDate = Integer.parseInt(dateFormat.format(_calendar.getTime()));
        dateFormat.applyPattern("y");
        int cYear = Integer.parseInt(dateFormat.format(_calendar.getTime()));
        ConverterDialog.CalenderHijriDate = cDate;
        String currentDate = cDate + " " + months[month] + ", " + cYear;
        Log.e("curent date", currentDate);
        currentHijriDate.setText(currentDate);
        currentHijriDate.setTypeface(((GlobalClass) getApplicationContext()).faceRobotoR);
        //**********************************


        //Premium icon
        RelativeLayout layoutImagePremium = (RelativeLayout) findViewById(R.id.layout_image_premium);
        layoutImagePremium.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityNew.this, UpgradeActivity.class);
                startActivity(intent);

            }
        });
        if (((GlobalClass) getApplication()).isPurchase) {
            layoutImagePremium.setVisibility(View.GONE);
        }
    }

    public void doFirstBookMarksAyah() {
        String[] surahNames = getResources().getStringArray(R.array.surah_names);
        DBManagerQuran dbObj = new DBManagerQuran(MainActivityNew.this);
        dbObj.open();
        dbObj.addBookmark(surahNames[3 - 1], 3, 193);
        dbObj.addBookmark(surahNames[18 - 1], 18, 10);
        dbObj.close();

    }

    public void registerHandlerService() {
        ///Ads Stretegy
        CommunityGlobalClass.isAdAlreadyShow = false;
        CommunityGlobalClass.isQuranModuleOpen = false;

        CommunityGlobalClass.getInstance().handler = new Handler();
        CommunityGlobalClass.getInstance().runTimerForAdsStratedgy();
        LocationPref locationPref = new LocationPref(this);
        if (locationPref.isFirstTimeAppLaunch()) {
            CommunityGlobalClass.getInstance().timeDelay = 50 * 1000;//for 50 sec
        } else {

            CommunityGlobalClass.getInstance().timeDelay = 10 * 1000;
        }
        CommunityGlobalClass.getInstance().handler.postDelayed(CommunityGlobalClass.getInstance().runnable, CommunityGlobalClass.getInstance().timeDelay);//Run handler
    }
}
