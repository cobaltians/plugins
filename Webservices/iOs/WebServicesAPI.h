//
//  WebServicesAPI.h
//

#import "NSThread+withBlocks.h"
#import "CobaltViewController.h"

#define DEBUGAPI    1


@protocol WebServicesStorageDelegate <NSObject>

- (id) storedValueForKey: (NSString *) key;
- (void)storeValue: (id)value forKey: (NSString *)key;

@end

@interface WebServicesAPI : NSObject <WebServicesStorageDelegate> {
}

@property int nbRequete;
@property (nonatomic, strong) NSOperationQueue *queue;

+ (WebServicesAPI *)sharedInstance;

- (void)doWebServicesRequestWithData: (NSDictionary *)data andViewController: (CobaltViewController *)viewController andCallId: (NSNumber *)callId;
- (void)checkNetworkActivity;

@end

