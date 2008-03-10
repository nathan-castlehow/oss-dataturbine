#include "detectsensor.hpp"

int
main() {

	struct StatusInfo* status[10];
	METADATA *temp;
	int num;
	int flag = 1;
	
	struct StatusInfo* tempnode = (struct StatusInfo*)malloc(sizeof(struct StatusInfo));
	status[0] = tempnode;
	status[0]->address = 0;
	status[0]->present = 0;
	strcpy(status[0]->id,"sen1");
	
	tempnode = (struct StatusInfo*)malloc(sizeof(struct StatusInfo));
	status[1] = tempnode;
	status[1]->address = 0;
	status[1]->present = 1;
	strcpy(status[1]->id,"sen1");
	
	tempnode = (struct StatusInfo*)malloc(sizeof(struct StatusInfo));
	status[2] = tempnode;
	status[2]->address = 0;
	status[2]->present = 1;
	strcpy(status[2]->id,"sen1");
	
	tempnode = (struct StatusInfo*)malloc(sizeof(struct StatusInfo));
	status[3] = tempnode;
	status[3]->address = 1;
	status[3]->present = 1;
	strcpy(status[3]->id,"test");
	
	temp = update_sensor_list(status, 4, &flag, &num);
	if(temp == NULL){
		printf("No information found\n");
		return 0;
	}
	printf("\nNumber of sensors %d\n",num);	
	for(int i = 0; i< num; i++) {
	
		printf("\n Sensor ID %s",temp[i]->sensorID);
		printf("\n Lake ID %s",temp[i]->lakeID);
		printf("\n Logger ID %s",temp[i]->loggerID);
		printf("\n Buoy ID %s",temp[i]->buoyID);
		printf("\n TableName %s",temp[i]->tableName);
		printf("\n SampleRate %s\n",temp[i]->sampleRate);
		
		for(int j = 0; j < MAX_NUM_IDS; j++) {
			if(strcmp(temp[i]->dataID[j],"")) 
				printf("Data ID %s\n",temp[i]->dataID[j]);
		}
	}
	
	return 1;
}



// vim: set sw=4 sts=4 expandtab ai:
