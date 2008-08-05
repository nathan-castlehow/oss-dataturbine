/*!

@file nsds_util.c
@author Paul Hubbard
@date 9/5/02
@brief Utility / common routines for the NSDS driver and DAQ 
@note Requires the flog message library as of 9/24/02
*/

#include "nsds_util.h"
#include "flog.h"

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <unistd.h>
#include <stdint.h>
#include <string.h>
#include <ctype.h>
#include <signal.h>
#include <time.h>
#include <sys/time.h>
#include <getopt.h>

// Ref to control-c handler
extern bool control_break;

// ---------------------------------------------------------------------
/*!
@brief Printout out command line arguments based on getopt args
 @date 12/16/02
 
 Just prints out the command line arguments, long and short.
 @param long_opts Pointer to getopt_long argument structure
 */
void print_args(const struct option *long_opts)
{
    char          me[] = "print_args";
    struct option *cur_opt = NULL;
    
    if(long_opts == NULL)
    {
        flog_usr(FLOG_ERROR, FL_ERR_NULL_PTR, me, "Null pointer");
        return;
    }
	
    flog_usr(FLOG_NOTICE, 0, me, "Options are as follows:");
	
    // set pointer to first argument in list
    cur_opt = (struct option *) &long_opts[0];
    
    while((cur_opt != NULL) && (cur_opt->name != NULL))
    {
        flog_usr(FLOG_CNOTICE, 0, me,
                 "--%s, -%c", cur_opt->name, (char) cur_opt->val);
        cur_opt++;
    }
	
    return;
}

// ---------------------------------------------------------------------
/*!
@brief Generate an ISO 8601-compliant timestamp
 @note Not threadsafe!
 @note Requires gettimeofday call
 @retval Ptr to static buffer with timestamp string
 @date 9/19/02
 
 This is, frankly, a hack. It generates a UTC-format ISO8601 timestamp,
 with 5-digit fractional seconds. Its for the fake_daq program, so that
 it can spew psuedo data. The format, however, should be correct.
 
 */
char *gen_timestamp(void)
{
    char           me[] = "gen_timestamp";
    struct tm      l_tm;
    struct timeval t_highres;
    int            rc;
    static char    timebuf[80];
    time_t         in_time;
    
    // get fractional seconds
    rc = gettimeofday(&t_highres, NULL);
    if(rc != 0)
    {
		flog_usr(FLOG_ERROR, FL_ERR_SYSTEM, me,
				 "Error reading current time!");
		return(NULL);
    }
	
    // Save elapsed seconds into time_t
    in_time = (time_t) t_highres.tv_sec;
	
    // Convert current into UTC
    gmtime_r(&in_time, &l_tm);
    
    // ISO 8601 format, as per 
    // http://www.exit109.com/~ghealton/y2k/yrexamples.html
    sprintf(timebuf, "%04d-%02d-%02dT%02d:%02d:%02d.%05.0f",
			l_tm.tm_year + 1900,
			l_tm.tm_mon + 1,
			l_tm.tm_mday,
			l_tm.tm_hour,
			l_tm.tm_min,
			l_tm.tm_sec,
			(float) (t_highres.tv_usec / 10.0));
    
    return(timebuf);
}


/*! ---------------------------------------------------------------------
@brief Create server socket, ready for accept()

@note Borrowed from FNAL server code
@date 8/27/02
@retval > 0 (Socket, OK)
@retval <=0 (Error)
@param SRV_PORT Port to create
@param QUEUE_LENGTH TCP queue length, passed to listen()
@param HOST_NAME If set, hostname to bind to. If null, binds to INADDR_ANY
Does the low-level work of setting up a listen socket, very careful
about possible errors and socket options. Produced socket is ready
for accept() calls.

*/
int tcp_socket_make(const uint16_t SRV_PORT, const int QUEUE_LENGTH, const char *HOST_NAME)
{
    char                me[] = "tcp_socket_make";
    struct sockaddr_in 	serv_in;
    struct protoent     *ppe = NULL;
    int    	            itmp;
    int                 new_socket = 0;
    const int           no_socket = -1;
    int                 so_reuseaddr = 1;
    struct linger       so_linger;
	
    // Clear struct
    memset(&serv_in, 0x00, sizeof(serv_in));
	
    ppe = getprotobyname("tcp");
    if(ppe == NULL)
    {
		flog_usr(FLOG_ERROR, FL_ERR_SYSTEM, me, 
				 "Error on getprotobyname");
		return(no_socket);
    }
    
    itmp = socket(PF_INET, SOCK_STREAM, ppe->p_proto);
	
    // Socket returns -1 on an error
    if(itmp < 0)
    {
		flog_usr(FLOG_ERROR, FL_ERR_SOCKET, me,
				 "Error on socket creation; server running?");
		return(no_socket);
    }
    else
		new_socket = itmp;
	
    // Set socket options - see LSPBE book for details
    itmp = setsockopt(new_socket, SOL_SOCKET, SO_REUSEADDR,
					  &so_reuseaddr, sizeof(so_reuseaddr));
    if(itmp != 0)
		flog_usr(FLOG_WARNING, 0, me,
				 "Failure on setsockopt");
	
    // Set linger to true so that close waits for flush
    so_linger.l_onoff = true;
    so_linger.l_linger = 5;    // 5 second timeout on close(2)
    itmp = setsockopt(new_socket, SOL_SOCKET, SO_LINGER,
					  &so_linger, sizeof(so_linger));
    if(itmp != 0)
		flog_usr(FLOG_WARNING, 0, me,
				 "Failure on setsockopt, continuing");
    
    // Set up servers' address
    serv_in.sin_family = AF_INET;
    serv_in.sin_port = htons(SRV_PORT);

    // Oddly, sin_len is MIA on Linux. And Solaris. Works on OSX without it, so remove.
    // serv_in.sin_len = sizeof(struct sockaddr_in);

    // Did the caller specify an IP to bind to? If not, use the wildcard
    if(HOST_NAME != NULL)
    {
        flog_usr(FLOG_NOTICE, 0, me, "Binding to %s", HOST_NAME);
		
		// Convert from ASCII IP into network-order binary
		itmp = inet_pton(AF_INET, HOST_NAME, &serv_in.sin_addr.s_addr);
		if(itmp < 0)
		{
			flog_usr(FLOG_ERROR, FL_ERR_BAD_VALUE, me, "Unable to parse IP address");
			close(new_socket);
			return(no_socket);
		}		
    }
    else
    {
        flog_usr(FLOG_NOTICE, 0, me, "Binding to all local addresses");
        serv_in.sin_addr.s_addr = INADDR_ANY;
    }
    // Bind the socket to address
    itmp = bind(new_socket, (struct sockaddr *) &serv_in, sizeof(serv_in));
	
    // bind also returns -1 on an error
    if(itmp < 0)
    {
        flog_usr(FLOG_ERROR, FL_ERR_SOCKET, me,
                 "Unable to bind to port %d; server already running?", SRV_PORT);            
//        perror("Errno says");
        
        close(new_socket);
		return(no_socket);
    }
	
    // enable incoming connections
    // No max queue length in linux >= 2.2, not sure about other unixes -
    // this is a side benefit of the syn flood fixes
    itmp = listen(new_socket, QUEUE_LENGTH);
    if(itmp < 0)
    {
		flog_usr(FLOG_ERROR, FL_ERR_SOCKET, me, "Listen failure");
		close(new_socket);
		
		return(no_socket);
    }
    
    flog_usr(FLOG_NOTICE, 0, me,
			 "Network setup completed OK on port %d", SRV_PORT);
	
    return(new_socket);
}

/*! ---------------------------------------------------------------------
@brief Send a string, appending a newline character
@date 8/29/02
@retval Number of bytes written, not counting newline!
@param socket Socket/fd
@param buf C string to send
@note Forces a sync to flush at the end of the send

Very simple, writes a buffer and appends a newline. 
*/
int tcp_nl_write(const int socket, const char *buf)
{
    char   me[] = "tcp_nl_write";
    int    rc;
    int    len = strlen(buf);
    char   newline[] = "\n";
    
    rc = write(socket, buf, len);
    if(rc != len)
    {
		flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me,
				 "Error writing to socket");
		return(rc);
    }
    
    // Append newline
    rc = write(socket, newline, 1);
    if(rc != 1)
    {
		flog_usr(FLOG_ERROR, FL_ERR_TRANSCEIVE, me,
				 "Error writing newline to socket");
		return(rc);
    }
	
    // Flush
    fsync(socket);
    
    // Made it OK
    return(len);
}

/*! ---------------------------------------------------------------------
@brief Read until EOL or MAX_CMD_LEN bytes

Read from TCP into static buffer until
- get newline
- or exceed buffer length.

	All messages are newline-delimted, so this is used all over the place
	to read commands and data.

	Yeah, yeah, I know already. Needs dynamic buffers, or sizing, or
	code that Sucks Less.

	@note Removes newline and terminates string with 0x00
	@date 8/28/02

	@param socket tcp socket
	@param buf Buffer, of >= MAX_CMD_LEN bytes
	@param timeout If > 0, uses this as timeout. 0 waits forever.
	@retval Number of bytes read, < 0 on an error, 0 if timed out
	*/
int tcp_nl_read(const uint16_t socket, char *buf, const time_t timeout)
{
    char              me[] = "tcp_nl_read";
    int               bytes_read = 0;
    int               rc;
    char              *write_ptr = buf;
    bool              done = false;
    bool              got_newline = false;
    socket_wait_ret   sel_rc;
    bool              got_err = false;    
	
    if(buf == NULL)
		return(-1);
	
    // DDT zero the buffer
    memset(buf, 0x00, MAX_CMD_LEN);
    
    while(!done)
    {
		sel_rc = SWAIT_SYS_ERR;
		
		if(bytes_read >= (MAX_CMD_LEN - 1))
		{
			flog_usr(FLOG_ERROR, FL_ERR_BUF_NOSPACE, me,
					 "Max cmd length %d exceeded with no delimiter",
					 MAX_CMD_LEN);
			done = true;
			got_err = true;
			break;
		}
		
		// Wait for data available
		sel_rc = tcp_socket_wait(socket, timeout);
		if(sel_rc == SWAIT_TIMEOUT)
		{
			flog_usr(FLOG_L4BUG, 0, me, "No data before timeout");
			done = true;
			break;
		}
		if(sel_rc != SWAIT_GOT_DATA)
		{
			flog_usr(FLOG_ERROR, FL_ERR_SOCKET, me, "Error waiting for data");
			done = true;
			got_err = true;
			break;
		}
		
		// read a byte at a time, Bottlenecks Anonymous
		rc = read(socket, write_ptr, 1);
		if(rc == 1)
		{
			// got a byte
			if((*write_ptr == '\n') || (*write_ptr == '\r'))
			{
				// Strip leading newlines, in case get cr/lf combo
				if(bytes_read == 0)
				{
					// Discard the data by leaving write ptr in same place
					continue;
				}
				
				got_newline = true;
				done = true;
				
				// Terminate the string, overwrites newline
				*write_ptr = 0x00;
				
				break;
			}
			else // No newline
			{
				write_ptr++;
				bytes_read++;
			}
			
		}
		else if(rc <= 0) // Error on read?
		{
			done = true;
			got_err = true;
			break;
		}
		else // should never occur, but just in case
			usleep(10);
    }
	
    // DDT
    //printf("\nnl_read bytes: %d newline: %d", bytes_read, got_newline);
    
    if(got_newline)
		return(bytes_read);
    else if(got_err == false)
		return(0);
    else
		return(-1);
}


/*! ---------------------------------------------------------------------
@brief Routine, after Comer, for opening a TCP connection.
@note hardwired to use TCP, stream mode

Assumes
Server ready and able to answer connections
TCP/IP available
Not multithreaded - gethostbyname is NOT reentrant!

@param server Ptr to string of server name
@param port Port number on server
@retval 0 if no errors, !=0 on any error.

@date  4/00, modified 8/28/02
*/
int tcp_connect(const char *server, const uint16_t port)
{
    char               me[] = "tcp_connect";
    struct hostent     *phe;    /* pointer to host information entry    */
    struct protoent    *ppe;    /* pointer to protocol information entry*/
    struct sockaddr_in sin;     /* an Internet endpoint address         */
    int                s, type; /* socket descriptor and socket type    */
    struct linger      so_linger;
    int                itmp;
    
    // Zero struct
    memset(&sin, 0x00, sizeof(sin));
	
    // Address family is INET
    sin.sin_family = AF_INET;
	
    /* Map service name to port number, ensuring network byte order */
    sin.sin_port = htons(port);
	
    /* Map host name to IP address, allowing for dotted decimal */
    phe = gethostbyname(server);
	
    if(phe != NULL)
        memcpy(&sin.sin_addr, phe->h_addr, phe->h_length);
    else
    {
		flog_usr(FLOG_ERROR, FL_ERR_HOST_NOTFOUND, me,
				 "Connection error in gethostbyname");
        return(-1);
    }
	
    /* Map protocol name to protocol number */
    if ( (ppe = getprotobyname("tcp")) == 0)
    {
		flog_usr(FLOG_ERROR, FL_ERR_SYSTEM, me, 
				 "Connection error in getprotobyname");
        return(-2);
    }
	
    type = SOCK_STREAM;
	
    /* Allocate a socket */
    s = socket(PF_INET, type, ppe->p_proto);
    if (s < 0)
    {
		flog_usr(FLOG_ERROR, FL_ERR_SYSTEM, me, "Error allocating socket");
        return(-3);
    }
	
    // Set linger to true so that close waits for flush, new 6/24/01 pfh
    so_linger.l_onoff = true;
    so_linger.l_linger = 5;    // 5 second timeout on close(2)
    itmp = setsockopt(s, SOL_SOCKET, SO_LINGER,
					  &so_linger, sizeof(so_linger));
    if(itmp != 0)
		flog_usr(FLOG_WARNING, FL_ERR_SOCKET, me, 
				 "Error setting socket opts");
    
    /* Connect the socket */
    if(connect(s, (struct sockaddr *)&sin, sizeof(sin)) < 0)
    {
		close(s);
        return(-1);
    }
	
    return s;
}

// ------------------------------------------------------------------------------
/*! @brief Function to wait on a socket, with or without timeout.

Blocking or non-blocking wait until a socket is ready for reading. Used
in functions that cannot wait forever, as well as ones that can.

@note Also copied from FNAL code
@note  Negative or zero timeout means blocking wait; might wanna change
this to a bool param?
@retval socket_wait_ret enumerated type
@param socket file descriptor
@param timeout Timeout, in seconds
*/
socket_wait_ret tcp_socket_wait(const int socket, const time_t timeout)
{
    char            me[] = "tcp_socket_wait";
    fd_set          read_fds;
    fd_set          err_fds;
    struct timeval  *sel_timeout = NULL;
    int             num_fds;
    int             sel_return;
    struct timeval  foo;
	
	
    // Do we have to worry about timeouts?
    if(timeout > 0)
    {
		sel_timeout = (struct timeval *) &foo;
		
		sel_timeout->tv_sec = timeout;
		sel_timeout->tv_usec = 0L;
    }
    else
		sel_timeout = NULL;
    
    // Clear all bits in read and error bitmaps
    FD_ZERO(&read_fds);
    FD_ZERO(&err_fds);
	
    // Flag socket descriptor as active in both
    FD_SET(socket, &read_fds);
    FD_SET(socket, &err_fds);
	
    // Ask system how many file descriptors we have to track
    num_fds = getdtablesize();
	
    // Call select, note that this is _only_ for reading!
    sel_return = select(num_fds, &read_fds,
						NULL, &err_fds, sel_timeout);
	
    // See if we got a readable socket or an error
    if(FD_ISSET(socket, &err_fds))
    {
		flog_usr(FLOG_INFO, FL_ERR_SOCKET, me, 
				 "Error bit set in select");
		return(SWAIT_SYS_ERR);
    }
	
    // Figure out return code
    if((sel_return > 0) && (FD_ISSET(socket, &read_fds)))
		return(SWAIT_GOT_DATA);
	
    if(sel_return == -1)
    {
		flog_usr(FLOG_INFO, FL_ERR_SOCKET, me, "select returns -1");
		return(SWAIT_SYS_ERR);
    }
	
    if(sel_return == 0)
		return(SWAIT_TIMEOUT);
	
    // Catch-all; should be unreachable
    flog_usr(FLOG_L3BUG, 0, me,
			 "Select returned %d, errno=%d", sel_return, errno);
    return(SWAIT_SYS_ERR);
}

// ---------------------------------------------------------------------
/*!
@brief Function to return a string w/name of connected peer
 @note Non-reentrant, uses static buffer
 
 Does a DNS lookup on the peer, returns their name or (if DNS fails)
 their IP address as a dotted-decimal string. Quite handy.
 
 */
char * tcp_peername(const int socket)
{
    char               me[] = "tcp_peername";
	socklen_t	       socklen;
    int                itmp, rc;
    struct sockaddr    client_info;
    struct sockaddr_in *so = (struct sockaddr_in *) &client_info;
    struct hostent     *cl_hostent = NULL;
    static char        remote_hostname[128] = "";
    
	
    // Check who connected
    socklen = sizeof(struct sockaddr);
    rc = getpeername(socket, &client_info, &socklen);
    if((rc != 0) || (itmp < 0))
    {
		flog_usr(FLOG_ERROR, FL_ERR_HOST_NOTFOUND, me,
				 "Error getting peer information");
		return(remote_hostname);
    }
	
    // Do reverse DNS lookup
    cl_hostent = gethostbyaddr((char *) &so->sin_addr.s_addr,
							   sizeof(so->sin_addr.s_addr), AF_INET);
    if(cl_hostent == NULL)
    {
		flog_usr(FLOG_ERROR, FL_ERR_HOST_NOTFOUND, me,
				 "Reverse DNS lookup failed");
		strcpy(remote_hostname, inet_ntoa(so->sin_addr));
    }
    else
		strcpy(remote_hostname, cl_hostent->h_name);
    
    return(remote_hostname);
}



// ---------------------------------------------------------------------
/*!
@brief Loop until can open client connection
 @param host Hostname
 @param port Port number
 @param retry_delay Time, in seconds, to sleep between attempts
 @param end_time If >0, when we give up 
 @param description String to print to screen
 @retval If > 0, socket FD
 @retval <= 0, error or timeout
 
 Retry, every N seconds, to open a connection. Used to provide robustness
 against network failures; non-blocking.
 
 */
int tcp_connect_retry(const char *host, const uint16_t port,
					  const time_t retry_delay, const time_t end_time,
					  const char *description)
{
    char      me[] = "tcp_connect_retry";
    bool      done = false;
    int       retry_count = 0;
    int       cl_socket = -1;
    const char      *desc = NULL;
    
	
    // Use description if provided
    if(description != NULL)
		desc = description;
    else
		desc = host;
    
    while((done == false) && !(control_break))
    {
		// If end time is valid (>0), check
		if((end_time > 0) && (time(NULL) >= end_time))
		{
			flog_usr(FLOG_L3BUG, 0, me,
					 "End time reached without connection, exiting");
			
			done = true;
			break;
		}
		
		flog_usr(FLOG_L2BUG, 0, me, 
				 "Connecting to %s on %s, port %d", desc, host, port);
		
		cl_socket = tcp_connect(host, port);
		if(cl_socket > 0)
		{
			done = true;
			break;
		}
		
		flog_usr(FLOG_CL2BUG, 0, me,
				 "Unable to connect to %s on %s, sleeping %d seconds",
				 desc, host, (int) retry_delay);
		
		retry_count++;
		sleep(retry_delay);
    }
    
    return(cl_socket);
}

// ---------------------------------------------------------------------
/*!
@brief Shutdown and close TCP socket
 
 Does shutdown (read/write) and then close if fd > 0
 
 @param socket_fd File descriptor of socket
 */
void tcp_close(const int socket_fd)
{
    if(socket_fd <= 0)
		return;
    
    //Assume valid FD
    shutdown(socket_fd, SHUT_RDWR);
    close(socket_fd);
    
    return;
}

// ---------------------------------------------------------------------
/*!
@brief Scan channel ID string for invalid characters
 
 Scan string - if at least 1 numeric, and zero non-numeric, its OK
 @param chan_id String to test, must be zero-terminated
 @retval true No invalid characters found
 @retval false Invalid character(s) found
 */
bool chan_id_valid(const char *chan_id)
{
    char  me[] = "chan_id_valid";
    bool  inv_char = false;
    int   valid_chars = 0;
    const char  *cur_ptr = chan_id;
	
    if(chan_id == NULL)
    {
		flog_usr(FLOG_ERROR, FL_ERR_BAD_VALUE, me,
				 "Null pointer passed");
		return(false);
    }
	
    while((*cur_ptr != 0x00) && (inv_char == false))
    {
		// End of line? Should only be newline, but test for CR as well
		if((*cur_ptr == '\n') || (*cur_ptr == '\r'))
			break;
		
		if(isdigit(*cur_ptr) == 0)
		{
			inv_char = true;
			break;
		}
		valid_chars++;
		cur_ptr++;
    }
	
    if((valid_chars > 0) && (inv_char == false))
		return(true);
    else
		return(false);
}
