/* ************************************************************************* */
/* flog.c     Fermilab CDF Stager Package, routines for message logging   */
/*                                                                           */
/* Copyright (c) 2000 Universities Research Association, Inc., author, and   */
/*               others. All Rights Reserved.                                */
/*                                                                           */
/* Author:       Stephan Lammel, Fermilab                                    */
/*                                                                           */
/* Disclaimer:   This software is licensed on an "as is" basis only.  Author */
/*               and Universities Research Association, Inc. (URA)  make  no */
/*               representations or warranties, express or implied.  By  way */
/*               of example but not of limitation,  author and URA  make  no */
/*               representations or WARRANTIES OF MERCHANTABILITY OR FITNESS */
/*               FOR ANY PARTICULAR PURPOSE,  that the use of  the  licensed */
/*               software  will  not  infringe  any  patent,  copyright,  or */
/*               trademark,  or as to the use (or the results of the use) of */
/*               the licensed  software  or written  material  in  terms  of */
/*               correctness,   accuracy,   reliability,   currentness    or */
/*               otherwise.   The  entire  risk  as  to  the   results   and */
/*               performance of the licensed  software  is  assumed  by  the */
/*               user.  Author  and  Universities Research Association, Inc. */
/*               and any of its trustees,  overseers,  directors,  officers, */
/*               employees, agents, or contractors shall not be liable under */
/*               any claim, charge, or demand,  whether in  contract,  tort, */
/*               or otherwise,  for any and all loss, cost,  charge,  claim, */
/*               demand, fee, expense, or damage of  every nature  and  kind */
/*               arising out of, connected with, resulting from or sustained */
/*               as a result  of using  this software.  In  no  event  shall */
/*               author or URA be liable for special,  direct,  indirect  or */
/*               consequential  damages,  losses,  costs,  charges,  claims, */
/*               demands, fees, or expenses of any nature or kind.           */
/*                                                                           */
/* This material resulted  from work developed  under a  Government Contract */
/*               and is subject to the following license:                    */
/*               The Government retains a paid-up, nonexclusive, irrevocable */
/*               worldwide license  to reproduce, prepare  derivative works, */
/*               perform  publicly  and  display  publicly  by  or  for  the */
/*               Government,  including the right  to  distribute  to  other */
/*               Government contractors.  Neither the United States nor  the */
/*               United  States  Department  of  Energy,  nor  any of  their */
/*               employees,  makes  any warranty,  express  or  implied,  or */
/*               assumes  any  legal  liability or  responsibility  for  the */
/*               accuracy, completeness, or usefulness  of  any information, */
/*               apparatus, product, or  process  disclosed,  or  represents */
/*               that its use would not infringe privately owned rights.     */
/*                                                                           */
/* The information in this software is subject to change without notice  and */
/*               should not be construed as a commitment by  the  author  or */
/*               Universities Research Association, Inc.                     */
/* ************************************************************************* */

#include <sys/types.h>
#include <inttypes.h>
#include <sys/time.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <time.h>
#include <pthread.h>
#include <syslog.h>
#include <pwd.h>
#include <grp.h>
#include <netdb.h>
#include <errno.h>

// pfh 8/8/01, want prototypes for mutex lock/unlock
#include "flog.h"


