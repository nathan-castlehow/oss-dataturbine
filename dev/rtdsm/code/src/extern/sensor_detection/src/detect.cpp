/*
We are opening a device(datalogger) specified as argument 1, loading a program from an array
into the datalogger. Inside the main function we write certain commands directly to the datalogger,then load the 
program from array to the datalogger. We create a reader thread which reads the output from datalogger to 
an array which is parsed and used for knowing if a sensor is connected to datalogger or not. 
We also have a loop in main function where we wait for input from standard input incase if the user 
wants to intervene manually.
*/

#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <stdio.h>
#include <assert.h>
#include <ctype.h>
#include <pthread.h>
#include <string.h>
#include <stdlib.h>
#include <termios.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <errno.h>
#include "dlcomm.hpp"
#include "Array.hpp"
#include "Program.hpp"
#include "detectsensor.hpp"

// Added by Vinay
// Start
#include "connector.h"
// End

// XXX - dbinsert.h needed.
// Altered by Vinay
// Start
// Commented the line below
// int insertData(const char *tableName, const char* values);
// End

#define MAX_ARRAY_ID	500

#define POLLING_RESULTS_ARRAY_ID 501

// Read FSA for new sensorID's
void detect_new_sensors(int fd);

// determine difference in seconds between the two timestructures(by Dr. Chiu). 
double timedifference(struct timeval *t2, struct timeval *t1);

struct timeval *start_t; /* to record start/stop times */
struct timeval *stop_t; 

// testing addresses from 1 to 10 to see if a sensor is connected to them
// or not Currently testing address 1 only 10 ten times to make sure I
// handle case when sensors are present at all address I can read all
// sensor ID's from FSA correctly
char program_ten_locations[] =
    "MODE 2\r"
    "SCAN RATE 10\r"

    // Set high-res output.
    "15:P78\r"
    "1:1\r"

    // Turn on output flag.
    "16:P86\r"
    "1:10\r"

    // Set the array ID to 1.
    "16:P80\r"
    "1:01\r"    // FSA 1
    "2:501\r"     // Output array ID

    // Record the start time.
    "17:P77\r"
    "1:0020\r"

    // Clear loc 1
    "1:P37\r"
    "1:1\r"
    "2:0\r"
    "3:1\r"

    // Loop 10 times to zero-out 10 locs, starting from 1.
    "2:P87\r"
    "1:0\r"
    "2:10\r"

    // Copy from loc 1 to 2++.
    "3:P31\r"
    "1:1\r"
    "2:2--\r"

    "4:P95\r"
    // End loop

    "5:P105\r"
    "1:0\r"     // Address 1
    "2:11\r"    // ID command
    "3:8\r"     // Control port 1
    "4:2\r"     // Input loc 2
    "5:1\r"     // Multiplier
    "6:0\r"     // Offset

    "6:P105\r"
    "1:1\r"
    "2:11\r"
    "3:8\r"
    "4:3\r"
    "5:1\r"
    "6:0\r"

    "7:P105\r"
    "1:2\r"
    "2:11\r"
    "3:8\r"
    "4:4\r"
    "5:1\r"
    "6:0\r"

    "8:P105\r"
    "1:3\r"
    "2:11\r"
    "3:8\r"
    "4:5\r"
    "5:1\r"
    "6:0\r"

    "9:P105\r"
    "1:4\r"
    "2:11\r"
    "3:8\r"
    "4:6\r"
    "5:1\r"
    "6:0\r"

    "10:P105\r"
    "1:5\r"
    "2:11\r"
    "3:8\r"
    "4:7\r"
    "5:1\r"
    "6:0\r"

    "11:P105\r"
    "1:6\r"
    "2:11\r"
    "3:8\r"
    "4:8\r"
    "5:1\r"
    "6:0\r"

    "12:P105\r"
    "1:7\r"
    "2:11\r"
    "3:8\r"
    "4:9\r"
    "5:1\r"
    "6:0\r"

    "13:P105\r"
    "1:8\r"
    "2:11\r"
    "3:8\r"
    "4:10\r"
    "5:1\r"
    "6:0\r"

    "14:P105\r"
    "1:9\r"
    "2:11\r"
    "3:8\r"
    "4:11\r"
    "5:1\r"
    "6:0\r"

    // Record the end time.
    "17:P77\r"
    "1:0020\r"

    // Sample 10 locations, starting from loc 2.
    "18:P70\r"
    "1:10\r"
    "2:2\r"
    ; 

