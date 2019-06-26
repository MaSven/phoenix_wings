import 'package:phoenix_wings/src/utils/utils.dart';
import 'dart:async';
import 'dart:io';
import 'dart:ui';
import 'package:flutter/services.dart';


abstract class BackgroundSericePlugin {
  static const MethodChannel _channel =
  const MethodChannel('de.movementfam.webapp/backgroundService');

  /// Initialize the plugin
  static Future<bool> initialize() async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    await _channel.invokeMethod('BackgroundService.initializeService',
        <dynamic>[callback.toRawHandle()]);
  }

  /// Register Service
  static Future<bool> registerService(
      void Function(List<String> id, Message message, MessageEvent event)
      callback) {
    final args = <dynamic>[
      PluginUtilities.getCallbackHandle(callback).toRawHandle()
    ];
    //args.addAll(id);
    _channel.invokeMethod('BackgroundService.registerService', args);
  }
}