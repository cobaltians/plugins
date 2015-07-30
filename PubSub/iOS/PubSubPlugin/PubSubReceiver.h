//
//  PubSubReceiver.h
//  PubSubPlugin
//
//  Created by Kristal on 29/07/2015.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CobaltViewController.h"

@interface PubSubReceiver : NSObject {
    CobaltViewController *viewController;
    NSMutableDictionary * callbackForChannel;
}

- (id)initWithViewController:(CobaltViewController *)viewController;
- (id)initWithViewController:(CobaltViewController *)viewController
                 andCallback:(NSString *)callback
                  forChannel:(NSString *)channel;
- (void)subscribeToChannel:(NSString *)channel
              withCallback:(NSString *)callback;
- (BOOL)unsubscribeFromChannel:(NSString *)channel;
- (void)receiveMessage:(NSDictionary *)message
            forChannel:(NSString *)channel;

@end
