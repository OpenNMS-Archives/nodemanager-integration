
%module(directors=1) NNM
%include "typemaps.i"

%typemap(javadirectorin) SWIGTYPE *, SWIGTYPE (CLASS::*), SWIGTYPE &, const SWIGTYPE & "(($jniinput == 0) ? null : new $javaclassname($jniinput, false))"

%{

#include <sys/types.h>
#include <sys/time.h>

#include <sys/socket.h>
#ifndef __hppa__
#include <sys/select.h>
#endif

#include <netinet/in.h>
#include <arpa/inet.h>

#include "OV/OVsPMD.h"
#include "OV/OVsnmpApi.h"

%}

%inline %{

#define LOG_ERR 1
#define LOG_WRN 2
#define LOG_INF 3
#define LOG_DBG 4
#define LOG_TRC 5

int logLevel = LOG_TRC;

%}

%{

#define LOG(lvl, lvlName, fmt, ...) if (logLevel >= lvl) { fprintf(stderr, lvlName"(%s, %d): "fmt"\n", __FILE__, __LINE__, __VA_ARGS__); }

#define TRACE(fmt, ...) LOG(LOG_TRC, "TRACE", fmt, __VA_ARGS__)
#define DEBUG(fmt, ...) LOG(LOG_DBG, "DEBUG", fmt, __VA_ARGS__)
#define INFO(fmt, ...) LOG(LOG_INF, "INFO", fmt, __VA_ARGS__)
#define WARN(fmt, ...) LOG(LOG_WRN, "WARN", fmt, __VA_ARGS__)
#define ERROR(fmt, ...) LOG(LOG_ERR, "ERROR", fmt, __VA_ARGS__)

#define CHECK(expr, msg) if (!(expr))  { WARN("Assertion Failed: %s : %s", msg, #expr); }
#define NOTNULL(val, msg) if ((val) == NULL)  { WARN("Assertion Failed: %s, %s is NULL", msg, #val); }

%}

%constant LOG_ERR = 1;
%constant LOG_WRN = 2;
%constant LOG_INF = 3;
%constant LOG_DBG = 4;
%constant LOG_TRC = 5;


typedef int OVsCodeType;

%constant OVS_NO_CODE                 = 0;
%constant OVS_RSP_SUCCESS             = 1;
%constant OVS_RSP_FAILURE             = 2;
%constant OVS_RSP_DONE                = 3;
%constant OVS_RSP_STATUS              = 4;
%constant OVS_CMD_NOOP                = 5;
%constant OVS_CMD_EXIT                = 6;
%constant OVS_CMD_DEPENDENCY_FAILED   = 7;

%constant OVS_REQ_START               = 8;
%constant OVS_REQ_STOP                = 9;
%constant OVS_REQ_STATUS              = 10;
%constant OVS_REQ_ABORT               = 11;
%constant OVS_RSP_ERROR               = 12;

/* Used by ovpause/ovresume to request ovspmd to pause/resume daemons */
%constant OVS_REQ_PAUSE               = 13;
%constant OVS_REQ_RESUME              = 14;

/* Used for Pause/Resume communication between ovspmd and the daemons */
%constant OVS_CMD_PAUSE               = 15;
%constant OVS_CMD_RESUME              = 16;
%constant OVS_RSP_PAUSE_ACK           = 17;
%constant OVS_RSP_RESUME_ACK          = 18;
%constant OVS_RSP_PAUSE_NACK          = 19;
%constant OVS_RSP_RESUME_NACK         = 20;

/* Used by daemons to update their short and verbose messages   */
/* without altering state                                       */
%constant OVS_RSP_VERBOSE_MSG         = 21;
%constant OVS_RSP_LAST_MSG            = 22;

/* used to return system state info via request socket */
%constant OVS_STATE_RUNNING           = 23;
%constant OVS_STATE_PAUSED            = 24;
%constant OVS_STATE_ALL_STOPPED       = 25;
%constant OVS_STATE_START_COMPLETE    = 26;

/*Internal use, for communication between ovuispmd and ovspmd*/
%constant OVS_CMD_STATUS              = 27;
%constant OVS_RSP_STATUS_NCHNG        = 28;
%constant OVS_RSP_STATUS_TIMEOUTVAL   = 29;


typedef struct {
    OVsCodeType code;
    char *message;
} OVsPMDCommand;


