#########################################################
# Makefile for flog
# Requires GNU make!
# pfh 9/02
#########################################################

export

# Figure out which operating system to build for
OPSYS = $(shell uname -s)

# Destinations
LIB_DIR = .

# Include directory, used by etags
INC_DIR = .

# library
flog_lib = $(LIB_DIR)/libflog.a
flog_src = flog.c
flog_obj = flog.o
flog_inc = flog.h

#########################################################
# Linux version
ifeq ($(OPSYS), Linux)
 CC = gcc
 CC_FLAGS = -ggdb -g3 -pthread -Wall
 CC_INCLUDES = -I$(INC_DIR)
 CC_DEFINES = -DIntel_Linux 
 external_LIBS = -lpthread -lm
 LINKER = gcc
 LINKER_FLAGS = -L$(LIB_DIR) -ggdb -g3
 LINKER_ENTRY =
 CTOHFLAGS = 
endif
#########################################################
# Mac OSX
ifeq ($(OPSYS), Darwin)
 cc = gcc
 CC_FLAGS = -ggdb -g3 -pthread -Wall
 CC_INCLUDES = -I$(INC_DIR)
 CC_DEFINES = -DMacOSX
 external_LIBS = -lpthread -lm
 LINKER = gcc
 LINKER_FLAGS = -L$(LIB_DIR) -ggdb -g3
 LINKER_ENTRY =
 CTOHFLAGS = 
endif

#########################################################
# Solaris
ifeq ($(OPSYS), SunOS)
  CC = cc
  MAKEDEP = makedepend
  CC_FLAGS = -xCC -Xa -g
  CC_INCLUDES =  -I$(INC_DIR)
  CC_DEFINES =  -D_POSIX_C_SOURCE=199506L -D__EXTENSIONS__ -DSun_Solaris \
		-D_REENTRANT
  external_LIBS = -lm -lpthread -lposix4 -lsocket -lrpcsoc \
		-lnsl -lc -lucb 
  LINKER = cc
  LINKER_FLAGS = -L$(LIB_DIR) -z muldefs -L/usr/ucblib -R/usr/ucblib
  LINKER_ENTRY =
endif
#########################################################
# Irix 6.5 or greater, I think.
ifeq ($(OPSYS), IRIX64)
  CC = cc
# cc flags note - warning 1275 is identical redefinition flag.
# Supressed because I can't seem to get rid of it in the source.
# pfh 10.17.00
  CC_FLAGS = -n32 -mips3 -g -TENV:align_aggregate=8 \
		-diag_suppress1275 
  CC_INCLUDES = -I$(INC_DIR)
  CC_DEFINES = -DSGI_IRIX 
  MAKEDEP = makedepend
  external_LIBS = -lds -lpthread -lgen -lm
  LINKER = cc
# Linker flags note - disabling message 84 means no warnings about
# libraries not needed for the link. SHould be harmless.
# can also change linker to ld and then use show_unref to find dead code.
# pfh 10/17/00
  LINKER_FLAGS = -L$(LIB_DIR) -lds -LD_MSG:off=84
  LINKER_ENTRY =
endif 

#########################################################
# OS-common stuff follows
#########################################################

# Emacs tag file (output from program)
etags = ETAGS

#########################################################
# Build targets - default is to just build binary and library
default-build: $(flog_lib)

# Clean it up
# Note that leading dash means ignore return code of rm
clean:
	@echo "Cleaning up"
	-rm -f $(flog_obj)
	-rm -f $(flog_lib)
#
# Distclean removes the font lock cache and etags as well
distclean:
	@echo "Cleaning up for distribution"
	-rm -f $(etags)
	-rm -f $(flog_obj)
	-rm -f $(flog_lib)


# end build targets
#########################################################
# Rulesets, new (GNU) syntax
#
%.o : %.c
	$(CC) -c $< $(CC_FLAGS) $(CC_DEFINES) $(CC_INCLUDES)

#########################################################
#
# Build rules/dependencies
#
$(flog_lib): $(flog_obj)
	ar r $(flog_lib) $(flog_obj)
	ranlib $(flog_lib)

# Make eetags for Emacs fans; ignore errors if not installed
$(etags): $(flog_src) $(flog_inc)
	-etags $(flog_src) $(INC_DIR)/$(flog_inc)

# pfh 7/23/04
splint: $(flog_src)
	splint +unixlib +posixlib -I$(INC_DIR) $(flog_src) $(flog_inc)