int main(int argc, char *argv[]) {

    struct termios tios;
    int fd;
    int ec;
    int brk_value;
    int inp_file_fd;
    int chars_read;

    // to clear Table 1 and Table 2 of datalogger
    char clearing_program[] = "MODE 1 \r"
	"SCAN RATE 0 \r"
	"1:P0 \r MODE 2 \r"
	"SCAN RATE 0 \r 1:P0 \r ";


    // Allocate memory for timestructures used as timestamps
    if((start_t = (struct timeval*)malloc(sizeof(*start_t))) == NULL) {
	fprintf(stderr,"Error in allocating memory for start timeval structure\n");
	return -1;
    }

    if((stop_t = (struct timeval*)malloc(sizeof(*stop_t))) == NULL) {
	fprintf(stderr,"Error in allocating memory for stop timeval structure\n");
	free(start_t);
	return -1;
    }

    // as temporary counters 
    int j,n; 

    assert(argc == 2);

    fd = open(argv[1], O_RDWR);
    if (fd < 0) {
        int en = errno;
        fprintf(stderr, "Error opening %s: %s\n", argv[1], strerror(en));
        assert(0);
        abort();
    }

    //connect with datalogger
    init_conn(fd);   
    printf("\n...Established connection to datalogger...\n");

    //wake up datalogger so we can enter commands
    ec = wake_up(fd);	
    assert(ec == 0);
    printf("\n...Datalogger is ready for commands...\n");

    // Added by Vinay
    // Start
    printf("Initializing connector agent...\n");
    initConnectorAgent();
    printf("Done\n");
    // End
    
    //Run clearing program command
    ec = load_blank_program(fd);
    assert(ec == 0);
    printf("\n...Program 0 (empty program) loaded successfully...\n");

    /*
    I don't see why we need this. -kec
    //Load clearing program into datalogger
    ec = load_program(fd, clearing_program);
    assert(ec == 0);
    printf("\n...Clearing program loaded and compiled successfully...\n");
    */
  
    // To clear the Input,Intermediate and Final Storage Areas of values from
    // previous programs by allocating current memory in Input Storage
    // locations again which causes all values in three storage areas to be
    // zero. Current locations for Input Storage is 1499.  By going to *A mode
    // and allocating 1499 locations to Input Storage clears Input,Intermediate
    // and Final Storage Area. I understand it is not the quite correct method
    // as if anybody manually changes number of Input Locations in *5 mode to
    // any other number than 1499 and runs this program the clearing of all
    // values in all three storage areas wont work.  Ideally I should be going
    // to *5 mode knowing the number of Input Storage Locations(say N) and then
    // doing the below steps for the number N and I need to do that in future
    // after other issues are sorted out.

    // There seems to be a bug here in some versions of the datalogger.  If 0
    // is entered for intermediate storage and program storage, before we load
    // the polling program, it fails with an E04 error.  If, however, we enter
    // some value for the program storage, like 2048, then it seems to work
    // fine. It also works fine if 0 is entered after the polling program has
    // been loaded.  We think this is a bug (rather than being designed to work
    // that way), because it seemed to work differently in some other versions.
    // -kec
    ec = allocate_memory(fd, "100\r", "500\r", "0\r", "0\r", "2048\r");
    assert(ec == 0);
    printf("\n...Memory has been allocated successfully...\n");
    
    // Load ten locations program into datalogger
    ec = load_program(fd, program_ten_locations);
    assert(ec == 0);
    printf("\n...Ten locations program loaded and compiled successfully...\n");

    //and is running, start to look for new sensorID's
    detect_new_sensors(fd);

    return 0;
}


