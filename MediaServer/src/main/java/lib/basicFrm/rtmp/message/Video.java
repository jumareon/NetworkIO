package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.utils.BufferUtils;
import lib.basicFrm.utils.StringUtils;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class Video extends DataMessage
{
	static final byte FLV_FRAME_KEY = 0x10;
	
	private int codecId = 0;
	
	public String getCodec()
	{
		if( codecId == 1 )
			return "JPEG";
		else if( codecId == 2 )
			return "H263";
		else if( codecId == 4 )
			return "VP6";
		else if( codecId == 7 )
			return "AVC(H264)";
		else
			return "none";
	}
	
    @Override
    public boolean isConfig() {
    	// Video Data가 Empty 값이 아니며 FLV_Frame_Key 값과 H264 Codec의 데이터가 들어올때 (0x17) 이후데이터에 000000 이 되면 해당 패킷은 설정값이다.
        return data.readableBytes() > 3 && data.getInt(0) == 0x17000000;
    }

    public Video(final RTMPHeader header, final ChannelBuffer in) {
        super(header, in);
        
        codecId = (int) in.getByte(0) & 0x0F;
        
//        System.out.println( "Video Codec : " +
//        					", " + StringUtils.toHex( in.getByte(0) ) +
//        					", " + StringUtils.toHex( in.getByte(1) ) +
//        					", " + StringUtils.toHex( in.getByte(2) ) +
//        					", " + StringUtils.toHex( in.getByte(3) ) +
//        					", " + StringUtils.toHex( in.getByte(4) ) +
//        					", " + StringUtils.toHex( in.getByte(5) ) +
//        					", " + StringUtils.toHex( in.getByte(6) ) +
//        					", " + StringUtils.toHex( in.getByte(7) ) +
//        					", " + StringUtils.toHex( in.getByte(8) ) +
//        					", " + StringUtils.toHex( in.getByte(9) ) +
//        					", " + StringUtils.toHex( in.getByte(10) ) +
//        					", " + StringUtils.toHex( in.getByte(11) ) +
//        					", " + StringUtils.toHex( in.getByte(13) ) +
//        					", " + StringUtils.toHex( in.getByte(14) ) +
//        					", " + StringUtils.toHex( in.getByte(15) ) +
//        					", " + StringUtils.toHex( in.getByte(16) ) +
//        					", " + StringUtils.toHex( in.getByte(17) ) +
//        					", " + StringUtils.toHex( in.getByte(18) ) +
//        					", " + StringUtils.toHex( in.getByte(19) ) +
//        					", " + StringUtils.toHex( in.getByte(20) ) +
//        					", " + StringUtils.toHex( in.getByte(21) ) +
//        					", " + StringUtils.toHex( in.getByte(22) ));
    }

    public Video(final byte[] ... bytes) {
        super(bytes);
    }

    public Video(final int time, final byte[] prefix, final int compositionOffset, final byte[] videoData) {
        header.setTime(time);
        data = ChannelBuffers.wrappedBuffer(prefix, BufferUtils.toInt24(compositionOffset), videoData);
        header.setSize(data.readableBytes());
    }

    public Video(final int time, final ChannelBuffer in) {
        super(time, in);
    }

    public static Video empty() {
        Video empty = new Video();
        
        empty.data = ChannelBuffers.wrappedBuffer( new byte[2] );
        
        return empty;
    }
    
    public static Video emptyTest()
    {
    	Video empty = new Video();
    	
    	empty.data = ChannelBuffers.wrappedBuffer( BufferUtils.toInt0(87) );
    	
    	return empty;
    }
    
    public static Video emptyTest1()
    {
    	Video empty = new Video();
    	
    	empty.data = ChannelBuffers.wrappedBuffer( BufferUtils.toInt1(87) );
    	
    	return empty;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.VIDEO;
    }
}
