/*

File: GraphView.m
Abstract: This class is responsible for updating and drawing the accelerometer
history of values. The history is a circular buffer implementation, with a
pointer moving repeatedly through the buffer, resetting to zero each time it
reaches the end.

Version: 1.7

Copyright (C) 2008 Apple Inc. All Rights Reserved.

*/

#import "GraphView.h"

// Constant for maximum acceleration.
#define kMaxAcceleration 3.0
// Constant for the high-pass filter.
#define kFilteringFactor 0.1

// Proxy port on iguassu
const int DEST_PORT = 3000;
// iguassu.sdsc.edu
//const char DEST_IP[] = "137.110.118.250";

// Paul, laptop, at UCSD
//const char DEST_IP[] = " 137.110.115.182";
const char DEST_IP[] = "127.0.0.1";

// GraphView class implementation.
@implementation GraphView

// Instruct the compiler to generate accessors for the property, and use the internal variable _filter for storage.
@synthesize filteringIsEnabled;
@synthesize updatingIsEnabled;
@synthesize streamingIsEnabled;

- (void)updateHistoryWithX:(float)x Y:(float)y Z:(float)z {
    // If filtering is active, apply a basic high-pass filter to remove the gravity influence from the accelerometer values
    if (filteringIsEnabled) {
        acceleration[0] = x * kFilteringFactor + acceleration[0] * (1.0 - kFilteringFactor);
        history[nextIndex][0] = x - acceleration[0];
        acceleration[1] = y * kFilteringFactor + acceleration[1] * (1.0 - kFilteringFactor);
        history[nextIndex][1] = y - acceleration[1];
        acceleration[2] = z * kFilteringFactor + acceleration[2] * (1.0 - kFilteringFactor);
        history[nextIndex][2] = z - acceleration[2];
    } else {
        history[nextIndex][0] = x;
        history[nextIndex][1] = y;
        history[nextIndex][2] = z;
    }
    // Advance buffer pointer to next position or reset to zero.
    nextIndex = (nextIndex + 1) % kHistorySize;
    
    if (updatingIsEnabled) {
        [self setNeedsDisplay];
    }
}

// Do-very-little function to create the UDP socket and address
- (void) createSocket
{
    udpSocket = socket(AF_INET, SOCK_DGRAM, 0);
    
    memset(&udpAddress, 0x00, sizeof(udpAddress));
    
    udpAddress.sin_family = AF_INET;
    udpAddress.sin_port = htons(DEST_PORT);
    
    inet_pton(AF_INET, DEST_IP, &udpAddress.sin_addr.s_addr);
}

// Stream data to RBNB, via UDP, expecting that Proxy is running on iguassu.sdsc.edu:3333
- (void) streamData:(float) x Y:(float) y Z:(float) z
{
    uint16_t msgBuf[3];    
    // If no socket, just bail
    if(udpSocket < 0)
        return;
    
    if(streamingIsEnabled == FALSE)
        return;
    
    // Convert to i16 and place into send buffer
    msgBuf[0] = x * 100;
    msgBuf[1] = y * 100;
    msgBuf[2] = z * 100;
    
    // Blast it off!
    sendto(udpSocket, (const void *) msgBuf, sizeof(msgBuf), 0, 
           (struct sockaddr *) &udpAddress, sizeof(udpAddress));    
}

- (void)setFilteringEnabled:(BOOL)enabled {
    filteringIsEnabled = enabled;
    // Reset the acceleration filter.
    acceleration[0] = acceleration[1] = acceleration[2] = 0.0;
}

