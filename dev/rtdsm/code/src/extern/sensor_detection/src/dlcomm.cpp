/*
Implementation of datalogger communicate functions
*/

#include <regex.h>
#include <string.h>
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <poll.h>
#include <termios.h>
#include <errno.h>
#include <ctype.h>
#include "dlcomm.hpp"
#include "Program.hpp"
#include "Array.hpp"
#include "String.hpp"

// Establish a connection with the datalogger
void
init_conn(int fd)
{
    struct termios tios;
    int ec;
    int brk_value;
    
    brk_value = tcsendbreak(fd,0);
    assert(brk_value == 0);

    ec = tcgetattr(fd, &tios);
    assert(ec == 0);

    if (ec != 0) {
	perror("tcgetattr");
	abort();
    }

    cfsetispeed(&tios,B9600);
    cfsetospeed(&tios,B9600);
    tios.c_lflag &= ~(ECHO|ICANON|IEXTEN|ISIG);

    tios.c_iflag &= ~(BRKINT|ICRNL|INPCK|ISTRIP|IXON);
    // Set char size.
    tios.c_cflag &= ~CSIZE;
    tios.c_cflag |= CS8;
    // Turn off parity.
    tios.c_cflag &= ~PARENB;
    // Ignore modem control lines.  The datalogger either doesn't
    // properly set them, or we are using a crappy serial cable
    // that doesn't carry the signals.
    tios.c_cflag |= CLOCAL;
    tios.c_oflag &= ~OPOST;

    tios.c_cc[VMIN]=1;
    tios.c_cc[VTIME]=0;
    ec = tcsetattr(fd, TCSANOW, &tios);
    assert(ec == 0);
}

// Wake up the datalogger by sending a couple returns and waiting for a response
// XXX - this delays longer than it should for some reason
int
wake_up(int fd) {
  
    int ec, i;
    int ready = 0;	//will be > 0 when datalogger sends a response

    struct pollfd pfd[1];
    pfd[0].fd = fd;
    pfd[0].events = POLLIN;

    //send returns and poll for a response 10 times
    for(i = 0; i < 10 && ready <= 0; ++i) {
	ec = write(fd, "\r", 1);
	assert(ec == 1);
	ready = poll(pfd, 1, 100);  //poll for a tenth of a second
    }
    if(ready <= 0)  // the datalogger never sent a response, so return -1 since wake_up failed
	return -1;
    
    ec = write(fd, "\r", 1);
    assert(ec == 1);
    ec = wait_for_response(fd, TELECOM_MODE|KEYBOARD_MODE|COMMAND_MODE);	//wait for anything
    assert(ec != NO_RESPONSE);
    
    if(ec == KEYBOARD_MODE || ec == COMMAND_MODE) {
	send_command(fd, "*0\r");    //this puts us back in telecom mode
	ec = wait_for_response(fd, TELECOM_MODE);
	assert(ec == TELECOM_MODE);
	if(ec == NO_RESPONSE)
	    return -1;	
    }
    return 0;
}

// Send a command to the datalogger
void
send_command(int fd, const char *command) {
    int ec;
    ec = write(fd, command, strlen(command));
    assert(size_t(ec) == strlen(command));
}

// Read until either a * or > is returned or until we time out
// The * means we are in telecom mode and the > means we are in
// remote keyboard mode
int
wait_for_response(int fd, int mode) {

    char buf[50];
    int i, n,write_chr, ec;
    int rval = NO_RESPONSE;	//response value from datalogger	
    
    //set up structure for polling datalogger
    struct pollfd pfd[1];
    pfd[0].fd = fd;
    pfd[0].events = POLLIN;
    
    //Read in a loop and sleep for a tenth of a second between each read
    //So, after 100 reads or 10 seconds we will return with a NO_RESPONSE value
    while((mode & rval) == 0) {
	//poll the datalogger	
	ec = poll(pfd, 1, RESPONSE_TIMEOUT);
        // If there is no response (ec == 0) from datalogger breakout of loop.
        if (ec == 0) {
            break;
        } else if (ec < 0) {
            if (errno == EINTR) {
                continue;
            } else {
                // Unknown, unrecoverable error.
                perror("poll");
                assert(0);
                abort();
            }
        }

	n = read(fd, buf, 50);
	assert(n > 0);

        // Make unprintable characters be #, so they don't mess
        // up the terminal.
        for (i = 0; i < n; i++) {
            if (!isprint(buf[i]) && !isspace(buf[i])) {
                buf[i] = '#';
            }
        }
	
	write_chr = write(1, buf, n);
	assert(write_chr == n);

	if(buf[n-1] == '*' && (mode&TELECOM_MODE) != 0)		// ready for command in telecom mode
	    rval = TELECOM_MODE;
	else if(buf[n-1] == '>' && (mode&KEYBOARD_MODE) != 0)	// remote keyboard ready for command
	    rval = KEYBOARD_MODE;
	else if(buf[n-1] == '<' && (mode&LOAD_ASCII_MODE) != 0)	// remote keyboard ready for ASCII program
	    rval = LOAD_ASCII_MODE;
	else if(buf[n-1] == ' ' && (mode&COMMAND_MODE) != 0)    // in the middle of a command in keyboard mode  
	    rval = COMMAND_MODE;				// XXX - I'm not sure if this is a correct assumption, needs to be checked
    }
    return rval;
}

