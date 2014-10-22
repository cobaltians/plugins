//
//  CobaltWebservicesPlugin.m
//  Cobalt
//
//  Created by Haploid on 22/10/14.
//  Copyright (c) 2014 Haploid. All rights reserved.
//

#import "CobaltWebservicesPlugin.h"

@implementation CobaltWebservicesPlugin

- (id)init{
    if (self = [super init]) {
        //
    }
    return self;
}

- (void)onMessageFromCobaltController:(CobaltViewController *)viewController andData: (NSDictionary *)data {
    _viewController = viewController;
    
    [[WebServicesAPI sharedInstance] doWebServicesRequestWithData: [data objectForKey: @"data"] andViewController: viewController];
}


@end
