GSOAP_PATH=/home/rohit/gsoap-linux-2.7
# Added by Vinay
# Start
CONNECTOR_PATH=connector_interface/
# End

# Altered by Vinay
# Start
# Commented the below line
# CPPFLAGS := -I$(GSOAP_PATH)
# Added the line below
CPPFLAGS := -I$(GSOAP_PATH) -I$(CONNECTOR_PATH)
# End

CC := gcc
CFLAGS := -g -pthread -std=c99 -Wall -Wno-unused

CXX := g++
CXXFLAGS := -g -pthread -Wall -Wno-unused

LDFLAGS := -L/usr/lib/mysql
LDLIBS := -lmysqlclient

# DO NOT DELETE THE BLANK LINE AFTER THIS!
CSI_COMM_SRCS := \
 csi_comm.c \

CSI_COMM_OBJS := $(CSI_COMM_SRCS:.c=.o)

# Altered by Vinay
# Start
# Commented the lines below
## # DO NOT DELETE THE BLANK LINE AFTER THIS!
## DETECT_SRCS  = \
##  detect.cpp \
##  dlcomm.cpp \
##  dbinsert.cpp \
##  Program.cpp \
##  Array.cpp \
##  String.cpp \
##  detectsensor.cpp \
## 
# Added the lines below
# DO NOT DELETE THE BLANK LINE AFTER THIS!
DETECT_SRCS  = \
 detect.cpp \
 dlcomm.cpp \
 Program.cpp \
 Array.cpp \
 String.cpp \
 detectsensor.cpp \
 $(CONNECTOR_PATH)/connector.c \

# End
# Altered by Vinay
# Start
# Commented below line
## DETECT_OBJS := $(DETECT_SRCS:.cpp=.o) soapC.o soapClient.o
# Added below line
DETECT_OBJS := $(DETECT_SRCS:.cpp=.o)
# End

# Altered by Vinay
# Start
# Commented below lines
## # DO NOT DELETE THE BLANK LINE AFTER THIS!
## KEN_SRCS := \
##  ken.cpp \
##  dlcomm.cpp \
##  dbinsert.cpp \
##  Program.cpp \
##  Array.cpp \
##  String.cpp \
## 
# Added the lines below
# DO NOT DELETE THE BLANK LINE AFTER THIS!
KEN_SRCS := \
 ken.cpp \
 dlcomm.cpp \
 Program.cpp \
 Array.cpp \
 String.cpp \
 $(CONNECTOR_PATH)/connector.c \

#End
KEN_OBJS := $(KEN_SRCS:.cpp=.o)

all: csi_comm ken detect

# Altered by Vinay
# Start
# Commented two lines below
## detect: $(DETECT_OBJS)
## 	g++ $(CXXFLAGS) $(LDFLAGS) -o detect $(DETECT_OBJS) $(GSOAP_PATH)/stdsoap2.c $(LDLIBS)
# Added the below 2 lines
detect: $(DETECT_OBJS)
	g++ $(CXXFLAGS) $(LDFLAGS) -o detect $(DETECT_OBJS) $(LDLIBS)
# End

ken: $(KEN_OBJS)
	g++ $(CXXFLAGS) $(LDFLAGS) -o ken $(KEN_OBJS) $(LDLIBS)

# Commented by Vinay
# Start
## webservice.o: webservice.c soapH.h InstrumentMetadataSoapBinding.nsmap
## 
## soapC.c soapClient.c soapH.h InstrumentMetadataSoapBinding.nsmap: webservice.h
## 	$(GSOAP_PATH)/soapcpp2 -c webservice.h
## 
## webservice.h: InstrumentMetadata.wsdl
## 	$(GSOAP_PATH)/wsdl2h -c -o webservice.h <InstrumentMetadata.wsdl
# End

csi_comm: $(CSI_COMM_OBJS)
	gcc $(CFLAGS) $(LDFLAGS) -o csi_comm $(CSI_COMM_OBJS) $(LDLIBS)

# Just make all .o file depend on all header files.  Not that efficient, but
# compile time is not an issue now.
# Altered by Vinay
# Start
# Commented the line below
## $(CSI_COMM_OBJS) $(DETECT_OBJS) $(KEN_OBJS): $(wildcard *.hpp) $(wildcard *.h) soapH.h
# Added the line below
$(CSI_COMM_OBJS) $(DETECT_OBJS) $(KEN_OBJS): $(wildcard *.hpp) $(wildcard *.h)
# End

.PHONY: clean
clean:
	rm -f *.o detect csi_comm ken core core.*
	rm -f InstrumentMetadataSoapBinding.*.xml
	rm -f InstrumentMetadataSoapBinding.nsmap
	rm -f soapC.c
	rm -f soapClient.c
	rm -f soapClientLib.c
	rm -f soapH.h
	rm -f soapServer.c
	rm -f soapServerLib.c
	rm -f soapStub.h
	rm -f webservice.h
