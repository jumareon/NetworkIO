package lib.basicFrm.utils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnyLogger
{
	public static boolean isDebug = true;
	public static boolean isInfo = true;
	public static boolean isError = true;
	public static boolean isTrace = false;
	
	private static AnyLogger log;
	public static AnyLogger getInstance()
	{
		if( log == null )
			log = new AnyLogger();
		
		return log;
	}
	
	private final String DEBUG = "debug";
	private final String INFO  = "info ";
	private final String ERROR = "error";
	private final String TRACE = "trace";
	
	private PrintWriter file;
	private Date today;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;
	private String fileName;
	private boolean isNewFile = false;
	
	private String date = "";
	
	public void info( String $value )
	{
		if( !isInfo )
			return;
		
		log( INFO, $value );
	}
	public void debug( Object $value )
	{
		if( !isDebug )
			return;
		
		log( DEBUG, $value.toString() );
	}
	public void debug( String $value )
	{
		if( !isDebug )
			return;
		
		log( DEBUG, $value );
	}
	public void debug( String $value, Object $value2 )
	{
		if( !isDebug )
			return;
		
		$value = $value + ", " + $value2;
		
		log( DEBUG, $value );
	}
	public void debug( String $value, Object $value2, Object $value3 )
	{
		if( !isDebug )
			return;
		
		$value = $value + ", " + $value2 + ", " + $value3;
		
		log( DEBUG, $value );
	}
	public void error( String $value )
	{
		if( !isError )
			return;
		
		log( ERROR, $value );
	}
	public void trace( String $value )
	{
		if( !isTrace )
			return;
		
		log( TRACE, $value );
	}
	
	public AnyLogger()
	{
		dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		timeFormat = new SimpleDateFormat( "HH:mm:ss" );
	}
	
	private void checkLog()
	{
		today = new Date();
		
		date = dateFormat.format( today );
		
//		String path = "log/";
//		
//		if( fileName != "AnyServer_Log_" + date + ".log" || fileName == null )
//		{
//			if( file != null )
//				file.close();
//				
//			isNewFile = true;
//		}
//		
//		fileName = "AnyServer_Log_" + date + ".log";
//		
//		try
//		{
//			if( isNewFile )
//				file = new PrintWriter( new FileWriter( path + fileName, true ), true );
//			
//			isNewFile = false;
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//		}
	}
	
	private void log( String $type, String $value )
	{
		checkLog();
		
		//file.println( "[" + date + " " + timeFormat.format(today) + "] " + $type.toUpperCase() + " " + $value );
		System.out.println( "[" + date + " " + timeFormat.format(today) + "] " + $type.toUpperCase() + " " + $value );
	}
}
