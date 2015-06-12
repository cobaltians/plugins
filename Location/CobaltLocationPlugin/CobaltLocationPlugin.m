//
//  CobaltLocationPlugin.m
//  Cobalt
//
//  Created by Haploid on 23/07/14.
//  Copyright (c) 2014 Haploid. All rights reserved.
//

#import "CobaltLocationPlugin.h"

@implementation CobaltLocationPlugin

- (id)init
{
	if (self = [super init])
    {
        _locationManager = [[CLLocationManager alloc] init];
        _locationManager.distanceFilter = kCLDistanceFilterNone; // whenever we move
        _locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters; // 100 m
        _locationManager.delegate = self;
        
        if([_locationManager respondsToSelector: @selector(requestWhenInUseAuthorization)])
        {
            [_locationManager requestWhenInUseAuthorization];
        }
        else
        {
            [_locationManager startUpdatingLocation];
        }
    }
    
	return self;
}

- (void)onMessageFromCobaltController:(CobaltViewController *)viewController andData: (NSDictionary *)data
{
    _viewController = viewController;
    
    _sendToWeb = YES;
    
    if([CLLocationManager authorizationStatus] == kCLAuthorizationStatusDenied)
    {
        [self sendErrorToWeb];
    }
    else if(_locationManager.location && ([CLLocationManager authorizationStatus] != kCLAuthorizationStatusNotDetermined))
    {
        [self sendLocationToWeb: _locationManager.location];
    }
}

-(void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    [self sendLocationToWeb: newLocation];
}

-(void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    [self sendErrorToWeb];
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
{
    if(status == kCLAuthorizationStatusAuthorized)
    {
        if(_locationManager.location)
        {
            _sendToWeb = YES;
            [self sendLocationToWeb: _locationManager.location];
        }
    }
    
    if([_locationManager respondsToSelector: @selector(requestWhenInUseAuthorization)])
    {
        [_locationManager startUpdatingLocation];
    }
}

- (void)sendLocationToWeb: (CLLocation *) location
{
    if(!_sendToWeb)
        return;
    
    _sendToWeb = NO;
    
    NSDictionary * data = nil;
    
    if(location)
        data = @{ kJSType : kJSTypePlugin, kJSPluginName : @"location", kJSData : @{@"error": @NO, kJSValue: @{LONGITUDE : [NSNumber numberWithDouble: location.coordinate.longitude], LATITUDE: [NSNumber numberWithDouble: location.coordinate.latitude]}}};
    [_viewController sendMessage: data];
    //[_locationManager stopUpdatingLocation];
}

- (void)sendErrorToWeb
{
    if(!_sendToWeb)
        return;
    
    _sendToWeb = NO;
    
    if([CLLocationManager authorizationStatus] == kCLAuthorizationStatusDenied)
    {
        NSDictionary * data = @{ kJSType : kJSTypePlugin, kJSPluginName : @"location", kJSData : @{@"error": @YES, @"code": @"DISABLED", @"text" : @"Location detection has been disabled by user"}};
        [_viewController sendMessage: data];
    }
    else
    {
        NSDictionary * data = @{ kJSType : kJSTypePlugin, kJSPluginName : @"location", kJSData : @{@"error": @YES, @"code": @"NULL", @"text" : @"No location found"}};
        [_viewController sendMessage: data];
    }
}

@end
