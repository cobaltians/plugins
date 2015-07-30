//
//  PubSubReceiver.m
//  PubSubPlugin
//
//  Created by Kristal on 29/07/2015.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "PubSubReceiver.h"

@implementation PubSubReceiver

- (id)initWithViewController:(CobaltViewController *)viewController {
    if (self = [super init]) {
        _viewController = viewController;
        callbackForChannel = [NSMutableDictionary dictionary];
    }
    
    return self;
}

- (id)initWithViewController:(CobaltViewController *)viewController
                 andCallback:(NSString *)callback
                  forChannel:(NSString *)channel {
    if (self = [super init]) {
        _viewController = viewController;
        callbackForChannel = [NSMutableDictionary dictionaryWithDictionary:@{channel: callback}];
    }
    
    return self;
}

- (void)subscribeToChannel:(NSString *)channel
              withCallback:(NSString *)callback {
    NSAssert(channel, @"Cannot subscribe for a nil channel.");
    
    [callbackForChannel setObject:callback
                           forKey:channel];
}

- (BOOL)unsubscribeFromChannel:(NSString *)channel {
    NSAssert(channel, @"Cannot unsubscribe from a nil channel.");
    
    [callbackForChannel removeObjectForKey:channel];
    
    return ! callbackForChannel.count;
}

- (void)receiveMessage:(NSDictionary *)message
            forChannel:(NSString *)channel {
    // TODO: replace this assert with a nil check and re-order instructions
    NSAssert(_viewController, @"viewController is not initialized. \
             Use initWithViewController: or initWithViewController:andCallback:forChannel: methods to instantiate a PubSubReceiver.");
    NSAssert(channel, @"Cannot send message to nil channel");
    
    NSString *callback = [callbackForChannel objectForKey:channel];
    
    if (! callback) {
        NSLog(@"PubSubReceiver receiveMessage:forChannel: - %@ has not subscribed to %@ channel or has already unsubscribe.", [_viewController class], channel);
        return;
    }
    
    [_viewController sendCallback:callback
                        withData:message];
}

@end