/* ************************************************************************* */
/* Messages are logged in three locations:                                   */
/*    0) to standard output or standard error (depending on severity level)  */
/*    1) to the syslog (as stager daemon)                                    */
/*    2) to a logfile (default filename stg_<time>.log in current directory) */
/* Messages are automatically prefixed with any of the following:            */
/*    0x4000) the date (in %Y-%b-%d format)                                  */
/*    0x2000) the time (in %H:%M:%S format)                                  */
/*    0x1000) the milliseconds in the second                                 */
/*    0x0800) the user name                                                  */
/*    0x0400) the group name                                                 */
/*    0x0200) the host name                                                  */
/*    0x0100) the domain name                                                */
/*    0x0080) the parent process id                                          */
/*    0x0040) the process id                                                 */
/*    0x0020) the thread id                                                  */
/*    0x0010) the name of the routine that is logging the message            */
/*    0x0008) a character indicating the severity of the message             */
/*    0x0004) the full name of the severity level of the message             */
/*    0x0002) the error code of the message for warning level and higher     */
/*    0x0001) the error code of the message for all severity levels          */
/* Messages have a severity level of:                                        */
/*    0) (D) DEBUG (temporary debug message, logged to stdout and logfile)   */
/*    1) (A) ALERT (immediate action required)                               */
/*    2) (F) FATAL (fatal error condition)                                   */
/*    3) (E) ERROR (error condition)                                         */
/*    4) (W) WARNING (warning condition)                                     */
/*    5) (N) NOTICE (normal but significant message)                         */
/*    6) (P) PROGRESS (progress report)                                      */
/*    7) (I) INFO (informational message)                                    */
/*    8) (1) L1BUG (level 1, high level, debug message)                      */
/*    9) (2) L2BUG (level 2 debug message)                                   */
/*   10) (3) L3BUG (level 3 debug message)                                   */
/*   11) (4) L4BUG (level 4, low level, debug message)                       */
/*   12) (?) NOLOG (not to be printed debug message)                         */
/* ************************************************************************* */



/* ************************************************************************* */
/* parameters with file scope:                                               */
#define STG_PREFIX_LEN         192                   /* max length of prefix */
#define FLOG_CONTINUE       (FLOG_NOLOG + 1)
#define STG_FILE_PATH_MAX      512


/* ************************************************************************* */
/* types with file scope:                                                    */
typedef struct flog_file_s {
   FILE      *fp;
   char      name[STG_FILE_PATH_MAX + 1];
} flog_file_t;



/* ************************************************************************* */
/* variables with file scope:                                                */
static pthread_mutex_t   flog_mutex =                /* mutex for logging */
                            PTHREAD_MUTEX_INITIALIZER;
static flog_file_t    flog_file =              /* log file information */
                            {NULL, {'\0'}};
static uint32_t          flog_style[3] =                  /* prefix style */
                            {0x200a, 0x002a, 0x707a};
static int16_t           flog_report[3] =            /* report severities */
                            {FLOG_WARNING, FLOG_QUIET, FLOG_QUIET};
static const char        flog_slevelc[14] =             /* severity level */
                            {'D',                         /* character codes */
                             'A', 'F', 'E', 'W',
                             'N', 'P', 'I', '1', '2', '3', '4', '?',
                             '+'};
static const char        *flog_sleveln[14] =           /* severity level */
                            {"DEBUG",                          /* full names */
                             "ALERT", "FATAL", "ERROR", "Warning",
                             "Notice", "Progress", "Info",
                             "L1debug", "L2debug", "L3debug", "L4debug", "?",
                             "+"};



/* ************************************************************************* */
int flog_prefix(int16_t severity, const int ec, const char *routine,
                   char prefix[3][STG_PREFIX_LEN]) {
   int              status = FL_ERR_SUCCESS;
   uint32_t         style;
   int              rc;
   struct timeval   tp;
   struct tm        tm_now;
   uid_t            uid;
   struct passwd    pw_rec;
   char             pw_line[128];
   struct passwd    *pw_ptr;
   char             user[15+1];
   gid_t            gid;
   struct group     gr_rec;
   char             gr_line[128];
   struct group     *gr_ptr;
   char             group[15+1];
   char             hostname[31+1];
   struct hostent   *he_ptr;
   struct hostent   he_struct;
   char             he_buffer[512];
   int              he_errno;
   char             hostfull[63+1];
   pid_t            ppid = 0;
   pid_t            pid = 0;
   pthread_t        tid = 0;
   char             location[24+1];
   int16_t          slevel;
   int16_t          ecode = 0;
   char             *c_ptr;
   size_t           len;
   int              i;
   int              j;


   /* get list of all the information needed */
   /* ************************************** */
   style = flog_style[0] | flog_style[1] | flog_style[2];


   /* get date/time information as needed */
   /* *********************************** */
   if ( (style & 0x7000) != 0 ) {
      rc = gettimeofday(&tp, NULL);
      if ( rc != 0 ) {
         /* fall back to use time() and get time in seconds */
         tp.tv_sec = time(NULL);
         if ( tp.tv_sec == (time_t) -1 ) {
            tp.tv_sec = 0;
         }
         tp.tv_usec = 0;
      }
      /* convert time in sec since UTC to tm structure */
      (void) localtime_r((const time_t *) &tp.tv_sec, &tm_now);
   }


   /* get user name as needed */
   /* *********************** */
   if ( (style & 0x0800) != 0 ) {
      uid = getuid();
      rc = getpwuid_r(uid, &pw_rec, pw_line, sizeof(pw_line), &pw_ptr);
      if ( rc == 0 ) {
         (void) snprintf(user, sizeof(user), "%s", pw_rec.pw_name);
      } else {
         (void) snprintf(user, sizeof(user), "uid=%d", uid);
      }
      user[sizeof(user) - 1] = '\0';
   }


   /* get group name as needed */
   /* ************************ */
   if ( (style & 0x0400) != 0 ) {
      gid = getgid();
      rc = getgrgid_r(gid, &gr_rec, gr_line, sizeof(gr_line), &gr_ptr);
      if ( rc == 0 ) {
         (void) snprintf(group, sizeof(group), "%s", gr_rec.gr_name);
      } else {
         (void) snprintf(group, sizeof(group), "gid=%d", gid);
      }
      group[sizeof(group) - 1] = '\0';
   }


   /* get host/domain name as needed */
   /* ****************************** */
   if ( (style & 0x0300) != 0 ) {
      rc = gethostname(hostname, sizeof(hostname));
      if ( rc != 0 ) {
         (void) strncpy(hostname, "localhost", sizeof(hostname));
      }
      hostname[sizeof(hostname) - 1] = '\0';

#if defined(MacOSX)
      he_ptr = gethostbyname(hostname);
#else
#if defined(Intel_Linux)
      (void) gethostbyname_r(hostname,
         &he_struct, he_buffer, sizeof(he_buffer), &he_ptr, &he_errno);
#else
      he_ptr = gethostbyname_r(hostname,
         &he_struct, he_buffer, sizeof(he_buffer), &he_errno);
#endif
#endif
      if ( he_ptr == NULL ) {
         (void) snprintf(hostfull, sizeof(hostfull), "%s.unknown", hostname);
         hostfull[sizeof(hostfull) - 1] = '\0';
      }
      else {
         (void) strncpy(hostname, he_struct.h_name, sizeof(hostname));
         hostname[sizeof(hostname) - 1] = '\0';
         c_ptr = strchr(hostname, '.');
         if ( c_ptr != NULL ) {
            *c_ptr = '\0';
         }
         (void) strncpy(hostfull, he_struct.h_name, sizeof(hostfull));
         hostfull[sizeof(hostfull) - 1] = '\0';
      }
   }


   /* get parent process id as needed */
   /* ******************************* */
   if ( (style & 0x0080) != 0 ) {
      ppid = getppid();
   }


   /* get process id as needed */
   /* ************************ */
   if ( (style & 0x0040) != 0 ) {
      pid = getpid();
   }


   /* get thread id as needed */
   /* *********************** */
   if ( (style & 0x0020) != 0 ) {
      tid = pthread_self();
   }


   /* trim routine name as needed */
   /* *************************** */
   if ( (style & 0x0010) != 0 ) {
      strncpy(location, routine, sizeof(location));
      location[sizeof(location) - 1] = '\0';
   }


   /* get severity level as needed */
   /* **************************** */
   if ( (style & 0x000f) != 0 ) {
      if (( severity < FLOG_CL4BUG ) || ( severity > FLOG_NOLOG )) {
         severity = FLOG_NOLOG;
      }
   }


   /* get error code as needed */
   /* **************************** */
   if ( (style & 0x0003) != 0 ) {
      if (( ec < 0 ) || ( ec > FL_ERR_GENERIC )) {
         ecode = FL_ERR_GENERIC;
      }
      else {
         ecode = ec;
      }
   }


   /* compose prefixes */
   /* **************** */
   for ( i=0; i<3; i++ ) {
      len = 0;


      /* date information */
      /* **************** */
      if ( (flog_style[i] & 0x4000) == 0x4000 ) {
         if ( tp.tv_sec == 0 ) {
	     (void) strcpy(prefix[i],"- Bad time-");
         }
         else {
            (void) strftime(prefix[i], 12, "%Y-%b-%d", &tm_now);
         }
         len += 11;
      }


      /* time information */
      /* **************** */
      if ( (flog_style[i] & 0x2000) == 0x2000 ) {
         if ( len != 0 ) {
            prefix[i][len] = ' ';
            len++;
            prefix[i][len] = '\0';
         }
         if ( tp.tv_sec == 0 ) {
            (void) strcpy(&prefix[i][len], "--:--:--");
         }
         else {
            (void) strftime(&prefix[i][len], 9, "%H:%M:%S", &tm_now);
         }
         len += 8;
      }


      /* millisecond information */
      /* *********************** */
      if ( (flog_style[i] & 0x1000) == 0x1000 ) {
         if ( tp.tv_sec == 0 ) {
            (void) strcpy(&prefix[i][len], ".???");
            len += 4;
         }
         else {
            rc = snprintf(&prefix[i][len], 5, ".%.3ld",
			  (long int) ((tp.tv_usec + 500) / 1000));
            if ( rc > 0 ) len += rc;
         }
      }


      /* user, group, host, and domain information */
      /* ***************************************** */
      if ( (flog_style[i] & 0x0f00) != 0x0000 ) {
         if ( len != 0 ) {
            prefix[i][len] = ' ';
            len++;
            prefix[i][len] = '\0';
         }


         /* user information */
         /* **************** */
         if ( (flog_style[i] & 0x0800) == 0x0800 ) {
            rc = snprintf(&prefix[i][len], 16, "%s", user);
            if ( rc > 0 ) len += rc;
         }


         /* group information */
         /* ***************** */
         if ( (flog_style[i] & 0x0400) == 0x0400 ) {
            rc = snprintf(&prefix[i][len], 17, ":%s", group);
            if ( rc > 0 ) len += rc;
         }


         /* host and domain information */
         /* *************************** */
         if ( (flog_style[i] & 0x0300) != 0x0000 ) {
            if ( (flog_style[i] & 0x0c00) != 0x0000 ) {
               prefix[i][len] = '@';
               len++;
               prefix[i][len] = '\0';
            }

            if ( (flog_style[i] & 0x0100) == 0x0000 ) {
               rc = snprintf(&prefix[i][len], 32, "%s", hostname);
            }
            else {
               rc = snprintf(&prefix[i][len], 64, "%s", hostfull);
            }
            if ( rc > 0 ) len += rc;
         }
      }


      /* parent, process, thread, and routine information */
      /* ************************************************ */
      if ( (flog_style[i] & 0x00f0) != 0x0000 ) {
         if ( len != 0 ) {
            prefix[i][len] = ' ';
            len++;
         }
         prefix[i][len] = '(';
         len++;
         prefix[i][len] = '\0';


         /* parent process information */
         /* ************************** */
         if ( (flog_style[i] & 0x0080) == 0x0080 ) {
            rc = snprintf(&prefix[i][len], 13, "%d->", ppid);
            if ( rc > 0 ) len += rc;
         }


         /* process information */
         /* ******************* */
         if ( (flog_style[i] & 0x0040) == 0x0040 ) {
            rc = snprintf(&prefix[i][len], 11, "%d", pid);
            if ( rc > 0 ) len += rc;
         }


         /* thread information */
         /* ****************** */
         if ( (flog_style[i] & 0x0020) == 0x0020 ) {
            rc = snprintf(&prefix[i][len], 12, ".%d", (int) tid);
            if ( rc > 0 ) len += rc;
         }


         /* routine information */
         /* ******************* */
         if ( (flog_style[i] & 0x0010) == 0x0010 ) {
            if ( (flog_style[i] & 0x00e0) != 0x0000 ) {
               prefix[i][len] = ' ';
               len++;
               prefix[i][len] = '\0';
            }
            rc = snprintf(&prefix[i][len], 25, "%s", location);
            if ( rc > 0 ) len += rc;
         }


         prefix[i][len] = ')';
         len++;
         prefix[i][len] = '\0';
      }


      /* severity level and error code information */
      /* ***************************************** */
      if ( len != 0 ) {
         prefix[i][len] = ' ';
         len++;
      }

      if ( severity < 0 ) {
         for ( j=0; j<len; j++ ) {
            prefix[i][j] = ' ';
         }
         slevel = FLOG_CONTINUE;
      }
      else {
         slevel = severity;
      }

      prefix[i][len] = '[';
      len++;
      prefix[i][len] = '\0';

      if ( (flog_style[i] & 0x000f) != 0x0000 ) {

         /* severity level information */
         /* ************************** */
         if ( (flog_style[i] & 0x000c) == 0x0008 ) {
            rc = snprintf(&prefix[i][len], 2, "%c", flog_slevelc[slevel]);
            if ( rc > 0 ) len += rc;
         }
         else if ( (flog_style[i] & 0x0004) == 0x0004 ) {
            rc = snprintf(&prefix[i][len], 9, "%s", flog_sleveln[slevel]);
            if ( rc > 0 ) len += rc;
         }


         /* error code information */
         /* ********************** */
         if (( (flog_style[i] & 0x0001) == 0x0001 ) ||
             (( (flog_style[i] & 0x0002) == 0x0002 ) &&
              ( slevel <= FLOG_WARNING ))) {
            rc = snprintf(&prefix[i][len], 7, ":%d", ecode);
            if ( rc > 0 ) len += rc;
         }

      }
      else {
         prefix[i][len] = '!';
         len++;
      }

      prefix[i][len] = ']';
      len++;
      prefix[i][len] = ' ';
      len++;
      prefix[i][len] = '\0';


   }


   return(status);
}



