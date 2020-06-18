package id.ac.stiki.doleno.digipub;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import id.ac.stiki.doleno.digipub.activities.MainActivity;

public class ForegroundService extends Service {

    private NotificationManager notificationManager;

    private int sensorTemperature;
    private int temperature;

    private boolean isNotificationDelivered = false;
    private boolean isNotificationEnabled = true;

    private int mWarningTemperature;
    private int mMeasuringUnit;

    public SharedPreferences mSharedPref;
    private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals(getString(R.string.key_warning_temperature))){
                        mWarningTemperature =
                                Integer.parseInt(mSharedPref.getString(getString(R.string.key_warning_temperature), Constants.DEFAULT_VALUES.WARNING_TEMPERATURE));
                        checkTemperature();
                    } else if (key.equals(getString(R.string.key_on_off_notification))) {
                        isNotificationEnabled = mSharedPref.getBoolean(getString(R.string.key_on_off_notification), true);
                        checkTemperature();
                    } else if (key.equals(getString(R.string.key_measuring_unit))){
                        mMeasuringUnit = Integer.parseInt(mSharedPref.getString(getString(R.string.key_measuring_unit), Constants.DEFAULT_VALUES.MEASURING_UNIT));
                        if (mMeasuringUnit == Constants.MEASURING_UNIT.CELSIUS){
                            temperature = sensorTemperature;
                        } else {
                            temperature = (int) (sensorTemperature * 1.8 + 32);
                        }
                        updateNotification();
                    }
                }
            };

    public ForegroundService() {
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            //Temperature in Celsius or Fahrenheit.
            sensorTemperature = intent.getIntExtra("temperature", 250) / 10;
            if (mMeasuringUnit== Constants.MEASURING_UNIT.CELSIUS) {
                temperature = sensorTemperature;
            } else {
                temperature = (int) (sensorTemperature * 1.8 +32);
            }
            updateNotification();
            checkTemperature();
        }
    };

    BroadcastReceiver mCameraReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public void onCreate() {
        //Register the Battery Info receiver
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //Setup Preferences
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPref.registerOnSharedPreferenceChangeListener(listener);
        mWarningTemperature =
                Integer.parseInt(mSharedPref.getString(getString(R.string.key_warning_temperature), Constants.DEFAULT_VALUES.WARNING_TEMPERATURE));
        mMeasuringUnit = Integer.parseInt(mSharedPref.getString(getString(R.string.key_measuring_unit), Constants.DEFAULT_VALUES.MEASURING_UNIT));
        isNotificationEnabled = mSharedPref.getBoolean(getString(R.string.key_on_off_notification), true);

        //Set up the NotificationManager
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Executes only on API26 and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(Constants.CHANNEL_ID.MAIN_CHANNEL,
                    getString(R.string.app_name),
                    getString(R.string.notification_channel_description),
                    NotificationManager.IMPORTANCE_DEFAULT);
            createNotificationChannel(Constants.CHANNEL_ID.WARNING_CHANNEL,
                    getString(R.string.app_name),
                    getString(R.string.warning_notification_channel_description),
                    NotificationManager.IMPORTANCE_HIGH);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String action = intent.getAction();
        if (action!=null) {
            switch (action) {
                case Constants.ACTION.STARTFOREGROUND_ACTION: {
                    Notification notification = createTrackingNotification();
                    startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                    break;
                }
                case Constants.ACTION.STOPFOREGROUND_ACTION: {
                    sendStopServiceBroadcast();
                    stopForeground(true);
                    stopSelf();
                }
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mSharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        unregisterReceiver(mBatInfoReceiver);
    }

    private Notification createTrackingNotification(){

        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                0
        );

        PendingIntent stopServicePendingIntent = PendingIntent.getService(
                this,
                0,
                new Intent(this, ForegroundService.class)
                        .setAction(Constants.ACTION.STOPFOREGROUND_ACTION),
                0);

//        NotificationCompat.Action action =
//                new NotificationCompat.Action.Builder(
//                        android.R.drawable.ic_dialog_info,
//                        getString(R.string.stop),
//                        stopServicePendingIntent).build();

        RemoteViews customNotif = new RemoteViews(getPackageName(), R.layout.notif_custom);
        customNotif.setTextViewText(R.id.tempValueTv, temperature + "");
        customNotif.setTextViewText(R.id.tempUnitTv, mMeasuringUnit == Constants.MEASURING_UNIT.CELSIUS? "°C" : "°F");

        return new NotificationCompat.Builder(this, Constants.CHANNEL_ID.MAIN_CHANNEL)
                //.setContentTitle(getString(R.string.app_name))
                //.setContentText(temperature + (mMeasuringUnit == Constants.MEASURING_UNIT.CELSIUS? "°C" : "°F"))
                .setSmallIcon(R.drawable.digipub_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_logo_digipub))
                .setCustomContentView(customNotif)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId(Constants.CHANNEL_ID.MAIN_CHANNEL)
                .setContentIntent(openAppPendingIntent)
                //.addAction(action)
                .build();
    }

    @TargetApi(26)
    protected void createNotificationChannel(String id, String name, String description, int importance) {

        NotificationChannel channel =
                new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        channel.setShowBadge(false);

        notificationManager.createNotificationChannel(channel);
    }

    private void updateNotification() {
        notificationManager.notify(
                Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                createTrackingNotification());
    }

    private void checkTemperature(){
        if (temperature > mWarningTemperature && !isNotificationDelivered && isNotificationEnabled) {
            notificationManager.notify(
                    Constants.NOTIFICATION_ID.TEMPERATURE_ALERT,
                    createAlertNotification());
            isNotificationDelivered = true;
        } else if (temperature <= mWarningTemperature && isNotificationDelivered) {
            isNotificationDelivered = false;
        }
    }

    private Notification createAlertNotification() {

        return new NotificationCompat.Builder(this, Constants.CHANNEL_ID.WARNING_CHANNEL)
                .setContentTitle(getString(R.string.battery_too_hot_title))
                .setContentText(getString(R.string.battery_too_hot_message))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.battery_too_hot_message)))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SYSTEM)
                .setVibrate(new long[] {0})
                .setChannelId(Constants.CHANNEL_ID.WARNING_CHANNEL)
                .build();
    }

    public void sendStopServiceBroadcast(){
        Intent intent = new Intent()
                .setAction(Constants.ACTION.STOPFOREGROUND_ACTION)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        sendBroadcast(intent);
    }
}
