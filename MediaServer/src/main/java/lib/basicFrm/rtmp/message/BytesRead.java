package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class BytesRead extends AbstractMessage {

    private int value;

    @Override
    MessageType getMessageType() {
        return MessageType.BYTES_READ;
    }

    public BytesRead(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public BytesRead(long bytesRead) {        
        this.value = (int) bytesRead;
    }

    public int getValue() {
        return value;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.buffer(4);
        out.writeInt(value);
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        value = in.readInt();
    }

    @Override
    public String toString() {
        return super.toString() + value;
    }

}
