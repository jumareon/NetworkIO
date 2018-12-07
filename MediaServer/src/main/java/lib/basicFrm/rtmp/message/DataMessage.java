package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public abstract class DataMessage extends AbstractMessage
{
	private boolean encoded;
    protected ChannelBuffer data;

    public DataMessage() {
        super();
    }

    public DataMessage(final byte[] ... bytes) {
        data = ChannelBuffers.wrappedBuffer(bytes);
        header.setSize(data.readableBytes());
    }

    public DataMessage(final RTMPHeader header, final ChannelBuffer in) {
        super(header, in);
    }

    public DataMessage(final int time, final ChannelBuffer in) {        
        header.setTime(time);
        header.setSize(in.readableBytes());
        data = in;
    }

    @Override
    public ChannelBuffer encode() {
        if(encoded) {
            // in case used multiple times e.g. broadcast
            data.resetReaderIndex();            
        } else {
            encoded = true;
        }
        return data;
    }

    @Override
    public void decode(ChannelBuffer in) {
        data = in;
    }

    @Override
    public String toString() {
        return super.toString() + ChannelBuffers.hexDump(data);
    }

    public abstract boolean isConfig(); // TODO abstraction for audio / video ?
}
