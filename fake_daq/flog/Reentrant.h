/*!
\file Reentrant.h
\brief Code from http://www.opensslbook.com/code.html with mods by Paul Hubbard
Contains macros, defines and prototypes, all of which related to threading, reentrancy and the like.
\date July 25 2006
 */
#ifndef REENTRANT_H
#define REENTRANT_H

#ifndef WIN32
  #include <unistd.h>
  #include <pthread.h>
#else
  #include <windows.h>
  #include <process.h>
#endif

#include <openssl/ssl.h>

/*! @note Clever macros for cross-platform thread functions. Borrowed from SSL book */
#ifndef WIN32
#include <pthread.h>
/*! @def THREAD_CC Used for C compiler on Windows */
#define THREAD_CC 
/*! @def THREAD_TYPE
pthread type on Unix, uint16_t on windows */
#define THREAD_TYPE                    pthread_t
/*! @def THREAD_CREATE Thread creation macro, pthreads on unix and _beginthred on win32 */
#define THREAD_CREATE(tid, entry, arg) pthread_create(&(tid), NULL, \
                                                      (entry), (arg))
#else
#include <windows.h>
#define THREAD_CC                      __cdecl
#define THREAD_TYPE                    DWORD
#define THREAD_CREATE(tid, entry, arg) do { _beginthread( (pfunc_t)(entry), 0, (arg));\
    (tid) = GetCurrentThreadId();   \
} while (0)
#endif

#if defined(WIN32)
#define MUTEX_TYPE HANDLE
#define MUTEX_SETUP(x) (x) = CreateMutex(NULL, FALSE, NULL)
#define MUTEX_CLEANUP(x) CloseHandle(x)
#define MUTEX_LOCK(x) WaitForSingleObject((x), INFINITE)
#define MUTEX_UNLOCK(x) ReleaseMutex(x)
#define THREAD_ID GetCurrentThreadId( )
#elif defined(_POSIX_THREADS)
/* _POSIX_THREADS is normally defined in unistd.h if pthreads are available
on your platform. */
#define MUTEX_TYPE pthread_mutex_t
#define MUTEX_SETUP(x) pthread_mutex_init(&(x), NULL)
#define MUTEX_CLEANUP(x) pthread_mutex_destroy(&(x))
#define MUTEX_LOCK(x) pthread_mutex_lock(&(x))
#define MUTEX_UNLOCK(x) pthread_mutex_unlock(&(x))
#define THREAD_ID pthread_self( )
#else
#error You must define mutex operations appropriate for your platform!
#endif

// Function prototypes

int THREAD_setup(void);
int THREAD_cleanup(void);


#endif /* REENTRANT_H */
