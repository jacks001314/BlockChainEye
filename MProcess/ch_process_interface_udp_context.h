/*
 *
 *      Filename: ch_process_interface_udp_context.h
 *
 *        Author: shajf,csp001314@gmail.com
 *   Description: ---
 *        Create: 2018-02-06 10:31:14
 * Last Modified: 2018-02-06 10:31:14
 */

#ifndef CH_PROCESS_INTERFACE_UDP_CONTEXT_H
#define CH_PROCESS_INTERFACE_UDP_CONTEXT_H

typedef struct ch_process_interface_udp_context_t ch_process_interface_udp_context_t;

#include "ch_mpool.h"
#include "ch_process_interface.h"

#define MAX_PORT_ARRAY_SIZE 1024

struct ch_process_interface_udp_context_t {

	ch_pool_t *mp;
	
	ch_process_interface_t *pint;
	const char *pool_name;

	const char *qprefix;

	uint32_t qnumber;

	uint32_t qsize;

    int is_pkt_copy;

	uint16_t accept_ports[MAX_PORT_ARRAY_SIZE];
	
};

extern ch_process_interface_udp_context_t *ch_process_interface_udp_context_create(ch_pool_t *mp,const char *cfname,int is_write);

#endif /*CH_PROCESS_INTERFACE_UDP_CONTEXT_H*/
