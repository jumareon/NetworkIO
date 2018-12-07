package lib.basicFrm.rtmp.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lib.basicFrm.amf.Amf0Object;
import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class Metadata extends AbstractMessage {

    protected String name;
    protected Object[] data;

    public Metadata(String name, Object... data) {
        this.name = name;
        this.data = data;
        header.setSize(encode().readableBytes());
    }

    public Metadata(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public Object getData(int index) {
        if(data == null || data.length < index + 1) {
            return null;
        }
        return data[index];
    }

    private Object getValue(String key) {
        final Map<String, Object> map = getMap(0);
        if(map == null) {
            return null;
        }
        return map.get(key);
    }

    public void setValue(String key, Object value) {
        if(data == null || data.length == 0) {
            data = new Object[]{new LinkedHashMap<String, Object>()};
        }
        if(data[0] == null) {
            data[0] = new LinkedHashMap<String, Object>();
        }
        final Map<String, Object> map = (Map) data[0];
        map.put(key, value);
    }

    public Map<String, Object> getMap(int index) {
        return (Map<String, Object>) getData(index);
    }

    public String getString(String key) {
        return (String) getValue(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) getValue(key);
    }

    public Double getDouble(String key) {
        return (Double) getValue(key);
    }

    public double getDuration() {
        if(data == null || data.length == 0) {
            return -1;
        }
        final Map<String, Object> map = getMap(0);
        if(map == null) {
            return -1;
        }
        final Object o = map.get("duration");
        if(o == null) {
            return -1;
        }
        return ((Double) o).longValue();
    }

    public void setDuration(final double duration) {
        if(data == null || data.length == 0) {
            data = new Object[] {map(pair("duration", duration))};
        }
        final Object meta = data[0];
        final Map<String, Object> map = (Map) meta;
        if(map == null) {
            data[0] = map(pair("duration", duration));
            return;
        }
        map.put("duration", duration);
    }

    //==========================================================================

    public static Metadata onPlayStatus(double duration, double bytes) {
        Map<String, Object> map = Command.onStatus(Command.OnStatus.STATUS,
                "NetStream.Play.Complete",
                pair("duration", duration),
                pair("bytes", bytes));
        return new MetadataAmf0("onPlayStatus", map);
    }

    public static Metadata rtmpSampleAccess() {
        return new MetadataAmf0("|RtmpSampleAccess", false, false);
    }

    public static Metadata dataStart() {
        return new MetadataAmf0("onStatus", object(pair("code", "NetStream.Data.Start")));
    }

    //==========================================================================

    /**
    [ (map){
        duration=112.384, moovPosition=28.0, width=640.0, height=352.0, videocodecid=avc1,
        audiocodecid=mp4a, avcprofile=100.0, avclevel=30.0, aacaot=2.0, videoframerate=29.97002997002997,
        audiosamplerate=24000.0, audiochannels=2.0, trackinfo= [
            (object){length=3369366.0, timescale=30000.0, language=eng, sampledescription=[(object){sampletype=avc1}]},
            (object){length=2697216.0, timescale=24000.0, language=eng, sampledescription=[(object){sampletype=mp4a}]}
        ]}]
    */

    //==========================================================================
    
    public static Metadata onMetaDataTest() {
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
        
        return new MetadataAmf0("onMetaData", map);
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("name: ").append(name);
        sb.append(" data: ").append(Arrays.toString(data));
        return sb.toString();
    }

}
