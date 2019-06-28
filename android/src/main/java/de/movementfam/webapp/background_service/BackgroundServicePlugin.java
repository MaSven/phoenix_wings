package de.movementfam.webapp.background_service;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;;
import android.os.Build;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

/** BackgroundServicePlugin */
public class BackgroundServicePlugin implements MethodCallHandler {

  private static final String TAG = "BackgroundServicePlugin";
  static final String SHARED_PREFERENCES_KEY = "backgroundservice_plugin_cache";
  static final String CALLBACK_HANDLE_KEY = "callback_handle";
  static final String CALLBACKI_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler";
  private static final String PERSISTENT_MESSAGES_KEY = "persistent_messages";
  private static final String PERSISTENT_MESSAGES_IDS = "persistent_messages_ids";
  private static final String REQUIRED_PERMISSIONS = Manifest.permission.ACCESS_FINE_LOCATION;
  private static final Object backgroundCacheLock = new Object();


  private Context mContext;
  private Activity mActivity;
  //private LocationService mLocations;

  BackgroundServicePlugin(Context context) {
    this.mContext = context;
  }

  BackgroundServicePlugin(Context context, Activity activity) {
    this.mContext = context;
    this.mActivity = activity;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final BackgroundServicePlugin plugin = new BackgroundServicePlugin(registrar.context(),registrar.activity());
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "webapp.movementfam.de/background_service");
    channel.setMethodCallHandler(plugin);
  }

  public static void reRegisterAfterReboot(Context context) {
    synchronized (backgroundCacheLock){
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
              (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                      == PackageManager.PERMISSION_DENIED)) {
        final String  msg = "'registerGeofence' requires the ACCESS_FINE_LOCATION permission.";
        Log.w(TAG, msg);
      }


    }
  }

  private String getPersistentMessageKey(String id){
    return "persistent_message/"+id;
  }

  private void initializeService(Context context, ArrayList<?> args){
    Log.d(TAG,"initializeService");
    //Java is a shitshow
    if(args.size()==0 || args.size()>=1 && (args.get(0) == null || !(args.get(0) instanceof Long))){
      throw new RuntimeException("No Callbackhandle found in argumentlist");
    }
    final Long callBackhandle = (Long) args.get(0);
    context.getSharedPreferences(SHARED_PREFERENCES_KEY,Context.MODE_PRIVATE)
            .edit()
            .putLong(CALLBACKI_DISPATCHER_HANDLE_KEY,callBackhandle)
            .apply();
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    final Object  arguments = call.arguments();
    String method = call.method;
    switch (method){
      case "BackgroundService.initializeService":
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
          mActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},12312);
        }
        result.success(true);
        break;
      default: result.notImplemented();
      break;
    }
  }
}
