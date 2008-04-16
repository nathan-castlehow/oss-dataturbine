/*
 * Created on May 13, 2005
 */
package org.nees.rbnb;

/**
 * This is the "top level" of the server side of a control program that runs
 * server-side data capture and data transformation appplications. The list of
 * applications that are available is supplied by a file, the name of which is
 * given on the command line. Those applications all obey an API defined by the
 * interface, org.nees.rbnb.ServerSideApplication. This top level control
 * program understands how to talk to application that use this interface
 * for determining control and configuration information. The format of 
 * that file is a list of entries in the following xml format:
 * 	<server-side-application>
 * 		<name>Name</name>
 * 		<description>This is a description.</description>
 * 		<class>org.nees.rbnb.TheApplicationClass</class>
 * 	</server-side-application>
 * 
 * @author Terry E. Weymouth
 * @version $Revision$ (CVS Revision number)
 * @see org.nees.rbnb.ServerSideApplication
 * 
 */
public class NeesControl
{
	//TODO: Server Side control application
	public static void main(String[] args) {
		System.out.println("Under Development.");
	}		
} // NeesControl
