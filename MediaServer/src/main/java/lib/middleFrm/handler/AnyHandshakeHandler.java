package lib.middleFrm.handler;

import lib.basicFrm.rtmp.RTMPHandshake;
import lib.basicFrm.rtmp.RTMPPublisher;
import lib.basicFrm.utils.AnyLogger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class AnyHandshakeHandler extends FrameDecoder implements ChannelDownstreamHandler
{
	private AnyLogger log = AnyLogger.getInstance();
	
	private final RTMPHandshake handshake;
	private boolean handshakeDone;
	private boolean partOneDone;
	
	public AnyHandshakeHandler()
	{
		handshake = new RTMPHandshake();
	}
	@Override
	protected Object decode( ChannelHandlerContext ctx, Channel channel, ChannelBuffer in ) throws Exception
	{
		if(!partOneDone)
		{
			if( in.readableBytes() < RTMPHandshake.HANDSHAKE_SIZE + 1 )
			{
				return null;
			}
			
			handshake.decodeClient0( in.readBytes(1) );
			handshake.decodeClient1( in.readBytes(RTMPHandshake.HANDSHAKE_SIZE) );
			
			ChannelFuture future = Channels.succeededFuture( channel );
			Channels.write( ctx, future, handshake.encodeServer0() );
			Channels.write( ctx, future, handshake.encodeServer1() );
			Channels.write( ctx, future, handshake.encodeServer2() );
			
			partOneDone = true;
		}
		
		if(!handshakeDone)
		{
			if( in.readableBytes() < RTMPHandshake.HANDSHAKE_SIZE )
			{
				return null;
			}
			
			handshake.decodeClient2(in);
			handshakeDone = true;
		}
		
		return in;
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
	{
		if (!handshakeDone || !(e instanceof MessageEvent))
		{
			super.handleUpstream(ctx, e);
			return;
		}
		
		final MessageEvent me = (MessageEvent) e;
		
		if(me.getMessage() instanceof RTMPPublisher.Event)
		{
			super.handleUpstream(ctx, e);
			return;
		}
		
		final ChannelBuffer in = (ChannelBuffer) ((MessageEvent) e).getMessage();
		Channels.fireMessageReceived(ctx, in);
	}
	
	@Override
	public void handleDownstream( ChannelHandlerContext ctx, ChannelEvent e ) throws Exception
	{
		if( !handshakeDone || !(e instanceof MessageEvent) )
		{
			ctx.sendDownstream(e);
			return;
		}
		
		ctx.sendDownstream(e);
	}
}
