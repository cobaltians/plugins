//
//  PubSubPlugin.h
//  PubSubPlugin
//
//  Created by Kristal on 06/01/15.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "CobaltAbstractPlugin.h"

@interface PubSubPlugin : CobaltAbstractPlugin {
    NSMutableDictionary * receiversForChannel;
}

@end
