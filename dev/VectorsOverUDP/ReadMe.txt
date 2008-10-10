
pfh 10/9/08 Renamed to VectorsOverUDP, this is starting code for streaming acceleromter readings to DataTurbine over UDP.
Currently, it is hardwired to sent binary UDP packets to a fixed address. The next steps are

 1) Change from binary/IEEE format to Protocol Buffers (PB)
 2) Add screens to set destination IP/Port and sampling rate
 3) Add device ID to PB as source name
 4) Geotag data
 5) Add image capture
 
 Paul Hubbard hubbard@sdsc.edu
 
### AccelerometerGraph ###

===========================================================================
DESCRIPTION:

AccelerometerGraph sample application graphs the motion of the device along each of three axes. It demonstrates how to use the UIAccelerometer class as well as how to render Quartz 2D graphics in a UIView that continually updates. It also demonstrates a technique to "remove" the gravity influence from the accelerometer values using a basic high-pass filter.  

Run this sample on the device to learn how the accelerometer behaves when moving the device. (Using the simulator is not recommended because it doesn't emulate the accelerometer; you'll see only flat lines.) Use the toolbar at the bottom to pause and resume updates to the graphical display or to apply a high-pass filter on the accelerometer values.

===========================================================================
BUILD REQUIREMENTS:

Mac OS X 10.5.3, Xcode 3.1, iPhone OS 2.0

===========================================================================
RUNTIME REQUIREMENTS:

Mac OS X 10.5.3, iPhone OS 2.0

===========================================================================
PACKAGING LIST:

AppDelegate.h
AppDelegate.m
UIApplication's delegate class, the central controller of the application. This object creates the user interface, configures the internal accelerometer, and handles action messages from the buttons in the UI.

GraphView.h
GraphView.m
This class is responsible for updating and drawing the accelerometer history of values. The history is a circular buffer implementation, with a pointer moving repeatedly through the buffer, resetting to zero each time it reaches the end.

main.m
Entry point for the application. Creates the application object, sets its delegate, and causes the event loop to start.

===========================================================================
CHANGES FROM PREVIOUS VERSIONS:

Version 1.7
- Updated for and tested with iPhone OS 2.0. First public release.
- Simplified updating of drawing by eliminating the NSTimer previously used to mark the view as needing to be redrawn. In this new version, the view is marked whenever new data arrives from the accelerometer.

Version 1.6
- Now use fixed-width buttons in UI.
- Modified update frequency to smooth animations. 

Version 1.5
- Removed underscore prefixes on ivars to match sample code guidelines.
- Updated for Beta 6. 

Version 1.4 
- Updated for Beta 5. 

Version 1.3 
- Updated build settings.
- Updated ReadMe file and converted it to plain text format for viewing on website. 

Version 1.2 
- Updated ReadMe file. 
- Added an icon and a default.png file.

===========================================================================
Copyright (C) 2008 Apple Inc. All rights reserved.