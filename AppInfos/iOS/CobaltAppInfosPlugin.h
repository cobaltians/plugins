//
//  CobaltAppInfosPlugin.h
//  Cobalt
//
//  Created by Kristal on 06/01/15.
//  Copyright (c) 2015 Kristal. All rights reserved.
//

#import "CobaltAbstractPlugin.h"
#import "WebServicesAPI.h"

#define ACTION_GET_APP_INFOS   @"getAppInfos"

#define VERSION_CODE    @"versionCode"
#define BUILD           @"CFBundleVersion"

#define VERSION_NAME    @"versionName"
#define VERSION         @"CFBundleShortVersionString"

@interface CobaltAppInfosPlugin: CobaltAbstractPlugin

@end
