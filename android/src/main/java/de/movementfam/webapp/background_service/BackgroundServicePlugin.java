package de.movementfam.webapp.background_service;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

/** BackgroundServicePlugin */
public class BackgroundServicePlugin implements MethodCallHandler {

  private static String TAG = "BackgroundServicePlugin";
  private static String SHARED_PREFERENCES_KEY = "backgroundservice_plugin_cache";
  private static String CALLBACK_HANDLE_KEY = "callback_handle";
  private static String CALLBACKI_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler";
  private static String PERSISTENT_MESSAGES_KEY = "persistent_messages";
  private static String PERSISTENT_MESSAGES_IDS = "persistent_messages_ids";
  private static String REQUIRED_PERMISSIONS = Manifest.permission.ACCESS_FINE_LOCATION;
  private static Object backgroundCacheLock = new Object();


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
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "de.movementfam.webapp/backgroundService");
    channel.setMethodCallHandler(plugin);
  }

  public static void reRegisterAfterReboot(Context context) {

  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }
}