int flog_print(const int16_t severity, char prefix[3][STG_PREFIX_LEN],
                  const char *message) {
   int       status = FL_ERR_SUCCESS;
   char      msg[512];


   /* print to stdout/stderr and flush */
   if ( abs(severity) <= flog_report[0] ) {
      if (( abs(severity) >= FLOG_NOTICE ) ||
          ( severity == FLOG_DEBUG )) {
         fflush(stderr);
         fprintf(stdout, " %s%s\n", prefix[0], message);
         fflush(stdout);
      }
      else {
         fflush(stdout);
         fprintf(stderr, " %s%s\n", prefix[0], message);
         fflush(stderr);
      }
   }


   /* print to the syslog */
   if (( abs(severity) <= flog_report[1] ) &&
       ( severity != FLOG_DEBUG )) {
      (void) snprintf(msg, 511, "%.*s%s", STG_PREFIX_LEN, prefix[1], message);
      msg[511] = '\0';
      switch( abs(severity) ) {
      case FLOG_ALERT:
         syslog(LOG_ALERT, msg);
         break;
      case FLOG_FATAL:
         syslog(LOG_CRIT, msg);
         break;
      case FLOG_ERROR:
         syslog(LOG_ERR, msg);
         break;
      case FLOG_WARNING:
         syslog(LOG_WARNING, msg);
         break;
      case FLOG_NOTICE:
         syslog(LOG_NOTICE, msg);
         break;
      case FLOG_PROGRESS:
         syslog(LOG_INFO, msg);
         break;
      case FLOG_INFO:
         syslog(LOG_INFO, msg);
         break;
      default:
         syslog(LOG_DEBUG, msg);
         break;
      }
   }


   /* print to a logfile */
   if ( abs(severity) <= flog_report[2] ) {
      fprintf(flog_file.fp, " %s%s\n", prefix[2], message);
      fflush(flog_file.fp);
   }

   return(status);
}



