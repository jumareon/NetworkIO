package lib.basicFrm.io.mp3;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import lib.basicFrm.io.BufferReader;
import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.rtmp.RTMPMessage;
import lib.basicFrm.rtmp.message.MessageType;
import lib.basicFrm.utils.AnyLogger;

public class AtomMP3 implements RTMPMessage
{
protected AnyLogger log = AnyLogger.getInstance();
	
	private final RTMPHeader header;
	private ChannelBuffer data;
	
	public static ChannelBuffer tagID3v2()
	{
		// MP3 ID3 Tag (10Byte)
		final ChannelBuffer out = ChannelBuffers.buffer(10);
		
		// Byte 3
		out.writeByte((byte) 0x49); // I (Byte 0)
		out.writeByte((byte) 0x44); // D (Byte 1)
		out.writeByte((byte) 0x33); // 3 (Byte 2)
		
		out.writeByte((byte) 0x03); // major Version (Byte 3)
		
		out.writeByte((byte) 0x00); // reversion (Byte 4)
		
		out.writeByte((byte) 0x00); // Flags (Byte 5)
		
		out.writeByte((byte) 0x00); // Size (Byte 6)
		out.writeByte((byte) 0x00); // Size (Byte 7)
		out.writeByte((byte) 0x01); // Size (Byte 8)
		out.writeByte((byte) 0x77); // Size (Byte 9)
		
		return out;
	}
	
	public static ChannelBuffer mp3Header()
	{
		// MP3 Header ( 4Byte )
		final ChannelBuffer out = ChannelBuffers.buffer(4);
		
		// Byte 0 ~ 1 ( MP3 Frame Sync Word )
		out.writeByte((byte) 0xFF);
		
		// Version, Layer, Error
		out.writeByte((byte) 0xFB);
		
		// BitRate Index, Frequency, Padding Bit, Private
		out.writeByte((byte) 0xDE);
		
		// Channel, Stereo, Copyright, Original, Emphasis
		out.writeByte((byte) 0xEF);
		
		return out;
	}
	
	public AtomMP3( final ChannelBuffer in )
	{
		header = readHeader(in);
		data = in.readBytes(header.getSize());
		in.skipBytes(4); // prev offset
	}
	
	public AtomMP3( final BufferReader in )
	{
		header = readHeader( in.read(11) );
		data = in.read( header.getSize() );
		in.position( in.position() + 4 ); // prev offset
	}

	public AtomMP3(final MessageType messageType, final int time, final ChannelBuffer in)
	{
		header = new RTMPHeader(messageType, time, in.readableBytes());
		data = in;
	}
	
	public ChannelBuffer write()
	{
		final ChannelBuffer out = ChannelBuffers.buffer(15 + header.getSize());
		out.writeByte((byte) header.getMessageType().intValue());
		out.writeMedium(header.getSize());
		out.writeMedium(header.getTime());
		out.writeInt(0); // 4 bytes of zeros (reserved)
		out.writeBytes(data);
		out.writeInt(header.getSize() + 11); // previous tag size
		return out;
	}
	
	public static RTMPHeader readHeader(final ChannelBuffer in) {
		final MessageType messageType = MessageType.valueToEnum(in.readByte());
		final int size = in.readMedium();
		final int time = in.readMedium();
		in.skipBytes(4); // 0 - reserved
		return new RTMPHeader(messageType, time, size);
	}
	
	//============================ RtmpMessage =================================
	
	@Override
	public RTMPHeader getHeader() {
		return header;
	}
	
	public ChannelBuffer getData() {
		return data;
	}
	
	@Override
	public ChannelBuffer encode() {
		return data;
	}
	
	@Override
	public void decode(final ChannelBuffer in) {
		data = in;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append(" data: ").append(data);		
		return sb.toString();
	}
}