// Load a program to into the datalogger and compile it
int
load_program(int fd, const char *program) {
    
    int ec;
    
    //wake up datalogger for communication
    ec = wake_up(fd);
    assert(ec == 0);

    // Switching to keyboard mode of datalogger	
    send_command(fd, "7H\r");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);
    //printf("Entered remote keyboard mode\n");

    // Using the *D option 2 to load program in ASCII text format	i
    send_command(fd, "*D2\r");	
    ec = wait_for_response(fd, LOAD_ASCII_MODE);
    assert(ec == LOAD_ASCII_MODE);
    //printf("Datalogger is ready to load ASCII Program\n");
    
    //Type in the program
    send_command(fd, program);

    // compile the program by sending Ctrl E, Ctrl E 
    send_command(fd, "\005\005");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);

    // Return to telecom mode
    send_command(fd, "*0\r");
    ec = wait_for_response(fd, TELECOM_MODE);
    assert(ec == TELECOM_MODE);

    return 0;
}

// Load a program to into the datalogger and compile it.
int
load_Program(int fd, int table_num, const Cdl_Program *prog) {
    
    int ec;

    // Generate the string.
    
    //wake up datalogger for communication
    ec = wake_up(fd);
    assert(ec == 0);

    // Switching to keyboard mode of datalogger	
    send_command(fd, "7H\r");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);
    //printf("Entered remote keyboard mode\n");

    // Using the *D option 2 to load program in ASCII text format	i
    send_command(fd, "*D2\r");	
    ec = wait_for_response(fd, LOAD_ASCII_MODE);
    assert(ec == LOAD_ASCII_MODE);
    //printf("Datalogger is ready to load ASCII Program\n");

    ///////////////////////////////////////////
    /*
    for (int i = 0; i < 10; i++) {
        Cdl_String s;
        Cdl_String__ctor(&s);
        assert(table_num == 1 || table_num == 2);
        Cdl_String__fcatl(&s, "MODE %d", table_num);
        Cdl_String s2 = Cdl_Program__String(prog);
        Cdl_String__cat(&s, s2.str);
        Cdl_String__dtor(&s2);
        Cdl_String__dtor(&s);
    }
    */
    ///////////////////////////////////////////

    Cdl_String s;
    Cdl_String__ctor(&s);
    assert(table_num == 1 || table_num == 2);
    Cdl_String__fcatl(&s, "MODE %d", table_num);
    Cdl_String s2 = Cdl_Program__String(prog);
    Cdl_String__cat(&s, s2.str);
    Cdl_String__dtor(&s2);

    // XXX - Bigger than this seems to require chunking. -kec
    assert(s.length < 1500);
    
    /*
    printf("\n======\n");
    printf("\nProg:\n%s\n", s.str);
    printf("\n======\n");
    //Type in the program
    send_command(fd, s.str);
    */

    char str[] =
    "MODE 1\r"
    "SCAN RATE 5\r"
    "1:P17\r"
    "1:0\r"
    "2:P70\r"
    "1:1\r"
    "2:0\r";
    send_command(fd, str);

    Cdl_String__dtor(&s);

    // compile the program by sending Ctrl E, Ctrl E 
    send_command(fd, "\005\005");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);

    // Return to telecom mode
    send_command(fd, "*0\r");
    ec = wait_for_response(fd, TELECOM_MODE);
    assert(ec == TELECOM_MODE);

    return 0;
}

//Load a blank program and return to datalogger
//This is the *D Mode w/ command 7
//Wake up datalogger, enter command, then return to telecom_mode
int
load_blank_program(int fd) {

    int ec;

    //wake up datalogger for communication
    ec = wake_up(fd);
    assert(ec == 0);

     // Switching to keyboard mode of datalogger	
    send_command(fd, "7H\r");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);

    // Clearing the program memory by loading a blank program
    send_command(fd, "*D7\r0\r");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);

    // Return to telecom mode
    send_command(fd, "*0\r");
    ec = wait_for_response(fd, TELECOM_MODE);
    assert(ec == TELECOM_MODE);

    return 0;
}

// Allocate memory in datalogger with *A command
// input = number of input storage locations
// inter = number of intermediate store locations
// fsa2 = Final Storage Area 2 Locations
// fsa1 = FSA 1 - Automatically altered when above is changed
// bytes = number of bytes allocated, 0 assigns the exact number needed
int
allocate_memory(int fd, char *input, char *inter, char *fsa2, char *fsa1, char *bytes) {

    int ec;
    char buf[10];

    //wake up datalogger for communication
    ec = wake_up(fd);
    assert(ec == 0);

     // Switching to keyboard mode of datalogger	
    send_command(fd, "7H\r");
    ec = wait_for_response(fd, KEYBOARD_MODE);
    assert(ec == KEYBOARD_MODE);

    // Enter allocated memory commands one step at a time
    // Go into memory allocation mode
    send_command(fd, "*A");
    ec = wait_for_response(fd, COMMAND_MODE);
    assert(ec == COMMAND_MODE);
    // Enter the number of input locations
    send_command(fd, input); 
    ec = wait_for_response(fd, COMMAND_MODE);
    assert(ec == COMMAND_MODE);
    // Enter the number of intermediate locations
    send_command(fd, inter);
    ec = wait_for_response(fd, COMMAND_MODE);
    assert(ec == COMMAND_MODE);
    // Enter the number of fsa2 locations
    send_command(fd, fsa2); 
    ec = wait_for_response(fd, COMMAND_MODE);
    assert(ec == COMMAND_MODE);
    // Enter the number of fsa1 locations
    send_command(fd, fsa1);
    ec = wait_for_response(fd, COMMAND_MODE);
    assert(ec == COMMAND_MODE);
    // Enter the number of bytes allocated
    send_command(fd, bytes);
    ec = wait_for_response(fd, COMMAND_MODE);
    assert(ec == COMMAND_MODE);

    // Return to telecom mode
    send_command(fd, "*0\r");
    ec = wait_for_response(fd, TELECOM_MODE);
    assert(ec == TELECOM_MODE);

    return 0;
}

const Cdl_Array *
decode_binary_dump(const char *dp, int len, int *arrays_len) {

    assert(arrays_len != 0);

    int n_arrays = 0;
    int arrays_capacity = 10;
    Cdl_Array *arrays = (Cdl_Array *) malloc(arrays_capacity*sizeof(Cdl_Array));

    // Use char * for the interface, just to make it easier to use.  But
    // internally, we want to treat everything as unsigned. -kec
    unsigned char *data = (unsigned char *) dp;

    enum State {
        // Make 0 invalid, just to help guard against uninitialized states.
        ST_INVALID,
        ST_START,
        ST_ARRAY_ID_SECOND_BYTE,
        ST_LO_RES_SECOND_BYTE,
        ST_HI_RES_SECOND_BYTE,
        ST_HI_RES_THIRD_BYTE,
        ST_HI_RES_FOURTH_BYTE,
        ST_STRING_CR,
        ST_STRING_LF,
        ST_STRING_CHAR,
        ST_STRING_SECOND_FF,
        ST_STRING_SECOND_HASH,
        ST_STRING_THIRD_HASH,
        ST_STRING_H,
        ST_STRING_END,
    };

    enum State state = ST_START;

    unsigned int
     array_id_first_byte,
     lo_res_first_byte,
     hi_res_first_byte,
     hi_res_second_byte,
     hi_res_third_byte;

    char str[1024];
    unsigned int str_len;
    // Initialize to NULL so that we can detect if we get any data before
    // the start of the first array, since we currently are not handling
    // that.
    Cdl_Array *cur_array = 0;
    
    unsigned char checksum_S1 = 0xaa, checksum_S0 = 0xaa;

    for (int i = 0; i < len - 2; i++) {

        int c = data[i];

        // Update checksum.
        {
            unsigned char S0_n = checksum_S0;
            unsigned char S1_n = checksum_S1;
            unsigned char S0_n1, S1_n1;
            unsigned char T1, T2;
            unsigned char M_n1 = c;

            T1 = S1_n;
            S1_n1 = S0_n;
            // Rotate left 1 bit. -kec
            T2 = (S0_n << 1) | ((0x80&S0_n) >> 7);
            S0_n1 = T2 + S1_n + M_n1;

            checksum_S0 = S0_n1;
            checksum_S1 = S1_n1;
        }

        switch (state) {

            case ST_START:

                // If start of string.
                if (c == 0x7d) {

                    state = ST_STRING_CR;

                // Check if lo-res format, which is when D,E,F not all ones.
                // 0x1c == D,E,F all ones.
                } else if ((0x1c&c) != 0x1c) { 

                    lo_res_first_byte = c;
                    state = ST_LO_RES_SECOND_BYTE;

                // Else, if D,E,F are all ones.
                } else {

                    // Start of output array.
                    if ((0xe0&c) == 0xe0) {

                        array_id_first_byte = c;
                        state = ST_ARRAY_ID_SECOND_BYTE;

                    // First byte of 4-byte value.
                    } else if ((0x20&c) == 0x00) {
                    
                        hi_res_first_byte = c;
                        state = ST_HI_RES_SECOND_BYTE;

                    } else {

                        assert(0);
                        abort();
                    }

                }
                break;

            case ST_ARRAY_ID_SECOND_BYTE:
                {
                    int id = ((0x03&array_id_first_byte) << 8) | c;

                    printf("Decoded array id: %d\n", id);

                    // Expand arrays if necessary.
                    if (n_arrays == arrays_capacity) {
                        arrays_capacity *= 2;
                        arrays = (Cdl_Array *) realloc(arrays,
                         arrays_capacity*sizeof(Cdl_Array));
                    }
                    cur_array = &arrays[n_arrays++];
                    Cdl_Array__ctor(cur_array, id);

                    state = ST_START;
                }

                break;

            case ST_LO_RES_SECOND_BYTE:
                {
                    int sign;

                    // Sign bit is high bit.
                    if (0x80&lo_res_first_byte) {
                        sign = -1;
                    } else {
                        sign = 1;
                    }

                    // Decimal locator.  We could use a divide by 10 loop, or
                    // something like that to make this neater, but dividing by
                    // 10 in a loop might introduce some rounding errors.  I'm
                    // not sure if they would be significant or not, though.
                    // -kec
                    double multiplier;
                    switch ((0x60&lo_res_first_byte) >> 5) {
                        case 0:
                            multiplier = 1;
                            break;
                        case 1:
                            multiplier = 0.1;
                            break;
                        case 2:
                            multiplier = 0.01;
                            break;
                        case 3:
                            multiplier = 0.001;
                            break;
                        default:
                            assert(0);
                            abort();
                    }
                                
                    int i = sign*((0x1f&lo_res_first_byte << 8) | c);
                    double value = multiplier*(double)i;

                    printf("Decoded lo-res: %f\n", value);
		    if (cur_array != 0) {
			Cdl_Array__append(cur_array,
			 Cdl_Element__make_lo_res(value));
		    } else {
			fprintf(stderr, "WARNING: Data before first array discarded.\n");
		    }

                    state = ST_START;
                }
                break;


            case ST_HI_RES_SECOND_BYTE:
               {
                   hi_res_second_byte = c;				
                   state = ST_HI_RES_THIRD_BYTE;
               }
               break;
               
            case ST_HI_RES_THIRD_BYTE:
                {
                    hi_res_third_byte = c;
                    state = ST_HI_RES_FOURTH_BYTE;
                }
                break;

            case ST_HI_RES_FOURTH_BYTE:
                {
                    int sign;
                    if((hi_res_first_byte&0x40) == 0)
                        sign = 1;
                    else 
                        sign = -1;
                                
                    // Same issues apply here as to the lo res case about
                    // putting this in a loop, or using a y^x function of some
                    // kind. -kec
                    double multiplier = 0;
                    int decimal_locator = 
                     ((0x03&hi_res_first_byte) << 1)
                     | ((0x80&hi_res_first_byte) >> 7);
                    switch (decimal_locator) {
                        case 0:
                            multiplier = 1;
                            break;
                        case 1:
                            multiplier = 0.1;
                            break;
                        case 2:
                            multiplier = 0.01;
                            break;
                        case 3:
                            multiplier = 0.001;
                            break;
                        case 4:
                            multiplier = 0.0001;
                            break;
                        case 5:
                            multiplier = 0.00001;															
                            break;
                        default:
                            assert(0);
                            abort();
                            break;
                    }

                    int i = sign*(((0x01&hi_res_third_byte) << 16)
                     | (hi_res_second_byte << 8) | c);

                    double value = multiplier*(double) i;

                    printf("Decoded hi res: %f\n", value);
		    if (cur_array != 0) {
			Cdl_Array__append(cur_array,
			 Cdl_Element__make_hi_res(value));
		    } else {
			fprintf(stderr, "WARNING: Data before first array discarded.\n");
		    }

                    state = ST_START;
                }
                break;	

            case ST_STRING_CR:
                {
                    if (c == 0x0d) {
                        state = ST_STRING_LF;
                    } else {
                        assert(0);
                        abort();
                    }
                }
                break;

            case ST_STRING_LF:
                {
                    if (c == 0x0a) {
                        str_len = 0;
                        state = ST_STRING_CHAR;
                    } else {
                        assert(0);
                        abort();
                    }
                }
                break;

            case ST_STRING_CHAR:
                {
                    if (c == 0xff) {
			if ((i + 1) % 2 == 1) {
			    state = ST_STRING_SECOND_FF;
			} else {
			    str[str_len] = '\0';
			    printf("Decoded string: \"%s\".\n", str);
			    Cdl_Array__append(cur_array,
			     Cdl_Element__make_string(str));
			    state = ST_START;
			}
		    // This is to deal with the strange format on CR23X.
                    } else if (c == '#') {
			state = ST_STRING_SECOND_HASH;
                    } else {
                        if (!(str_len + 1 < sizeof str)) {
                            assert(0);
                            abort();
                        }
                        str[str_len++] = c;
                        state = ST_STRING_CHAR;
                    } 
                }
                break;

            case ST_STRING_SECOND_FF:
                {
		    printf("pos: %d\n", i);
                    if (c == 0xff) {
			str[str_len] = '\0';
			printf("Decoded string: \"%s\".\n", str);
			if (cur_array != 0) {
			    Cdl_Array__append(cur_array,
			     Cdl_Element__make_string(str));
			} else {
			    fprintf(stderr, "WARNING: Data before first array discarded.\n");
			}
			state = ST_START;
                    } else {
                        assert(0);
                        abort();
                    } 
                }
                break;

	    case ST_STRING_SECOND_HASH:
		{
		    if (c == '#') {
			state = ST_STRING_H;
		    } else {
			assert(0);
			abort();
		    }
		}
		break;
	    case ST_STRING_H:
		{
		    if (c == 'H') {
			state = ST_STRING_THIRD_HASH;
		    } else {
			assert(0);
			abort();
		    }
		}
		break;
	    case ST_STRING_THIRD_HASH:
		{
		    if (c == '#') {
			str[str_len] = '\0';
			printf("Decoded string: \"%s\".\n", str);
			Cdl_Array__append(cur_array,
			 Cdl_Element__make_string(str));
			state = ST_START;
		    } else {
			assert(0);
			abort();
		    }
		}
		break;

            default:
                {
                    assert(0);
                    abort();
                }
                break;
        }
    }

    // Verify checksum.
    /*
    printf("received: %04x, computed: %04x\n",
     (data[len - 2] << 8) | data[len-1],
     (checksum_S1 << 8) | checksum_S0);
    */
    if (checksum_S1 != data[len - 2] || checksum_S0 != data[len - 1]) {
        fprintf(stderr, "Checksum mismatch.\n");
        for (int i = 0; i < len; i++) {
            printf("%d: %d, %c\n",
             i, (int) dp[i], isprint(dp[i]) ? dp[i] : '#');
        }
        abort();
    }

    *arrays_len = n_arrays;
    return arrays;
}

FSA_Status
get_fsa_status(int fd) {
    
    char buf[100];
    char status[100];
    int ec, n, mode;


    buf[0] = '\0';
    status[0] = '\0';

    //set up structure for polling datalogger
    struct pollfd pfd[1];
    pfd[0].fd = fd;
    pfd[0].events = POLLIN;

    FSA_Status fsa_status;
    
    // Given the line below, and the points on it
    //    A B   C D   E F G H I J   K L  M N O P Q R    S T       U V 
    //      R+1.  F+0.  V5  A1  L+1.  E00  00  00  M2688  B+3.0591  C2656
    // the regular expression below matches it as indicated.
    // I added .* on the front and the end since the datalogger echos A and
    // it returns a * when the datalogger is ready for more commands
    const char expr_cr510[] =
     "^"
     ".*"
     "[[:space:]]*"             // A
     "R\\+([[:digit:]]+)\\."    // B
     "[[:space:]]+"             // C
     "F\\+([[:digit:]]+)\\."    // D
     "[[:space:]]+"             // E
     "V([[:digit:]])+"          // F
     "[[:space:]]+"             // G
     "A([[:digit:]])+"          // H
     "[[:space:]]+"             // I
     "L\\+([[:digit:]]+)\\."    // J
     "[[:space:]]+"             // K
     "E([[:digit:]]+)"          // L
     "[[:space:]]+"             // M
     "([[:digit:]]+)"           // N
     "[[:space:]]+"             // O
     "([[:digit:]]+)"           // P
     "[[:space:]]+"             // Q
     "M([[:digit:]]+)"          // R
     "[[:space:]]+"             // S
     "B\\+([[:digit:]]*\\.[[:digit:]]*)" // T
     "[[:space:]]+"             // U
     "C([[:digit:]]+)"          // V
     "[[:space:]]*"             // Y
     ".*"
     "$" 
     ;

    // XXX - Fix this comment later.  It doesn't apply to this
    // regex.
    //
    // Given the line below, and the points on it
    //    A B   C D   E F G H I J   K L  M N O P Q R    S T       U V 
    //      R+1.  F+0.  V5  A1  L+1.  E00  00  00  M2688  B+3.0591  C2656
    // the regular expression below matches it as indicated.
    // I added .* on the front and the end since the datalogger echos A and
    // it returns a * when the datalogger is ready for more commands
    const char expr_cr23x[] =
     "^"
     ".*"
     "[[:space:]]*"             // A
     "R\\+([[:digit:]]+)\\."    // B
     "[[:space:]]+"             // C
     "F\\+([[:digit:]]+)\\."    // D
     "[[:space:]]+"             // E
     "V([[:digit:]])+"          // F
     "[[:space:]]+"             // G
     "A([[:digit:]])+"          // H
     "[[:space:]]+"             // I
     "L\\+([[:digit:]]+)\\."    // J
     "[[:space:]]+"             // K
     "E([[:digit:]]+)"          // L
     "[[:space:]]+"             // M
     "([[:digit:]]+)"           // N
     "[[:space:]]+"             // O
     "([[:digit:]]+)"           // P
     "[[:space:]]+"             // Q
     "([[:digit:]]+)"           // P'
     "[[:space:]]+"             // Q'
     "M([[:digit:]]+)"          // R
     "[[:space:]]+"             // S
     "B\\+([[:digit:]]*\\.[[:digit:]]*)" // T
     "[[:space:]]+"             // U
     "C([[:digit:]]+)"          // V
     "[[:space:]]*"             // Y
     ".*"
     "$" 
     ;

    //Isse the A command (Select Area/Status)
    send_command(fd, "A\r");

    //Keep polling datalogger until we have the entire response
    //for the A command put together
    while(1) {

	//poll the datalogger	
	ec = poll(pfd, 1, RESPONSE_TIMEOUT);
        // If there is no response (ec == 0) from datalogger breakout of loop.
        if (ec == 0) {
            break;
        } else if (ec < 0) {
            if (errno == EINTR) {
                continue;
            } else {
                // Unknown, unrecoverable error.
                perror("poll");
                assert(0);
                abort();
            }
        }

	n = read(fd, buf, 100);
	assert(n > 0);
	//append buf to status, so we can get the entire A command response
	buf[n] = '\0';
	strcat(status, buf);

	if(buf[n-1] == '*') 	// ready for command in telecom mode
	    break;
    }

    // Use the regular expression to parse the data from the A command

    static int initialized = 0;
    static regex_t re_cr510, re_cr23x;
    if (!initialized) {
	ec = regcomp(&re_cr510, expr_cr510, REG_EXTENDED);
	assert(ec == 0);
	ec = regcomp(&re_cr23x, expr_cr23x, REG_EXTENDED);
	assert(ec == 0);
	initialized = 1;
    }

    regmatch_t pm[20];
    char c;

    //printf("Result of A command: %s\n", status);
    ec = regexec(&re_cr510, status, 20, pm, 0);
    if (ec == REG_NOMATCH) {
	// printf("A output no match CR510.\n");
    } else if (ec == 0) {
	// printf("A output matched CR510.\n");

	//Fill fsa_status with the parsed data
	//DSP_location
	c = status[pm[1].rm_eo];
	status[pm[1].rm_eo] = '\0';
	fsa_status.DSP_location = atoi(status + pm[1].rm_so);
	status[pm[1].rm_eo] = c;
	//num_filled_FSA
	c = status[pm[2].rm_eo];
	status[pm[2].rm_eo] = '\0';
	fsa_status.num_filled_FSA = atoi(status + pm[2].rm_so);
	status[pm[2].rm_eo] = c;
	//version
	c = status[pm[3].rm_eo];
	status[pm[3].rm_eo] = '\0';
	fsa_status.version = atoi(status + pm[3].rm_so);
	status[pm[3].rm_eo] = c;
	//FSA_area
	c = status[pm[4].rm_eo];
	status[pm[4].rm_eo] = '\0';
	fsa_status.FSA_area = atoi(status + pm[4].rm_so);
	status[pm[4].rm_eo] = c;
	//MPTR_location
	c = status[pm[5].rm_eo];
	status[pm[5].rm_eo] = '\0';
	fsa_status.MPTR_location = atoi(status + pm[5].rm_so);
	status[pm[5].rm_eo] = c;
	//err_1
	c = status[pm[6].rm_eo];
	status[pm[6].rm_eo] = '\0';
	fsa_status.err_1 = atoi(status + pm[6].rm_so);
	status[pm[6].rm_eo] = c;
	//err_2
	c = status[pm[7].rm_eo];
	status[pm[7].rm_eo] = '\0';
	fsa_status.err_2 = atoi(status + pm[7].rm_so);
	status[pm[7].rm_eo] = c;
	//err_3
	c = status[pm[8].rm_eo];
	status[pm[8].rm_eo] = '\0';
	fsa_status.err_3 = atoi(status + pm[8].rm_so);
	status[pm[8].rm_eo] = c;
	//memory
	c = status[pm[9].rm_eo];
	status[pm[9].rm_eo] = '\0';
	fsa_status.memory = atoi(status + pm[9].rm_so);
	status[pm[9].rm_eo] = c;
	//battery_voltage
	c = status[pm[10].rm_eo];
	status[pm[10].rm_eo] = '\0';
	fsa_status.battery_voltage = atof(status + pm[10].rm_so);
	status[pm[10].rm_eo] = c;
	//checksum
	c = status[pm[11].rm_eo];
	status[pm[11].rm_eo] = '\0';
	fsa_status.checksum = atoi(status + pm[11].rm_so);
	status[pm[11].rm_eo] = c;

    } else {
	abort();
    }
    ec = regexec(&re_cr23x, status, 20, pm, 0);
    if (ec == REG_NOMATCH) {
	// printf("A output no match CR23X.\n");
    } else if (ec == 0) {
	// printf("A output matched CR23X.\n");

	//Fill fsa_status with the parsed data
	//DSP_location
	c = status[pm[1].rm_eo];
	status[pm[1].rm_eo] = '\0';
	fsa_status.DSP_location = atoi(status + pm[1].rm_so);
	status[pm[1].rm_eo] = c;
	//num_filled_FSA
	c = status[pm[2].rm_eo];
	status[pm[2].rm_eo] = '\0';
	fsa_status.num_filled_FSA = atoi(status + pm[2].rm_so);
	status[pm[2].rm_eo] = c;
	//version
	c = status[pm[3].rm_eo];
	status[pm[3].rm_eo] = '\0';
	fsa_status.version = atoi(status + pm[3].rm_so);
	status[pm[3].rm_eo] = c;
	//FSA_area
	c = status[pm[4].rm_eo];
	status[pm[4].rm_eo] = '\0';
	fsa_status.FSA_area = atoi(status + pm[4].rm_so);
	status[pm[4].rm_eo] = c;
	//MPTR_location
	c = status[pm[5].rm_eo];
	status[pm[5].rm_eo] = '\0';
	fsa_status.MPTR_location = atoi(status + pm[5].rm_so);
	status[pm[5].rm_eo] = c;
	//err_1
	c = status[pm[6].rm_eo];
	status[pm[6].rm_eo] = '\0';
	fsa_status.err_1 = atoi(status + pm[6].rm_so);
	status[pm[6].rm_eo] = c;
	//err_2
	c = status[pm[7].rm_eo];
	status[pm[7].rm_eo] = '\0';
	fsa_status.err_2 = atoi(status + pm[7].rm_so);
	status[pm[7].rm_eo] = c;
	//err_3
	c = status[pm[8].rm_eo];
	status[pm[8].rm_eo] = '\0';
	fsa_status.err_3 = atoi(status + pm[8].rm_so);
	status[pm[8].rm_eo] = c;
	// XXX - Skipping error 4
	//memory
	c = status[pm[10].rm_eo];
	status[pm[10].rm_eo] = '\0';
	fsa_status.memory = atoi(status + pm[9].rm_so);
	status[pm[10].rm_eo] = c;
	//battery_voltage
	c = status[pm[11].rm_eo];
	status[pm[11].rm_eo] = '\0';
	fsa_status.battery_voltage = atof(status + pm[10].rm_so);
	status[pm[11].rm_eo] = c;
	//checksum
	c = status[pm[12].rm_eo];
	status[pm[12].rm_eo] = '\0';
	fsa_status.checksum = atoi(status + pm[11].rm_so);
	status[pm[12].rm_eo] = c;

    } else {
	abort();
    }
    
    return fsa_status;
}

