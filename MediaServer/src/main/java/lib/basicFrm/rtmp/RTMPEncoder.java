package lib.basicFrm.rtmp;

import lib.basicFrm.rtmp.message.ChunkSize;
import lib.basicFrm.rtmp.message.Control;
import lib.basicFrm.utils.AnyLogger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;

public class RTMPEncoder extends SimpleChannelDownstreamHandler
{
	private AnyLogger log = AnyLogger.getInstance();
	
	private int chunkSize = 128;
	private RTMPHeader[] channelPrevHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];
	
	private void clearPrevHeaders()
	{
		channelPrevHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];
	}
	
	@Override
	public void writeRequested( ChannelHandlerContext ctx, MessageEvent e ) throws Exception
	{
		Channels.write(ctx, e.getFuture(), encode((RTMPMessage) e.getMessage()));
	}
	
	public ChannelBuffer encode(final RTMPMessage message)
	{
		final ChannelBuffer in = message.encode();
		final RTMPHeader header = message.getHeader();
		
		if( header.isChunkSize() )
		{
			final ChunkSize csMessage = (ChunkSize) message;
			chunkSize = csMessage.getChunkSize();
			log.info("Change ChunkSize : " + chunkSize);
		}
		else if( header.isControl() )
		{
			final Control control = (Control) message;
			if( control.getType() == Control.Type.STREAM_BEGIN )
			{
				clearPrevHeaders();
			}
		}
		
		final int channelId = header.getChannelId();
		header.setSize( in.readableBytes() );
		final RTMPHeader prevHeader = channelPrevHeaders[channelId];
		
		if( prevHeader != null
			&& header.getStreamId() > 0
			&& header.getTime() > 0 )
		{
			if( header.getSize() == prevHeader.getSize() )
			{
				header.setHeaderType( RTMPHeader.Type.VALUE2 );
			}
			else
			{
				header.setHeaderType( RTMPHeader.Type.VALUE1 );
			}
			
			final int delta = header.getTime() - prevHeader.getTime();
			
			if( delta < 0 )
			{
				header.setDeltaTime( 0 );
			}
			else
			{
				header.setDeltaTime( delta );
			}
		}
		else
		{
			header.setHeaderType( RTMPHeader.Type.VALUE0 );
		}
		
		channelPrevHeaders[channelId] = header;
		
		log.trace( "Encode >> " + message );
		
		final ChannelBuffer out = ChannelBuffers.buffer( RTMPHeader.MAX_ENCODED_SIZE + header.getSize() + header.getSize() / chunkSize );
		
		boolean first = true;
		while( in.readable() )
		{
			final int size = Math.min( chunkSize, in.readableBytes() );
			if( first )
			{
				header.encode( out );
				first = false;
			}
			else
			{
				out.writeBytes( header.getTinyHeader() );
			}
			
			in.readBytes( out, size );
		}
		
		return out;
	}
	
	public RTMPEncoder()
	{
		
	}
}
