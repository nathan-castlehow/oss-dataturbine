/*

File: GraphView.h
Abstract: This class is responsible for updating and drawing the accelerometer
history of values. The history is a circular buffer implementation, with a
pointer moving repeatedly through the buffer, resetting to zero each time it
reaches the end.

Version: 1.7


Copyright (C) 2008 Apple Inc. All Rights Reserved.

*/

#import <UIKit/UIKit.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

// Constant for the number of acceleration samples kept in history.
#define kHistorySize 150

// GraphView class interface.

@interface GraphView : UIView 
{
    NSUInteger nextIndex;
    UIAccelerationValue acceleration[3];
    // Two dimensional array of acceleration data.
    UIAccelerationValue history[kHistorySize][3];
    BOOL filteringIsEnabled;
    BOOL updatingIsEnabled;
    BOOL streamingIsEnabled;
    int udpSocket;
    struct sockaddr_in udpAddress;
}

@property (setter=setFilteringEnabled:) BOOL filteringIsEnabled;
@property BOOL updatingIsEnabled;

@property BOOL streamingIsEnabled;


// Add a sample of acceleration data to the history.
- (void)updateHistoryWithX:(float)x Y:(float)y Z:(float)z;

// Create socket
- (void)createSocket;

// Stream data to DT via UDP
- (void) streamData:(float) x Y:(float) y Z:(float) z;


@end
