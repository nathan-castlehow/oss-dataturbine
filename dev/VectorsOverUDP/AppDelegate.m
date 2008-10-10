/*

File: AppDelegate.m
Abstract: UIApplication's delegate class, the central controller of the
application. This object creates the user interface, configures the internal
accelerometer, and handles action messages from the buttons in the UI.

Version: 1.7

Copyright (C) 2008 Apple Inc. All Rights Reserved.
*/

#import "AppDelegate.h"

// Constant for the number of times per second (Hertz) to sample acceleration.
#define kAccelerometerFrequency     40

// AppDelegate class implementation.

@implementation AppDelegate

@synthesize window;
@synthesize graphView;
@synthesize toolbar;


- (void)applicationDidFinishLaunching:(UIApplication*)application {
    // Calculate the frames for the toolbar and the main view.
    CGRect toolbarRect = CGRectMake(0, 431, 320, 49);
    UIToolbar *aToolbar = [[UIToolbar alloc] initWithFrame:toolbarRect];
    self.toolbar = aToolbar;
    [aToolbar release];
    [window addSubview:toolbar];

    // Create each toolbar item...
    // The pause item is a button for pausing updates to the view.
    UIBarButtonItem *pauseItem = [[UIBarButtonItem alloc] initWithTitle:@"Pause" style:UIBarButtonItemStyleBordered target:self action:@selector(toggleRun:)];
    pauseItem.width = 80.0;
    // The high pass item toggles between enabled and disabled for filtering in the view.
    UIBarButtonItem *highPassItem = [[UIBarButtonItem alloc] initWithTitle:@"Enable Filter" style:UIBarButtonItemStyleBordered target:self action:@selector(toggleFiltering:)];
    highPassItem.width = 100.0;
    // pfh
    UIBarButtonItem *streamItem = [[UIBarButtonItem alloc] initWithTitle:@"Stream" style:UIBarButtonItemStyleBordered target:self action:@selector(toggleStreaming:)];
    streamItem.width = 90.0;
                                
    // Collect the items in a temporary array.
    NSArray *items = [NSArray arrayWithObjects:pauseItem, highPassItem, streamItem, nil];
    [pauseItem release];
    [highPassItem release];
    [streamItem release];
    
    // Pass the items to the toolbar.
    [toolbar setItems:items];
    
    // Show the window
    [window makeKeyAndVisible];
    
    // set the graphView's initial state
    graphView.updatingIsEnabled = YES;
    graphView.filteringIsEnabled = NO;
    graphView.streamingIsEnabled = NO;
    
    // Configure and start the accelerometer
    [[UIAccelerometer sharedAccelerometer] setUpdateInterval:(1.0 / kAccelerometerFrequency)];
    [[UIAccelerometer sharedAccelerometer] setDelegate:self];
    
    // Create the UDP socket
    [graphView createSocket];
}

// Release resources.
- (void)dealloc {
    [toolbar release];
    [graphView release];
    [window release];
    
    [super dealloc];
}

// UIAccelerometerDelegate method, called when the device accelerates.
- (void)accelerometer:(UIAccelerometer *)accelerometer didAccelerate:(UIAcceleration *)acceleration {
    // Update the accelerometer graph view
    [graphView updateHistoryWithX:acceleration.x Y:acceleration.y Z:acceleration.z];

    [graphView streamData:acceleration.x Y:acceleration.y Z:acceleration.z];
}

// Invoked when the user touches the toolbar button for Pause/Resume.
- (void)toggleRun:(id)sender {
    // Update toolbar button
    NSString *title = (graphView.updatingIsEnabled) ? @"Resume" : @"Pause";
    [[[toolbar items] objectAtIndex:0] setTitle:title];
    
    // Toggle update state
    graphView.updatingIsEnabled = !graphView.updatingIsEnabled;
}

// Invoked when the user touches the toolbar button for High Pass Filtering.
- (void)toggleFiltering:(id)sender {
    // Enable or disable filtering
    graphView.filteringIsEnabled = !graphView.filteringIsEnabled;
    
    // Update toolbar
    NSString *title = (graphView.filteringIsEnabled) ? @"Disable Filter" : @"Enable Filter";
    [[[toolbar items] objectAtIndex:1] setTitle:title];
}

// Invoked when user hits 'Stream'
- (void)toggleStreaming:(id)sender {
    graphView.streamingIsEnabled = !graphView.streamingIsEnabled;
    
    NSString *title = (graphView.streamingIsEnabled) ? @"Stop streaming" : @"Stream";
    [[[toolbar items] objectAtIndex:2] setTitle:title];
}

@end
