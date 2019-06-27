package de.movementfam.webapp.background_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import io.flutter.view.FlutterMain;

public class BackgroundBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        FlutterMain.ensureInitializationComplete(context,null);
        BackgroundService.enqueueWork(context,intent);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("BackgroundReceiver","Reregistering Message");
            BackgroundServicePlugin.reRegisterAfterReboot(context);
        }
    }
}
