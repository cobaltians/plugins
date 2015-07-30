//
//  PubSubPlugin.m
//  PubSubPlugin
//
//  Created by Kristal on 06/01/15.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "PubSubPlugin.h"

#import "PubSubReceiver.h"

@implementation PubSubPlugin

- (id)init {
    if (self = [super init]) {
        receiversForChannel = [NSMutableDictionary dictionary];
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
    NSArray *receivers = [receiversForChannel objectForKey:channel];
    
    if (! receivers) {
        NSLog(@"PubSubPlugin publishMessage:toChannel: - No receiver has already subscribed to %@ channel or they all have already unsubscribe.", channel);
        return;
    }
    
    [receivers enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        [(PubSubReceiver *)obj receiveMessage:message
                                   forChannel:channel];
    }];
}

- (void)subscribeViewController:(CobaltViewController *)viewController
                      toChannel:(NSString *)channel
                   withCallback:(NSString *)callback {
    NSMutableArray *receivers = [receiversForChannel objectForKey:channel];
    __block PubSubReceiver *receiver;
    
    if (receivers) {
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
            [receivers addObject:receiver];
        }
    }
    else {
        receiver = [[PubSubReceiver alloc] initWithViewController:viewController
                                                      andCallback:callback
                                                       forChannel:channel];
        receivers = [NSMutableArray arrayWithObject:receiver];
        [receiversForChannel setObject:receivers forKey:channel];
    }
}

- (void)unsubscribeViewController:(CobaltViewController *)viewController
                      FromChannel:(NSString *)channel {
    // TODO: implement
}

@end
