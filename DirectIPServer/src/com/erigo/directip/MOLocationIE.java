
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
 * MOLocationIE
 * 
 * This class stores data from a Mobile Originated Location Information Element.
 * 
 * Modification History
 * Date		Programmer	Action
 * -----------------------------------
 * 04/07/2010	JPW		Created
 *
 */

package com.erigo.directip;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class MOLocationIE extends InformationElement {
	
	public double latitude = 0.0;
	public double longitude = 0.0;
	public int cepRadius = 0;
	
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
	public MOLocationIE(byte IDI, ByteArrayInputStream baisI) throws Exception {
		
		super(IDI,baisI);
		
		if (content.length != 11) {
			throw new Exception(new String("Invalid length for location packet: expected 11, got " + content.length));
		}
		
		//
		// Latitude and Longitude - parse from the first 7 bytes
		//
		// Byte 1 contains info on north/south and east/west location
		byte locationCodeByte = content[0];
		int iNorthLat = (locationCodeByte >> 1) & 0x0001;
		boolean bNorthLat = false;
		if (iNorthLat == 0) {
			bNorthLat = true;
		}
		int iEastLon = locationCodeByte & 0x0001;
		boolean bEastLon = false;
		if (iEastLon == 0) {
			bEastLon = true;
		}
		// Byte 2 contains whole degrees latitude
		double wholeDegLat = (double)content[1];
		// Bytes 3-4 contain thousands of a minute latitude
		byte[] minLatB = new byte[4];
		minLatB[0] = 0;
		minLatB[1] = 0;
		minLatB[2] = content[2];
		minLatB[3] = content[3];
		ByteBuffer minLatBB = ByteBuffer.wrap(minLatB);
		int minLatInt = minLatBB.getInt();
		// Convert from number of thousandths of a minute to degrees
		double minLatD = (double)minLatInt/60000.0;
		latitude = wholeDegLat + minLatD;
		if (!bNorthLat) {
			latitude = -1.0 * latitude;
		}
		// Byte 5 contains whole degrees longitude
		double wholeDegLon = (double)content[4];
		// Bytes 6-7 contain thousands of a minute longitude
		byte[] minLonB = new byte[4];
		minLonB[0] = 0;
		minLonB[1] = 0;
		minLonB[2] = content[5];
		minLonB[3] = content[6];
		ByteBuffer minLonBB = ByteBuffer.wrap(minLonB);
		int minLonInt = minLonBB.getInt();
		// Convert from number of thousandths of a minute to degrees
		double minLonD = (double)minLonInt/60000.0;
		longitude = wholeDegLon + minLonD;
		if (!bEastLon) {
			longitude = -1.0 * longitude;
		}
	    
		// CEP Radius
		byte[] cepRadiusB = new byte[8];
		cepRadiusB[0] = 0;
		cepRadiusB[1] = 0;
		cepRadiusB[2] = 0;
		cepRadiusB[3] = 0;
		cepRadiusB[4] = content[7];
		cepRadiusB[5] = content[8];
		cepRadiusB[6] = content[9];
		cepRadiusB[7] = content[10];
		ByteBuffer cepRadiusBB = ByteBuffer.wrap(cepRadiusB);
		cepRadius = (int)cepRadiusBB.getLong();
		
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
		int idx = cm.Add("lat");
		double[] latData = new double[1];
		latData[0] = latitude;
		cm.PutDataAsFloat64(idx, latData);
		idx = cm.Add("lon");
		double[] lonData = new double[1];
		lonData[0] = longitude;
		cm.PutDataAsFloat64(idx, lonData);
		idx = cm.Add("cep_radius");
		int[] cepData = new int[1];
		cepData[0] = cepRadius;
		cm.PutDataAsInt32(idx, cepData);
		*/
		
		int idx = cm.Add(folderNameI + "/lat");
		cm.PutDataAsString(idx, new String(Double.toString(latitude) + "\n"));
		idx = cm.Add(folderNameI + "/lon");
		cm.PutDataAsString(idx, new String(Double.toString(longitude) + "\n"));
		idx = cm.Add(folderNameI + "/cep_radius");
		cm.PutDataAsString(idx, new String(Integer.toString(cepRadius) + "\n"));
		
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
				super.toString("\t", "Mobile Originated Location Information Element") + "\n\n" +
				"\t\tDecoded MO Location IE content:\n" +
				"\t\tLatitude: " + latitude + "\n" +
				"\t\tLongitude: " + longitude + "\n" +
				"\t\tCEP Radius: " + cepRadius + "\n\n");
	}
}
