#include "connector.h"

enum pType_ {
  GET_META_DATA=0,
  SEND_DATA
};

enum endian_ {
  LITTLE_ENDIAN_=0,
  BIG_ENDIAN_,
  UNKNOWN_ENDIAN_
};

enum datatypes_{
  DOUBLE_DATA_TYPE=0,
  NUM_DATA_TYPES
};

int endian;
char* hostname = NULL;
int port = -1;
static SensorMetaData* metaDataListHead;

/** Utility functions **/

uint16_t convert16BitsToNetworkBO(uint16_t x){

  uint16_t retVal;
  if(endian == LITTLE_ENDIAN_) {

    retVal = (((x>>8)) | (x<<8));

  }
  else if(endian == BIG_ENDIAN_) {
    retVal = x;
  }
  else {
    fprintf(stderr, "Unknown endian system. Cannot support swapping 8 bits\n");
    assert(0);
  }
  return retVal;
}

uint32_t convert32BitsToNetworkBO(uint32_t x){

  uint32_t retVal;
  if(endian == LITTLE_ENDIAN_) {

    retVal = ((x<<24 & 0xFF000000) | (x<<8 & 0x00FF0000) |
             (x>>8 & 0x0000FF00) | (x>>24 & 0x000000FF));

  }
  else if(endian == BIG_ENDIAN_) {
    retVal = x;
  }
  else {
    fprintf(stderr, "Unknown endian system. Cannot support swapping 32 bits\n");
    assert(0);
  }
  return retVal;
}

uint64_t convert64BitsToNetworkBO(uint64_t x){

  uint64_t retVal;
  if(endian == LITTLE_ENDIAN_) {

    uint32_t fpart;
    uint32_t spart;
    char* ptr = (char*)&x;
    memcpy(&fpart, &x, sizeof(uint32_t));
    memcpy(&spart, ptr + 4, sizeof(uint32_t));
    fpart = convert32BitsToNetworkBO(fpart);
    spart = convert32BitsToNetworkBO(spart);
    ptr = (char*) &retVal;
    memcpy(ptr, &spart, sizeof(uint32_t));
    memcpy(ptr + 4, &fpart, sizeof(uint32_t));
    // retVal = ((x<<54 & 0xFF00000000000000) | (x<<40 & 0x00FF000000000000)|
    //           (x<<24 & 0x0000FF0000000000) | (x<< 8 & 0x000000FF00000000)|
    //           (x>> 8 & 0x00000000FF000000) | (x>>24 & 0x0000000000FF0000)|
    //           (x<<40 & 0x000000000000FF00) | (x<<54 & 0x00000000000000FF));
  }
  else if(endian == BIG_ENDIAN_) {
    retVal = x;
  }
  else {
    fprintf(stderr, "Unknown endian system. Cannot support swapping 32 bits\n");
    assert(0);
  }
  return retVal;
}

double myHtond(double d){
  double retVal=-1;
  switch(sizeof(double)){
    case 8:
      {
        uint64_t t;
        memcpy(&t,&d,sizeof(double));
        t = convert64BitsToNetworkBO(t);
        memcpy(&retVal,&t,sizeof(double));
      }
      break;
    default:
      fprintf(stderr, "\"double\" was 8 bytes in IA32 and IA64. "
          "New changes in size? Non-intel architecture?");
      abort();
  }
  return retVal;
}

float myHtonf(float f){

  float retVal = -1;
  switch(sizeof(float)){
    case 4:
      {
      uint32_t t;
      memcpy(&t,&f,sizeof(float));
      t = convert32BitsToNetworkBO(t);
      memcpy(&retVal,&t,sizeof(float));
      }
      break;
    default:
      fprintf(stderr, "\"float\" was 4 bytes in IA32 and IA64. "
          "New changes in size? Non-intel architecture?");
      abort();
  }
  return retVal;
}

long long myHtonll(long long l){

  long long retVal;
  switch(sizeof(long long)){
    case 8:
      {
      uint64_t t;
      memcpy(&t,&l,sizeof(long long));
      t = convert64BitsToNetworkBO(t);
      memcpy(&retVal,&t,sizeof(long long));
      }
      break;
    default:
      fprintf(stderr, "\"long long\" was 8 bytes in IA32 and IA64. "
          "New changes in size? Non-intel architecture?");
      abort();
  }
  return retVal;
}

// End - Utility functions

