package lib.basicFrm.rtmp.message;

import java.util.ArrayList;
import java.util.List;

import lib.basicFrm.amf.Amf0Value;
import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class MetadataAmf0 extends Metadata {        

    public MetadataAmf0(String name, Object ... data) {
        super(name, data);
    }

    public MetadataAmf0(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }

    @Override
    MessageType getMessageType() {
        return MessageType.METADATA_AMF0;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        Amf0Value.encode(out, name);
        Amf0Value.encode(out, data);
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        name = (String) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.readable()) {
            list.add(Amf0Value.decode(in));
        }
        data = list.toArray();
    }

}