- (void)drawHistory:(unsigned)axis fromIndex:(unsigned)index inContext:(CGContextRef)context bounds:(CGRect)bounds {
    UIFont *font = [UIFont systemFontOfSize:12];
    unsigned i;
    float value, temp;
    
    // Draw the background
    CGContextSetGrayFillColor(context, 0.6, 1.0);
    CGContextFillRect(context, bounds);
    
    // Draw the intermediate lines
    CGContextSetGrayStrokeColor(context, 0.5, 1.0);
    CGContextBeginPath(context);
    for (value = -kMaxAcceleration + 1.0; value <= kMaxAcceleration - 1.0; value += 1.0) {
    
        if (value == 0.0) {
            continue;
        }
        temp = roundf(bounds.origin.y + bounds.size.height / 2 + value / (2 * kMaxAcceleration) * bounds.size.height);
        CGContextMoveToPoint(context, bounds.origin.x, temp);
        CGContextAddLineToPoint(context, bounds.origin.x + bounds.size.width, temp);
    }
    CGContextStrokePath(context);
    
    // Draw the center line
    CGContextSetGrayStrokeColor(context, 1.0, 1.0);
    CGContextBeginPath(context);
    temp = roundf(bounds.origin.y + bounds.size.height / 2);
    CGContextMoveToPoint(context, bounds.origin.x, temp);
    CGContextAddLineToPoint(context, bounds.origin.x + bounds.size.width, temp);
    CGContextStrokePath(context);
    
    // Draw the top & bottom lines
    CGContextSetGrayStrokeColor(context, 0.25, 1.0);
    CGContextBeginPath(context);
    CGContextMoveToPoint(context, bounds.origin.x, bounds.origin.y);
    CGContextAddLineToPoint(context, bounds.origin.x + bounds.size.width, bounds.origin.y);
    CGContextMoveToPoint(context, bounds.origin.x, bounds.origin.y + bounds.size.height);
    CGContextAddLineToPoint(context, bounds.origin.x + bounds.size.width, bounds.origin.y + bounds.size.height);
    CGContextStrokePath(context);
    
    // Draw the history lines
    CGContextSetRGBStrokeColor(context, (axis == 0 ? 1.0 : 0.0), (axis == 1 ? 1.0 : 0.0), (axis == 2 ? 1.0 : 0.0), 1.0);
    CGContextBeginPath(context);
    for (i = 0; i < kHistorySize; ++i) {
        // NOTE: We need to draw upside-down as UIView referential has the Y axis going down
        value = history[(index + i) % kHistorySize][axis] / -kMaxAcceleration; 
        if (i > 0) {
            CGContextAddLineToPoint(context, bounds.origin.x + (float)i / (float)(kHistorySize - 1) * bounds.size.width, 
                                    bounds.origin.y + bounds.size.height / 2 + value * bounds.size.height / 2);
        } else {
            CGContextMoveToPoint(context, bounds.origin.x + (float)i / (float)(kHistorySize - 1) * bounds.size.width, 
                                 bounds.origin.y + bounds.size.height / 2 + value * bounds.size.height / 2);
        }
    }
	CGContextSetLineWidth(context, 2.0);
    CGContextStrokePath(context);
	CGContextSetLineWidth(context, 1.0);
    
    // Draw the labels
    CGContextSetGrayFillColor(context, 1.0, 1.0);
    CGContextSetAllowsAntialiasing(context, true);
    for (value = -kMaxAcceleration; value <= kMaxAcceleration - 1.0; value += 1.0) {
        temp = roundf(bounds.origin.y + bounds.size.height / 2 + value / (2 * kMaxAcceleration) * bounds.size.height);
        // NOTE: We need to draw upside-down as UIView referential has the Y axis going down
        [[NSString stringWithFormat:@"%+.1f", -(value >= 0.0 ? value + 1.0 : value)] 
         drawAtPoint:CGPointMake(bounds.origin.x + 4, temp + (value >= 0.0 ? 3 : 0)) withFont:font]; 
    }
    temp = roundf(bounds.origin.y + bounds.size.height / 2);
    CGPoint sPoint = CGPointMake(bounds.origin.x + bounds.size.width - 40, temp - 16);
    [[NSString stringWithFormat:@"%c Axis", 'X' + axis] drawAtPoint:sPoint withFont:font];
    CGContextSetAllowsAntialiasing(context, false);
}

- (void)drawRect:(CGRect)clip {
    CGSize size = [self bounds].size;
    CGContextRef context = UIGraphicsGetCurrentContext();
    unsigned i;
    unsigned index = nextIndex;
    
    // Draw the X, Y & Z graphs with anti-aliasing turned off
    CGContextSetAllowsAntialiasing(context, false);
    CGFloat hOver3 = size.height / 3, hOver4 = size.height / 4;
    for (i = 0; i < 3; ++i) {
        CGRect hBounds = CGRectMake(0, (hOver3 - hOver4) / 2 + (float)i * hOver3, size.width, hOver4);
        [self drawHistory:i fromIndex:index inContext:context bounds:hBounds];
    }
    CGContextSetAllowsAntialiasing(context, true);
}

@end