int connectToTurbine(char* hostName, int port){

  int sockfd;
	struct hostent *he;
	struct sockaddr_in their_addr; 

	if ((he=gethostbyname(hostName)) == NULL) {
	    perror("gethostbyname");
	    exit(1);
	}

	if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
	    perror("socket");
	    exit(1);
	}

	their_addr.sin_family = AF_INET;
	their_addr.sin_port = htons(port);
	their_addr.sin_addr = *((struct in_addr *)he->h_addr);
	memset(&(their_addr.sin_zero), '\0', 8);

	if (connect(sockfd, (struct sockaddr *)&their_addr, sizeof(struct sockaddr)) == -1) {
	    perror("connect");
	    exit(1);
	}
  return sockfd;
}

void sendToTurbine(int sockfd, void* buf, int len){

  int numbytes;
	if ((numbytes=send(sockfd, buf, len, 0)) != len) {
	    perror("send");
	    exit(1);
	}
}

int recvFromTurbine(int sockfd, void* buf, int maxLen) {

  fd_set rfds;
  struct timeval tv;
  int retval;

  FD_ZERO(&rfds);
  FD_SET(sockfd, &rfds);
  tv.tv_sec = RECV_TIMEOUT_SECS;
  tv.tv_usec = RECV_TIMEOUT_USECS;

  retval = select(sockfd + 1, &rfds, NULL, NULL, &tv);
  if(retval == -1) {
    //Timed out. Server dint send anything
    //TODO: Retry
    return -1;
  }
  else if(retval == 0) {
    //Timed out. Server end closed
    return -1;
  }
  else {
    
    // TODO: If we get chunks here and then the server
    // stops to respond??
    retval = recv(sockfd, buf, maxLen, 0);
  }
  return retval;
}

void closeTurbineConnection(int sockfd){
	close(sockfd);
}

void addUInt16(void* buf, uint16_t len) {
  // Java is Big-endian
  len=htons(len);
  memcpy(buf, &len, sizeof(uint16_t));
}

void addUInt8(void* buf, uint8_t len) {
  
  // Copy only the least significant byte of len
  memcpy(buf, &len, ONE_BYTE_SIZE);
}

void sendDataPacketOverSocket(int sockfd, void* key, uint16_t keyLen, void* dataBuf, 
    uint16_t dataBufLen) {

  uint16_t pktLen = 
    TWO_BYTES_SIZE + // For the header length
    ONE_BYTE_SIZE + // For the packet type
    TWO_BYTES_SIZE + // For the key length
    keyLen + // For the actual key
    TWO_BYTES_SIZE + // For the data length
    dataBufLen; // For the actual data

  void* sendBuf = malloc(pktLen);
  char* tmpBuf = (char*)sendBuf;

  // Add header length
  addUInt16(tmpBuf,pktLen);
  tmpBuf+=TWO_BYTES_SIZE;
  
  // Add header type
  addUInt8(tmpBuf,(uint8_t)SEND_DATA);
  tmpBuf+=ONE_BYTE_SIZE;

  // Add key length
  addUInt16(tmpBuf,keyLen);
  tmpBuf+=TWO_BYTES_SIZE;

  // Add key
  memcpy(tmpBuf, key, keyLen);
  tmpBuf+=keyLen;

  // Add data length
  addUInt16(tmpBuf,dataBufLen);
  tmpBuf+=TWO_BYTES_SIZE;

  // Add data
  memcpy(tmpBuf, dataBuf, dataBufLen);
  tmpBuf+=dataBufLen;

  printf("connector: Sending %d bytes\n",pktLen);
  // int i=0;
  // char c=0;
  // char* tmp = (char*) sendBuf;
  // for(i=0;i<pktLen;i++) {
  //   c = tmp[i];
  //   printf("\tbuffer[%d]=%d\n",i,c);
  // }
  sendToTurbine(sockfd, sendBuf, pktLen);
}

int testEndian(){

  union u {
		long x;
		char c[4];
	} u;

	u.x = 0x01020304;

  if(u.c[0] == 1){
    // printf("Big Endian\n");
    return BIG_ENDIAN_;
  }
  else if (u.c[0] == 4) {
    // printf("Little Endian\n");
    return LITTLE_ENDIAN_;
  }
  else {
    // printf("Neither..\n");
    return UNKNOWN_ENDIAN_;
  }

	// printf("%lx\n", u.c[0]);
}

