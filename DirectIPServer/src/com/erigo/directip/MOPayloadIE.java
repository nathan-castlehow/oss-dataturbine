
/*****************************************************************************
 * 
 * Copyright 2010 Erigo Technologies LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * MOPayloadIE
 * 
 * This class stores data from a Mobile Originated Payload Information Element.
 * 
 * Modification History
 * Date		Programmer	Action
 * -----------------------------------
 * 04/07/2010	JPW		Created
 *
 */

package com.erigo.directip;

import java.io.ByteArrayInputStream;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class MOPayloadIE extends InformationElement {
	
	public String payloadStr = "";
	
	/*************************************************************************
	 * 
	 * Constructor
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/07/2010	JPW		Created
	 *
	 */
	public MOPayloadIE(byte IDI, ByteArrayInputStream baisI) throws Exception {
		super(IDI, baisI);
		payloadStr = new String(content);
	}
	
	/*************************************************************************
	 * 
	 * Send data to the RBNB from this information element.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/09/2010	JPW		Created
	 * 10/11/2010	JPW		Add folderNameI argument.
	 *
	 */
	public void sendDataToRBNB(Source srcI, String folderNameI, double timestampI) throws SAPIException {
		
		ChannelMap cm = new ChannelMap();
		cm.PutTime(timestampI, 0);
		cm.Add(folderNameI + "/payload");
		cm.PutDataAsString(0, new String(payloadStr + "\n"));
		srcI.Flush(cm);
		
	}
	
	/*************************************************************************
	 * 
	 * Print information from this information element.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/07/2010	JPW		Created
	 *
	 */
	public String toString() {
		return
			new String(
				super.toString("\t", "Mobile Originated Payload Information Element") + "\n\n" +
				"\t\tDecoded MO Payload IE content:\n" +
				"\t\t" + payloadStr + "\n\n");
	}
}
