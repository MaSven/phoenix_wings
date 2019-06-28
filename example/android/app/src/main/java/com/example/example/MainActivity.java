package com.example.example;

import android.os.Bundle;

import de.movementfam.webapp.background_service.BackgroundService;
import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
      BackgroundService service = new BackgroundService();
  }
}
