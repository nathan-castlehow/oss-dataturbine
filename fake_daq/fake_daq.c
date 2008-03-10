/*! 
@file fake_daq.c
@brief A fake DAQ program for testing NSDS et al
@note Requires the flog messaging library.

This has gotten a bit more useful. Currently streams NUM_CHANNELS fake data
channels of sinusoidal data at 20Hz with a period of 100 samples. Threaded,
with data on the second thread. If you replace the data_generate routine, this
would make a decent skeleton DAQ program. Data rate is selectable on the
command line.

Robust against most network failures - knows to restart, etc. Simple
signal handler a la the driver as well.

Note that this uses integer channel IDs, where LabVIEW often has multicharacter
descriptive strings like "RoomTempCelsius". That makes this code much much
simpler, and for this application simplicity wins. For more useful testing, you
should get labview and its fake DAQ (or real!) code working anyway.

If you're using this as skeleton code for another DAQ system, be aware than you can
name a DAQ channel as any printable ASCII string. It does complicate the parsing
of open-port and close-port requests, though.

@date 9/5/02
@author Paul Hubbard
*/


#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <unistd.h>
#include <stdint.h>
#include <string.h>
#include <signal.h>
#include <time.h>
#include <pthread.h>
#include <getopt.h>

#include "nsds_util.h"
#include "flog.h"

// ---------------------------------------------------------------------
// Globals

//! Default TCP port to listen on
const int DAQ_PORT = 55055;

//! TCP queue length, number of queued connections allowed
const int TCP_Q_LEN = 1;

//! Global boolean set by signal handler to force exit
bool control_break;

//! Fake data sample rate
float sample_rate;

//! Global boolean for IPC between threads
bool streaming_active;

//! Length (period) of the sine wave we generate
const int SINE_PERIOD = 80*NUM_CHANNELS;

//! Shared structure for subscribed data channels
struct
{
    pthread_mutex_t   mutex;               //! R/W mutex for struct
    bool              active[NUM_CHANNELS];
    int               num_active;
} chan_struct;

//!hold a copy of the network file descriptors so that the signal handler can stop the program
typedef struct 
{
    //! FD of control socket
    int    control_socket;
    //! Current data TCP socket fd
    int    data_socket;
} connection_fds;

//! Global, hold file descriptors for signal handler to close
connection_fds curr_connection;

// ---------------------------------------------------------------------
/*!
@brief Return number of active channels
@retval Number active, <0 if error
@date 9/27/02
*/
int data_active_count(void)
{
    char         me[] = "data_active_count";
    int          rc;
    int          count;
    
    // This should be unthinkable, check anyway
    rc = pthread_mutex_lock(&chan_struct.mutex);
    if(rc != 0)
    {
	flog_usr(FLOG_ERROR, FL_ERR_MUTEX_FAILURE, me,
		 "Error locking mutex!");
	return(-1);
    }

    // Copy data
    count = chan_struct.num_active;
    
    rc = pthread_mutex_unlock(&chan_struct.mutex);
    if(rc != 0)
	flog_usr(FLOG_ERROR, FL_ERR_MUTEX_FAILURE, me,
		 "Error unlocking mutex!");
    
    return(count);
}
    
// ---------------------------------------------------------------------
/*!
@brief Is a channel subscribed?

Locks mutex and checks array to see if a channel is subscribed.

@param channel_id Channel ID to check
@retval True or false, false if error
@note uses mutex lock on chan_struct
@date 9/24/02
*/
bool data_channel_enabled(const int channel_id)
{
    char   me[] = "data_channel_enabled";
    int    rc;
    bool   is_act = false;
    
    // Range check
    if((channel_id < 0) || (channel_id >= NUM_CHANNELS))
    {
	flog_usr(FLOG_ERROR, FL_ERR_BAD_PARAM, me,
		 "Error, invalid channel ID %d", channel_id);
	return(false);
    }

    // This should be unthinkable, check anyway
    rc = pthread_mutex_lock(&chan_struct.mutex);
    if(rc != 0)
    {
	flog_usr(FLOG_ERROR, FL_ERR_MUTEX_FAILURE, me,
		 "Error locking mutex!");
	return(false);
    }

    is_act = chan_struct.active[channel_id];
    
    rc = pthread_mutex_unlock(&chan_struct.mutex);
    if(rc != 0)
	flog_usr(FLOG_ERROR, FL_ERR_MUTEX_FAILURE, me,
		 "Error unlocking mutex!");
    
    return(is_act);
}

// ---------------------------------------------------------------------
/*!
@brief Mark a channel as subscribed or unsubscribed

Lock the mutex, set the bit, report if it changed.

@note Idempotent - call multiple times w/no harm done.
@param channel_id Channel ID in question
@param subscribe if true, subscribe, if false un-sub
@retval 0 if OK, non-zero if error
@note Uses chan_struct and mutex therein
@date 9/24/02
*/
int data_channel_flag(const int channel_id, const bool subscribe)
{
    char      me[] = "data_channel_flag";
    int       rc;
    bool      old_setting;
    
    // Range check
    if((channel_id < 0) || (channel_id >= NUM_CHANNELS))
    {
	flog_usr(FLOG_ERROR, FL_ERR_BAD_PARAM, me,
		 "Error, invalid channel ID %d", channel_id);
	return(-1);
    }

    // This should be unthinkable, check anyway
    rc = pthread_mutex_lock(&chan_struct.mutex);
    if(rc != 0)
    {
	flog_usr(FLOG_ERROR, FL_ERR_MUTEX_FAILURE, me,
		 "Error locking mutex!");
	return(rc);
    }

    // Save old
    old_setting = chan_struct.active[channel_id];
    
    // Flag as requested - idempotent
    chan_struct.active[channel_id] = subscribe;
    
    // If setting has changed, update counts
    if(subscribe != old_setting)
    {
	if(subscribe)
	    chan_struct.num_active++;
	else
	    chan_struct.num_active--;
	
	if(chan_struct.num_active < 0)
	{
	    flog_usr(FLOG_ERROR, FL_ERR_NOCANDO, me,
		     "Underflow in active channel count; code error!");
	    chan_struct.num_active = 0;
	}
	
	if(chan_struct.num_active > NUM_CHANNELS)
	{
	    flog_usr(FLOG_ERROR, FL_ERR_NOCANDO, me,
		     "Overflow in active channel count; code error!");
	    chan_struct.num_active = (NUM_CHANNELS - 1);
	}
    }
    
    rc = pthread_mutex_unlock(&chan_struct.mutex);
    if(rc != 0)
    {
	flog_usr(FLOG_ERROR, FL_ERR_MUTEX_FAILURE, me,
		 "Error unlocking mutex!");
	return(rc);
    }

    // DDT, once mutex released
    if((subscribe) && (old_setting != subscribe))
    {
	flog_usr(FLOG_L2BUG, 0, me,
		 "Channel %d marked as active", channel_id);
    }
    else if(old_setting != subscribe)
    {
	flog_usr(FLOG_L2BUG, 0, me,
		 "Channel %d marked as inactive", channel_id);
    }
    
    return(0);
}

// ---------------------------------------------------------------------
/*!
@brief Data generator. Sine wave of sorts.
@date 9/16/02
@retval sin(x), scaled to period
@param index 0 to N-1
@param sin_period Period of function, cannot be zero!
*/
double data_generate(const int index, const double sin_period)
{
    double    t_arg = 0.0;
    double    t_per = 0.0;
    
    // Almost useless, this
    assert(sin_period != 0.0);
    
    // Scale index to period of function - this yields [0,1]
    t_per = (double) index / sin_period;
    
    // Map index onto [0.0, 2*pi]
    t_arg = t_per * 2.0 * M_PI;
    
    return(sin(t_arg));
}
    
