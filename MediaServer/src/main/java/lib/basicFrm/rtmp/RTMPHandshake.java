package lib.basicFrm.rtmp;

import lib.basicFrm.utils.AnyLogger;
import lib.basicFrm.utils.BufferUtils;
import lib.basicFrm.utils.StringUtils;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class RTMPHandshake
{
	public static final int HANDSHAKE_SIZE = 1536;
	
	private AnyLogger log = AnyLogger.getInstance();
	
	
	private byte[] peerTime;
	private byte[] peerVersion;
	
	private ChannelBuffer peerPartOne;
	
	private byte[] serverVersionToUse = new byte[]{0x03, 0x05, 0x01, 0x01};
	
	public RTMPHandshake()
	{
		
	}
	
	public ChannelBuffer encodeServer0()
	{
		ChannelBuffer out = ChannelBuffers.buffer(1);
		out.writeByte( (byte) 0x03 );
		
		return out;
	}
	public ChannelBuffer encodeServer1()
	{
		ChannelBuffer out = BufferUtils.generateRandomHandshake( HANDSHAKE_SIZE );
		
		out.setInt( 0, 0 );
		//out.setBytes( 0, peerTime );
		out.setBytes( 4, serverVersionToUse );
		
		return out;
	}
	public ChannelBuffer encodeServer2()
	{
		peerPartOne.setBytes( 0, peerTime );
		peerPartOne.setInt( 4, 0 );
		return peerPartOne;
	}
	
	public void decodeClient0( ChannelBuffer in )
	{
		final byte firstByte = in.readByte();
		if( firstByte == 0x03 )
		{
			log.info( "Use to RTMP Protocal"  );
		}
		else
		{
			log.error( "잘못된 프로토콜로 호출됨 RTMP(0x03) 만 허용 : " + StringUtils.toHex(firstByte) );
		}
	}
	public boolean decodeClient1( ChannelBuffer in )
	{
		peerTime = new byte[4];
		in.getBytes(0, peerTime);
		
		peerVersion = new byte[4];
		in.getBytes(4, peerVersion);
		
		log.trace("client time : " + StringUtils.toHex(peerTime) );
		log.trace("client version : " + StringUtils.toHex(peerVersion) + " but always default 0" );
		
		peerPartOne = in;
		return true;
	}
	public void decodeClient2( ChannelBuffer in )
	{
		in.readBytes(HANDSHAKE_SIZE);
	}
}
