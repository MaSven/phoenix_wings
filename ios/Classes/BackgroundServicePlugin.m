#import "BackgroundServicePlugin.h"
#import "BackgroundServicePlugin.m"
//#import "SwiftBackgroundServicePlugin.swift"
//#import <background_service/background_service-Swift.h>

@implementation BackgroundServicePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    [BackgroundServicePlugin registerWithRegistrar:registrar];
}
@end
