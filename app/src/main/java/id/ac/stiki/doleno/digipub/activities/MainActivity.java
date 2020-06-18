package id.ac.stiki.doleno.digipub.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import id.ac.stiki.doleno.digipub.Constants;
import id.ac.stiki.doleno.digipub.ForegroundService;
import id.ac.stiki.doleno.digipub.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    boolean doubleBackToExit = false;

    private DrawerLayout drawer;

    private TextView batTempTv;
    private int sensorTemperature;

    private int measuringUnit;
    MaterialButton stopStartButton;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sensorTemperature = intent.getIntExtra("temperature", 0) / 10;
            updateMainText();
        }
    };

    private BroadcastReceiver mServiceStoppedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            updateButtonState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopStartButton = findViewById(R.id.startTrackingBtn);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new MainFragment()).commit();
//            //navigationView.setCheckedItem(R.id.nav);
//        }

        //TODO:Change adMob app id to a real one
        //MobileAds.initialize(this, getString(R.string.admob_app_id));

        measuringUnit = getMeasuringUnit();

        //The main temperature TextView
        batTempTv = findViewById(R.id.batTempTv);

        //Register the Battery Info receiver
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        this.registerReceiver(this.mServiceStoppedReceiver,
                new IntentFilter(Constants.ACTION.STOPFOREGROUND_ACTION));

//        //TODO: Change Banner id to a real one
//        AdView adBanner = findViewById(R.id.adBanner);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adBanner.loadAd(adRequest);

        final TextView azimuthView = findViewById(R.id.azimuth);
        final TextView pitchView = findViewById(R.id.pitch);
        final TextView rollView = findViewById(R.id.roll);

//        getLifecycle().addObserver(new OrientationReporter(this, new OrientationConsumer() {
//            @Override
//            public void accept(float azimuth, float pitch, float roll) {
//                //azimuthView.setText(MainActivity.this.getString(R.string.float_value, azimuth));
//                //pitchView.setText(MainActivity.this.getString(R.string.float_value, pitch));
//                //rollView.setText(MainActivity.this.getString(R.string.float_value, roll));
//
//                azimuthView.setText("azimuth: " + azimuth);
//                pitchView.setText("pitch: " + pitch);
//                rollView.setText("roll: " + roll);
//
//            }
//        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        measuringUnit = getMeasuringUnit();
        updateMainText();
        updateButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatInfoReceiver);
    }


    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("id.ac.stiki.doleno.digipub.ForegroundService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void onStartStopTracking(View view) {
        if (isServiceRunning()) {
            stopService(new Intent(this, ForegroundService.class)
                    .setAction(Constants.ACTION.STOPFOREGROUND_ACTION));
            updateButtonState();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, ForegroundService.class)
                        .setAction(Constants.ACTION.STARTFOREGROUND_ACTION));
            } else {
                startService(new Intent(this, ForegroundService.class)
                        .setAction(Constants.ACTION.STARTFOREGROUND_ACTION));
            }
            finish();
        }
    }

    private void updateButtonState() {
        if (isServiceRunning()) {
            stopStartButton.setText(getString(R.string.stop_tracking));
        } else {
            stopStartButton.setText(getString(R.string.start_tracking));
        }
    }

    private void updateMainText() {
        int temperature;
        if (measuringUnit == Constants.MEASURING_UNIT.CELSIUS) {
            temperature = sensorTemperature;
            batTempTv.setText(temperature + "°C");
        } else {
            temperature = (int) (sensorTemperature * 1.8 + 32);
            batTempTv.setText(temperature + "°F");
        }
    }

    public int getMeasuringUnit() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(
                mSharedPreferences.getString(getString(R.string.key_measuring_unit), Constants.DEFAULT_VALUES.MEASURING_UNIT));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_history:
                Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_guide:
                startActivity(new Intent(this, GuideActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExit) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExit = true;
            Toast.makeText(MainActivity.this, "Click again to exit the app", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExit = false;
                }
            }, 2000);
        }
    }
}
