//
//  BackgroundService.swift
//  Runner
//
//  Created by Markus Krebs on 23.06.19.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//




import Foundation
import CoreLocation

private let kRegionKey = "region"
private let kEventType = "event_type"
private let kEnterEvent = 1
private let kExitEvent = 2
private let kCallbackMapping = "geofence_region_callback_mapping"
private var instance: BackgroundService? = nil
private var registerPlugins: FlutterPluginRegistrantCallback? = nil
private var initialized = false


class BackgroundService: NSObject, CLLocationManagerDelegate, FlutterPlugin {
   
    
    /*static func register(with registrar: FlutterPluginRegistrar) {
        <#code#>
    }
    
    func isEqual(_ object: Any?) -> Bool {
        <#code#>
    }
    
    var hash: Int
    
    var superclass: AnyClass?
    
    @objc func `self`() -> Self {
        <#code#>
    }
    
    func perform(_ aSelector: Selector!) -> Unmanaged<AnyObject>! {
        <#code#>
    }
    
    func perform(_ aSelector: Selector!, with object: Any!) -> Unmanaged<AnyObject>! {
        <#code#>
    }
    
    func perform(_ aSelector: Selector!, with object1: Any!, with object2: Any!) -> Unmanaged<AnyObject>! {
        <#code#>
    }
    
    func isProxy() -> Bool {
        <#code#>
    }
    
    func isKind(of aClass: AnyClass) -> Bool {
        <#code#>
    }
    
    func isMember(of aClass: AnyClass) -> Bool {
        <#code#>
    }
    
    func conforms(to aProtocol: Protocol) -> Bool {
        <#code#>
    }
    
    func responds(to aSelector: Selector!) -> Bool {
        <#code#>
    }*/
    
    
    private var locationManager: CLLocationManager?
    private var headlessRunner: FlutterEngine?
    private var callbackChannel: FlutterMethodChannel?
    private var mainChannel: FlutterMethodChannel?
    private weak var registrar: FlutterPluginRegistrar?
    private var persistentState: UserDefaults?
    private var eventQueue: [AnyHashable]? = []
    private let onLocationUpdateHandle: Int64 = 0
    
    init(_ registrar: FlutterPluginRegistrar?) {
        super.init()
        
        // 1. Retrieve NSUserDefaults which will be used to store callback handles
        // between launches.
        persistentState = UserDefaults.standard
        
        // 2. Initialize the location manager, and register as its delegate.
        locationManager = CLLocationManager()
        locationManager!.delegate = self
        locationManager!.requestAlwaysAuthorization()
        locationManager!.allowsBackgroundLocationUpdates = true
        
        // 3. Initialize the Dart runner which will be used to run the callback
        // dispatcher.
        self.headlessRunner = FlutterEngine(name: "GeofencingIsolate", project: nil, allowHeadlessExecution: true)
        self.registrar = registrar!
        
        // 4. Create the method channel used by the Dart interface to invoke
        // methods and register to listen for method calls.
        self.mainChannel = FlutterMethodChannel(name: "plugins.flutter.io/geofencing_plugin", binaryMessenger: (registrar?.messenger())!)
        
        self.registrar?.addMethodCallDelegate(self, channel: mainChannel!)
        
        // 5. Create a second method channel to be used to communicate with the
        // callback dispatcher. This channel will be registered to listen for
        // method calls once the callback dispatcher is started.
        self.callbackChannel = FlutterMethodChannel(name: "plugins.flutter.io/geofencing_plugin_background", binaryMessenger: self.headlessRunner!)
    }
    

    
    
     static func register(with registrar: FlutterPluginRegistrar) {
        let lockQueue = DispatchQueue(label: "self")
        lockQueue.sync {
            if instance == nil {
                instance = BackgroundService(registrar)
                registrar.addApplicationDelegate(instance! as! FlutterPlugin)
            }
        }
    }
    static func setPluginRegistrantCallback(_ callback: @escaping FlutterPluginRegistrantCallback) {
        registerPlugins = callback
    }
    static func handle(_ call: FlutterMethodCall?, result: FlutterResult) {
        let arguments = call?.arguments
        if ("GeofencingPlugin.initializeService" == call?.method) {
            assert((arguments as AnyObject).count == 1, "Invalid argument count for 'GeofencingPlugin.initializeService'")
            //startGeofencingService((arguments[0] as NSNumber)?.intValue)
            result(NSNumber(value: true))
        } else if ("GeofencingService.initialized" == call?.method) {
            let lockQueue = DispatchQueue(label: "self")
            lockQueue.sync {
                initialized = true
                // Send the geofence events that occurred while the background
                // isolate was initializing.
                while instance!.eventQueue!.count > 0 {
                    let event = instance!.eventQueue![0] as? [AnyHashable : Any]
                    instance!.eventQueue!.remove(at: 0)
                    let region = event?[kRegionKey] as? CLRegion
                    let type = (event?[kEventType] as? NSNumber)?.intValue ?? 0
                    instance!.sendLocationEvent(region, eventType: type)
                }
            }
            result(nil)
        } else {
            result(FlutterMethodNotImplemented)
        }
    }
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        // Check to see if we're being launched due to a location event.
        if launchOptions?[UIApplication.LaunchOptionsKey.location] != nil {
            // Restart the headless service.
            startGeofencingService(getCallbackDispatcherHandle())
        }
        // Note: if we return NO, this vetos the launch of the application.
        return true
    }
    // MARK: LocationManagerDelegate Methods
    func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        let lockQueue = DispatchQueue(label: "self")
        lockQueue.sync {
            if initialized {
                sendLocationEvent(region, eventType: kEnterEvent)
            } else {
                let dict = [
                    kRegionKey: region,
                    kEventType: NSNumber(value: kEnterEvent)
                ]
                instance!.eventQueue!.append(dict)
            }
        }
    }
    func sendLocationEvent(_ region: CLRegion?, eventType event: Int) {
        assert((region is CLCircularRegion), "region must be CLCircularRegion")
        //let center = region?.
        //let handle = getCallbackHandle(forRegionId: region?.identifier)
        callbackChannel!.invokeMethod("", arguments: [
            NSNumber(value: 0),
            [0],
            [NSNumber(value: 0), NSNumber(value: 0)],
            NSNumber(value: 0)
            ])
    }
    func startGeofencingService(_ handle: Int64) {
        print("Initializing GeofencingService")
        setCallbackDispatcherHandle(handle)
        
        let info = FlutterCallbackCache.lookupCallbackInformation(handle)
        assert(info != nil, "failed to find callback")
        
        let entrypoint = info?.callbackName
        let uri = info?.callbackLibraryPath
        headlessRunner!.run(withEntrypoint: entrypoint, libraryURI: uri)
        assert(registerPlugins != nil, "failed to set registerPlugins")
        
        // Once our headless runner has been started, we need to register the application's plugins
        // with the runner in order for them to work on the background isolate. `registerPlugins` is
        // a callback set from AppDelegate.m in the main application. This callback should register
        // all relevant plugins (excluding those which require UI).
        registerPlugins!(headlessRunner!)
        registrar!.addMethodCallDelegate(self as! FlutterPlugin, channel: callbackChannel!)
    }
    func getCallbackDispatcherHandle() -> Int64 {
        let handle = persistentState!.object(forKey: "callback_dispatcher_handle")

        if handle == nil {
            return 0
        }
        return (handle as? NSNumber)?.int64Value ?? 0
    }
    
    func setCallbackDispatcherHandle(_ handle: Int64) {
        persistentState!.set(handle, forKey:"callback_dispatcher_handle")
    }
}
