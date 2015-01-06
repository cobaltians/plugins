//
//  CobaltAppInfosPlugin.m
//  Cobalt
//
//  Created by Kristal on 06/01/15.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "CobaltAppInfosPlugin.h"

@implementation CobaltAppInfosPlugin

- (void) onMessageFromCobaltController: (CobaltViewController *)viewController
                               andData: (NSDictionary *)data {
    [self onMessageWithCobaltController:viewController andData:data];
}

- (void) onMessageFromWebLayerWithCobaltController: (CobaltViewController *)viewController
                                           andData: (NSDictionary *)data {
    [self onMessageWithCobaltController:viewController andData:data];
}

- (void) onMessageWithCobaltController: (CobaltViewController *)viewController
                               andData: (NSDictionary *)data {
    NSString * callback = [data objectForKey:kJSCallback];
    NSString * action = [data objectForKey:kJSAction];
    
    if (action != nil && [action isEqualToString:ACTION_GET_APP_INFOS]) {
        NSDictionary * appInfos = [CobaltAppInfosPlugin getAppInfos];
        
        [viewController sendCallback: callback
                            withData: appInfos];
    }
}

+ (NSDictionary *) getAppInfos {
    NSBundle * mainBundle = [NSBundle mainBundle];
    if (mainBundle == nil) return @{};
    
    NSDictionary * infos = [[NSBundle mainBundle] infoDictionary];
    NSString * version = [infos objectForKey:VERSION];
    NSString * build = [infos objectForKey:BUILD];
    
    return @{VERSION_NAME: version, VERSION_CODE: build};
}

@end
