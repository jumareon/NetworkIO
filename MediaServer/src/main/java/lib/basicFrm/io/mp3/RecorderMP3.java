package lib.basicFrm.io.mp3;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import lib.basicFrm.io.RTMPWriter;
import lib.basicFrm.rtmp.RTMPHeader;
import lib.basicFrm.rtmp.RTMPMessage;
import lib.basicFrm.utils.AnyLogger;

import org.jboss.netty.buffer.ChannelBuffer;

public class RecorderMP3 implements RTMPWriter
{
	protected AnyLogger log = AnyLogger.getInstance();
	
	private final FileChannel out;
	private final int[] channelTimes = new int[RTMPHeader.MAX_CHANNEL_ID];
	private int primaryChannel = -1;
	private int lastLoggedSeconds;
	private final int seekTime;
	private final long startTime; 
	
	public RecorderMP3( String $fileName )
	{
		this( 0, $fileName );
	}
	
	public RecorderMP3( int $seekTime, String $fileName )
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
			out.write( AtomMP3.mp3Header().toByteBuffer() );
			
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
				final AtomMP3 mp3Atom = new AtomMP3(in);
				final int absoluteTime = mp3Atom.getHeader().getTime();
				channelTimes[primaryChannel] = absoluteTime;
				write(mp3Atom);
				// logger.debug("aggregate atom: {}", mp3Atom);
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
			write(new AtomMP3(header.getMessageType(), channelTimes[channelId], $message.encode()));
			if (channelId == primaryChannel) {
				logWriteProgress();
			}
		}
	}
	
	private void write(final AtomMP3 mp3Atom) {
		if( AnyLogger.isDebug )
		{
			log.debug("writing: " + mp3Atom);
		}
		if(out == null)
		{
			return;
		}
		try
		{
			out.write(mp3Atom.write().toByteBuffer());
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
//		if(primaryChannel == -1) {
//			log.trace("no media was written, closed file");
//			return;
//		}
//		log.info("finished in {} seconds, media duration: {} seconds (seek time: {})",
//				new Object[]{(System.currentTimeMillis() - startTime) / 1000,
//				(channelTimes[primaryChannel] - seekTime) / 1000, 
//				seekTime / 1000});
	}

}
