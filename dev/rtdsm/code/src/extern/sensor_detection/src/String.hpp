#ifndef CDL_STRING_H
#define CDL_STRING_H



typedef struct Cdl_String_s {
    int length, capacity;
    char *str;
} Cdl_String;


void Cdl_String__ctor(Cdl_String *const s);
void Cdl_String__dtor(Cdl_String *const s);

void Cdl_String__fcat(Cdl_String *const s, const char *fm, ...);
void Cdl_String__fcatl(Cdl_String *const s, const char *fmt, ...);
void Cdl_String__cat(Cdl_String *s, const char *cs);



static const char Cdl_String_id[] = "$Id: String.hpp 153 2007-09-24 20:10:37Z ljmiller $";




#endif



// vim: set sw=4 sts=4 expandtab ai:
