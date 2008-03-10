#include "String.hpp"
#include <stdarg.h>
#include <stdlib.h>
#include <assert.h>
#include <stdio.h>
#include <string.h>



static void fcat_helper(Cdl_String *const s, const char *fmt, va_list ap);



void Cdl_String__ctor(Cdl_String *const s) {
    s->length = 0;
    s->capacity = 50;
    // Allocate one more than capacity to handle terminating null byte.
    s->str = (char *) malloc(s->capacity + 1);
    s->str[0] = '\0';
}

void Cdl_String__dtor(Cdl_String *const s) {
    assert(s->length >= 0);
    assert(s->capacity >= 50);
    assert(s->str != 0);
    free(s->str);
}

void
Cdl_String__fcat(Cdl_String *const s, const char *fmt, ...) {

    va_list ap;
    va_start(ap, fmt);
    fcat_helper(s, fmt, ap);
    va_end(ap);
}

void
Cdl_String__fcatl(Cdl_String *const s, const char *fmt, ...) {

    va_list ap;
    va_start(ap, fmt);
    fcat_helper(s, fmt, ap);
    va_end(ap);

    Cdl_String__cat(s, "\r");
}

static void
fcat_helper(Cdl_String *const s, const char *fmt, va_list ap) {

    int ec = -1;

    /*
     * Start with a buffer of 64 that is on the stack for efficiency, and
     * double every iteration if it is too small.  Not very efficient, but
     * should be okay for most applications of this class.
     */

    char stack_buf[64];
    char *buf = stack_buf;
    int n = sizeof stack_buf;
    while (1) {

        // In case nothing is printed, we need to null-terminate before we
        // start.  Otherwise, it is not null-terminated, it seems. -kec
        buf[0] = '\0';
        ec = vsnprintf(buf, n, fmt, ap);
        assert(ec >= 0);

        // Some versions of [v]snprintf() return the number of characters that
        // would have been printed had the buffer been long enough.  Others
        // just return size - 1 if the buffer is too small.
	if (ec < n - 1) {
	    break;
	}

	n *= 2;
	if (buf != stack_buf) {
            free(buf);
	}

        buf = (char *) malloc(n);
        assert(buf != NULL);
    }

    Cdl_String__cat(s, buf);

    if (buf != stack_buf) {
        free(buf);
    }
}

void
Cdl_String__cat(Cdl_String *const s, const char *str) {

    while (s->length + int(strlen(str)) > s->capacity) { 
        s->capacity *= 2;
        s->str = (char *) realloc(s->str, s->capacity + 1);
    }

    strcat(s->str, str);
    s->length += strlen(str);
}



// vim: set sw=4 sts=4 expandtab ai:
