package lib.basicFrm.rtmp;

import org.jboss.netty.buffer.ChannelBuffer;


public interface RTMPMessage
{
	RTMPHeader getHeader();
	
	ChannelBuffer encode();
	
	void decode(ChannelBuffer in);
}