int flog_msg(const int16_t severity, const int ec, const char *routine,
                const char *message) {
   int          status = FL_ERR_SUCCESS;
   const char   me[] = "flog_msg";
   int16_t      level;
   char         prefix[3][STG_PREFIX_LEN];
   int          rc;
   char         msg[128];


   /* limit level to valid range */
   if (( severity > FLOG_NOLOG ) || ( severity < FLOG_CL4BUG )) {
      level = 0;
   } else {
      level = severity;
   }

   if (( abs(level) <= flog_report[0] ) ||
       ( abs(level) <= flog_report[1] ) ||
       ( abs(level) <= flog_report[2] )) {

      /* get message prefixes */
      status = flog_prefix(level, ec, routine, prefix);


      /* lock mutex */
      rc = pthread_mutex_lock(&flog_mutex);


      status = flog_print(level, prefix, message);

      if ( rc != 0 ) {
         (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
         (void) snprintf(msg, 127,
            "pthread_mutex_lock failed, rc = %d, errno = %d", rc, errno);
         msg[127] = '\0';
         (void) flog_print(FLOG_FATAL, prefix, msg);
         if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
      }


      /* unlock mutex */
      rc = pthread_mutex_unlock(&flog_mutex);
      if ( rc != 0 ) {
         (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
         (void) snprintf(msg, 127,
            "pthread_mutex_unlock failed, rc = %d, errno = %d", rc, errno);
         msg[127] = '\0';
         (void) flog_print(FLOG_FATAL, prefix, msg);
         if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
      }
   }


   return(status);
}



int flog_sys(const int16_t severity, const int ec, const char *routine,
                const char *command, int errnum) {
   int    status = FL_ERR_SUCCESS;
   char   msg[128];


   /* is errno provided or do we use the global errno value */
   /* ***************************************************** */
   if ( errnum == FLOG_GET_ERRNO ) {
      errnum = errno;
   }


   /* compose message string */
   /* ********************** */
   (void) snprintf(msg, 127, "%.32s failed, %d, %s", command, errnum,
      strerror(errnum));
   msg[127] = '\0';

   status = flog_msg(severity, ec, routine, msg);


   return(status);
}



int flog_usr(const int16_t severity, const int ec, const char *routine,
                const char *format, ...) {
   int       status = FL_ERR_SUCCESS;
   char      msg[512];
   va_list   argp;

   /* interprete format until space in msg is exhausted */  
   va_start(argp, format);
   vsnprintf(msg, 511, format, argp);
   va_end(argp);
   msg[511] = '\0';

   status = flog_msg(severity, ec, routine, msg);

   return(status);
}



int flog_report_setting() {
   int          status = FL_ERR_SUCCESS;
   const char   me[] = "flog_report_setting";
   char         s0[16];
   char         s1[16];
   char         s2[16];
   char         msg[512];



   /* logging to stdout/stderr */
   if ( flog_report[0] == FLOG_QUIET ) {
      (void) strncpy(s0, "OFF", sizeof(s0));
   }
   else if (( FLOG_DEBUG <= flog_report[0] ) ||
            ( flog_report[0] <= FLOG_NOLOG )) {
      (void) strncpy(s0, flog_sleveln[ flog_report[0] ], sizeof(s0));
   }
   else {
      (void) snprintf(s0, sizeof(s0), "%d", flog_report[0]);
   }
   s0[ sizeof(s0) - 1 ] = '\0';

   /* logging to syslog */
   if ( flog_report[1] == FLOG_QUIET ) {
      (void) strncpy(s1, "OFF", sizeof(s1));
   }
   else if (( FLOG_DEBUG <= flog_report[1] ) ||
            ( flog_report[1] <= FLOG_NOLOG )) {
      (void) strncpy(s1, flog_sleveln[ flog_report[1] ], sizeof(s1));
   }
   else {
      (void) snprintf(s1, sizeof(s1), "%d", flog_report[1]);
   }
   s1[ sizeof(s1) - 1 ] = '\0';

   /* logging to file */
   if ( flog_report[2] == FLOG_QUIET ) {
      (void) strncpy(s2, "OFF", sizeof(s2));
   }
   else if (( FLOG_DEBUG <= flog_report[2] ) ||
            ( flog_report[2] <= FLOG_NOLOG )) {
      (void) strncpy(s2, flog_sleveln[ flog_report[2] ], sizeof(s2));
   }
   else {
      (void) snprintf(s2, sizeof(s2), "%d", flog_report[2]);
   }
   s2[ sizeof(s2) - 1 ] = '\0';



   /* compose message string */
   /* ********************** */
   (void) snprintf(msg, sizeof(msg),
      "Message logging: %s= %s (0x%4.4x), %s= %s (0x%4.4x), %s= %s (0x%4.4x)",
      "stdout/err", s0, flog_style[0],
      "syslog", s1, flog_style[1],
      "file", s2, flog_style[2]);
   msg[511] = '\0';


   status = flog_msg(FLOG_DEBUG, 0, me, msg);

   return(status);
}



int flog_get_style(uint32_t *style_std, uint32_t *style_sys,
                      uint32_t *style_file) {
   int          status = FL_ERR_SUCCESS;
   const char   me[] = "flog_get_style";
   int          rc;
   char         prefix[3][STG_PREFIX_LEN];
   char         msg[128];


   /* lock mutex */
   rc = pthread_mutex_lock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_lock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }

   *style_std = flog_style[0];
   *style_sys = flog_style[1];
   *style_file = flog_style[2];

   /* unlock mutex */
   rc = pthread_mutex_unlock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_unlock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }


   return(status);
}



