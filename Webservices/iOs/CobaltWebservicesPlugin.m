//
//  CobaltWebservicesPlugin.m
//  Cobalt
//
//  Created by Haploid on 22/10/14.
//  Copyright (c) 2014 Haploid. All rights reserved.
//

#import "CobaltWebservicesPlugin.h"

@implementation CobaltWebservicesPlugin

- (id)init
{
    if (self = [super init])
    {
        self.callId = @0;
    }
    
    return self;
}

- (void)onMessageFromCobaltController:(CobaltViewController *)viewController andData: (NSDictionary *)data
{
    _viewController = viewController;
    
     NSString * callback = [data objectForKey:kJSCallback];
    
    @synchronized(self.callId)
    {
        self.callId = @([self.callId intValue] + 1);
        [_viewController sendCallback: callback withData: @{ @"callId": self.callId}];
    
        [[WebServicesAPI sharedInstance] doWebServicesRequestWithData: [data objectForKey: @"data"] andViewController: viewController andCallId: self.callId];
    }
}


@end
