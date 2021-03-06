package com.antell.cloudhands.api.packet.udp.dns.common;

import java.nio.ByteBuffer;

/**
 * Created by dell on 2017/4/10.
 */
public class BasicDNSDataInput implements DNSDataInput {

    private ByteBuffer byteBuffer;
    private int saved_pos;
    private int saved_end;
    private int base_pos;

    /**
     * Creates a new DNSInput
     * @param input The byte array to read from
     */
    public BasicDNSDataInput(byte [] input,int base_pos) {
        byteBuffer = ByteBuffer.wrap(input);
        saved_pos = -1;
        saved_end = -1;
        this.base_pos = base_pos;

    }
    /**
     * Creates a new DNSInput
     * @param input The byte array to read from
     */
    public BasicDNSDataInput(byte [] input) {
        byteBuffer = ByteBuffer.wrap(input);
        saved_pos = -1;
        saved_end = -1;
        this.base_pos = 0;
    }
    /**
     * Creates a new DNSInput from the given {@link ByteBuffer}
     * @param byteBuffer The ByteBuffer
     */
    public BasicDNSDataInput(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        saved_pos = -1;
        saved_end = -1;
        this.base_pos = 0;
    }

    /**
     * Returns the current position.
     */
    @Override
    public int current() {
        return byteBuffer.position();
    }

    /**
     * Returns the number of bytes that can be read from this stream before
     * reaching the end.
     */
    @Override
    public int remaining() {
        return byteBuffer.remaining();
    }

    private void require(int n) throws ParseException {
        if (n > remaining()) {
            throw new ParseException("end of input");
        }
    }

    /**
     * Marks the following bytes in the stream as active.
     * @param len The number of bytes in the active region.
     * @throws IllegalArgumentException The number of bytes in the active region
     * is longer than the remainder of the input.
     */
    @Override
    public void setActive(int len) {
        if (len > byteBuffer.capacity() - byteBuffer.position()) {
            throw new IllegalArgumentException("cannot set active " +
                    "region past end of input");
        }
        byteBuffer.limit(byteBuffer.position() + len);
    }

    /**
     * Clears the active region of the string.  Further operations are not
     * restricted to part of the input.
     */
    @Override
    public void clearActive() {
        byteBuffer.limit(byteBuffer.capacity());
    }

    /**
     * Returns the position of the end of the current active region.
     */
    @Override
    public int saveActive() {
        return byteBuffer.limit();
    }

    /**
     * Restores the previously set active region.  This differs from setActive() in
     * that restoreActive() takes an absolute position, and setActive takes an
     * offset from the current location.
     * @param pos The end of the active region.
     */
    @Override
    public void restoreActive(int pos) {
        if (pos > byteBuffer.capacity()) {
            throw new IllegalArgumentException("cannot set active " +
                    "region past end of input");
        }
        byteBuffer.limit(byteBuffer.position());
    }

    /**
     * Resets the current position of the input stream to the specified index,
     * and clears the active region.
     * @param index The position to continue parsing at.
     * @throws IllegalArgumentException The index is not within the input.
     */
    @Override
    public void jump(int index) {
        if (index >= byteBuffer.capacity()) {
            throw new IllegalArgumentException("cannot jump past " +
                    "end of input");
        }
        byteBuffer.position(index);
        byteBuffer.limit(byteBuffer.capacity());
    }

    /**
     * Saves the current state of the input stream.  Both the current position and
     * the end of the active region are saved.
     * @throws IllegalArgumentException The index is not within the input.
     */
    @Override
    public void save() {
        saved_pos = byteBuffer.position();
        saved_end = byteBuffer.limit();
    }

    /**
     * Restores the input stream to its state before the call to {@link #save}.
     */
    @Override
    public void restore() {
        if (saved_pos < 0) {
            throw new IllegalStateException("no previous state");
        }
        byteBuffer.position(saved_pos);
        byteBuffer.limit(saved_end);
        saved_pos = -1;
        saved_end = -1;
    }

    /**
     * Reads an unsigned 8 bit value from the stream, as an int.
     * @return An unsigned 8 bit value.
     * @throws ParseException The end of the stream was reached.
     */
    @Override
    public int readU8() throws ParseException {
        require(1);
        return (byteBuffer.get() & 0xFF);
    }

    /**
     * Reads an unsigned 16 bit value from the stream, as an int.
     * @return An unsigned 16 bit value.
     * @throws ParseException The end of the stream was reached.
     */
    @Override
    public int readU16() throws ParseException {
        require(2);
        return (byteBuffer.getShort() & 0xFFFF);
    }

    /**
     * Reads an unsigned 32 bit value from the stream, as a long.
     * @return An unsigned 32 bit value.
     * @throws ParseException The end of the stream was reached.
     */
    @Override
    public long readU32() throws ParseException {
        require(4);
        return (byteBuffer.getInt() & 0xFFFFFFFFL);
    }

    /**
     * Reads a byte array of a specified length from the stream into an existing
     * array.
     * @param b The array to read into.
     * @param off The offset of the array to start copying data into.
     * @param len The number of bytes to copy.
     * @throws ParseException The end of the stream was reached.
     */
    @Override
    public void readByteArray(byte [] b, int off, int len) throws ParseException {
        require(len);
        byteBuffer.get(b, off, len);
    }

    /**
     * Reads a byte array of a specified length from the stream.
     * @return The byte array.
     * @throws ParseException The end of the stream was reached.
     */
    @Override
    public byte [] readByteArray(int len) throws ParseException {
        require(len);
        byte [] out = new byte[len];
        byteBuffer.get(out, 0, len);
        return out;
    }

    /**
     * Reads a byte array consisting of the remainder of the stream (or the
     * active region, if one is set.
     * @return The byte array.
     */
    @Override
    public byte [] readByteArray() {
        int len = remaining();
        byte [] out = new byte[len];
        byteBuffer.get(out, 0, len);
        return out;
    }

    /**
     * Reads a counted string from the stream.  A counted string is a one byte
     * value indicating string length, followed by bytes of data.
     * @return A byte array containing the string.
     * @throws ParseException The end of the stream was reached.
     */
    @Override
    public byte [] readCountedString() throws ParseException {
        int len = readU8();
        return readByteArray(len);
    }

    public int getBase_pos() {
        return base_pos;
    }

    public void setBase_pos(int base_pos) {
        this.base_pos = base_pos;
    }
}