// Read from FSA to detect new sensors being attached to datalogger.
void detect_new_sensors(int fd) {
    
    int i,n,write_chr;
    int ec;
    int mptr = 1;
    int can_check_fsa = 1;
    int new_data;
    FSA_Status fsa_status;

    typedef struct Sensor_s {
	int valid;
	char *type;
	char *table_name;
  // Added by Vinay
  // Start
  SensorMetaData* smd;
  // End
    } Sensor;
    // This array is indexed by array ID - 1;
    static Sensor sensors[MAX_ARRAY_ID];
    for (int i = 0; i < MAX_ARRAY_ID; i++) {
	sensors[i].valid = 0;
	sensors[i].type = NULL;
	sensors[i].table_name = NULL;
      // Added by Vinay
      // Start
      sensors[i].smd = NULL;
      // End
    }
    int array_id = 1;

    // Initialize the sensor StatusInfo  struct
    struct StatusInfo sen_status[10];
    for (i = 0; i < 10; ++i) {
	sen_status[i].present = 0;
	sen_status[i].address = i;
    }

    METADATA *metadata;
    int flag = 1;

    //Continuously check for new data in the FSA
    while (1) {
    
	sleep(1);

	//Check the status of the Final Storage Area
	fsa_status = get_fsa_status(fd);
	
	//If the dsp and mptr are different, then we have new data in the FSA
	new_data = fsa_status.DSP_location - mptr;

	if (new_data > 0) {
	    //Manually set the mptr	
	    printf("\n*** New data in FSA ***\n");
	    printf("\nDSP is %d\n", fsa_status.DSP_location);
	    printf("\nMPTR is %d\n", mptr);
	    set_mptr(fd, mptr);
	    
	    //Get the new binary data
	    char dump[new_data*2 + 2];
	    get_binary_dump(fd, new_data, dump);
	    
	    //Decode the binary data
	    int n_arrays;
	    const Cdl_Array *arrays = decode_binary_dump(dump, new_data*2 + 2 ,
	     &n_arrays);

	    //present or not present values
	    const double pres = 0.0;
	    const double not_pres = -99999.0;
	    
	    for (int i = 0; i < n_arrays; i++) {

		const Cdl_Array *a = &arrays[i];
		printf("array id: %d, n: %d\n", a->id, a->length);
		for (int j = 0; j < a->length; j++) {
		    Cdl_Element *e = &a->elements[j];
		    switch (e->type) {
			case CDL_ET_LO_RES:
			case CDL_ET_HI_RES:
			    printf("element %d: %f\n", j, e->value.dble);
			    break;
			case CDL_ET_STRING:
			    printf("element %d: %s\n", j, e->value.strng);
			    break;
			default:
			    assert(0);
			    abort();
		    }
		}
	
		if (a->id == POLLING_RESULTS_ARRAY_ID) {

		    // If truncated array, just ignore it.
		    // XXX - Length should not be hard-coded.
		    if (a->length < 12) {
			fprintf(stderr, "WARNING: Truncated array.\n");
			continue;
		    }

		    Cdl_Element *e = &a->elements[0];
		    if (e->type != CDL_ET_LO_RES) {
			fprintf(stderr, "WARNING: Time format unexpected.\n");
			continue;
		    }
		    double start_time = e->value.dble;
		    printf("start time is %f\n", start_time);

		    int first_status = -1;
		    for (int i = 1; i < a->length; i++) {
			if (a->elements[i].type != CDL_ET_STRING) {
			    first_status = i + 1;
			    break;
			}
		    }
		    if (first_status == -1) {
			fprintf(stderr,
			 "WARNING: Couldn't find second timestamp.\n");
			continue;
		    }

		    e = &a->elements[first_status - 1];
		    if (e->type != CDL_ET_LO_RES) {
			fprintf(stderr, "WARNING: Time format unexpected.\n");
			continue;
		    }
		    double stop_time = e->value.dble;
		    printf("Stop time is %f\n", stop_time);

		    if (first_status + 10 > a->length) {
			fprintf(stderr, "WARNING: Array is truncated.\n");
			continue;
		    }

		    // Traverse the ten "present or not present" data elements.
		    // When the value of the element is 0.0, then there is a
		    // sensor present.  So we set the StatusInfo to present and
		    // read the sensorID that is located at cur_id.  We
		    // increment to the next id with cur_id and move on If the
		    // value of the element is -99999.0, then the sensor is not
		    // present.  Set the StatusInfo to not present and move on.
		    int cur_id = 1; // Current index into ID region of array.
		    for (int i = 0; i < 10; i++) {
			Cdl_Element *e = &a->elements[i + first_status];
			assert(e->type == CDL_ET_HI_RES);
			if (e->value.dble == pres) {
			    sen_status[i].present = 1;
			    strcpy(sen_status[i].id,
			     a->elements[cur_id].value.strng);
			    // XXX - Need to remove only trailing spaces, but
			    // for demo, remove all spaces. -kec
			    remove_spaces(sen_status[i].id);
			    printf("sensor %s present at address %d\n",
			     a->elements[cur_id].value.strng, i);
			    cur_id++;
			} else if (e->value.dble == not_pres) {
			    sen_status[i].present = 0;
			    sen_status[i].id[0] = '\0';
			    printf("sensor not present at address %d\n", i);
			} else {
			    fprintf(stderr,
			     "unknown value for checking sensor");
			    abort();
			}
		    }

		} else {

		    assert(a->id >= 1 && a->id <= 500);

		    const Sensor *s = &sensors[a->id - 1];
		    if (s->valid) {
			printf("array id: %d, n: %d\n", a->id, a->length);
			for (int j = 0; j < a->length; j++) {
			    Cdl_Element *e = &a->elements[j];
			    switch (e->type) {
				case CDL_ET_LO_RES:
				case CDL_ET_HI_RES:
				    printf("element %d: %f\n", j, e->value.dble);
				    break;
				case CDL_ET_STRING:
				    printf("element %d: %s\n", j, e->value.strng);
				    break;
				default:
				    assert(0);
				    abort();
			    }
			}
			if (strcmp(s->type, "Greenspan") == 0) {
			    printf("Got data from greenspan.\n");

			    double data[4];
			    for (int i = 0; i < 4; i++) {
				Cdl_Element *e = &a->elements[i];
				assert(e->type == CDL_ET_HI_RES);
				data[i] = e->value.dble;
			    }

			    char buf[100];
			    char tb[100];
			    {
				time_t t = time(0);
				struct tm tm;
				localtime_r(&t, &tm);
				strftime(tb, sizeof tb, "%Y-%m-%d %H:%M:%S",
				 &tm);
			    }
			    sprintf(buf, "'%s',%f,%f,%f,%f",
			     tb, data[0], data[1], data[2], data[3]);
			    printf("Name: %s, value: %s\n", s->table_name, buf);
                // Altered by Vinay
                // Start
                // Commented 2 line below
			    // int ec = insertData(s->table_name, buf);
			    // assert(ec);
                // Added the below code
                {
                  int bufSize = sizeof(double)*4;
                  double* dataBuffer = (double*)malloc(bufSize);
                  long long timestamp = getTimeInMillis();
                  for (int i = 0; i < 4; i++) {
                    dataBuffer[i] = (double) data[i];
                  }

                  printf("Sending the data to the connector...\n");
                  sendData(s->smd, dataBuffer, bufSize, timestamp);
                  printf("Done\n");
                  free(dataBuffer);
                }
                // End
			}
		    } else {
			printf("WARNING: array id doesn't correspond.\n");
		    }
		}
	    }
	    
	    //Free up memory
	    for (int i = 0; i < n_arrays; i++) {
		Cdl_Array__dtor(&arrays[i]);
	    }
	    free((void *) arrays);

	    //print out struct
	    for (int i = 0; i < 10; i++) {
		printf("--Address %d--\n", i);
		printf("address = %d\n", sen_status[i].address);
		printf("present = %d\n", sen_status[i].present);
		printf("id = %s\n", sen_status[i].id);
	    }

	    int num;

	    //Call webservice
	    // XXX - The number of status should not be hard-coded.
	    metadata = update_sensor_list(sen_status, 10, &flag, &num);

	    if(metadata == NULL){
		printf("No information found\n");
	    } else {
		printf("\nNumber of sensors %d\n",num); 
		for(int i = 0; i< num; i++) {
				
		    printf("\n Sensor ID %s",metadata[i]->sensorID);
		    printf("\n Lake ID %s",metadata[i]->lakeID);
		    printf("\n Logger ID %s",metadata[i]->loggerID);
		    printf("\n Buoy ID %s",metadata[i]->buoyID);
		    printf("\n TableName %s",metadata[i]->tableName);
		    printf("\n SampleRate %s\n",metadata[i]->sampleRate);
		    printf("\n sensorType %s\n",metadata[i]->sensorType);

		    for(int j = 0; j < MAX_NUM_IDS; j++) {
			if(strcmp(metadata[i]->dataID[j],"")) 
			    printf("Data ID %s\n",metadata[i]->dataID[j]);
		    }
		}

		/*
		 * Generate new datalogger program for reading data.
		 */
		 {
		    for (int i = 0; i < MAX_ARRAY_ID; i++) {
			sensors[i].valid = 0;
			free(sensors[i].type);
			free(sensors[i].table_name);
			sensors[i].table_name = NULL;
            // Added by Vinay
            // Start
            sensors[i].smd = NULL;
            // End
		    }

		    Cdl_Program input, output;
		    Cdl_Program__ctor(&input, 10);
		    Cdl_Program__ctor(&output, 5);

		    // Set hi-res output.
		    Cdl_Program__p78(&input, 1);
		    // Turn on output flag.
		    Cdl_Program__p86(&input, 10);

		    int loc = 1;
		    for (int i = 0; i < num; i++) {

			const struct Metadata *const m = metadata[i];

			sensors[array_id - 1].valid = 1;
			sensors[array_id - 1].type = strdupe(m->sensorType);
			sensors[array_id - 1].table_name
			 = strdupe(m->tableName);
            // Added by Vinay
            // Start
            sensors[array_id - 1].smd = m->smd;
            // End

			if (strcmp(m->sensorType, "Greenspan") == 0) {

			    printf("greenspan detected.\n");

			    Cdl_Program__p105(&input,
			    // XXXX - Fix hard-coded address.
			     m->address,	// Address
			     0, // Command
			     8, // Port
			     loc,
			     1, 0);

			    Cdl_Program__p80(&input, 1, array_id);
			    Cdl_Program__p70(&input, 4, loc);
			    loc += 4;

			    printf("greenspan detected: 2.\n");

			} else if (strcmp(metadata[i]->sensorType,
			 "TempLine thermistor chain") == 0) {

			    printf("greenspan deteced.\n");

			} else {
			    assert(0);
			    abort();
			}

			array_id++;
			assert(array_id <= 501);
			if (array_id == 501) {
			    array_id = 1;
			}
		    }

		    Cdl_String tbl1_s = Cdl_Program__String(&input);

		    Cdl_String s;
		    Cdl_String__ctor(&s);

		    Cdl_String__fcatl(&s, "MODE 1");
		    Cdl_String__cat(&s, tbl1_s.str);
		    Cdl_String__cat(&s, program_ten_locations);

		    printf("Program: %s\n", s.str);

		    // Upload program.
		    //load_Program(fd, 1, &input);
		    load_program(fd, s.str);

		    Cdl_Program__dtor(&input);

		    // Skip old data.
		    FSA_Status st = get_fsa_status(fd);
		    mptr = st.DSP_location;
		    goto end_cycle;
		}
	    }
	    mptr += new_data;	//increment mptr past the binary data that we just read
	}
	//printf("\n-------------------------------------\n");
	end_cycle:;
    }
}

double timedifference(struct timeval *t2, struct timeval *t1) {

    unsigned long usec, sec;

    // If t2's usec value is greater than t1's usec 
    if (t2->tv_usec > t1->tv_usec) {
        usec = (t1->tv_usec + 1000000) - t2->tv_usec;
        sec = (t1->tv_sec - 1) - t2->tv_sec;
    } else {
        usec = t1->tv_usec - t2->tv_usec;
        sec = t1->tv_sec - t2->tv_sec;
    }
    return ((double)sec + (double)(usec/1000000));
}



// vim: set sw=4 sts=4 expandtab ai:
