//
//  NSThread+withBlocks.h
//

#import <Foundation/Foundation.h>

@interface NSThread (withBlocks)

- (void)performBlock: (void (^)()) block waitUntilDone: (BOOL) wait;

@end