int flog_set_style(const uint32_t style_std, const uint32_t style_sys,
                      const uint32_t style_file) {
   int          status = FL_ERR_SUCCESS;
   const char   me[] = "flog_set_style";
   int          rc;
   char         prefix[3][STG_PREFIX_LEN];
   char         msg[128];


   /* lock mutex */
   rc = pthread_mutex_lock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_lock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }

   flog_style[0] = style_std;
   flog_style[1] = style_sys;
   flog_style[2] = style_file;

   /* unlock mutex */
   rc = pthread_mutex_unlock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_unlock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }


   return(status);
}



int flog_set_file(const char *filename) {
   int          status = FL_ERR_SUCCESS;
   const char   me[] = "flog_set_file";
   int          rc;
   char         prefix[3][STG_PREFIX_LEN];
   char         msg[128];

   /* lock mutex */
   rc = pthread_mutex_lock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_lock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }

   (void) strncpy(flog_file.name, filename, STG_FILE_PATH_MAX);
   flog_file.name[STG_FILE_PATH_MAX] = '\0';

   /* unlock mutex */
   rc = pthread_mutex_unlock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_unlock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }


   return(status);
}



int flog_get_report(int16_t *report_std, int16_t *report_sys,
                       int16_t *report_file) {
   int          status = FL_ERR_SUCCESS;
   const char   me[] = "flog_get_report";
   int          rc;
   char         prefix[3][STG_PREFIX_LEN];
   char         msg[128];


   /* lock mutex */
   rc = pthread_mutex_lock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
		     "pthread_mutex_lock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }

   *report_std = flog_report[0];
   *report_sys = flog_report[1];
   *report_file = flog_report[2];

   /* unlock mutex */
   rc = pthread_mutex_unlock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
		     "pthread_mutex_unlock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }


   return(status);
}



