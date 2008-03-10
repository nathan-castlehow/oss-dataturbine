/*
We are opening a device(datalogger) specified as argument 1, loading a program from an array
into the datalogger. Inside the main function we write certain commands directly to the datalogger,then load the 
program from array to the datalogger. We create a reader thread which reads the output from datalogger to 
an array which is parsed and used for knowing if a sensor is connected to datalogger or not. 
We also have a loop in main function where we wait for input from standard input incase if the user 
wants to intervene manually.
*/

#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <stdio.h>
#include <assert.h>
#include <ctype.h>
#include <pthread.h>
#include <string.h>
#include <stdlib.h>
#include <termios.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <errno.h>
#include "dlcomm.hpp"
#include "Program.hpp"
#include "Array.hpp"



int main(int argc, char *argv[]) {

    int ec;

#if 0
    static char buf[100000];

    int fd = open("dump-4.bd", O_RDONLY);
    assert(fd > 0);
    int len = read(fd, buf, sizeof buf);

    int n_arrays;
    const Cdl_Array *arrays = decode_binary_dump(buf, len, &n_arrays);

    for (int i = 0; i < n_arrays; i++) {
        const Cdl_Array *a = &arrays[i];
        printf("array id: %d, n: %d\n", a->id, a->length);
        for (int j = 0; j < a->length; j++) {
            Cdl_Element *e = &a->elements[j];
            switch (e->type) {
                case CDL_ET_LO_RES:
                case CDL_ET_HI_RES:
                    printf("element %d: %f\n", j, e->value.dble);
                    break;
                case CDL_ET_STRING:
                    printf("element %d: %s\n", j, e->value.strng);
                    break;
                default:
                    assert(0);
                    abort();
            }
        }
    }

    for (int i = 0; i < n_arrays; i++) {
        const Cdl_Array *a = &arrays[i];
        Cdl_Array__dtor(a);
    }
    free((void*) arrays);
#endif

    assert(argc == 2);

    int fd = open(argv[1], O_RDWR);
    if (fd < 0) {
        int en = errno;
        fprintf(stderr, "Error opening %s: %s\n", argv[1], strerror(en));
        assert(0);
        abort();
    }

    //connect with datalogger
    init_conn(fd);   
    printf("\n...Established connection to datalogger...\n");

    //wake up datalogger so we can enter commands
    ec = wake_up(fd);	
    assert(ec == 0);
    printf("\n...Datalogger is ready for commands...\n");

    //Run clearing program command
    ec = load_blank_program(fd);
    assert(ec == 0);
    printf("\n...Program 0 (empty program) loaded successfully...\n");

    // Load program to ...
    Cdl_Program prog;

    Cdl_Program__ctor(&prog, 5);
    // Internal temp: input_loc
    Cdl_Program__p17(&prog, 0);
    // Sample: reps, input_loc
    Cdl_Program__p70(&prog, 1, 0);


    load_Program(fd, 1, &prog);

    Cdl_Program__dtor(&prog);

    return 0;
}



// vim: set sw=4 sts=4 expandtab ai:
