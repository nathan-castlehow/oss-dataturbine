# 
# Makefile for fake_daq C code
# Paul Hubbard hubbard@sdsc.edu
# 8/27/02, modified 9/10/03
# Rewrite March 2006, now bundling flog and nsds-util as part of the package.

VERSION = 1.3.1
DISTNAME = fake-daq

# Figure out which operating system to build for
OPSYS = $(shell uname -s)

CC = gcc
CFLAGS = -g -Wall -Iflog -Insds-util

FLOG_DIR = flog
UTIL_DIR = nsds-util

LDFLAGS = -g -L. -L$(FLOG_DIR) -L$(UTIL_DIR)
LOADLIBS = -lflog -lnsds_util -lpthread -lm

# Target: flog library
FLOG_LIB = $(FLOG_DIR)/libflog.a

# Target: nsds-util library
UTIL_LIB = $(UTIL_DIR)/libnsds_util.a

# Target: Fake DAQ program
DAQ_APP = fake_daq
DAQ_SRC = fake_daq.c
DAQ_OBJ = fake_daq.o

# Emacs source code browser/db
etags = TAGS

# CVS changelog
CLOG = ChangeLog

# Hold-all 
SOURCE = $(DAQ_SRC)
OBJECTS = $(DAQ_OBJ)

# Default make target
bins: $(DAQ_APP) $(etags)

# add doxygen and changelog, mostly for developers
all: $(DAQ_APP) $(etags) doxygen $(CLOG)

#######################
# OS-specific stuff
# Mac OSX
ifeq ($(OPSYS), Darwin)
 CFLAGS += -I/sw/include/gnugetopt
 LOADLIBS = -lnsds_util -lflog -lgnugetopt -lpthread -lm
 LDFLAGS += -L/sw/lib
endif

ifeq ($(OPSYS), SunOS)
 LOADLIBS += -lsocket -lnsl -lresolv
endif

# Explicit dependency
$(OBJECTS): $(SOURCE) 

# fake daq depends on the two libraries
$(DAQ_APP): $(FLOG_LIB) $(UTIL_LIB)

# ...and nsds-util depends on flog
$(UTIL_LIB): $(FLOG_LIB)

# Build rule for the flog library
$(FLOG_LIB): $(FLOG_DIR)/flog.c
	cd $(FLOG_DIR); make; cd ..

# Build rule for the util library
$(UTIL_LIB): $(UTIL_DIR)/nsds_util.c
	cd $(UTIL_DIR); make; cd ..

# General compliation rule
.c.o:	$(SOURCE) 
	$(CC) -c $(CFLAGS) $< 

# Fake daq app
$(DAQ_APP): $(DAQ_OBJ) $(LIB_TGT)
	$(CC) $(LDFLAGS) $(DAQ_OBJ) $(LOADLIBS) -o $(DAQ_APP)

clean:
	-rm -f $(etags) $(DAQ_APP) $(DAQ_OBJ) $(FLOG_LIB) $(UTIL_LIB)

# Etags build rule
$(etags):	$(SOURCE)
	-etags $(SOURCE) 

# Doxygen docs - see also Doxyfile
doxygen: $(SOURCE)
	-doxygen

# Changelog - must have cvs2cl.pl in path
$(CLOG): $(SOURCE)
	-cvs2cl.pl

dist:
	mkdir ../$(DISTNAME)-self
	cp -R * ../$(DISTNAME)-self
	mv ../$(DISTNAME)-self ./$(DISTNAME)
	cd $(DISTNAME) && rm -rf `find -name CVS`
	tar cvzf $(DISTNAME).tar.gz $(DISTNAME)
	rm -rf $(DISTNAME)
