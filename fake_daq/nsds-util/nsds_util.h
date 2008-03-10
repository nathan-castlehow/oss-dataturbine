/*!
@file nsds_util.h
@date 9/5/02
@brief Header file for the NSDS driver util routines

Types, prototypes and defines for common code used by both driver and
fake_daq.

*/

#if !defined(NSDS_UTIL_H_)

//! read-once define
#define NSDS_UTIL_H_


#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <stdint.h>
#include <stdbool.h>
#include <time.h>
#include <getopt.h>

//! Hack - prototype pthread_setconcurrency since gcc doesnt have same
int pthread_setconcurrency(int new_level);

// ---------------------------------------------------------------------
//! Possible returns on wait-on-socket, simple tristate
typedef enum
{
    SWAIT_SYS_ERR,
    SWAIT_TIMEOUT,
    SWAIT_GOT_DATA
} socket_wait_ret;


//! Max length of a command on the control channel, see tcp_nl_read
#define MAX_CMD_LEN 1024

//! Length, in chars, of ISO 8601 timestamp
#define TSTAMP_LEN 32

//! Length, in chars, of a single data point + channel ID
#define DATUM_LEN 32

//! Max number of data channels, only really affects fake_daq
#define NUM_CHANNELS 16

//! String labelling a bad or missing port argument
static const char BAD_PORT[] = "Invalid port request: bad or missing channel ID";

//! New error string for invalid open-port and close-port commands
static const char BAD_OPEN_CLOSE[] = "Invalid port '%s'";

//! Valid delimiter characters for data stream
static const char DATA_BUF_DELIM_CHARS[] = "\t\n\r";

//! Delimiter chars for commands, add space
static const char CMD_BUF_DELIM_CHARS[] = " \n\r\t";

/* Function prototypes*/
int tcp_socket_make(const uint16_t SRV_PORT, const int QUEUE_LENGTH, const char *HOST_NAME);
int tcp_nl_write(const int socket, const char *buf);
int tcp_nl_read(const uint16_t socket, char *buf, const time_t timeout);
int tcp_connect(const char *server, const uint16_t port);
socket_wait_ret tcp_socket_wait(const int socket, const time_t timeout);
char * tcp_peername(const int socket);
int tcp_connect_retry(const char *host, const uint16_t port,
		      const time_t retry_delay, const time_t end_time,
		      const char *description);
void tcp_close(const int socket_fd);
char *gen_timestamp(void);
void print_args(const struct option *long_opts);
bool chan_id_valid(const char *chan_id);
#endif
