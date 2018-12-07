package lib.middleFrm.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.netty.channel.Channel;

import lib.basicFrm.rtmp.RTMPMessage;
import lib.basicFrm.utils.AnyLogger;

public class ApplicationStream
{
	private final List<RTMPMessage> userMessages = new ArrayList<RTMPMessage>();
	
	private HashMap<String, Publisher> publisher;
	
	private AnyLogger log = AnyLogger.getInstance();
	
	public ApplicationStream( final String appName )
	{
		publisher = new HashMap<String, Publisher>();
		
		log.info( "CreationComplete ApplicationName : " + appName );
	}
	
	public void setPublisher( String publishName, String publishType, Channel channel )
	{
		Publisher publisher;
		
		if( this.publisher.get( publishName ) == null )
		{
			publisher = new Publisher( publishType );
			publisher.setPublisher( publishName, channel );
			this.publisher.put( publishName, publisher );
		}
		else
		{
			publisher = this.publisher.get( publishName );
			publisher.setPublishType( publishType );
			publisher.setPublisher( publishName, channel );
		}
	}
	
	public Publisher getPublisher( String name )
	{
		Publisher publisher = this.publisher.get( name );
		
		if( publisher == null )
		{
			publisher = new Publisher( null );
			this.publisher.put( name, publisher );
		}
		
		return publisher;
	}
	
	public void remove( String name )
	{
		publisher.remove( name );
	}
	
	public HashMap<String, Publisher> getMap()
	{
		return publisher;
	}
	
	public List<RTMPMessage> getUserMessages()
	{
		return userMessages;
	}
	
	public void addUserMessage( final RTMPMessage message )
	{
		userMessages.add( message );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("[publisher: ").append(publisher);
		sb.append(']');
		return sb.toString();
	}
}
