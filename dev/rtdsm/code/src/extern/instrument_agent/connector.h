#ifndef CONNECTOR_H
#define CONNECTOR_H

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <assert.h>
#include <time.h>
#include <sys/time.h>

#define MAXDATASIZE 1000

// Length field in the Header (in bytes)
// This should 2 bytes
#define TWO_BYTES_SIZE 2
#define ONE_BYTE_SIZE 1

#define RECV_TIMEOUT_SECS 60
#define RECV_TIMEOUT_USECS 0

#define PROP_FILE_NAME "connector.prop"
#define HOST_NAME_TAG "hostname"
#define PORT_NUM_TAG "port"


typedef struct _sensor_meta_data {
  char* id;
  uint16_t numChannels;
  uint16_t* channelDataTypes;
  char* webSerStr;
  struct _sensor_meta_data* next;
}SensorMetaData;

SensorMetaData* lookup(char* id);
void sendData(SensorMetaData* addr, void* data, int size, long long timestamp);
void initConnectorAgent();
long long getTimeInMillis();

#endif // CONNECTOR_H
