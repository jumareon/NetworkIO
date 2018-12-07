package lib.middleFrm.core;

import java.util.HashMap;

import lib.middleFrm.stream.ApplicationStream;

public class ApplicationAdepter
{
	private static ApplicationAdepter _Instance;
	
	public static ApplicationAdepter getInstance()
	{
		if( _Instance == null )
		{
			_Instance = new ApplicationAdepter();
		}
		
		return _Instance;
	}
	
	private HashMap<String, ApplicationStream> app = new HashMap<String, ApplicationStream>();
	
	public ApplicationAdepter()
	{
		reset();
	}
	
	public void reset()
	{
		app.clear();
	}
	
	public void addApplication( final String appName )
	{
		if( app.get(appName) == null )
		{
			app.put( appName, new ApplicationStream( appName ) );
		}
	}
	
	public ApplicationStream getApplication( final String appName )
	{
		if( app.get(appName) == null )
		{
			addApplication( appName );
		}
		
		return app.get( appName );
	}
}
