//
//  PubSubPlugin.m
//  Cobalt
//
//  Created by Kristal on 06/01/15.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "PubSubPlugin.h"

@implementation PubSubPlugin

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
    
}

@end
