CCC	  = gcc
CCOPT = -O0 -g
CCLIBS = 
CCFLAGS = $(CCOPT) $(CCLIBS)
CCLNFLAGS =
CPP_EX = connector


all: $(CPP_EX)

connector : connector.c
	$(CCC) $(CCFLAGS) connector.c -o connector $(CCLNFLAGS)

clean:
	rm -rf connector core*
