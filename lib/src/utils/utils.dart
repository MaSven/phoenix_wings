import 'package:flutter/services.dart';
import 'dart:ui';
import 'package:flutter/material.dart';


enum MessageEvent {
  send,receive,end
}

class Message {
  
}

void callbackDispatcher() {
  const MethodChannel _backgroundChannel =
  MethodChannel('de.movementfam.webapp/backgroundService');
  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final List<dynamic> args = call.arguments;
    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));
    assert(callback != null);
    callback();
  });
  _backgroundChannel.invokeMethod('BackgroundService.initialized');
}