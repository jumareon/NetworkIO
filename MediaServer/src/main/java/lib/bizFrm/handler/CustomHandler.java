package lib.bizFrm.handler;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import lib.basicFrm.rtmp.message.Command;
import lib.middleFrm.handler.AnyAdepterHandler;

public class CustomHandler extends AnyAdepterHandler
{
	public boolean onTestCall01( Map<String, Object> param1 )
	{
		System.out.println( "onTestCall01 Method!! : " + param1 );
		
		return true;
	}
	public boolean onTestCall02( Map<String, Object> param2 )
	{
		System.out.println( "onTestCall02 Method!! : " + param2.get("0") );
		
		return true;
	}
	public boolean onTestCall03( Map<String, Object> param3 )
	{
		System.out.println( "onTestCall03 Method!! : " + param3 );
		
		return true;
	}
	public boolean onTestCall04( Map<String, Object> param1 )
	{
		System.out.println( "onTestCall04 Method!! : " + param1 );
		
		channel.write( Command.onCall( "onTestHandler", "1234", 123) );
		
		return true;
	}
}
