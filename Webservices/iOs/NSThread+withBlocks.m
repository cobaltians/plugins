//
//  NSThread+withBlocks.m
//
//  Created by Fehmi Toumi on 7/24/12.
//  Copyright (c) 2012 Haploid. All rights reserved.
//

#import "NSThread+withBlocks.h"

@implementation NSThread (withBlocks)

- (void) _plblock_execute: (void (^)()) block
{
    block();
}

- (void)performBlock: (void (^)()) block waitUntilDone: (BOOL) wait
{
	[self performSelector: @selector(_plblock_execute:)
                 onThread: self
               withObject: [block copy]
            waitUntilDone: wait];
}

@end