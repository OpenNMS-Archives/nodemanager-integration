
%module(directors=1) NNM
%include "typemaps.i"

%{

#include <sys/types.h>
#include <sys/time.h>

#include <sys/socket.h>
#include <sys/select.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include "OV/OVsPMD.h"
#include "OV/OVsnmpApi.h"

%}

%ignore timezone;
%ignore itimerval;
%ignore adjtime;
%ignore futimesat;
%ignore getitimer;
%ignore utimes;
%ignore setitimer;
%ignore settimeofday;
%ignore gettimeofday;
%ignore gethrtime;
%ignore gethrvtime;
%ignore fds_bits;

%include "sys/time.h"
%include "sys/select.h"

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

int select(int, fd_set *, fd_set *, fd_set *, struct timeval *);


%extend timeval {
  void  setTimeInMillis(long long millis) {
    self->tv_sec = millis/1000;
    self->tv_usec = (millis % 1000)*1000;
  }
  long long getTimeInMillis() {
    return (self->tv_sec * 1000)+(self->tv_usec/1000);
  }
}

%extend fd_set {
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
    }
    ~OVsnmpVal() {
      if ( self->integer != NULL ) {
        OVsnmpFree(self->integer);
      }
      OVsnmpFree(self);
    }
    bool isNullValue() {
      return (self->integer) == NULL;
    }
    int getIntValue() {
      if (self->integer == NULL) {
	return -1;
      }
      return *(self->integer);
    }
    void setIntValue(int v) {
      if (self->integer == NULL) {
	self->integer = (long*)OVsnmpMalloc(sizeof(long));
      }
      *(self->integer) = v;
    }
    long long getUnsigned32Value() {
      if (self->unsigned32 == NULL) {
	return -1;
      }
      return *(self->unsigned32);
    }
    void setUnsigned32Value(long long v) {
      if (self->unsigned32 == NULL) {
	self->unsigned32 = (u_long*)OVsnmpMalloc(sizeof(u_long));
      }
      *(self->unsigned32) = (u_long)v;
    }
    
    unsigned long long getCounter64Value() {
      if (self->unsigned64 == NULL) {
	return 0;
      }
      unsigned long long h = (self->unsigned64->high);
      unsigned long long l = (self->unsigned64->low);
      
      return (h << 32) | l;
    }
    void setCounter64Value(unsigned long long v) {
      if (self->unsigned64 == NULL) {
	self->unsigned64 = (OVuint64*)OVsnmpMalloc(sizeof(OVuint64));
      }
      self->unsigned64->low = (unsigned long)(v & 0xFFFFFFFFL);
      self->unsigned64->high = (unsigned long)((v >> 32) & 0xFFFFFFFFL);
    }

    void setOctetString(signed char octetsInOut[], int len) {
      self->string =  (unsigned char*)OVsnmpMalloc(len+1*sizeof(unsigned char));
      memcpy(self->string, octetsInOut, len);
      self->string[len] = '\0';
    }

    bool getOctetString(signed char octetsInOut[], int len) {

      if (self->string == NULL) {
	return false;
      }

      memcpy(octetsInOut, self->string, len);

      return true;
    }

    char* getObjectId(int len) {
      return objectIdToString(self->objid, len);
    }
    
    int setObjectId(char* oidStr) {
      
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
	return self->next_variable;
      }
      
      char* getObjectId() {
	return objectIdToString(self->oid, self->oid_length);     
      }

      int getType() {
      	return self->type;
      }

      OVsnmpVal* getValue() {
	return &(self->val);
      }

      int getValLength() {
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

typedef struct SNMPPdu {
  
  %extend {

    unsigned short getPort() {
      return self->address.sin_port;
    }
    
    char* getIpAddress() {
      return inet_ntoa(self->address.sin_addr);
    }
    
    int getCommand() {
      return self->command;
    }
    
    int getRequestId() {
      return self->request_id;
    }
    
    int getErrorStatus() {
      return self->error_status;
    }
    
    int getErrorIndex() {
      return self->error_index;
    }
    
    char* getEnterpriseObjectId() {
      return objectIdToString(self->enterprise, self->enterprise_length);
    }
    
    char* getAgentAddress() {
      struct in_addr addr;
      addr.s_addr = self->agent_addr;
      return inet_ntoa(addr);
    }
    
    int getGenericType() {
      return self->generic_type;
    }
    
    int getSpecificType() {
      return self->specific_type;
    }
    
    unsigned long getTime() {
      return self->time;
    }
    
    OVsnmpVarBind* getVarBinds() {
      return self->variables;
    }
    
    char* getCommunity() {
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
      return objectIdToString(self->notify_oid, self->notify_oid_length);
    }

    static OVsnmpPdu* create(int type) {
      return OVsnmpCreatePdu(type);
    }

    void free() {
      OVsnmpFreePdu(self);
    }

    OVsnmpVarBind* addNullVarBind(ObjectID oid[], int oid_length) {
      return OVsnmpAddNullVarBind(self, oid, oid_length);
    }
      
    OVsnmpVarBind* addTypedVarBind(ObjectID oid[], int oid_length, unsigned char type, OVsnmpVal* val, int val_len) {
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
    virtual void callback(int reason, OVsnmpSession* session, OVsnmpPdu* pdu) = 0;
  };

%}


%{

  static void callback_function(int reason, OVsnmpSession* session, OVsnmpPdu* pdu, void *callback_data) {
    if (callback_data == NULL) {
      return;
    }

    SnmpCallback* cb = (SnmpCallback*)callback_data;

    cb->callback(reason, session, pdu);
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

    int close() {
      return OVsnmpClose(self);
    }

    OVsnmpPdu* blockingSend(OVsnmpPdu* request) {
      return OVsnmpBlockingSend(self, request);
    }

    int cancelRequest(int reqId) {
      return OVsnmpCancel(self, reqId);
    }

    int send(OVsnmpPdu* pdu) {
      return OVsnmpSend(self, pdu);
    }

    OVsnmpPdu* receive() {
      return OVsnmpRecv(self);
    }

    static void read(fd_set* fdset) {
      OVsnmpRead(fdset);
    }

    static void doRetry() {
      OVsnmpDoRetry();
    }

    static int getRetryInfo(fd_set* fdset, timeval* timeout) {
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

      boolean loaded = false;
      String libname = System.getProperty("nnm.jni.library");

      if (libname != null) {
	try {
	  System.load(libname);
	  loaded = true;
	} catch(UnsatisfiedLinkError e) {
	  System.err.println("Unable to load library at "+libname+". Trying to load library 'NNM'. Reason: \n" + e);
	}
      } 

      if (!loaded) {
	try {
	  System.loadLibrary("NNM");
	} catch (UnsatisfiedLinkError e) {
	  System.err.println("Native code library failed to load. \n" + e);
	  System.exit(1);
	}
      }
      
  }
%}
