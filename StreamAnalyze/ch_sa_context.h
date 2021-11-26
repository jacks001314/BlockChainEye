/*
 *
 *      Filename: ch_sa_context.h
 *
 *        Author: shajf,csp001314@gmail.com
 *   Description: ---
 *        Create: 2017-12-21 11:41:38
 * Last Modified: 2017-12-21 11:41:38
 */

#ifndef CH_SA_CONTEXT_H
#define CH_SA_CONTEXT_H

typedef struct ch_sa_context_t ch_sa_context_t;

#include "ch_mpool.h"

struct ch_sa_context_t {

	ch_pool_t *mp;

	const char *log_name;
	int log_level;
	
	uint64_t entry_size;
	uint64_t shm_size;

	const char *key;
	int proj_id;

	const char *mmap_file_dir;

	uint16_t tasks_n;

	const char *tcp_cfg_path;

	const char *rdb_store_dir;

	const char *rdb_name_create_type;

	uint32_t payload_data_size;

	const char *tcp_cfname;

	const char *pint_sa_cfname;



	uint64_t rdb_using_timeout;

	uint32_t dstore_limits;

    int tcp_sa_on;
    int is_break_data_ok;

    const char *filter_json_file;

    size_t ptable_ring_size;
    uint64_t ptable_check_tv;
    
    int use_msgpack;

};

extern ch_sa_context_t * ch_sa_context_create(ch_pool_t *mp,const char *cfname);


#endif /*CH_SA_CONTEXT_H*/