// ---------------------------------------------------------------------
/*!
@brief Control-break handler. Sets global flag to indicate exit request.

@note Assumes installed in interrupt chain by someone else
@param signal Signal number
@date 3/25/02
*/
void sighandler(int signal)
{
    char    me[] = "sighandler";

    flog_usr(FLOG_NOTICE, 0, me, "Got signal %d, closing network and exiting", 
	     signal);
    control_break = true;

    // New 9/2/03, force a quit to propagate
    shutdown(curr_connection.control_socket, SHUT_RDWR);
    shutdown(curr_connection.data_socket, SHUT_RDWR);
    close(curr_connection.control_socket);
    close(curr_connection.data_socket);

    return;
}

// ---------------------------------------------------------------------
/*!
@brief Data handling thread

Transient thread to send data out the data channel. Dead simple.
Generates data and sends it out the data socket.

@note Assumes NSDS will read at least as fast as the DAQ sends!

@param arg Ptr to file descriptor to use for socket
@retval Null always
*/
void *data_thread_main(void *arg)
{
    char          me[] = "data_thread_main";
    int           *iptr = (int *) arg;
    int           data_socket;
    uint32_t      idx = 0;
    uint32_t      data_idx = 0;
    uint32_t      byte_count = 0;
    char          *data_buf = NULL;
    char          datum_buf[DATUM_LEN] = "";
    int           bufsize;
    double        data[SINE_PERIOD];
    int           rc;
    int           sample_delay = 50000; // 20Hz, 5e4
    time_t        t_start, t_delta;
    int           ipos = 0;
    int           jpos;
        
    // Check argument
    if(iptr == NULL)
    {
	flog_usr(FLOG_ERROR, FL_ERR_NULL_PTR, me, "Null pointer to thread!");
	return(NULL);
    }

    // Figure space required
    bufsize = (4 * sizeof(char) * NUM_CHANNELS * (TSTAMP_LEN + DATUM_LEN + 4));

    // Allocate send buffer
    data_buf = (char *) malloc(bufsize);
    if(data_buf == NULL)
    {
	flog_usr(FLOG_ERROR, FL_ERR_NO_MEMORY, me,
		 "Malloc failure on %d bytes!", bufsize);

	// Clear flags
	streaming_active = false;

	return(NULL);
    }

    // New 12/5/02, precalculate data
    for(idx = 0; idx < SINE_PERIOD; idx++)
	data[idx] = data_generate(idx, SINE_PERIOD);
    
    // If user set sample rate, let it override default
    // Limit to 100KHz, its a sanity thing.
    if((sample_rate > 0) && (sample_rate < 100000))
    {
	// Hz to microseconds conversion
	sample_delay = (int) (1000000.0 / sample_rate);
    }
	
    // Save args into local variable, just in case
    data_socket = *iptr;

    flog_usr(FLOG_NOTICE, 0, me, "Data thread running, %d Hz, %d usec delay", 
	     sample_rate, sample_delay);

    // Note the time we started running, used for rate calculations
    t_start = time(NULL);

    // Run until port closed, error, control-c or done streaming
    while((streaming_active) && (!control_break))
    {
	// If no channels open, sleep and re-loop
	if(data_active_count() <= 0)
	{
	    usleep(sample_delay);
	    continue;
	}

	// DDT zero the buffer
	memset(data_buf, 0x00, (size_t) bufsize);

	// Start buffer off with timestamp
	strcpy(data_buf, gen_timestamp());

	// Loop over channels, appending subscribed ones
	for(idx = 0; idx < NUM_CHANNELS; idx++)
	{
	    if(data_channel_enabled(idx))
	    {
		// Format it up, NSDS-style, i.e. channel# / value
		// More specs online; check the webpage
		jpos = ( ipos * (NUM_CHANNELS - idx) ) % SINE_PERIOD;
		sprintf(datum_buf, "\t%d\t%f", idx, data[jpos]);

		// Append to send buffer
		strcat(data_buf, datum_buf);
	    }
        }

	// Sine wraparound
	ipos = (ipos + 1) % SINE_PERIOD;
	    
	// Send it off
	rc = tcp_nl_write(data_socket, data_buf);
	if(rc != strlen(data_buf))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me,
		     "Error sending data, short write");
	    goto thread_bailout;
	}

	// Just in case, check for buffer overrun.
	assert(rc <= bufsize);

	// Track ttl xmitted bytes
	byte_count += rc;
	data_idx++;
	
	// Sleep
	usleep(sample_delay);
    }
    
  thread_bailout:

    // Figure elapsed time, to nearest second
    t_delta = time(NULL) - t_start;
    if(t_delta == 0) 
      t_delta = 1;

    flog_usr(FLOG_NOTICE, 0, me, 
	     "Data thread exiting, %d points sent, appx %f kb/sec", 
	     data_idx + 1, ((float) (byte_count >> 10) / t_delta));

    // Notify main thread
    streaming_active = false;

    free(data_buf);
    
    return(NULL);
}


// ---------------------------------------------------------------------
/*!
@brief Main worker routine: read / parse / respond
@date 9/5/02

@retval 0 OK, != 0 means error
@param control_socket FD of control connection
@param data_socket FD of data connection
Read commands, respond as required. Starts data thread if necessary.

*/
int daq_do_work(const int control_socket, const int data_socket)
{
    char              me[] = "daq_do_work";
    int               rc, bytes_read, idx;
    time_t            req_timeout = 0;
    char              cmd_buf[MAX_CMD_LEN] = "";
    char              daq_running[] = "Running";
    char              unk_cmd[] = "Unknown command";
    char              strm_ok[] = "Streaming data on data channel from port";
    char              strm_stop[] = "Stopping data on data channel from port";
    pthread_t         data_thread;
    char              *cptr = NULL;
    char              *s_ptr = NULL;
    char              reply_buf[128] = "";
    int               chan_id;
    char              tok_buf[MAX_CMD_LEN] = "";
    char              channel_list[NUM_CHANNELS * 5] = "";
    char              unit_list[NUM_CHANNELS * 3] = "";
    
    flog_usr(FLOG_PROGRESS, 0, me, "Waiting for request (indefinite wait)...");
    
    // Build lists of channels and units, static
    // Uses reply_buf as a scratch buffer
    for(idx = 0; idx < NUM_CHANNELS; idx++)
    {
	if(idx < (NUM_CHANNELS - 1))
	{
	    sprintf(reply_buf, "%d,", idx);
	    strcat(unit_list, "V, ");  // All units are volts
	}
	else
	{
	    sprintf(reply_buf, "%d", idx);
	    strcat(unit_list, "V");  // All units are volts
	}
	strcat(channel_list, reply_buf);
    }
    
    // DDT
    assert(strlen(channel_list) < ((NUM_CHANNELS * 4) - 1));

    // Read request from NSDS
    bytes_read = tcp_nl_read(control_socket, cmd_buf, req_timeout);
    if(bytes_read <= 0)
    {
	flog_usr(FLOG_NOTICE, FL_ERR_TRANSCEIVE, me, 
		 "Error on request read");
	return(1);
    }
    
    // Units are, of course, also fake
    if(strstr(cmd_buf, "list-units") != NULL)
    {
	flog_usr(FLOG_PROGRESS, 0, me, 
		 "Got request for list of units");

	// Send pre-built list
	rc = tcp_nl_write(control_socket, unit_list);
	if(rc != strlen(unit_list))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me, 
		     "Error writing status response on control channel");
	    return(2);
	}
    }
    else if(strstr(cmd_buf, "list-channels") != NULL)
    {
	flog_usr(FLOG_PROGRESS, 0, me, 
		 "Got request for channel list");
	
	// Send pre-built list
	rc = tcp_nl_write(control_socket, channel_list);
	if(rc != strlen(channel_list))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me, 
		     "Error writing status response on control channel");
	    return(2);
	}
    }
    else if(strstr(cmd_buf, "daq-status") != NULL)
    {
	flog_usr(FLOG_L2BUG, 0, me, 
		 "Got status request '%s' OK, responding with '%s'",
		 cmd_buf, daq_running);

	rc = tcp_nl_write(control_socket, daq_running);
	if(rc != strlen(daq_running))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me, 
		     "Error writing status response on control channel");
	    return(2);
	}
    }
    else if(strstr(cmd_buf, "open-port") != NULL)
    {
	// Save a copy to munge
	strcpy(tok_buf, cmd_buf);
	
	// Start the parser, ignore first result ('open-port')
	cptr = strtok_r(tok_buf, CMD_BUF_DELIM_CHARS, &s_ptr);

	// Now look for channel ID string
	cptr = strtok_r(NULL, CMD_BUF_DELIM_CHARS, &s_ptr);

	// Cptr should now point to numeric argument
	if((cptr == NULL) || (chan_id_valid(cptr) == false))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_BAD_PARAM, me,
		     "Invalid data port '%s' on subscribe", cptr);

	    // Send out bad port message, blind send
	    sprintf(reply_buf, BAD_OPEN_CLOSE, cptr);

	    flog_usr(FLOG_DEBUG, 0, me, 
		     "Sending '%s' to NSDS", reply_buf);
	    tcp_nl_write(control_socket, reply_buf);
	    return(0);
	}

	// Single arg in channel ID
	chan_id = atoi(cptr);

	// Range check channel id for validity
	if((chan_id < 0) || (chan_id > (NUM_CHANNELS - 1)))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_BAD_VALUE, me,
		     "Invalid channel ID '%s' in subscribe request",
		     cptr);

	    // Send out bad port message, blind send
	    sprintf(reply_buf, BAD_OPEN_CLOSE, cptr);
	    tcp_nl_write(control_socket, reply_buf);
	    return(0);
	}

	flog_usr(FLOG_L2BUG, 0, me,
		 "Got data subscription request for channel %d", chan_id);

	if(streaming_active == false)
	{
	    flog_usr(FLOG_CL2BUG, 0, me, "Starting data thread");
	    
	    // Set boolean semaphore for data thread
	    streaming_active = true;
	    
	    // Clear out all old subscriptions
	    for(idx = 0; idx < NUM_CHANNELS; idx++)
		data_channel_flag(idx, false);

	    // Mark this one as active
	    rc = data_channel_flag(chan_id, true);
	    if(rc != 0)
	    {
		flog_usr(FLOG_ERROR, FL_ERR_BAD_PARAM, me,
			 "Error %d activating channel %d", rc, chan_id);
		return(rc);
	    }

	    // Fire up a thread
	    rc = pthread_create(&data_thread, NULL, data_thread_main,
				(void *) &data_socket);
	    if(rc != 0)
	    {
		flog_usr(FLOG_ERROR, FL_ERR_SYSTEM, me,
			 "Error on thread creation!");
		return(3);
	    }
	    
	    // Let 'er rip
	    pthread_detach(data_thread);
	    pthread_setconcurrency(2);
	    
	    flog_usr(FLOG_CL2BUG, 0, me, "Data thread started OK");
	}
	else // Streaming already up, just add new channel
	{
	    flog_usr(FLOG_CL4BUG, 0, me, "adding to active list");

	    // Mark as active
	    rc = data_channel_flag(chan_id, true);
	    if(rc != 0)
	    {
		flog_usr(FLOG_ERROR, FL_ERR_BAD_PARAM, me,
			 "Error %d activating channel %d", rc, chan_id);
		return(rc);
	    }
	}

	// Append channel ID
	sprintf(reply_buf, "%s %d", strm_ok, chan_id);
	
	// Ack to driver, on _control_ channel
	rc = tcp_nl_write(control_socket, reply_buf);
	if(rc != strlen(reply_buf))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me, 
		     "Error writing response on control channel");
	    return(2);
	}
    }
    else if(strstr(cmd_buf, "close-port") != NULL)
    {
	// Save a copy to munge
	strcpy(tok_buf, cmd_buf);
	
	// Start the parser, ignore first result ('close-port')
	cptr = strtok_r(tok_buf, CMD_BUF_DELIM_CHARS, &s_ptr);

	// Find channel ID string
	cptr = strtok_r(NULL, CMD_BUF_DELIM_CHARS, &s_ptr);

	// Cptr should now point to numeric argument
	if((cptr == NULL) || (chan_id_valid(cptr) == false))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_BAD_PARAM, me,
		     "Invalid data port '%s' on unsubscribe", cptr);

	    sprintf(reply_buf, BAD_OPEN_CLOSE, cptr);
	    flog_usr(FLOG_DEBUG, 0, me, 
		     "Sending '%s' to NSDS", reply_buf);
	    tcp_nl_write(control_socket, reply_buf);
	    return(0);
	}

	// Single arg in channel ID
	chan_id = atoi(cptr);

	if((chan_id < 0) || (chan_id > (NUM_CHANNELS - 1)))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_BAD_VALUE, me,
		     "Invalid channel ID '%s' in unsubscribe request",
		     cptr);

	    // Send out bad port message, blind send
	    sprintf(reply_buf, BAD_OPEN_CLOSE, cptr);
	    tcp_nl_write(control_socket, reply_buf);
	    return(0);
	}

	flog_usr(FLOG_NOTICE, 0, me,
		 "Got port close request on channel %d, %d active", 
		 chan_id, data_active_count());	

	// Mark as inactive
	rc = data_channel_flag(chan_id, false);

	// Append channel ID
	sprintf(reply_buf, "%s %d", strm_stop, chan_id);

	// Ack to driver, on _control_ channel
	rc = tcp_nl_write(control_socket, reply_buf);
	if(rc != strlen(reply_buf))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me,
		     "Error writing response on control channel");
	    return(2);
	}
    }
    else // all other commands
    {
	flog_usr(FLOG_ERROR, FL_ERR_NOCANDO, me, 
		 "Got unknown command '%s'", cmd_buf);

	// Reply with "Unknown command 'command'"
	sprintf(reply_buf, "%s '%s'", unk_cmd, cmd_buf);
	rc = tcp_nl_write(control_socket, reply_buf);
	if(rc != strlen(reply_buf))
	{
	    flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me, 
		     "Error writing response on control channel");
	    return(2);
	}
    }

    // Make super-sure response is sent
    fsync(control_socket);

    flog_usr(FLOG_L4BUG, 0, me, "done");
    
    return(0);
}


