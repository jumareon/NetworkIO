package lib.basicFrm.io;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;

import lib.basicFrm.io.flv.AtomFLV;
import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.rtmp.RTMPMessage;
import lib.basicFrm.utils.AnyLogger;

public class RecorderFLV implements RTMPWriter
{
	protected AnyLogger log = AnyLogger.getInstance();
	
	private final FileChannel out;
	private final int[] channelTimes = new int[RTMPHeader.MAX_CHANNEL_ID];
	private int primaryChannel = -1;
	private int lastLoggedSeconds;
	private final int seekTime;
	private final long startTime; 
	
	public static RTMPWriter recordAudio()
	{
		Date today = new Date();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd" );
		SimpleDateFormat timeFormat = new SimpleDateFormat( "HHmmss" );
		
		String path = "flvs/";
		String fileName = path + "ViewNTalk_" + dateFormat.format( today ) + "_" + timeFormat.format( today ) + ".flv";
		
		return new RecorderFLV( fileName );
	}
	
	public RecorderFLV( String $fileName )
	{
		this( 0, $fileName );
	}
	
	public RecorderFLV( int $seekTime, String $fileName )
	{
		this.seekTime = $seekTime < 0 ? 0 : $seekTime;
		this.startTime = System.currentTimeMillis();
		
		if( $fileName == null )
		{
			log.error( "Error To No FileName" );
			out = null;
			return;
		}
		
		try
		{
			File file = new File( $fileName );
			FileOutputStream output = new FileOutputStream( file );
			out = output.getChannel();
			out.write( AtomFLV.flvHeader().toByteBuffer() );
			
			log.info( "Recorde FLV Start" );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	private void logWriteProgress() {
		final int seconds = (channelTimes[primaryChannel] - seekTime) / 1000;
		if (seconds >= lastLoggedSeconds + 10) {
			log.info("write progress: " + seconds + " seconds");
			lastLoggedSeconds = seconds - (seconds % 10);
		}
	}
	
	@Override
	public void write( RTMPMessage $message )
	{
		final RTMPHeader header = $message.getHeader();
		if(header.isAggregate()) {
			final ChannelBuffer in = $message.encode();
			while (in.readable()) {
				final AtomFLV flvAtom = new AtomFLV(in);
				final int absoluteTime = flvAtom.getHeader().getTime();
				channelTimes[primaryChannel] = absoluteTime;
				write(flvAtom);
				// logger.debug("aggregate atom: {}", flvAtom);
				logWriteProgress();
			}
		} else { // METADATA / AUDIO / VIDEO
			final int channelId = header.getChannelId();
			channelTimes[channelId] = seekTime + header.getTime();
			if(primaryChannel == -1 && (header.isAudio() || header.isVideo())) {
				log.info("first media packet for channel: " + header);
				primaryChannel = channelId;
			}
			if(header.getSize() <= 2) {
				return;
			}
			write(new AtomFLV(header.getMessageType(), channelTimes[channelId], $message.encode()));
			if (channelId == primaryChannel) {
				logWriteProgress();
			}
		}
	}
	
	private void write(final AtomFLV flvAtom) {
		/*
		if( AnyLogger.isDebug )
		{
			log.debug("writing: " + flvAtom);
		}
		*/
		if(out == null)
		{
			return;
		}
		try
		{
			out.write(flvAtom.write().toByteBuffer());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close()
	{
		if(out != null) {
			try {
				out.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
