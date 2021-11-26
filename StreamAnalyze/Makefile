include ../make.include
DPDK=/usr/local/dpdk/dpdk_src
DPDK_FLAGS=$(shell cat ${DPDK}/include/cflags.txt)
DPDK_LIB_FLAGS = $(shell cat ${DPDK}/lib/ldflags.txt)
CFLAGS  = ${BUILD_CFLAGS}  -O0 -rdynamic -D_GNU_SOURCE -DUSE_DPDK ${DPDK_FLAGS} -pthread
CFLAGS += -I../lib/common  -I../lib/shm -I../lib/thread -I../lib/ptables 
CFLAGS +=-I../packet -I../MProcess  -I../rule -I../filter 
CFLAGS += -I../lib/fstore  -I${DPDK}/include -include ${DPDK}/include/rte_config.h

LDFLAGS  = ${BUILD_LDFLAGS} ${DPDK_LIB_FLAGS} -lpthread -lcjson


common_package = ../lib/common/*.o
cpool_package = ../lib/thread/*.o
shm_package = ../lib/shm/*.o
ptables_package = ../lib/ptables/*.o
pkt_package = ../packet/*.o
mprocess_package = ../MProcess/*.o
rule_package=../rule/*.o
filter_package=../filter/*.o

sa_package= SAMain

sa_SOURCES = StreamAnalyze.c \
			 ch_sa_context.c \
			 ch_assemble_fragment.c \
			 ch_tcp_context.c \
			 ch_tcp_session.c \
			 ch_tcp_session_endpoint.c \
			 ch_tcp_session_pool.c \
             ch_tcp_session_request_pool.c \
			 ch_sa_tcp_session_request_handler.c \
			 ch_sa_tcp_session_handler.c \
			 ch_sa_session_task.c \
			 ch_sa_session_task_pool.c \
			 ch_sa_work.c \
			 ch_sa_packet_dp.c \
			 ch_sa_data_store_pool.c 

sa_OBJECTS =$(patsubst %.c,%.o,$(sa_SOURCES))
sa_DEPENDS =$(patsubst %.c,%.d,$(sa_SOURCES))
sa_ASMFILE =$(patsubst %.c,%.s,$(sa_SOURCES))

quiet_cmd_dpdk = LINK   $@ 
	cmd_sa = ${CC} ${CFLAGS} -o $@ $(sa_OBJECTS)  \
				   $(common_package) $(cpool_package) $(shm_package) $(ptables_package) $(rule_package)\
				   $(pkt_package)  $(mprocess_package)  $(filter_package)\
				   $(LDFLAGS)

.PHONY: all clean

all: $(sa_package)

$(sa_package): $(sa_OBJECTS) 
	$(call cmd,sa)
-include $(sa_DEPENDS)

$(common_package):
	$(call,make)

$(shm_package):
	$(call,make)

$(thread_package):
	$(call,make)

$(ptables_package):
	$(call,make)

$(rule_package):
	$(call,make)

$(pkt_package):
	$(call,make)

$(mprocess_package):
	$(call,make)

$(filter_package):
	$(call,make)

clean:
	@rm -rf $(sa_OBJECTS) $(sa_DEPENDS) $(sa_ASMFILE) $(sa_package)


