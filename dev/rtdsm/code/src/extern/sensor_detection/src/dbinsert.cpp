#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "mysql/mysql.h"

/*DB info*/
#define host "yunyi.sdsc.edu"
//#define host "137.219.227.234"
#define username "lake"
#define password "demo4march"
#define database "lake"
#define port 3301
#define MAX_LEN 120
#define MAX_COLS 100
/*Insert stmt body*/
#define QUERYBODY1 "INSERT INTO "
#define QUERYBODY2 " VALUES ("
#define QUERYBODY3 ")"

/* Keeping the connection hanlder global*/
MYSQL *conn;

/* Initialize connection with MySql */
int initDB() {

	conn = mysql_init(NULL);
	if (conn == NULL)
		return 0;
	return 1;
}

/* Open connection with MySql */
int openDB() {
	
	if (!mysql_real_connect(conn,host,username,password,database,port,NULL,0))
		return 0;
	return 1;
}

/*Close connection with MySql*/
void closeDB() {
	mysql_close(conn);
}

/*To execute MySql statements*/
int mysql_exec_sql(const char *create_definition) {
   return mysql_real_query(conn,create_definition,strlen(create_definition));
}

void
tokenize(char infostr[], int* count) {

	char* column;
	//printf("Before, Count is %d\n", *count);
	column = strtok(infostr, ",");
	//printf("%s\n",column);
	
	while ( (column = strtok(NULL, ",")) != NULL) {		
		//printf("%s\n",column);
		(*count) = (*count) + 1;
	}	
	//printf("After, Count is %d\n", *count);
}


/* InsertData in required tables*/
/* Here connection to the db can be opened for every insertion OR
   Once before all insertions are done and clode after all insertions are done */
int insertData(const char *tableName, const char* values) {
	
	int i;
	int count = 0;
	
	if (!initDB()) {		
		return 0;
	}
	
	if (openDB() == 0) {		
		return 0;
	}
	
	/*Counting how many 0's we need to append*/
	char *tempstr = (char*)malloc(strlen(values) + 1);
	if(tempstr == NULL)
		return 0;
	strcpy(tempstr,values);
	strcat(tempstr, "\0");		
	tokenize(tempstr, &count);
	free(tempstr);
	
	/*Calculate size for insert query*/
	int size = strlen(QUERYBODY1) + strlen(QUERYBODY2) + strlen(QUERYBODY3) + strlen(tableName) + strlen(values) + (count*2) +1;
	
	/*Allocate memory*/
	char* mysql_stmt = (char*)malloc(size);
	if(mysql_stmt == NULL)
		return 0;
		
	strcpy(mysql_stmt, QUERYBODY1);
	strcat(mysql_stmt, tableName);
	strcat(mysql_stmt, QUERYBODY2);
	strcat(mysql_stmt, values);
	
	/*Append 0's*/
	for(i = 0; i < count; i++) {		
		strcat(mysql_stmt,",");
		strcat(mysql_stmt,"0");
	}
	
	strcat(mysql_stmt, QUERYBODY3);
	strcat(mysql_stmt, "\0");
	
	printf("stmt: %s\n",mysql_stmt);
	
	/*Execute statement*/
	if(mysql_exec_sql(mysql_stmt)!=0) {	/*failure*/
		/*Free memory and close connection*/
		printf( "Failed to insert into table: Error: %s\n", mysql_error(conn));
		free(mysql_stmt);
		closeDB();		
		return 0;
	}	
	
	/*Free memory and close connection*/
	free(mysql_stmt);
	closeDB();	
	return 1;	
}



// vim: set sw=4 sts=4 expandtab ai:
