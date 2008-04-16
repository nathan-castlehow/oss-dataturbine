/*!
@file DaqListener.java
@date Feb 11, 2005
@author Terry Weymouth
@author Lawrence J. Miller
@brief An interface for classes that will be listening for DAQ data.
@not modified to handle the nwp as a daq
@version CVS:$Revision$
@brief The interface for classes that want to receive DAQ data.
The assumptions for this API are that it will receive data from a number of
channels. The channels that are expected are first registered with the method
registerChannel, and data is poested to each channel with the method postData.
Data is assume to arrive on a set of related channels all with the same timestamp.
Effectively, what happens is this:

read list of channels
for each ch in channel list
{
	listener.registerChannel(ch)
}
while (daq.dataAvailble())
{
	line = daq.getLine()
	time = get timestamp from line
	listener.postTimeStamp(time)
	for each ch/data pair in line
	{
		listener.postToChannel(ch,data);
	}
	listener.endTick()
}
listener.abort();

This interface supplies templates for a "call back" methods for classes that
which to subscribe to DAQ data. It is used by a private inner class in
DaqToRbnb. 

@Note implementers of this method must assure that they are responding
fast enough so as not to block the DAQ data handeling.
*/
package edu.ucsd.osdt.daq;

import com.rbnb.sapi.SAPIException;

public interface DaqListener {
	
	/*!
		@brief Method template: the "call back" to establish a channel from DAQ.
        
        The implementor of this inferface should do what is needed to ready the
       	applicaiton to recenve data from the DAQ on this channel
        
		@throws SAPIException
        
		@param channel Channel Path (in RBNB parlance).
        
		@see DaqToRbnb
	 */
	public void registerChannel(String channelName)
		throws SAPIException;

	/*!
		@brief Method template: the "call back" to establish a channel from DAQ.
        
        This interface is called each time there is a new timestamp value. The
        timestamp values are garenteed to be monitonicilly increasing. The
        implementor of this inferface should do what is needed to update
        the application with respect to new data at this timestaml
        
		@throws SAPIException
        
		@param time Timestamp, seconds since the epoc, GMT

		@see DaqToRbnb
	 */
	public void postTimestamp(double time)
		throws SAPIException;
    /*!
        @brief Method template: the "call back" for the arival of DAQ data.

        This interface is called each time there is new data on the given channel.
        The data is assumed to be be posted at the time given by the last call to
        postTimestamp. Data will be clustered in a "tick" (as in "tick of the clock").
        that is a timestamp followed by several data.
        
        @throws SAPIException
        
        @param channel Channel Path (in RBNB parlance).
        @param data The datum
        
        @see DaqToRbnb
     */
	public void postData(String channel, double data)
		throws SAPIException;
	
	/*! @brief callback for posting strings */
	public void postString(String channel, String stringData) throws SAPIException;
		
	/*!
	 * This method is called each time there is a new event sent from the DAQ.
	 * 
	 * @param type the type of event
	 * @param content the content of the event
	 */
	public void postEvent(String type, String content)
		throws SAPIException;
	
	/*!
		@brief Method template: the "call back" for the arival of DAQ data.

		This interface is called at the end of a cluster of data to denote that 
		all the data for that timestamp has been sent to the application. This gives
		the application a chance to do any batching of processing of data that is
		desired.  The data is assumed to be be posted at the time given by the last
		call to postTimestamp. Data will be clustered in a "tick" (as in "tick
		of the clock"), that is a timestamp followed by several data.
        
		@throws SAPIException
        
		@param channel Channel Path (in RBNB parlance).
		@param data The datum
        
		@see DaqToRbnb
	 */
	public void endTick()
		throws SAPIException;
}
