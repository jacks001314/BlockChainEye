package com.antell.cloudhands.api.packet.udp.dns.record;

import com.antell.cloudhands.api.packet.udp.dns.common.DNSDataInput;
import com.antell.cloudhands.api.utils.MessagePackUtil;
import com.antell.cloudhands.api.utils.Text;
import com.antell.cloudhands.api.utils.TextUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.msgpack.core.MessageUnpacker;

import java.io.DataInput;
import java.io.IOException;

/**
 * Host Information - describes the CPU and OS of a host
 */

public class HINFORecord extends Record {

    private String cpu;
    private String os;

    public HINFORecord() {
    }

    @Override
    public Record getObject() {
        return new HINFORecord();
    }


    @Override
    public void read(DataInput in) throws IOException {
        cpu = Text.readString(in,2);
        os = Text.readString(in,2);

    }

    @Override
    public void read(MessageUnpacker unpacker) throws IOException {

        MessagePackUtil.parseMapHeader(unpacker,true);
        cpu = MessagePackUtil.parseText(unpacker);
        os = MessagePackUtil.parseText(unpacker);
    }

    @Override
    public void read(DNSDataInput in) throws IOException {

        cpu = byteArrayToString(in.readCountedString(),false);
        os = byteArrayToString(in.readCountedString(),false);
    }

    /**
     * Returns the host's CPU
     */
    public String getCPU() {

        return cpu;
    }

    /**
     * Returns the host's OS
     */
    public String getOS() {

        return os;
    }

    /**
     * Converts to a string
     */
    @Override
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(cpu);
        sb.append(" ");
        sb.append(os);
        return sb.toString();
    }

    @Override
    public XContentBuilder rdataToJson(XContentBuilder cb) throws IOException {

        cb.field("cpu",cpu);
        cb.field("os",os);

        return cb;
    }

    @Override
    public void rdataToJsonString(StringBuffer sb) {
        sb.append("{");
        TextUtils.addText(sb, "cpu", cpu, true);
        TextUtils.addText(sb, "os", os, false);
        sb.append("}");
    }

}
