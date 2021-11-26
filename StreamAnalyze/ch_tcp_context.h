/*
 *
 *      Filename: ch_tcp_context.h
 *
 *        Author: shajf,csp001314@gmail.com
 *   Description: ---
 *        Create: 2017-12-25 11:00:04
 * Last Modified: 2017-12-25 11:00:04
 */

#ifndef CH_TCP_CONTEXT_H
#define CH_TCP_CONTEXT_H

typedef struct ch_tcp_context_t ch_tcp_context_t;

#include "ch_mpool.h"

struct ch_tcp_context_t {

	ch_pool_t *mp;
   
	int tcp_session_request_pool_type;
	int tcp_session_pool_type;
	
	size_t tcp_session_request_limits;
	size_t tcp_session_limits;
	
	size_t tcp_session_request_tbl_size;
	size_t tcp_session_tbl_size;

	size_t tcp_session_request_cache_limits;
	size_t tcp_session_cache_limits;

	uint64_t tcp_session_request_timeout;
	uint64_t tcp_session_timeout;


	size_t mm_max_cache_size;
	uint64_t mm_timeout;

    int use_mpa;
    unsigned long mpa_caches;
    unsigned long mpa_cache_inits;
    unsigned long mpa_pool_size;

    size_t ptable_ring_size;

    uint64_t ptable_check_tv;

};

extern ch_tcp_context_t * ch_tcp_context_create(ch_pool_t *mp,const char *cfname);

#endif /*CH_TCP_CONTEXT_H*/
