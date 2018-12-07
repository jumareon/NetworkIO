package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.utils.StringUtils;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class Audio extends DataMessage
{
	@Override
    public boolean isConfig() { // TODO now hard coded for mp4a
        return data.readableBytes() > 3 && data.getInt(0) == 0xaf001310;
    }

    public Audio(final RTMPHeader header, final ChannelBuffer in) {
        super(header, in);
        
//        System.out.println( "Audio Codec : " +
//				", " + StringUtils.toHex( in.getByte(0) ) +
//				", " + StringUtils.toHex( in.getByte(1) ) +
//				", " + StringUtils.toHex( in.getByte(2) ) +
//				", " + StringUtils.toHex( in.getByte(3) ) +
//				", " + StringUtils.toHex( in.getByte(4) ) +
//				", " + StringUtils.toHex( in.getByte(5) ) +
//				", " + StringUtils.toHex( in.getByte(6) ) +
//				", " + StringUtils.toHex( in.getByte(7) ) +
//				", " + StringUtils.toHex( in.getByte(8) ) +
//				", " + StringUtils.toHex( in.getByte(9) ) +
//				", " + StringUtils.toHex( in.getByte(10) ) +
//				", " + StringUtils.toHex( in.getByte(11) ) +
//				", " + StringUtils.toHex( in.getByte(12) ));
    }

    public Audio(final byte[] ... bytes) {
        super(bytes);
    }

    public Audio(final int time, final byte[] prefix, final byte[] audioData) {
        header.setTime(time);
        data = ChannelBuffers.wrappedBuffer(prefix, audioData);
        header.setSize(data.readableBytes());
    }

    public Audio(final int time, final ChannelBuffer in) {
        super(time, in);
    }
    
    public static Audio empty() {
        Audio empty = new Audio();
        empty.data = ChannelBuffers.EMPTY_BUFFER;
        return empty;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.AUDIO;
    }
}
