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

+ (WebServicesAPI *)sharedInstance
{
	@synchronized(self)
    {
		if (sharedApi == nil)
        {
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

- (id)init
{
	if ((self = [super init]))
    {
        self.queue = [[NSOperationQueue alloc] init];
        nbRequete = 0;
	}
	return self;
}


//**************
// WS REQUEST  *
//**************
//
// Description :
/*!
 \param :
 */
- (void)doWebServicesRequestWithData: (NSDictionary *)dataToCreateRequest andViewController: (CobaltViewController *)viewController andCallId: (NSNumber *)callId
{
    __unsafe_unretained WebServicesAPI * wsAPI = self;
    [queue addOperationWithBlock:^
    {
        @autoreleasepool
        {
            NSString * url = [dataToCreateRequest objectForKey: @"url"];
            NSString * type = [dataToCreateRequest objectForKey: @"type"];
            NSString * params = [dataToCreateRequest objectForKey: @"params"];
            NSNumber * saveToStorage = [dataToCreateRequest objectForKey: @"saveToStorage"];
            NSNumber * sendCacheResult = [dataToCreateRequest objectForKey: @"sendCacheResult"];
            NSString * storageKey = [dataToCreateRequest objectForKey: @"storageKey"];
            NSDictionary * processData = [dataToCreateRequest objectForKey: @"processData"];
            NSDictionary * HTTPHeaders = [dataToCreateRequest objectForKey: @"headers"];

            if([sendCacheResult boolValue])
            {
                id storedValue = nil;
                
                if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(storedValueForKey:)])
                {
                    id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                    storedValue = [p storedValueForKey: storageKey];
                }
                else
                {
                    storedValue = [wsAPI storedValueForKey: storageKey];
                }
                
                if(storedValue)
                {
                    NSDictionary * storedDataToSend = nil;
                    
                    NSError * error;
                    NSData *data = [storedValue dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary * storedJSONValue = [NSJSONSerialization JSONObjectWithData:data
                                                                                     options:kNilOptions
                                                                                       error:&error];
                    
                    if(data)
                    {
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(processData:withParameters:)] && processData)
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                            storedJSONValue = [p processData: storedJSONValue withParameters: processData];
                        }
                        
                        storedDataToSend = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onStorageResult", @"data" : @{
                                                      @"callId" : callId,
                                                      @"data" : storedJSONValue
                                                      }};
                    }
                    else if(storedValue)
                    {
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(processData:withParameters:)] && processData)
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                            storedValue = [p processData: storedValue withParameters: processData];
                        }
                        
                        storedDataToSend = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onStorageResult", @"data" : @{
                                                      @"callId" : callId,
                                                      @"text" : storedValue
                                                      }};
                    }
                    else
                    {
                        NSDictionary * noStoredDataToSend = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onStorageError", @"data" : @{
                                                                       @"callId" : callId,
                                                                       @"text" : @"UNKNOWN_ERROR",
                                                                       }};
                        [viewController sendMessage: noStoredDataToSend];
                    }
                    
                    if(storedDataToSend)
                    {
                        [viewController sendMessage: storedDataToSend];
                    }
                }
                else
                {
                    NSDictionary * noStoredDataToSend = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onStorageError", @"data" : @{
                                                  @"callId" : callId,
                                                  @"text" : @"NOT_FOUND",
                                                  }};
                    [viewController sendMessage: noStoredDataToSend];
                }
            }
            
            if(url.length == 0)
            {
                return;
            }
            
            NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
            
            NSMutableString * requestURL = [NSMutableString stringWithString: url];
            
            if([type isEqualToString: @"GET"] || [type isEqualToString: @"DELETE"])
            {
                [requestURL appendFormat: @"?%@", params];
            }
            else if([type isEqualToString: @"POST"] || [type isEqualToString: @"PUT"])
            {
                NSData *postData = [params dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
                [request setHTTPBody: postData];
            }
            else
            {
                return;
            }
            
            [request setHTTPMethod: type];
            [request setURL: [NSURL URLWithString: requestURL]];
            
            for (NSString* key in HTTPHeaders) {
                NSString * value = [HTTPHeaders objectForKey:key];
                [request setValue: value forHTTPHeaderField: key];
            }
                
                
            if (DEBUGAPI) NSLog(@"%@", request);
            
            NSError * errorHttp = nil;
            NSHTTPURLResponse * response;
            
            wsAPI.nbRequete++;
            [wsAPI checkNetworkActivity];
            NSData * requestData = [NSURLConnection sendSynchronousRequest: request returningResponse: &response error: &errorHttp];
            wsAPI.nbRequete--;
            [wsAPI checkNetworkActivity];
            
            __block NSString *responseString = [[NSString alloc] initWithData: requestData encoding:NSUTF8StringEncoding];
            
            if (DEBUGAPI) NSLog(@"%@", responseString);
            
            if ((response.statusCode < 400) && (response.statusCode != 0))
            {
                NSError *error;
                
                __block NSDictionary *data = [NSJSONSerialization JSONObjectWithData: requestData options:kNilOptions error:&error];
                
                if(data) {
                    [[NSThread mainThread] performBlock:^
                    {
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(processData:withParameters:)] && processData)
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                                data = [p processData: data withParameters: processData];
                        }
                        
                        NSDictionary * dataToSendToWeb = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onWSResult", @"data" : @{
                                                                    @"callId" : callId,
                                                                    @"data" : data,
                                                                    kStatusCode : @(response.statusCode)
                                                                    }};
                        
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(handleWebResponseWithData:withRequest:)])
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                            [p handleWebResponseWithData: dataToSendToWeb withRequest: dataToCreateRequest];
                        }
                        else {
                            [viewController sendMessage: dataToSendToWeb];
                        }
                        
                    } waitUntilDone:NO];
                } else {
                    [[NSThread mainThread] performBlock:^
                    {
                        
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(processData:withParameters:)] && processData)
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                                responseString = [p processData: responseString withParameters: processData];
                        }
                        
                        NSDictionary * dataToSendToWeb = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onWSResult", @"data" : @{
                                                                    @"callId" : callId,
                                                                    @"text" : responseString,
                                                                    kStatusCode : @(response.statusCode)
                                                                    }};
                        
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(handleWebResponseWithData:withRequest:)])
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                            [p handleWebResponseWithData: dataToSendToWeb withRequest: dataToCreateRequest];
                        }
                        else {
                            [viewController sendMessage: dataToSendToWeb];
                        }
                        
                    } waitUntilDone:NO];
                }
                
                [[NSThread mainThread] performBlock:^
                {
                    if(saveToStorage && storageKey)
                    {
                        if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(storeValue:forKey:)])
                        {
                            id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                            [p storeValue: responseString forKey: storageKey];
                        }
                        else
                        {
                            [wsAPI storeValue: responseString forKey: storageKey];
                        }
                    }
                } waitUntilDone: NO];
                
                //In case we want any VC in the app get the WS response
                /*[[NSThread mainThread] performBlock:^{
                    [[NSNotificationCenter defaultCenter] postNotificationName: @"lol" object: nil userInfo: nil];
                } waitUntilDone:NO];*/
                
            }
            else
            {
                [[NSThread mainThread] performBlock:^
                {
                    int errorCode = -1;
                    switch (errorHttp.code) {
                        case kCFURLErrorUserCancelledAuthentication:
                            errorCode = 401;
                            break;
                        case kCFURLErrorBadServerResponse:
                            errorCode = 406;
                            break;
                            
                        default:
                            break;
                    }
                    
                    if(errorCode == -1) {
                        errorCode = [response statusCode];
                    }
                    
                    NSDictionary * dataToSendToWeb = @{ @"type" : @"plugin", @"name" : @"webservices", @"action" : @"onWSError", @"data" : @{
                                                                @"callId" : callId,
                                                                @"text" : responseString,
                                                                kStatusCode : @(errorCode)
                                                                }};
                    
                    if([viewController conformsToProtocol:@protocol(WebServicesStorageDelegate)] && [viewController respondsToSelector: @selector(handleErrorWithData:withRequest:)])
                    {
                        id<WebServicesStorageDelegate> p = (id<WebServicesStorageDelegate>)viewController;
                        [p handleErrorWithData: dataToSendToWeb withRequest: dataToCreateRequest];
                    }
                    else {
                        [viewController sendMessage: dataToSendToWeb];
                    }
                } waitUntilDone:NO];
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


//////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark WEBSERVICESSTORAGEDELEGATE
//////////////////////////////////////////////////////////////////////////////////////////////////

- (id) storedValueForKey: (NSString *) key {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    return [defaults objectForKey: key];
}

- (void)storeValue: (id)value forKey: (NSString *)key {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setObject: value forKey: key];
    [defaults synchronize];
}

@end

