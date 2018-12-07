package lib.basicFrm.rtmp;

import lib.basicFrm.rtmp.RTMPDecoder.DecoderState;
import lib.basicFrm.rtmp.message.ChunkSize;
import lib.basicFrm.rtmp.message.MessageType;
import lib.basicFrm.utils.AnyLogger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

public class RTMPDecoder extends ReplayingDecoder<DecoderState>
{
	public static enum DecoderState
	{
		GET_HEADER,
		GET_PAYLOAD
	}
	
	private AnyLogger log = AnyLogger.getInstance();
	
	private RTMPHeader header;
	private int channelId;
	private ChannelBuffer payload;
	private int chunkSize = 128;
	
	private final RTMPHeader[] incompleteHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];
	private final ChannelBuffer[] incompletePayloads = new ChannelBuffer[RTMPHeader.MAX_CHANNEL_ID];
	private final RTMPHeader[] completedHeaders = new RTMPHeader[RTMPHeader.MAX_CHANNEL_ID];
	
	public RTMPDecoder()
	{
		super(DecoderState.GET_HEADER);
	}
	
	protected Object decode( final ChannelHandlerContext ctx, final Channel channel, final ChannelBuffer in, final DecoderState state )
	{
		switch(state)
		{
			case GET_HEADER:
				header = new RTMPHeader(in, incompleteHeaders);
				channelId = header.getChannelId();
				
				if(incompletePayloads[channelId] == null)
				{
					incompleteHeaders[channelId] = header;
					incompletePayloads[channelId] = ChannelBuffers.buffer(header.getSize());
				}
				
				payload = incompletePayloads[channelId];
				checkpoint(DecoderState.GET_PAYLOAD);
				
			case GET_PAYLOAD:
				final byte[] bytes = new byte[Math.min(payload.writableBytes(), chunkSize)];
				in.readBytes(bytes);
				payload.writeBytes(bytes);
				checkpoint(DecoderState.GET_HEADER);
				
				if(payload.writable())
				{
					return null;
				}
				
				incompletePayloads[channelId] = null;
				final RTMPHeader prevHeader = completedHeaders[channelId];
				
				if(!header.isValue0())
				{
					header.setTime(prevHeader.getTime() + header.getDeltaTime());
				}
				
				final RTMPMessage message = MessageType.decode(header, payload);
				
				log.trace("Decode << " + message);
				
				payload = null;
				if( header.isChunkSize() )
				{
					final ChunkSize csMessage = (ChunkSize) message;
					log.trace("decoder new chunk size : " + csMessage);
					chunkSize = csMessage.getChunkSize();
				}
				
				completedHeaders[channelId] = header;
				
				return message;
			default:
				throw new RuntimeException("unexpected decoder state: " + state);
		}
	}

}