int OVsInit(int* OUTPUT);
int OVsInitComplete(OVsCodeType, const char *);
int OVsResponse(OVsCodeType, const char *);
int OVsReceive(OVsPMDCommand *);
int OVsDone(const char *);
int OVsSpmdSocketFd();

typedef struct timeval {
  %extend {
    void  setTimeInMillis(long long millis) {
      self->tv_sec = millis/1000;
      self->tv_usec = (millis % 1000)*1000;
    }
    long long getTimeInMillis() {
      return (self->tv_sec * 1000)+(self->tv_usec/1000);
    }
  }
} timeval;

typedef struct fd_set {
  %extend {
    void set(int fd) {
      FD_SET(fd, self);
    }
    void clr(int fd) {
      FD_CLR(fd, self);
    }
    void zero() {
    FD_ZERO(self);
    }
    bool isSet(int fd) {
      return FD_ISSET(fd, self);
    }
  }
} fd_set;

int select(int, fd_set *, fd_set *, fd_set *, struct timeval *);

%include "OV/OVsnmpErr.h"
%include "OV/OVsnmpAsn1.h"

#define GET_REQ_MSG         (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x0)
#define GETNEXT_REQ_MSG     (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x1)
#define GET_RSP_MSG         (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x2)
#define SET_REQ_MSG         (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x3)
#define TRAP_REQ_MSG        (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x4)
#define GETBULK_REQ_MSG     (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x5)
#define INFORM_REQ_MSG      (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x6)
#define V2TRAP_REQ_MSG      (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x7)
#define REPORT_MSG          (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x8)
#define RESPONSE_MSG        GET_RSP_MSG
#define V1TRAP_REQ_MSG      TRAP_REQ_MSG

/*
 *      SNMP Trap Types (RFC 1157)
 */

#define SNMP_TRAP_COLDSTART             (0x0)
#define SNMP_TRAP_WARMSTART             (0x1)
#define SNMP_TRAP_LINKDOWN              (0x2)
#define SNMP_TRAP_LINKUP                (0x3)
#define SNMP_TRAP_AUTHFAIL              (0x4)
#define SNMP_TRAP_EGPNEIGHBORLOSS       (0x5)
#define SNMP_TRAP_ENTERPRISESPECIFIC    (0x6)

%constant const char* SNMP_USE_DEFAULT_COMMUNITY = NULL;
%constant const char* SNMP_USE_DEFAULT_PROXY  	 = NULL;
#define SNMP_USE_DEFAULT_RETRIES	-1
#define SNMP_USE_DEFAULT_TIMEOUT        -1
#define SNMP_USE_DEFAULT_INTERVAL       SNMP_USE_DEFAULT_TIMEOUT
#define SNMP_USE_DEFAULT_REMOTE_PORT	0xffff
#define SNMP_USE_DEFAULT_LOCAL_PORT	0xffff
#define SNMP_USE_DEFAULT_POLL_INTERVAL  -1

#define SNMP_USE_DEFAULT_TIME		0xffffffff  /* Use application uptime */
#define SNMP_USE_DEFAULT_ADDRESS	0xffffffff  /* Use default src IP addr*/

%{

  static char buf[8192];

  static char* objectIdToString(ObjectID* oid, int oid_len) {

    if (oid == NULL) {
      return NULL;
    }

    NOTNULL(oid, "objectIdToString()");
    CHECK((oid_len > 0), "objectIdToString()");
    return OVsnmpOidToStr(oid, oid_len, buf, 8192);
  }
  
%}

%typemap(jni) (signed char octetsInOut[], int len) "jbyteArray"

%typemap(jtype) (signed char octetsInOut[], int len) "byte[]"

%typemap(jstype) (signed char octetsInOut[], int len) "byte[]"

%typemap(javain) (signed char octetsInOut[], int len) "$javainput"

%typemap(in) (signed char octetsInOut[], int len) {
  int i = 0;
  $1 = jenv->GetByteArrayElements($input, 0);
  $2 = jenv->GetArrayLength($input);
}

%typemap(freearg) (signed char octetsInOut[], int len) {
  jenv->ReleaseByteArrayElements($input, $1, 0);
}


