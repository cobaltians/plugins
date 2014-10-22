//
//  WebServicesAPI.m
//

#import "WebServicesAPI.h"

//////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark IMPLEMENTATION
//////////////////////////////////////////////////////////////////////////////////////////////////

@implementation WebServicesAPI

@synthesize nbRequete;
@synthesize queue;

//////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark SINGLETON
//////////////////////////////////////////////////////////////////////////////////////////////////

static WebServicesAPI *sharedApi = nil;

//*******************
// SHARED INSTANCE  *
//*******************
//
// Description :
/*!
 \param :
 */

+ (WebServicesAPI *)sharedInstance{
	@synchronized(self){
		if (sharedApi == nil){
			sharedApi = [[self alloc] init];
		}
	}
	return sharedApi;
}

//////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark INITIALISATION
//////////////////////////////////////////////////////////////////////////////////////////////////
//********
// INIT  *
//********
//
// Description :
/*!
 \param :
 */

- (id)init{
	if ((self = [super init])) {
        self.queue = [[NSOperationQueue alloc] init];
        nbRequete = 0;
	}
	return self;
}

- (void)doWebServicesRequestWithData: (NSDictionary *)data andViewController: (CobaltViewController *)viewController {
    __unsafe_unretained WebServicesAPI * wsAPI = self;
    [queue addOperationWithBlock:^{
        @autoreleasepool {
            NSString * url = [data objectForKey: @"url"];
            NSString * type = [data objectForKey: @"type"];
            NSDictionary * params = [data objectForKey: @"params"];
            
            NSMutableString * requestURL = [NSMutableString stringWithString: url];
            [requestURL appendString: @"?"];
            for(NSString * key in [params allKeys]) {
                [requestURL appendFormat: @"%@=%@&", key, [params objectForKey: key]];
            }
            
            NSURLRequest * request = [NSURLRequest requestWithURL: [NSURL URLWithString: requestURL]];
            
            if (DEBUGAPI) NSLog(@"%@", request);
            
            NSError * errorHttp = nil;
            NSHTTPURLResponse * response;
            
            wsAPI.nbRequete++;
            [wsAPI checkNetworkActivity];
            NSData * requestData = [NSURLConnection sendSynchronousRequest: request returningResponse: &response error: &errorHttp];
            wsAPI.nbRequete--;
            [wsAPI checkNetworkActivity];
            
            NSString *responseString = [[NSString alloc] initWithData: requestData encoding:NSUTF8StringEncoding];
            
            if (DEBUGAPI) NSLog(@"%@", responseString);
            
            if (response.statusCode == 200){
                
                NSError *error;
                
                NSDictionary *data = [NSJSONSerialization JSONObjectWithData: requestData options:kNilOptions error:&error];
                
                if(data) {
                    [[NSThread mainThread] performBlock:^{
                        NSDictionary * dataToSendToWeb = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onWSResult", @"data" : @{
                            @"callId" : @0,
                            @"data" : data,
                            @"text" : @""
                            }};
                        
                        [viewController sendMessage: dataToSendToWeb];
                    } waitUntilDone:NO];
                }
                
                [[NSThread mainThread] performBlock:^{
                    [[NSNotificationCenter defaultCenter] postNotificationName: @"lol" object: nil userInfo: nil];
                } waitUntilDone:NO];
                
            }
            else {
                [wsAPI performSelectorOnMainThread:@selector(processError:) withObject:errorHttp waitUntilDone:YES];
            }
        }
	}];
}

//**************************
// CHECK NETWORK ACTIVITY  *
//**************************
//
// Description :
/*!
 \param :
 */

- (void)checkNetworkActivity {
    if (nbRequete > 0){
		[[NSThread mainThread] performBlock:^{
			[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
        } waitUntilDone:NO];
	}
	else {
		[[NSThread mainThread] performBlock:^{
			[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
		} waitUntilDone:NO];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
#pragma mark ERROR MANAGEMENT
#pragma mark -
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//****************
// PROCESS ERROR *
//****************
/*!
 @method		- (void)processError:(NSError *)error
 @abstract		Processes any error that might occur when calling web services.
 @param         error   An NSError object that encapsulates richer and more extensible error information.
 @discussion    You might consider using [error.userInfo objectForKey:kErrorNotificationKey]
 to retrieve the web service that raised the error and deal with it accordingly.
 */

/*
- (void)processError:(NSError *)error {
    if (!displayingAlertView) {
		UIAlertView *alertView = nil;
		switch (error.code) {
            case 500:
				alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", nil) message:([error.userInfo objectForKey:kErrorMessageKey] != nil ? [error.userInfo objectForKey:kErrorMessageKey] : NSLocalizedString(@"Unknown error.", nil)) delegate:self cancelButtonTitle:NSLocalizedString(@"OK", nil) otherButtonTitles:nil];
				break;
			case 501:
				alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", nil) message:NSLocalizedString(@"Error while deserializing JSON object.", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"OK", nil) otherButtonTitles:nil];
				break;
		}
        if ([error.domain isEqualToString:@"NSURLErrorDomain"]) {
            // Check reachability for internet connection
            Reachability *reachability = [Reachability reachabilityForInternetConnection];
            NetworkStatus status = [reachability currentReachabilityStatus];
            if (status == NotReachable) alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Vous n'êtes pas connecté", nil) message:NSLocalizedString(@"Cette opération nécessite une connexion internet", nil) delegate:self cancelButtonTitle:NSLocalizedString(@"OK", nil) otherButtonTitles:nil];
        }
		if (alertView != nil) {
			[alertView show];
			displayingAlertView = YES;
		}
	}
}
*/

@end

