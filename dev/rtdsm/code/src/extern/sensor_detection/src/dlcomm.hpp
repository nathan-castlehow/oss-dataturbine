/* 
This header contains all the function headers for communicating with
the datalogger.  There are also a few constants that describe what state
the datalogger is in.
*/

#ifndef _DLCOMM_H_
#define _DLCOMM_H_

#include <assert.h>

#define RESPONSE_TIMEOUT 10000	//how long we poll for (msec) before quitting

//used for checking for responses from datalogger
#define NO_RESPONSE     0x00	//no response from datalogger
#define TELECOM_MODE    0x01	//communicating in telecom mode
#define KEYBOARD_MODE   0x02	//communicating in keyboard mode
#define LOAD_ASCII_MODE 0x04	//load ASCII program in keyboard mode
#define COMMAND_MODE    0x08	//in the middle of a command in keyboard mode

//The 
typedef struct FSA_Status_s {
    int DSP_location;	//the current DSP location
    int num_filled_FSA;	//number of filled FSA locations
    int version;	//version of datalogger
    int FSA_area;	//the FSA area number
    int MPTR_location;	//the current location of MPTR
    int err_1;
    int err_2;
    int err_3;
    int memory;		//total memory in CR510
    float  battery_voltage;//voltage of the lithium battery
    int checksum;
} FSA_Status;

// Some forward decls.
struct Cdl_Program_s;

// establish a connection with the datalogger
void init_conn(int fd);

// wake up datalogger, and put it in telecom mode
// return 0 on success, -1 on failure
int wake_up(int fd);

// Read from datalogger until the datalogger returns a response
// Input the mode(s) we expect to be in
// Return the mode we are in, if it is specified in mode; otherwise,
// return NO_RESPONSE (0)
int wait_for_response(int fd, int mode);

// Send a command to the datalogger
void send_command(int fd, const char *command);

// Load a program to into the datalogger and compile it
int load_program(int fd, const char *program);
int load_Program(int fd, int table_num, const struct Cdl_Program_s *const);

// Load a blank program and return to datalogger
// *D Mode, Command 7
int load_blank_program(int fd);

// Allocate memory in datalogger with *A command
// input = number of input storage locations
// inter = number of intermediate store locations
// fsa2 = Final Storage Area 2 Locations
// fsa1 = FSA 1 - Automatically altered when above is changed
// bytes = number of bytes allocated, 0 assigns the exact number needed
int allocate_memory(int fd, char *input, char *inter, char *fsa2, char *fsa1, char *bytes);

const struct Cdl_Array_s *
decode_binary_dump(const char *buf, int len, int *ret_len);

FSA_Status
get_fsa_status(int fd);

int
set_mptr(int fd, int mptr);

void
get_binary_dump(int fd, int dump_size, char *ret_dump);

void remove_spaces(char *const);

char * strdupe(const char *const s);

#endif



// vim: set sw=4 sts=4 expandtab ai:
