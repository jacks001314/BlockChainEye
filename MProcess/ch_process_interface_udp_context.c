/*
 *
 *      Filename: ch_process_interface_udp_context.c
 *
 *        Author: shajf,csp001314@gmail.com
 *   Description: ---
 *        Create: 2018-02-06 11:16:43
 * Last Modified: 2018-09-14 15:33:07
 */

#include "ch_process_interface_udp_context.h"
#include "ch_log.h"
#include "ch_config.h"
#include "ch_packet_udp.h"
#include "ch_jhash.h" 
#include "ch_util.h"

static void do_pint_udp_context_init(ch_process_interface_udp_context_t *pint_context){

	pint_context->pool_name = "udp_process_interface_pool";
	pint_context->qprefix = "udp_process_interface_queue";
	pint_context->qnumber = 1;
	pint_context->qsize = 65536;

    pint_context->is_pkt_copy = 0;

	memset(pint_context->accept_ports,0,MAX_PORT_ARRAY_SIZE);
}


static const char *cmd_process_interface_name(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1,const char *p2){


    ch_process_interface_udp_context_t *context = (ch_process_interface_udp_context_t*)_dcfg;

    context->pool_name = p1;
    context->qprefix = p2;

    return NULL;
}

static const char *cmd_process_interface_size(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1,const char *p2){

    char *endptr;

    ch_process_interface_udp_context_t *context = (ch_process_interface_udp_context_t*)_dcfg;

    context->qnumber = (uint32_t)strtoul(p1,&endptr,10);
    context->qsize = (uint32_t)strtoul(p2,&endptr,10);
    
    if(context->qnumber <=0||context->qsize<=0){
        return "invalid process pool queue number and queue size config value !";
    }

    return NULL;
}

static const char * cmd_accept_ports(cmd_parms *cmd ch_unused,void *_dcfg,int argc,char *const argv[]){

    char *endptr;
	int i;
	uint16_t port;

	ch_process_interface_udp_context_t *context = (ch_process_interface_udp_context_t*)_dcfg;

    if(argc>MAX_PORT_ARRAY_SIZE){
    
        return "The ports is too much!!";
    }
    
	for(i = 0; i<argc;i++){
	
		port = (uint16_t)strtoul(argv[i],&endptr,10);
		if(port == 0){
		
			return "The port is zero!";
		}
		
		context->accept_ports[i] = port;
	}

    return NULL;

}

static const char *cmd_is_pkt_copy(cmd_parms *cmd ch_unused, void *_dcfg, const char *p1){


    ch_process_interface_udp_context_t *context = (ch_process_interface_udp_context_t*)_dcfg;

    context->is_pkt_copy = 0;

    if(strcasecmp(p1,"true")==0)
        context->is_pkt_copy = 1;


    return NULL;
}


static const command_rec pint_context_udp_directives[] = {

	CH_INIT_TAKE2(
            "CHUDPProcessInterfaceName",
            cmd_process_interface_name,
            NULL,
            0,
            "set udp process interface pool name and queue prefix config item"
            ),

    CH_INIT_TAKE2(
            "CHUDPProcessInterfaceSize",
            cmd_process_interface_size,
            NULL,
            0,
            "set udp process interface queue number and queue size"
            ),
    
	CH_INIT_TAKE_ARGV(
            "CHUDPAcceptPorts",
            cmd_accept_ports,
            NULL,
            0,
            "set  udp accept ports"
            ),
	
    CH_INIT_TAKE1(
            "CHPacketCopy",
            cmd_is_pkt_copy,
            NULL,
            0,
            "set udp packet copy from dpdk mbuf"
            ),

};

static inline void dump_pint_udp_context(ch_process_interface_udp_context_t *pint_context){

	int i;
    fprintf(stdout,"Dump process interface udp  context-------------------------------------------\n");

    fprintf(stdout,"udp process interface pool name:%s\n",pint_context->pool_name);
    fprintf(stdout,"udp process interface queue prefix:%s\n",pint_context->qprefix);
    fprintf(stdout,"udp process interface queue number:%lu\n",(unsigned long)pint_context->qnumber);
    fprintf(stdout,"udp process interface queue size:%lu\n",(unsigned long)pint_context->qsize);
    fprintf(stdout,"udp process interface is_pkt_copy:%s\n",pint_context->is_pkt_copy?"true":"false");
    

	fprintf(stdout,"accept udp ports:\n");
	for(i = 0;i<MAX_PORT_ARRAY_SIZE;i++){

		if(pint_context->accept_ports[i] == 0)
			break;

		fprintf(stdout,"%d,",(int)pint_context->accept_ports[i]);
	}

}

static int _udp_filter(ch_packet_t *pkt,void *priv_data ch_unused){

	ch_packet_udp_t udp_pkt;
	
	//ch_process_interface_udp_context_t *pint_context = (ch_process_interface_udp_context_t*)priv_data;

	if(pkt->pkt_type!=PKT_TYPE_UDP){
	
		return 1;
	}

	if(-1 == ch_packet_udp_init_from_pkt(&udp_pkt,pkt))
		return 1;

	return 0;
}

static uint32_t _udp_hash(ch_packet_t *pkt,void *priv_data ch_unused){


    return pkt->hash;

}


ch_process_interface_udp_context_t * ch_process_interface_udp_context_create(ch_pool_t *mp,const char *cfname,int is_write){

    const char * msg = NULL;

	ch_process_interface_udp_context_t *pint_context = ch_palloc(mp,sizeof(*pint_context));

	pint_context->mp = mp;

    do_pint_udp_context_init(pint_context);


    msg = ch_process_command_config(pint_context_udp_directives,(void*)pint_context,mp,mp,cfname);

    /*config failed*/
    if(msg!=NULL){

        ch_log(CH_LOG_ERR,"Config process interface udp  context error:%s",msg);
        return NULL;
    }

	if(is_write){
	
		pint_context->pint = ch_process_interface_writer_create(mp,
			pint_context->qprefix,
			pint_context->qnumber,
			pint_context->qsize,
			_udp_filter,
			_udp_hash,
			(void*)pint_context);
		

	}else {
	
	
		pint_context->pint = ch_process_interface_reader_create(mp,
			pint_context->qprefix,
			pint_context->qnumber,
            pint_context->is_pkt_copy);
	}

	if(pint_context->pint == NULL){
	
		ch_log(CH_LOG_ERR,"Cannot create process interface!");
		return NULL;
	}

    dump_pint_udp_context(pint_context);

    return pint_context;

}

