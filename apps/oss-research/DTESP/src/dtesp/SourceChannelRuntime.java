package dtesp;



import dtesp.Config.*;

/**
 *   channel_index- channel index associated with source (initialized when connected)
 */		
class SourceChannelRuntime
{
	public int					channel_index;
	SourceRuntime				source;
	SourceChannelItem			conf;
	double						last_data=-1;
	Boolean 					first_write=true;
}
