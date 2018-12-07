package lib.basicFrm.utils;

import java.util.Random;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class BufferUtils
{
	public static ChannelBuffer generateRandomHandshake( int $size )
	{
		byte[] randomBytes = new byte[$size];
		Random random = new Random();
		random.nextBytes(randomBytes);
		return ChannelBuffers.wrappedBuffer(randomBytes);
	}
	
	public static byte[] toInt24(final int value) {
		return new byte[] {(byte)(value >>> 16), (byte)(value >>> 8), (byte)value};
	}
	
	public static byte[] toInt1(final int value)
	{
		return new byte[] {(byte)value, (byte)(01)};
	}
	
	public static byte[] toInt0(final int value)
	{
		return new byte[] {(byte)value, (byte)(value >>> 8)};
	}
	
	public static byte toInt(final int value)
	{
		return (byte)value;
	}
	
	public BufferUtils()
	{
	}

}
