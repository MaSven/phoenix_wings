package de.movementfam.webapp.background_service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.*;


import io.flutter.app.FlutterPluginRegistry;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundService extends JobIntentService implements MethodCallHandler {

    private final ArrayDeque queue = new ArrayDeque();
    private MethodChannel mBackgroundChannel;
    private Context mContext;
    private static final String TAG = "BackgroundService";
    private static final int JOB_ID = -1;
    private static FlutterNativeView sBackgroundFlutterView;
    private static final AtomicBoolean sServiceStarted = new AtomicBoolean(false);
    private static PluginRegistrantCallback sPluginRegistrantCallback;


    public static void setsPluginRegistrantCallback(PluginRegistrantCallback sPluginRegistrantCallback){
        sPluginRegistrantCallback=sPluginRegistrantCallback;
    }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context,BackgroundService.class,JOB_ID,intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        intent.getLongExtra(BackgroundServicePlugin.CALLBACK_HANDLE_KEY,0);
        synchronized (sServiceStarted){
            if(sServiceStarted.get()){
                mBackgroundChannel.invokeMethod("",new ArrayList<Object>());
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startBackgroundService(this);
    }



    private void startBackgroundService(Context context){
        synchronized (sServiceStarted){
            mContext = context;
            if(sBackgroundFlutterView==null){
                long callbackHandle = context.getSharedPreferences(BackgroundServicePlugin.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                        .getLong(BackgroundServicePlugin.CALLBACKI_DISPATCHER_HANDLE_KEY, 0);
                FlutterCallbackInformation flutterCallbackInformation = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle);
                if(flutterCallbackInformation == null){
                    Log.e(TAG,"Fatal: failed to find callback");
                    return;
                }
                Log.i(TAG,"Starting Backgroundservice ..");
                sBackgroundFlutterView = new FlutterNativeView(context,true);
                FlutterPluginRegistry pluginRegistry = sBackgroundFlutterView.getPluginRegistry();
                sPluginRegistrantCallback.registerWith(pluginRegistry);
                FlutterRunArguments flutterRunArguments = new FlutterRunArguments();
                flutterRunArguments.bundlePath = FlutterMain.findAppBundlePath(context);
                flutterRunArguments.entrypoint = flutterCallbackInformation.callbackName;
                flutterRunArguments.libraryPath = flutterCallbackInformation.callbackLibraryPath;
                sBackgroundFlutterView.runFromBundle(flutterRunArguments);
                new IsolateHolderService().setBackgroundFlutterView(sBackgroundFlutterView);
            }
        }
        mBackgroundChannel = new MethodChannel(sBackgroundFlutterView,"webapp.movement.de/background_service_plugin");
        mBackgroundChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall methodCall, Result result) {
        switch (methodCall.method){
            case "BackgroundService.initialized":
                synchronized (sServiceStarted){
                    while (!queue.isEmpty()){
                        mBackgroundChannel.invokeMethod("",queue.remove());
                    }
                    sServiceStarted.set(true);
                }
                result.success(null);
                break;
            case "BackgroundService.promoteToForeground":
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(new Intent(mContext, IsolateHolderService.class));
                }else{
                    mContext.startService(new Intent(mContext, IsolateHolderService.class));
                }
                result.success(null);
                break;
            case "BackgroundService.demoteToBackground":
                Intent intent = new Intent(mContext,IsolateHolderService.class);
                intent.setAction(IsolateHolderService.ACTION_SHUTDOWN);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(intent);
                }else{
                    mContext.startService(intent);
                }
                result.success(null);
                break;
                default: result.notImplemented();
                break;
        }
    }
}
