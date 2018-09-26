#///////////////////////////////////////////////////////////////////////////////
#//
#//  Makefile for the DALServer Tasks
#//
#///////////////////////////////////////////////////////////////////////////////

# Primary dependencies

NAME       =  dalserver
VERSION    = 1.0
PLATFORM  := $(shell uname -s)
#PLATFORM  = LINUX
#PLATFORM  = MACOSX
#PLATFORM  = CYGWIN
HERE      := $(shell /bin/pwd)


# Secondary dependencies

LIBBASE    = lib$(NAME)
STATICLIB  = $(HERE)/$(LIBBASE).a
SHAREDLIB  = $(HERE)/$(LIBBASE).so


# Stuff that's precious to keep

.PRECIOUS:	$(STATICLIB) $(SHAREDLIB)
.KEEP_STATE:


# Includes, flags and libraries
CC        = gcc
CINCS     = -I$(HERE)
#CFLAGS   = -O2 -DDEBUG -D$(PLATFORM)
#CFLAGS    = -arch i386 -arch ppc -g -Wall -D$(PLATFORM)
CFLAGS    = -g -Wall -D$(PLATFORM)


# List of source and include files
SRCS		= phead.c vocutout.c vometa.c votaskd.c
OBJS		= phead.o vocutout.o vometa.o votaskd.o
INCS 		= 
LIBS		= lib$(NAME).a $(CLIBS)


# Targets

all: Static Shared $(STATICLIB)

all_static: Static $(STATICLIB)

all_shared: Shared $(SHAREDLIB)

World:
	make clean
	make all
	make install

clean:
	/bin/rm -rf Shared Static *.o
	/bin/rm -rf lib$(NAME).* *$(NAME).so
	/bin/rm -rf .make.state .nse_depinfo *.dSYM
	/bin/rm -f  phead vocutout vometa votaskd

help: HELP

#phead: phead.c Static $(STATICLIB)
phead: phead.c 
	$(CC) $(CFLAGS) -o phead phead.c `ast_link` -lcfitsio

#votaskd: votaskd.c Static $(STATICLIB)
votaskd: votaskd.c 
	$(CC) $(CFLAGS) -o votaskd votaskd.c

#vocutout: vocutout.c Static $(STATICLIB)
vocutout: vocutout.c
	$(CC) $(CFLAGS) -o vocutout vocutout.c `ast_link` -lcfitsio

#vometa: vometa.c Static $(STATICLIB)
vometa: vometa.c
	$(CC) $(CFLAGS) -o vometa vometa.c `ast_link` -lcfitsio



###############################################################################
# Leave this stuff alone.
###############################################################################

$(STATICLIB): $(SRCS:%.c=Static/%.o)
	/usr/bin/ar rv $@ $?
Static/%.o: %.c $(INCS)
	$(CC) $(CINCS) $(CFLAGS) -c $< -o $@
Static:
	/bin/mkdir $@
	chmod 777 $@

$(SHAREDLIB): $(SRCS:%.c=Shared/%.o)
	/usr/bin/ld -shared -o $@ $? -lc -ldl
Shared/%.o: %.c $(INCS)
	$(CC) $(CINCS) $(CFLAGS) -fpic -shared -c $< -o $@
Shared:
	/bin/mkdir $@
	chmod 777 $@
