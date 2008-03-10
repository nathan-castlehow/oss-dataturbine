/* ************************************************************************* */
/* stg_stserr.h  Stager Package, status and error codes                      */
/*                                                                           */
/* Copyright (c) 1998 Universities Research Association, Inc., author, and   */
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

#if !defined(_FLOG_H_)
#define _FLOG_H_

/* Function prototypes */
int flog_levelcheck(const int16_t );
const char *flog_level_desc(const int16_t );
int flog_sys(const int16_t, const int, const char *, const char *, int);
int flog_usr(const int16_t, const int, const char *, const char *, ...);
int flog_report_setting(void);
int flog_get_style(uint32_t *, uint32_t *, uint32_t *);
int flog_set_style(const uint32_t, const uint32_t, const uint32_t);
int flog_set_file(const char *);
int flog_get_report(int16_t *, int16_t *, int16_t *);
int flog_set_report(const int16_t, const int16_t, const int16_t);

/* ************************************************************************* */
/* stager severity levels for logged messages:                               */
#define FLOG_DEBUG               0   /* temporary debug message           */
#define FLOG_ALERT               1   /* immediate action required message */
#define FLOG_FATAL               2   /* fatal error condition message     */
#define FLOG_ERROR               3   /* error message                     */
#define FLOG_WARNING             4   /* warning message                   */
#define FLOG_NOTICE              5   /* normal but significant message    */
#define FLOG_PROGRESS            6   /* progress report                   */
#define FLOG_INFO                7   /* informational message             */
#define FLOG_L1BUG               8   /* high-level debug message          */
#define FLOG_L2BUG               9   /* level 2 debug message             */
#define FLOG_L3BUG              10   /* level 3 debug message             */
#define FLOG_L4BUG              11   /* low-level debug message           */
#define FLOG_NOLOG              12   /* not to be printed message         */

#define FLOG_CALERT             -1   /* immediate action required message */
#define FLOG_CFATAL             -2   /* fatal error condition message     */
#define FLOG_CERROR             -3   /* error message                     */
#define FLOG_CWARNING           -4   /* warning message                   */
#define FLOG_CNOTICE            -5   /* normal but significant message    */
#define FLOG_CPROGRESS          -6   /* progress report                   */
#define FLOG_CINFO              -7   /* informational message             */
#define FLOG_CL1BUG             -8   /* high-level debug message          */
#define FLOG_CL2BUG             -9   /* level 2 debug message             */
#define FLOG_CL3BUG            -10   /* level 3 debug message             */
#define FLOG_CL4BUG            -11   /* low-level debug message           */

/* ************************************************************************* */
/* stager log utility parameters:                                            */
#define FLOG_QUIET              -1      /* disable message logging        */
#define FLOG_GET_ERRNO         -15      /* use global errno               */

/* ************************************************************************* */
/* stager error codes:                                                       */
#define FL_ERR_SUCCESS             0     /* success, no error               */
#define FL_ERR_NULL_PTR            1     /* Null pointer                    */
#define FL_ERR_SOCKET              2     /* socket error                    */
#define FL_ERR_NOCANDO             5     /* wrong arguments                 */
#define FL_ERR_TOO_BIG             6     /* parameter/value/arg too large   */
#define FL_ERR_TOO_SMALL           6     /* parameter/value/arg too small   */
#define FL_ERR_BAD_VALUE           7     /* bad value for variable detected */
#define FL_ERR_BAD_PARAM           8     /* bad or inconsistent parameter   */
#define FL_ERR_BAD_KEYWORD         9     /* bad or inconsistent keyword     */
#define FL_ERR_FILE_NAME          19     /* bad or incorrect filename       */
#define FL_ERR_FILE_EXIST         20     /* file exists already             */
#define FL_ERR_FILE_NOEXIST       21     /* file does not exist             */
#define FL_ERR_FILE_EMPTY         22     /* file is empty                   */
#define FL_ERR_FILE_OPEN          23     /* file open error                 */
#define FL_ERR_FILE_CLOSE         24     /* file close error                */
#define FL_ERR_FILE_END           25     /* end of file reached             */
#define FL_ERR_FILE_READ          26     /* file read error                 */
#define FL_ERR_FILE_WRITE         27     /* file write error                */
#define FL_ERR_FILE_COPY          28     /* file copy error                 */
#define FL_ERR_FILE_DELETE        29     /* file delete error               */
#define FL_ERR_DIR_OPEN           30     /* directory open error            */
#define FL_ERR_DIR_CLOSE          31     /* directory close error           */
#define FL_ERR_DIR_CREATE         32     /* directory close error           */
#define FL_ERR_DIR_DELETE         33     /* directory close error           */
#define FL_ERR_NO_MEMORY          90     /* out of memory                   */
#define FL_ERR_LOG_MUTEX          96     /* error (un)locking msg log mutex */
#define FL_ERR_LOG_FILE           97     /* error involving log file        */
#define FL_ERR_SYSTEM             98     /* generic system error            */
#define FL_ERR_UNKNOWN            99     /* unknown error                   */
#define FL_ERR_TRANSCEIVE        101     /* client/server transceive error  */
#define FL_ERR_BUF_NOSPACE       102     /* buffer space exhausted          */
#define FL_ERR_MUTEX_FAILURE     200     /* Failure to lock / unlock mutex  */
#define FL_ERR_HOST_NOTFOUND     301     /* DNS lookup failed               */
#define FL_ERR_GENERIC         32767     /* generic and max error code      */
/* ************************************************************************* */

#endif

