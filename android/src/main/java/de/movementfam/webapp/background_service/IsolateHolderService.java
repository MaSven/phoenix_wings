package de.movementfam.webapp.background_service;

import io.flutter.view.FlutterNativeView;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class IsolateHolderService extends Service {
    private static String ACTION_SHUTDOWN = "de.movementfam.webapp.ACTION_SHUTDOWN";
    private static String ACTION_START = "de.movementfam.webapp.ACTION_START";
    private String TAG ="BackgroundService::IsolateHolderService";
    private String WAKELOCK_TAG = "IsolateHolderService::WAKE_LOCK";

    private FlutterNativeView backgroundFlutterView = null;


    public static void setBackgroundFlutterView(FlutterNativeView view) {
        this.backgroundFlutterView = view;
    }

    @androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() == ACTION_SHUTDOWN) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification =
                new Notification.Builder(this, "BackgroundService::IsolateHolderService")
                        .setContentTitle("Test")
                        .setContentText("Test")
                        .setSmallIcon(getResources().getIdentifier("ic_launcher","mipmap",getPackageName()))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .build();
        startForeground(1,notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
