package dtesp;



import dtesp.Config.*;

/**
 * <pre>
 *  Runtime information for source channel
 *   channel_index- channel index associated with source (initialized when connected)
 *   first_write-	if the channel hasn't written anything
 *   last_data-		last data written to channel(needed for bar graph)
 */		
class SourceChannelRuntime
{
	public int					channel_index;
	SourceRuntime				source;
	SourceChannelItem			conf;
	double						last_data=-1;
	Boolean 					first_write=true;
}
