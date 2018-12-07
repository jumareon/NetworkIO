package lib.middleFrm.factory;

import static org.jboss.netty.channel.Channels.pipeline;
import lib.basicFrm.rtmp.RTMPDecoder;
import lib.basicFrm.rtmp.RTMPEncoder;
import lib.bizFrm.handler.CustomHandler;
import lib.middleFrm.handler.AnyAdepterHandler;
import lib.middleFrm.handler.AnyHandshakeHandler;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

public class AnyAdepterPipeline implements ChannelPipelineFactory
{
	public ChannelPipeline getPipeline() throws Exception
    {
		ChannelPipeline pipeline = pipeline();
		
		pipeline.addLast("handshaker", new AnyHandshakeHandler() );
		
		pipeline.addLast("decoder", new RTMPDecoder());
		pipeline.addLast("encoder", new RTMPEncoder());
		
		pipeline.addLast("handler", new CustomHandler());
		
		return pipeline;
    }
}
