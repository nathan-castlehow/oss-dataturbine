/*

File: AppDelegate.h
Abstract: UIApplication's delegate class, the central controller of the
application. This object creates the user interface, configures the internal
accelerometer, and handles action messages from the buttons in the UI.

Version: 1.7

Copyright (C) 2008 Apple Inc. All Rights Reserved.

*/

#import "GraphView.h"

// AppDelegate class interface.

@interface AppDelegate : NSObject <UIAccelerometerDelegate>
{
    IBOutlet UIWindow *window;
    IBOutlet GraphView *graphView;
    IBOutlet UIToolbar *toolbar;
}

@property (nonatomic, retain) UIWindow *window;
@property (nonatomic, retain) GraphView *graphView;
@property (nonatomic, retain) UIToolbar *toolbar;

- (IBAction)toggleRun:(id)sender;
- (IBAction)toggleFiltering:(id)sender;


@end
