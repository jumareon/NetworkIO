import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import lib.basicFrm.utils.AnyLogger;
import lib.middleFrm.factory.AnyAdepterPipeline;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


public class AnyAdepter
{
private static final int port = 1935;
	
	public static void main(String[] args)
	{
		ChannelFactory factory = new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool() );
		
		ServerBootstrap bootstrap = new ServerBootstrap( factory );
		
		bootstrap.setPipelineFactory( new AnyAdepterPipeline() );
		
		AnyLogger log = AnyLogger.getInstance();
		
		log.info( "Start AnyMediaAdepter" );
		log.info( "OpenPort : " + port );
		
		bootstrap.setOption( "child.tcpNoDelay", true );
		bootstrap.setOption( "child.keepAlive", true );
		bootstrap.setOption( "reuseAddress", true );
		bootstrap.setOption( "tcpNoDelay", true );
		bootstrap.setOption( "keepAlive", true );
		
		bootstrap.bind( new InetSocketAddress( port ) );
		
		log.info( "Adepter Bind Complete" );
	}
}
