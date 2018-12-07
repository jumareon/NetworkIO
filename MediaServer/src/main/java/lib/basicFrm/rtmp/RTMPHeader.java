package lib.basicFrm.rtmp;

import org.jboss.netty.buffer.ChannelBuffer;

import lib.basicFrm.rtmp.message.MessageType;
import lib.basicFrm.utils.*;

public class RTMPHeader
{
	public static enum Type implements ValueToEnum.IntValue
	{
		VALUE0(0), VALUE1(1), VALUE2(2), VALUE3(3);
		
		private final int value;
		
		private Type(int value)
		{
			this.value = value;
		}
		
		@Override
		public int intValue()
		{
			return value;
		}
		
		private static final ValueToEnum<Type> converter = new ValueToEnum<Type>(Type.values());
		
		public static Type valueToEnum(final int value)
		{
			return converter.valueToEnum(value);
		}
	}
	
	public static final int MAX_CHANNEL_ID = 65600;
	public static final int MAX_NORMAL_HEADER_TIME = 0xFFFFFF;
	public static final int MAX_ENCODED_SIZE = 18;
	
	private Type headerType;
	private int channelId;
	private int deltaTime;
	private int time;
	private int size;
	private MessageType messageType;
	private int streamId;
	
	public RTMPHeader( ChannelBuffer in, RTMPHeader[] incompleteHeaders )
	{
		//=================== TYPE AND CHANNEL (1 - 3 bytes) ===================
		final int firstByteInt = in.readByte();
		final int typeAndChannel;
		final int headerTypeInt;
		if( (firstByteInt & 0x3f) == 0 )
		{
			typeAndChannel = (firstByteInt & 0xff) << 8 | (in.readByte() & 0xff);
			channelId = 64 + (typeAndChannel & 0xff);
			headerTypeInt = typeAndChannel >> 14;
		}
		else if( (firstByteInt & 0x3f) == 1 )
		{
			typeAndChannel = (firstByteInt & 0xff) << 16 | (in.readByte() & 0xff) << 8 | (in.readByte() & 0xff);
			channelId = 64 + ((typeAndChannel >> 8) & 0xff) + ((typeAndChannel & 0xff) << 8);
			headerTypeInt = typeAndChannel >> 22;
		}
		else
		{
			typeAndChannel = firstByteInt & 0xff;
			channelId = (typeAndChannel & 0x3f);
			headerTypeInt = typeAndChannel >> 6;
		}
		
		headerType = Type.valueToEnum(headerTypeInt);
		
		//========================= REMAINING HEADER ===========================
		final RTMPHeader prevHeader = incompleteHeaders[channelId];
		switch(headerType)
		{
			case VALUE0:
				time = in.readMedium();
				size = in.readMedium();
				messageType = MessageType.valueToEnum(in.readByte());
				streamId = IntegerUtils.readInt32Reverse(in);
				if(time == MAX_NORMAL_HEADER_TIME)
				{
					time = in.readInt();
				}
				break;
			case VALUE1:
				deltaTime = in.readMedium();
				size = in.readMedium();
				messageType = MessageType.valueToEnum(in.readByte());
				streamId = prevHeader.streamId;
				if(deltaTime == MAX_NORMAL_HEADER_TIME)
				{
					deltaTime = in.readInt();
				}
				break;
			case VALUE2:
				deltaTime = in.readMedium();
				size = prevHeader.size;
				messageType = prevHeader.messageType;
				streamId = prevHeader.streamId;
				if(deltaTime == MAX_NORMAL_HEADER_TIME)
				{
					deltaTime = in.readInt();
				}
				break;
			case VALUE3:
				headerType = prevHeader.headerType;
				time = prevHeader.time;
				deltaTime = prevHeader.deltaTime;
				size = prevHeader.size;
				messageType = prevHeader.messageType;
				streamId = prevHeader.streamId;
				break;
		}
    }
	
	public RTMPHeader( MessageType messageType, int time, int size )
	{
		this(messageType);
		this.time = time;
		this.size = size;
	}
	
	public RTMPHeader( MessageType messageType )
	{
		this.messageType = messageType;
		headerType = Type.VALUE0;
		channelId = messageType.getDefaultChannelId();
	}
	
	public boolean isMedia()
	{
		switch( messageType )
		{
			case AUDIO:
			case VIDEO:
			case AGGREGATE:
				return true;
			default:
				return false;
		}
	}
	
	public boolean isMetadata()
	{
		return messageType == MessageType.METADATA_AMF0 || messageType == MessageType.METADATA_AMF3;
	}
	
	public boolean isAggregate()
	{
		return messageType == MessageType.AGGREGATE;
	}
	
	public boolean isAudio()
	{
		return messageType == MessageType.AUDIO;
	}
	
	public boolean isVideo()
	{
		return messageType == MessageType.VIDEO;
	}
	
	public boolean isValue0()
	{
		return headerType == Type.VALUE0;
	}
	
	public boolean isControl()
	{
		return messageType == MessageType.CONTROL;
	}
	
	public boolean isChunkSize()
	{
		return messageType == MessageType.CHUNK_SIZE;
	}
	
	public Type getHeaderType()
	{
		return headerType;
	}
	
	public void setHeaderType(Type headerType)
	{
		this.headerType = headerType;
	}
	
	public int getChannelId()
	{
		return channelId;
	}
	
	public void setChannelId(int channelId)
	{
		this.channelId = channelId;
	}
	
	public int getTime()
	{
		return time;
	}
	
	public void setTime(int time)
	{
		this.time = time;
	}
	
	public int getDeltaTime()
	{
		return deltaTime;
	}
	
	public void setDeltaTime(int deltaTime)
	{
		this.deltaTime = deltaTime;
	}
	
	public void setSize(int size)
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return this.size;
	}
	
	public MessageType getMessageType()
	{
		return messageType;
	}
	
	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;
	}
	
	public int getStreamId()
	{
		return streamId;
	}
	
	public void setStreamId(int streamId)
	{
		this.streamId = streamId;
	}
	
	public void encode(ChannelBuffer out)
	{
		out.writeBytes(encodeHeaderTypeAndChannel(headerType.value, channelId));
		
		if(headerType == Type.VALUE3)
		{
			return;
		}     
		
		final boolean extendedTime;
		
		if(headerType == Type.VALUE0)
		{
			extendedTime = time >= MAX_NORMAL_HEADER_TIME;             
		}
		else
		{
			extendedTime = deltaTime >= MAX_NORMAL_HEADER_TIME;
		}
		
		if(extendedTime)
		{
			out.writeMedium(MAX_NORMAL_HEADER_TIME); 
		}
		else
		{
			out.writeMedium(headerType == Type.VALUE0 ? time : deltaTime);
		}
		
		if(headerType != Type.VALUE2)
		{
			out.writeMedium(size);
			out.writeByte((byte) messageType.intValue());
			if(headerType == Type.VALUE0)
			{
				IntegerUtils.writeInt32Reverse(out, streamId);
			}
		}
		
		if(extendedTime)
		{
			out.writeInt(headerType == Type.VALUE0 ? time : deltaTime);
		}
	}

	public byte[] getTinyHeader() {
		return encodeHeaderTypeAndChannel(Type.VALUE3.intValue(), channelId);
	}

	private static byte[] encodeHeaderTypeAndChannel(final int headerType, final int channelId) {
		if (channelId <= 63) {
			return new byte[] {(byte) ((headerType << 6) + channelId)};
		} else if (channelId <= 320) {
			return new byte[] {(byte) (headerType << 6), (byte) (channelId - 64)};
		} else {            
			return new byte[] {(byte) ((headerType << 6) | 1), (byte) ((channelId - 64) & 0xff), (byte) ((channelId - 64) >> 8)};
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(headerType.ordinal());
		sb.append(' ').append(messageType);
		sb.append(" c").append(channelId);
		sb.append(" #").append(streamId);
		sb.append(" t").append(time);
		sb.append(" (").append(deltaTime);
		sb.append(") s").append(size);
		sb.append(']');
		return sb.toString();
	}
	
}
