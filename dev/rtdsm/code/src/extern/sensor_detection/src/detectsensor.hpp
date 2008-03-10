#ifndef CDL_DETECT_SENSOR_H
#define CDL_DETECT_SENSOR_H



// KEC: Put CDL_ in front of these to avoid
// conflicts with other libraries, in case we
// ever turn this into a library.
#define MAX_ID_LEN 100
#define NUM_OF_ADDRESSES 120
#define MAX_NUM_IDS 120

// Added by Vinay
// Start
#include "connector.h"
// End

/* Input Structure */
struct StatusInfo {
	int present;
	int address;
	char id[MAX_ID_LEN];
};

/* Structure that will be returned as output. */
typedef struct Metadata {
	
	int address;
	char sensorID[MAX_ID_LEN];
	char lakeID[MAX_ID_LEN];
	char buoyID[MAX_ID_LEN];
	char loggerID[MAX_ID_LEN];
	char tableName[MAX_ID_LEN];
	char sampleRate[MAX_ID_LEN];
	char sensorType[MAX_ID_LEN];
	char dataID[MAX_NUM_IDS][MAX_ID_LEN];	
  // Added by Vinay
  // Start
  SensorMetaData* smd;
  // End
} *METADATA;
/*
 * KEC: I personally save all caps for #define.
 * I would prefer something like:
 *     typedef struct Metadata_s {
 *         ...
 *     } Metadata;
*/

METADATA *
update_sensor_list(struct StatusInfo status[], int n, int *is_change,
 int *n_sensors);



#endif



// vim: set sw=4 sts=4 expandtab ai:
