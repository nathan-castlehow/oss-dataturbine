
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
 * MOHeaderIE
 * 
 * This class stores data from a Mobile Originated Header Information Element.
 * 
 * Modification History
 * Date		Programmer	Action
 * -----------------------------------
 * 02/15/2010	JPW		Created
 *
 */

package com.erigo.directip;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class MOHeaderIE extends InformationElement {
	
	public long cdrReference;
	public String imei;
	public byte sessionStatus;
	public String sessionStatusStr = "";
	public int momsn;
	public int mtmsn;
	public long time;
	public String timeStr = "";
	
	/*************************************************************************
	 * 
	 * Constructor
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 02/15/2010	JPW		Created
	 *
	 */
	
	public MOHeaderIE(byte IDI, ByteArrayInputStream baisI) throws Exception {
		
		super(IDI, baisI);
		
		if (content.length != 28) {
			throw new Exception(new String("Invalid length for header packet: expected 28, got " + content.length));
		}
		
		// CDR Reference (Auto ID)
		byte[] cdrReferenceB = new byte[8];
		cdrReferenceB[0] = 0;
		cdrReferenceB[1] = 0;
		cdrReferenceB[2] = 0;
		cdrReferenceB[3] = 0;
		cdrReferenceB[4] = content[0];
		cdrReferenceB[5] = content[1];
		cdrReferenceB[6] = content[2];
		cdrReferenceB[7] = content[3];
		ByteBuffer cdrReferenceBB = ByteBuffer.wrap(cdrReferenceB);
		cdrReference = cdrReferenceBB.getLong();
		
		// IMEI
		imei = new String(content,4,15);
		
		// Session Status
		sessionStatus = content[19];
		switch (sessionStatus) {
			case 0:
				sessionStatusStr = "00 - Transfer OK";
				break;
			case 1:
				sessionStatusStr = "01 - Transfer OK MT Message Too Large";
				break;
			case 2:
				sessionStatusStr = "02 - Transfer OK Bad Location";
				break;
			case 10:
				sessionStatusStr = "10 - Timeout";
				break;
			case 12:
				sessionStatusStr = "12 - MO Message Too Large";
				break;
			case 13:
				sessionStatusStr = "13 - RF link loss";
				break;
			case 14:
				sessionStatusStr = "14 - IMEI Protocol Anomaly";
				break;
			case 15:
				sessionStatusStr = "15 - IMEI Prohibited Access";
				break;
			default:
				sessionStatusStr = new String("Unrecognized session status (" + sessionStatus + ")");
				break;
		}
		
		// MOMSN
		byte[] momsnB = new byte[4];
		momsnB[0] = 0;
		momsnB[1] = 0;
		momsnB[2] = content[20];
		momsnB[3] = content[21];
		ByteBuffer momsnBB = ByteBuffer.wrap(momsnB);
		momsn = momsnBB.getInt();
		
		// MTMSN
		byte[] mtmsnB = new byte[4];
		mtmsnB[0] = 0;
		mtmsnB[1] = 0;
		mtmsnB[2] = content[22];
		mtmsnB[3] = content[23];
		ByteBuffer mtmsnBB = ByteBuffer.wrap(mtmsnB);
		mtmsn = mtmsnBB.getInt();
		
		// Time of session (UTC epoch time in seconds)
		byte[] timeB = new byte[8];
		timeB[0] = 0;
		timeB[1] = 0;
		timeB[2] = 0;
		timeB[3] = 0;
		timeB[4] = content[24];
		timeB[5] = content[25];
		timeB[6] = content[26];
		timeB[7] = content[27];
		ByteBuffer timeBB = ByteBuffer.wrap(timeB);
		time = timeBB.getLong();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date(time*1000);
		timeStr = sdf.format(date);
		
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
	public void sendDataToRBNB(Source srcI, String folderNameI, double timestampI) throws SAPIException{
		
		// For now, put all data into RBNB as string, so it is
		// easily visible from browser
		
		ChannelMap cm = new ChannelMap();
		cm.PutTime(timestampI, 0);
		
		/*
		int idx = cm.Add("cdr_reference");
		long[] cdrData = new long[1];
		cdrData[0] = cdrReference;
		cm.PutDataAsInt64(idx, cdrData);
		idx = cm.Add("IMEI");
		cm.PutDataAsString(idx, imei);
		// Don't bother putting the session status byte
		// into the RBNB; we can look at the session
		// status string if desired.
		// idx = cm.Add("session_status");
		// byte[] sessionByte = new byte[1];
		// sessionByte[0] = sessionStatus;
		// cm.PutDataAsByteArray(idx, sessionByte);
		idx = cm.Add("session_status_str");
		cm.PutDataAsString(idx, sessionStatusStr);
		idx = cm.Add("MOMSN");
		int[] momsnInt = new int[1];
		momsnInt[0] = momsn;
		cm.PutDataAsInt32(idx, momsnInt);
		idx = cm.Add("MTMSN");
		int[] mtmsnInt = new int[1];
		mtmsnInt[0] = mtmsn;
		cm.PutDataAsInt32(idx, mtmsnInt);
		idx = cm.Add("time_str");
		cm.PutDataAsString(idx, timeStr);
		*/
		
		int idx = cm.Add(folderNameI + "/cdr_reference");
		cm.PutDataAsString(idx, new String(Long.toString(cdrReference) + "\n"));
		idx = cm.Add(folderNameI + "/IMEI");
		cm.PutDataAsString(idx, new String(imei + "\n"));
		// Don't bother putting the session status byte
		// into the RBNB; we can look at the session
		// status string if desired.
		idx = cm.Add(folderNameI + "/session_status");
		cm.PutDataAsString(idx, new String(sessionStatusStr + "\n"));
		idx = cm.Add(folderNameI + "/MOMSN");
		cm.PutDataAsString(idx, new String(Integer.toString(momsn) + "\n"));
		idx = cm.Add(folderNameI + "/MTMSN");
		cm.PutDataAsString(idx, new String(Integer.toString(mtmsn) + "\n"));
		idx = cm.Add(folderNameI + "/time_of_session");
		cm.PutDataAsString(idx, new String(timeStr + "\n"));
		
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
	 * 04/09/2010	JPW		Created
	 *
	 */
	public String toString() {
		return
			new String(
				super.toString("\t", "Mobile Originated Header Information Element") + "\n\n" +
				"\t\tDecoded MO Header IE content:\n" +
				"\t\tCDR Reference: " + cdrReference + "\n" +
				"\t\tIMEI: " + imei + "\n" +
				"\t\tSession Status: " + sessionStatusStr + "\n" +
				"\t\tMOMSN: " + momsn + "\n" +
				"\t\tMTMSN: " + mtmsn + "\n" +
				"\t\tTime of session: " + timeStr + "\n\n");
	}
	
}
