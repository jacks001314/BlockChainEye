/*
 *
 *      Filename: ch_tcp_context.c
 *
 *        Author: shajf,csp001314@gmail.com
 *   Description: ---
 *        Create: 2018-01-30 12:19:59
 * Last Modified: 2018-09-27 11:12:04
 */

#include "ch_config.h"
#include "ch_tcp_context.h"
#include "ch_entry_pool.h"
#include "ch_log.h"

static void do_tcp_context_init(ch_tcp_context_t *tcp_context){

    tcp_context->tcp_session_request_pool_type = FROM_OBJ_POOL;
	tcp_context->tcp_session_pool_type = FROM_OBJ_POOL;
	tcp_context->tcp_session_request_limits = 10000000;
    tcp_context->tcp_session_limits = 10000000; 
    
    tcp_context->tcp_session_tbl_size = 65536;
    tcp_context->tcp_session_request_tbl_size = 65536;
    tcp_context->tcp_session_cache_limits = 1024;
    tcp_context->tcp_session_request_cache_limits = 1024;

    tcp_context->tcp_session_request_timeout = 60;
    tcp_context->tcp_session_timeout = 5*60;

	tcp_context->mm_max_cache_size = 128*1024*1024;
	tcp_context->mm_timeout = 60;

    tcp_context->use_mpa = 0;
    tcp_context->mpa_caches = 0;
    tcp_context->mpa_cache_inits = 0;
    tcp_context->mpa_pool_size = 0;

    tcp_context->ptable_ring_size = 4096;

    tcp_context->ptable_check_tv = 60;
}



static const char *cmd_session_pool_type(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){


    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

	if(strcasecmp(p1,"dpdk_pool")==0){

		context->tcp_session_pool_type = FROM_DPDK_POOL;
	}else{
	
		context->tcp_session_pool_type = FROM_OBJ_POOL;
	}

    return NULL;
}

static const char *cmd_session_request_pool_type(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){


    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

	if(strcasecmp(p1,"dpdk_pool")==0){

		context->tcp_session_request_pool_type = FROM_DPDK_POOL;
	}else{
	
		context->tcp_session_request_pool_type = FROM_OBJ_POOL;
	}

    return NULL;
}


static const char *cmd_tcp_session_request_limits(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_request_limits = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_request_limits <=0){
        return "invalid tcp session requests number limits config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_limits(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_limits = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_limits <=0){
        return "invalid tcp session number limits config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_tbl_size(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_tbl_size = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_tbl_size <=0){
        return "invalid tcp session table size config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_request_tbl_size(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_request_tbl_size = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_request_tbl_size <=0){
        return "invalid tcp session requests table size config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_cache_limits(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_cache_limits = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_cache_limits <=0){
        return "invalid tcp session table cache limits config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_request_cache_limits(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_request_cache_limits = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_request_cache_limits <=0){
        return "invalid session request table cache limits config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_request_timeout(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_request_timeout = (uint64_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_request_timeout <=0){
        return "invalid tcp session request timeout config value";
    }

    return NULL;
}

static const char *cmd_tcp_session_timeout(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->tcp_session_timeout = (uint64_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_timeout <=0){
        return "invalid tcp  session timeout config value";
    }

    return NULL;
}


static const char *cmd_mm(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1,const char *p2){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->mm_max_cache_size = (size_t)strtoul(p1,&endptr,10);
    context->mm_timeout = (uint64_t)strtoul(p2,&endptr,10);
    
    if(context->mm_timeout <=0||context->mm_max_cache_size<=0){
        return "invalid memory max cache size and timeout config value !";
    }

    return NULL;
}


static const char *cmd_mpa_use(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->use_mpa = 0;
    if(strcasecmp(p1,"true")==0)
        context->use_mpa = 1;


    return NULL;
}

static const char *cmd_mpa_caches(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->mpa_caches = strtoul(p1,&endptr,10);
    

    return NULL;
}

static const char *cmd_mpa_cache_inits(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->mpa_cache_inits = (size_t)strtoul(p1,&endptr,10);
    

    return NULL;
}

static const char *cmd_mpa_pool_size(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->mpa_pool_size = (size_t)strtoul(p1,&endptr,10);
    

    return NULL;
}

static const char *cmd_ptable_ring_size(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->ptable_ring_size = (size_t)strtoul(p1,&endptr,10);
    
    if(context->tcp_session_tbl_size <=0){
        return "invalid tcp session ptable ring size";
    }

    return NULL;
}

static const char *cmd_ptable_check_tv(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){

    char *endptr;

    ch_tcp_context_t *context = (ch_tcp_context_t*)_dcfg;

    context->ptable_check_tv = (size_t)strtoul(p1,&endptr,10);
    

    return NULL;
}

static const command_rec tcp_context_directives[] = {
    

	CH_INIT_TAKE1(
            "CHTCPSessionPoolType",
            cmd_session_pool_type,
            NULL,
            0,
            "set tcp session pool type(dpdk_pool or obj_pool) config item"
            ),
	
	CH_INIT_TAKE1(
            "CHTCPSessionRequestPoolType",
            cmd_session_request_pool_type,
            NULL,
            0,
            "set tcp session request pool type(dpdk_pool or obj_pool) config item"
            ),

    
    CH_INIT_TAKE1(
            "CHTCPSessionRequestLimits",
            cmd_tcp_session_request_limits,
            NULL,
            0,
            "set tcp session requests limits config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionLimits",
            cmd_tcp_session_limits,
            NULL,
            0,
            "set tcp  session limits config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionTblSize",
            cmd_tcp_session_tbl_size,
            NULL,
            0,
            "set tcp session table size config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionRequestTblSize",
            cmd_tcp_session_request_tbl_size,
            NULL,
            0,
            "set tcp session request table size config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionTblCacheLimits",
            cmd_tcp_session_cache_limits,
            NULL,
            0,
            "set tcp  session table cache limits config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionRequestTblCacheLimits",
            cmd_tcp_session_request_cache_limits,
            NULL,
            0,
            "set tcp session request table cache limits config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionRequestTimeout",
            cmd_tcp_session_request_timeout,
            NULL,
            0,
            "set tcp session request timeout config item"
            ),

    CH_INIT_TAKE1(
            "CHTCPSessionTimeout",
            cmd_tcp_session_timeout,
            NULL,
            0,
            "set tcp session timeout config item"
            ),

	CH_INIT_TAKE2(
            "CHTCPMM",
            cmd_mm,
            NULL,
            0,
            "set memory max cache size and timeout"
            ),
	
    
    CH_INIT_TAKE1(
            "CHTCPUseMPA",
            cmd_mpa_use,
            NULL,
            0,
            "use memory pool alloc agent or not"
            ),

    CH_INIT_TAKE1(
            "CHTCPMPACaches",
            cmd_mpa_caches,
            NULL,
            0,
            "memory pool alloc agent cache number"
            ),

    CH_INIT_TAKE1(
            "CHTCPMPACacheInits",
            cmd_mpa_cache_inits,
            NULL,
            0,
            "memory pool alloc agent cache init number "
            ),

    CH_INIT_TAKE1(
            "CHTCPMPAPoolSize",
            cmd_mpa_pool_size,
            NULL,
            0,
            "memory pool alloc agent cache pool size"
            ),

    CH_INIT_TAKE1(
            "CHTCPPtableRingSize",
            cmd_ptable_ring_size,
            NULL,
            0,
            "set tcp ptable ring size"
            ),

    CH_INIT_TAKE1(
            "CHTCPPtableCheckTV",
            cmd_ptable_check_tv,
            NULL,
            0,
            "set tcp ptable entries time out check time interval"
            ),
};

static inline void dump_tcp_context(ch_tcp_context_t *tcp_context){

    fprintf(stdout,"Dump tcp  context-------------------------------------------\n");

    
	fprintf(stdout,"tcp session pool type:%s\n",tcp_context->tcp_session_pool_type==FROM_DPDK_POOL?"dpdk_pool":"obj_pool");
    fprintf(stdout,"tcp session request pool type:%s\n",tcp_context->tcp_session_request_pool_type==FROM_DPDK_POOL?"dpdk_pool":"obj_pool");
	
	fprintf(stdout,"tcp session requests limits:%lu\n",(unsigned long)tcp_context->tcp_session_request_limits);
    fprintf(stdout,"tcp session limits:%lu\n",(unsigned long)tcp_context->tcp_session_limits);
    fprintf(stdout,"tcp session table size:%lu\n",(unsigned long)tcp_context->tcp_session_tbl_size);
    fprintf(stdout,"tcp session request table size:%lu\n",(unsigned long)tcp_context->tcp_session_request_tbl_size);
    fprintf(stdout,"tcp session table cache limits:%lu\n",(unsigned long)tcp_context->tcp_session_cache_limits);
    fprintf(stdout,"tcp session request table cache limits:%lu\n",(unsigned long)tcp_context->tcp_session_request_cache_limits);
    fprintf(stdout,"tcp session request timeout:%lu\n",(unsigned long)tcp_context->tcp_session_request_timeout);
    fprintf(stdout,"tcp session timeout:%lu\n",(unsigned long)tcp_context->tcp_session_timeout);
    fprintf(stdout,"tcp memory max cache size:%lu\n",(unsigned long)tcp_context->mm_max_cache_size);
    fprintf(stdout,"tcp memory timeout:%lu\n",(unsigned long)tcp_context->mm_timeout);

    fprintf(stdout,"tcp session use memory pool agent:%s\n",tcp_context->use_mpa?"yes":"no");

	fprintf(stdout,"tcp session mpa.caches:%lu\n",tcp_context->mpa_caches);
	fprintf(stdout,"tcp session mpa.cache.inits:%lu\n",tcp_context->mpa_cache_inits);
	fprintf(stdout,"tcp session mpa.cache.pool.size:%lu\n",tcp_context->mpa_pool_size);
	fprintf(stdout,"tcp session ptable.ring.size:%lu\n",tcp_context->ptable_ring_size);
	fprintf(stdout,"tcp session ptable.check.tv:%lu\n",tcp_context->ptable_check_tv);

}

ch_tcp_context_t * ch_tcp_context_create(ch_pool_t *mp,const char *cfname){

    const char * msg = NULL;

	ch_tcp_context_t *tcp_context = ch_palloc(mp,sizeof(*tcp_context));

	tcp_context->mp = mp;

    do_tcp_context_init(tcp_context);


    msg = ch_process_command_config(tcp_context_directives,(void*)tcp_context,mp,mp,cfname);

    /*config failed*/
    if(msg!=NULL){

        ch_log(CH_LOG_ERR,"Config tcp context error:%s",msg);
        return NULL;
    }

    dump_tcp_context(tcp_context);

    return tcp_context;

}

