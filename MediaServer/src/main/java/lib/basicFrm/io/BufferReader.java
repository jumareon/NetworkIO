package lib.basicFrm.io;

import org.jboss.netty.buffer.ChannelBuffer;

public interface BufferReader {

    long size();

    long position();

    void position(long position);

    ChannelBuffer read(int size);

    byte[] readBytes(int size);

    int readInt();

    long readUnsignedInt();

    void close();

}