// ---------------------------------------------------------------------
/*!
@brief Open network socket, loop doing work until control-c
@date 9/5/02
@note Opens server socket

This handles the dropped connections and other TCP errors, mostly by
restarting the connects. Also kills data thread if connections die.

*/
void daq_main_loop(const int daq_port, const char *HOST_NAME)
{
    char               me[] = "daq_main_loop";
    int                ctrl_master, data_master;
    int                control_socket, data_socket;
    struct sockaddr_in fsin;
    int                addr_len = sizeof(fsin);
    bool               do_init = false;
    int                cmd_count = 0;

    // Set up master control socket
    ctrl_master = tcp_socket_make(daq_port, TCP_Q_LEN, HOST_NAME);
    if(ctrl_master <= 0)
	return;
        
    // Set up master data socket
    data_master = tcp_socket_make(daq_port + 1, TCP_Q_LEN, HOST_NAME);
    if(data_master <= 0)
	return;

    // ------------------------
    // Main loop, runs until control-c
    while(control_break == false)
    {
	// If restarting, close all first
	if(do_init == true)
	{
	    flog_usr(FLOG_PROGRESS, 0, me, 
		     "Re-init DAQ, closing connection");

	    tcp_close(control_socket);
	    tcp_close(data_socket);

	    // Tell data thread to exit
	    streaming_active = false;
	}
	else // Set flag for subsequent loops
	    do_init = true;
	
	
	// Off we go
	flog_usr(FLOG_L1BUG, 0, me, "Waiting for driver control connection...");
    
	control_socket = accept(ctrl_master, 
			       (struct sockaddr *) &fsin, &addr_len);
	// Check who connected
	flog_usr(FLOG_CL1BUG, 0, me, 
		 "Driver '%s' connected to control channel", 
		 tcp_peername(control_socket));

	flog_usr(FLOG_L1BUG, 0, me, "Waiting for driver data connection...");
    
	data_socket = accept(data_master, 
			     (struct sockaddr *) &fsin, &addr_len);

	// Save into struct for signal handler
	curr_connection.control_socket = control_socket;
	curr_connection.data_socket = data_socket;

	// Check who connected
	flog_usr(FLOG_CL1BUG, 0, me, 
		 "Driver '%s' connected to data channel", 
		 tcp_peername(data_socket));

	/* Call the routine to read and respond
	   If an error, we will re-init the TCP connections.
	*/
	while(daq_do_work(control_socket, data_socket) == 0)
	{
	    flog_usr(FLOG_L2BUG, 0, me, 
		     "Command #%d completed OK", cmd_count++);
	}
    }
    
    return;
}
	