int flog_set_report(const int16_t report_std, const int16_t report_sys,
                       const int16_t report_file) {
   int             status = FL_ERR_SUCCESS;
   const char      me[] = "flog_set_report";
   int             rc;
   char            prefix[3][STG_PREFIX_LEN];
   char            msg[128];


   /* lock mutex */
   rc = pthread_mutex_lock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
		     "pthread_mutex_lock failed, rc = %d, errno = %d",
         rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }


   flog_report[0] = report_std;


   if (( report_sys > FLOG_QUIET ) &&
       ( flog_report[1] <= FLOG_QUIET )) {
      (void) openlog("stager", LOG_CONS | LOG_NOWAIT, LOG_DAEMON);
   }
   else if (( report_sys <= FLOG_QUIET ) &&
            ( flog_report[1] > FLOG_QUIET )) {
      (void) closelog();
   }
   flog_report[1] = report_sys;


   if (( report_file > FLOG_QUIET ) &&
       ( flog_report[2] <= FLOG_QUIET )) {
      if ( flog_file.name[0] == '\0' ) {
         rc = snprintf(flog_file.name, STG_FILE_PATH_MAX, "stg_%lu.log",
            (unsigned long int) time(NULL));
         flog_file.name[STG_FILE_PATH_MAX] = '\0';
      }

      flog_file.fp = fopen(flog_file.name, "a");
      if ( flog_file.fp == NULL ) {
         (void) flog_prefix(FLOG_ERROR, FL_ERR_LOG_FILE, me, prefix);
         (void) sprintf(msg, "fopen failed, rc = %d, errno = %d", rc, errno);
         (void) flog_print(FLOG_ERROR, prefix, msg);
         if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_FILE;
      }
      else {
         flog_report[2] = report_file;
      }
   }
   else if (( report_file <= FLOG_QUIET ) &&
            ( flog_report[2] > FLOG_QUIET )) {
      rc = fclose(flog_file.fp);
      if ( rc != 0 ) {
         (void) flog_prefix(FLOG_ERROR, FL_ERR_LOG_FILE, me, prefix);
         (void) sprintf(msg, "fclose failed, rc = %d, errno = %d", rc, errno);
         (void) flog_print(FLOG_ERROR, prefix, msg);
         if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_FILE;
      }
      flog_file.fp = NULL;
      flog_report[2] = FLOG_QUIET;
   }
   else {
      flog_report[2] = report_file;
   }


   /* unlock mutex */
   rc = pthread_mutex_unlock(&flog_mutex);
   if ( rc != 0 ) {
      (void) flog_prefix(FLOG_FATAL, FL_ERR_LOG_MUTEX, me, prefix);
      (void) sprintf(msg,
         "pthread_mutex_unlock failed, rc = %d, errno = %d", rc, errno);
      (void) flog_print(FLOG_FATAL, prefix, msg);
      if ( status == FL_ERR_SUCCESS ) status = FL_ERR_LOG_MUTEX;
   }


   return(status);
}