int
set_mptr(int fd, int mptr) {
    
    int ec, n;
    char command[15];
    char mptr_buf[10];
    
    command[0] = '\0';
    sprintf(mptr_buf, "%d", mptr);

    strcat(command, mptr_buf);
    strcat(command, "G\r");
   
    //Issue the G command (Set MPTR)
    send_command(fd, command);	
    ec = wait_for_response(fd, TELECOM_MODE);
    assert(ec == TELECOM_MODE);

    return 0;
}

void
get_binary_dump(int fd, int dump_size, char *ret_dump) {

    int ec, n, i;
    char command[15];
    char dump_size_buf[10];
    char dump[dump_size*2 + 15];
    char buf[75];
    //this is the string that is returned after parsing
    //char ret_dump[dump_size*2 + 2]; 
    
    for(i = 0; i < dump_size*2 + 15; ++i) {
	dump[i]=100;
    }
    //set up structure for polling datalogger
    struct pollfd pfd[1];
    pfd[0].fd = fd;
    pfd[0].events = POLLIN;

    dump[0] = '\0';
    command[0] = '\0';
    sprintf(dump_size_buf, "%d", dump_size);

    strcat(command, dump_size_buf);
    strcat(command, "F\r\r");
   
    //Issue the F command (Binary dump)
    send_command(fd, command);	

    int char_loc = 0;   //where the first character from buf should be put in dump
    while(1) {

	//poll the datalogger	
	ec = poll(pfd, 1, RESPONSE_TIMEOUT);
        // If there is no response (ec == 0) from datalogger breakout of loop.
        if (ec == 0) {
            break;
        } else if (ec < 0) {
            if (errno == EINTR) {
                continue;
            } else {
                // Unknown, unrecoverable error.
                perror("poll");
                assert(0);
                abort();
            }
        }

	n = read(fd, buf, 75);
	assert(n > 0);

	// We copy from buf to dump this way because we have to
	// watch out for bytes of value 0.
	for (i = 0; i < n; ++i)	{
	    dump[char_loc + i] = buf[i];
	}
	char_loc += n;
	    
//	if(buf[n-1] == '*') 	// ready for command in telecom mode
//	    break;


// as binary dump is 2*FSA locations long so if we have read that
// many characters means we have completely read the FSA. We need to read the extra characters
// equal to length of command(Binary Dump command we sent) as they are echoed back to us and not part of FSA,
// two bytes which are the checksum after end of dump,CR,LF and then one more so that we are at '*'. So total
// we read 5 extra bytes,twice dump size locations and length of command locations.


	  int ending_point = 2*dump_size + 5 + strlen(command); 
	  if(char_loc == ending_point) {
		if(buf[n-1] == '*') 	// means we got a '*' as we should have at end of FSA
		  break;				
		else {
			  fprintf(stderr,"Expected '*' at end of FSA but did not get '*' ... Error in reading FSA \n");
			  // clean up
		}
	   }




    }
    // assert(char_loc == dump_size*2 + 2 + strlen);
    printf("char_loc: %d, dump_size = %d\n", char_loc, dump_size);
    //parse off the command from the beginning of the response
    //XXX - This could cause a problem if we get more than the
    //      initial command echoed back to us.  It should not
    //      be a problem for now though.
    for (i = 0; i < dump_size*2 + 2; ++i) {
	ret_dump[i] = dump[i + strlen(command)];
    }
    //return ret_dump;
}

#if 0
void
get_binary_dump(int fd, int dump_size, char *ret_dump) {

    int ec, n, i;
    char command[15];
    char dump_size_buf[10];
    char dump[dump_size*2 + 15];
    char buf[75];
    //this is the string that is returned after parsing
    //char ret_dump[dump_size*2 + 2]; 
    
    for(i = 0; i < dump_size*2 + 15; ++i) {
	dump[i]=100;
    }
    //set up structure for polling datalogger
    struct pollfd pfd[1];
    pfd[0].fd = fd;
    pfd[0].events = POLLIN;

    dump[0] = '\0';
    command[0] = '\0';
    sprintf(dump_size_buf, "%d", dump_size);

    strcat(command, dump_size_buf);
    strcat(command, "F\r\r");
   
    //Issue the F command (Binary dump)
    send_command(fd, command);	

    int char_loc = 0;   //where the first character from buf should be put in dump
    while(1) {

	//poll the datalogger	
	ec = poll(pfd, 1, RESPONSE_TIMEOUT);
        // If there is no response (ec == 0) from datalogger breakout of loop.
        if (ec == 0) {
            break;
        } else if (ec < 0) {
            if (errno == EINTR) {
                continue;
            } else {
                // Unknown, unrecoverable error.
                perror("poll");
                assert(0);
                abort();
            }
        }

	n = read(fd, buf, 75);
	assert(n > 0);

	// We copy from buf to dump this way because we have to
	// watch out for bytes of value 0.
	for (i = 0; i < n; ++i)	{
	    dump[char_loc + i] = buf[i];
	}
	char_loc += n;
	    
	if(buf[n-1] == '*') 	// ready for command in telecom mode
	    break;
    }
    // assert(char_loc == dump_size*2 + 2 + strlen);
    printf("char_loc: %d, dump_size = %d\n", char_loc, dump_size);
    //parse off the command from the beginning of the response
    //XXX - This could cause a problem if we get more than the
    //      initial command echoed back to us.  It should not
    //      be a problem for now though.
    for (i = 0; i < dump_size*2 + 2; ++i) {
	ret_dump[i] = dump[i + strlen(command)];
    }
    //return ret_dump;
}
#endif

void
remove_spaces(char *const str) {

    char *d, *s;
    d = s = str;
    while (*s != '\0') {
	if (!isspace(*s)) {
	    *d = *s;
	    d++;
	}
	s++;
    }
    *d = '\0';
}

char *
strdupe(const char *const s) {
    char *p = (char *) malloc(strlen(s) + 1);
    strcpy(p, s);
    return p;
}



// vim: set sw=4 sts=4 expandtab ai:
