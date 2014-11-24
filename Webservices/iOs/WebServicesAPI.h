//
//  WebServicesAPI.h
//

#import "NSThread+withBlocks.h"
#import "CobaltViewController.h"

#define DEBUGAPI    0


#define kStatusCode @"statusCode"

@protocol WebServicesStorageDelegate <NSObject>

@optional

- (id) storedValueForKey: (NSString *) key;
- (void)storeValue: (id)value forKey: (NSString *)key;

- (id)processData: (id)data withParameters: (NSDictionary *)parameters;

- (void)handleWebResponseWithData: (NSDictionary *)responseDict withRequest: (NSDictionary *)requestPararameters;
- (void)handleErrorWithData: (NSDictionary *)errorDict withRequest: (NSDictionary *)requestPararameters;

@end

@interface WebServicesAPI : NSObject <WebServicesStorageDelegate> {
}

@property int nbRequete;
@property (nonatomic, strong) NSOperationQueue *queue;

+ (WebServicesAPI *)sharedInstance;

- (void)doWebServicesRequestWithData: (NSDictionary *)data andViewController: (CobaltViewController *)viewController andCallId: (NSNumber *)callId;
- (void)checkNetworkActivity;

@end