#ifdef DEBUG
/* ************************************************************************* */
int main(int argc, char **argv) {
   int    status = FL_ERR_SUCCESS;

   printf(" main(flog.c):\n");

   status = flog_usr(FLOG_ERROR, FL_ERR_GENERIC, "main",
      "Test of flog_usr");
   printf(" flog_usr(%d, %d, main, Test of, flog_usr) = %d\n",
      FLOG_ERROR, FL_ERR_GENERIC, status);

   status = flog_sys(FLOG_ERROR, 0, "main", "none", 0);
   printf(" flog_sys(%d, 1, main, Test of, flog_sys) = %d\n",
      FLOG_ERROR, status);

   status = flog_set_style(0xffff, 0xffff, 0xffff);
   printf(" flog_set_style = %d\n", status);

   status = flog_set_report(10, 0, 0);
   printf(" flog_set_report = %d\n", status);

   status = flog_usr(FLOG_L4BUG, 0, "main", "***error if printed***");
   printf(" flog_usr(%d, 0, main, Test) = %d\n", FLOG_L4BUG, status);

   status = flog_usr(FLOG_L3BUG, 0, "main", "Test 11");
   printf(" flog_usr(%d, 0, main, Test) = %d\n", FLOG_L3BUG, status);

   status = flog_set_style(0x6e79, 0x6e79, 0x6e79);
   printf(" flog_set_style = %d\n", status);

   status = flog_set_report(12, 0, 10);
   printf(" flog_set_report = %d\n", status);

   status = flog_usr(FLOG_L4BUG, 0, "main", "Test 21");
   printf(" flog_usr(%d, 0, main, Test) = %d\n", FLOG_L4BUG, status);

   status = flog_usr(FLOG_CL3BUG, 0, "main", "Test 22");
   printf(" flog_usr(%d, 0, main, Test) = %d\n", FLOG_L3BUG, status);

   exit(status);
}
#endif
