package lib.basicFrm.io;

import lib.basicFrm.rtmp.RTMPMessage;

public interface RTMPWriter
{
	void write(RTMPMessage message);
	
	void close();
}
