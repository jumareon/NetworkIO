package lib.basicFrm.io.flv;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import lib.basicFrm.io.BufferReader;
import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.rtmp.RTMPMessage;
import lib.basicFrm.rtmp.message.MessageType;
import lib.basicFrm.utils.AnyLogger;

public class AtomFLV implements RTMPMessage
{
	protected AnyLogger log = AnyLogger.getInstance();
	
	private final RTMPHeader header;
	private ChannelBuffer data;
	
	public static ChannelBuffer flvHeader()
	{
		final ChannelBuffer out = ChannelBuffers.buffer(13);
		out.writeByte((byte) 0x46); // F
		out.writeByte((byte) 0x4C); // L
		out.writeByte((byte) 0x56); // V
		out.writeByte((byte) 0x01); // version
		out.writeByte((byte) 0x04); // flags: 00000101( 0x05: Audio+Video ), 00000100( 0x04: Audio ), 00000001( 0x01: Video )
		out.writeInt(0x09); // header size = 9
		out.writeInt(0); // previous tag size, here = 0
		return out;
	}
	
	public AtomFLV( final ChannelBuffer in )
	{
		header = readHeader(in);
		data = in.readBytes(header.getSize());
		in.skipBytes(4); // prev offset
	}
	
	public AtomFLV( final BufferReader in )
	{
		header = readHeader( in.read(11) );
		data = in.read( header.getSize() );
		in.position( in.position() + 4 ); // prev offset
	}

	public AtomFLV(final MessageType messageType, final int time, final ChannelBuffer in)
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

	public static RTMPHeader readHeader(final ChannelBuffer in)
	{
		final MessageType messageType = MessageType.valueToEnum(in.readByte());
		final int size = in.readMedium();
		final int time = in.readMedium();
		in.skipBytes(4); // 0 - reserved
		return new RTMPHeader(messageType, time, size);
	}

	//============================ RtmpMessage =================================

	@Override
	public RTMPHeader getHeader()
	{
		return header;
	}

	public ChannelBuffer getData()
	{
		return data;
	}

	@Override
	public ChannelBuffer encode()
	{
		return data;
	}

	@Override
	public void decode(final ChannelBuffer in)
	{
		data = in;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append(" data: ").append(data);
		return sb.toString();
	}
}
