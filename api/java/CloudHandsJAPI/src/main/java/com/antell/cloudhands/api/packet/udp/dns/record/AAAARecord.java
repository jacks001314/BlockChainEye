package com.antell.cloudhands.api.packet.udp.dns.record;

import com.antell.cloudhands.api.packet.udp.dns.common.DNSDataInput;
import com.antell.cloudhands.api.utils.MessagePackUtil;
import com.antell.cloudhands.api.utils.Text;
import com.antell.cloudhands.api.utils.TextUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.msgpack.core.MessageUnpacker;

import java.io.DataInput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IPv6 Address Record - maps a domain name to an IPv6 address
 */

public class AAAARecord extends Record {


    private byte[] address;

    public AAAARecord() {
    }

    @Override
    public Record getObject() {
        return new AAAARecord();
    }


    @Override
    public void read(DataInput in) throws IOException {

        address = Text.readBytes(in,2);
    }

    @Override
    public void read(MessageUnpacker unpacker) throws IOException {

        MessagePackUtil.parseMapHeader(unpacker,true);
        address = MessagePackUtil.parseBin(unpacker);
    }

    @Override
    public void read(DNSDataInput dataInput) throws IOException {

        this.address = dataInput.readByteArray(16);
    }

    /**
     * Converts rdata to a String
     */
    @Override
    public String rrToString() {
        InetAddress addr;
        try {
            addr = InetAddress.getByAddress(null, address);
        } catch (UnknownHostException e) {
            return null;
        }
        if (addr.getAddress().length == 4) {
            // Deal with Java's broken handling of mapped IPv4 addresses.
            StringBuffer sb = new StringBuffer("0:0:0:0:0:ffff:");
            int high = ((address[12] & 0xFF) << 8) + (address[13] & 0xFF);
            int low = ((address[14] & 0xFF) << 8) + (address[15] & 0xFF);
            sb.append(Integer.toHexString(high));
            sb.append(':');
            sb.append(Integer.toHexString(low));
            return sb.toString();
        }
        return addr.getHostAddress();
    }

    @Override
    public XContentBuilder rdataToJson(XContentBuilder cb) throws IOException {

        String addr = rrToString();
        cb.field("address",addr == null?"":addr);
        return cb;
    }

    @Override
    public void rdataToJsonString(StringBuffer sb) {
        String addr = rrToString();
        sb.append("{");
        TextUtils.addText(sb, "address", addr == null?"":addr, false);
        sb.append("}");
    }

    /**
     * Returns the address
     */
    public InetAddress getAddress() {
        try {
            if (name == null)
                return InetAddress.getByAddress(address);
            else
                return InetAddress.getByAddress(name.toString(),
                        address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
