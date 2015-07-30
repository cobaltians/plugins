//
//  PubSubPlugin.m
//  PubSubPlugin
//
//  Created by Kristal on 06/01/15.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "PubSubPlugin.h"

@implementation PubSubPlugin

- (id)init {
    if (self = [super init]) {
        viewControllersForChannel = [NSMutableDictionary dictionary];
    }
    
    return self;
}

////////////////////////////////////////////////////////////////////////////////////////

#pragma mark - COBALT

////////////////////////////////////////////////////////////////////////////////////////

- (void)onMessageFromCobaltController:(CobaltViewController *)viewController
                              andData:(NSDictionary *)data {
    [self onMessageWithCobaltController:viewController andData:data];
}

- (void)onMessageFromWebLayerWithCobaltController:(CobaltViewController *)viewController
                                          andData:(NSDictionary *)data {
    [self onMessageWithCobaltController:viewController andData:data];
}

- (void)onMessageWithCobaltController:(CobaltViewController *)viewController
                              andData:(NSDictionary *)data {
    id action = [data objectForKey:@"action"];
    id channel = [data objectForKey:@"channel"];
    id message = [data objectForKey:@"message"];
    id callback = [data objectForKey:@"callback"];
    
    NSAssert(action && [action isKindOfClass:[NSString class]], @"Missing action field or not a string...");
    NSAssert(channel && [channel isKindOfClass:[NSString class]], @"Subscribe - Missing channel field or not a string...");
    
    if ([action isEqualToString:@"publish"]) {
        [self publishMessage:message
                   toChannel:channel];
    }
    else if ([action isEqualToString:@"subscribe"]) {
        [self subscribeViewController:viewController
                            toChannel:channel
                         withCallback:callback];
    }
    else if ([action isEqualToString:@"unsubscribe"]) {
        [self unsubscribeViewController:viewController
                            FromChannel:channel];
    }
}

////////////////////////////////////////////////////////////////////////////////////////

#pragma mark - HELPERS

////////////////////////////////////////////////////////////////////////////////////////

- (void)publishMessage:(NSDictionary *)message
             toChannel:(NSString *)channel {
    // TODO: implement
}

- (void)subscribeViewController:(CobaltViewController *)viewController
                      toChannel:(NSString *)channel
                   withCallback:(NSString *)callback {
    // TODO: implement
}

- (void)unsubscribeViewController:(CobaltViewController *)viewController
                      FromChannel:(NSString *)channel {
    // TODO: implement
}

@end
