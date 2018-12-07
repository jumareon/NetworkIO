package lib.middleFrm.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import lib.basicFrm.rtmp.RTMPMessage;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class Publisher extends DefaultChannelGroup
{
	private final List<RTMPMessage> configMessages = new ArrayList<RTMPMessage>();
	private HashMap<Channel, Integer> streamList = new HashMap<Channel, Integer>();
	private PublishType publishType;
	private Channel publisher;
	private String publishName;
	private int streamId = 0;
	
	public Channel getPublisher()
	{
		return this.publisher;
	}
	public void setPublisher( final String publishName, final Channel channel )
	{
		this.publishName = publishName;
		this.publisher = channel;
		
		configMessages.clear();
	}
	
	public String getPublishName()
	{
		return this.publishName;
	}
	
	public Publisher( final String typeString )
	{
		super();
		
		if( typeString != null )
		{
			this.publishType = PublishType.parse(typeString);
		}
		else
		{
			this.publishType = null;
		}
	}
	
	public boolean isLive()
	{
		return publishType != null && publishType == PublishType.LIVE;
	}
	
	public PublishType getPublishType()
	{
		return publishType;
	}
	
	public void setPublishType( String typeString )
	{
		this.publishType = PublishType.parse(typeString);
	}
	
	public List<RTMPMessage> getConfigMessages()
	{
		return configMessages;
	}
	
	public void addConfigMessage( final RTMPMessage message )
	{
		configMessages.add( message );
	}
	
	public void setStreamId( int id )
	{
		this.streamId = id;
	}
	
	public int getStreamId()
	{
		return this.streamId;
	}
	
	public void reset()
	{
		this.publisher = null;
	}
	
	public boolean add( Channel channel, int streamId )
	{
		streamList.put( channel, streamId );
		
		return super.add(channel);
	}
	
	@Override
	public void clear()
	{
		streamList.clear();
		
		super.clear();
	}
	
	@Override
	public boolean remove(Object o)
	{
		streamList.remove( o );
		
		return super.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		streamList.clear();
		
		return super.removeAll(c);
	}
	
	public boolean writeStreamChannel( final RTMPMessage message )
	{
		if( this.size() <= 0 )
		{
			return false;
		}
		
		Iterator<Channel> list = this.iterator();
		
		while( list.hasNext() )
		{
			Channel channel = list.next();
			int streamId;
			
			if( streamList.get(channel) != null )
			{
				streamId = streamList.get(channel);
				
				message.getHeader().setStreamId(streamId);
			}
			
			channel.write( message );
		}
		
		return true;
	}
	
}
