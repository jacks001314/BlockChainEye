package com.antell.cloudhands.api.packet.udp.dns.record;

import com.antell.cloudhands.api.packet.udp.dns.common.DNSDataInput;
import com.antell.cloudhands.api.packet.udp.dns.common.Rcode;
import com.antell.cloudhands.api.utils.MessagePackUtil;
import com.antell.cloudhands.api.utils.TextUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.msgpack.core.MessageUnpacker;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Options - describes Extended DNS (EDNS) properties of a Message.
 * No specific options are defined other than those specified in the
 * header.  An OPT should be generated by Resolver.
 * <p>
 * EDNS is a method to extend the DNS protocol while providing backwards
 * compatibility and not significantly changing the protocol.  This
 * implementation of EDNS is mostly complete at level 0.
 *
 */

public class OPTRecord extends Record {

    private List options;

    public OPTRecord() {

    }

    @Override
    public Record getObject() {
        return new OPTRecord();
    }

    @Override
    public void read(DataInput in) throws IOException {

        int n = in.readUnsignedShort();
        options = new ArrayList();

        for(int i = 0;i<n;i++){
            EDNSOption option = EDNSOption.build(in);
            options.add(option);
        }

    }

    @Override
    public void read(MessageUnpacker unpacker) throws IOException {

        options = new ArrayList();

        MessagePackUtil.parseMapHeader(unpacker,true);
        int n = MessagePackUtil.parseArrayHeader(unpacker,true);
        for(int i = 0;i<n;i++){
            EDNSOption option = EDNSOption.build(unpacker);
            options.add(option);
        }
    }

    @Override
    public void read(DNSDataInput in) throws IOException {


    }

    /**
     * Converts rdata to a String
     */
    @Override
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        if (options != null) {
            sb.append(options);
            sb.append(" ");
        }
        sb.append(" ; payload ");
        sb.append(getPayloadSize());
        sb.append(", xrcode ");
        sb.append(getExtendedRcode());
        sb.append(", version ");
        sb.append(getVersion());
        sb.append(", flags ");
        sb.append(getFlags());
        return sb.toString();
    }

    @Override
    public XContentBuilder rdataToJson(XContentBuilder cb) throws IOException {

        cb.field("options",options);
        cb.field("payload",getPayloadSize());
        cb.field("xrcode",getExtendedRcode());
        cb.field("version",getVersion());
        cb.field("flags",getFlags());

        return cb;
    }

    @Override
    public void rdataToJsonString(StringBuffer sb) {
        sb.append("{");
        TextUtils.addText(sb, "options", options, true);
        TextUtils.addText(sb, "payload", getPayloadSize(), true);
        TextUtils.addText(sb, "xrcode", getExtendedRcode(), true);
        TextUtils.addText(sb, "version", getVersion(), true);
        TextUtils.addText(sb, "flags", getFlags(), false);
        sb.append("}");
    }

    /**
     * Returns the maximum allowed payload size.
     */
    public int getPayloadSize() {
        return dclass;
    }

    /**
     * Returns the extended Rcode
     *
     * @see Rcode
     */
    public int getExtendedRcode() {
        return (int) (ttl >>> 24);
    }

    /**
     * Returns the highest supported EDNS version
     */
    public int getVersion() {
        return (int) ((ttl >>> 16) & 0xFF);
    }

    /**
     * Returns the EDNS flags
     */
    public int getFlags() {
        return (int) (ttl & 0xFFFF);
    }


    /**
     * Gets all options in the OPTRecord.  This returns a list of EDNSOptions.
     */
    public List getOptions() {
        if (options == null)
            return Collections.EMPTY_LIST;
        return Collections.unmodifiableList(options);
    }

    /**
     * Gets all options in the OPTRecord with a specific code.  This returns a list
     * of EDNSOptions.
     */
    public List getOptions(int code) {
        if (options == null)
            return Collections.EMPTY_LIST;
        List list = Collections.EMPTY_LIST;
        for (Iterator it = options.iterator(); it.hasNext(); ) {
            EDNSOption opt = (EDNSOption) it.next();
            if (opt.getCode() == code) {
                if (list == Collections.EMPTY_LIST)
                    list = new ArrayList();
                list.add(opt);
            }
        }
        return list;
    }

    /**
     * Determines if two OPTRecords are identical.  This compares the name, type,
     * class, and rdata (with names canonicalized).  Additionally, because TTLs
     * are relevant for OPT records, the TTLs are compared.
     *
     * @param arg The record to compare to
     * @return true if the records are equal, false otherwise.
     */
    @Override
    public boolean equals(final Object arg) {
        return super.equals(arg) && ttl == ((OPTRecord) arg).ttl;
    }

}