typedef union SNMPVal {
  %extend {
    OVsnmpVal() { 
    	OVsnmpVal* self = (OVsnmpVal*)OVsnmpMalloc(sizeof(OVsnmpVal));
	self->integer = NULL;		   	
	return self;
    }
    ~OVsnmpVal() {
      NOTNULL(self, "~OVsnmpVal");
      if ( self->integer != NULL ) {
        OVsnmpFree(self->integer);
      }
      OVsnmpFree(self);
    }
    bool isNullValue() {
      NOTNULL(self, "OVsnmpVal::isNullValue()");
      return (self->integer) == NULL;
    }
    int getIntValue() {
      NOTNULL(self, "OVsnmpVal::getIntValue()");
      if (self->integer == NULL) {
	return -1;
      }
      return *(self->integer);
    }
    void setIntValue(int v) {
      NOTNULL(self, "OVsnmpVal::setIntValue()");
      if (self->integer == NULL) {
	self->integer = (long*)OVsnmpMalloc(sizeof(long));
      }
      *(self->integer) = v;
    }
    long long getUnsigned32Value() {
      NOTNULL(self, "OVsnmpVal::getUnsigned32Value()");
      if (self->unsigned32 == NULL) {
	return -1;
      }
      return *(self->unsigned32);
    }
    void setUnsigned32Value(long long v) {
      NOTNULL(self, "OVsnmpVal::setUnsigned32Value()");
      if (self->unsigned32 == NULL) {
	self->unsigned32 = (u_long*)OVsnmpMalloc(sizeof(u_long));
      }
      *(self->unsigned32) = (u_long)v;
    }
    
    unsigned long long getCounter64Value() {
      NOTNULL(self, "OVsnmpVal::getCounter64Value()");
      if (self->unsigned64 == NULL) {
	return 0;
      }
      unsigned long long h = (self->unsigned64->high);
      unsigned long long l = (self->unsigned64->low);
      
      return (h << 32) | l;
    }
    void setCounter64Value(unsigned long long v) {
      NOTNULL(self, "OVsnmpVal::setCounter64Value()");
      if (self->unsigned64 == NULL) {
	self->unsigned64 = (OVuint64*)OVsnmpMalloc(sizeof(OVuint64));
      }
      self->unsigned64->low = (unsigned long)(v & 0xFFFFFFFFL);
      self->unsigned64->high = (unsigned long)((v >> 32) & 0xFFFFFFFFL);
    }

    void setOctetString(signed char octetsInOut[], int len) {
      NOTNULL(self, "OVsnmpVal::setOctetString()");
      self->string =  (unsigned char*)OVsnmpMalloc(len+1*sizeof(unsigned char));
      memcpy(self->string, octetsInOut, len);
      self->string[len] = '\0';
    }

    bool getOctetString(signed char octetsInOut[], int len) {

      NOTNULL(self, "OVsnmpVal::getOctetString()");
      NOTNULL(octetsInOut, "OVsnmpVal::getOctetString()");

      if (self->string == NULL) {
	return false;
      }

      memcpy(octetsInOut, self->string, len);

      return true;
    }

    char* getObjectId(int len) {
      NOTNULL(self, "OVsnmpVal::getObjectId()");
      return objectIdToString(self->objid, len);
    }
    
    int setObjectId(char* oidStr) {
      
      NOTNULL(self, "OVsnmpVal::setObjectId()");

      if (oidStr == NULL) {
	self->objid = NULL;
	return 0;
      }
      
      ObjectID* oid;
      unsigned int oid_len;
      
      OVsnmpOidFromStr(&oid, &oid_len, oidStr);
      
      self->objid = oid;
      
      return (int)oid_len;
      
    }
  }
  
} OVsnmpVal ;


typedef struct SNMPVarBind {

    %extend {

      OVsnmpVarBind* getNextVarBind() {
        NOTNULL(self, "OVsnmpVarBind::getNextVarBind()");
	return self->next_variable;
      }
      
      char* getObjectId() {
        NOTNULL(self, "OVsnmpVarBind::getObjectId()");
	return objectIdToString(self->oid, self->oid_length);     
      }

      int getType() {
        NOTNULL(self, "OVsnmpVarBind::getType()");
      	return self->type;
      }

      OVsnmpVal* getValue() {
        NOTNULL(self, "OVsnmpVarBind::getValue()");
	return &(self->val);
      }

      int getValLength() {
        NOTNULL(self, "OVsnmpVarBind::getValLength()");
	return self->val_len;
      }

    }
} OVsnmpVarBind;

