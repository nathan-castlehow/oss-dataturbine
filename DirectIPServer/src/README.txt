

-- Copyright 2010 Erigo Technologies LLC
-- 
-- Licensed under the Apache License, Version 2.0 (the "License"); you may not
-- use this file except in compliance with the License. You may obtain a copy
-- of the License at
-- 
-- http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
-- WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
-- License for the specific language governing permissions and limitations
-- under the License.


INTRODUCTION
------------

This Java application implements a DirectIP server for receiving SBD packets
from the Iridium gateway.  This server receives and parses each packet and
puts the data into individual channels in a DataTurbine server.

When service is established for an Iridium SBD modem, the subscriber can
select up to 5 parallel destinations for each SBD packet.  These destinations
can include a mix of email addresses, other IMEI units, and DirectIP servers.

In writing this program, information on the format of the SBD packet was
taken from a publication from NAL Research, "Additional Information on
DirectIP SBD, Technical Note", available at their FTP site:

ftp://ftp.nalresearch.com/Satellite%20Products/Standard%20Modems/9601-D/9601-D%20SW%20Version%203.2.0/Manuals

DataTurbine is an open source networked middleware server which has
been used used in various applications, from collecting climate data to
studying Titanium welding.  More information is available from:

http://dataturbine.org/
http://code.google.com/p/dataturbine/


BUILD THE DirectIP SERVER
-------------------------

The following packages must be installed on your machine prior to
compiling the DirectIP server:

1. Java JDK (http://java.sun.com/)
2. Apache Ant (http://ant.apache.org/)
3. DataTurbine distribution (http://code.google.com/p/dataturbine/)

To build the project:

1. Edit the definition of "RBNBBIN" near the top of src/build.xml to
   point to the bin folder in your DataTurbine installation.

2. Open a console window in the "src" directory and run "ant".

A "dist" directory is created which has the output files.


RUN THE DirectIP SERVER
-----------------------

Here's how to execute the DirectIP server (this example uses Linux syntax in the classpath definition):

java -cp <path to rbnb.jar>:directipserver.jar com.erigo.directip.DirectIPServer <server port> <RBNB host:port>

NOTES:

1. "rbnb.jar" is found in the "bin" directory of your DataTurbine installation.

2. <server port> is the port the SBD packets will be sent to by the Iridium
   gateway; if none is specified, 1537 is used by default.

3. <RBNB host:port> is the host and port of the DataTurbine server; if none
   is specified, the application will attempt to send the output data to the
   DataTurbine server at localhost:3333.