// ---------------------------------------------------------------------
/*!
@brief Init the network, talk to all

Sets up messaging, installs signal handler, calls worker routine.

@note Driver port passed on command line
@note Ditto w/sample rate
@param argc Argc as passed by shell
@param argv Argv as passed by shell
@return 0 All is good
@return Non-zero All is not good

*/
int main(int argc, char *argv[])
{
    char          me[] = "main";
    uint16_t      daq_port = 0;
    int           help_flag = 0;
    char          *hostname = NULL;
    int           idx, rc;
    
    struct option long_options[] = 
	{
	    {"port", required_argument, 0, 'p'},
	    {"rate", required_argument, 0, 'r'},
        {"host", required_argument, 0, 'i'},
	    {"help", no_argument, &help_flag, 'h'},
	    {0, 0, 0, 0}
	};
    
    // Set defaults before (possibly) overriden by command line
    daq_port = DAQ_PORT;
    sample_rate = 20.0;

    // Set logging options
    flog_set_report(FLOG_L3BUG, FLOG_QUIET, FLOG_QUIET);
    flog_set_style(0x2019, 0x0038, 0x6008);    

    pthread_mutex_init(&chan_struct.mutex, NULL);
    
    // Parse command line with getopt_long
    while(1)
    {
        rc = getopt_long(argc, argv, "p:r:i:h",
                         long_options, &idx);
        if(rc == -1)
            break;
        
        switch(rc)
        {
            case 0:
                /* If this option set a flag, do nothing else now. */
                break;
                
            case 'p':
                daq_port = atoi(optarg);
                flog_usr(FLOG_NOTICE, 0, me, 
                         "Setting port to %d", daq_port);
                break;
                
            case 'r':
                sample_rate = atof(optarg);
                
                flog_usr(FLOG_NOTICE, 0, me,
                         "Setting sample rate to %f Hz", sample_rate);
                break;
                
            case 'i':
                hostname = optarg;
                flog_usr(FLOG_NOTICE, 0, me,
                         "Will bind to ports on host %s", hostname);
                break;
                
                
            case '?':
                /* getopt_long already printed an error message. */
                break;  
                
            case 'h':
                help_flag = 1;
                break;
                
            default:
                flog_usr(FLOG_ERROR, FL_ERR_SYSTEM, me, 
                         "Unreachable case in getopt_long parse");
                exit(1);
        }
    }
    
    if(help_flag != 0)
    {
        print_args(long_options);
        return(1);
    }

    flog_usr(FLOG_NOTICE, 0, me, 
	     "Compiled for %d data channels max", NUM_CHANNELS);
    
    // Signal handler
    flog_usr(FLOG_NOTICE, 0, me, "Installing signal handler");
    control_break = false;
    signal(SIGINT, sighandler);
    signal(SIGPIPE, sighandler);

    // Call main routine
    daq_main_loop(daq_port, hostname);

    // Bail
    flog_usr(FLOG_NOTICE, 0, me, "Done");
    return(0);
}
