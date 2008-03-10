#include "Array.hpp"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <stdio.h>
#include "String.hpp"

// $Id: Array.cpp 153 2007-09-24 20:10:37Z ljmiller $



void Cdl_Array__ctor(Cdl_Array *const a, const int id) {
    a->id = id;
    a->length = 0;
    a->capacity = 5;
    a->elements = (Cdl_Element *) malloc(a->capacity*sizeof(Cdl_Element));
}

void Cdl_Array__dtor(const Cdl_Array *const a) {
    assert(a->capacity >= 5);
    assert(a->length >= 0);
    for (int i = 0; i < a->length; i++) {
        Cdl_Element__dtor(&a->elements[i]);
    }
    free(a->elements);
}

void
Cdl_Array__append(Cdl_Array *const a, const Cdl_Element e) {
    
    //printf("elements: %p\n", a->elements);
    if (a->length == a->capacity) {
        a->capacity *= 2;
        a->elements = (Cdl_Element *) realloc(a->elements, a->capacity*sizeof(Cdl_Element));
        //printf("realloc to %d: elements: %p\n", a->capacity, a->elements);
    }

    assert(a->length < a->capacity);

    a->elements[a->length++] = e;
}

Cdl_Element
Cdl_Element__make_lo_res(double v) {
    Cdl_Element e;
    e.type = CDL_ET_LO_RES;
    e.value.dble = v;
    return e;
}

Cdl_Element
Cdl_Element__make_hi_res(double v) {
    Cdl_Element e;
    e.type = CDL_ET_HI_RES;
    e.value.dble = v;
    return e;
}

Cdl_Element
Cdl_Element__make_string(const char *s) {
    Cdl_Element e;
    e.type = CDL_ET_STRING;
    e.value.strng = (const char *) malloc(strlen(s) + 1);
    strcpy((char *) e.value.strng, s);
    return e;
}

void
Cdl_Element__dtor(const Cdl_Element *const e) {
    if (e->type == CDL_ET_STRING) {
        // To get rid of warning about discarding qualifier. -kec
        free((void *) (e->value.strng));
        ((Cdl_Element *) e)->value.strng = 0;
    }
}



// vim: set sw=4 sts=4 expandtab ai:
