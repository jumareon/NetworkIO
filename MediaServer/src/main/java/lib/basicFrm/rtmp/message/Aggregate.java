package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;

public class Aggregate extends DataMessage {

    public Aggregate(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public Aggregate(int time, ChannelBuffer in) {
        super();
        header.setTime(time);
        data = in;
        header.setSize(data.readableBytes());
    }

    @Override
    MessageType getMessageType() {
        return MessageType.AGGREGATE;
    }

    @Override
    public boolean isConfig() {
        return false;
    }

}
