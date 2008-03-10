/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.api;

/**
 * Build file loader class.
 * <p>
 * This class contains a static method for loading the build file into a class
 * that implements the <code>BuildInterface</code>
 *
 * @author Ian Brown
 *
 * @see com.rbnb.api.BuildInterface
 * @since V2.0
 * @version 10/05/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 10/05/2004  JPW	In loadBuildFile(), add preprocessor directives which
 *			will be used by "sed" to create a version of the code
 *			appropriate for compiling under J#.
 * 08/04/2004  JPW      Change in loadBuildFile(): if search over classpath
 *                      doesn't yield anything, then just check in the local
 *                      directory for rbnbBuild.txt
 * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
 * 03/19/2003  INB	Added code to handle the case where we can't get the
 *			system class loader.
 * 12/20/2001  INB	Created.
 *
 */
public final class BuildFile {

    /**
     * Loads the build file.
     * <p>
     * The build file contains various information that specifies things like
     * when the class files were built.
     * <p>
     *
     * @author Ian Brown
     *
     * @param biI the class to store the loaded values in.
     * @since V2.0
     * @version 10/05/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/05/2004  JPW	Add preprocessor directives which will be used by
     *                  "sed" to create a version of the code appropriate for
     *                  compiling under J#.  Under J#, we don't want to include
     *			the call to getSystemClassLoader().
     * 08/04/2004  JPW  If search over classpath doesn't yield anything,
     *                  just check in the local directory for rbnbBuild.txt
     * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
     * 03/19/2003  INB	Added code to handle the case where we cannot get the
     *			system class loader.
     * 10/24/2001  INB	Created.
     *
     */
	public final static void loadBuildFile(BuildInterface biI) 
	{
	}
}
