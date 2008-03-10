#ifndef CDL_ARRAY_H
#define CDL_ARRAY_H

// $Id: Array.hpp 153 2007-09-24 20:10:37Z ljmiller $



// XXX - Since this is a public header file, should be put in subdir. -kec
#include "String.hpp"


enum ElementType {
	// Make zero an invalid one, to help catch uninitialized things. -kec
	CDL_ET_FIRST,
	CDL_ET_LO_RES,
	CDL_ET_HI_RES,
	CDL_ET_STRING,
	CDL_ET_LAST,
};


typedef struct Cdl_Element_s {
    ElementType type;
    union {
        // Modify variable name so it's not a keyword.
        double dble;
        // Modify variable name so it won't clash with possible std::string.
        const char *strng;
    } value;
} Cdl_Element;

typedef struct Cdl_Array_s {
    int id;
    int length, capacity;
    Cdl_Element *elements;
} Cdl_Array;



void Cdl_Array__ctor(Cdl_Array *const, int id);
void Cdl_Array__dtor(const Cdl_Array *const);
void Cdl_Array__append(Cdl_Array *const a, const Cdl_Element e);

Cdl_Element Cdl_Element__make_lo_res(const double);
Cdl_Element Cdl_Element__make_hi_res(const double);
Cdl_Element Cdl_Element__make_string(const char *const);
void Cdl_Element__dtor(const Cdl_Element *const);



static char Cdl_Array_id[] = "$Id: Array.hpp 153 2007-09-24 20:10:37Z ljmiller $";



#endif



// vim: set sw=4 sts=4 expandtab ai:
