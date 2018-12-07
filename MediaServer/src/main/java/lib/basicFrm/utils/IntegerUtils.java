package lib.basicFrm.utils;

import org.jboss.netty.buffer.ChannelBuffer;

public class IntegerUtils
{
	public static int readInt32Reverse( final ChannelBuffer in )
	{
		final byte a = in.readByte();
		final byte b = in.readByte();
		final byte c = in.readByte();
		final byte d = in.readByte();
		int val = 0;
		val += d << 24;
		val += c << 16;
		val += b << 8;
		val += a;
		return val;
	}
	
	public static void writeInt32Reverse( final ChannelBuffer out, final int value )
	{
		out.writeByte((byte) (0xFF & value));
		out.writeByte((byte) (0xFF & (value >> 8)));
		out.writeByte((byte) (0xFF & (value >> 16)));
		out.writeByte((byte) (0xFF & (value >> 24)));
	}
}