typedef int ObjectID;

%typemap(jni) (ObjectID oid[], int oid_length) "jintArray"

%typemap(jtype) (ObjectID oid[], int oid_length) "int[]"

%typemap(jstype) (ObjectID oid[], int oid_length) "int[]"

%typemap(javain) (ObjectID oid[], int oid_length) "$javainput"

%typemap(in) (ObjectID oid[], int oid_length) {
  int i = 0;
  $1 = (ObjectID*)jenv->GetIntArrayElements($input, 0);
  $2 = jenv->GetArrayLength($input);
}

%typemap(freearg) (ObjectID oid[], int oid_length) {
  jenv->ReleaseIntArrayElements($input, (jint*)$1, 0);
}

%typemap(javafinalize) SWIGTYPE ""

typedef struct SNMPPdu {

  %extend {

    OVsnmpPdu(int type) {
      return OVsnmpCreatePdu(type);
    }

    ~OVsnmpPdu() {
      NOTNULL(self, "~OVsnmpPdu()");
      OVsnmpFreePdu(self);
    }

    unsigned short getPort() {
      NOTNULL(self, "OVsnmpPdu::getPort()");
      return self->address.sin_port;
    }
    
    char* getIpAddress() {
      NOTNULL(self, "OVsnmpPdu::getIpAddress()");
      return inet_ntoa(self->address.sin_addr);
    }
    
    int getCommand() {
      NOTNULL(self, "OVsnmpPdu::getCommand()");
      return self->command;
    }
    
    int getRequestId() {
      NOTNULL(self, "OVsnmpPdu::getRequestId()");
      return self->request_id;
    }
    
    int getErrorStatus() {
      NOTNULL(self, "OVsnmpPdu::getErrorStatus()");
      return self->error_status;
    }
    
    int getErrorIndex() {
      NOTNULL(self, "OVsnmpPdu::getErrorIndex()");
      return self->error_index;
    }
    
    char* getEnterpriseObjectId() {
      NOTNULL(self, "OVsnmpPdu::getEnterpriseId()");
      TRACE("ENTER:getEnterpriseObjectId(%p)", self);

      char* ret = objectIdToString(self->enterprise, self->enterprise_length);

      TRACE("EXIT:getEnterpriseObjectId(%p) returns %s", self, (ret ? ret : "NULL"));
      return ret;
    }
    
    char* getAgentAddress() {
      NOTNULL(self, "OVsnmpPdu::getAgentAddress()");
      struct in_addr addr;
      addr.s_addr = self->agent_addr;
      return inet_ntoa(addr);
    }
    
    int getGenericType() {
      NOTNULL(self, "OVsnmpPdu::getGenericType()");
      return self->generic_type;
    }
    
    int getSpecificType() {
      NOTNULL(self, "OVsnmpPdu::getSpecificType()");
      return self->specific_type;
    }
    
    unsigned long getTime() {
      NOTNULL(self, "OVsnmpPdu::getType()");
      return self->time;
    }
    
    OVsnmpVarBind* getVarBinds() {
      NOTNULL(self, "OVsnmpPdu::getVarBinds()");
      return self->variables;
    }
    
    char* getCommunity() {
      NOTNULL(self, "OVsnmpPdu::getCommunity()");
      static char buf[8192];
      
      if (self->community == NULL) {
	return NULL;
      }      
      
      int len = (self->community_len > 8191 ? 8191 : self->community_len);
      char* result = strncpy(buf,(const char *)self->community, len);
      result[8191] = '\0';
      return result;
    }
    
    char* getNotifyObjectId() {
      NOTNULL(self, "OVsnmpPdu::getNotifyObjecTId()");
      TRACE("ENTER:getNotifyObjectId(%p)", self);

      char* ret = objectIdToString(self->notify_oid, self->notify_oid_length);

      TRACE("EXIT:getNotifyObjectId(%p) returns %s", self, (ret ? ret : "NULL"));
      return ret;
    }

    OVsnmpVarBind* addNullVarBind(ObjectID oid[], int oid_length) {
      NOTNULL(self, "OVsnmpPdu::addNullVarBind()");
      NOTNULL(oid, "OVsnmpPdu::addNullVarBind()");
      CHECK((oid_length > 0), "OVsnmpPdu::addNullVarBind()");
      return OVsnmpAddNullVarBind(self, oid, oid_length);
    }
      
    OVsnmpVarBind* addTypedVarBind(ObjectID oid[], int oid_length, unsigned char type, OVsnmpVal* val, int val_len) {
      NOTNULL(self, "OVsnmpPdu::addTypedVarBind()");
      NOTNULL(oid, "OVsnmpPdu::addTypedVarBind()");
      CHECK((oid_length > 0), "OVsnmpPdu::addTypedVarBind()");
      NOTNULL(val, "OVsnmpPdu::addTypedVarBind()");
      CHECK((val_len > 0), "OVsnmpPdu::addTypedVarBind()");
      return OVsnmpAddTypedVarBind(self, oid, oid_length, type, val, val_len);
    }
    
  }
  
} OVsnmpPdu;


#define RECV_TRAPS		0x0001
#define IS_PROXIED_FOR		0x0002
#define FREE_PDU		0x0004
#define OVSNMP_V2API            0x0008  
#define OVSNMP_CLOSE_CB		0x0010	

#define OVSNMP_RECV_EVENTS	RECV_TRAPS
#define OVSNMP_FREE_PDU		FREE_PDU

%feature("director") SnmpCallback;

%inline %{

  class SnmpCallback {
  public:
    virtual ~SnmpCallback() {}
    virtual bool callback(int reason, OVsnmpSession* session, OVsnmpPdu* pdu) {
      return true;
    }
  };

%}

%{

  static void callback_function(int reason, OVsnmpSession* session, OVsnmpPdu* pdu, void *callback_data) {
   
    TRACE("ENTER:callback_function(%d, %p, %p, %p)", reason, session, pdu, callback_data);

    bool shouldFree = true;
    if (callback_data != NULL) {

      SnmpCallback* cb = (SnmpCallback*)callback_data;
      
      shouldFree = cb->callback(reason, session, pdu);
    
    }

    if (pdu && shouldFree) {
      TRACE("EXIT: freeing pdu at %p", pdu);
      OVsnmpFreePdu(pdu);
    }

    TRACE("EXIT:callback_function(%d, %p, %p, %p)", reason, session, pdu, callback_data);
  }

%}

%inline %{
  OVsnmpSession* OVsnmpEventOpen(const char* remoteOVMgrHostname, const char* applName, SnmpCallback* cb, const char* filter) {

    OVsnmpCallback callback = callback_function;
    if (cb == NULL) {
      callback = NULL;
    }
    
    return OVsnmpEventOpen(remoteOVMgrHostname, applName, callback, cb, filter);
  }

  OVsnmpSession* OVsnmpOpen(const char* community, const char* peername, 
				 int retries, int interval, 
				 unsigned short local_port, unsigned short remote_port, 
				 SnmpCallback* cb)
  {
    OVsnmpCallback callback = callback_function;
    if (cb == NULL) {
      callback = NULL;
    }

    return OVsnmpOpen(community, peername, retries, interval, local_port, remote_port, callback, cb);
  }

%}

typedef struct SNMPSession {

  %extend {
    static OVsnmpSession* open(const char* community, const char* peername, 
			       int retries, int interval, 
			       unsigned short local_port, unsigned short remote_port,
			       SnmpCallback* cb)
    {
      return OVsnmpOpen(community, peername, retries, interval, local_port, remote_port, cb);
    }

    static OVsnmpSession* open(const char* peername, unsigned short remote_port, SnmpCallback* cb)
    {
      return OVsnmpOpen(SNMP_USE_DEFAULT_COMMUNITY, peername, SNMP_USE_DEFAULT_RETRIES, SNMP_USE_DEFAULT_INTERVAL, SNMP_USE_DEFAULT_LOCAL_PORT, remote_port, cb);
    }

    static OVsnmpSession* eventOpen(const char* remoteOVMgrHostname, const char* applName, SnmpCallback* cb, const char* filter) {
      return OVsnmpEventOpen(remoteOVMgrHostname, applName, cb, filter);
    }

    static OVsnmpSession* eventOpen(const char* applName, SnmpCallback* cb, const char* filter) {
      return OVsnmpEventOpen(NULL, applName, cb, filter);
    }

    int getFlags() {
      NOTNULL(self, "OVsnmpSession::getFlags()");
      return self->session_flags;
    }

    int isFlagSet(int mask) {
      NOTNULL(self, "OVsnmpSession::isFlagSet()");
      return OVSNMP_TEST_FLAG(mask, self->session_flags);
    }

    void clearFlag(int mask) {
      NOTNULL(self, "OVsnmpSession::clearFlag()");
      OVSNMP_CLEAR_FLAG(mask, self->session_flags);
    }

    void setFlag(int mask) {
      NOTNULL(self, "OVsnmpSession::setFlag()");
      OVSNMP_SET_FLAG(mask, self->session_flags);
    }

    int close() {
      NOTNULL(self, "OVsnmpSession::close()");
      return OVsnmpClose(self);
    }

    OVsnmpPdu* blockingSend(OVsnmpPdu* request) {
      NOTNULL(self, "OVsnmpSession::blockingSend()");
      NOTNULL(request, "OVsnmpSession::blockingSend()");
      return OVsnmpBlockingSend(self, request);
    }

    int cancelRequest(int reqId) {
      NOTNULL(self, "OVsnmpSession::cancelRequest()");
      return OVsnmpCancel(self, reqId);
    }

    int send(OVsnmpPdu* pdu) {
      NOTNULL(self, "OVsnmpSession::send()");
      NOTNULL(pdu, "OVsnmpSession::send()");
      return OVsnmpSend(self, pdu);
    }

    OVsnmpPdu* receive() {
      NOTNULL(self, "OVsnmpSession::receive()");
      return OVsnmpRecv(self);
    }

    static void read(fd_set* fdset) {
      NOTNULL(fdset, "OVsnmpSession::read()");
      OVsnmpRead(fdset);
    }

    static void doRetry() {
      OVsnmpDoRetry();
    }

    static int getRetryInfo(fd_set* fdset, timeval* timeout) {
      NOTNULL(fdset, "OVsnmpSession::getRetryInfo()");
      NOTNULL(timeout, "OVsnmpSession::getRetryInfo()");
      return OVsnmpGetRetryInfo(fdset, timeout);
    }

  }

} OVsnmpSession;


OVsnmpVarBind *OVsnmpAddNullVarBind(
   OVsnmpPdu       *pdu,
   ObjectID        oid[],
   int             oid_length
);


OVsnmpVarBind *OVsnmpAddTypedVarBind(
   OVsnmpPdu       *pdu,
   ObjectID        oid[],
   int             oid_length,
   unsigned char   type,
   OVsnmpVal       *val,
   int             val_len
);



OVsnmpPdu *OVsnmpBlockingSend(
   OVsnmpSession	*session,
   OVsnmpPdu	*pdu
);


int OVsnmpCancel(
   OVsnmpSession	*session,
   int            reqId
);


int OVsnmpClose(
   OVsnmpSession	*session
);


OVsnmpPdu *OVsnmpCopyPdu(
   const OVsnmpPdu *pdu
) ;


%newobject OVsnmpCreatePdu;
OVsnmpPdu *OVsnmpCreatePdu(
   int		type
) ;


void OVsnmpDoRetry();


OVsnmpPdu *OVsnmpFixPdu(
   OVsnmpPdu	*pdu,
   int		type
);


void  OVsnmpFreePdu(
   OVsnmpPdu	*pdu
);

int OVsnmpGetRetryInfo(
   fd_set	*fdset,
   timeval	*timeout
);

void OVsnmpRead(
   fd_set	*fdset
);

OVsnmpPdu *OVsnmpRecv(
   OVsnmpSession	*session
);


int OVsnmpSend(
   OVsnmpSession	*session,
   OVsnmpPdu	*pdu
);




%pragma(java) jniclasscode=%{
  static {
      org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NNMJNI.class);
      log.info("loading NNM Library");
      String libname = System.getProperty("nnm.jni.library");

      if (libname != null) {
	try {
	  System.load(libname);
	  log.info("Successfully loaded NNM library from "+libname);
	} catch(Throwable e) {
	  log.fatal("Unable to load library at "+libname+". Trying to load library 'NNM'. Reason: \n" + e, e);
	  System.exit(14);
	}
      } else {
	try {
	  System.loadLibrary("NNM");
	  log.info("Successfully loaded NNM library from "+System.mapLibraryName("NNM"));
	} catch (Throwable e) {
	  log.fatal("Native code library failed to load. \n" + e, e);
	  System.exit(13);
	}
      }

      
  }
%}
