//
//  PubSubReceiver.h
//  PubSubPlugin
//
//  Created by Kristal on 29/07/2015.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CobaltViewController.h"

@class PubSubReceiver;

@protocol PubSubReceiverDelegate <NSObject>

@required

- (void)receiverReadyForRemove:(PubSubReceiver *)receiver;

@end

@interface PubSubReceiver : NSObject {
    NSMutableDictionary *callbackForChannel;
}

@property (weak, nonatomic, readonly) CobaltViewController *viewController;
@property (weak, nonatomic) id <PubSubReceiverDelegate> delegate;

- (id)initWithViewController:(CobaltViewController *)viewController;
- (id)initWithViewController:(CobaltViewController *)viewController
                 andCallback:(NSString *)callback
                  forChannel:(NSString *)channel;
- (void)subscribeToChannel:(NSString *)channel
              withCallback:(NSString *)callback;
- (void)unsubscribeFromChannel:(NSString *)channel;
- (void)receiveMessage:(NSDictionary *)message
            forChannel:(NSString *)channel;

@end
