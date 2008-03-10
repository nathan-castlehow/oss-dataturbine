# 
# Makefile for NSDS driver
# Paul Hubbard hubbard@mcs.anl.gov
# 8/27/02
# 9/10/03 Removed fake_daq to its own project

VERSION = 1.5

# Figure out which operating system to build for
OPSYS = $(shell uname -s)

CC = gcc
CFLAGS = -g -Wall -I../flog -DDIST_VER=$(VERSION)

LDFLAGS = -g -L. -L../flog
LOADLIBS = -lnsds_util -lflog -lpthread -lm

# Target: util library
LIB_TGT = libnsds_util.a
LIB_SRC = nsds_util.c
LIB_OBJ = nsds_util.o
LIB_HDR = nsds_util.h

# Emacs source code browser/db
etags = TAGS

# Hold-all 
SOURCE = $(LIB_SRC) $(LIB_HDR) 
OBJECTS = $(LIB_OBJ) 
INCLUDES = $(LIB_HDR)

# Default make target
bins: $(LIB_TGT)

#######################
# OS-specific stuff
# Mac OSX
ifeq ($(OPSYS), Darwin)
 CFLAGS += -I/sw/include/gnugetopt
 LOADLIBS = -lnsds_util -lflog -lgnugetopt -lpthread -lm
 LDFLAGS += -L/sw/lib
endif

# Explicit dependency
$(OBJECTS): $(SOURCE) $(INCLUDES)

# General compliation rule
.c.o:	$(SOURCE) $(INCLUDES)
	$(CC) -c $(CFLAGS) $< 

$(LIB_TGT): $(LIB_OBJ)
	ar r $(LIB_TGT) $(LIB_OBJ)
	ranlib $(LIB_TGT)
clean:
	-rm -f $(objs) $(etags)	$(DRV_OBJ) $(DRV_APP) \
		$(LIB_OBJ) $(LIB_TGT) 

