package org.nees.rbnb;
/**
* AxisSourceTTask.java
* @see org.nees.rbnb.AxisSource
* @since 051108  
* @author Lawrence J. Miller <ljmiller@sdsc.edu>
* @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
* Please see copywrite information at the end of this file.
*
* Perforce RCS info:
* $Id: AxisSourceTTask.java 153 2007-09-24 20:10:37Z ljmiller $
* $Header: $
* $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
* $Change: $
* $File: $
* $Revision: 153 $
* $Author: ljmiller $
*/

import java.util.TimerTask;
import java.util.Date;
import com.rbnb.sapi.*;
import org.nees.rbnb.AxisSource;

public class AxisSourceTTask extends TimerTask {

   private AxisSource myCaller = null;
   
   /** constructor */
   public AxisSourceTTask (AxisSource maker) {
      super ();
      this.myCaller = maker;
   }
   
   /** The main method of this thread. */
   public void run () {
      
   }
   
}

/** Copyright (c) 2005, Lawrence J. Miller and NEESit
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*    * Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*    * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the 
* documentation and/or other materials provided with the distribution.
*   * Neither the name of the San Diego Supercomputer Center nor the names of
* its contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/