TOP        = /usr/local/doc/swig/Examples
OV         = /opt/OV 
OVINC      = /opt/OV/include
OVLIB      = /opt/OV/lib
SWIG       = swig
SWIGOPT    = -I$(OVINC) -I/usr/include
SRCS       =
TARGET     = NNM
INTERFACE  = nnm.i

LIBDIRS    := $(OVLIB)
ifeq ($(OS), HP-UX)
	LIBDIRS += $(OVLIB)/hpux32
else
	LIBDIRS += /usr/local/lib
endif

LIBFLAGS  := $(foreach dir, $(LIBDIRS), -L$(dir))

ifeq ($(OS), SunOS)
	LIBFLAGS := $(LIBFLAGS) $(foreach dir, $(LIBDIRS), -R$(dir))
endif

LIBS       = $(LIBFLAGS) -lovsnmp
INCLUDES   = -I$(OVINC)

JSRCDIR    = target/generated-sources/swig

PACKAGE    = org.opennms.nnm.swig
PKGDIR     = org/opennms/nnm/swig

MVNVER     = 0.9-SNAPSHOT

# For a release version use the below url
# MVNDEPLOY = scpexe://repo.opennms.org/var/www/sites/opennms.org/site/repo/maven2
MVNDEPLOY  = scpexe://repo.opennms.org/var/www/sites/opennms.org/site/repo/snapshots



LDSHARED=       ld -G
CCSHARED=
CXXSHARED=      ld -G

# You need to set this variable to the java directories containing the
# files "jni.h" and "md.h"
# usually something like /usr/java/include and /usr/java/include/<arch-osname>.
JAVA_INCLUDE := -I$(JDK_HOME)/include

ifeq ($(OS), SunOS) 
	JAVA_INCLUDE += -I$(JDK_HOME)/include/solaris
endif

ifeq ($(OS), HP-UX)
	JAVA_INCLUDE += -I$(JDK_HOME)/include/hp-ux
endif

# Extra Java specific dynamic linking options
JAVA_DLNK  = -static-libgcc
JAVA_LIBPREFIX = lib
JAVASO =.so
JAVALDSHARED = $(LDSHARED)
#JAVACXXSHARED = $(CXXSHARED)
#JAVACXXSHARED = $(CXXSHARED) -shared

ifeq ($(OS), HP-UX)
	JAVACXXSHARED = g++ -shared
else
	JAVACXXSHARED = g++ -Wl,-G
endif
JAVACFLAGS =

IWRAP      = $(INTERFACE:.i=_wrap.i)
ISRCS      = $(IWRAP:.i=.c)
ICXXSRCS   = $(IWRAP:.i=.cxx)
IOBJS      = $(IWRAP:.i=.o)

OBJS      = $(SRCS:.c=.o) $(CXXSRCS:.cxx=.o)

# This is used for building shared libraries with a number of C++
# compilers.   If it doesn't work,  comment it out.
CXXSHARED= g++ -Wl,-G




all:: java_cpp

java_cpp: $(SRCS)
	mkdir -p $(JSRCDIR)/$(PKGDIR)
	$(SWIG) -java -c++ -outdir $(JSRCDIR)/$(PKGDIR) -package $(PACKAGE) $(SWIGOPT) $(INTERFACE) 
	$(CXX) -c $(CCSHARED) $(CFLAGS) $(JAVACFLAGS) $(SRCS) $(CXXSRCS) $(ICXXSRCS) $(INCLUDES) $(JAVA_INCLUDE)
	$(JAVACXXSHARED) $(OBJS) $(IOBJS) $(JAVA_DLNK) $(LIBS) $(CPP_DLLIBS) -o $(JAVA_LIBPREFIX)$(TARGET)$(JAVASO)

mvn-package:: java_cpp
	mvn package


mvn-install:: java_cpp
	mvn install

mvn-deploy:: java_cpp
	mvn deploy

clean_cpp::
	rm -f *_wrap* *~ .~* *.class 
	rm -f core
	rm -f *.o *.so
	rm -f *.gif

clean:: clean_cpp
	mvn clean

check: all
