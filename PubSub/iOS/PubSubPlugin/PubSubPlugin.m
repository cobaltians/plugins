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
        receivers = [NSMutableArray array];
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
    NSAssert(action && [action isKindOfClass:[NSString class]], @"Missing action field or not a string...");
    
    id innerData = [data objectForKey:@"data"];
    NSAssert(innerData && [innerData isKindOfClass:[NSDictionary class]], @"Missing data field or not a object...");
    
    id channel = [innerData objectForKey:@"channel"];
    id message = [innerData objectForKey:@"message"];
    id callback = [innerData objectForKey:@"callback"];
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
    [receivers enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        [(PubSubReceiver *)obj receiveMessage:message
                                   forChannel:channel];
    }];
}

- (void)subscribeViewController:(CobaltViewController *)viewController
                      toChannel:(NSString *)channel
                   withCallback:(NSString *)callback {
    __block PubSubReceiver *receiver;
    
    [receivers enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([viewController isEqual:[(PubSubReceiver *)obj viewController]]) {
            receiver = obj;
            *stop = YES;
        }
    }];
    
    if (receiver) {
        [receiver subscribeToChannel:channel
                        withCallback:callback];
    }
    else {
        receiver = [[PubSubReceiver alloc] initWithViewController:viewController
                                                      andCallback:callback
                                                       forChannel:channel];
        [receiver setDelegate:self];
        [receivers addObject:receiver];
    }
}

- (void)unsubscribeViewController:(CobaltViewController *)viewController
                      FromChannel:(NSString *)channel {
    __block PubSubReceiver *receiver;
    
    [receivers enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([viewController isEqual:[(PubSubReceiver *)obj viewController]]) {
            receiver = obj;
            *stop = YES;
        }
    }];
    
    if (receiver) {
        [receiver unsubscribeFromChannel:channel];
    }
}

////////////////////////////////////////////////////////////////////////////////////////

#pragma mark - PUBSUB RECEIVER DELEGATE

////////////////////////////////////////////////////////////////////////////////////////

- (void)receiverReadyForRemove:(PubSubReceiver *)receiver {
    [receivers removeObject:receiver];
}

@end
