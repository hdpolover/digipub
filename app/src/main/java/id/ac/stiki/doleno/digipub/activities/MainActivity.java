package id.ac.stiki.doleno.digipub.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.vision.CameraSource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

import id.ac.stiki.doleno.digipub.Constants;
import id.ac.stiki.doleno.digipub.ForegroundService;
import id.ac.stiki.doleno.digipub.OrientationConsumer;
import id.ac.stiki.doleno.digipub.OrientationReporter;
import id.ac.stiki.doleno.digipub.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    boolean doubleBackToExit = false;

    private DrawerLayout drawer;

    private TextView batTempTv, camDisTv;
    private int sensorTemperature;

    private int measuringUnit;
    MaterialButton stopStartButton;

    String azimuthValue;

    MaterialCardView batteryCard, cameraDisCard, rotationCard;

    float F = 1f;           //focal length
    float sensorX, sensorY; //camera sensor dimensions
    float angleX, angleY;
    float distance;

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

        measuringUnit = getMeasuringUnit();

        //The main temperature TextView
        batTempTv = findViewById(R.id.batTempTv);
        camDisTv = findViewById(R.id.camDisTv);

        batteryCard = findViewById(R.id.batteryCard);
        cameraDisCard = findViewById(R.id.cameraDisCard);
        rotationCard = findViewById(R.id.rotationCard);

        //Register the Battery Info receiver
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        this.registerReceiver(this.mServiceStoppedReceiver,
                new IntentFilter(Constants.ACTION.STOPFOREGROUND_ACTION));

        getLifecycle().addObserver(new OrientationReporter(this, new OrientationConsumer() {
            @Override
            public void accept(float azimuth, float pitch, float roll) {
                sendRotationBroadcast(azimuth, pitch, roll);
            }
        }));

        initCameraTracker();

        batteryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BatteryActivity.class));
            }
        });

        cameraDisCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraDistanceActivity.class));
            }
        });

        rotationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RotationActivity.class));
            }
        });

    }

    void sendRotationBroadcast(float a, float p, float r) {
        Intent intent = new Intent("id.ac.stiki.doleno.digipub.ROTATION_ACTION");
        intent.putExtra("id.ac.stiki.doleno.digipub.AZIMUTH_VALUE", a);
        intent.putExtra("id.ac.stiki.doleno.digipub.PITCH_VALUE", p);
        intent.putExtra("id.ac.stiki.doleno.digipub.ROLL_VALUE", r);

        sendBroadcast(intent);
    }

    void sendCameraBroadcast() {
        Intent intent = new Intent("id.ac.stiki.doleno.digipub.CAMERA_ACTION");
        intent.putExtra("id.ac.stiki.doleno.digipub.DISTANCE_VALUE", distance);

        sendBroadcast(intent);
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
        unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
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
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(this, HistoryActivity.class));
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

    void initCameraTracker() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        }
        else {
            Camera camera = frontCam();
            Camera.Parameters campar = camera.getParameters();
            F = campar.getFocalLength();
            angleX = campar.getHorizontalViewAngle();
            angleY = campar.getVerticalViewAngle();
            sensorX = (float) (Math.tan(Math.toRadians(angleX/2))*2*F);
            sensorY = (float) (Math.tan(Math.toRadians(angleY/2))*2*F);
            camera.stopPreview();
            camera.release();
            createCameraSource();
        }
    }

    private Camera frontCam() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            Log.v("CAMID", camIdx+"");
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("FAIL", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    public void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
        detector.setProcessor(new LargestFaceFocusingProcessor(detector, new FaceTracker()));

        CameraSource cameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FaceTracker extends Tracker<Face> {

        private FaceTracker() {

        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            float p =(float) Math.sqrt(
                    (Math.pow((face.getLandmarks().get(Landmark.LEFT_EYE).getPosition().x-
                            face.getLandmarks().get(Landmark.RIGHT_EYE).getPosition().x), 2)+
                            Math.pow((face.getLandmarks().get(Landmark.LEFT_EYE).getPosition().y-
                                    face.getLandmarks().get(Landmark.RIGHT_EYE).getPosition().y), 2)));

            float H = 63;
            float d = F*(H/sensorX)*(768/(2*p));

//            showStatus("focal length: "+F+
//                    "\nsensor width: "+sensorX
//                    +"\nd: "+String.format("%.0f",d)+"mm");

            float distanceInCm = d / 10;
            showStatus(String.format("%.0f", distanceInCm)+" cm", true);

            distance = distanceInCm;
            sendCameraBroadcast();
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            showStatus("Face undetected", false);
            distance = 0;
            sendCameraBroadcast();
        }

        @Override
        public void onDone() {
            super.onDone();
        }
    }

    public void showStatus(final String message, final boolean isDetected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isDetected) {
                    camDisTv.setTextSize(34);
                } else {
                    camDisTv.setTextSize(24);
                }

                camDisTv.setText(message);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showTooltipText(View view) {
        if (view.getId() == R.id.batteryTip) {
            view.setTooltipText(getResources().getString(R.string.menu_battery));
        } else if (view.getId() == R.id.cameraTip) {
            view.setTooltipText(getResources().getString(R.string.menu_camera));
        } else if (view.getId() == R.id.orientationTip) {
            view.setTooltipText(getResources().getString(R.string.menu_rotation));
        }
    }
}
