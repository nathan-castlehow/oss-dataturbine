#include "stdio.h"

#include "soapH.h"
#include "InstrumentMetadataSoapBinding.nsmap"

int main()
{
  struct soap soap;

  //expected temperature returned
  //by my web service
  char* info;

  soap_init(&soap);
  
  //you will find in the soapClient.c file
  //the prototype of the remote procedure 
  //you want to consume (in bold below)
  if(soap_call_ns1__getSensor(&soap, 
                              NULL  /*endpoint address*/, 
                              NULL  /*soapAction*/, 
                              "sen1"     /*fixed value needed for my remote procedure*/, 
                              &info /*temperature returned*/
                             )== SOAP_OK)

    printf("info is: %s\n",info);

  else

    soap_print_fault(&soap, stderr); 

  soap_destroy(&soap); 
  soap_end(&soap); 
  soap_done(&soap); 

  return 0;

}



// vim: set sw=4 sts=4 expandtab ai:
