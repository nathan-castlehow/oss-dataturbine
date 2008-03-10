/*!
\file platform_specific.h
\author Greg Crawford 
\date 10/11/06
\brief Platform-specific code and macros. Should be included by all NHCP source code.
*/

#ifndef _NEES_PLATFORM_SPECIFIC_H_
#define _NEES_PLATFORM_SPECIFIC_H_

/// Windows defines
#ifdef _MSC_VER
#define SLEEP(milliseconds) Sleep(milliseconds)
#define LIB_EXTENSION "dll"
#define _EXPORT __declspec(dllexport)

// Ahh, sweet comforting Unix.
#else
#define SLEEP(milliseconds) usleep(milliseconds * 1000)
#define LIB_EXTENSION "so"
#define _EXPORT
#endif

/// OSX uses a different extension by default
#if defined(__APPLE__)
#undef LIB_EXTENSION
#define LIB_EXTENSION "dylib"
#endif

#endif // header file sentinel
