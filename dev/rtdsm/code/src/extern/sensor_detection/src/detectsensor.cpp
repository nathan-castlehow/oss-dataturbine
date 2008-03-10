#include "detectsensor.hpp"
#include <stdio.h>
#include <string.h>
// Commented by Vinay
// Start
// #include "soapH.h"
// #include "InstrumentMetadataSoapBinding.nsmap"
// End
#include "dlcomm.hpp"
// Added by Vinay
// Start
#include "connector.h"
// End



/*Internal Address Map*/
typedef struct addresses {
	int present;
	char id[MAX_ID_LEN];
} ADDR_MAP;

/* Local cache, This info will not be persisted*/
typedef struct InternalStatusInfo {	
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
} *INTERNALMETADATA;

ADDR_MAP address_map[NUM_OF_ADDRESSES];
INTERNALMETADATA internal_sensorinfo[NUM_OF_ADDRESSES];
int internal_sensor_count = 0;
int Initialized = 0;

/* Global declarations */
METADATA sensorInfo[NUM_OF_ADDRESSES];



/* Helper function for string tokenizer */
void 
Mystrrev(char *str) {
	
	char temp;
	for (int i = 0, j = strlen(str) - 1; i < j; i++,j--) {
		temp = str[i];
		str[i] = str[j];
		str[j] = temp;
	}
}

/* Helper function for string tokenizer */
void
retriveData(char* url) {
	
	char* subString;
	char id[MAX_ID_LEN];
	
	if ((subString = strstr(url,"DataID")) != NULL) {		
		int j = 0;
		int id_pos = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else {
				i = i - 2;
				char data_id_num[MAX_ID_LEN];
				int k;
				for (k = 0; k < MAX_ID_LEN, subString[i] != 'D' ; k++, i--) {					
					data_id_num[k] = subString[i];
				}
				data_id_num[k] = '\0';
				Mystrrev(data_id_num);				
				id_pos = atoi(data_id_num);				
				break;
			}
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->dataID[id_pos],id);		
	}
	else if ((subString = strstr(url,"SensorID")) != NULL) {		
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);	
		strcpy(internal_sensorinfo[internal_sensor_count]->sensorID,id);
	}
	else if((subString = strstr(url,"LakeID")) != NULL) {		
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->lakeID,id);
		
	}
	else if((subString = strstr(url,"BuoyID")) != NULL) {		
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->buoyID,id);
	}
	else if((subString = strstr(url,"SampleRate")) != NULL) {		
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];			
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->sampleRate,id);
	}
	else if((subString = strstr(url,"TableName")) != NULL) {		
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->tableName,id);
	}
	else if((subString = strstr(url,"LoggerID")) != NULL) {	
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->loggerID,id);
	}
	else if((subString = strstr(url,"SensorType")) != NULL) {	
		int j = 0;
		for( int i = strlen(subString)-1; i>0; i--) {
			if (subString[i] != '>') {
				id[j] = subString[i];				
				j++;
			}
			else
				break;
		}
		id[j] = '\0';		
		Mystrrev(id);		
		strcpy(internal_sensorinfo[internal_sensor_count]->sensorType,id);
	}
}

/* Tokenize the metadata which we get in a form of a string from the webservice*/
void
tokenizeMetadata(char infostr[]) {

	char* url;	
	url = strtok(infostr, ",");	
	retriveData(url);	
	
	while ( (url = strtok(NULL, ",")) != NULL) {		
		retriveData(url);	
	}
}

/* Allocate memory for a node of METADATA structure */
METADATA
getmetadataNode() {

	int i;
	METADATA temp;	
	temp = (struct Metadata*) malloc(sizeof(struct Metadata));
	if (temp == NULL)
		return temp;
	temp->address = -1;
	strcpy(temp->sensorID,"");
	strcpy(temp->lakeID,"");
	strcpy(temp->buoyID,"");
	strcpy(temp->loggerID,"");
	strcpy(temp->tableName,"");
	strcpy(temp->sampleRate,"");
	strcpy(temp->sensorType,"");	
	for (i = 0; i < MAX_NUM_IDS; i++)
		strcpy(temp->dataID[i],"");	
  // Added by Vinay
  // Start
  temp->smd = NULL;
  // End
	return temp;
}

/* Allocate memory for a node of INTERNALMETADATA structure */
INTERNALMETADATA 
getinternalmetadataNode() {

	INTERNALMETADATA temp;
	int i;
	temp = (INTERNALMETADATA) malloc(sizeof(struct InternalStatusInfo));
	if (temp == NULL)
		return temp;
	temp->address = -1;
	strcpy(temp->sensorID,"");
	strcpy(temp->lakeID,"");
	strcpy(temp->buoyID,"");
	strcpy(temp->loggerID,"");
	strcpy(temp->tableName,"");
	strcpy(temp->sampleRate,"");
	strcpy(temp->sensorType,"");
	for (i = 0; i < MAX_NUM_IDS; i++)
		strcpy(temp->dataID[i],"");		
  // Added by Vinay
  // Start
  temp->smd = NULL;
  // End
	return temp;
}

// Altered by Vinay
// Commented the webservice call
// Start
/* This is just a wrapper for calling the webservice invocation function*/
// char*
// invokeWebservice(char* sensorID) {
// 	struct soap soap;
// 	char* info;
// 
// 	soap_init(&soap);	
// 	if(soap_call_ns1__getSensor(&soap, 
// 							  NULL  /*endpoint address*/, 
// 							  NULL  /*soapAction*/, 
// 							  sensorID, 
// 							  &info 
// 							 )== SOAP_OK) {
// 	}
// 	else	
// 		soap_print_fault(&soap, stderr); 
// 
//         // Must copy the string, it seems.
//         char *s = strdupe(info);
// 	
// 	soap_destroy(&soap); 
// 	soap_end(&soap); 
// 	soap_done(&soap); 	
// 
//     printf("Called web service with: %s, got back %s.\n", sensorID, s);
// 	
// 	return s;
// }
// End

/* Copy from cache to output structure */
void
copy_from_to(INTERNALMETADATA status, METADATA metadata) {	
	
	metadata->address = status->address;
	strcpy(metadata->sensorID,status->sensorID);
	strcpy(metadata->lakeID,status->lakeID);
	strcpy(metadata->buoyID,status->buoyID);
	strcpy(metadata->loggerID,status->loggerID);
	strcpy(metadata->tableName,status->tableName);
	strcpy(metadata->sampleRate,status->sampleRate);
	strcpy(metadata->sensorType,status->sensorType);
	for (int i = 0; i < MAX_NUM_IDS; i++)
		strcpy(metadata->dataID[i],status->dataID[i]);	
  // Added by Vinay
  // Start
  metadata->smd = status->smd;
  // End
}

/* Initialize the address map only once */
void init_address_map() {

	if (!Initialized) {		
		for(int iCnt = 0; iCnt < NUM_OF_ADDRESSES; iCnt++) {
			address_map[iCnt].present = 0;
			strcpy(address_map[iCnt].id,"");
		}
		Initialized = 1;
	}
}

/* Here we parse the input structure to detect if any news sensos have been
 * detected*/
