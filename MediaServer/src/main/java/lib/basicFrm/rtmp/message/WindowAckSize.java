package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class WindowAckSize extends AbstractMessage {
    
    private int value;
    
    public WindowAckSize(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }
    
    public WindowAckSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.WINDOW_ACK_SIZE;
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