SensorMetaData* getSensorMetaData(char* id){
  SensorMetaData* smd;
  SensorMetaData* curr = metaDataListHead;
  SensorMetaData* prev = NULL;

  int len = strlen(id)+1;

  for(curr = metaDataListHead; curr != NULL; ){

    if(memcmp(id, curr->id, len) == 0){
      printf("Found an entry already present in the list with id "
          "\"%s\"\n", id);
      break;
    }
    prev = curr;
    curr= curr->next;
  }

  if(curr == NULL) {

    // No item found
    smd = (SensorMetaData*) malloc(sizeof(SensorMetaData));
    smd->id = (char*) malloc(sizeof(char)*(len));
    memcpy(smd->id, id, len);
    smd->numChannels = 0;
    smd->channelDataTypes = NULL;
    smd->webSerStr = NULL;
    smd->next = NULL;
    if(prev != NULL){
      prev->next = smd;
    }
    else {
      metaDataListHead = smd;
    }
  }
  else {
    
    // Already exists
    smd = curr;
  }

  return smd;
}

void printSmdList(){

  SensorMetaData* curr;
  int i=0,j;

  for(curr = metaDataListHead; curr!= NULL; curr = curr->next, i++){

    printf("\t%d: id=%s\n",i, curr->id);
    printf("\t\t%d\n", curr->numChannels);
    printf("\t\t");

    for(j=0;j<curr->numChannels; j++) {

      printf(" %d, ", (int)curr->channelDataTypes[j]);
    }
    printf("\n");
  }
}

SensorMetaData* parseMetaData(void* buf, int len){

  uint16_t idLen, webSerStrLen;
  char* id;
  uint16_t numChannels;
  char* theBuf = (char*) buf;
  char* tmpBuf;
  char* webSerStr;
  int offset = 0;
  int i=0;

  memcpy(&idLen,theBuf+offset, sizeof(uint16_t));
  idLen = ntohs(idLen);
  offset += sizeof(uint16_t);
  // printf("Size of the id string=%d %x %x\n", (int)idLen, idLen, 
  //     idLen & 0x8000); 
  
  // Java is sensing a signed short. 
  // Make sure that it is positive
  assert((idLen & 0x8000) == 0);
  

  id = (char*) malloc(sizeof(char)*(int)(idLen+1));
  memcpy(id,theBuf+offset, sizeof(char)*idLen);
  offset += sizeof(char) * idLen;
  id[idLen] = '\0';
  // printf("id=%s\n", id); 
  
  SensorMetaData* smd = getSensorMetaData(id);
  assert(smd != NULL);
  memcpy(&smd->numChannels,theBuf+offset, sizeof(uint16_t));
  smd->numChannels = ntohs(smd->numChannels);
  offset += sizeof(uint16_t);
  // printf("Number of channels=%d\n", (int)smd->numChannels); 
  assert((smd->numChannels & 0x8000) == 0);

  smd->channelDataTypes = (uint16_t*) malloc(
      sizeof(uint16_t)*smd->numChannels);
  tmpBuf = (char*) smd->channelDataTypes;

  for(i=0;i<smd->numChannels; i++){

    memcpy(&idLen, theBuf+offset, sizeof(uint16_t));
    idLen = ntohs(idLen);
    offset += sizeof(uint16_t);
    memcpy(tmpBuf + i*sizeof(uint16_t), &idLen, sizeof(uint16_t));
    assert((smd->channelDataTypes[i] & 0x8000) == 0);
  }

  memcpy(&webSerStrLen,theBuf+offset, sizeof(uint16_t));
  webSerStrLen = ntohs(webSerStrLen);
  offset += sizeof(uint16_t);
  // printf("Length of webser string = %d\n",(int)webSerStrLen); 

  smd->webSerStr = (char*) malloc(sizeof(char)*(webSerStrLen + 1));
  memcpy(smd->webSerStr, theBuf+offset, sizeof(char)*webSerStrLen); 
  smd->webSerStr[webSerStrLen] = '\0';
  // printf("Webservice string = %s\n",smd->webSerStr); 
  
  // for(i=0;i < smd->numChannels; i++){
  //   printf("Channel(%d)=%d\n", i, (int)smd->channelDataTypes[i]); 
  // }
  // printSmdList();
  free(id);
  return smd;
}

SensorMetaData* lookup(char* id) {
  
  assert(id != NULL);
  int len = strlen(id);
  uint16_t pktLen = 
    TWO_BYTES_SIZE + // For the header length
    ONE_BYTE_SIZE + // For the packet type
    TWO_BYTES_SIZE + // For the id length
    len; // For the actual id

  void* sendBuf = malloc(pktLen);
  char* tmpBuf = (char*)sendBuf;

  // Add header length
  addUInt16(tmpBuf,pktLen);
  tmpBuf+=TWO_BYTES_SIZE;
  
  // Add header type
  addUInt8(tmpBuf,(uint8_t)GET_META_DATA);
  tmpBuf+=ONE_BYTE_SIZE;

  // Add id length
  addUInt16(tmpBuf,len);
  tmpBuf+=TWO_BYTES_SIZE;

  // Add key
  memcpy(tmpBuf, id, len);
  tmpBuf+=len;

  // printf("Bytes being sent:\n");
  // int i=0;
  // char c=0;
  // char* tmp = (char*) sendBuf;
  // for(i=0;i<pktLen;i++) {
  //   c = tmp[i];
  //   printf("\tbuffer[%d]=%d\n",i,c);
  // }

  int sockfd = connectToTurbine(hostname, port);
  assert(sockfd != -1);
  sendToTurbine(sockfd, sendBuf, pktLen);
  free (sendBuf);

  void* recvBuf = malloc(MAXDATASIZE);

  printf("connector: Waiting for response...\n");
  int numBytesRecvd = recvFromTurbine(sockfd, recvBuf, MAXDATASIZE);
  printf("connector: Number of bytes received = %d\n", numBytesRecvd);
  // tmp = (char*) recvBuf;
  // for(i=0;i<numBytesRecvd;i++) {
  //   c = tmp[i];
  //   printf("\tRecv buffer[%d]=%d\n",i,c);
  // }
  SensorMetaData* smd = parseMetaData(recvBuf, numBytesRecvd);
  free(recvBuf);
  return smd;
}

char* parseSendDataAck(void* data, int size, int* success) {

  uint16_t pktSize;
  uint16_t ack;
  int offset = 0;
  int i=0;
  char* theBuf = (char*) data;
  char* feedbackMsg = NULL;

  memcpy(&pktSize,theBuf+offset, sizeof(uint16_t));
  pktSize = ntohs(pktSize);
  offset += sizeof(uint16_t);

  memcpy(&ack,theBuf+offset, sizeof(uint16_t));
  ack = ntohs(ack);
  offset += sizeof(uint16_t);
  printf("connector: pktSize=%d ack received %d\n", (int)pktSize, (int)ack);

  if(pktSize > sizeof(uint16_t) ){
    //We have a feedback
    short feedbackSize;
    memcpy(&feedbackSize,theBuf+offset, sizeof(uint16_t));
    feedbackSize = ntohs(feedbackSize);
    offset += sizeof(uint16_t);
    // for the \0 char
    feedbackSize ++;
    printf("Feedback Size: %d\n", (int)feedbackSize);

    feedbackMsg = (char*) malloc(sizeof(char)*feedbackSize);
    memcpy(feedbackMsg,theBuf+offset, feedbackSize - 1);
    feedbackMsg[feedbackSize - 1] = '\0';
    printf("Feedback Msg: %s\n", feedbackMsg);
  }
  return feedbackMsg;
}

void sendData(SensorMetaData* addr, void* data, int size, long long timestamp){
  
  assert(addr != NULL);
  assert(data != NULL);

  SensorMetaData* smd = (SensorMetaData*) addr;
  int totSize = size + sizeof(uint64_t);
  printf("smd: size=%d totSize=%d\n", size, totSize);
  char* dataBuf = (char*)malloc(totSize);

  int testSize=0;
  long long networkOrderTs = myHtonll(timestamp);
  memcpy(dataBuf + testSize, &networkOrderTs, sizeof(long long));
  testSize += sizeof(long long);

  char* dataBuftmp = (char*)data;
  int tmpOffset = 0;
  int i;
  
  printf("smd: numchannels=%d\n", (int)smd->numChannels);
  for(i=0;i<smd->numChannels; i++){

    switch(smd->channelDataTypes[i]){

      case DOUBLE_DATA_TYPE:
      {
        double testData = 0;
        memcpy(&testData, dataBuftmp + tmpOffset, sizeof(double));
        printf("Data [%d] = %f (testSize=%d tmpOffset=%d)\n", i, testData,
            testSize, tmpOffset);
        testData = myHtond(testData);
        memcpy(dataBuf+testSize,&testData, sizeof(double));
        testSize += sizeof(double);
        tmpOffset += sizeof(double);
        break;
      }
      default:
        fprintf(stderr, "Currently double data type is supported. More to "
            "be provided soon\n");
        assert(0);
    }
  }
  assert(testSize == totSize);
  int sockfd = connectToTurbine(hostname, port);
  assert(sockfd != -1);
  sendDataPacketOverSocket(sockfd, smd->id,strlen(smd->id), (void*)dataBuf, totSize);
  free(dataBuf);

  void* recvBuf = malloc(MAXDATASIZE);
  int success = -1;
  printf("connector: Waiting for ack and feedback response from server...\n");
  int numBytesRecvd = recvFromTurbine(sockfd, recvBuf, MAXDATASIZE);
  char* feedbackMsg = parseSendDataAck(recvBuf, numBytesRecvd, &success);
  if(feedbackMsg) {
    free(feedbackMsg);
  }
  free(recvBuf);
}

void processLine(char* line){

  char* token = strtok(line, "=");
  char* key = (char*) malloc(sizeof(char)*(strlen(line) + 1));
  uint8_t findKey = 1;
  while(token != NULL) {

    if(findKey) {
      memcpy(key,token,sizeof(char)*(strlen(token)+1));
      findKey = 0;
    }
    else {
      if(strcmp(key,PORT_NUM_TAG) == 0){
        assert(sscanf(token, "%d", &port) != 0);
      }
      else if(strcmp(key, HOST_NAME_TAG) == 0){

        hostname = (char*) malloc(sizeof(char)*(strlen(token) + 1));
        memcpy(hostname,token,sizeof(char)*(strlen(token)+1));
      }
      
    }
    token=strtok(NULL, "=");
  }
  free(key);
}

void readProperties() {

  int c, index;
  uint8_t skipLine;
  char tmpStr[MAXDATASIZE];

  FILE* f = fopen(PROP_FILE_NAME,"r");
  if(f == NULL) {
    perror("Error opening property file:");
    exit(-1);
  }

  index = 0;
  skipLine=0;
  while ((c = fgetc(f)) != EOF){

    if(c== '#'){

      // Ignore comment line
      skipLine = 1;
      continue;
    }
    if(c== '\n' || c== '\r'){

      if(!skipLine && index !=0 ) {
        tmpStr[index] = '\0';
        processLine(tmpStr);
      }
      skipLine = 0;
      index=0;
      continue;
    }

    if(!skipLine && (c!= ' ' || c!='\t' || c== '\n' || c== '\r')) {
      tmpStr[index++] = c;
    }
  }
  assert(hostname != NULL);
  assert(port != -1);
  // printf("hostname=%s\n", hostname);
  // printf("port=%d\n", port);
}

void initConnectorAgent(){
  metaDataListHead = NULL;
  // printf("original LI=%d\n",LITTLE_ENDIAN);
  // printf("original BI=%d\n",BIG_ENDIAN);
  // printf("original BO=%d\n",BYTE_ORDER);
  endian = testEndian();
  readProperties();

  // Checks to ensure proper program execution
  // Too many bit level conversions!
  assert (sizeof(unsigned short) == sizeof(uint16_t));
  assert (sizeof(short) == sizeof(uint16_t));
}

long long getTimeInMillis(){
  
  struct timeval tv;
  long long retVal = 0;
  if(gettimeofday(&tv, NULL)){
    perror("Could not find time of the day");
    exit(-1);
  }
  retVal = (long long)tv.tv_sec*1000 ;
  double x = ((double)tv.tv_usec)/1000.00;
  retVal += (long long)x;
  if ((x-(int)x) >= 0.5) retVal++;
  return retVal;
}


// int main(int argc, char *argv[]) {
// 
// 	if (argc != 1) {
// 	    fprintf(stderr,"usage: %s\n", argv[0]);
// 	    exit(1);
// 	}
// 
//   initConnectorAgent();
//   SensorMetaData* sensorId = lookup("12GREENSPN000007014000007E1A285");
//   int count=0;
//   int i;
//   int size=sizeof(double)*4;
//   double* buf = (double*) malloc(size);
//   long long t;
//   for(count=0;count<10; count++){
// 
//     for(i=0;i<4;i++){
//       buf[i] = count + (double)(i)/10.0;
//       // printf("buf[%d]=%f\n",i,buf[i]);
//     }
//     // printf("Sending %d data:", count);
//     t = getTimeInMillis();
//     printf("timestamp=%lld\n",t);
//     usleep(1000000);
//     sendData(sensorId, buf, size, t);
//     usleep(2000000);
//   }
//   free(buf);
// }
