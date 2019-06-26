package de.movementfam.webapp.background_service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterRunArguments;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.UUID;

import com.google.android.gms.location.GeofencingEvent;

public class BackgroundService extends JobIntentService implements MethodCallHandler {

    private final ArrayDeque queue = new ArrayDeque();
    private MethodChannel mBackgroundChannel;
    private Context mContext;
    private static final String TAG = "BackgroundService";
    private static final int JOB_ID = -1;
    private static FlutterNativeView sBackgroundFlutterView;
    private static final AtomicBoolean sServiceStarted = null;
    private static PluginRegistrantCallback sPluginRegistrantCallback;

    public static void enqueueWork(Context context, Intent intent) {
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }

    @Override
    public void onMethodCall(MethodCall methodCall, Result result) {

    }
}
