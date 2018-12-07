package lib.basicFrm.rtmp.message;

import java.util.LinkedHashMap;
import java.util.Map;

import lib.basicFrm.amf.Amf0Object;
import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.rtmp.RTMPMessage;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class AbstractMessage implements RTMPMessage
{
	protected final RTMPHeader header;
	
	public AbstractMessage()
	{
		header = new RTMPHeader(getMessageType());
	}
	
	public AbstractMessage( RTMPHeader header, ChannelBuffer in )
	{
		this.header = header;
		decode(in);
	}
	
	@Override
	public RTMPHeader getHeader()
	{
		return header;
	}
	
	abstract MessageType getMessageType();
	
	@Override
	public String toString()
	{
		return header.toString() + ' ';
	}
	
	public static Amf0Object object(Amf0Object object, Pair ... pairs)
	{
		if(pairs != null)
		{
			for(Pair pair : pairs)
			{
				object.put(pair.name, pair.value);
			}
		}
		return object;
	}
	
	public static Amf0Object object(Pair ... pairs)
	{
		return object(new Amf0Object(), pairs);
	}
	
	public static Map<String, Object> map(Map<String, Object> map, Pair ... pairs)
	{
		if(pairs != null)
		{
			for(Pair pair : pairs)
			{
				map.put(pair.name, pair.value);
			}
		}
		
		return map;
	}
	
	public static Map<String, Object> map(Pair ... pairs)
	{
		return map(new LinkedHashMap<String, Object>(), pairs);
	}
	
	public static class Pair
	{
		String name;
		Object value;
	}
	
	public static Pair pair(String name, Object value)
	{
		Pair pair = new Pair();
		pair.name = name;
		pair.value = value;
		
		return pair;
	}
	
}