METADATA*
update_sensor_list(struct StatusInfo status[], int n, int *is_change,
 int *n_sensors) {

    /*Initialize the internal address map*/	
    init_address_map();	
    int op_sensor_cnt = 0;

    int new_sensor_detected = 0;

    /* Check if there is a new sensor detected */
    for(int iCnt = 0; iCnt < n; iCnt++) {
        if((address_map[status[iCnt].address].present
         != status[iCnt].present)){

            /* A sensor has been rempved */
            if (status[iCnt].present == 0) {
                address_map[status[iCnt].address].present = 0;
                strcpy(address_map[status[iCnt].address].id,"");
            }
            /* A sensor has been added*/
            else {
                address_map[status[iCnt].address].present = 1;
                strcpy(address_map[status[iCnt].address].id,status[iCnt].id);
            }
            new_sensor_detected = 1;				
        }
        /* An old sensor removed and new attached as a single event, this is a
         * rare case  */
        else if(status[iCnt].present) {

            if(strcmp(address_map[status[iCnt].address].id,status[iCnt].id)) {

                address_map[status[iCnt].address].present = 1;
                strcpy(address_map[status[iCnt].address].id,status[iCnt].id);
                new_sensor_detected = 1;				
            }
        }
    }

    /* If no change detected return NULL*/
    if(!new_sensor_detected)
        return NULL;

    /* Else return sensors present on all addresses */	
    for(int iCnt = 0; iCnt < NUM_OF_ADDRESSES; iCnt++) {

        /* Check if we have information for this sensor */
        /*Search the internal data structure to see if we have the info for
         * this sensor*/
        int found = 0;
        int j;
        if (address_map[iCnt].present) {
            for(j = 0; j < internal_sensor_count; j++) {
                if (strcmp(internal_sensorinfo[j]->sensorID,
                 address_map[iCnt].id)==0) {				
                    found = 1;
                    break;
                }
            }	
        }
        else
        continue;

        /* If sensorID found, populate o/p info with info from internal status*/
        if(found) {

            // Get a new node for sensor_meta
            METADATA temp;
            temp = getmetadataNode();
            if(temp == NULL)
                return NULL;			
            sensorInfo[op_sensor_cnt] = temp;
			
			//store the address information
			internal_sensorinfo[j]->address = iCnt;

            //copy from internal cache to this node
            copy_from_to(internal_sensorinfo[j], sensorInfo[op_sensor_cnt]);

            //Increment count of sensors to be returned
            op_sensor_cnt++;
        }

        /* If sensorID not found,  call webservice, populate o/p and internal
         * status */
        else {

            // Get a new node for sensor_meta
            METADATA mtemp;			
            mtemp = getmetadataNode();			
            if(mtemp == NULL)
                return NULL;			
            sensorInfo[op_sensor_cnt] = mtemp;	

            // Get a new node for local cache
            INTERNALMETADATA itemp;
            itemp = getinternalmetadataNode();
            if(itemp == NULL)
                return NULL;
            internal_sensorinfo[internal_sensor_count] = itemp;
			
			//Store address information
			internal_sensorinfo[internal_sensor_count]->address = iCnt;
			
            //Copy the sensor ID
            strcpy(internal_sensorinfo[internal_sensor_count]->sensorID,
             status[iCnt].id);

            //Call webservice with the new sensor ID
            char *sensor_metadata;
            // Changed by Vinay
            // Start
            // Commented the below line
            // sensor_metadata = invokeWebservice(address_map[iCnt].id);			
            printf("Looking up for meta-data information..\n");
            SensorMetaData* smd = lookup(address_map[iCnt].id);
            printf("Done\n");
            assert(smd);
            assert(smd->webSerStr);
            sensor_metadata = smd->webSerStr;
            // End
            if ((sensor_metadata == NULL)
             || (strcmp(sensor_metadata,"") == 0)) {				
                continue;
            }

            //Tokenize the string and populate both the strucutres
            char* tokenize = (char*)malloc(strlen(sensor_metadata) + 1);
            strcpy(tokenize,sensor_metadata);			
            tokenizeMetadata(tokenize);
			free(tokenize);
            // Added by Vinay
            // Start
            // Store the SensorMetaData structure obtained from lookup
            internal_sensorinfo[internal_sensor_count]->smd = smd;
            // End

            //copy from internal cache to this node
            copy_from_to(internal_sensorinfo[internal_sensor_count],
             sensorInfo[op_sensor_cnt]);

            //Increment count of sensors to be returned
            op_sensor_cnt++;
            //Increment count of sensors in local cache
            internal_sensor_count++;			
        }
    }

    *n_sensors = op_sensor_cnt;
    if(op_sensor_cnt > 0)
        return sensorInfo;

    return NULL;
}



// vim: set sw=4 sts=4 expandtab ai:
