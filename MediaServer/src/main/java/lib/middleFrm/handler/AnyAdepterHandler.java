package lib.middleFrm.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lib.basicFrm.io.RTMPWriter;
import lib.basicFrm.io.RecorderFLV;
import lib.basicFrm.io.mp3.RecorderMP3;
import lib.basicFrm.rtmp.RTMPMessage;
import lib.basicFrm.rtmp.RTMPPublisher;
import lib.basicFrm.rtmp.message.Audio;
import lib.basicFrm.rtmp.message.BytesRead;
import lib.basicFrm.rtmp.message.ChunkSize;
import lib.basicFrm.rtmp.message.Command;
import lib.basicFrm.rtmp.message.Control;
import lib.basicFrm.rtmp.message.DataMessage;
import lib.basicFrm.rtmp.message.MessageType;
import lib.basicFrm.rtmp.message.Metadata;
import lib.basicFrm.rtmp.message.SetPeerBw;
import lib.basicFrm.rtmp.message.Video;
import lib.basicFrm.rtmp.message.WindowAckSize;
import lib.basicFrm.utils.AnyLogger;
import lib.middleFrm.core.ApplicationAdepter;
import lib.middleFrm.stream.ApplicationStream;
import lib.middleFrm.stream.PublishType;
import lib.middleFrm.stream.Publisher;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;

public class AnyAdepterHandler  extends SimpleChannelHandler
{
	protected static Map<String, Channel> userList = new HashMap<String, Channel>();
	
	protected AnyLogger log = AnyLogger.getInstance();
	protected Channel channel;
	protected ApplicationAdepter app = ApplicationAdepter.getInstance();
	protected ApplicationStream subscriberStream;
	protected Publisher stream;
	protected String userName;
	
	private int timerTickSize = 100;
	
	private int bytesReadWindow = 2500000;
	private long bytesRead;
	private long bytesReadLastSent;
	
	private long bytesWritten;
	private int bytesWrittenWindow = 2500000;
	private int bytesWrittenLastReceived;
	
	private String clientId;
	private String playName;
	private int streamId = 0;
	private int bufferDuration;
	
	private Method[] methods;
	
	private static RTMPWriter recorder;
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	{
		RTMPMessage message;
		
		try
		{
			channel = e.getChannel();
			message = (RTMPMessage) e.getMessage();
			
			bytesRead += message.getHeader().getSize();
			
			if( (bytesRead - bytesReadLastSent) > bytesReadWindow )
			{
				BytesRead ack = new BytesRead(bytesRead);
				channel.write(ack);
				bytesReadLastSent = bytesRead;
			}
			
			switch( message.getHeader().getMessageType() )
			{
				case CHUNK_SIZE:
					log.info( "ChunkSize [" + message + "]" );
					break;
				case COMMAND_AMF0:
				case COMMAND_AMF3:
					final Command command = (Command) message;
					final String name = command.getName();
					
					log.info( "Command Name [" + name + "]" );
					
					if( name.equals("connect") )
					{
						connectResponse(channel, command);
					}
					else if( name.equals("createStream") )
					{
						streamId = streamId + 1;
						
						channel.write( Command.createStreamSuccess(command.getTransactionId(), streamId) );
					}
					else if( name.equals("play") )
					{
						playResponse(channel, command);
					}
					else if( name.equals("deleteStream") )
					{
						streamId = streamId - 1;
						
						if( stream != null )
						{
							if( stream.getPublisher() == channel )
							{
								// 종료되었을때 기존 subscriber 는 대기 상태로 변경해줘야 한다.
								stream.writeStreamChannel( Command.pauseNotify( playName, clientId ) );
								
								stream.reset();
							}
							else
							{
								stream.remove( channel );
							}
							
							log.info( "deleteStream Size : " + stream.size() + ", stream publisher : " + stream.getPublisher() );
							if( stream.size() == 0 && stream.getPublisher() == null)
							{
								stream.close();
								subscriberStream.remove( stream.getPublishName() );
								
								if( recorder != null )
								{
									recorder.close();
									recorder = null;
								}
							}
							
							log.debug( "deleteStream : " + stream.getPublishName() );
						}
					}
					else if( name.equals("closeStream") )
					{
						log.info( "CloseStream User : " + stream.getPublishName() + ", " + command.getHeader().getStreamId() );
						
					}
					else if( name.equals("publish") )
					{
						publishResponse(channel, command);
					}
					else if( name.equals("seek") )
					{
						seekResponse(channel, command);
					}
					else if( name.equals( "record" ) )
					{
						log.info( "Record Data : " + command.getArg(0) );
						
						if( (Boolean) command.getArg(0) )
						{
							recorder = RecorderFLV.recordAudio();
						}
						else
						{
							recorder.close();
							recorder = null;
						}
					}
					else
					{
						log.info( "command : " + command );
						invoke( name, command.getArgs() );
					}
					break;
				case METADATA_AMF0:
				case METADATA_AMF3:
					final Metadata metadata = (Metadata) message;
					if( metadata.getName().equals( "onMetaData" ) )
					{
						metadata.setDuration( -1 );
						stream.addConfigMessage( metadata );
					}
					
					broadcast( metadata );
					break;
				case AUDIO:
					if( recorder != null )
					{
						recorder.write(message);
					}
				case VIDEO:
					if(((DataMessage) message).isConfig())
					{
						if( stream != null )
						{
							stream.addConfigMessage(message);
						}
					}
				case AGGREGATE:
					broadcast(message);
					break;
				case BYTES_READ:
					break;
				case WINDOW_ACK_SIZE:
					WindowAckSize was = (WindowAckSize) message;
					if( was.getValue() != bytesReadWindow )
					{
						channel.write(SetPeerBw.dynamic(bytesReadWindow));
					}
					break;
				case SET_PEER_BW:
					SetPeerBw bw = (SetPeerBw) message;
					if( bw.getValue() != bytesWrittenWindow )
					{
						channel.write( new WindowAckSize(bytesWrittenWindow) );
					}
					break;
				default:
					break;
			}
		}
		catch( Exception ect )
		{
			log.error( "MessageReceived : " + ect );
		}
		
		//fireNext(channel);
	}
	
//	private void fireNext( final Channel channel )
//	{
//		if( stream.getPublisher() != null )
//		{
//			fireNext( channel, 0 );
//		}
//	}
//	
//	private void fireNext( final Channel channel, final long delay )
//	{
//		if(delay > timerTickSize)
//		{
//			
//		}
//		else
//		{
//			Channels.fireMessageReceived(channel, new RTMPPublisher.Event(currentConversationId));
//		}
//	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	{
		log.error( "원격 클라이언트가 비정상 종료되었습니다. : " + e );
		
		Channel ch = e.getChannel();
		ChannelFuture future = ch.getCloseFuture();
		future.addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete( ChannelFuture future ) throws Exception
			{
				if( future.isSuccess() )
				{
					log.info( "사용자 접속 종료" );
				}
			}
		});
		ch.close();
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
	{
		System.out.println( "===============================================================================================================" );
		log.info( "Connected!" );
		log.info( "User Size : " + userList.size() );
		log.info( "User List : " + userList.keySet().toString() );
		log.info( "streamId : " + streamId );
		System.out.println( "===============================================================================================================" );
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
	{
		userList.remove( userName );
		
		this.callOtherUser( "exitUser", userName );
		
		userName = null;
		channel = null;
		
		System.out.println( "===============================================================================================================" );
		log.info( "Disconnected!" );
		log.info( "User Size : " + userList.size() );
		log.info( "User List : " + userList.keySet().toString() );
		log.info( "publisherMap : " + subscriberStream.getMap() );
		log.info( "subscriberStream : " + subscriberStream );
		System.out.println( "===============================================================================================================" );
	}
	
	private boolean checkLoginUser()
	{
		if( userList.get( userName ) == null )
		{
			return true;
		}
		
		return false;
	}
	
	private void connectResponse( final Channel channel, final Command connect )
	{
		final String appName = (String) connect.getObject().get("app");
		
		if( connect.getArgCount() > 0 )
		{
			userName = (String) connect.getArg(0);
		}
		else
		{
			userName = "joey";
		}
		
		clientId = channel.getId() + "";
		subscriberStream = app.getApplication(appName);
		
		if( checkLoginUser() )
		{
			channel.write( new WindowAckSize(bytesWrittenWindow) );
			channel.write( SetPeerBw.dynamic(bytesReadWindow) );
			channel.write( Control.streamBegin(streamId) );
			channel.write( new ChunkSize( 4096 ) );
			channel.write( Command.connectSuccess( connect.getTransactionId() ) );
			channel.write( Command.onBWDone() );
			
			// 접속 유저 ID 정보를 전송
			//this.callAllUser( "joinUser", userName );
			//this.callOtherUser( "joinUser", userName );
			//channel.write( Command.onCall( "connectUserList", userList.keySet().toArray() ) );
			
			userList.put( userName, channel );
			
			this.callAllUser( "connectUserList", userList.keySet().toArray() );
		}
		else
		{
			channel.write( Command.connectFault( userName ) );
		}
	}
	
	private void playResponse( final Channel channel, final Command play )
	{
		final String clientPlayName = (String) play.getArg(0);
		final Publisher stream = subscriberStream.getPublisher( clientPlayName );
		
		int streamId = play.getHeader().getStreamId();
		
		playName = clientPlayName;
		
		channel.write( Control.streamBegin(streamId) );
		
		if( stream.getPublisher() == null )
		{
			channel.write( Command.streamNotFound(streamId) );
		}
		else
		{
			channel.write( Command.playReset(clientPlayName, clientPlayName, streamId) );
			
			channel.write( Command.playStart(clientPlayName, clientPlayName, streamId) );
			
			channel.write( Metadata.rtmpSampleAccess() );
			
			channel.write( Audio.empty() );
			
			boolean videoConfigPresent = false;
			for( final RTMPMessage message : stream.getConfigMessages() )
			{
				if( message.getHeader().isVideo() )
				{
					videoConfigPresent = true;
				}
				
				writeToStream( channel, message, streamId );
			}
			
			if(!videoConfigPresent)
			{
				writeToStream( channel, Video.emptyTest(), streamId );
				writeToStream( channel, Video.emptyTest1(), streamId );
			}
			
			//channel.write( Command.onMetaDataTest( streamId ) );
		}
		
		stream.add( channel, streamId );
		
		log.info( "test : " + stream + ", name : " + clientPlayName );
	}
	
	private void publishResponse( final Channel channel, final Command command )
	{
		if( command.getArgCount() > 1 )
		{
			final String publishTypeString = (String) command.getArg(1);
			String streamName = (String) command.getArg(0);
			
			int streamId = command.getHeader().getStreamId();
			
			if( streamName.indexOf("?") > -1 )
			{
				streamName = streamName.substring(0, streamName.indexOf("?"));
			}
			
			log.info( "publish, stream name : " + streamName + ", type : " + publishTypeString );
			
			this.subscriberStream.setPublisher( streamName, publishTypeString, channel );
			stream = this.subscriberStream.getPublisher( streamName );
			
			if( stream == null )
			{
				log.info( "disconnecting publisher client, stream already in use" );
				ChannelFuture future = channel.write( Command.publishBadName( streamId ) );
				future.addListener( ChannelFutureListener.CLOSE );
				return;
			}
			
			channel.write(Command.publishStart(streamName, clientId, streamId));
			//channel.write(new ChunkSize(4096));
			channel.write(Control.streamBegin(streamId));
			
			final PublishType publishType = stream.getPublishType();
			
			log.info( "created publish stream : " + subscriberStream );
			
			switch( publishType )
			{
				case LIVE:
					stream.writeStreamChannel( Command.publishNotify( streamId ) );
					//stream.writeStreamChannel( Command.playStart(streamName, clientId, streamId) );
					stream.writeStreamChannel( Video.empty() );
					stream.writeStreamChannel( Audio.empty() );
					break;
			}
		}
	}
	
	private void seekResponse( final Channel channel, final Command command )
	{
		final int clientTimePosition = ((Double) command.getArg(0)).intValue();
		final Command seekNotify = Command.seekNotify(streamId, clientTimePosition, playName, clientId);
		seekNotify.getHeader().setTime(0);
		channel.write( seekNotify );
	}
	
	private void broadcast(final RTMPMessage message)
	{
		//this.stream.write( Control.streamControlStart() );
		this.stream.writeStreamChannel( message );
		//this.stream.write( Control.streamControlEnd() );
	}
	
	private RTMPMessage[] getStartMessages( final RTMPMessage message )
	{
		final List<RTMPMessage> list = new ArrayList<RTMPMessage>();
		
		return list.toArray(new RTMPMessage[list.size()]);
	}
	
	private void writeToStream( final Channel channel, final RTMPMessage message, final int streamId )
	{
		if( message.getHeader().getChannelId() > 2 )
		{
			message.getHeader().setStreamId( streamId );
		}
		
		channel.write( message );
	}
	
	private void writeToStream( final ChannelGroup channelGroup, final RTMPMessage message, final int streamId )
	{
		if( message.getHeader().getChannelId() > 2 )
		{
			message.getHeader().setStreamId( streamId );
		}
		
		channelGroup.write( message );
	}
	
	private void invoke( final String name, Object[] param )
	{
		if( this.methods == null )
		{
			methods = this.getClass().getMethods();
		}
		
		for( int i=0; i < methods.length; i++ )
		{
			if( methods[i].getName().equals( name ) )
			{
				try
				{
					methods[i].invoke( this, param );
				}
				catch (Exception e)
				{
					log.error( "Method Invoke Error : " + e );
					log.error( "Param " + Arrays.toString(param) );
				}
			}
		}
	}
	
	// =============================================================================================================
	// Flex 연동 Call Function 
	// =============================================================================================================
	
	@SuppressWarnings("rawtypes")
	public boolean onAllUserCall( Map<String, Object> $param )
	{
		Collection<Channel> list = userList.values();
		Iterator<Channel> list2 = list.iterator();
		
		String func = (String) $param.get("0");
		LinkedHashMap params = (LinkedHashMap) $param.get("1");
		
		while( list2.hasNext() )
		{
			Channel channel = list2.next();
			
			channel.write( Command.onCall( func, params.values().toArray() ) );
		}
		
		return true;
	}
	public boolean callAllUser( String $func, Object ... $param )
	{
		Collection<Channel> list = userList.values();
		Iterator<Channel> list2 = list.iterator();
		
		while( list2.hasNext() )
		{
			Channel channel = list2.next();
			
			channel.write( Command.onCall( $func, $param ) );
		}
		
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean onOtherUserCall( Map<String, Object> $param )
	{
		Collection<Channel> list = userList.values();
		Iterator<Channel> list2 = list.iterator();
		
		String func = (String) $param.get("0");
		LinkedHashMap params = (LinkedHashMap) $param.get("1");
		
		while( list2.hasNext() )
		{
			Channel channel = list2.next();
			
			if( !channel.equals( this.channel ) )
			{
				channel.write( Command.onCall( func, params.values().toArray() ) );
			}
		}
		
		return true;
	}
	protected boolean callOtherUser( String $func, Object ... $param )
	{
		Collection<Channel> list = userList.values();
		Iterator<Channel> list2 = list.iterator();
		
		while( list2.hasNext() )
		{
			Channel channel = list2.next();
			
			if( !channel.equals( this.channel ) )
			{
				channel.write( Command.onCall( $func, $param ) );
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean onUserCall( Map<String, Object> $param )
	{
		String func = (String) $param.get("0");
		String user = (String) $param.get("1");
		LinkedHashMap params = (LinkedHashMap) $param.get("2");
		
		final Channel channel = userList.get(user);
		
		channel.write( Command.onCall( func, params.values().toArray() ) );
		
		return true;
	}
	
	public boolean onDisconnect()
	{
		log.info( "정상종료 호출" );
		return true;
	}
	
}