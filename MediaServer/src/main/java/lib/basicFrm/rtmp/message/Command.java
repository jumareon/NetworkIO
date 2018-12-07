package lib.basicFrm.rtmp.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lib.basicFrm.amf.Amf0Object;
import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class Command extends AbstractMessage {
    
    protected String name;
    protected int transactionId;
    protected Amf0Object object;
    protected Object[] args;

    public Command(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }
    
    public Command(int transactionId, String name, Amf0Object object, Object ... args) {
        this.transactionId = transactionId;
        this.name = name;
        this.object = object;
        this.args = args;
    }

    public Command(String name, Amf0Object object, Object ... args) {
        this(0, name, object, args);
    }

    public Amf0Object getObject() {
        return object;
    }

    public Object getArg(int index) {
        return args[index];
    }
    
    public Object[] getArgs()
    {
    	return args;
    }

    public int getArgCount() {
        if(args == null) {
            return 0;
        }
        return args.length;
    }

    //==========================================================================
    
    public static enum OnStatus {
        
        ERROR, STATUS, WARNING;        
        
        public static OnStatus parse(final String raw) {
            return OnStatus.valueOf(raw.substring(1).toUpperCase());
        }

        public String asString() {
            return "_" + this.name().toLowerCase();
        }
        
    }

    private static Amf0Object onStatus(final OnStatus level, final String code,
            final String description, final String details, final Pair ... pairs) {
        final Amf0Object object = object(
            pair("level", level.asString()),
            pair("code", code));
        if(description != null) {
            object.put("description", description);
        }
        if(details != null) {
            object.put("details", details);
        }
        return object(object, pairs);
    }

    private static Amf0Object onStatus(final OnStatus level, final String code,
            final String description, final Pair ... pairs) {
        return onStatus(level, code, description, null, pairs);
    }

    public static Amf0Object onStatus(final OnStatus level, final String code, final Pair ... pairs) {
        return onStatus(level, code, null, null, pairs);
    }

    //==========================================================================

    public static Command connectSuccess(int transactionId) {
        Map<String, Object> object = onStatus(OnStatus.STATUS,
            "NetConnection.Connect.Success", "Connection succeeded.",            
            pair("fmsVer", "FMS/3,5,1,516"),
            pair("capabilities", 31.0),
            pair("mode", 1.0),
            pair("audioCodecs", 2048.0),
            pair("videoCodecs", 128.0),
            pair("objectEncoding", 0.0));
        return new CommandAmf0(transactionId, "_result", null, object);
    }
    
    public static Command connectFault( String $userName )
    {
        Map<String, Object> object = onStatus(OnStatus.STATUS,
            "NetConnection.Connect.Duplication", "Connection succeeded.",            
            pair("details", $userName));
        return new CommandAmf0("onStatus", null, object);
    }

    public static Command createStream() {
        return new CommandAmf0("createStream", null);
    }

    public static Command onBWDone() {
        return new CommandAmf0("onBWDone", null);
    }
    
    public static Command onCall( final String func, Object ... values ) {
    	return new CommandAmf0( func, null, values );
    }
    
    public static Command onCall( final String func ) {
    	return new CommandAmf0( func, null );
    }
    
    public static Command onMetaDataTest( final int streamId ) {
        Amf0Object map = object(
            pair("audiochannels", 1),
            pair("audiocodecid", ".mp3"),
            pair("audiodatarate", 48),
            pair("audiodevice", "USB"),
            pair("audioinputvolume", 75),
            pair("audiosamplerate", 22050),
            pair("author", ""),
            pair("avclevel", 31),
            pair("avcprofile", 66),
            pair("copyright", ""),
            pair("creationdate", ""),
            pair("description", ""),
            pair("framerate", 24),
            pair("height", 720),
            pair("keywords", ""),
            pair("presetname", "Custom"),
            pair("rating", ""),
            pair("title", ""),
            pair("videocodecid", "avc1"),
            pair("videodatarate", 1000),
            pair("videodevice", "Logitech HD Pro Webcam C910"),
            pair("videokeyframe_frequency", 5),
            pair("width", 960)
        );
        
        Command command = new CommandAmf0("onMetaData", null, map);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command createStreamSuccess(int transactionId, int streamId) {
        return new CommandAmf0(transactionId, "_result", null, streamId);
    }

    private static Command playStatus(String code, String description, String playName, String clientId, Pair ... pairs) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Play." + code, description + " " + playName + ".",
                pair("details", playName),
                pair("clientid", clientId));
        object(status, pairs);
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        return command;
    }
    
    private static Command playStatus(String code, String description, String playName, String clientId, int streamId, Pair ... pairs) {
    	Amf0Object status = onStatus(OnStatus.STATUS,
    			"NetStream.Play." + code, description + " " + playName + ".",
    			pair("details", playName),
    			pair("clientid", clientId));
    	object(status, pairs);
    	Command command = new CommandAmf0("onStatus", null, status);
    	command.header.setChannelId(5);
    	command.header.setStreamId(streamId);
    	return command;
    }
    
    public static Command playReset(String playName, String clientId) {
    	Command command = playStatus("Reset", "Playing and resetting", playName, clientId);
    	command.header.setChannelId(4); // ?
    	return command;
    }

    public static Command playReset(String playName, String clientId, int streamId) {
        Command command = playStatus("Reset", "Playing and resetting", playName, clientId, streamId);
        command.header.setChannelId(4); // ?
        return command;
    }

    public static Command playStart(String playName, String clientId, int streamId) {
        Command play = playStatus("Start", "Started playing", playName, clientId, streamId);
        return play;
    }

    public static Command playStop(String playName, String clientId) {
        return playStatus("Stop", "Stopped playing", playName, clientId);
    }
    
    public static Command playStop(String playName, String clientId, String streamId) {
    	return playStatus("Stop", "Stopped playing", playName, clientId);
    }

    public static Command playFailed(String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.ERROR,
                "NetStream.Play.Failed", "Stream not found");
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(8);
        return command;
    }

    public static Command seekNotify(int streamId, int seekTime, String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Seek.Notify", "Seeking " + seekTime + " (stream ID: " + streamId + ").",
                pair("details", playName),
                pair("clientid", clientId));        
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        command.header.setStreamId(streamId);
        command.header.setTime(seekTime);
        return command;
    }

    public static Command pauseNotify(String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Pause.Notify", "Pausing " + playName,
                pair("details", playName),
                pair("clientid", clientId));
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        return command;
    }

    public static Command unpauseNotify(String playName, String clientId) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                "NetStream.Unpause.Notify", "Unpausing " + playName,
                pair("details", playName),
                pair("clientid", clientId));
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(5);
        return command;
    }
    
    @SuppressWarnings("unused")
	private static Command publishStatus(String code, String streamName, String clientId, Pair ... pairs) {
        Amf0Object status = onStatus(OnStatus.STATUS,
                code, null, streamName,
                pair("details", streamName),
                pair("clientid", clientId));
        object(status, pairs);
        Command command = new CommandAmf0("onStatus", null, status);
        command.header.setChannelId(8);
        return command;
    }
    
    private static Command publishStatus(String code, String streamName, String clientId, int streamId, Pair ... pairs) {
    	Amf0Object status = onStatus(OnStatus.STATUS,
    			code, null, streamName,
    			pair("details", streamName),
    			pair("clientid", clientId));
    	object(status, pairs);
    	Command command = new CommandAmf0("onStatus", null, status);
    	command.header.setChannelId(8);
    	command.header.setStreamId(streamId);
    	return command;
    }
    
    @SuppressWarnings("unused")
	private static Command publishStatus(String code, int streamId, Pair ... pairs) {
    	Amf0Object status = onStatus(OnStatus.STATUS, code);
    	Command command = new CommandAmf0("onStatus", null, status);
    	command.header.setChannelId(8);
    	command.header.setStreamId(streamId);
    	return command;
    }

    public static Command publishStart(String streamName, String clientId, int streamId) {
        return publishStatus("NetStream.Publish.Start", streamName, clientId, streamId);
    }

    public static Command unpublishSuccess(String streamName, String clientId, int streamId) {
        return publishStatus("NetStream.Unpublish.Success", streamName, clientId, streamId);
    }

    public static Command unpublish(int streamId) {
        Command command = new CommandAmf0("publish", null, false);
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command publishBadName(int streamId) {
        Command command = new CommandAmf0("onStatus", null, 
                onStatus(OnStatus.ERROR, "NetStream.Publish.BadName", "Stream already exists."));
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command publishNotify(int streamId) {
        Command command = new CommandAmf0("onStatus", null,
                onStatus(OnStatus.STATUS, "NetStream.Play.PublishNotify"));
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }

    public static Command unpublishNotify(int streamId) {
        Command command = new CommandAmf0("onStatus", null,
        		onStatus(OnStatus.STATUS, "NetStream.Play.UnpublishNotify"));
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }
    
    public static Command unpublishNotify() {
    	Command command = new CommandAmf0("onStatus", null,
    			onStatus(OnStatus.STATUS, "NetStream.Play.UnpublishNotify"));
    	command.header.setChannelId(8);
    	return command;
    }
    
    public static Command streamNotFound(int streamId)
    {
    	Command command = new CommandAmf0("onStatus", null,
    			onStatus(OnStatus.STATUS, "NetStream.Play.StreamNotFound"));
    	command.header.setChannelId(8);
    	command.header.setStreamId(streamId);
    	return command;
    }
    
    public static Command closeStream(int streamId) {
        Command command = new CommandAmf0("closeStream", null);
        command.header.setChannelId(8);
        command.header.setStreamId(streamId);
        return command;
    }



    //==========================================================================

    public String getName() {
        return name;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());        
        sb.append("name: ").append(name);
        sb.append(", transactionId: ").append(transactionId);
        sb.append(", object: ").append(object);
        sb.append(", args: ").append(Arrays.toString(args));
        return sb.toString();
    }

}
